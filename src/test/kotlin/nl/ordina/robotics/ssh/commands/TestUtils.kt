package nl.ordina.robotics.ssh.commands

import io.ktor.client.HttpClient
import io.ktor.client.plugins.websocket.DefaultClientWebSocketSession
import io.ktor.client.plugins.websocket.WebSockets
import io.ktor.client.plugins.websocket.webSocket
import io.ktor.server.testing.ApplicationTestBuilder
import io.ktor.server.testing.testApplication
import io.ktor.websocket.Frame
import io.ktor.websocket.readText
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import nl.ordina.robotics.module
import nl.ordina.robotics.socket.Command
import nl.ordina.robotics.socket.Message

fun Message.toJson(): String = Json.encodeToString(this)

fun Command.toJson(): String = Json.encodeToString(this)

suspend fun DefaultClientWebSocketSession.receiveText(): String? =
    (incoming.receive() as? Frame.Text)?.readText()

suspend fun DefaultClientWebSocketSession.receiveMessage(): Message? =
    (incoming.receive() as? Frame.Text)?.readText()?.let(Json::decodeFromString)

suspend inline fun <reified T> DefaultClientWebSocketSession.receiveMessageOfType(): T? {
    var message = receiveMessage()
    while (message !is T) {
        message = receiveMessage()
    }

    return message
}

fun withApp(block: suspend ApplicationTestBuilder.(HttpClient) -> Unit) = testApplication {
    application {
        module()
    }

    val client = createClient {
        install(WebSockets)
    }

    block(client)
}

fun withReadySocketSession(block: suspend DefaultClientWebSocketSession.() -> Unit) = withApp { client ->
    client.webSocket("/subscribe") {
        receiveText()
        receiveText()
        block()
    }
}
