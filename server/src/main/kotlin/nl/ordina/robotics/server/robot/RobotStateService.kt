package nl.ordina.robotics.server.robot

import io.github.oshai.kotlinlogging.KotlinLogging
import io.vertx.core.eventbus.EventBus
import io.vertx.core.json.JsonObject
import io.vertx.kotlin.coroutines.CoroutineVerticle
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.InternalSerializationApi
import kotlinx.serialization.serializer
import nl.ordina.robotics.server.bus.Addresses
import nl.ordina.robotics.server.socket.Message
import nl.ordina.robotics.server.socket.publishBoundaryMessage
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
    private val typeMap = mutableMapOf<KClass<out Message>, String>()
    private val stateMap = mutableMapOf<String, Message>()
    private lateinit var robotId: String
    private lateinit var eb: EventBus

    override suspend fun start() {
        val config = vertx.loadConfig()
        robotId = config.getString("robot.id")
        eb = vertx.eventBus()

        logger.info { "[ROBOT $robotId] Starting robot state service" }

        eb.consumer<JsonObject>(Addresses.initialSlice()) {
            val type = it.body().getString("slice")
            logger.trace { "[ROBOT $robotId] Received initial state request for $type" }

            if (type == Addresses.Boundary.updates(robotId)) {
                for ((_, state) in stateMap) {
                    try {
                        eb.publishBoundaryMessage(
                            Addresses.initialSlice(it.body().getString("subscriptionId")),
                            state,
                        )
                    } catch (e: Exception) {
                        logger.error { "Failed to send initial state $state, ${e.message}" }
                    }
                }
            }
        }

        eb.consumer<Message>(Addresses.Robots.message(robotId)) {
            logger.trace { "Received message for $robotId: ${it.body()} from ${it.address()}" }
            val message = it.body()

            if (it.replyAddress() != null) {
                TODO()
            }

            updateRobotState(message)
        }
    }

    @OptIn(InternalSerializationApi::class, ExperimentalSerializationApi::class)
    private fun updateRobotState(state: Message) {
        val type = typeMap.getOrPut(state::class) {
            val serializer = state::class.serializer()
            serializer.descriptor.serialName
        }

        if (stateMap[type] == state) {
            logger.trace { "State of type ${state::class} is unchanged, not publishing" }
            return
        }

        stateMap[type] = state

        eb.publishBoundaryMessage(Addresses.Boundary.updates(robotId), state)
    }
}
