package nl.ordina.robotics.server.transport.cli.checks

import nl.ordina.robotics.server.socket.StatusLine
import nl.ordina.robotics.server.transport.cli.Cmd
import nl.ordina.robotics.server.transport.cli.Instruction
import nl.ordina.robotics.server.transport.cli.InstructionExecutor

suspend fun revisionCheck(execute: InstructionExecutor): StatusLine {
    val dir = execute(Instruction(Cmd.Unix.list("."), inWorkDir = true)).resultOrError
    val projectCloned = !dir.contains("No such file or directory")
    val revision = if (projectCloned) execute(Instruction(Cmd.Git.revision)).resultOrError else ""

    return StatusLine(
        name = "Revision",
        success = revision.isNotEmpty(),
        message = revision,
        pending = false,
        actionLabel = "Pull",
    )
}
