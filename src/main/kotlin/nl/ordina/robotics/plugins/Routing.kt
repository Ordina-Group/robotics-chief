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
import io.ktor.server.websocket.DefaultWebSocketServerSession
import io.ktor.server.websocket.webSocket
import io.ktor.util.pipeline.PipelineContext
import io.ktor.websocket.Frame
import io.ktor.websocket.readText
import io.ktor.websocket.send
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import nl.ordina.robotics.socket.CommandFailure
import nl.ordina.robotics.socket.CommandSuccess
import nl.ordina.robotics.socket.handleChiefSocket
import nl.ordina.robotics.ssh.Cmd
import nl.ordina.robotics.ssh.SshSettingsLoader
import nl.ordina.robotics.ssh.checks.createSshStatusTable
import nl.ordina.robotics.ssh.ignoreFailure
import nl.ordina.robotics.ssh.runInWorkDir
import nl.ordina.robotics.ssh.runSshCommand
import javax.security.auth.PrivateCredentialPermission

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
            val output = runSshCommand("echo \"${Cmd.Ros.sourceBash}\" >> ~/.bashrc")

            respondCommand(
                command = "Command.SetupEnv",
                success = true,
                message = "Setup output: $output",
            )
        }

        post("/commands/clone") {
            val output =
                runSshCommand(Cmd.Git.clone("https://github.com/OrdinaNederland/robotics-workshop /home/jetson/robotics-workshop"))

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
            val controllerId = call.parameters["controllerId"]
            val output = runSshCommand("bluetoothctl connect $controllerId")

            respondCommand(
                command = "Command.BluetoothConnect",
                success = output.contains("Failed to connect"),
                message = output,
            )
        }

        post("/commands/disconnect/{controllerId}") {
            val controllerId = call.parameters["controllerId"]
            val output = runSshCommand("bluetoothctl disconnect $controllerId")

            respondCommand(
                command = "Command.BluetoothDisconnect",
                success = output.contains("Failed to disconnect"),
                message = output,
            )
        }

        post("/commands/restart/{number}") {
            val number = call.parameters["number"]
            val output = runInWorkDir(
                Cmd.Ros.stop.ignoreFailure(),
                Cmd.Ros.sourceBash,
                Cmd.Ros.sourceLocalSetup,
                "ROS_DOMAIN_ID=$number ros2 launch -n robot_app gamepad_launch.py gamepad_type:=playstation &",
            )

            respondCommand(
                command = "Command.RestartRos",
                success = true,
                message = "Launched robot number $number: $output",
            )
        }

        post("/commands/launch/{number}") {
            val number = call.parameters["number"]
            val output = runInWorkDir(
                Cmd.Ros.sourceBash,
                Cmd.Ros.sourceLocalSetup,
                "ROS_DOMAIN_ID=$number ros2 launch -n robot_app gamepad_launch.py gamepad_type:=playstation &",
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

        webSocket(path = "/subscribe", handler = DefaultWebSocketServerSession::handleChiefSocket)

        staticResources("/", null)
    }
}

private suspend fun PipelineContext<Unit, ApplicationCall>.respondCommand(command: String, success: Boolean, message: String) {
    val (status, result) = if (success) {
        Pair(HttpStatusCode.InternalServerError, CommandFailure(command, message))
    } else {
        Pair(HttpStatusCode.OK, CommandSuccess(command, message))
    }

    call.respond(status, result)
}
