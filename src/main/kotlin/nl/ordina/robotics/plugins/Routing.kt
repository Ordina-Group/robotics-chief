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
import io.ktor.server.routing.routing
import io.ktor.server.websocket.webSocket
import io.ktor.websocket.Frame
import io.ktor.websocket.readText
import io.ktor.websocket.send
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import nl.ordina.robotics.runCableCommand

@OptIn(DelicateCoroutinesApi::class)
fun Application.configureRouting() {
    install(StatusPages) {
        exception<Throwable> { call, cause ->
            call.respondText(text = "500: $cause", status = HttpStatusCode.InternalServerError)
        }
    }

    install(Resources)

    routing {
        get("/debug") {
            val user = runCableCommand("whoami")
            val dir = runCableCommand("ls -lah /home/jetson/robotics-workshop")
            val projectCloned = !dir.contains("No such file or directory")

            call.respondText(
                """
                Logged in as $user
                Project cloned: $projectCloned
                """.trimIndent(),
            )
        }

        get("/commands/clone") {
            val output =
                runCableCommand("git clone https://github.com/OrdinaNederland/robotics-workshop /home/jetson/robotics-workshop")

            call.respondText("Clone output: $output")
        }

        get("/commands/build") {
            val output =
                runCableCommand("cd /home/jetson/robotics-workshop && colcon build --symlink-install && source install/local_setup.bash")

            call.respondText("Clone output: $output")
        }

        get("/commands/launch/{number}") {
            val number = call.parameters["number"]
            val output =
                runCableCommand("cd /home/jetson/robotics-workshop && source install/local_setup.bash && ROS_DOMAIN_ID=$number ros2 launch robot_app gamepad_launch.py gamepad_type:=playstation")

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

        webSocket("/subscribe") {
            send("Hailing the Chief!")

            launch {
                var lastValue: String? = null

                while (this@webSocket.isActive && !incoming.isClosedForReceive) {
                    val result = try {
                        runCableCommand("whoami")
                    } catch (e: Exception) {
                        e.message
                    }
                    if (lastValue != result) {
                        send(result ?: "emtpy")
                        lastValue = result
                    }
                    delay(200)
                }
            }.join()
        }

        staticResources("/", null)
    }
}
