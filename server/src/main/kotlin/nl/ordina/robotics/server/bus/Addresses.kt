package nl.ordina.robotics.server.bus

object Addresses {
    fun initialSlice() = "/initial_slice"

    object Robots {
        private const val PREFIX = "/robots"

        /**
         * Inbound external commands for a particular robot.
         */
        fun commands(robotId: String) = "$PREFIX/$robotId/commands"

        /**
         * Outbound state updates for a particular robot.
         */
        fun updates(robotId: String) = "$PREFIX/$robotId/updates"

        /**
         * Commands to be executed on a particular robot.
         */
        fun commandsInternal(robotId: String) = "$PREFIX/$robotId/commands/internal"

        /**
         * Message for a particular robot.
         */
        fun message(robotId: String) = "$PREFIX/$robotId/message"
    }
}
