package nl.ordina.robotics.server.transport.cli.commands

import nl.ordina.robotics.server.socket.CommandFailure
import nl.ordina.robotics.server.socket.CommandSuccess
import nl.ordina.robotics.server.socket.ConnectWifi
import nl.ordina.robotics.server.socket.Message
import nl.ordina.robotics.server.transport.cli.Cmd
import nl.ordina.robotics.server.transport.cli.Instruction
import nl.ordina.robotics.server.transport.cli.InstructionExecutor
import nl.ordina.robotics.server.transport.cli.Script
import org.apache.sshd.common.SshException

class ConnectWifiInstruction(val command: ConnectWifi) : Script {
    override suspend fun run(execute: InstructionExecutor): Message = try {
        val connectCommand = if (command.password != null) {
            Instruction(Cmd.Networking.connectWifi(command.ssid, command.password), withSudo = true)
        } else {
            Instruction(Cmd.Networking.activateConnection(command.ssid), withSudo = true)
        }
        val output = execute(connectCommand).resultOrError

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
