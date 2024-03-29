package nl.ordina.robotics.server.network.ssh

import io.github.oshai.kotlinlogging.KotlinLogging
import io.opentelemetry.instrumentation.annotations.WithSpan
import io.vertx.kotlin.coroutines.awaitBlocking
import nl.ordina.robotics.server.network.CommandRunner
import nl.ordina.robotics.server.network.RobotNetwork
import nl.ordina.robotics.server.robot.RobotSettings
import org.apache.sshd.client.ClientBuilder
import org.apache.sshd.client.session.ClientSession
import kotlin.time.toJavaDuration

class SshSession(private val settings: RobotSettings, private val connectionUpdated: (Boolean) -> Unit) : RobotNetwork {
    private val logger = KotlinLogging.logger {}
    private val client = ClientBuilder.builder()
        .serverKeyVerifier { _, _, _ -> true }
        .build()
    private var current: ClientSession? = null

    override suspend fun connected(): Boolean {
        return client != null && current != null && !current!!.isClosed && client.isStarted && client.isOpen
    }

    override suspend fun tryConnect() {
        if (!connected()) {
            logger.debug { "Trying to connect via SSH" }
            try {
                initialize()
            } catch (e: Exception) {
                connectionUpdated(false)
                logger.warn { e.message }
            }
        }
    }

//    override fun <T> streamWithSession(
//        settings: Settings?,
//        block: suspend (streamCommand: CommandStreamer) -> Flow<T>,
//    ): Flow<T> {
//        val settings = settings ?: SshSettingsLoader.load()
//        val session = settings.current?.validOrNull() ?: settings.initialize()
//
//        val executor = vertx.createSharedWorkerExecutor("foobar")
//
//        val handler = block { cmd: String ->
//            session.streamCommand(cmd)
//        }
//
//        return executor.executeBlocking { handler }
//    }

    override suspend fun <T> withSession(block: suspend (runCommand: CommandRunner) -> T): T {
        current = current.validOrReinitialize()

        return try {
            block { cmd: String ->
                current!!.runCommand(cmd)
            }
        } catch (e: Exception) {
            current = null
            connectionUpdated(false)
            throw e
        }
    }

    private suspend fun ClientSession?.validOrReinitialize(): ClientSession =
        if (this == null || sessionState.contains(ClientSession.ClientSessionEvent.CLOSED)) {
            initialize()
        } else {
            this
        }

    private suspend fun initialize(): ClientSession = awaitBlocking {
        connectionUpdated(false)
        settings.doInitialize()
            .also { connectionUpdated(true) }
    }

    @WithSpan
    private fun RobotSettings.doInitialize(): ClientSession {
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
