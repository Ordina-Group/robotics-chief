package nl.ordina.robotics.plugins

import io.ktor.http.CacheControl
import io.ktor.http.HttpStatusCode
import io.ktor.server.application.Application
import io.ktor.server.application.install
import io.ktor.server.http.content.staticResources
import io.ktor.server.plugins.statuspages.StatusPages
import io.ktor.server.resources.Resources
import io.ktor.server.response.respondText
import io.ktor.server.routing.routing
import io.ktor.server.websocket.webSocket
import nl.ordina.robotics.routes
import nl.ordina.robotics.socket.SocketSession
import nl.ordina.robotics.socket.handleChiefSocket
import kotlin.time.Duration.Companion.days

fun Application.configureRouting() {
    install(StatusPages) {
        exception<Throwable> { call, cause ->
            call.respondText(text = "500: $cause", status = HttpStatusCode.InternalServerError)
        }
    }

    install(Resources)

    routing {
        routes()

        webSocket(path = "/subscribe") {
            SocketSession(this).handleChiefSocket()
        }

        staticResources("/", null) {
            cacheControl {
                val cacheControl = when (it.file.split(".").lastOrNull()) {
                    "html" -> CacheControl.MaxAge(maxAgeSeconds = 60)
                    "js" -> CacheControl.MaxAge(maxAgeSeconds = 365.days.inWholeSeconds.toInt())
                    "css" -> CacheControl.MaxAge(maxAgeSeconds = 365.days.inWholeSeconds.toInt())
                    else -> null
                }

                listOfNotNull(cacheControl)
            }
        }
    }
}
