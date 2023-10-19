package nl.ordina.robotics.server.transport.cli.checks

import nl.ordina.robotics.server.socket.StatusLine
import nl.ordina.robotics.server.transport.cli.Cmd
import nl.ordina.robotics.server.transport.cli.Instruction
import nl.ordina.robotics.server.transport.cli.InstructionExecutor

suspend fun buildCheck(execute: InstructionExecutor): StatusLine {
    val projectBuilding = execute(Instruction("pgrep -f /usr/bin/colcon")).result.isNullOrEmpty().not()
    val projectBuilt = !projectBuilding && !execute(
        Instruction(Cmd.Unix.list("./build"), inWorkDir = true),
    ).resultOrError.contains("No such file or directory")

    return StatusLine(
        name = "Build",
        success = projectBuilt,
        pending = projectBuilding,
        actionUrl = "/commands/build",
        actionLabel = "Build".onlyWhen(!projectBuilt),
    )
}
