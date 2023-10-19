package nl.ordina.robotics.server.transport.cli.checks

import nl.ordina.robotics.server.socket.StatusLine
import nl.ordina.robotics.server.transport.cli.Cmd
import nl.ordina.robotics.server.transport.cli.Instruction
import nl.ordina.robotics.server.transport.cli.InstructionExecutor

suspend fun loginCheck(execute: InstructionExecutor): StatusLine {
    val user = execute(Instruction(Cmd.Unix.userInfo))
    val hostname = execute(Instruction(Cmd.Unix.hostname)).resultOrError

    return StatusLine(
        name = "Login",
        message = "Logged in as ${user.resultOrError}@$hostname",
        success = user.success,
        pending = false,
    )
}
