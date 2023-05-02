package nl.ordina.robotics.plugins

import io.ktor.http.HttpStatusCode
import io.ktor.server.application.Application
import io.ktor.server.application.call
import io.ktor.server.application.install
import io.ktor.server.http.content.staticResources
import io.ktor.server.plugins.BadRequestException
import io.ktor.server.plugins.statuspages.StatusPages
import io.ktor.server.resources.Resources
import io.ktor.server.response.respondText
import io.ktor.server.routing.get
import io.ktor.server.routing.post
import io.ktor.server.routing.routing
import io.ktor.server.websocket.DefaultWebSocketServerSession
import io.ktor.server.websocket.webSocket
import io.ktor.websocket.Frame
import io.ktor.websocket.readText
import io.ktor.websocket.send
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import nl.ordina.robotics.socket.handleChiefSocket
import nl.ordina.robotics.runCableCommand
import nl.ordina.robotics.socket.SshCommands

fun Application.configureRouting() {
    install(StatusPages) {
        exception<Throwable> { call, cause ->
            call.respondText(text = "500: $cause", status = HttpStatusCode.InternalServerError)
        }
    }

    install(Resources)

    routing {
        get("/debug") {
            call.respondText(Json.encodeToString(SshCommands.debug()))
        }

        post("/commands/setupenv") {
            val output = runCableCommand("echo \"source /opt/ros/foxy/setup.bash\" >> ~/.bashrc")

            call.respondText("Done: $output")
        }

        post("/commands/clone") {
            val output =
                runCableCommand("git clone https://github.com/OrdinaNederland/robotics-workshop /home/jetson/robotics-workshop")

            call.respondText("Clone output: $output")
        }

        post("/commands/build") {
            val output =
                runCableCommand("cd /home/jetson/robotics-workshop && colcon build --symlink-install && source install/local_setup.bash")

            call.respondText("Clone output: $output")
        }

        post("/commands/launch/{number}") {
            val number = call.parameters["number"]
            val output =
                runCableCommand("cd /home/jetson/robotics-workshop && source /opt/ros/foxy/setup.bash && source install/local_setup.bash && ROS_DOMAIN_ID=$number ros2 launch -n robot_app gamepad_launch.py gamepad_type:=playstation &")

            call.respondText("Launched robot number $number: $output")
        }

        get("/exec/{command}") {
            val command = call.parameters["command"] ?: throw BadRequestException("No command given")
            val result = runCableCommand(command)

            call.respondText(result)
        }

        webSocket("/chat") {
            send("You are connected!")
            for (frame in incoming) {
                frame as? Frame.Text ?: continue
                val receivedText = frame.readText()
                send("You said: $receivedText")
            }
        }

        webSocket(path = "/subscribe", handler = DefaultWebSocketServerSession::handleChiefSocket)

        staticResources("/", null)
    }
}
