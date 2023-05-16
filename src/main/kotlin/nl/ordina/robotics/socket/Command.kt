package nl.ordina.robotics.socket

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
sealed interface Command {
    val name: String
}

@Serializable
@SerialName("Command.CheckRobotConnection")
data class CheckRobotConnection(override val name: String = "Command.CheckRobotConnection") : Command

@Serializable
@SerialName("Command.UpdateHost")
data class UpdateHost(val host: String) : Command {
    override val name = "Command.UpdateHost"
}

@Serializable
@SerialName("Command.UpdateDomain")
data class UpdateDomain(val domain: Int) : Command {
    override val name = "Command.UpdateDomain"
}

@Serializable
@SerialName("Command.ConnectWifi")
data class ConnectWifi(val ssid: String, val password: String? = null) : Command {
    override val name = "Command.ConnectWifi"
}

@Serializable
@SerialName("Command.ForgetWifi")
data class ForgetWifi(val ssid: String) : Command {
    override val name = "Command.ForgetWifi"
}

@Serializable
@SerialName("Command.GetWifiNetworks")
object GetWifiNetworks : Command {
    override val name = "Command.GetWifiNetworks"
}

@Serializable
@SerialName("Command.GetWifiInfo")
data class GetWifiInfo(override val name: String = "Command.GetWifiInfo") : Command

@Serializable
@SerialName("Command.ScanBluetooth")
data class ScanBluetooth(val scan: Boolean) : Command {
    override val name = "Command.ScanBluetooth"
}

@Serializable
@SerialName("Command.BluetoothConnect")
data class BluetoothConnect(val mac: String) : Command {
    override val name = "Command.BluetoothConnect"
}

@Serializable
@SerialName("Command.GetBluetoothDevices")
object GetBluetoothDevices : Command {
    override val name = "Command.GetBluetoothDevices"
}

@Serializable
@SerialName("Command.ListTopics")
object ListTopics : Command {
    override val name = "Command.ListTopics"
}

@Serializable
@SerialName("Command.SubscribeTopic")
data class SubscribeTopic(val id: String) : Command {
    override val name = "Command.SubscribeTopic"
}

@Serializable
@SerialName("Command.UnsubscribeTopic")
data class UnsubscribeTopic(val id: String) : Command {
    override val name = "Command.UnsubscribeTopic"
}
