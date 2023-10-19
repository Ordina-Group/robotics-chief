package nl.ordina.robotics.server.transport.cli.commands

import nl.ordina.robotics.server.socket.CommandFailure
import nl.ordina.robotics.server.socket.CommandSuccess
import nl.ordina.robotics.server.socket.ForgetWifi
import nl.ordina.robotics.server.socket.Message
import nl.ordina.robotics.server.transport.cli.Cmd
import nl.ordina.robotics.server.transport.cli.Instruction
import nl.ordina.robotics.server.transport.cli.InstructionExecutor
import nl.ordina.robotics.server.transport.cli.Script
import org.apache.sshd.common.SshException

class ForgetWifiInstruction(val command: ForgetWifi) : Script {
    override suspend fun run(execute: InstructionExecutor): Message = try {
        val output = execute(Instruction(Cmd.Networking.forgetConnection(command.ssid), withSudo = true)).resultOrError

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
