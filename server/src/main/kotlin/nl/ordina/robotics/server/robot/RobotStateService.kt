package nl.ordina.robotics.server.robot

import nl.ordina.robotics.server.socket.Message
import org.springframework.stereotype.Service

@Service
class RobotStateService(
//    @Autowired
//    private val simpMessagingTemplate: SimpMessagingTemplate,
) {
    fun updateRobotState(robotId: RobotId, state: Message) {
//        simpMessagingTemplate.convertAndSend("/robots/${robotId.value}/updates", state)
    }
}
