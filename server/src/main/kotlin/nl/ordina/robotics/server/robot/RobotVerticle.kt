package nl.ordina.robotics.server.robot

import io.github.oshai.kotlinlogging.KotlinLogging
import io.vertx.core.DeploymentOptions
import io.vertx.core.buffer.Buffer
import io.vertx.core.eventbus.EventBus
import io.vertx.kotlin.core.json.get
import io.vertx.kotlin.coroutines.CoroutineVerticle
import io.vertx.kotlin.coroutines.setPeriodicAwait
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.decodeFromStream
import nl.ordina.robotics.server.socket.Command
import nl.ordina.robotics.server.socket.CreateStatusTable
import nl.ordina.robotics.server.socket.Info
import nl.ordina.robotics.server.socket.requestCommand
import nl.ordina.robotics.server.socket.publishMessage
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

    @OptIn(ExperimentalSerializationApi::class)
    override suspend fun start() {
        val config = vertx.loadConfig()
        id = config.getString("robot.id")

        logger.info { "Starting robot verticle with robot $id" }

        eb = vertx.eventBus()

        eb.consumer<Buffer>("/robots/$id/commands") {
            logger.info { "Consuming websocket command from ${it.address()}" }
            val command = Json.decodeFromStream<Command>(ByteArrayInputStream(it.body().bytes))

            eb.requestCommand<String>("/robots/$id/commands/internal", command)
                .onSuccess {
                    logger.info { "Successfully executed command $command with result ${it.body()}" }
                }
        }

        logger.info { "Publishing hello $id" }

        deployStateService()
        initializeRobotConnection()

        logger.info { "Publishing RSS test $id" }

        eb.publishMessage(
            "/robots/$id/message",
            Info("Test message through RSS"),
        )

        vertx.setPeriodicAwait(5_000) {
            try {
                eb.requestCommand<String>("/robots/$id/commands/internal", CreateStatusTable)
                    .onSuccess {
                        logger.info { "Successfully executed StatusTable with result ${it.body()}" }
                    }
            } catch (e: Exception) {
                logger.error(e) { "Failed to update robot state: ${e.message}" }
            }
        }
    }

    private fun deployStateService() {
        val options = DeploymentOptions().setConfig(config)
        vertx.deployVerticle(RobotStateService::class.java, options)
    }

    private fun initializeRobotConnection() {
        require(config.get<String>("robot.connection") == "ssh") {
            "Only SSH is supported"
        }

        val options = DeploymentOptions().setConfig(config)
        vertx.deployVerticle(SshConnectionVerticle::class.java, options)
    }
}
