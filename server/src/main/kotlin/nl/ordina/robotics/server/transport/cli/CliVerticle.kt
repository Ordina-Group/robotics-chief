package nl.ordina.robotics.server.transport.cli

import io.github.oshai.kotlinlogging.KotlinLogging
import io.vertx.core.json.JsonObject
import io.vertx.kotlin.coroutines.CoroutineVerticle
import io.vertx.kotlin.coroutines.await
import io.vertx.kotlin.coroutines.vertxFuture
import kotlinx.serialization.json.Json
import nl.ordina.robotics.server.bus.Addresses
import nl.ordina.robotics.server.socket.Command
import nl.ordina.robotics.server.socket.replyMessage
import nl.ordina.robotics.server.support.decodeFromVertxJsonObject
import nl.ordina.robotics.server.support.encodeToVertxJsonObject
import nl.ordina.robotics.server.transport.cli.commands.toInstructionSet

class CliVerticle : CoroutineVerticle() {
    val logger = KotlinLogging.logger {}

    override suspend fun start() {
        val robotId = config.getString("robot.id")
        val eb = vertx.eventBus()

        logger.info { "Starting CLI verticle for robot $robotId" }

        val executor: InstructionExecutor = { instruction: Instruction ->
            val test = eb.request<JsonObject>(
                Addresses.Network.executeInstruction(robotId),
                Json.encodeToVertxJsonObject(instruction),
            ).await()

            Json.decodeFromVertxJsonObject<InstructionResult>(test.body())
                .also { logger.trace { it } }
        }

        eb.consumer(Addresses.Transport.execute(robotId)) { message ->
            logger.debug { "Preparing command for robot $robotId for network" }

            val command = Json.decodeFromVertxJsonObject<Command>(message.body())
            val instructions = command.toInstructionSet()

            vertxFuture {
                instructions.run(executor)
            }.onSuccess {
                message.replyMessage(it)
            }.onFailure {
                message.fail(500, it.message)
            }
        }
    }
}
