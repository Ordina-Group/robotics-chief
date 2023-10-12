package nl.ordina.robotics.server.robot

import io.github.oshai.kotlinlogging.KotlinLogging
import io.vertx.core.DeploymentOptions
import io.vertx.core.json.JsonObject
import io.vertx.kotlin.coroutines.CoroutineVerticle

class RobotDeploymentVerticle : CoroutineVerticle() {
    private val logger = KotlinLogging.logger {}

    private val robots = listOf("3") // listOf("1", "2", "3")

    override suspend fun start() {
        logger.info { "Starting robot deployment verticle" }

        for (robotId in robots) {
            val config = JsonObject()
                .put("robot.id", robotId)
                .put("robot.connection", "ssh")
            val options = DeploymentOptions().setConfig(config)
            vertx.deployVerticle(RobotVerticle::class.java, options)
        }
    }
}