package nl.ordina.robotics.server.transport.cli.commands

import nl.ordina.robotics.server.socket.BluetoothConnect
import nl.ordina.robotics.server.socket.CommandFailure
import nl.ordina.robotics.server.socket.CommandSuccess
import nl.ordina.robotics.server.socket.Message
import nl.ordina.robotics.server.transport.cli.Cmd
import nl.ordina.robotics.server.transport.cli.Instruction
import nl.ordina.robotics.server.transport.cli.InstructionExecutor
import nl.ordina.robotics.server.transport.cli.InstructionSet

class ConnectBluetoothInstruction(val command: BluetoothConnect) : InstructionSet {
    override suspend fun run(execute: InstructionExecutor): Message = try {
        execute(Instruction(Cmd.Bluetooth.connect(command.mac)))
        CommandSuccess(
            command = "Command.BluetoothConnect",
            message = "Connected device.",
        )
    } catch (e: Exception) {
        CommandFailure(
            command = "Command.BluetoothConnect",
            message = e.message ?: "Failed to connect.",
        )
    }
}
