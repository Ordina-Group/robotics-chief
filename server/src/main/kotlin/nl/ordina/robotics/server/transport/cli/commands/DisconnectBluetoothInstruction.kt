package nl.ordina.robotics.server.transport.cli.commands

import nl.ordina.robotics.server.socket.BluetoothDisconnect
import nl.ordina.robotics.server.socket.CommandFailure
import nl.ordina.robotics.server.socket.CommandSuccess
import nl.ordina.robotics.server.socket.Message
import nl.ordina.robotics.server.transport.cli.Cmd
import nl.ordina.robotics.server.transport.cli.Instruction
import nl.ordina.robotics.server.transport.cli.InstructionExecutor
import nl.ordina.robotics.server.transport.cli.Script

class DisconnectBluetoothInstruction(val command: BluetoothDisconnect) : Script {
    override suspend fun run(execute: InstructionExecutor): Message = try {
        execute(Instruction(Cmd.Bluetooth.disconnect(command.mac)))
        CommandSuccess(
            command = "Command.BluetoothDisconnect",
            message = "Connected device.",
        )
    } catch (e: Exception) {
        CommandFailure(
            command = "Command.BluetoothDisconnect",
            message = e.message ?: "Failed to connect.",
        )
    }
}
