package nl.ordina.robotics.server.transport.cli.commands

import nl.ordina.robotics.server.socket.CommandFailure
import nl.ordina.robotics.server.socket.CommandSuccess
import nl.ordina.robotics.server.socket.Message
import nl.ordina.robotics.server.transport.cli.Cmd
import nl.ordina.robotics.server.transport.cli.Instruction
import nl.ordina.robotics.server.transport.cli.InstructionExecutor
import nl.ordina.robotics.server.transport.cli.Script

class ScanBluetoothInstruction : Script {
    override suspend fun run(execute: InstructionExecutor): Message = try {
        execute(Instruction(Cmd.Bluetooth.scan)) // timeout = 200.milliseconds
        CommandSuccess(
            command = "Command.ScanBluetooth",
            message = "Started scan.",
        )
    } catch (e: Exception) {
        CommandFailure(
            command = "Command.ScanBluetooth",
            message = e.message ?: "Failed to scan.",
        )
    }
}
