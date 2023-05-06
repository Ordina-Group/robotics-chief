package nl.ordina.robotics.ssh.commands

import nl.ordina.robotics.socket.Message
import nl.ordina.robotics.socket.SocketSession
import nl.ordina.robotics.socket.Topic
import nl.ordina.robotics.socket.Topics
import nl.ordina.robotics.ssh.Cmd
import nl.ordina.robotics.ssh.runSshCommand

suspend fun SocketSession.listTopics(): Message {
    val topics = runSshCommand(
        Cmd.Ros.sourceBash,
        Cmd.Ros.listTopics(settings.domainId),
    )
        .split('\n')
        .filter { it.startsWith('/') }
        .map { id -> Topic(id, -1) }

    return Topics(topics)
}
