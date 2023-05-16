package nl.ordina.robotics.ssh.commands

import nl.ordina.robotics.socket.CommandFailure
import nl.ordina.robotics.socket.CommandSuccess
import nl.ordina.robotics.socket.ForgetWifi
import nl.ordina.robotics.socket.Info
import nl.ordina.robotics.socket.Message
import nl.ordina.robotics.socket.SocketSession
import nl.ordina.robotics.socket.sendMessage
import nl.ordina.robotics.ssh.Cmd
import nl.ordina.robotics.ssh.runSshCommand
import org.apache.sshd.common.SshException

suspend fun SocketSession.forgetWifi(command: ForgetWifi): Message {
    sendMessage(Info("Forgetting wireless network ${command.ssid}..."))

    return try {
        val output = settings.runSshCommand(Cmd.Networking.forgetConnection(command.ssid))

        if (output.contains("successfully deleted")) {
            CommandSuccess(
                command = "Command.ForgetWifi",
                message = "Forgot ${command.ssid}",
            )
        } else {
            CommandFailure(command = "Command.ForgetWifi", message = output)
        }
    } catch (e: SshException) {
        CommandFailure(
            command = "Command.ForgetWifi",
            message = e.message ?: "Unknown error removing wireless network.",
        )
    }
}
