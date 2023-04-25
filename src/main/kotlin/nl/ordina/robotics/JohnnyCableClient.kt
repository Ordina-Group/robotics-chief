package nl.ordina.robotics

import org.apache.sshd.client.SshClient
import org.apache.sshd.client.channel.ClientChannelEvent
import org.apache.sshd.client.session.ClientSession
import java.io.ByteArrayOutputStream
import java.nio.charset.Charset
import java.util.EnumSet
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.toJavaDuration

object JohnnyCableSettings {
    const val username = "jetson"
    const val password = "jetson"
    const val host = "192.168.55.1"
    const val port = 22
    val timeout = 500.milliseconds.toJavaDuration()
}

fun runCableCommand(command: String = "whoami"): String = withConnection { runCommand ->
    runCommand(command)
}

fun <T> withConnection(block: (runCommand: (String) -> String) -> T) = SshClient.setUpDefaultClient().use { client ->
    client.start()
    val connection = client.connect(
        JohnnyCableSettings.username,
        JohnnyCableSettings.host,
        JohnnyCableSettings.port,
    )
    connection.await(JohnnyCableSettings.timeout)

    val session = connection.verify(JohnnyCableSettings.timeout).session
    session.addPasswordIdentity(JohnnyCableSettings.password)
    session.auth().verify(JohnnyCableSettings.timeout)

    block { session.runCommand(it) }
}

fun ClientSession.runCommand(command: String): String {
    val channel = createExecChannel(command)

    val stream = ByteArrayOutputStream()
    channel.out = stream
    channel.err = stream
    channel.open().verify(JohnnyCableSettings.timeout)
    // Wait (forever) for the channel to close - signalling command finished
    channel.waitFor(EnumSet.of(ClientChannelEvent.CLOSED), 0L)

    return stream.toByteArray().toString(Charset.defaultCharset())
}
