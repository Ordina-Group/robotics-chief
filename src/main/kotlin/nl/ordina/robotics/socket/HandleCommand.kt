package nl.ordina.robotics.socket

import nl.ordina.robotics.ssh.commands.connectBluetooth
import nl.ordina.robotics.ssh.commands.connectWifi
import nl.ordina.robotics.ssh.commands.forgetWifi
import nl.ordina.robotics.ssh.commands.getBluetoothDevices
import nl.ordina.robotics.ssh.commands.getWifiNetworks
import nl.ordina.robotics.ssh.commands.listTopics
import nl.ordina.robotics.ssh.commands.robotConnection
import nl.ordina.robotics.ssh.commands.scanBluetooth
import nl.ordina.robotics.ssh.commands.subscribeTopic
import nl.ordina.robotics.ssh.commands.updateDomain
import nl.ordina.robotics.ssh.commands.updateHost
import nl.ordina.robotics.ssh.commands.wifiInfo

suspend fun SocketSession.handleCommand(command: Command) {
    val message = when (command) {
        is CheckRobotConnection -> robotConnection(command)
        is UpdateHost -> updateHost(command)
        is UpdateDomain -> updateDomain(command)
        is ConnectWifi -> connectWifi(command)
        is ForgetWifi -> forgetWifi(command)
        is GetWifiNetworks -> getWifiNetworks(command)
        is GetWifiInfo -> wifiInfo(command)
        is ScanBluetooth -> scanBluetooth(command)
        is GetBluetoothDevices -> getBluetoothDevices()
        is BluetoothConnect -> connectBluetooth(command)
        is ListTopics -> listTopics()
        is SubscribeTopic -> subscribeTopic(command)
        is UnsubscribeTopic -> {
            subscriptions[command.id]?.cancel()
            null
        }
    }

    if (message is Message) {
        sendMessage(message)
    }
}
