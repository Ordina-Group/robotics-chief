package nl.ordina.robotics.server.transport.cli.checks

import nl.ordina.robotics.server.socket.StatusLine
import nl.ordina.robotics.server.transport.cli.Cmd
import nl.ordina.robotics.server.transport.cli.Instruction
import nl.ordina.robotics.server.transport.cli.InstructionExecutor

suspend fun osCheck(execute: InstructionExecutor): StatusLine {
    val info = execute(Instruction(Cmd.Unix.osInfo))

    return StatusLine(
        name = "OS",
        message = info.resultOrError,
        success = info.success,
        pending = false,
    )
}
