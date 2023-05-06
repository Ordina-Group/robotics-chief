package nl.ordina.robotics.ssh.checks

import nl.ordina.robotics.socket.StatusLine
import nl.ordina.robotics.ssh.Cmd
import nl.ordina.robotics.ssh.SshSettings
import nl.ordina.robotics.ssh.runSshCommand

suspend fun osCheck(settings: SshSettings): StatusLine {
    val info = settings.runSshCommand(Cmd.Unix.osInfo)

    return StatusLine(
        name = "OS",
        message = info,
        success = true,
        pending = false,
    )
}
