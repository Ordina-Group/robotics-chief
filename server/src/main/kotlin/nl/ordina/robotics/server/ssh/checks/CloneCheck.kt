package nl.ordina.robotics.server.ssh.checks

import nl.ordina.robotics.server.robot.CommandExecutor
import nl.ordina.robotics.server.robot.Robot
import nl.ordina.robotics.server.socket.StatusLine
import nl.ordina.robotics.server.ssh.Cmd

suspend fun Robot.cloneCheck(executor: CommandExecutor): StatusLine {
    val dir = executor.executeCommand(id, Cmd.Unix.list(settings.workDir))
    val projectCloned = !dir.contains("No such file or directory")
    val projectCloning = executor.executeInWorkDir(id, Cmd.Git.status).contains("No commits yet")

    return StatusLine(
        name = "Cloned",
        success = projectCloned,
        pending = projectCloning,
        actionUrl = "/commands/clone",
        actionLabel = "Clone".onlyWhen(!projectCloned),
    )
}
