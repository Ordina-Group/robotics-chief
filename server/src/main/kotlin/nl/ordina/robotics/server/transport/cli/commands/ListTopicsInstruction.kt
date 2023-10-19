package nl.ordina.robotics.server.transport.cli.commands

import nl.ordina.robotics.server.socket.Message
import nl.ordina.robotics.server.socket.Topic
import nl.ordina.robotics.server.socket.Topics
import nl.ordina.robotics.server.transport.cli.Cmd
import nl.ordina.robotics.server.transport.cli.Instruction
import nl.ordina.robotics.server.transport.cli.InstructionExecutor
import nl.ordina.robotics.server.transport.cli.Script

class ListTopicsInstruction(private val domainId: Int) : Script {
    override suspend fun run(execute: InstructionExecutor): Message {
        val topicNames = execute(
            Instruction(
                Cmd.Ros.sourceBash,
                Cmd.Ros.listTopics(domainId),
            ),
        )
            .resultOrError
            .split('\n')
            .filter { it.startsWith('/') }

        val topicInfos = topicNames.associateWith {
            execute(
                Instruction(
                    Cmd.Ros.sourceBash,
                    Cmd.Ros.topicInfo(domainId, it),
                ),
            )
                .resultOrError
                .split("/n")
                .first()
                .split("\n")
                .first()
                .split(": ")
                .last()
        }

        val topics = topicNames.map { id ->
            Topic(id, topicInfos.getOrDefault(id, "[MISSING]"), -1)
        }

        return Topics(topics)
    }
}
