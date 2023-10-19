package nl.ordina.robotics.server.transport.cli.checks

import nl.ordina.robotics.server.socket.StatusLine
import nl.ordina.robotics.server.transport.cli.Cmd
import nl.ordina.robotics.server.transport.cli.Instruction
import nl.ordina.robotics.server.transport.cli.InstructionExecutor

suspend fun revisionCheck(execute: InstructionExecutor): StatusLine {
    val projectCloned = execute(Instruction(Cmd.Unix.list("."), inWorkDir = true)).success
    val revision = execute(Instruction(Cmd.Git.revision))

    return StatusLine(
        name = "Revision",
        success = projectCloned && revision.success,
        message = if (projectCloned) revision.resultOrError else "",
        pending = false,
        commandLabel = "Pull",
    )
}
