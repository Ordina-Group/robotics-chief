package nl.ordina.robotics.server.ssh.commands

import nl.ordina.robotics.server.robot.Robot
import nl.ordina.robotics.server.socket.CommandFailure
import nl.ordina.robotics.server.socket.CommandSuccess
import nl.ordina.robotics.server.socket.ForgetWifi
import nl.ordina.robotics.server.socket.Message
import nl.ordina.robotics.server.ssh.Cmd
import nl.ordina.robotics.server.robot.CommandExecutor
import nl.ordina.robotics.server.ssh.withSudo
import org.apache.sshd.common.SshException

suspend fun Robot.forgetWifi(executor: CommandExecutor, command: ForgetWifi): Message {
//    sendMessage(Info("Forgetting wireless network ${command.ssid}..."))

    return try {
        val output = executor.runSshCommand(id, Cmd.Networking.forgetConnection(command.ssid).withSudo(settings.password))

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
