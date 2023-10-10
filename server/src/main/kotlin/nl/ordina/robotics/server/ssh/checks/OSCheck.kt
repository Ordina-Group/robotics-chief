package nl.ordina.robotics.server.ssh.checks

import nl.ordina.robotics.server.robot.Robot
import nl.ordina.robotics.server.socket.StatusLine
import nl.ordina.robotics.server.ssh.Cmd
import nl.ordina.robotics.server.robot.CommandExecutor

suspend fun Robot.osCheck(executor: CommandExecutor): StatusLine {
    val info = executor.executeCommand(id, Cmd.Unix.osInfo)

    return StatusLine(
        name = "OS",
        message = info,
        success = true,
        pending = false,
    )
}