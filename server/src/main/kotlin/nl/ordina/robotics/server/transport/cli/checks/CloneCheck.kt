package nl.ordina.robotics.server.transport.cli.checks

import nl.ordina.robotics.server.socket.ClientAction
import nl.ordina.robotics.server.socket.StatusLine
import nl.ordina.robotics.server.transport.cli.Cmd
import nl.ordina.robotics.server.transport.cli.Instruction
import nl.ordina.robotics.server.transport.cli.InstructionExecutor

suspend fun cloneCheck(execute: InstructionExecutor): StatusLine {
    val gitStatus = execute(Instruction(Cmd.Git.status, inWorkDir = true))
    val projectCloned = gitStatus.success
    val projectCloning = gitStatus.resultOrError.contains("No commits yet")

    return StatusLine(
        name = "Cloned",
        success = gitStatus.success,
        message = gitStatus.error.orEmpty(),
        pending = projectCloning,
        commandLabel = "Clone".onlyWhen(!projectCloned),
        command = ClientAction("/commands/clone"),
    )
}
