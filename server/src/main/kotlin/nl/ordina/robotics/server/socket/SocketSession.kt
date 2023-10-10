package nl.ordina.robotics.server.socket

import kotlinx.coroutines.Job
import kotlinx.coroutines.sync.Mutex

data class SocketSession(
//    val session: WebSocketSession,
    val settingsLock: Mutex = Mutex(false),
    val subscriptions: MutableMap<String, Job> = mutableMapOf(),
)
