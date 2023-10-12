package nl.ordina.robotics.server.ssh

import io.github.oshai.kotlinlogging.KotlinLogging
import io.opentelemetry.instrumentation.annotations.WithSpan
import io.vertx.kotlin.coroutines.awaitBlocking
import nl.ordina.robotics.server.robot.CommandRunner
import nl.ordina.robotics.server.robot.RobotTransport
import nl.ordina.robotics.server.robot.Settings
import org.apache.sshd.client.SshClient
import org.apache.sshd.client.session.ClientSession
import kotlin.time.Duration
import kotlin.time.toJavaDuration

class SshSession(val settings: Settings) : RobotTransport {
    private val logger = KotlinLogging.logger {}
    private val client = SshClient.setUpDefaultClient()

    override suspend fun connected(): Boolean {
        val current = settings.current

        return client != null && current != null && !current.isClosed && client.isStarted && client.isOpen
    }

    override suspend fun tryConnect() {
        if (!connected()) {
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
        val session = settings.current?.validOrNull() ?: settings.initialize()

        return try {
            block { cmd: String, timeout: Duration ->
                session.runCommand(cmd, timeout)
            }
        } catch (e: Exception) {
            settings.current = null
            throw e
        }
    }

    private fun ClientSession.validOrNull(): ClientSession? =
        if (sessionState.contains(ClientSession.ClientSessionEvent.CLOSED)) {
            null
        } else {
            this
        }

    private suspend fun Settings.initialize(): ClientSession = awaitBlocking {
        doInitialize()
    }

    @WithSpan
    private fun Settings.doInitialize(): ClientSession {
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

        logger.debug { "SSH session for robot $id initialized" }

        return session
    }
}
