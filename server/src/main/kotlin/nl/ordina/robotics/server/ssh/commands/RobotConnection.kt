package nl.ordina.robotics.server.ssh.commands

import nl.ordina.robotics.server.robot.CommandExecutor
import nl.ordina.robotics.server.robot.Robot
import nl.ordina.robotics.server.socket.CheckRobotConnection
import nl.ordina.robotics.server.socket.Message
import nl.ordina.robotics.server.socket.RobotConnection
import nl.ordina.robotics.server.ssh.Cmd
import org.apache.sshd.common.SshException

suspend fun Robot.robotConnection(executor: CommandExecutor, command: CheckRobotConnection): Message = try {
    executor.executeCommand(id, Cmd.Unix.userInfo)
    RobotConnection(true)
} catch (e: SshException) {
    RobotConnection(false)
}
