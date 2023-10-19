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
import io.vertx.kotlin.coroutines.await
import io.vertx.kotlin.coroutines.setPeriodicAwait
import io.vertx.kotlin.coroutines.toReceiveChannel
import io.vertx.kotlin.coroutines.vertxFuture
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.decodeFromStream
import nl.ordina.robotics.server.bus.Addresses
import nl.ordina.robotics.server.network.ssh.SshConnectionVerticle
import nl.ordina.robotics.server.socket.Command
import nl.ordina.robotics.server.socket.CreateStatusTable
import nl.ordina.robotics.server.socket.SubscribeTopic
import nl.ordina.robotics.server.socket.requestCommand
import nl.ordina.robotics.server.support.loadConfig
import nl.ordina.robotics.server.transport.cli.CliVerticle
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

//        val messageStream = eb.consumer<Buffer>(Addresses.Boundary.commands(id))
//            .toReceiveChannel(vertx)
//            .receiveAsFlow()

        eb.consumer(Addresses.Boundary.commands(id)) {
            vertxFuture {
                handleWebsocketCommand(it)
            }
        }

        logger.info { "Starting RSS and RCS" }

        val ss = deployStateService()
        val rc = initializeRobotConnection()
        val transport = vertx.deployVerticle(CliVerticle::class.java, DeploymentOptions().setConfig(config))
        val network = vertx.deployVerticle(SshConnectionVerticle::class.java, DeploymentOptions().setConfig(config))

        Future.all(ss, rc, transport, network).onSuccess {
            logger.info { "Requesting initial status table" }
            updateTable()
        }

        vertx.setPeriodicAwait(10_000) {
            updateTable()
        }

//        messageStream.collect(::handleWebsocketCommand)
    }

    @OptIn(ExperimentalSerializationApi::class)
    private suspend fun handleWebsocketCommand(message: Message<Buffer>) {
        try {
            logger.info { "Consuming websocket command from ${message.address()}" }
            val command = Json.decodeFromStream<Command>(ByteArrayInputStream(message.body().bytes))

            if (command is SubscribeTopic) {
                handleTopicSubscribe(command.id)
            } else {
                handleRegularCommand(message, command)
            }
        } catch (e: Exception) {
            logger.error(e) { "Failed to handle websocket command: ${e.message}" }
        }
    }

    private suspend fun handleRegularCommand(message: Message<Buffer>, command: Command) {
        try {
            val it = eb.requestCommand<JsonObject>(Addresses.Transport.execute(id), command).await()

            if (message.replyAddress() != null) {
                logger.error { "websockets do have reply addresses" }
                message.reply(it.body())
            } else {
                eb.publish(Addresses.Robots.message(id), it.body())
            }
        } catch (e: Exception) {
            logger.error(e) { "Failed to execute command: ${e.message}" }
        }
    }

    private suspend fun handleTopicSubscribe(topic: String) {
        vertx.sharedData().getLocalMap<String, String>("topics").getOrPut(topic) {
            logger.debug { "Launching topic listener for $topic" }

            val topicConfig = JsonObject()
                .put("robot.id", id)
                .put("topic.id", topic)

            vertx
                .deployVerticle(
                    TopicListenerVerticle::class.java,
                    DeploymentOptions().setConfig(topicConfig),
                )
                .await()
        }
    }

    @WithSpan
    private fun updateTable() {
        try {
            eb.requestCommand<JsonObject>(Addresses.Transport.execute(id), CreateStatusTable)
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
        val connection = config.get<String>("robot.connection")
        require(connection == "ssh") {
            "Only SSH is supported"
        }

        val options = DeploymentOptions().setConfig(config)
        return vertx.deployVerticle(SshConnectionVerticle::class.java, options)
    }
}
