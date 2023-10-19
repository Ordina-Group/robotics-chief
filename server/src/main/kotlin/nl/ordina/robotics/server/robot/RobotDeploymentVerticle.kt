package nl.ordina.robotics.server.robot

import io.github.oshai.kotlinlogging.KotlinLogging
import io.vertx.core.DeploymentOptions
import io.vertx.core.json.JsonObject
import io.vertx.kotlin.coroutines.CoroutineVerticle
import kotlinx.serialization.json.Json
import nl.ordina.robotics.server.support.encodeToVertxJsonObject

class RobotDeploymentVerticle : CoroutineVerticle() {
    private val logger = KotlinLogging.logger {}
    private val json = Json { encodeDefaults = true }

    private val robots = listOf("3") // listOf("1", "2", "3")

    override suspend fun start() {
        logger.info { "Starting robot deployment verticle" }

        for (robotId in robots) {
            val config = toConfigJson(RobotSettings(robotId))
            val options = DeploymentOptions().setConfig(config)
            vertx.deployVerticle(RobotVerticle::class.java, options)
        }
    }

    private fun toConfigJson(settings: RobotSettings) = json.encodeToVertxJsonObject(settings)
        .associate { (k, v) -> "robot.$k" to v }
        .let { JsonObject(it) }
}
