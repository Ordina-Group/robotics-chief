package nl.ordina.robotics.server.socket

import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import mu.KotlinLogging

private val logger = KotlinLogging.logger {}

private val socketSerializer = Json {
    encodeDefaults = true
}

internal fun SocketSession.sendMessage(message: Message) {
    try {
        logger.info { "Sending message to client: ${socketSerializer.encodeToString(message)}" }
//        session.sendMessage(TextMessage(socketSerializer.encodeToString(message)))
    } catch (e: Exception) {
        logger.error { "Exception sending message to client: ${e.message}" }
    }
}
