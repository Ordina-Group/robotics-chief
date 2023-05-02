package nl.ordina.robotics.socket

import nl.ordina.robotics.JohnnyCableSettings
import nl.ordina.robotics.socket.checks.buildCheck
import nl.ordina.robotics.socket.checks.cloneCheck
import nl.ordina.robotics.socket.checks.controllerCheck
import nl.ordina.robotics.socket.checks.loginCheck
import nl.ordina.robotics.socket.checks.revisionCheck
import nl.ordina.robotics.socket.checks.runningCheck
import nl.ordina.robotics.socket.checks.wifiCheck

object SshCommands {
    fun debug(): StatusTable = debug(JohnnyCableSettings())

    fun debug(settings: JohnnyCableSettings): StatusTable {
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
