package nl.ordina.robotics.server.ssh.commands

import nl.ordina.robotics.server.robot.Robot
import nl.ordina.robotics.server.socket.CommandFailure
import nl.ordina.robotics.server.socket.CommandSuccess
import nl.ordina.robotics.server.socket.Message
import nl.ordina.robotics.server.socket.ScanBluetooth
import nl.ordina.robotics.server.ssh.Cmd
import nl.ordina.robotics.server.robot.CommandExecutor
import kotlin.time.Duration.Companion.milliseconds

suspend fun Robot.scanBluetooth(executor: CommandExecutor, command: ScanBluetooth): Message = try {
    executor.executeCommand(id, Cmd.Bluetooth.scan, timeout = 200.milliseconds)
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
