package nl.ordina.robotics.server.transport.cli.checks

import nl.ordina.robotics.server.socket.BluetoothDevices
import nl.ordina.robotics.server.socket.ClientAction
import nl.ordina.robotics.server.socket.CommandFailure
import nl.ordina.robotics.server.socket.StatusLine
import nl.ordina.robotics.server.transport.cli.InstructionExecutor
import nl.ordina.robotics.server.transport.cli.commands.GetBluetoothDevicesInstruction

suspend fun controllerCheck(execute: InstructionExecutor): StatusLine {
    val message = when (val result = GetBluetoothDevicesInstruction().run(execute)) {
        is BluetoothDevices -> {
            val paired = result.devices.count { it.paired }
            val connected = result.devices.count { it.connected }
            val total = result.devices.count()

            "$connected connected, $paired paired, $total found"
        }

        is CommandFailure -> null
        else -> error("Unexpected result: $result")
    }

    return StatusLine(
        name = "Controller",
        success = message != null,
        pending = false,
        message = message ?: "No bluetooth devices connected.",
        commandLabel = "Pair & Connect",
        command = ClientAction("/actions/modal?resource=bluetooth"),
    )
}
