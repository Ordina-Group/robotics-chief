package nl.ordina.robotics.socket.checks

import nl.ordina.robotics.socket.StatusLine
import nl.ordina.robotics.ssh.Cmd
import nl.ordina.robotics.ssh.SshSettings
import nl.ordina.robotics.ssh.runSshCommand

fun controllerCheck(settings: SshSettings): StatusLine {
    val controllers = settings.runSshCommand(Cmd.Bluetooth.paired)
    val devices = settings.runSshCommand(Cmd.Bluetooth.list)

    return StatusLine(
        name = "Controller",
        success = controllers.isNotEmpty(),
        pending = false,
        message = controllers.ifEmpty { devices },
        actionUrl = "/commands/connect/${settings.controller}",
        actionLabel = if (controllers.isEmpty()) "Pair & Connect" else null
    )
}
