package nl.ordina.robotics.server.network.ssh

import io.github.oshai.kotlinlogging.KotlinLogging
import io.opentelemetry.instrumentation.annotations.WithSpan
import io.vertx.core.eventbus.EventBus
import io.vertx.core.eventbus.Message
import io.vertx.core.json.JsonObject
import io.vertx.kotlin.coroutines.CoroutineVerticle
import io.vertx.kotlin.coroutines.receiveChannelHandler
import io.vertx.kotlin.coroutines.vertxFuture
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeout
import kotlinx.serialization.json.Json
import nl.ordina.robotics.server.bus.Addresses
import nl.ordina.robotics.server.robot.RobotSettings
import nl.ordina.robotics.server.socket.RobotConnection
import nl.ordina.robotics.server.socket.publishMessage
import nl.ordina.robotics.server.support.decodeFromVertxJsonObject
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

        robotSettings = fromConfigJson(config)
        network = SshSession(robotSettings, ::updateConnectionState)

        val adapter = vertx.receiveChannelHandler<Message<Instruction>>()
        eb.consumer(Addresses.Network.executeInstruction(id), adapter)

        launch {
            while (true) {
                if (!connected()) {
                    delay(100)
                    continue
                }

                val msg = adapter.receive()
                try {
                    withTimeout(10_000) {
                        try {
                            val instruction = msg.body()
                            val result = executeCommand(instruction)

                            msg.reply(result)
                        } catch (e: Exception) {
                            logger.error(e) { "Error executing instruction: ${e.message}" }
                            msg.reply(InstructionResult(null, error = e.message))
                        }
                    }
                } catch (e: TimeoutCancellationException) {
                    msg.reply(InstructionResult(null, error = e.message))
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

        initializeConnection()
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
        updateConnectionState(connected())
    } catch (e: Exception) {
        updateConnectionState(false)
        publish("Error connecting to robot $id")
        logger.error(e) { "Failed to initialize robot" }
    }

    private fun fromConfigJson(config: JsonObject): RobotSettings = config
        .filter { it.key.startsWith("robot.") }
        .associate { it.key.removePrefix("robot.") to it.value }
        .let { Json.decodeFromVertxJsonObject(JsonObject(it)) }

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
