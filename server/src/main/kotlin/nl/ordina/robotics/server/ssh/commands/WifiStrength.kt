package nl.ordina.robotics.server.ssh.commands

import nl.ordina.robotics.server.robot.Robot
import nl.ordina.robotics.server.socket.CommandFailure
import nl.ordina.robotics.server.socket.GetWifiInfo
import nl.ordina.robotics.server.socket.Message
import nl.ordina.robotics.server.socket.WifiInfo
import nl.ordina.robotics.server.ssh.Cmd
import nl.ordina.robotics.server.robot.CommandExecutor
import org.apache.sshd.common.SshException

suspend fun Robot.wifiInfo(executor: CommandExecutor, command: GetWifiInfo): Message = try {
    executor.executeCommand(id, Cmd.Networking.connectionInfo).parseWifiInfo()
} catch (e: SshException) {
    CommandFailure(
        command = "Command.ConnectWifi",
        message = e.message ?: "Unknown error connecting to wifi.",
    )
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
