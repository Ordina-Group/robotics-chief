package nl.ordina.robotics.socket.checks

import nl.ordina.robotics.Cmd
import nl.ordina.robotics.JohnnyCableSettings
import nl.ordina.robotics.runCableCommand
import nl.ordina.robotics.socket.StatusLine

fun loginCheck(settings: JohnnyCableSettings): StatusLine {
    val user = settings.runCableCommand(Cmd.Unix.userInfo)

    return StatusLine(
        name = "Login",
        message = "Logged in as $user@${settings.host}:${settings.port}",
        success = user.isNotBlank(),
        pending = false,
    )
}
