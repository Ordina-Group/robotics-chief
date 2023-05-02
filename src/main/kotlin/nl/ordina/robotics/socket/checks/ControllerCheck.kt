package nl.ordina.robotics.socket.checks

import nl.ordina.robotics.Cmd
import nl.ordina.robotics.JohnnyCableSettings
import nl.ordina.robotics.runCableCommand
import nl.ordina.robotics.socket.StatusLine

fun controllerCheck(settings: JohnnyCableSettings): StatusLine {
    val controllers = settings.runCableCommand(Cmd.Bluetooth.paired)
    val devices = settings.runCableCommand(Cmd.Bluetooth.list)

    return StatusLine(
        name = "Controller",
        success = controllers.isNotEmpty(),
        pending = false,
        message = controllers.ifEmpty { devices },
        actionUrl = "/commands/connect/${settings.controller}",
        actionLabel = if (controllers.isEmpty()) "Pair & Connect" else null,
    )
}
