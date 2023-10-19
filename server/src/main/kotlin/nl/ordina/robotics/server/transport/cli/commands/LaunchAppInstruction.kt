package nl.ordina.robotics.server.transport.cli.commands

import nl.ordina.robotics.server.socket.CommandSuccess
import nl.ordina.robotics.server.socket.LaunchApp
import nl.ordina.robotics.server.socket.Message
import nl.ordina.robotics.server.transport.cli.Cmd
import nl.ordina.robotics.server.transport.cli.Instruction
import nl.ordina.robotics.server.transport.cli.InstructionExecutor
import nl.ordina.robotics.server.transport.cli.Script
import nl.ordina.robotics.server.transport.cli.ignoreFailure

class LaunchAppInstruction(
    private val command: LaunchApp,
    private val domainId: Int,
) : Script {
    override suspend fun run(execute: InstructionExecutor): Message {
        val constrainedId = domainId.coerceIn(1..100)

        val instructions = listOfNotNull(
            if (command.restart) Cmd.Ros.stop.ignoreFailure() else null,
            Cmd.Ros.sourceBash,
            Cmd.Ros.sourceLocalSetup,
            Cmd.Ros.launch(constrainedId),
        )

        val output = execute(Instruction(instructions))

        return CommandSuccess(
            command = "Command.LaunchApp",
            message = "Launched robot number $constrainedId: $output",
        )
    }
}
