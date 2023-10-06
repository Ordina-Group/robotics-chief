package nl.ordina.robotics.server.ssh.commands

import nl.ordina.robotics.server.robot.Robot
import nl.ordina.robotics.server.socket.Message
import nl.ordina.robotics.server.socket.SubscribeTopic
import nl.ordina.robotics.server.ssh.CommandExecutor

suspend fun Robot.subscribeTopic(executor: CommandExecutor, command: SubscribeTopic): Message? {
//    subscriptions[command.id]?.cancel()

//    subscriptions[command.id] = session.send {
//        streamSshCommand(
//            Cmd.Ros.sourceBash,
//            Cmd.Ros.subscribeTopic(settings.domainId, command.id),
//        )
//            .onEach { sendMessage(TopicMessage(command.id, it)) }
//            .collect()
//    }

    return null
}
