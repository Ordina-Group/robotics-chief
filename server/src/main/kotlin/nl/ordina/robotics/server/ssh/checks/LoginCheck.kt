package nl.ordina.robotics.server.ssh.checks

import nl.ordina.robotics.server.robot.CommandExecutor
import nl.ordina.robotics.server.robot.Robot
import nl.ordina.robotics.server.socket.StatusLine
import nl.ordina.robotics.server.ssh.Cmd

suspend fun Robot.loginCheck(executor: CommandExecutor): StatusLine {
    val user = executor.executeCommand(id, Cmd.Unix.userInfo)

    return StatusLine(
        name = "Login",
        message = "Logged in as $user@${settings.host}:${settings.port}",
        success = user.isNotBlank(),
        pending = false,
    )
}
