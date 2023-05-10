package nl.ordina.robotics

import io.ktor.server.application.Application
import io.ktor.server.cio.CIO
import io.ktor.server.engine.embeddedServer
import nl.ordina.robotics.plugins.configureHTTP
import nl.ordina.robotics.plugins.configureMonitoring
import nl.ordina.robotics.plugins.configureRouting
import nl.ordina.robotics.plugins.configureSecurity
import nl.ordina.robotics.plugins.configureSerialization
import nl.ordina.robotics.plugins.configureSockets

fun main(args: Array<String>) {
    embeddedServer(CIO, port = 8080, host = "0.0.0.0", module = Application::module)
        .start(wait = true)
}

fun Application.module() {
    configureSerialization()
    configureMonitoring()
    configureHTTP()
    configureSecurity()
    configureSockets()
    configureRouting()
}
