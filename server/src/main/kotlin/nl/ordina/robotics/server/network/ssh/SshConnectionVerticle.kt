package nl.ordina.robotics.server.network.ssh

import io.github.oshai.kotlinlogging.KotlinLogging
import io.opentelemetry.instrumentation.annotations.WithSpan
import io.vertx.core.eventbus.EventBus
import io.vertx.core.json.JsonObject
import io.vertx.kotlin.coroutines.CoroutineVerticle
import io.vertx.kotlin.coroutines.toReceiveChannel
import io.vertx.kotlin.coroutines.vertxFuture
import kotlinx.coroutines.launch
import nl.ordina.robotics.server.bus.Addresses
import nl.ordina.robotics.server.robot.RobotSettings
import nl.ordina.robotics.server.robot.RobotSettingsRepository
import nl.ordina.robotics.server.socket.RobotConnection
import nl.ordina.robotics.server.socket.publishMessage
import nl.ordina.robotics.server.support.loadConfig
import nl.ordina.robotics.server.transport.cli.Instruction
import nl.ordina.robotics.server.transport.cli.InstructionResult

class SshConnectionVerticle : CoroutineVerticle() {
    private val logger = KotlinLogging.logger {}

    private lateinit var eb: EventBus
    private lateinit var id: String
    private lateinit var network: SshSession
    private lateinit var robotSettings: RobotSettings

    override suspend fun start() {
        val config = vertx.loadConfig()
        id = config.getString("robot.id")

        logger.info { "Starting SSH network verticle for $id" }
        eb = vertx.eventBus()

        initializeConnection()

        val instructions = eb
            .consumer<Instruction>(Addresses.Network.executeInstruction(id))
            .toReceiveChannel(vertx)

        launch {
            for (msg in instructions) {
                try {
                    val instruction = msg.body()
                    val result = executeCommand(instruction)

                    msg.reply(result)
                } catch (e: Exception) {
                    logger.error(e) { "Error executing instruction" }
                    msg.fail(500, e.message)
                }
            }
        }

        lateinit var refresh: (Long) -> Unit

        refresh = { _ ->
            vertxFuture {
                network.tryConnect()
            }

            vertx.setTimer(200, refresh)
        }

        vertx.setTimer(200, refresh)

//        logger.debug { "Setting up SSH command listener for robot $id" }
//        eb.consumer(Addresses.Robots.commands(id)) {
//            handleInternalCommand(it)
//            eb.request<JsonObject>(Addresses.Transport.execute(id), it.body())
//        }

//        val channel = eb
//            .consumer<JsonObject>(Addresses.Robots.topicStart(id))
//            .toReceiveChannel(vertx)
//
//        for (subscribeRequest in channel) {
//            val topicId = Json.decodeCommand<SubscribeTopic>(subscribeRequest.body()).id
//
//            robot.subscribeTopic(executor, SubscribeTopic(topicId)).collect { message ->
//                logger.trace { "Publishing on topic $topicId: $message" }
//                eb.publishMessage(Addresses.Robots.topicMessage(id, topicId), message)
//            }
//        }
    }

    override suspend fun stop() {
        updateConnectionState(false)
    }

    private suspend fun connected(): Boolean {
        if (!network.connected()) {
            vertx.sharedData().getLock("ssh") {
            }
            network.tryConnect()
            if (network.connected()) {
                updateConnectionState(true)
            } else {
                updateConnectionState(false)
                logger.warn { "Failed to connect" }
            }
        }

        return network.connected()
    }

    private suspend fun executeCommand(
        instruction: Instruction,
    ): InstructionResult = network.withSession { runCommand ->
        runCommand(instruction.toInstructionString(robotSettings))
    }

    @WithSpan
    private suspend fun initializeConnection() = try {
        val repository = RobotSettingsRepository()
        robotSettings = repository.create(id, RobotSettings())
        network = SshSession(robotSettings, ::updateConnectionState)

        updateConnectionState(connected())
    } catch (e: Exception) {
        updateConnectionState(false)
        publish("Error connecting to robot $id")
        logger.error(e) { "Failed to initialize robot" }
    }

    private fun publish(message: String) {
        eb.publish(
            Addresses.Boundary.updates(id),
            JsonObject().put("message", message),
        )
    }

    private fun updateConnectionState(connected: Boolean) {
        eb.publishMessage(
            Addresses.Network.message(id),
            RobotConnection(connected),
        )
    }
}
