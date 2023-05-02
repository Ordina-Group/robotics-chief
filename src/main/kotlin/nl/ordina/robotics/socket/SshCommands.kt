package nl.ordina.robotics.socket

import nl.ordina.robotics.JohnnyCableSettings
import nl.ordina.robotics.runCableCommand

object SshCommands {
    fun debug(): StatusTable = debug(JohnnyCableSettings())

    fun debug(settings: JohnnyCableSettings): StatusTable {
        val user = settings.runCableCommand("whoami")
        val dir = settings.runCableCommand("ls -lah /home/jetson/robotics-workshop")
        val projectCloned = !dir.contains("No such file or directory")
        val projectCloning = settings.runCableCommand("cd /home/jetson/robotics-workshop/ && git status").contains("No commits yet")
        val projectBuilding = settings.runCableCommand("pgrep -f /usr/bin/colcon").isNotEmpty()
        val projectBuilt = !projectBuilding && !settings
            .runCableCommand("ls -lah /home/jetson/robotics-workshop/build")
            .contains("No such file or directory")
        val running = settings.runCableCommand("pgrep -a ros2")

        return StatusTable(
            listOf(
                StatusLine(
                    name = "login",
                    message = "Logged in as $user@${settings.host}:${settings.port}",
                    success = user.isNotBlank(),
                    pending = false,
                ),
                StatusLine(
                    name = "cloned",
                    success = projectCloned,
                    pending = projectCloning,
                    fixUrl = "/commands/clone",
                ),
                StatusLine(
                    name = "built",
                    success = projectBuilt,
                    pending = projectBuilding,
                    fixUrl = "/commands/build",
                ),
                StatusLine(
                    name = "running",
                    message = "PID $running",
                    success = running.isNotEmpty(),
                    pending = false,
                    fixUrl = "/commands/launch/8",
                ),
            ),
        )
    }

    private fun Boolean.checked(success: String = "", fail: String = ""): String =
        if (this) {
            "✅ $success"
        } else {
            "❌ $fail"
        }
}
