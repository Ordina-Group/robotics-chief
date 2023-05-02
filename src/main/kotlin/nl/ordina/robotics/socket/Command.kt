package nl.ordina.robotics.socket

import kotlinx.serialization.Serializable

@Serializable
sealed class Command {
    @Serializable
    data class UpdateHost(val host: String) : Command()
}
