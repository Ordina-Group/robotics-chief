package nl.ordina.robotics.server.transport.cli.checks

import io.vertx.core.Future
import io.vertx.kotlin.coroutines.await
import io.vertx.kotlin.coroutines.vertxFuture
import nl.ordina.robotics.server.socket.Message
import nl.ordina.robotics.server.socket.StatusLine
import nl.ordina.robotics.server.socket.StatusTable
import nl.ordina.robotics.server.transport.cli.InstructionExecutor
import nl.ordina.robotics.server.transport.cli.Script

class CreateStatusTableInstruction : Script {
    override suspend fun run(execute: InstructionExecutor): Message {
        val instructions = listOf(
            vertxFuture { osCheck(execute) },
            vertxFuture { loginCheck(execute) },
            vertxFuture { wifiCheck(execute) },
            vertxFuture { cloneCheck(execute) },
            vertxFuture { revisionCheck(execute) },
            vertxFuture { buildCheck(execute) },
            vertxFuture { controllerCheck(execute) },
            vertxFuture { runningCheck(execute) },
        )

        val results = Future.join(instructions).await().list<StatusLine>()

        return StatusTable(
            results,
        )
    }
}
