package nl.ordina.robotics.server.transport.cli.commands

import nl.ordina.robotics.server.socket.CommandFailure
import nl.ordina.robotics.server.socket.CommandSuccess
import nl.ordina.robotics.server.socket.CustomCommand
import nl.ordina.robotics.server.socket.Message
import nl.ordina.robotics.server.transport.cli.Instruction
import nl.ordina.robotics.server.transport.cli.InstructionExecutor
import nl.ordina.robotics.server.transport.cli.Script
import org.apache.sshd.common.SshException

class CustomCommandInstruction(val command: CustomCommand) : Script {
    override suspend fun run(execute: InstructionExecutor): Message = try {
        val output = execute(Instruction(command.command, withSudo = true)).resultOrError

        CommandSuccess(
            command = "Command.CustomCommand",
            message = output,
        )
    } catch (e: SshException) {
        CommandFailure(
            command = "Command.CustomCommand",
            message = e.message ?: "Unknown error running custom command.",
        )
    }
}
