package nl.ordina.robotics.server.robot

import io.github.oshai.kotlinlogging.KotlinLogging
import io.vertx.kotlin.coroutines.CoroutineVerticle
import kotlinx.serialization.json.Json
import nl.ordina.robotics.server.bus.Addresses
import nl.ordina.robotics.server.socket.Message
import nl.ordina.robotics.server.socket.SubscribeTopic
import nl.ordina.robotics.server.socket.publishMessage
import nl.ordina.robotics.server.socket.requestCommand
import nl.ordina.robotics.server.support.decodeFromVertxJsonObject

class TopicListenerVerticle : CoroutineVerticle() {
    private val logger = KotlinLogging.logger {}

    override suspend fun start() {
        val robotId = config.getString("robot.id")
        val topicId = config.getString("topic.id")

        logger.info { "Starting topic listener for robot $robotId on topic $topicId" }

        val eb = vertx.eventBus()

        eb.localConsumer(Addresses.Robots.topicMessage(robotId, topicId)) {
            logger.warn { "Received message on topic $topicId: ${it.body()}" }
            val message = Json.decodeFromVertxJsonObject<Message>(it.body())
            eb.publishMessage(Addresses.Boundary.updates(robotId), message)
        }

        eb.requestCommand<Unit>(Addresses.Robots.topicStart(robotId), SubscribeTopic(topicId))
    }
}
