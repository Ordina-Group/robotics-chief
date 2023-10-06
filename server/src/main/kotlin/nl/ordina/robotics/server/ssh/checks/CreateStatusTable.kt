package nl.ordina.robotics.server.ssh.checks

import nl.ordina.robotics.server.robot.Robot
import nl.ordina.robotics.server.socket.StatusTable
import nl.ordina.robotics.server.ssh.CommandExecutor

suspend fun createSshStatusTable(robot: Robot, executor: CommandExecutor): StatusTable = StatusTable(
    listOf(
        robot.osCheck(executor),
        robot.loginCheck(executor),
        robot.wifiCheck(executor),
        robot.cloneCheck(executor),
        robot.revisionCheck(executor),
        robot.buildCheck(executor),
        robot.controllerCheck(executor),
        robot.runningCheck(executor),
    ),
)
