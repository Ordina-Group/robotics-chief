package nl.ordina.robotics.ssh.checks

import nl.ordina.robotics.socket.StatusLine
import nl.ordina.robotics.ssh.Cmd
import nl.ordina.robotics.ssh.SshSettings
import nl.ordina.robotics.ssh.runSshCommand

fun buildCheck(settings: SshSettings): StatusLine {
    val projectBuilding = settings.runSshCommand("pgrep -f /usr/bin/colcon").isNotEmpty()
    val projectBuilt = !projectBuilding && !settings
        .runSshCommand(Cmd.Unix.list("${settings.workDir}/build"))
        .contains("No such file or directory")

    return StatusLine(
        name = "Build",
        success = projectBuilt,
        pending = projectBuilding,
        actionUrl = "/commands/build",
        actionLabel = "Build".onlyWhen(!projectBuilt),
    )
}
