package nl.ordina.robotics.server.robot

import io.github.oshai.kotlinlogging.KotlinLogging
import io.opentelemetry.instrumentation.annotations.WithSpan
import io.vertx.core.DeploymentOptions
import io.vertx.core.buffer.Buffer
import io.vertx.core.eventbus.EventBus
import io.vertx.core.eventbus.Message
import io.vertx.core.json.JsonObject
import io.vertx.kotlin.coroutines.CoroutineVerticle
import io.vertx.kotlin.coroutines.await
import io.vertx.kotlin.coroutines.setPeriodicAwait
import io.vertx.kotlin.coroutines.vertxFuture
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.decodeFromStream
import nl.ordina.robotics.server.bus.Addresses
import nl.ordina.robotics.server.socket.Command
import nl.ordina.robotics.server.socket.CreateStatusTable
import nl.ordina.robotics.server.socket.SubscribeTopic
import nl.ordina.robotics.server.socket.requestCommand
import nl.ordina.robotics.server.support.loadConfig
import java.io.ByteArrayInputStream

/**
 * Represents a Robot in the network.
 */
class RobotVerticle : CoroutineVerticle() {
    private val logger = KotlinLogging.logger {}
    private lateinit var eb: EventBus
    private lateinit var robotId: String

    override suspend fun start() {
        val config = vertx.loadConfig()
        robotId = config.getString("robot.id")

        logger.info { "Starting robot verticle with robot $robotId" }

        eb = vertx.eventBus()

        eb.consumer(Addresses.Boundary.commands(robotId)) {
            vertxFuture {
                handleWebsocketCommand(it)
            }
        }

        vertx.setPeriodicAwait(10_000) {
            updateTable()
        }
    }

    @OptIn(ExperimentalSerializationApi::class)
    private suspend fun handleWebsocketCommand(message: Message<Buffer>) {
        try {
            val command = Json.decodeFromStream<Command>(ByteArrayInputStream(message.body().bytes))
            logger.debug { "[ROBOT $robotId] Command: $command" }

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
            val it = eb.requestCommand<JsonObject>(Addresses.Transport.execute(robotId), command).await()

            if (message.replyAddress() != null) {
                logger.error { "websockets do have reply addresses" }
                message.reply(it.body())
            } else {
                logger.debug { "[ROBOT $robotId] Answer for $command: ${it.body()}" }
                eb.publish(Addresses.Robots.message(robotId), it.body())
            }
        } catch (e: Exception) {
            logger.error(e) { "Failed to execute command: ${e.message}" }
        }
    }

    private suspend fun handleTopicSubscribe(topic: String) {
        vertx.sharedData().getLocalMap<String, String>("topics").getOrPut(topic) {
            logger.debug { "Launching topic listener for $topic" }

            val topicConfig = JsonObject()
                .put("robot.id", robotId)
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
            eb.requestCommand<JsonObject>(Addresses.Transport.execute(robotId), CreateStatusTable)
                .onSuccess {
                    if (logger.isTraceEnabled()) {
                        logger.trace { "Publish StatusTable with result ${it.body()}" }
                    } else {
                        logger.debug { "Publish StatusTable" }
                    }

                    eb.publish(Addresses.Robots.message(robotId), it.body())
                }
        } catch (e: Exception) {
            logger.error(e) { "Failed to update robot state: ${e.message}" }
        }
    }
}
