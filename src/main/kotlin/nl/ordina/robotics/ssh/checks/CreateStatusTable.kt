package nl.ordina.robotics.ssh.checks

import nl.ordina.robotics.socket.StatusTable
import nl.ordina.robotics.ssh.SshSettings

suspend fun createSshStatusTable(settings: SshSettings): StatusTable = StatusTable(
    listOf(
        osCheck(settings),
        loginCheck(settings),
        wifiCheck(settings),
        cloneCheck(settings),
        revisionCheck(settings),
        buildCheck(settings),
        controllerCheck(settings),
        runningCheck(settings),
    ),
)
