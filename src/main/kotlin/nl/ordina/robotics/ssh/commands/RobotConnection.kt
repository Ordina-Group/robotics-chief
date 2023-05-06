package nl.ordina.robotics.ssh.commands

import nl.ordina.robotics.socket.CheckRobotConnection
import nl.ordina.robotics.socket.Message
import nl.ordina.robotics.socket.RobotConnection
import nl.ordina.robotics.socket.SocketSession
import nl.ordina.robotics.ssh.Cmd
import nl.ordina.robotics.ssh.runSshCommand
import org.apache.sshd.common.SshException

suspend fun SocketSession.robotConnection(command: CheckRobotConnection): Message = try {
    settings.runSshCommand(Cmd.Unix.userInfo)
    RobotConnection(true)
} catch (e: SshException) {
    RobotConnection(false)
}
