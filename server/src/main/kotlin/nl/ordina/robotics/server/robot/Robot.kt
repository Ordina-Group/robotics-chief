package nl.ordina.robotics.server.robot

import nl.ordina.robotics.server.ssh.SshSettings

class Robot(val id: RobotId, val settings: SshSettings) {
    val connected = false
    val connecting = false
}
