package nl.ordina.robotics.server

import io.github.oshai.kotlinlogging.KotlinLogging
import io.vertx.core.Vertx
import io.vertx.core.json.JsonObject
import nl.ordina.robotics.server.web.WebVerticle
import kotlin.random.Random

val logger = KotlinLogging.logger {}

fun main() {
    logger.info { "Starting server" }

    Vertx.vertx().apply {
        val eb = eventBus()

        deployVerticle(WebVerticle())

        setPeriodic(1000) {
            eb.publish(
                "/robots/3/updates",
                JsonObject().put("message", "Hello from EventBus ${Random.nextInt(10_000)}"),
            )
        }
    }
}
