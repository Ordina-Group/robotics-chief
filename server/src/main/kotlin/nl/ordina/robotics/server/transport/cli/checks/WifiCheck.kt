package nl.ordina.robotics.server.transport.cli.checks

import nl.ordina.robotics.server.socket.StatusLine
import nl.ordina.robotics.server.transport.cli.Cmd
import nl.ordina.robotics.server.transport.cli.Instruction
import nl.ordina.robotics.server.transport.cli.InstructionExecutor

suspend fun wifiCheck(execute: InstructionExecutor): StatusLine {
    val addresses = execute(Instruction(Cmd.Networking.ipAddresses)).resultOrError
    val connected = addresses.split('\n').size > 3

    return StatusLine(
        name = "WiFi",
        success = connected,
        pending = false,
        message = addresses,
        actionUrl = "/actions/modal?resource=wifi",
        actionLabel = "Show networks",
    )
}
