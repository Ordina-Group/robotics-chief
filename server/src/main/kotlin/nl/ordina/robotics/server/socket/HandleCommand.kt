package nl.ordina.robotics.server.socket

import nl.ordina.robotics.server.robot.CommandExecutor
import nl.ordina.robotics.server.robot.Robot
import nl.ordina.robotics.server.ssh.checks.createSshStatusTable
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
        is CreateStatusTable -> createSshStatusTable(this, executor)
        is CheckRobotConnection -> robotConnection(executor, command)
        is UpdateHost -> Info("UpdateHost not implemented yet") // updateHost(executor, command)
        is UpdateDomain -> Info("UpdateHost not implemented yet") // updateDomain(executor, command)
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
