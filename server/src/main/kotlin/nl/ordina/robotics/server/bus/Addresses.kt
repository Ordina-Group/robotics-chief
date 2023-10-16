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
         * Commands for a particular robot.
         */
        fun commands(robotId: String) = "$PREFIX/$robotId/commands"

        /**
         * Message for a particular robot.
         */
        fun message(robotId: String) = "$PREFIX/$robotId/message"
    }
}
