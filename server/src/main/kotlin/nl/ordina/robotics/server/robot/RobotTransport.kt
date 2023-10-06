package nl.ordina.robotics.server.robot

import kotlin.time.Duration

typealias CommandRunner = suspend (command: String, timeout: Duration) -> String

interface RobotTransport {
    val connected: Boolean

    fun tryConnect()

    suspend fun <T> withSession(
        settings: Settings? = null,
        block: suspend (runCommand: CommandRunner) -> T,
    ): T
}
