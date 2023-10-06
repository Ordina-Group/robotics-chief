package nl.ordina.robotics.server.ssh.checks

import nl.ordina.robotics.server.robot.Robot
import nl.ordina.robotics.server.socket.StatusLine
import nl.ordina.robotics.server.ssh.Cmd
import nl.ordina.robotics.server.robot.CommandExecutor

suspend fun Robot.revisionCheck(executor: CommandExecutor): StatusLine {
    val dir = executor.executeCommand(id, Cmd.Unix.list(settings.workDir))
    val projectCloned = !dir.contains("No such file or directory")
    val revision = if (projectCloned) executor.executeInWorkDir(id, Cmd.Git.revision) else ""

    return StatusLine(
        name = "Revision",
        success = revision.isNotEmpty(),
        message = revision,
        pending = false,
        actionLabel = "Pull",
    )
}
