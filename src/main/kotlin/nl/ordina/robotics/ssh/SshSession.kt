package nl.ordina.robotics.ssh

import org.apache.sshd.client.SshClient
import org.apache.sshd.client.session.ClientSession
import kotlin.time.toJavaDuration

object SshSession {
    private val client = SshClient.setUpDefaultClient()

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
