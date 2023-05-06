package nl.ordina.robotics.ssh.checks

import nl.ordina.robotics.socket.StatusLine
import nl.ordina.robotics.ssh.Cmd
import nl.ordina.robotics.ssh.SshSettings
import nl.ordina.robotics.ssh.runSshCommand

suspend fun controllerCheck(settings: SshSettings): StatusLine {
    val controllers = settings.runSshCommand(Cmd.Bluetooth.paired)

    return StatusLine(
        name = "Controller",
        success = controllers.isNotEmpty(),
        pending = false,
        message = controllers.ifEmpty { "No bluetooth devices connected." },
        actionUrl = "/actions/modal?resource=bluetooth",
        actionLabel = "Pair & Connect",
    )
}
