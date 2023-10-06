package nl.ordina.robotics.server.socket

import nl.ordina.robotics.server.robot.Robot
import nl.ordina.robotics.server.ssh.Cmd.Networking.connectWifi
import nl.ordina.robotics.server.ssh.Cmd.Ros.listTopics
import nl.ordina.robotics.server.ssh.Cmd.Ros.subscribeTopic
import nl.ordina.robotics.server.ssh.CommandExecutor
import nl.ordina.robotics.server.ssh.commands.connectBluetooth
import nl.ordina.robotics.server.ssh.commands.connectWifi
import nl.ordina.robotics.server.ssh.commands.forgetWifi
import nl.ordina.robotics.server.ssh.commands.getBluetoothDevices
import nl.ordina.robotics.server.ssh.commands.getWifiNetworks
import nl.ordina.robotics.server.ssh.commands.listTopics
import nl.ordina.robotics.server.ssh.commands.robotConnection
import nl.ordina.robotics.server.ssh.commands.scanBluetooth
import nl.ordina.robotics.server.ssh.commands.subscribeTopic
import nl.ordina.robotics.server.ssh.commands.wifiInfo

suspend fun Robot.handleCommand(executor: CommandExecutor, command: Command): Message? {
    return when (command) {
        is CheckRobotConnection -> robotConnection(executor, command)
        is UpdateHost -> TODO() // updateHost(executor, command)
        is UpdateDomain -> TODO() // updateDomain(executor, command)
        is ConnectWifi -> connectWifi(executor, command)
        is ForgetWifi -> forgetWifi(executor, command)
        is GetWifiNetworks -> getWifiNetworks(executor, command)
        is GetWifiInfo -> wifiInfo(executor, command)
        is ScanBluetooth -> scanBluetooth(executor, command)
        is GetBluetoothDevices -> getBluetoothDevices(executor)
        is BluetoothConnect -> connectBluetooth(executor, command)
        is ListTopics -> listTopics(executor)
        is SubscribeTopic -> subscribeTopic(executor, command)
        is UnsubscribeTopic -> {
            println("TODO: UNSUBSCRIBE TOPIC")
//            sshSession.subscriptions[command.id]?.cancel()
            null
        }
    }
}
