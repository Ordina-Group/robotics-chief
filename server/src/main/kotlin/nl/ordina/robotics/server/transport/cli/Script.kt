package nl.ordina.robotics.server.transport.cli

import kotlinx.serialization.Serializable
import nl.ordina.robotics.server.robot.RobotSettings
import nl.ordina.robotics.server.socket.Message

typealias InstructionExecutor = suspend (Instruction) -> InstructionResult

fun interface Script {
    suspend fun run(execute: InstructionExecutor): Message
}

@Serializable
data class Instruction(
    val value: List<String>,
    val withSudo: Boolean = false,
    val inWorkDir: Boolean = false,
    val separator: String = Cmd.Unix.And,
) {
    constructor(
        vararg value: String,
        withSudo: Boolean = false,
        inWorkDir: Boolean = false,
        separator: String = Cmd.Unix.And,
    ) : this(value.toList(), withSudo, inWorkDir, separator)

    fun toInstructionString(robotSettings: RobotSettings): String {
        val prefixes = listOf(
            if (inWorkDir) "${Cmd.Unix.cd(robotSettings.workDir)} && " else "",
            if (withSudo) sudoPrefix(robotSettings.password) else "",
        ).joinToString(" ")

        return value.joinToString(
            separator,
            prefix = prefixes,
        )
    }
}

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
