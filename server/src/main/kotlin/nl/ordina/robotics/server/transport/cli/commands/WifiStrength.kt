package nl.ordina.robotics.server.transport.cli.commands

import nl.ordina.robotics.server.socket.CommandFailure
import nl.ordina.robotics.server.socket.GetWifiInfo
import nl.ordina.robotics.server.socket.Message
import nl.ordina.robotics.server.socket.WifiInfo
import nl.ordina.robotics.server.transport.cli.Cmd
import nl.ordina.robotics.server.transport.cli.Instruction
import nl.ordina.robotics.server.transport.cli.InstructionExecutor
import nl.ordina.robotics.server.transport.cli.InstructionSet
import org.apache.sshd.common.SshException

class GetWifiInfoInstruction(val command: GetWifiInfo) : InstructionSet {
    override suspend fun run(execute: InstructionExecutor): Message = try {
        execute(Instruction(Cmd.Networking.connectionInfo))
            .resultOrError
            .parseWifiInfo()
    } catch (e: SshException) {
        CommandFailure(
            command = "Command.GetWifiInfo",
            message = e.message ?: "Unknown error connecting to wifi.",
        )
    }
}

fun String.parseWifiInfo(): WifiInfo {
    val lines = this.split('\n')
        .map { it.trim() }

    val ssid = lines.first().split("ESSID:").last().trim('"')
    val signal = lines.getOrNull(5)?.split("Signal level=")?.last()
    val rate = lines.getOrNull(2)?.split("   ")?.first()?.split("Bit Rate=")?.last()

    if (ssid.startsWith("off")) {
        return WifiInfo(
            "Not connected",
            "",
            "",
            connected = false,
            protocol = "",
            known = false,
        )
    }

    return WifiInfo(
        ssid,
        signal ?: "",
        rate ?: "",
        connected = false,
        protocol = "",
        known = false,
    )
}
