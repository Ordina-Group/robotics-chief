package nl.ordina.robotics.server.robot

typealias CommandRunner = suspend (command: String) -> String

interface RobotTransport {
    suspend fun connected(): Boolean

    suspend fun tryConnect()

    suspend fun <T> withSession(
        settings: Settings? = null,
        block: suspend (runCommand: CommandRunner) -> T,
    ): T
}
