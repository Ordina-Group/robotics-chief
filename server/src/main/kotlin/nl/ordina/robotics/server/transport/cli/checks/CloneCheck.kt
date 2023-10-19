package nl.ordina.robotics.server.transport.cli.checks

import nl.ordina.robotics.server.socket.StatusLine
import nl.ordina.robotics.server.transport.cli.Cmd
import nl.ordina.robotics.server.transport.cli.Instruction
import nl.ordina.robotics.server.transport.cli.InstructionExecutor

suspend fun cloneCheck(execute: InstructionExecutor): StatusLine {
    val dir = execute(Instruction(Cmd.Unix.list("."), inWorkDir = true)).resultOrError
    val projectCloned = !dir.contains("No such file or directory")
    val projectCloning = execute(Instruction(Cmd.Git.status)).resultOrError.contains("No commits yet")

    return StatusLine(
        name = "Cloned",
        success = projectCloned,
        pending = projectCloning,
        actionUrl = "/commands/clone",
        actionLabel = "Clone".onlyWhen(!projectCloned),
    )
}
