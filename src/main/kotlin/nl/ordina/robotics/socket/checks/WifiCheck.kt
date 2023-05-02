package nl.ordina.robotics.socket.checks

import nl.ordina.robotics.Cmd
import nl.ordina.robotics.JohnnyCableSettings
import nl.ordina.robotics.runCableCommand
import nl.ordina.robotics.socket.StatusLine

fun wifiCheck(settings: JohnnyCableSettings): StatusLine {
    val addresses = settings.runCableCommand(Cmd.Networking.ipAddresses)
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
