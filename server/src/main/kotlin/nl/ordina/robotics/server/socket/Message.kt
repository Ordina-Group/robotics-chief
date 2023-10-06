package nl.ordina.robotics.server.socket

import nl.ordina.robotics.server.ssh.SshSettings

sealed interface Message {
    val message: String
}

data class Info(override val message: String) : Message {
    val type = "Message.Info"
}

data class CommandSuccess(val command: String, override val message: String) : Message {
    val type = "Message.CommandSuccess"
}

data class CommandFailure(val command: String, override val message: String) : Message {
    val type = "Message.CommandFailure"
}

data class RobotConnection(val connected: Boolean) : Message {
    val type = "Message.RobotConnection"

    override val message: String = "$connected"
}

data class WifiNetworks(
    val networks: List<WifiInfo>,
) : Message {
    val type = "Message.WifiNetworks"

    override val message: String = "${networks.size} wireless networks"
}

data class WifiInfo(
    val ssid: String,
    val signal: String,
    val rate: String,
    val connected: Boolean,
    val protocol: String,
    val known: Boolean,
) : Message {
    val type = "Message.WifiInfo"

    override val message: String = "$ssid $signal $rate"
}

data class BluetoothDevices(val devices: List<Device>) : Message {
    val type = "Message.BluetoothDevices"

    override val message = "Bluetooth scan update"
}

data class Topics(val topics: List<Topic>) : Message {
    val type = "Message.Topics"

    override val message = "Topics"
}

data class TopicMessage(val topic: String, override val message: String) : Message {
    val type = "Message.TopicMessage"
}

data class Settings(val value: SshSettings) : Message {
    val type = "Message.Settings"

    override val message = "ssh settings"
}

data class Device(
    val name: String,
    val mac: String,
    val paired: Boolean,
    val connected: Boolean,
)

data class Topic(
    val id: String,
    val type: String,
    val count: Int,
)

data class StatusTable(
    val items: List<StatusLine>,
) : Message {
    val type: String
        get() = "Message.StatusTable"

    override val message: String
        get() = "Status"
}

data class StatusLine(
    val name: String,
    val success: Boolean,
    val pending: Boolean,
    val message: String = "",
    val failure: String = "",
    val actionUrl: String? = null,
    val actionLabel: String? = null,
)
