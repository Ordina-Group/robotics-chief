package nl.ordina.robotics.socket

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
sealed class Command {
    @Serializable
    data class UpdateHost(val host: String) : Command()

    @Serializable
    data class UpdateController(val mac: String) : Command()

    @Serializable
    data class ConnectWifi(val ssid: String, val password: String) : Command()

    @Serializable
    @SerialName("ScanBluetooth")
    data class ScanBluetooth(val scan: Boolean) : Command()

    @Serializable
    @SerialName("GetBluetoothDevices")
    object GetBluetoothDevices : Command()
}
