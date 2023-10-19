package nl.ordina.robotics.server.transport.cli

import kotlinx.serialization.Serializable
import nl.ordina.robotics.server.socket.Message

typealias InstructionExecutor = suspend (Instruction) -> InstructionResult

fun interface InstructionSet {
    suspend fun run(execute: InstructionExecutor): Message
}

@Serializable
data class Instruction(
    val value: String,
    val withSudo: Boolean = false,
    val inWorkDir: Boolean = false,
)

@Serializable
data class InstructionResult(
    val result: String?,
    val error: String? = null,
) {
    init {
        require(result != null || error != null) {
            "Either result or error must be set."
        }
    }

    val resultOrError: String
        get() = result ?: error!!

    val success: Boolean
        get() = error == null
}
