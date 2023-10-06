package nl.ordina.robotics.server.socket

import org.springframework.messaging.Message
import org.springframework.messaging.handler.annotation.MessageMapping
import org.springframework.messaging.handler.annotation.SendTo
import org.springframework.stereotype.Controller

//@Service
//class HandleChiefSocket : WebSocketHandler {
//    private val logger = KotlinLogging.logger {}
//
//    override fun handle(session: WebSocketSession): Mono<Void?> {
//        logger.info { "Handling session $session" }
//
//        return session.receive()
//            .doOnNext {
//                logger.info { "Received ${it.payloadAsText}" }
//            }
//            .concatMap {
//                mono {
//                    session.send(Mono.just(session.textMessage("Hello!")))
//                }
//            }
//            .then()
//    }
//}

@Controller
class HandleChiefSocket2 {
    @MessageMapping("/hello")
    @SendTo("/topic/greetings")
    fun handle(message: Message<ByteArray>) {
        println("Received ${message.payload}")
    }
}

suspend fun SocketSession.handleChiefSocket() {
    sendMessage(Info("Chief says hi!"))

}
//    val broadcaster = session.launch(Dispatchers.IO) {
//        var lastValue: Message? = null
//
//        while (session.isActive && !session.incoming.isClosedForReceive) {
//            val result = try {
//                settingsLock.withLock {
//                    createSshStatusTable(settings)
//                }
//            } catch (e: Exception) {
//                CommandFailure("debug", e.message ?: "")
//            }
//            if (lastValue != result) {
//                sendMessage(result)
//                lastValue = result
//            }
//            delay(500)
//        }
//    }
//
//    val listener = session.launch(Dispatchers.IO) {
//        for (frame in session.incoming) {
//            frame as? Frame.Text ?: continue
//            val receivedText = frame.readText()
//            val command = Json.decodeFromString<Command>(receivedText)
//            try {
//                handleCommand(command)
//            } catch (e: Exception) {
//                sendMessage(CommandFailure(command.name, e.message ?: "unknown failure"))
//            }
//        }
//    }
//
//    joinAll(broadcaster, listener)
// }
