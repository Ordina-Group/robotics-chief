package nl.ordina.robotics.ssh.commands

import nl.ordina.robotics.socket.CommandFailure
import nl.ordina.robotics.socket.CommandSuccess
import nl.ordina.robotics.socket.Message
import nl.ordina.robotics.socket.ScanBluetooth
import nl.ordina.robotics.socket.SocketSession
import nl.ordina.robotics.ssh.Cmd
import nl.ordina.robotics.ssh.runSshCommand
import kotlin.time.Duration.Companion.milliseconds

suspend fun SocketSession.scanBluetooth(command: ScanBluetooth): Message = try {
    settings.runSshCommand(Cmd.Bluetooth.scan, timeout = 200.milliseconds)
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
