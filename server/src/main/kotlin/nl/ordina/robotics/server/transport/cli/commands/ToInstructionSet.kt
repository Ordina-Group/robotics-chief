package nl.ordina.robotics.server.transport.cli.commands

import nl.ordina.robotics.server.socket.BluetoothConnect
import nl.ordina.robotics.server.socket.Command
import nl.ordina.robotics.server.socket.ConnectWifi
import nl.ordina.robotics.server.socket.CreateStatusTable
import nl.ordina.robotics.server.socket.ForgetWifi
import nl.ordina.robotics.server.socket.GetBluetoothDevices
import nl.ordina.robotics.server.socket.GetWifiInfo
import nl.ordina.robotics.server.socket.GetWifiNetworks
import nl.ordina.robotics.server.socket.ListTopics
import nl.ordina.robotics.server.socket.ScanBluetooth
import nl.ordina.robotics.server.socket.SubscribeTopic
import nl.ordina.robotics.server.socket.UnsubscribeTopic
import nl.ordina.robotics.server.socket.UpdateDomain
import nl.ordina.robotics.server.socket.UpdateHost
import nl.ordina.robotics.server.transport.cli.InstructionSet
import nl.ordina.robotics.server.transport.cli.checks.CreateStatusTableInstruction

fun Command.toInstructionSet(): InstructionSet = when (this) {
    is CreateStatusTable -> CreateStatusTableInstruction()
    is ConnectWifi -> ConnectWifiInstruction(this)
    is GetWifiNetworks -> GetWifiNetworksInstruction()
    is GetWifiInfo -> GetWifiInfoInstruction(this)
    is ForgetWifi -> ForgetWifiInstruction(this)
    is GetBluetoothDevices -> GetBluetoothDevicesInstruction()
    is BluetoothConnect -> ConnectBluetoothInstruction(this)
    is ScanBluetooth -> ScanBluetoothInstruction()

    is ListTopics -> TODO()
    is SubscribeTopic -> TODO()
    is UnsubscribeTopic -> TODO()
    is UpdateDomain -> TODO()
    is UpdateHost -> TODO()
}
