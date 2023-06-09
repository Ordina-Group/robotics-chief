package nl.ordina.robotics.ssh.checks

import nl.ordina.robotics.socket.StatusLine
import nl.ordina.robotics.ssh.Cmd
import nl.ordina.robotics.ssh.SshSettings
import nl.ordina.robotics.ssh.runInWorkDir
import nl.ordina.robotics.ssh.runSshCommand

suspend fun revisionCheck(settings: SshSettings): StatusLine {
    val dir = settings.runSshCommand(Cmd.Unix.list(settings.workDir))
    val projectCloned = !dir.contains("No such file or directory")
    val revision = if (projectCloned) settings.runInWorkDir(Cmd.Git.revision) else ""

    return StatusLine(
        name = "Revision",
        success = revision.isNotEmpty(),
        message = revision,
        pending = false,
        actionLabel = "Pull",
    )
}
