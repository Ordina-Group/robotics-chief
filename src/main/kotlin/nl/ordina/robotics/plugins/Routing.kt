package nl.ordina.robotics.plugins

import io.ktor.http.HttpStatusCode
import io.ktor.server.application.Application
import io.ktor.server.application.ApplicationCall
import io.ktor.server.application.call
import io.ktor.server.application.install
import io.ktor.server.http.content.staticResources
import io.ktor.server.plugins.BadRequestException
import io.ktor.server.plugins.statuspages.StatusPages
import io.ktor.server.resources.Resources
import io.ktor.server.response.respond
import io.ktor.server.response.respondText
import io.ktor.server.routing.get
import io.ktor.server.routing.post
import io.ktor.server.routing.routing
import io.ktor.server.websocket.webSocket
import io.ktor.util.pipeline.PipelineContext
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import nl.ordina.robotics.socket.CommandFailure
import nl.ordina.robotics.socket.CommandSuccess
import nl.ordina.robotics.socket.SocketSession
import nl.ordina.robotics.socket.handleChiefSocket
import nl.ordina.robotics.ssh.Cmd
import nl.ordina.robotics.ssh.SshSettingsLoader
import nl.ordina.robotics.ssh.checks.createSshStatusTable
import nl.ordina.robotics.ssh.ignoreFailure
import nl.ordina.robotics.ssh.runInWorkDir
import nl.ordina.robotics.ssh.runSshCommand

fun Application.configureRouting() {
    install(StatusPages) {
        exception<Throwable> { call, cause ->
            call.respondText(text = "500: $cause", status = HttpStatusCode.InternalServerError)
        }
    }

    install(Resources)

    routing {
        get("/debug") {
            call.respondText(Json.encodeToString(createSshStatusTable(SshSettingsLoader.load())))
        }

        post("/commands/setupenv") {
            val output = runSshCommand(Cmd.Unix.addToBashRc(Cmd.Ros.sourceBash))

            respondCommand(
                command = "Command.SetupEnv",
                success = true,
                message = "Setup output: $output",
            )
        }

        post("/commands/clone") {
            val output = runSshCommand(
                Cmd.Git.clone(
                    "https://github.com/OrdinaNederland/robotics-workshop",
                    "/home/jetson/robotics-workshop",
                ),
            )

            respondCommand(
                command = "Command.Clone",
                success = true,
                message = "Clone output: $output",
            )
        }

        post("/commands/pull") {
            val output = runInWorkDir(Cmd.Git.pull)

            respondCommand(
                command = "Command.Pull",
                success = true,
                message = "Pull output: $output",
            )
        }

        post("/commands/build") {
            val output = runInWorkDir(
                Cmd.Ros.buildInstall,
                Cmd.Ros.sourceLocalSetup,
            )

            respondCommand(
                command = "Command.Build",
                success = output.contains("Failed to connect"),
                message = "Build output: $output",
            )
        }

        post("/commands/connect/{controllerId}") {
            val controllerId = call.parameters["controllerId"] ?: throw BadRequestException("Missing controller id")
            val output = runSshCommand(Cmd.Bluetooth.connect(controllerId))

            respondCommand(
                command = "Command.BluetoothConnect",
                success = !output.contains("Failed to connect"),
                message = output,
            )
        }

        post("/commands/disconnect/{controllerId}") {
            val controllerId = call.parameters["controllerId"] ?: throw BadRequestException("Missing controller id")
            val output = runSshCommand(Cmd.Bluetooth.disconnect(controllerId))

            respondCommand(
                command = "Command.BluetoothDisconnect",
                success = output.contains("Successful disconnected"),
                message = output,
            )
        }

        post("/commands/restart/{number}") {
            val number = call.parameters["number"] ?: throw BadRequestException("Missing domain id")
            val output = runInWorkDir(
                Cmd.Ros.stop.ignoreFailure(),
                Cmd.Ros.sourceBash,
                Cmd.Ros.sourceLocalSetup,
                Cmd.Ros.launch(number.toInt().coerceIn(1..100)),
            )

            respondCommand(
                command = "Command.RestartRos",
                success = true,
                message = "Launched robot number $number: $output",
            )
        }

        post("/commands/launch/{number}") {
            val number = call.parameters["number"] ?: throw BadRequestException("Missing domain id")
            val output = runInWorkDir(
                Cmd.Ros.sourceBash,
                Cmd.Ros.sourceLocalSetup,
                Cmd.Ros.launch(number.toInt().coerceIn(1..100)),
            )

            respondCommand(
                command = "Command.StartRos",
                success = true,
                message = "Launched robot number $number: $output",
            )
        }

        get("/exec/{command}") {
            val command = call.parameters["command"] ?: throw BadRequestException("No command given")
            val output = runSshCommand(command)

            respondCommand(
                command = "Command.Exec",
                success = true,
                message = "Output: $output",
            )
        }

        webSocket(path = "/subscribe") {
            SocketSession(this).handleChiefSocket()
        }

        staticResources("/", null)
    }
}

private suspend fun PipelineContext<Unit, ApplicationCall>.respondCommand(
    command: String,
    success: Boolean,
    message: String,
) {
    val (status, result) = if (success) {
        Pair(HttpStatusCode.OK, CommandSuccess(command, message))
    } else {
        Pair(HttpStatusCode.InternalServerError, CommandFailure(command, message))
    }

    call.respond(status, result)
}
