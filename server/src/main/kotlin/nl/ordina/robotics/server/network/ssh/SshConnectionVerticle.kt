package nl.ordina.robotics.server.network.ssh

import io.github.oshai.kotlinlogging.KotlinLogging
import io.opentelemetry.instrumentation.annotations.WithSpan
import io.vertx.core.eventbus.EventBus
import io.vertx.core.json.JsonObject
import io.vertx.kotlin.coroutines.CoroutineVerticle
import io.vertx.kotlin.coroutines.vertxFuture
import kotlinx.serialization.json.Json
import nl.ordina.robotics.server.bus.Addresses
import nl.ordina.robotics.server.robot.RobotSettings
import nl.ordina.robotics.server.robot.RobotSettingsRepository
import nl.ordina.robotics.server.support.decodeFromVertxJsonObject
import nl.ordina.robotics.server.support.encodeToVertxJsonObject
import nl.ordina.robotics.server.support.loadConfig
import nl.ordina.robotics.server.transport.cli.Cmd
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

        eb.consumer(Addresses.Network.executeInstruction(id)) { msg ->
            val instruction = Json.decodeFromVertxJsonObject<Instruction>(msg.body())

            vertxFuture {
                val result = if (instruction.inWorkDir) {
                    executeInWorkDir(instruction.value)
                } else {
                    executeCommand(instruction.value)
                }

                val payload = Json.encodeToVertxJsonObject(result)

                msg.reply(payload)
            }
        }

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

    private suspend fun connected(): Boolean {
        if (!network.connected()) {
            network.tryConnect()
            if (network.connected()) {
//                updateConnectionState(true)
            } else {
                logger.warn { "Failed to connect" }
            }
        }

        return network.connected()
    }

    private suspend fun executeInWorkDir(
        vararg command: String,
        separator: String = Cmd.Unix.And,
    ): InstructionResult = executeCommand(
        Cmd.Unix.cd(robotSettings.workDir),
        *command,
        separator = separator,
    )

    private suspend fun executeCommand(
        vararg command: String,
        separator: String = Cmd.Unix.And,
    ): InstructionResult = network.withSession { runCommand ->
        runCommand(command.joinToString(separator))
    }

    @WithSpan
    private suspend fun initializeConnection() = try {
        val repository = RobotSettingsRepository()
        robotSettings = repository.create(id, RobotSettings())
        network = SshSession(robotSettings)

        publish("Robot is ${connected()}!")
//        robotStateService.updateRobotState(RobotConnection(true))
    } catch (e: Exception) {
//        robotStateService.updateRobotState(RobotConnection(false))
        publish("Error connecting to robot $id")
        logger.error(e) { "Failed to initialize robot" }
    }

    private fun publish(message: String) {
        eb.publish(
            Addresses.Boundary.updates(id),
            JsonObject().put("message", message),
        )
    }
}
