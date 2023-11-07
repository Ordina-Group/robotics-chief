package nl.ordina.robotics.server.robot

import io.github.oshai.kotlinlogging.KotlinLogging
import io.vertx.core.CompositeFuture
import io.vertx.core.DeploymentOptions
import io.vertx.core.Future
import io.vertx.core.Verticle
import io.vertx.core.buffer.Buffer
import io.vertx.core.eventbus.ReplyException
import io.vertx.core.json.JsonObject
import io.vertx.kotlin.core.json.get
import io.vertx.kotlin.coroutines.CoroutineVerticle
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.decodeFromStream
import nl.ordina.robotics.server.bus.Addresses
import nl.ordina.robotics.server.network.mock.MockNetworkVerticle
import nl.ordina.robotics.server.network.ssh.SshConnectionVerticle
import nl.ordina.robotics.server.socket.ChiefSettings
import nl.ordina.robotics.server.socket.Command
import nl.ordina.robotics.server.socket.CreateStatusTable
import nl.ordina.robotics.server.socket.UpdateDomain
import nl.ordina.robotics.server.socket.UpdateHost
import nl.ordina.robotics.server.socket.publishBoundaryMessage
import nl.ordina.robotics.server.support.encodeToVertxJsonObject
import nl.ordina.robotics.server.transport.cli.CliVerticle
import java.io.ByteArrayInputStream

class RobotDeploymentVerticle : CoroutineVerticle() {
    private val logger = KotlinLogging.logger {}
    private val json = Json { encodeDefaults = true }

    private val robotSettings = mutableListOf(
        RobotSettings(
            id = "3",
            connection = "mock",
        ),
        RobotSettings(
            id = "4",
            connection = "ssh",
        ),
    )
    private val robotDeployments: MutableList<List<String>> = robotSettings.map { emptyList<String>() }.toMutableList()

    @OptIn(ExperimentalSerializationApi::class)
    override suspend fun start() {
        logger.info { "Starting robot deployment verticle" }
        val eb = vertx.eventBus()

        robotSettings.forEachIndexed { index, settings ->
            deployRobot(settings)
                .onSuccess { robotDeployments[index] = it }
                .onFailure {
                    logger.error { "Failed to deploy robot: ${it.message}" }
                }
        }

        eb.consumer<JsonObject>(Addresses.initialSlice()) {
            val type = it.body().getString("slice")

            if (type == "/chief/settings") {
                logger.trace { "[CHIEF] Received initial state request for settings" }

                try {
                    eb.publishBoundaryMessage(
                        Addresses.initialSlice(it.body().getString("subscriptionId")),
                        ChiefSettings(robotSettings),
                    )
                } catch (e: Exception) {
                    logger.error { "Failed to send initial settings; ${e.message}" }
                }
            }
        }

        eb.consumer<Buffer>(Addresses.Chief.command()) {
            println("Received chief command: ${it.body()}")
            when (val command = Json.decodeFromStream<Command>(ByteArrayInputStream(it.body().bytes))) {
                is UpdateDomain -> updateAndRedeploy(
                    command.robotId,
                    robotSettings.first { s -> s.id == command.robotId }.copy(domainId = command.domain),
                )
                is UpdateHost -> updateAndRedeploy(
                    command.robotId,
                    robotSettings.first { s -> s.id == command.robotId }.copy(host = command.host),
                )

                else -> logger.warn { "Unknown chief command: ${it::class.simpleName}" }
            }
        }
    }

    private fun updateAndRedeploy(robotId: String, settings: RobotSettings) {
        val index = robotSettings.indexOfFirst { it.id == robotId }
        robotSettings[index] = settings

        undeployRobot(robotId)
            .onSuccess {
                deployRobot(settings)
                    .onSuccess { robotDeployments[index] = it }
                    .onFailure {
                        logger.error { "Failed to deploy robot: ${it.message}" }
                    }
            }
            .onFailure {
                logger.error { "Failed to undeploy robot: ${it.message}" }
            }
    }

    private fun undeployRobot(robotId: String): CompositeFuture {
        val index = robotSettings.indexOfFirst { it.id == robotId }

        val undeployments = robotDeployments[index].map { vertx.undeploy(it) }

        return Future
            .all(undeployments)
            .onSuccess { robotDeployments[index] = emptyList() }
    }

    private fun deployRobot(settings: RobotSettings): Future<List<String>> {
        val config = toConfigJson(settings)
        val options = DeploymentOptions().setConfig(config)

        val ss = vertx.deployVerticle(RobotStateService::class.java, options)
        val transport = vertx.deployVerticle(CliVerticle::class.java, options)
        val network = vertx.deployVerticle(networkVerticle(options), options)
        val robot = vertx.deployVerticle(RobotVerticle::class.java, options)

        return Future.future { resultFuture ->
            Future.all(ss, transport, network, robot).onSuccess {
                logger.info { "Requesting initial status table" }
                val addresses = it.list<String>()
                val robotAddr = addresses.last()
                val message = Buffer.buffer(Json.encodeToString(CreateStatusTable))
                try {
                    vertx.eventBus().request<JsonObject>(robotAddr, message)
                } catch (e: ReplyException) {
                    logger.error(e) { "Error requesting initial status table" }
                }

                resultFuture.complete(addresses)
            }.onFailure { resultFuture.fail(it) }
        }
    }

    private fun toConfigJson(settings: RobotSettings) = json.encodeToVertxJsonObject(settings)
        .associate { (k, v) -> "robot.$k" to v }
        .let { JsonObject(it) }

    private fun networkVerticle(options: DeploymentOptions): Class<out Verticle> =
        when (val connection = options.config.get<String>("robot.connection")) {
            "ssh" -> SshConnectionVerticle::class.java
            "mock" -> MockNetworkVerticle::class.java
            else -> throw IllegalArgumentException("Unknown connection type: $connection")
        }
}
