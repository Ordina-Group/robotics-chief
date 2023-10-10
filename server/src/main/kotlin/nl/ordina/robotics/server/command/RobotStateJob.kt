package nl.ordina.robotics.server.command

import mu.KotlinLogging
import nl.ordina.robotics.server.robot.CommandExecutor
import nl.ordina.robotics.server.robot.Robot
import nl.ordina.robotics.server.robot.RobotId
import nl.ordina.robotics.server.robot.RobotRepository
import nl.ordina.robotics.server.robot.RobotStateService
import nl.ordina.robotics.server.socket.Message
import nl.ordina.robotics.server.socket.RobotConnection
import nl.ordina.robotics.server.ssh.Cmd.Ros.launch
import nl.ordina.robotics.server.ssh.checks.createSshStatusTable
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component

@Component
class RobotStateJob(
    @Autowired
    private val robotStateService: RobotStateService,
    @Autowired
    private val executor: CommandExecutor,
    @Autowired
    private val robotRepository: RobotRepository,
) {
    private val logger = KotlinLogging.logger {}

    var previousStates = mutableMapOf<RobotId, Message?>()

    //    @Scheduled(fixedRate = 2000)
    suspend fun updateRobots() {
        val robots = robotRepository.all()

        logger.info { "Updating ${robots.size} robots" }

        robots.forEach {
            updateRobot(it)
        }
    }

    suspend fun updateRobot(robot: Robot) {
        val previous = previousStates[robot.id]

        if ((previous == null || previous == RobotConnection(false)) && executor.connected) {
            logger.info { "Sending robot online message" }
            robotStateService.updateRobotState(robot.id, RobotConnection(true))
        }
        val message = if (executor.connected) {
            createSshStatusTable(robot, executor)
        } else {
            RobotConnection(false)
        }

        logger.trace { "Previous: $previous, next: $message" }

        if (previous != message) {
            logger.debug { "Updating state" }
            robotStateService.updateRobotState(robot.id, message)
        } else {
            logger.debug { "No changes" }
        }
        previousStates[robot.id] = message
    }
}
