package nl.ordina.robotics.server.messaging

import org.springframework.stereotype.Component
import kotlin.reflect.full.callSuspend

@Component
class MessageRouter(
    private val registry: MessageHandlerRegistry,
) {
    suspend fun publish(topic: String, message: Envelope<*>) {
        val handler = registry.resolveHandler(message.topic)

        if (handler != null) {
            if (handler.method.isSuspend) {
                handler.method.call(message)
            } else {
                handler.method.callSuspend(message)
            }
        }
    }
}
