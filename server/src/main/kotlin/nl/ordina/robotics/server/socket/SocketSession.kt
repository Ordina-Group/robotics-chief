package nl.ordina.robotics.server.socket

import kotlinx.coroutines.Job
import kotlinx.coroutines.sync.Mutex
import nl.ordina.robotics.server.ssh.SshSettings
import nl.ordina.robotics.server.ssh.SshSettingsLoader
import org.springframework.web.socket.WebSocketSession

data class SocketSession(
    val session: WebSocketSession,
    val settingsLock: Mutex = Mutex(false),
    var settings: SshSettings = SshSettingsLoader.load(),
    val subscriptions: MutableMap<String, Job> = mutableMapOf(),
)
