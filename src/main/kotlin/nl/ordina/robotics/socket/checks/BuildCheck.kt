package nl.ordina.robotics.socket.checks

import nl.ordina.robotics.Cmd
import nl.ordina.robotics.JohnnyCableSettings
import nl.ordina.robotics.runCableCommand
import nl.ordina.robotics.socket.StatusLine

fun buildCheck(settings: JohnnyCableSettings): StatusLine {
    val projectBuilding = settings.runCableCommand("pgrep -f /usr/bin/colcon").isNotEmpty()
    val projectBuilt = !projectBuilding && !settings
        .runCableCommand(Cmd.Unix.list("${settings.workDir}/build"))
        .contains("No such file or directory")

    return StatusLine(
        name = "Build",
        success = projectBuilt,
        pending = projectBuilding,
        actionUrl = "/commands/build",
        actionLabel = "Build".onlyWhen(!projectBuilt),
    )
}
