package nl.ordina.robotics.socket

import io.ktor.server.websocket.DefaultWebSocketServerSession
import kotlinx.coroutines.Job
import kotlinx.coroutines.sync.Mutex
import nl.ordina.robotics.ssh.SshSettings
import nl.ordina.robotics.ssh.SshSettingsLoader

data class SocketSession(
    val session: DefaultWebSocketServerSession,
    val settingsLock: Mutex = Mutex(false),
    var settings: SshSettings = SshSettingsLoader.load(),
    val subscriptions: MutableMap<String, Job> = mutableMapOf(),
)
