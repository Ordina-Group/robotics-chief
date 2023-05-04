package nl.ordina.robotics.ssh

import org.apache.sshd.client.channel.ClientChannelEvent
import org.apache.sshd.client.session.ClientSession
import java.io.ByteArrayOutputStream
import java.nio.charset.Charset
import java.util.EnumSet
import kotlin.time.Duration
import kotlin.time.toJavaDuration

fun runInWorkDir(vararg command: String, separator: String = " && "): String =
    with(SshSettings()) {
        runSshCommand("cd $workDir", *command, separator = separator)
    }

fun runSshCommand(vararg command: String, separator: String = " && "): String =
    SshSettings().runSshCommand(*command, separator = separator)

fun SshSettings.runInWorkDir(vararg command: String, separator: String = " && "): String =
    SshSession.withSession(this) { session ->
        session.runCommand(arrayOf("cd $workDir", *command).joinToString(separator), this.timeout)
    }

fun SshSettings.runSshCommand(vararg command: String, separator: String = " && "): String =
    SshSession.withSession(this) { session ->
        session.runCommand(command.joinToString(separator), this.timeout)
    }

fun ClientSession.runCommand(command: String, timeout: Duration): String {
    val channel = createExecChannel(command)

    val stream = ByteArrayOutputStream()
    channel.out = stream
    channel.err = stream
    channel.open().verify(timeout.toJavaDuration())
    // Wait (forever) for the channel to close - signalling command finished
    channel.waitFor(EnumSet.of(ClientChannelEvent.CLOSED), 0L)

    return stream.toByteArray().toString(Charset.defaultCharset()).trim()
}
