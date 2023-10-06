package nl.ordina.robotics.server.ssh

import mu.KotlinLogging
import org.apache.sshd.client.SshClient
import org.apache.sshd.client.session.ClientSession
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Service
import kotlin.time.toJavaDuration

@Service
@Profile("!test")
class SshSession(@Autowired val settings: SshSettings) {
    private val logger = KotlinLogging.logger {}
    private val client = SshClient.setUpDefaultClient()

    val connected: Boolean
        get() {
            val current = settings.current

            return client != null && current != null && !current.isClosed && client.isStarted && client.isOpen
        }

    fun tryConnect() {
        if (!connected) {
            logger.info { "Trying to connect via SSH" }
            try {
                settings.initialize()
            } catch (e: Exception) {
                logger.warn { e.message }
            }
        }
    }

    suspend fun <T> withSession(
        settings: SshSettings = SshSettingsLoader.load(),
        block: suspend (session: ClientSession) -> T,
    ): T {
        val session = settings.current ?: settings.initialize()

        try {
            return block(session)
        } catch (e: Exception) {
            settings.current = null
            throw e
        }
    }

    private fun SshSettings.initialize(): ClientSession {
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
