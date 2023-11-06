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
import nl.ordina.robotics.server.socket.UpdateDomain
import nl.ordina.robotics.server.socket.publishMessage
import nl.ordina.robotics.server.socket.replyMessage
import nl.ordina.robotics.server.socket.requestCommand
import nl.ordina.robotics.server.support.loadConfig
import java.io.ByteArrayInputStream
import kotlin.time.TimeSource

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

        eb.consumer(Addresses.Transport.message(robotId)) {
            logger.trace { "[ROBOT $robotId] Received message: ${it.body()}" }
            eb.publishMessage(Addresses.Robots.message(robotId), it.body())
        }

        eb.consumer(Addresses.Boundary.commands(robotId)) {
            vertxFuture {
                handleWebsocketCommand(it)
            }
        }

        vertx.setPeriodicAwait(5_000) {
            vertx.sharedData().getLockWithTimeout("updateTable-$robotId", 200) {
                updateTable()
            }
        }
    }

    @OptIn(ExperimentalSerializationApi::class)
    private suspend fun handleWebsocketCommand(message: Message<Buffer>) {
        try {
            val command = Json.decodeFromStream<Command>(ByteArrayInputStream(message.body().bytes))
            logger.trace { "[ROBOT $robotId] Command: $command" }

            when (command) {
                is SubscribeTopic -> handleTopicSubscribe(command.id)
                is UpdateDomain -> handleDomainUpdate(command.domain)
                else -> handleRegularCommand(message, command)
            }
        } catch (e: Exception) {
            logger.error(e) { "Failed to handle websocket command: ${e.message}" }
        }
    }

    private suspend fun handleRegularCommand(message: Message<Buffer>, command: Command) {
        try {
            val it = eb.requestCommand<nl.ordina.robotics.server.socket.Message>(
                Addresses.Transport.execute(robotId),
                command,
            ).await()

            if (message.replyAddress() != null) {
                logger.error { "websockets do have reply addresses" }
                message.replyMessage(it.body())
            } else {
                logger.trace { "[ROBOT $robotId] Answer for $command: ${it.body()}" }
                eb.publishMessage(Addresses.Robots.message(robotId), it.body())
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

    private suspend fun handleDomainUpdate(domainId: Int) {
        logger.debug { "Updating domain to $domainId" }

        vertx.sharedData().getLocalMap<String, String>("domains")[robotId] = domainId.toString(10)
    }

    @WithSpan
    private fun updateTable() {
        val start = TimeSource.Monotonic.markNow()

        try {
            eb.requestCommand<nl.ordina.robotics.server.socket.Message>(Addresses.Transport.execute(robotId), CreateStatusTable)
                .onSuccess {
                    logger.trace { "Publish StatusTable with result ${it.body()}" }

                    eb.publishMessage(Addresses.Robots.message(robotId), it.body())
                    logger.debug { "TABLE SUCCESS: ${start.elapsedNow()}" }
                }
        } catch (e: Exception) {
            logger.error(e) { "Failed to update robot state: ${e.message}" }
            logger.debug { "TABLE FAIL: ${start.elapsedNow()}" }
        }
    }
}
