package nl.ordina.server.ssh.commands

import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import nl.ordina.robotics.server.socket.Command
import nl.ordina.robotics.server.socket.Message

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
