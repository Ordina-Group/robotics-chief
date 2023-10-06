package nl.ordina.robotics.server.ssh

import mu.KotlinLogging
import nl.ordina.robotics.server.robot.RobotId
import nl.ordina.robotics.server.robot.RobotRepository
import nl.ordina.robotics.server.robot.RobotStateService
import nl.ordina.robotics.server.socket.RobotConnection
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import kotlin.time.Duration

@Service
class CommandExecutor(
    @Autowired
    val robotRepository: RobotRepository,
    @Autowired
    val sshSession: SshSession,
    @Autowired
    private val robotStateService: RobotStateService,
) {
    private val logger = KotlinLogging.logger {}

    val connected: Boolean
        get() {
            if (!sshSession.connected) {
                sshSession.tryConnect()
                if (sshSession.connected) {
                    robotStateService.updateRobotState(RobotId("3"), RobotConnection(true))
                } else {
                    logger.warn { "Failed to connect" }
                }
            }

            return sshSession.connected
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

    private suspend fun SshSettings.runSshCommand(
        vararg command: String,
        separator: String = Cmd.Unix.And,
        timeout: Duration = this.timeout,
    ): String = sshSession.withSession(this) { session ->
        session.runCommand(command.joinToString(separator), timeout)
    }
}
