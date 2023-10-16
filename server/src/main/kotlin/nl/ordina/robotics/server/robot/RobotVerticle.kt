package nl.ordina.robotics.server.robot

import io.github.oshai.kotlinlogging.KotlinLogging
import io.opentelemetry.instrumentation.annotations.WithSpan
import io.vertx.core.DeploymentOptions
import io.vertx.core.Future
import io.vertx.core.buffer.Buffer
import io.vertx.core.eventbus.EventBus
import io.vertx.core.eventbus.Message
import io.vertx.core.json.JsonObject
import io.vertx.kotlin.core.json.get
import io.vertx.kotlin.coroutines.CoroutineVerticle
import io.vertx.kotlin.coroutines.setPeriodicAwait
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.decodeFromStream
import nl.ordina.robotics.server.bus.Addresses
import nl.ordina.robotics.server.socket.Command
import nl.ordina.robotics.server.socket.CreateStatusTable
import nl.ordina.robotics.server.socket.requestCommand
import nl.ordina.robotics.server.ssh.SshConnectionVerticle
import nl.ordina.robotics.server.support.loadConfig
import java.io.ByteArrayInputStream

/**
 * Represents a Robot in the network.
 */
class RobotVerticle : CoroutineVerticle() {
    private val logger = KotlinLogging.logger {}
    private lateinit var eb: EventBus
    private lateinit var id: String

    override suspend fun start() {
        val config = vertx.loadConfig()
        id = config.getString("robot.id")

        logger.info { "Starting robot verticle with robot $id" }

        eb = vertx.eventBus()

        eb.consumer(Addresses.Robots.commands(id)) { message ->
            handleWebsocketCommand(message)
        }

        val ss = deployStateService()
        val rc = initializeRobotConnection()

        Future.all(ss, rc).onSuccess {
            logger.info { "Requesting initial status table" }
            updateTable()
        }

        vertx.setPeriodicAwait(10_000) {
            updateTable()
        }
    }

    @OptIn(ExperimentalSerializationApi::class)
    private fun handleWebsocketCommand(message: Message<Buffer>) {
        logger.info { "Consuming websocket command from ${message.address()}" }
        val command = Json.decodeFromStream<Command>(ByteArrayInputStream(message.body().bytes))

        eb.requestCommand<JsonObject>(Addresses.Robots.commandsInternal(id), command)
            .onSuccess {
                if (message.replyAddress() != null) {
                    logger.error { "websockets do have reply addresses" }
                    message.reply(it.body())
                } else {
                    eb.publish(Addresses.Robots.message(id), it.body())
                }
            }
            .onFailure {
                logger.error { "Failed to execute command: ${it.message}" }
            }
    }

    @WithSpan
    private fun updateTable() {
        try {
            eb.requestCommand<JsonObject>(Addresses.Robots.commandsInternal(id), CreateStatusTable)
                .onSuccess {
                    if (logger.isTraceEnabled()) {
                        logger.trace { "Publish StatusTable with result ${it.body()}" }
                    } else {
                        logger.debug { "Publish StatusTable" }
                    }

                    eb.publish(Addresses.Robots.message(id), it.body())
                }
        } catch (e: Exception) {
            logger.error(e) { "Failed to update robot state: ${e.message}" }
        }
    }

    private fun deployStateService(): Future<String> {
        val options = DeploymentOptions().setConfig(config)
        return vertx.deployVerticle(RobotStateService::class.java, options)
    }

    private fun initializeRobotConnection(): Future<String> {
        require(config.get<String>("robot.connection") == "ssh") {
            "Only SSH is supported"
        }

        val options = DeploymentOptions().setConfig(config)
        return vertx.deployVerticle(SshConnectionVerticle::class.java, options)
    }
}
