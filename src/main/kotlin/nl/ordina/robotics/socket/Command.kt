package nl.ordina.robotics.socket

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
sealed interface Command

@Serializable
@SerialName("Command.UpdateHost")
data class UpdateHost(val host: String) : Command

@Serializable
@SerialName("Command.UpdateController")
data class UpdateController(val mac: String) : Command

@Serializable
@SerialName("Command.ConnectWifi")
data class ConnectWifi(val ssid: String, val password: String) : Command

@Serializable
@SerialName("Command.ScanBluetooth")
data class ScanBluetooth(val scan: Boolean) : Command

@Serializable
@SerialName("Command.GetBluetoothDevices")
object GetBluetoothDevices : Command
