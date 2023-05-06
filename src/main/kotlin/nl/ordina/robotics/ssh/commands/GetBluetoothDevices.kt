package nl.ordina.robotics.ssh.commands

import nl.ordina.robotics.socket.BluetoothDevices
import nl.ordina.robotics.socket.CommandFailure
import nl.ordina.robotics.socket.Device
import nl.ordina.robotics.socket.Message
import nl.ordina.robotics.socket.SocketSession
import nl.ordina.robotics.ssh.Cmd
import nl.ordina.robotics.ssh.runSshCommand

suspend fun SocketSession.getBluetoothDevices(): Message = try {
    val pairedDevices = settings.runSshCommand(Cmd.Bluetooth.paired)
        .split("\n")
        .filter { it.isNotEmpty() }
        .map { it.split(' ')[1] }

    val devices = settings.runSshCommand(Cmd.Bluetooth.list)
        .split("\n")
        .map {
            val (_, mac, name) = it.split(" ")
            val paired = pairedDevices.contains(mac)
            val connected = if (paired) {
                settings.runSshCommand(Cmd.Bluetooth.info(mac)).contains("Connected: yes")
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
