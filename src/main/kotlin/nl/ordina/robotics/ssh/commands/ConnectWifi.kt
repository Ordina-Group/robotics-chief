package nl.ordina.robotics.ssh.commands

import nl.ordina.robotics.socket.CommandFailure
import nl.ordina.robotics.socket.CommandSuccess
import nl.ordina.robotics.socket.ConnectWifi
import nl.ordina.robotics.socket.Info
import nl.ordina.robotics.socket.Message
import nl.ordina.robotics.socket.SocketSession
import nl.ordina.robotics.socket.sendMessage
import nl.ordina.robotics.ssh.Cmd
import nl.ordina.robotics.ssh.runSshCommand
import org.apache.sshd.common.SshException

suspend fun SocketSession.connectWifi(command: ConnectWifi): Message {
    sendMessage(Info("Connecting to wifi network ${command.ssid}..."))

    return try {
        val output = if (command.password != null) {
            settings.runSshCommand(Cmd.Networking.connectWifi(command.ssid, command.password))
        } else {
            settings.runSshCommand(Cmd.Networking.activateConnection(command.ssid))
        }

        if (output.contains("successfully activated with")) {
            CommandSuccess(
                command = "Command.ConnectWifi",
                message = "Connected to ${command.ssid}",
            )
        } else {
            CommandFailure(command = "Command.ConnectWifi", message = output)
        }
    } catch (e: SshException) {
        CommandFailure(
            command = "Command.ConnectWifi",
            message = e.message ?: "Unknown error connecting to wifi.",
        )
    }
}
