package nl.ordina.robotics.ssh

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.asFlow
import org.apache.sshd.client.channel.ClientChannelEvent
import org.apache.sshd.client.session.ClientSession
import java.io.ByteArrayOutputStream
import java.io.PipedInputStream
import java.io.PipedOutputStream
import java.nio.charset.Charset
import java.util.EnumSet
import kotlin.time.Duration
import kotlin.time.toJavaDuration

fun runInWorkDir(vararg command: String, separator: String = " && "): String =
    with(SshSettingsLoader.load()) {
        runSshCommand("cd $workDir", *command, separator = separator)
    }

fun runSshCommand(vararg command: String, separator: String = " && "): String =
    SshSettingsLoader.load().runSshCommand(*command, separator = separator)

fun streamSshCommand(vararg command: String, separator: String = " && "): Flow<String> =
    SshSettingsLoader.load().streamSshCommand(*command, separator = separator)

fun SshSettings.runInWorkDir(vararg command: String, separator: String = " && "): String =
    SshSession.withSession(this) { session ->
        session.runCommand(arrayOf("cd $workDir", *command).joinToString(separator), this.timeout)
    }

fun SshSettings.runSshCommand(
    vararg command: String,
    separator: String = " && ",
    timeout: Duration = this.timeout,
): String =
    SshSession.withSession(this) { session ->
        session.runCommand(command.joinToString(separator), timeout)
    }

fun SshSettings.streamSshCommand(
    vararg command: String,
    separator: String = " && ",
): Flow<String> =
    SshSession.withSession(this) { session ->
        session.streamCommand(command.joinToString(separator))
    }

fun ClientSession.runCommand(command: String, timeout: Duration): String {
    val channel = createExecChannel(command)

    val stream = ByteArrayOutputStream()
    channel.out = stream
    channel.err = stream
    channel.open().verify(timeout.toJavaDuration())
    // Wait (forever) for the channel to close - signalling command finished
    channel.waitFor(EnumSet.of(ClientChannelEvent.CLOSED), timeout.inWholeMilliseconds)

    return stream.toByteArray().toString(Charset.defaultCharset()).trim()
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
