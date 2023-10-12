package nl.ordina.robotics.server.ssh.commands

import nl.ordina.robotics.server.robot.CommandExecutor
import nl.ordina.robotics.server.robot.Robot
import nl.ordina.robotics.server.socket.BluetoothDevices
import nl.ordina.robotics.server.socket.CommandFailure
import nl.ordina.robotics.server.socket.Device
import nl.ordina.robotics.server.socket.Message
import nl.ordina.robotics.server.ssh.Cmd

suspend fun Robot.getBluetoothDevices(executor: CommandExecutor): Message = try {
    val pairedDevices = executor.executeCommand(id, Cmd.Bluetooth.paired)
        .split("\n")
        .filter { it.isNotEmpty() }
        .map { it.split(' ')[1] }

    val devices = executor.executeCommand(id, Cmd.Bluetooth.list)
        .split("\n")
        .map {
            val (_, mac, name) = it.split(" ")
            val paired = pairedDevices.contains(mac)
            val connected = if (paired) {
                executor.executeCommand(id, Cmd.Bluetooth.info(mac)).contains("Connected: yes")
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
        command = "GetBluetoothDevices",
        message = e.message ?: "Failed to list devices.",
    )
}
