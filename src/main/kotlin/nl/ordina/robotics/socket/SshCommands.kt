package nl.ordina.robotics.socket

import nl.ordina.robotics.JohnnyCableSettings
import nl.ordina.robotics.runCableCommand
import nl.ordina.robotics.runInWorkDir

object SshCommands {
    fun debug(): StatusTable = debug(JohnnyCableSettings())

    fun debug(settings: JohnnyCableSettings): StatusTable {
        val user = settings.runCableCommand("whoami")
        val dir = settings.runCableCommand("ls -lah /home/jetson/robotics-workshop")
        val projectCloned = !dir.contains("No such file or directory")
        val projectCloning = settings.runInWorkDir("git status").contains("No commits yet")
        val revision = if (projectCloned) settings.runInWorkDir("git --no-pager log --decorate --oneline -1") else ""
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
                    fixLabel = "Clone",
                ),
                StatusLine(
                    name = "revision",
                    success = revision.isNotEmpty(),
                    message = revision,
                    pending = false,
                    fixLabel = "Pull",
                ),
                StatusLine(
                    name = "built",
                    success = projectBuilt,
                    pending = projectBuilding,
                    fixUrl = "/commands/build",
                    fixLabel = "Build",
                ),
                StatusLine(
                    name = "running",
                    message = "PID $running",
                    success = running.isNotEmpty(),
                    pending = false,
                    fixUrl = "/commands/launch/8",
                    fixLabel = "Start",
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
