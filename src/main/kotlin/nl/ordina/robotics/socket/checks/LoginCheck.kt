package nl.ordina.robotics.socket.checks

import nl.ordina.robotics.ssh.Cmd
import nl.ordina.robotics.ssh.SshSettings
import nl.ordina.robotics.ssh.runSshCommand
import nl.ordina.robotics.socket.StatusLine

fun loginCheck(settings: SshSettings): StatusLine {
    val user = settings.runSshCommand(Cmd.Unix.userInfo)

    return StatusLine(
        name = "Login",
        message = "Logged in as $user@${settings.host}:${settings.port}",
        success = user.isNotBlank(),
        pending = false,
    )
}
