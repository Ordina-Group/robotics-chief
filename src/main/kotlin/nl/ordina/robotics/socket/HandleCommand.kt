package nl.ordina.robotics.socket

import nl.ordina.robotics.ssh.commands.connectBluetooth
import nl.ordina.robotics.ssh.commands.connectWifi
import nl.ordina.robotics.ssh.commands.getBluetoothDevices
import nl.ordina.robotics.ssh.commands.listTopics
import nl.ordina.robotics.ssh.commands.scanBluetooth
import nl.ordina.robotics.ssh.commands.subscribeTopic
import nl.ordina.robotics.ssh.commands.updateHost

suspend fun SocketSession.handleCommand(command: Command) {
    val message = when (command) {
        is UpdateHost -> updateHost(command)
        is ConnectWifi -> connectWifi(command)
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
