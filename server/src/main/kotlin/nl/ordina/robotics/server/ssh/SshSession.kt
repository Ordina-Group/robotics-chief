package nl.ordina.robotics.server.ssh

import mu.KotlinLogging
import nl.ordina.robotics.server.robot.CommandRunner
import nl.ordina.robotics.server.robot.RobotTransport
import nl.ordina.robotics.server.robot.Settings
import org.apache.sshd.client.SshClient
import org.apache.sshd.client.session.ClientSession
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Service
import kotlin.time.Duration
import kotlin.time.toJavaDuration

@Service
@Profile("!test")
class SshSession(@Autowired val settings: Settings) : RobotTransport {
    private val logger = KotlinLogging.logger {}
    private val client = SshClient.setUpDefaultClient()

    override val connected: Boolean
        get() {
            val current = settings.current

            return client != null && current != null && !current.isClosed && client.isStarted && client.isOpen
        }

    override fun tryConnect() {
        if (!connected) {
            logger.info { "Trying to connect via SSH" }
            try {
                settings.initialize()
            } catch (e: Exception) {
                logger.warn { e.message }
            }
        }
    }

    override suspend fun <T> withSession(
        settings: Settings?,
        block: suspend (runCommand: CommandRunner) -> T,
    ): T {
        val settings = settings ?: SshSettingsLoader.load()
        val session = settings.current ?: settings.initialize()

        try {
            val test: CommandRunner = { cmd: String, timeout: Duration -> session.runCommand(cmd, timeout) }

            return block(test)
        } catch (e: Exception) {
            settings.current = null
            throw e
        }
    }

    private fun Settings.initialize(): ClientSession {
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
