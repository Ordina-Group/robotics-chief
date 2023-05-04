package nl.ordina.robotics.socket.checks

import nl.ordina.robotics.socket.StatusLine
import nl.ordina.robotics.ssh.Cmd
import nl.ordina.robotics.ssh.SshSettings
import nl.ordina.robotics.ssh.runInWorkDir
import nl.ordina.robotics.ssh.runSshCommand

fun cloneCheck(settings: SshSettings): StatusLine {
    val dir = settings.runSshCommand(Cmd.Unix.list(settings.workDir))
    val projectCloned = !dir.contains("No such file or directory")
    val projectCloning = settings.runInWorkDir(Cmd.Git.status).contains("No commits yet")

    return StatusLine(
        name = "Cloned",
        success = projectCloned,
        pending = projectCloning,
        actionUrl = "/commands/clone",
        actionLabel = "Clone".onlyWhen(!projectCloned)
    )
}
