package nl.ordina.robotics.ssh.commands

import mu.KotlinLogging
import nl.ordina.robotics.socket.CommandFailure
import nl.ordina.robotics.socket.GetWifiNetworks
import nl.ordina.robotics.socket.Message
import nl.ordina.robotics.socket.SocketSession
import nl.ordina.robotics.socket.WifiInfo
import nl.ordina.robotics.socket.WifiNetworks
import nl.ordina.robotics.ssh.Cmd
import nl.ordina.robotics.ssh.runSshCommand
import org.apache.sshd.common.SshException
import kotlin.time.Duration.Companion.milliseconds

private val logger = KotlinLogging.logger {}

private val networksLineSplitter = Regex("(?<!\\\\):")

suspend fun SocketSession.getWifiNetworks(command: GetWifiNetworks): Message = try {
    val saved = settings.runSshCommand(Cmd.Networking.listStoredNetworks, timeout = 200.milliseconds).parseStoredNetworks()
    settings.runSshCommand(Cmd.Networking.listWirelessNetworks, timeout = 200.milliseconds).parseWifiNetworks(saved)
} catch (e: SshException) {
    logger.error { e }

    CommandFailure(
        command = "Command.GetWifiNetworks",
        message = e.message ?: "Unknown listing wifi networks.",
    )
}

fun String.parseStoredNetworks(): List<String> = this
    .split("\n")
    .mapNotNull { it.split(networksLineSplitter).firstOrNull() }

fun String.parseWifiNetworks(knownNetworks: List<String>): WifiNetworks = this
    .split("\n")
    .map { it.split(networksLineSplitter) }
    .groupBy { it[LinePosition.SSID.index] }
    .filterKeys { it.isNotBlank() }
    .map { (ssid, infoLines) ->
        val connectedLine = infoLines.find { it[LinePosition.Connected.index].isNotBlank() }
        val infoLine = connectedLine ?: infoLines.maxByOrNull { it[LinePosition.Strength.index] }!!

        WifiInfo(
            ssid = ssid,
            signal = infoLine[LinePosition.Strength.index],
            rate = infoLine[LinePosition.Rate.index],
            connected = infoLine[LinePosition.Connected.index].isNotBlank(),
            protocol = infoLine[LinePosition.Protocol.index],
            known = knownNetworks.contains(ssid),
        )
    }
    .let { WifiNetworks(networks = it) }

private enum class LinePosition(val index: Int) {
    Connected(0),
    SSID(2),
    Rate(5),
    Strength(7),
    Protocol(8),
}
