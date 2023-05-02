package nl.ordina.robotics

import kotlinx.serialization.Serializable
import org.apache.sshd.client.SshClient
import org.apache.sshd.client.channel.ClientChannelEvent
import org.apache.sshd.client.session.ClientSession
import java.io.ByteArrayOutputStream
import java.nio.charset.Charset
import java.util.EnumSet
import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.toJavaDuration

@Serializable
data class JohnnyCableSettings(
    val username: String = "jetson",
    val password: String = "jetson",
    val host: String = "192.168.55.1",
    val port: Int = 22,
    val controller: String = "20:21:06:16:1B:F3",
    val timeout: Duration = 5000.milliseconds,
    val workDir: String = "/home/jetson/robotics-workshop",
    internal var current: ClientSession? = null,
)

fun runInWorkDir(vararg command: String, separator: String = " && "): String =
    with(JohnnyCableSettings()) {
        runCableCommand("cd $workDir", *command, separator = separator)
    }

fun runCableCommand(vararg command: String, separator: String = " && "): String =
    JohnnyCableSettings().runCableCommand(*command, separator = separator)

fun JohnnyCableSettings.runInWorkDir(vararg command: String, separator: String = " && "): String =
    JohnnyCableSession.withSession(this) { session ->
        session.runCommand(arrayOf("cd $workDir", *command).joinToString(separator), this.timeout)
    }

fun JohnnyCableSettings.runCableCommand(vararg command: String, separator: String = " && "): String =
    JohnnyCableSession.withSession(this) { session ->
        session.runCommand(command.joinToString(separator), this.timeout)
    }

object JohnnyCableSession {
    private val client = SshClient.setUpDefaultClient()

    fun <T> withSession(
        settings: JohnnyCableSettings = JohnnyCableSettings(),
        block: (session: ClientSession) -> T,
    ): T {
        val session = settings.current ?: settings.initialize()

        try {
            return block(session)
        } catch (e: Exception) {
            settings.current = null
            throw e
        }
    }

    private fun JohnnyCableSettings.initialize(): ClientSession {
        client.start()
        val connection = client.connect(
            username,
            host,
            port,
        )
        connection.await(timeout.toJavaDuration())

        val session = connection.verify(timeout.toJavaDuration()).session.apply {
            addPasswordIdentity(password)
            auth().verify(timeout.toJavaDuration())
        }

        current = session

        return session
    }
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
