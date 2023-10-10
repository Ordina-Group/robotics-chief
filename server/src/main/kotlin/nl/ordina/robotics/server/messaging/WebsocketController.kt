package nl.ordina.robotics.server.messaging

import mu.KotlinLogging
import org.springframework.stereotype.Controller
import org.springframework.web.reactive.socket.WebSocketHandler
import org.springframework.web.reactive.socket.WebSocketSession
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono

const val NIL = '\u0000'

private val CONNECTED = """
    CONNECTED
    version:1.2
    heart-beat:10000,10000

    $NIL
""".trimIndent()

@Controller
class WebsocketController(
    private val registry: MessageHandlerRegistry,
) : WebSocketHandler {
    private val logger = KotlinLogging.logger {}

    init {
        logger.info { "Starting WebSocket message handler" }
    }

    override fun getSubProtocols(): MutableList<String> =
        listOf("v12.stomp").toMutableList()

    override fun handle(session: WebSocketSession): Mono<Void> {
        logger.info { "Handling websocket session" }

        val output = session.send(Mono.just(session.textMessage(CONNECTED)))

        val input = session.receive()
            .doOnEach { println("RECEIVED MESSAGE") }
            .then()

        return Flux.zip(output, input).then()
    }
}
