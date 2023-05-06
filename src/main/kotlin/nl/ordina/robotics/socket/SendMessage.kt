package nl.ordina.robotics.socket

import io.ktor.websocket.send
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

private val socketSerializer = Json {
    encodeDefaults = true
}

internal suspend fun SocketSession.sendMessage(message: Message) {
    session.send(socketSerializer.encodeToString(message))
}
