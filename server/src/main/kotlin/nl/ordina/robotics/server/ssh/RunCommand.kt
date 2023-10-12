package nl.ordina.robotics.server.ssh

import io.github.oshai.kotlinlogging.KotlinLogging
import io.opentelemetry.instrumentation.annotations.WithSpan
import io.vertx.kotlin.coroutines.awaitBlocking
import org.apache.sshd.client.channel.ClientChannelEvent
import org.apache.sshd.client.session.ClientSession
import java.io.ByteArrayOutputStream
import java.nio.charset.Charset
import java.util.EnumSet
import kotlin.time.Duration
import kotlin.time.toJavaDuration

private val logger = KotlinLogging.logger {}

suspend fun ClientSession.runCommand(command: String, timeout: Duration): String = awaitBlocking {
    try {
        execCommand(command, timeout)
    } catch (e: Exception) {
        logger.error { "Error running SSH command: ${e.message}" }
        e.printStackTrace()
        ""
    }
}

@WithSpan
private fun ClientSession.execCommand(command: String, timeout: Duration): String {
    val channel = createExecChannel(command)

    val stream = ByteArrayOutputStream()
    channel.out = stream
    channel.err = stream
    channel.open().verify(timeout.toJavaDuration())
    // Wait (forever) for the channel to close - signalling command finished
    channel.waitFor(EnumSet.of(ClientChannelEvent.CLOSED), timeout.inWholeMilliseconds)

    return stream
        .toByteArray()
        .toString(Charset.defaultCharset())
        .trim()
}
