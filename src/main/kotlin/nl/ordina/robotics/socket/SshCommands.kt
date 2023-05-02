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
        val controllers = settings.runCableCommand("bluetoothctl paired-devices")
        val devices = settings.runCableCommand("bluetoothctl devices")
        val running = settings.runCableCommand("pgrep -af ros2")
        val runningParts = running.isNotEmpty()
        val runningMainProcess = running.contains("ros2 launch -n ")
        val runningMainAndController = runningMainProcess && running.split("\n").size >= 2

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
                    actionUrl = "/commands/clone",
                    actionLabel = "Clone".onlyWhen(!projectCloned),
                ),
                StatusLine(
                    name = "revision",
                    success = revision.isNotEmpty(),
                    message = revision,
                    pending = false,
                    actionLabel = "Pull",
                ),
                StatusLine(
                    name = "built",
                    success = projectBuilt,
                    pending = projectBuilding,
                    actionUrl = "/commands/build",
                    actionLabel = "Build".onlyWhen(!projectBuilt),
                ),
                StatusLine(
                    name = "controller",
                    success = controllers.isNotEmpty(),
                    pending = false,
                    message = controllers.ifEmpty { devices },
                    actionUrl = "/commands/connect/${settings.controller}",
                    actionLabel = if (controllers.isEmpty()) "Pair & Connect" else null,
                ),
                StatusLine(
                    name = "running",
                    message = "PID $running",
                    success = runningMainAndController,
                    pending = runningParts && !runningMainAndController,
                    actionUrl = if (runningParts) "/commands/restart/8" else "/commands/launch/8",
                    actionLabel = if (runningParts) "Restart" else "Start",
                ),
            ),
        )
    }

    private fun String.onlyWhen(condition: Boolean): String? =
        if (condition) {
            this
        } else {
            null
        }
}
