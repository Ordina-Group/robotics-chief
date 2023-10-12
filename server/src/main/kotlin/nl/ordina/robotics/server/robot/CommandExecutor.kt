package nl.ordina.robotics.server.robot

import io.github.oshai.kotlinlogging.KotlinLogging
import nl.ordina.robotics.server.ssh.Cmd
import kotlin.time.Duration

class CommandExecutor(
    private val robotRepository: RobotRepository,
    private val transport: RobotTransport,
//    private val robotStateService: RobotStateService,
) {
    private val logger = KotlinLogging.logger {}

    suspend fun connected(): Boolean {
        if (!transport.connected()) {
            transport.tryConnect()
            if (transport.connected()) {
//                robotStateService.updateRobotState(RobotConnection(true))
            } else {
                logger.warn { "Failed to connect" }
            }
        }

        return transport.connected()
    }

    suspend fun executeInWorkDir(
        robotId: RobotId,
        vararg command: String,
        separator: String = Cmd.Unix.And,
        timeout: Duration? = null,
    ): String =
        with(robotRepository.get(robotId.value)!!.settings) {
            executeCommand(
                Cmd.Unix.cd(workDir),
                *command,
                separator = separator,
                timeout = timeout ?: this.timeout,
            )
        }

    suspend fun executeCommand(
        robotId: RobotId,
        vararg command: String,
        separator: String = Cmd.Unix.And,
        timeout: Duration? = null,
    ): String = with(robotRepository.get(robotId.value)!!.settings) {
        executeCommand(*command, separator = separator, timeout = timeout ?: this.timeout)
    }

    private suspend fun Settings.executeCommand(
        vararg command: String,
        separator: String = Cmd.Unix.And,
        timeout: Duration = this.timeout,
    ): String = transport.withSession(this) { runCommand ->
        runCommand(command.joinToString(separator), timeout)
    }
}
