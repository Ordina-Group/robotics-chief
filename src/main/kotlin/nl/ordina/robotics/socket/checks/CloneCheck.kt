package nl.ordina.robotics.socket.checks

import nl.ordina.robotics.Cmd
import nl.ordina.robotics.JohnnyCableSettings
import nl.ordina.robotics.runCableCommand
import nl.ordina.robotics.runInWorkDir
import nl.ordina.robotics.socket.StatusLine

fun cloneCheck(settings: JohnnyCableSettings): StatusLine {
    val dir = settings.runCableCommand(Cmd.Unix.list(settings.workDir))
    val projectCloned = !dir.contains("No such file or directory")
    val projectCloning = settings.runInWorkDir(Cmd.Git.status).contains("No commits yet")

    return StatusLine(
        name = "Cloned",
        success = projectCloned,
        pending = projectCloning,
        actionUrl = "/commands/clone",
        actionLabel = "Clone".onlyWhen(!projectCloned),
    )
}
