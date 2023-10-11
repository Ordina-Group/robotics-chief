package nl.ordina.robotics.server.ssh.checks

import nl.ordina.robotics.server.robot.CommandExecutor
import nl.ordina.robotics.server.robot.Robot
import nl.ordina.robotics.server.socket.StatusLine
import nl.ordina.robotics.server.ssh.Cmd

suspend fun Robot.buildCheck(executor: CommandExecutor): StatusLine {
    val projectBuilding = executor.executeCommand(id, "pgrep -f /usr/bin/colcon").isNotEmpty()
    val projectBuilt = !projectBuilding && !executor
        .executeCommand(id, Cmd.Unix.list("${settings.workDir}/build"))
        .contains("No such file or directory")

    return StatusLine(
        name = "Build",
        success = projectBuilt,
        pending = projectBuilding,
        actionUrl = "/commands/build",
        actionLabel = "Build".onlyWhen(!projectBuilt),
    )
}
