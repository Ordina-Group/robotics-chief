package nl.ordina.robotics.server.transport.cli.commands

import nl.ordina.robotics.server.robot.RobotSettings
import nl.ordina.robotics.server.socket.BluetoothConnect
import nl.ordina.robotics.server.socket.BluetoothDisconnect
import nl.ordina.robotics.server.socket.ClientAction
import nl.ordina.robotics.server.socket.Command
import nl.ordina.robotics.server.socket.ConnectWifi
import nl.ordina.robotics.server.socket.CreateStatusTable
import nl.ordina.robotics.server.socket.CustomCommand
import nl.ordina.robotics.server.socket.ForgetWifi
import nl.ordina.robotics.server.socket.GetBluetoothDevices
import nl.ordina.robotics.server.socket.GetWifiInfo
import nl.ordina.robotics.server.socket.GetWifiNetworks
import nl.ordina.robotics.server.socket.LaunchApp
import nl.ordina.robotics.server.socket.ListTopics
import nl.ordina.robotics.server.socket.ScanBluetooth
import nl.ordina.robotics.server.socket.SubscribeTopic
import nl.ordina.robotics.server.socket.UnsubscribeTopic
import nl.ordina.robotics.server.socket.UpdateDomain
import nl.ordina.robotics.server.socket.UpdateHost
import nl.ordina.robotics.server.transport.cli.Script
import nl.ordina.robotics.server.transport.cli.checks.CreateStatusTableInstruction

fun Command.toInstructionSet(robotSettings: RobotSettings): Script = when (this) {
    is CreateStatusTable -> CreateStatusTableInstruction()
    is CustomCommand -> CustomCommandInstruction(this)
    is ConnectWifi -> ConnectWifiInstruction(this)
    is GetWifiNetworks -> GetWifiNetworksInstruction()
    is GetWifiInfo -> GetWifiInfoInstruction(this)
    is ForgetWifi -> ForgetWifiInstruction(this)
    is GetBluetoothDevices -> GetBluetoothDevicesInstruction()
    is BluetoothConnect -> ConnectBluetoothInstruction(this)
    is BluetoothDisconnect -> DisconnectBluetoothInstruction(this)
    is ScanBluetooth -> ScanBluetoothInstruction()
    is LaunchApp -> LaunchAppInstruction(this, robotSettings.domainId)

    is ListTopics -> ListTopicsInstruction(robotSettings.domainId)
    is SubscribeTopic -> Unimplemented(this)
    is UnsubscribeTopic -> Unimplemented(this)
    is UpdateDomain -> Unimplemented(this)
    is UpdateHost -> Unimplemented(this)
    is ClientAction -> error("Action is not a server-side command.")
}
