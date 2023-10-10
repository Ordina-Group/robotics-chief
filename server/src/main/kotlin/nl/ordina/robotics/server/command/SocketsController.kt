package nl.ordina.robotics.server.command

import kotlinx.coroutines.runBlocking
import kotlinx.serialization.Serializable
import mu.KotlinLogging
import nl.ordina.robotics.server.messaging.Envelope
import nl.ordina.robotics.server.messaging.MessageHandler
import nl.ordina.robotics.server.robot.CommandExecutor
import nl.ordina.robotics.server.robot.RobotRepository
import nl.ordina.robotics.server.robot.RobotStateService
import nl.ordina.robotics.server.socket.Command
import nl.ordina.robotics.server.socket.RobotConnection
import nl.ordina.robotics.server.socket.handleCommand
import nl.ordina.robotics.server.ssh.checks.createSshStatusTable
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Controller

@Controller
class SocketsController(
    @Autowired
    val robotRepository: RobotRepository,
    @Autowired
    val executor: CommandExecutor,
    @Autowired
    private val robotStateService: RobotStateService,
) {
    private val logger = KotlinLogging.logger {}

    @MessageHandler("/robots/{robotId}/command")
    fun processCommand(robotId: String, command: Command) = runBlocking {
        logger.info { "Received command: $command" }
        val robot = robotRepository.get(robotId)!!

        robot
            .handleCommand(executor, command)
            ?.let { Envelope("/TODO/SENDING", it) }
    }

    @MessageHandler("/robots/{robotId}/updates")
    fun subscribeToRobotUpdates(robotId: String) =
        runBlocking {
            val robot = robotRepository.get(robotId)!!

            val message = if (executor.connected) {
                robotStateService.updateRobotState(robot.id, createSshStatusTable(robot, executor))

                RobotConnection(true)
            } else {
                RobotConnection(false)
            }

//            robotStateService.updateRobotState(robot.id, message)

            Envelope("/TODO", message)
        }
}

//fun nl.ordina.robotics.server.socket.Message.toSpringMessage(): Message<nl.ordina.robotics.server.socket.Message> {
//    return MessageWrapper(this)
//}

//@Serializable
//data class MessageWrapper(
//    private val command: nl.ordina.robotics.server.socket.Message,
//) : Message<nl.ordina.robotics.server.socket.Message> {
//    override fun getHeaders(): MessageHeaders {
//        return MessageHeaders(emptyMap())
//    }
//
//    override fun getPayload(): nl.ordina.robotics.server.socket.Message {
//        return command
//    }
//}
