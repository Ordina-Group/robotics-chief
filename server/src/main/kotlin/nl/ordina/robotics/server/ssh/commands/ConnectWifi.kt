package nl.ordina.robotics.server.ssh.commands

import nl.ordina.robotics.server.robot.Robot
import nl.ordina.robotics.server.socket.CommandFailure
import nl.ordina.robotics.server.socket.CommandSuccess
import nl.ordina.robotics.server.socket.ConnectWifi
import nl.ordina.robotics.server.socket.Message
import nl.ordina.robotics.server.ssh.Cmd
import nl.ordina.robotics.server.robot.CommandExecutor
import nl.ordina.robotics.server.ssh.withSudo
import org.apache.sshd.common.SshException

suspend fun Robot.connectWifi(executor: CommandExecutor, command: ConnectWifi): Message {
//    sendMessage(Info("Connecting to wifi network ${command.ssid}..."))

    return try {
        val output = if (command.password != null) {
            executor.runSshCommand(
                id,
                Cmd.Networking.connectWifi(command.ssid, command.password).withSudo(settings.password),
            )
        } else {
            executor.runSshCommand(
                id,
                Cmd.Networking.activateConnection(command.ssid).withSudo(settings.password),
            )
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
