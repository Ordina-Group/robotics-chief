package nl.ordina.robotics.socket.checks

import nl.ordina.robotics.Cmd
import nl.ordina.robotics.JohnnyCableSettings
import nl.ordina.robotics.runCableCommand
import nl.ordina.robotics.socket.StatusLine

fun runningCheck(settings: JohnnyCableSettings): StatusLine {
    val running = settings.runCableCommand(Cmd.Ros.running)
    val runningParts = running.isNotEmpty()
    val runningMainProcess = running.contains(Cmd.Ros.mainCmdRunning)
    val runningMainAndController = runningMainProcess && running.split("\n").size >= 2

    return StatusLine(
        name = "Running",
        message = "PID $running",
        success = runningMainAndController,
        pending = runningParts && !runningMainAndController,
        actionUrl = if (runningParts) "/commands/restart/8" else "/commands/launch/8",
        actionLabel = if (runningParts) "Restart" else "Start",
    )
}
