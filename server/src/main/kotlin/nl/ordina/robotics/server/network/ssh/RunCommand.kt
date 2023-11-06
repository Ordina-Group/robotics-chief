package nl.ordina.robotics.server.network.ssh

import io.github.oshai.kotlinlogging.KotlinLogging
import io.opentelemetry.instrumentation.annotations.WithSpan
import io.vertx.kotlin.coroutines.awaitBlocking
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.withTimeout
import nl.ordina.robotics.server.transport.cli.InstructionResult
import org.apache.sshd.client.channel.ClientChannel
import org.apache.sshd.client.channel.ClientChannelEvent
import org.apache.sshd.client.session.ClientSession
import org.apache.sshd.common.util.io.output.NoCloseOutputStream
import org.apache.sshd.common.util.io.output.NullOutputStream
import java.io.ByteArrayOutputStream
import java.io.OutputStream
import java.io.PipedInputStream
import java.io.PipedOutputStream
import java.net.SocketTimeoutException
import java.nio.charset.StandardCharsets
import java.rmi.RemoteException
import java.rmi.ServerException
import kotlin.time.Duration.Companion.seconds

private val logger = KotlinLogging.logger {}

private const val CONNECTION_TIMEOUT = 5_000L
private const val COMMAND_TIMEOUT = 5_000L

suspend fun ClientSession.runCommand(command: String): InstructionResult = withTimeout(25.seconds) {
    awaitBlocking {
        execCommand(command)
    }
}

@WithSpan
private fun ClientSession.execCommand(command: String): InstructionResult = try {
    val output = executeRemoteCommandWithTimeout(command).trimEnd()
    InstructionResult(output)
} catch (e: RemoteException) {
    logger.debug { "Error running SSH command '$command': ${e.detail.message}" }
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

private fun ClientSession.executeRemoteCommandWithTimeout(command: String): String =
    ByteArrayOutputStream().use { stderr ->
        try {
            ByteArrayOutputStream(Byte.MAX_VALUE.toInt()).use { stdout ->
                executeRemoteCommand(command, stdout, stderr)
                val outBytes = stdout.toByteArray()

                String(outBytes, StandardCharsets.US_ASCII)
            }
        } finally {
            if (stderr.size() > 0) {
                val errorMessage = stderr.toString(StandardCharsets.US_ASCII.name())

                throw RemoteException(
                    "Error reported from remote command=$command",
                    ServerException(errorMessage),
                )
            }
        }
    }

private fun ClientSession.executeRemoteCommand(
    command: String,
    stdout: OutputStream = NullOutputStream(),
    stderr: OutputStream = NullOutputStream(),
) {
    NoCloseOutputStream(stderr).use { channelErr ->
        NoCloseOutputStream(stdout).use { channelOut ->
            createExecChannel(command).use { channel ->
                channel.out = channelOut
                channel.err = channelErr
                channel.open().await(CONNECTION_TIMEOUT)

                val waitMask: Collection<ClientChannelEvent> =
                    channel.waitFor(ClientSession.REMOTE_COMMAND_WAIT_EVENTS, COMMAND_TIMEOUT)
                if (waitMask.contains(ClientChannelEvent.TIMEOUT)) {
                    logger.error { "Failed to retrieve command result in time: $command" }
                    throw SocketTimeoutException("Failed to retrieve command result in time: $command")
                }

                ClientChannel.validateCommandExitStatusCode(command, channel.exitStatus)
            }
        }
    }
}
