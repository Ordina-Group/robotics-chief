package nl.ordina.robotics.socket

import nl.ordina.robotics.ssh.SshSettings
import nl.ordina.robotics.socket.checks.buildCheck
import nl.ordina.robotics.socket.checks.cloneCheck
import nl.ordina.robotics.socket.checks.controllerCheck
import nl.ordina.robotics.socket.checks.loginCheck
import nl.ordina.robotics.socket.checks.revisionCheck
import nl.ordina.robotics.socket.checks.runningCheck
import nl.ordina.robotics.socket.checks.wifiCheck

object SshCommands {
    fun debug(): StatusTable = debug(SshSettings())

    fun debug(settings: SshSettings): StatusTable {
        return StatusTable(
            listOf(
                loginCheck(settings),
                wifiCheck(settings),
                cloneCheck(settings),
                revisionCheck(settings),
                buildCheck(settings),
                controllerCheck(settings),
                runningCheck(settings),
            ),
        )
    }
}
