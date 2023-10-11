package nl.ordina.robotics.server.messaging

data class Envelope<out T>(
    val topic: String,
    val body: T,
)
