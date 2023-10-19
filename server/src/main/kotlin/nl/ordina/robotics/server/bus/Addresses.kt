package nl.ordina.robotics.server.bus

object Addresses {
    fun initialSlice() = "/initial_slice"

    object Boundary {
        private const val PREFIX = "/boundary"

        fun inboundPermitted() = "$PREFIX/robots/\\d+/(updates|commands)"

        fun outboundPermitted() = "$PREFIX/robots/\\d+/updates"

        /**
         * Commands for a particular robot from outside the boundary.
         */
        fun commands(robotId: String) = "$PREFIX/robots/$robotId/commands"

        /**
         * Outbound state updates for a particular robot.
         */
        fun updates(robotId: String) = "$PREFIX/robots/$robotId/updates"
    }

    object Robots {
        private const val PREFIX = "/robots"

        /**
         * Message for a particular robot.
         */
        fun message(robotId: String) = "$PREFIX/$robotId/message"

        fun topicStart(robotId: String) = "$PREFIX/$robotId/topic"

        fun topicEnd(robotId: String) = "$PREFIX/$robotId/topic"

        fun topicMessage(robotId: String, topic: String) = "$PREFIX/$robotId/topic"
    }

    object Transport {
        private const val PREFIX = "/transport"

        fun execute(robotId: String) = "$PREFIX/$robotId/execute"

        fun stream(robotId: String) = "$PREFIX/$robotId/stream"

        fun message(robotId: String) = "$PREFIX/$robotId/message"
    }

    object Network {
        private const val PREFIX = "/network"

        fun executeInstruction(robotId: String) = "$PREFIX/$robotId/execute"
    }
}
