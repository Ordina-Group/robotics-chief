package nl.ordina.robotics.ssh.commands

import nl.ordina.robotics.socket.BluetoothConnect
import nl.ordina.robotics.socket.CommandFailure
import nl.ordina.robotics.socket.CommandSuccess
import nl.ordina.robotics.socket.Message
import nl.ordina.robotics.socket.SocketSession
import nl.ordina.robotics.ssh.Cmd
import nl.ordina.robotics.ssh.runSshCommand

suspend fun SocketSession.connectBluetooth(command: BluetoothConnect): Message = try {
    settings.runSshCommand(Cmd.Bluetooth.connect(command.mac))
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
