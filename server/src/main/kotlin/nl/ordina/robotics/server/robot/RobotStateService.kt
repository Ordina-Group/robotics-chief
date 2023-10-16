package nl.ordina.robotics.server.robot

import io.github.oshai.kotlinlogging.KotlinLogging
import io.vertx.core.eventbus.EventBus
import io.vertx.core.json.JsonObject
import io.vertx.kotlin.coroutines.CoroutineVerticle
import kotlinx.serialization.json.Json
import nl.ordina.robotics.server.socket.Message
import nl.ordina.robotics.server.socket.publishMessage
import nl.ordina.robotics.server.support.decodeFromVertxJsonObject
import nl.ordina.robotics.server.support.loadConfig
import kotlin.reflect.KClass

/**
 * Publishes messages to the event bus if their state is changed.
 * Keeps the latest state of each message type in memory for new clients.
 * Consumes: /initial_state
 * Consumes: /robots/$robotId/message
 * Produces: /robots/$robotId/updates
 */
class RobotStateService : CoroutineVerticle() {
    private val logger = KotlinLogging.logger {}
    private val stateMap = mutableMapOf<KClass<out Message>, Message>()
    private lateinit var robotId: String
    private lateinit var eb: EventBus

    override suspend fun start() {
        logger.info { "Starting robot state service" }

        val config = vertx.loadConfig()

        robotId = config.getString("robot.id")
        eb = vertx.eventBus()

        eb.consumer<JsonObject>("/initial_state") {
            if (it.body().getString("slice") == "/robots/$robotId/updates") {
                for (state in stateMap.values) {
                    eb.publishMessage("/robots/$robotId/updates", state)
                }
            }
        }

        eb.consumer("/robots/$robotId/message") {
            logger.debug { "Received message for $robotId: ${it.body()} from ${it.address()}" }
            val message = Json.decodeFromVertxJsonObject<Message>(it.body())

            if (it.replyAddress() != null) {
                TODO()
            }
            updateRobotState(message)
        }
    }

    private fun updateRobotState(state: Message) {
        if (stateMap[state::class] == state) {
            logger.debug { "State of type ${state::class} is unchanged, not publishing" }
            return
        }

        stateMap[state::class] = state

        eb.publishMessage("/robots/$robotId/updates", state)
    }
}
