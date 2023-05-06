package nl.ordina.robotics.ssh.commands

import kotlinx.coroutines.sync.withLock
import nl.ordina.robotics.socket.Info
import nl.ordina.robotics.socket.Message
import nl.ordina.robotics.socket.SocketSession
import nl.ordina.robotics.socket.UpdateHost
import nl.ordina.robotics.socket.sendMessage
import nl.ordina.robotics.ssh.SshSettingsLoader

suspend fun SocketSession.updateHost(command: UpdateHost): Message {
    sendMessage(Info("Updating host..."))
    settingsLock.withLock {
        settings = settings.copy(host = command.host.trim()).also(SshSettingsLoader::save)
    }

    return Info("Updated host...")
}
