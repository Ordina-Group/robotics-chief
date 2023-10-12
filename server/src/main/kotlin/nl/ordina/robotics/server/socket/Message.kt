package nl.ordina.robotics.server.socket

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import nl.ordina.robotics.server.robot.Settings

@Serializable
sealed interface Message {
    val message: String
}

@Serializable
@SerialName("Message.Info")
data class Info(override val message: String) : Message

@Serializable
@SerialName("Message.CommandSuccess")
data class CommandSuccess(
    val command: String,
    override val message: String,
) : Message

@Serializable
@SerialName("Message.CommandFailure")
data class CommandFailure(
    val command: String,
    override val message: String,
) : Message

@Serializable
@SerialName("Message.RobotConnection")
data class RobotConnection(val connected: Boolean) : Message {
    override val message: String = "$connected"
}

@Serializable
@SerialName("Message.WifiNetworks")
data class WifiNetworks(
    val networks: List<WifiInfo>,
) : Message {
    override val message: String = "${networks.size} wireless networks"
}

@Serializable
@SerialName("Message.WifiInfo")
data class WifiInfo(
    val ssid: String,
    val signal: String,
    val rate: String,
    val connected: Boolean,
    val protocol: String,
    val known: Boolean,
) : Message {
    override val message: String = "$ssid $signal $rate"
}

@Serializable
@SerialName("Message.BluetoothDevices")
data class BluetoothDevices(val devices: List<Device>) : Message {
    override val message = "Bluetooth scan update"
}

@Serializable
@SerialName("Message.Topics")
data class Topics(val topics: List<Topic>) : Message {
    override val message = "Topics"
}

@Serializable
@SerialName("Message.TopicMessage")
data class TopicMessage(val topic: String, override val message: String) : Message

@Serializable
@SerialName("Message.Settings")
data class Settings(val value: Settings) : Message {
    override val message = "ssh settings"
}

@Serializable
data class Device(
    val name: String,
    val mac: String,
    val paired: Boolean,
    val connected: Boolean,
)

@Serializable
data class Topic(
    val id: String,
    val type: String,
    val count: Int,
)

@Serializable
@SerialName("Message.StatusTable")
data class StatusTable(
    val items: List<StatusLine>,
) : Message {
    override val message: String
        get() = "Status"
}

@Serializable
data class StatusLine(
    val name: String,
    val success: Boolean,
    val pending: Boolean,
    val message: String = "",
    val failure: String = "",
    val actionUrl: String? = null,
    val actionLabel: String? = null,
)
