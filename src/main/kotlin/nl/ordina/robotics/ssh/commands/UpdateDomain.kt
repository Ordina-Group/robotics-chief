package nl.ordina.robotics.ssh.commands

import kotlinx.coroutines.sync.withLock
import nl.ordina.robotics.socket.Info
import nl.ordina.robotics.socket.Message
import nl.ordina.robotics.socket.Settings
import nl.ordina.robotics.socket.SocketSession
import nl.ordina.robotics.socket.UpdateDomain
import nl.ordina.robotics.socket.sendMessage
import nl.ordina.robotics.ssh.SshSettingsLoader

suspend fun SocketSession.updateDomain(command: UpdateDomain): Message {
    sendMessage(Info("Updating domain..."))
    settingsLock.withLock {
        settings = settings.copy(domainId = command.domain).also(SshSettingsLoader::save)
    }

    return Settings(settings)
}
