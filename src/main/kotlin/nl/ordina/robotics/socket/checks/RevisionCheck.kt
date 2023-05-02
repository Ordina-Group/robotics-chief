package nl.ordina.robotics.socket.checks

import nl.ordina.robotics.Cmd
import nl.ordina.robotics.JohnnyCableSettings
import nl.ordina.robotics.runCableCommand
import nl.ordina.robotics.runInWorkDir
import nl.ordina.robotics.socket.StatusLine

fun revisionCheck(settings: JohnnyCableSettings): StatusLine {
    val dir = settings.runCableCommand(Cmd.Unix.list(settings.workDir))
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
