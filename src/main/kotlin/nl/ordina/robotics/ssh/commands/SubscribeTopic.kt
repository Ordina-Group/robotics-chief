package nl.ordina.robotics.ssh.commands

import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import nl.ordina.robotics.socket.Message
import nl.ordina.robotics.socket.SocketSession
import nl.ordina.robotics.socket.SubscribeTopic
import nl.ordina.robotics.socket.TopicMessage
import nl.ordina.robotics.socket.sendMessage
import nl.ordina.robotics.ssh.Cmd
import nl.ordina.robotics.ssh.streamSshCommand

suspend fun SocketSession.subscribeTopic(command: SubscribeTopic): Message? {
    subscriptions[command.id]?.cancel()

    subscriptions[command.id] = session.launch {
        streamSshCommand(
            Cmd.Ros.sourceBash,
            Cmd.Ros.subscribeTopic(settings.domainId, command.id),
        )
            .onEach { sendMessage(TopicMessage(command.id, it)) }
            .collect()
    }

    return null
}
