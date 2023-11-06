package nl.ordina.robotics.server.transport.cli

import io.github.oshai.kotlinlogging.KotlinLogging
import io.vertx.core.Future
import io.vertx.core.Handler
import io.vertx.core.eventbus.ReplyException
import io.vertx.kotlin.coroutines.CoroutineVerticle
import io.vertx.kotlin.coroutines.await
import io.vertx.kotlin.coroutines.vertxFuture
import kotlinx.serialization.json.Json
import nl.ordina.robotics.server.bus.Addresses
import nl.ordina.robotics.server.robot.RobotSettings
import nl.ordina.robotics.server.socket.Command
import nl.ordina.robotics.server.socket.GetBluetoothDevices
import nl.ordina.robotics.server.socket.GetWifiInfo
import nl.ordina.robotics.server.socket.Message
import nl.ordina.robotics.server.socket.publishMessage
import nl.ordina.robotics.server.socket.replyMessage
import nl.ordina.robotics.server.support.decodeFromVertxJsonObject
import nl.ordina.robotics.server.transport.cli.commands.toInstructionSet

class CliVerticle : CoroutineVerticle() {
    private val logger = KotlinLogging.logger {}
    private lateinit var update: Handler<Long>

    override suspend fun start() {
        val robotSettings = Json.decodeFromVertxJsonObject<RobotSettings>(config, prefix = "robot")
        val robotId = robotSettings.id

        val eb = vertx.eventBus()

        logger.info { "[ROBOT $robotId] Starting CLI verticle" }

        eb.consumer<Message>(Addresses.Network.message(robotId)) { message ->
            eb.publishMessage(Addresses.Transport.message(robotId), message.body())
        }

        val executor: InstructionExecutor = { instruction: Instruction ->
            try {
                eb
                    .request<InstructionResult>(
                        Addresses.Network.executeInstruction(robotId),
                        instruction,
                    )
                    .await()
                    .body()
                    .also { logger.trace { it } }
            } catch (e: ReplyException) {
                logger.error(e) { "Error executing instruction" }
                InstructionResult(null, error = e.message)
            }
        }

        eb.consumer<Command>(Addresses.Transport.execute(robotId)) { message ->
            val command = message.body()
            logger.trace { "[ROBOT $robotId] Preparing command $command for network" }

            val instructions = command.toInstructionSet(robotSettings)

            vertxFuture {
                instructions.run(executor)
            }.onSuccess {
                message.replyMessage(it)
            }.onFailure {
                message.fail(500, it.message)
            }
        }

        val scripts = listOf(
            GetWifiInfo().toInstructionSet(robotSettings),
            GetBluetoothDevices.toInstructionSet(robotSettings),
        )

        update = Handler {
            scripts
                .map { vertxFuture { it.run(executor) } }
                .let { Future.all(it) }
                .onSuccess { items ->
                    items
                        .list<Message>()
                        .filterNotNull()
                        .forEach {
                            eb.publishMessage(Addresses.Transport.message(robotId), it)
                        }

                    vertx.setTimer(1000, update)
                }
                .onFailure {
                    vertx.setTimer(1000, update)
                }
        }

        vertx.setTimer(1000, update)
    }
}
