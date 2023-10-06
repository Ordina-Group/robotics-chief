package nl.ordina.robotics.server.ssh.checks

import nl.ordina.robotics.server.robot.Robot
import nl.ordina.robotics.server.socket.StatusLine
import nl.ordina.robotics.server.ssh.Cmd
import nl.ordina.robotics.server.robot.CommandExecutor

suspend fun Robot.runningCheck(executor: CommandExecutor): StatusLine {
    val running = executor.runSshCommand(id, Cmd.Ros.running)
    val runningParts = running.isNotEmpty()
    val runningMainProcess = running.contains(Cmd.Ros.mainCmdRunning)
    val runningMainAndController = runningMainProcess && running.split("\n").size >= 2

    return StatusLine(
        name = "Running",
        message = "PID $running",
        success = runningMainAndController,
        pending = runningParts && !runningMainAndController,
        actionUrl = if (runningParts) {
            "/commands/restart/${settings.domainId}"
        } else {
            "/commands/launch/${settings.domainId}"
        },
        actionLabel = if (runningParts) "Restart" else "Start",
    )
}
