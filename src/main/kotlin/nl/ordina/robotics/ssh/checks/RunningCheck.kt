package nl.ordina.robotics.ssh.checks

import nl.ordina.robotics.socket.StatusLine
import nl.ordina.robotics.ssh.Cmd
import nl.ordina.robotics.ssh.SshSettings
import nl.ordina.robotics.ssh.runSshCommand

fun runningCheck(settings: SshSettings): StatusLine {
    val running = settings.runSshCommand(Cmd.Ros.running)
    val runningParts = running.isNotEmpty()
    val runningMainProcess = running.contains(Cmd.Ros.mainCmdRunning)
    val runningMainAndController = runningMainProcess && running.split("\n").size >= 2

    return StatusLine(
        name = "Running",
        message = "PID $running",
        success = runningMainAndController,
        pending = runningParts && !runningMainAndController,
        actionUrl = if (runningParts) "/commands/restart/${settings.domainId}" else "/commands/launch/${settings.domainId}",
        actionLabel = if (runningParts) "Restart" else "Start",
    )
}
