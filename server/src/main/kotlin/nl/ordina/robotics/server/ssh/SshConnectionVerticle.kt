package nl.ordina.robotics.server.ssh

import io.github.oshai.kotlinlogging.KotlinLogging
import io.opentelemetry.instrumentation.annotations.WithSpan
import io.vertx.core.eventbus.EventBus
import io.vertx.core.eventbus.Message
import io.vertx.core.json.JsonObject
import io.vertx.kotlin.coroutines.CoroutineVerticle
import io.vertx.kotlin.coroutines.vertxFuture
import kotlinx.serialization.json.Json
import nl.ordina.robotics.server.bus.Addresses
import nl.ordina.robotics.server.robot.CommandExecutor
import nl.ordina.robotics.server.robot.Robot
import nl.ordina.robotics.server.robot.RobotRepository
import nl.ordina.robotics.server.robot.Settings
import nl.ordina.robotics.server.socket.Command
import nl.ordina.robotics.server.socket.Info
import nl.ordina.robotics.server.socket.WifiNetworks
import nl.ordina.robotics.server.socket.handleCommand
import nl.ordina.robotics.server.socket.publishMessage
import nl.ordina.robotics.server.socket.replyMessage
import nl.ordina.robotics.server.support.decodeFromVertxJsonObject
import nl.ordina.robotics.server.support.loadConfig

class SshConnectionVerticle : CoroutineVerticle() {
    private val logger = KotlinLogging.logger {}

    private lateinit var eb: EventBus
    private lateinit var id: String
    private lateinit var executor: CommandExecutor
    private lateinit var robot: Robot

    override suspend fun start() {
        val config = vertx.loadConfig()
        id = config.getString("robot.id")

        logger.info { "Starting SSH connection verticle for $id" }
        eb = vertx.eventBus()

        initializeRobot()

        logger.debug { "Setting up SSH command listener for robot $id" }
        eb.consumer(Addresses.Robots.commandsInternal(id)) {
            handleInternalCommand(it)
        }
    }

    private fun handleInternalCommand(it: Message<JsonObject>) {
        logger.trace { "RECEIVED: ${it.replyAddress()}" }
        val command = Json.decodeFromVertxJsonObject<Command>(it.body())
        eb.publishMessage(Addresses.Robots.message(id), Info("Received command: $command"))

        vertxFuture {
            robot.handleCommand(executor, command)
                .also { msg ->
                    if (msg is WifiNetworks && msg.networks.size > 1) {
                        logger.warn { "Multiple networks found: ${msg.networks}" }
                    }
                }
        }.onSuccess { message ->
            if (message == null) return@onSuccess

            if (it.replyAddress() != null) {
                logger.trace { "REPLYING: ${it.replyAddress()}" }
                it.replyMessage(message)
            } else {
                eb.publishMessage(Addresses.Robots.message(id), message)
            }
        }.onFailure { exception ->
            val msg = Info("Error: ${exception.message}")
            if (it.replyAddress() != null) {
                logger.info { "REPLYING: ${it.replyAddress()}" }
                it.replyMessage(msg)
            } else {
                eb.publishMessage(Addresses.Robots.message(id), msg)
            }
        }
    }

    @WithSpan
    private suspend fun initializeRobot() = try {
        val repository = RobotRepository()
        robot = repository.create(id, Settings())
        val session = SshSession(robot.settings)
        executor = CommandExecutor(repository, session)

        publish("Robot is ${executor.connected()}!")
//        robotStateService.updateRobotState(RobotConnection(true))
    } catch (e: Exception) {
//        robotStateService.updateRobotState(RobotConnection(false))
        publish("Error connecting to robot $id")
        logger.error(e) { "Failed to initialize robot" }
    }

    private fun publish(message: String) {
        eb.publish(
            Addresses.Robots.updates(id),
            JsonObject().put("message", message),
        )
    }
}
