package nl.ordina.robotics.server.network.ssh

import io.github.oshai.kotlinlogging.KotlinLogging
import io.opentelemetry.instrumentation.annotations.WithSpan
import io.vertx.kotlin.coroutines.awaitBlocking
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.asFlow
import nl.ordina.robotics.server.transport.cli.InstructionResult
import org.apache.sshd.client.session.ClientSession
import java.io.PipedInputStream
import java.io.PipedOutputStream
import java.rmi.RemoteException

private val logger = KotlinLogging.logger {}

suspend fun ClientSession.runCommand(command: String): InstructionResult = awaitBlocking {
    execCommand(command)
}

@WithSpan
private fun ClientSession.execCommand(command: String): InstructionResult = try {
    InstructionResult(executeRemoteCommand(command).trimEnd())
} catch (e: RemoteException) {
    logger.error { "Error running SSH command: ${e.detail.message}" }
    InstructionResult(null, error = e.detail.message?.trimEnd() ?: "Unknown error")
}

fun ClientSession.streamCommand(command: String): Flow<String> {
    val channel = createExecChannel(command)

    val input = PipedInputStream()
    val output = PipedOutputStream(input)
    channel.out = output
    channel.err = output
    channel.open().verify()

    return input
        .reader()
        .buffered(100)
        .lineSequence()
        .asFlow()
}
