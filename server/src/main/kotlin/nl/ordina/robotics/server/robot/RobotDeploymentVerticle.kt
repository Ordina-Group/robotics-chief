package nl.ordina.robotics.server.robot

import io.github.oshai.kotlinlogging.KotlinLogging
import io.vertx.core.DeploymentOptions
import io.vertx.core.Future
import io.vertx.core.Verticle
import io.vertx.core.buffer.Buffer
import io.vertx.core.json.JsonObject
import io.vertx.kotlin.core.json.get
import io.vertx.kotlin.coroutines.CoroutineVerticle
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import nl.ordina.robotics.server.network.ssh.SshConnectionVerticle
import nl.ordina.robotics.server.socket.CreateStatusTable
import nl.ordina.robotics.server.support.encodeToVertxJsonObject
import nl.ordina.robotics.server.transport.cli.CliVerticle

class RobotDeploymentVerticle : CoroutineVerticle() {
    private val logger = KotlinLogging.logger {}
    private val json = Json { encodeDefaults = true }

    private val settings = listOf(
        RobotSettings(
            id = "3",
            connection = "mock",
        ),
    )

    override suspend fun start() {
        logger.info { "Starting robot deployment verticle" }

        for (setting in settings) {
            val config = toConfigJson(setting)
            val options = DeploymentOptions().setConfig(config)

            val ss = vertx.deployVerticle(RobotStateService::class.java, options)
            val transport = vertx.deployVerticle(CliVerticle::class.java, options)
            val network = vertx.deployVerticle(networkVerticle(), options)
            val robot = vertx.deployVerticle(RobotVerticle::class.java, options)

            Future.all(ss, transport, network, robot).onSuccess {
                logger.info { "Requesting initial status table" }
                val robotAddr = it.list<String>().last()
                val message = Buffer.buffer(Json.encodeToString(CreateStatusTable))
                vertx.eventBus().request<JsonObject>(robotAddr, message)
            }
        }
    }

    private fun toConfigJson(settings: RobotSettings) = json.encodeToVertxJsonObject(settings)
        .associate { (k, v) -> "robot.$k" to v }
        .let { JsonObject(it) }

    private fun networkVerticle(): Class<out Verticle> {
        when (val connection = config.get<String>("robot.connection")) {
            "ssh" -> return SshConnectionVerticle::class.java
            else -> throw IllegalArgumentException("Unknown connection type: $connection")
        }
    }
}
