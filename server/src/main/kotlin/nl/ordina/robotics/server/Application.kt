package nl.ordina.robotics.server

import io.github.oshai.kotlinlogging.KotlinLogging
import io.vertx.core.Vertx
import io.vertx.core.VertxOptions
import io.vertx.tracing.opentelemetry.OpenTelemetryOptions
import kotlinx.serialization.json.JsonObject
import nl.ordina.robotics.server.robot.RobotDeploymentVerticle
import nl.ordina.robotics.server.socket.Command
import nl.ordina.robotics.server.socket.Message
import nl.ordina.robotics.server.support.CommandCodec
import nl.ordina.robotics.server.support.InstructionCodec
import nl.ordina.robotics.server.support.InstructionResultCodec
import nl.ordina.robotics.server.support.KotlinxSerializationCodec
import nl.ordina.robotics.server.support.MessageCodec
import nl.ordina.robotics.server.transport.cli.Instruction
import nl.ordina.robotics.server.transport.cli.InstructionResult
import nl.ordina.robotics.server.web.WebVerticle
import java.util.Properties

private val logger = KotlinLogging.logger {}

fun main() {
    logger.info { "Starting server" }
    loadProperties()

    val options = VertxOptions()
        .setTracingOptions(
//            OpenTelemetryOptions(globalOtel.openTelemetrySdk),
            OpenTelemetryOptions(),
        )

    Vertx.vertx(options).apply {
        eventBus().registerDefaultCodec(JsonObject::class.java, KotlinxSerializationCodec())
        eventBus().registerDefaultCodec(Message::class.java, MessageCodec())
        eventBus().registerDefaultCodec(Command::class.java, CommandCodec())
        eventBus().registerDefaultCodec(Instruction::class.java, InstructionCodec())
        eventBus().registerDefaultCodec(InstructionResult::class.java, InstructionResultCodec())

        deployVerticle(WebVerticle())
        deployVerticle(RobotDeploymentVerticle())
    }
}

private fun loadProperties(filename: String = "application.properties") = object {}
    .javaClass
    .classLoader
    .getResourceAsStream(filename)
    .use { inputStream ->
        Properties().apply {
            load(inputStream)
        }
    }
    .map { (key, value) ->
        System.setProperty(key.toString(), value.toString())
    }
