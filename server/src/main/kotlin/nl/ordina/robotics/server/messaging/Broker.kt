package nl.ordina.robotics.server.messaging

import mu.KotlinLogging
import nl.ordina.robotics.server.socket.Message
import org.springframework.stereotype.Service

@Service
class Broker(
    private val registry: MessageHandlerRegistry,
) {
    private val logger = KotlinLogging.logger {}

    private val handlers = mutableMapOf<String, (Message) -> Unit>()

    /**
     * Register a handler for a topic and return a function that can be used to unregister the handler.
     */
    fun register(topic: String, handler: (Message) -> Unit): () -> Unit {
        logger.debug { "Registering handler for $topic" }

        handlers[topic] = handler

        return {
            logger.debug { "Unregistering handler for $topic" }
            handlers.remove(topic)
        }
    }

    fun publish(topic: String, message: Message) {
        logger.trace { "Publishing message to $topic" }

        handlers[topic]?.invoke(message)
    }
}
