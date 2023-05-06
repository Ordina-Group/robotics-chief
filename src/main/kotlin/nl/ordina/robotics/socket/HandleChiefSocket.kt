package nl.ordina.robotics.socket

import io.ktor.websocket.Frame
import io.ktor.websocket.readText
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.joinAll
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.withLock
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import nl.ordina.robotics.ssh.checks.createSshStatusTable

@OptIn(DelicateCoroutinesApi::class)
suspend fun SocketSession.handleChiefSocket() {
    sendMessage(Info("Chief says hi!"))

    sendMessage(Settings(settings))

    val broadcaster = session.launch(Dispatchers.IO) {
        var lastValue: Message? = null

        while (session.isActive && !session.incoming.isClosedForReceive) {
            val result = try {
                settingsLock.withLock {
                    createSshStatusTable(settings)
                }
            } catch (e: Exception) {
                CommandFailure("debug", e.message ?: "")
            }
            if (lastValue != result) {
                sendMessage(result)
                lastValue = result
            }
            delay(500)
        }
    }

    val listener = session.launch(Dispatchers.IO) {
        for (frame in session.incoming) {
            frame as? Frame.Text ?: continue
            val receivedText = frame.readText()
            val command = Json.decodeFromString<Command>(receivedText)
            try {
                handleCommand(command)
            } catch (e: Exception) {
                sendMessage(CommandFailure(command.name, e.message ?: "unknown failure"))
            }
        }
    }

    joinAll(broadcaster, listener)
}
