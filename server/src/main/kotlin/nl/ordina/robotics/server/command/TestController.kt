package nl.ordina.robotics.server.command

import nl.ordina.robotics.server.messaging.Envelope
import nl.ordina.robotics.server.messaging.MessageHandler
import nl.ordina.robotics.server.socket.Info
import org.springframework.stereotype.Controller

@Controller
class TestController {
    @MessageHandler("/info")
    fun handleInfoCommand(envelope: Envelope<Info>) {
        println("Received test command: ${envelope.body}")
    }
}
