package nl.ordina.robotics.server.ssh.commands

import nl.ordina.robotics.server.robot.Robot
import nl.ordina.robotics.server.socket.BluetoothConnect
import nl.ordina.robotics.server.socket.CommandFailure
import nl.ordina.robotics.server.socket.CommandSuccess
import nl.ordina.robotics.server.socket.Message
import nl.ordina.robotics.server.ssh.Cmd
import nl.ordina.robotics.server.ssh.CommandExecutor

suspend fun Robot.connectBluetooth(executor: CommandExecutor, command: BluetoothConnect): Message = try {
    executor.runSshCommand(id, Cmd.Bluetooth.connect(command.mac))
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
