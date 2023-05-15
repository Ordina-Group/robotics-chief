package nl.ordina.robotics.socket

import io.ktor.websocket.send
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import mu.KotlinLogging

private val logger = KotlinLogging.logger {}

private val socketSerializer = Json {
    encodeDefaults = true
}

internal suspend fun SocketSession.sendMessage(message: Message) = try {
    session.send(socketSerializer.encodeToString(message))
} catch (e: Exception) {
    logger.error { "Exception sending message to client: ${e.message}" }
}
