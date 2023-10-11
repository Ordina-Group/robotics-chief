package nl.ordina.robotics.server.ssh

import nl.ordina.robotics.server.robot.CommandRunner
import nl.ordina.robotics.server.robot.RobotTransport
import nl.ordina.robotics.server.robot.Settings
import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Service
import kotlin.time.Duration

@Service
@Profile("test")
class MockSshSession(
    override val connected: Boolean = false,
) : RobotTransport {
    private val logger = mu.KotlinLogging.logger {}

    override fun tryConnect() {
        // Nothing to connect to
    }

    override suspend fun <T> withSession(settings: Settings?, block: suspend (runCommand: CommandRunner) -> T): T {
        val runner: CommandRunner = { cmd: String, timeout: Duration ->
            logger.info { "Discarding $cmd for mock transport" }
            ""
        }

        return block(runner)
    }
}
