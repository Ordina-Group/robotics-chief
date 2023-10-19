package nl.ordina.robotics.server.transport.cli.commands

import io.github.oshai.kotlinlogging.KotlinLogging
import nl.ordina.robotics.server.socket.CommandFailure
import nl.ordina.robotics.server.socket.Message
import nl.ordina.robotics.server.socket.WifiInfo
import nl.ordina.robotics.server.socket.WifiNetworks
import nl.ordina.robotics.server.transport.cli.Cmd
import nl.ordina.robotics.server.transport.cli.Instruction
import nl.ordina.robotics.server.transport.cli.InstructionExecutor
import nl.ordina.robotics.server.transport.cli.InstructionSet
import nl.ordina.robotics.server.transport.cli.withSudo
import org.apache.sshd.common.SshException

private val logger = KotlinLogging.logger {}

private val networksLineSplitter = Regex("(?<!\\\\):")

class GetWifiNetworksInstruction : InstructionSet {
    override suspend fun run(execute: InstructionExecutor): Message = try {
        val saved = execute(
            Instruction(
                Cmd.Networking.listStoredNetworks,
                withSudo = true,
            ),
        ).resultOrError.parseStoredNetworks()
        execute(
            Instruction(
                Cmd.Networking.listWirelessNetworks,
                withSudo = true,
            ),
        ).resultOrError.parseWifiNetworks(saved)
            .also { logger.error { "Found ${it.networks.size} networks" } }
    } catch (e: SshException) {
        logger.error { e.message }

        CommandFailure(
            command = "Command.GetWifiNetworks",
            message = e.message ?: "Unknown listing wifi networks.",
        )
    }
}

fun String.parseStoredNetworks(): List<String> = this
    .split("\n")
    .filter { it.isNotBlank() }
    .mapNotNull { it.split(networksLineSplitter).firstOrNull() }

fun String.parseWifiNetworks(knownNetworks: List<String>): WifiNetworks = this
    .split("\n")
    .filter { it.isNotBlank() }
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
