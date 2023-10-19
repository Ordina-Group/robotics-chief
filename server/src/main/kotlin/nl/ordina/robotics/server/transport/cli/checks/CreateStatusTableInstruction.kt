package nl.ordina.robotics.server.transport.cli.checks

import nl.ordina.robotics.server.socket.Message
import nl.ordina.robotics.server.socket.StatusTable
import nl.ordina.robotics.server.transport.cli.InstructionExecutor
import nl.ordina.robotics.server.transport.cli.Script

class CreateStatusTableInstruction : Script {
    override suspend fun run(execute: InstructionExecutor): Message = StatusTable(
        listOf(
            osCheck(execute),
            loginCheck(execute),
            wifiCheck(execute),
            cloneCheck(execute),
            revisionCheck(execute),
            buildCheck(execute),
            controllerCheck(execute),
            runningCheck(execute),
        ),
    )
}
