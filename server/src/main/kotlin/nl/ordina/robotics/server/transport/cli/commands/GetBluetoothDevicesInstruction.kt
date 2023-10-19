package nl.ordina.robotics.server.transport.cli.commands

import nl.ordina.robotics.server.socket.BluetoothDevices
import nl.ordina.robotics.server.socket.CommandFailure
import nl.ordina.robotics.server.socket.Device
import nl.ordina.robotics.server.socket.Message
import nl.ordina.robotics.server.transport.cli.Cmd
import nl.ordina.robotics.server.transport.cli.Instruction
import nl.ordina.robotics.server.transport.cli.InstructionExecutor
import nl.ordina.robotics.server.transport.cli.InstructionSet

class GetBluetoothDevicesInstruction : InstructionSet {
    override suspend fun run(execute: InstructionExecutor): Message = try {
        val pairedDevices = execute(Instruction(Cmd.Bluetooth.paired))
            .resultOrError
            .split("\n")
            .filter { it.isNotEmpty() }
            .map { it.split(' ')[1] }

        val devices = execute(Instruction(Cmd.Bluetooth.list))
            .resultOrError
            .split("\n")
            .map {
                val (_, mac, name) = it.split(" ")
                val paired = pairedDevices.contains(mac)
                val connected = if (paired) {
                    execute(Instruction(Cmd.Bluetooth.info(mac))).resultOrError.contains("Connected: yes")
                } else {
                    false
                }

                Device(
                    name,
                    mac,
                    paired,
                    connected,
                )
            }

        BluetoothDevices(devices)
    } catch (e: Exception) {
        CommandFailure(
            command = "Command.GetBluetoothDevices",
            message = e.message ?: "Failed to list devices.",
        )
    }
}
