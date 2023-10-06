package nl.ordina.robotics.server.robot

import mu.KotlinLogging
import nl.ordina.robotics.server.socket.RobotConnection
import nl.ordina.robotics.server.ssh.Cmd
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import kotlin.time.Duration

@Service
class CommandExecutor(
    @Autowired
    val robotRepository: RobotRepository,
    @Autowired
    val transport: RobotTransport,
    @Autowired
    private val robotStateService: RobotStateService,
) {
    private val logger = KotlinLogging.logger {}

    val connected: Boolean
        get() {
            if (!transport.connected) {
                transport.tryConnect()
                if (transport.connected) {
                    robotStateService.updateRobotState(RobotId("3"), RobotConnection(true))
                } else {
                    logger.warn { "Failed to connect" }
                }
            }

            return transport.connected
        }

    suspend fun runInWorkDir(
        robotId: RobotId,
        vararg command: String,
        separator: String = Cmd.Unix.And,
        timeout: Duration? = null,
    ): String =
        with(robotRepository.get(robotId.value)!!.settings) {
            runSshCommand(
                Cmd.Unix.cd(workDir),
                *command,
                separator = separator,
                timeout = timeout ?: this.timeout,
            )
        }

    suspend fun runSshCommand(
        robotId: RobotId,
        vararg command: String,
        separator: String = Cmd.Unix.And,
        timeout: Duration? = null,
    ): String = with(robotRepository.get(robotId.value)!!.settings) {
        runSshCommand(*command, separator = separator, timeout = timeout ?: this.timeout)
    }

    private suspend fun Settings.runSshCommand(
        vararg command: String,
        separator: String = Cmd.Unix.And,
        timeout: Duration = this.timeout,
    ): String = transport.withSession(this) { runCommand ->
        runCommand(command.joinToString(separator), timeout)
    }
}
