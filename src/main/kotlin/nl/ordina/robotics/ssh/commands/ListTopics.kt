package nl.ordina.robotics.ssh.commands

import nl.ordina.robotics.socket.Message
import nl.ordina.robotics.socket.SocketSession
import nl.ordina.robotics.socket.Topic
import nl.ordina.robotics.socket.Topics
import nl.ordina.robotics.ssh.Cmd
import nl.ordina.robotics.ssh.runSshCommand

suspend fun SocketSession.listTopics(): Message {
    val topicNames = runSshCommand(
        Cmd.Ros.sourceBash,
        Cmd.Ros.listTopics(settings.domainId),
    )
        .split('\n')
        .filter { it.startsWith('/') }

    val topicInfos = topicNames.associateWith {
        runSshCommand(
            Cmd.Ros.sourceBash,
            Cmd.Ros.topicInfo(settings.domainId, it),
        )
            .split("/n")
            .first()
            .split("\n")
            .first()
            .split(": ")
            .last()
    }

    val topics = topicNames.map { id ->
        Topic(id, topicInfos[id]!!, -1)
    }

    return Topics(topics)
}
