package nl.ordina.robotics.server.transport.cli.commands

import nl.ordina.robotics.server.socket.Command
import nl.ordina.robotics.server.socket.CommandFailure
import nl.ordina.robotics.server.socket.Message
import nl.ordina.robotics.server.transport.cli.InstructionExecutor
import nl.ordina.robotics.server.transport.cli.Script

class Unimplemented(private val command: Command) : Script {
    override suspend fun run(execute: InstructionExecutor): Message {
        return CommandFailure(command.name, "Unimplemented")
    }
}
