package nl.ordina.robotics.ssh.commands

import nl.ordina.robotics.socket.CommandFailure
import nl.ordina.robotics.socket.GetWifiInfo
import nl.ordina.robotics.socket.Message
import nl.ordina.robotics.socket.SocketSession
import nl.ordina.robotics.socket.WifiInfo
import nl.ordina.robotics.ssh.Cmd
import nl.ordina.robotics.ssh.runSshCommand
import org.apache.sshd.common.SshException

suspend fun SocketSession.wifiInfo(command: GetWifiInfo): Message = try {
    settings.runSshCommand(Cmd.Networking.connectionInfo).parseWifiInfo()
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
    val signal = lines[5].split("Signal level=").last()
    val rate = lines[2].split("   ").first().split("Bit Rate=").last()

    return WifiInfo(
        ssid,
        signal,
        rate,
    )
}
