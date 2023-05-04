package nl.ordina.robotics.socket.checks

import nl.ordina.robotics.ssh.Cmd
import nl.ordina.robotics.ssh.SshSettings
import nl.ordina.robotics.ssh.runSshCommand
import nl.ordina.robotics.socket.StatusLine

fun wifiCheck(settings: SshSettings): StatusLine {
    val addresses = settings.runSshCommand(Cmd.Networking.ipAddresses)
    val connected = addresses.split('\n').size > 3

    return StatusLine(
        name = "WiFi",
        success = connected,
        pending = false,
        message = addresses,
        actionUrl = "/actions/modal?resource=wifi",
        actionLabel = "Connect to wifi".onlyWhen(!connected),
    )
}
