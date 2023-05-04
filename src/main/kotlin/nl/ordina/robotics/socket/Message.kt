package nl.ordina.robotics.socket

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import nl.ordina.robotics.ssh.SshSettings

@Serializable
sealed class Message {
    abstract val message: String

    @Serializable
    class Haling(override val message: String = "Chief says hi!") : Message()

    @Serializable
    data class Info(override val message: String) : Message()

    @Serializable
    data class CommandResult(val command: String, override val message: String) : Message()

    @Serializable
    data class CommandSuccess(val command: String, override val message: String) : Message()

    @Serializable
    data class CommandFailure(val command: String, override val message: String) : Message()

    @Serializable
    @SerialName("BluetoothDevices")
    data class BluetoothDevices(val devices: List<Device>) : Message() {
        override val message = "Bluetooth scan update"
    }

    @Serializable
    data class Settings(val value: SshSettings) : Message() {
        override val message = "ssh settings"
    }
}

@Serializable
data class Device(
    val name: String,
    val mac: String,
    val paired: Boolean,
    val connected: Boolean,
)

@Serializable
@SerialName("StatusTable")
data class StatusTable(
    val items: List<StatusLine>
) : Message() {
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
    val actionLabel: String? = null
)
