package nl.ordina.robotics.server.ssh.checks

import nl.ordina.robotics.server.robot.CommandExecutor
import nl.ordina.robotics.server.robot.Robot
import nl.ordina.robotics.server.socket.StatusLine
import nl.ordina.robotics.server.ssh.Cmd

suspend fun Robot.wifiCheck(executor: CommandExecutor): StatusLine {
    val addresses = executor.executeCommand(id, Cmd.Networking.ipAddresses)
    val connected = addresses.split('\n').size > 3

    return StatusLine(
        name = "WiFi",
        success = connected,
        pending = false,
        message = addresses,
        actionUrl = "/actions/modal?resource=wifi",
        actionLabel = "Show networks",
    )
}
