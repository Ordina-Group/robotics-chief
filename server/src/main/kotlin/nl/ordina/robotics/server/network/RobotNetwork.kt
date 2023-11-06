package nl.ordina.robotics.server.network

import kotlinx.coroutines.flow.Flow
import nl.ordina.robotics.server.transport.cli.InstructionResult

typealias CommandRunner = suspend (command: String) -> InstructionResult

typealias CommandStreamer = suspend (command: String) -> Flow<String>

interface RobotNetwork {
    suspend fun connected(): Boolean

    suspend fun tryConnect()

    suspend fun <T> withSession(block: suspend (runCommand: CommandRunner) -> T): T

//    fun <T> streamWithSession(
//        settings: Settings? = null,
//        block: suspend (streamCommand: CommandStreamer) -> Flow<T>,
//    ): Flow<T>
}
