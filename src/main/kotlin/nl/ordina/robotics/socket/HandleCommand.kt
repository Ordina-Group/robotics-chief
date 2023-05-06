package nl.ordina.robotics.socket

import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.withLock
import nl.ordina.robotics.ssh.Cmd
import nl.ordina.robotics.ssh.SshSettingsLoader
import nl.ordina.robotics.ssh.runSshCommand
import nl.ordina.robotics.ssh.streamSshCommand
import org.apache.sshd.common.SshException
import kotlin.time.Duration.Companion.milliseconds

suspend fun SocketSession.handleCommand(command: Command) {
    when (command) {
        is UpdateHost -> {
            sendMessage(Info("Updating host..."))
            settingsLock.withLock {
                settings = settings.copy(host = command.host.trim()).also(SshSettingsLoader::save)
            }
        }

        is UpdateController -> {
            sendMessage(Info("Updating controller..."))
            settingsLock.withLock {
                settings = settings.copy(controller = command.mac.trim()).also(SshSettingsLoader::save)
            }
        }

        is ConnectWifi -> {
            sendMessage(Info("Connecting to wifi network ${command.ssid}..."))
            try {
                val output = settings.runSshCommand(Cmd.Networking.connectWifi(command.ssid, command.password))

                if (output.contains("successfully activated with")) {
                    sendMessage(
                        CommandSuccess(
                            command = "Command.ConnectWifi",
                            message = "Connected to ${command.ssid}",
                        ),
                    )
                } else {
                    sendMessage(CommandFailure(command = "Command.ConnectWifi", message = output))
                }
            } catch (e: SshException) {
                sendMessage(
                    CommandFailure(
                        command = "Command.ConnectWifi",
                        message = e.message ?: "Unknown error connecting to wifi.",
                    ),
                )
            }
        }

        is ScanBluetooth -> {
            try {
                settings.runSshCommand(Cmd.Bluetooth.scan, timeout = 200.milliseconds)
            } catch (e: Exception) {
                sendMessage(
                    CommandFailure(
                        command = "Command.ScanBluetooth",
                        message = e.message ?: "Failed to scan.",
                    ),
                )
            }
        }

        is GetBluetoothDevices -> {
            try {
                val pairedDevices = settings.runSshCommand(Cmd.Bluetooth.paired)
                    .split("\n")
                    .filter { it.isNotEmpty() }
                    .map { it.split(' ')[1] }

                val devices = settings.runSshCommand(Cmd.Bluetooth.list)
                    .split("\n")
                    .map {
                        val (_, mac, name) = it.split(" ")
                        val paired = pairedDevices.contains(mac)
                        val connected = if (paired) {
                            settings.runSshCommand(Cmd.Bluetooth.info(mac)).contains("Connected: yes")
                        } else {
                            false
                        }

                        Device(
                            name,
                            mac,
                            paired,
                            connected,
                        )
                    }
                sendMessage(BluetoothDevices(devices))
            } catch (e: Exception) {
                sendMessage(
                    CommandFailure(
                        command = "Command.GetBluetoothDevices",
                        message = e.message ?: "Failed to list devices.",
                    ),
                )
            }
        }

        is BluetoothConnect -> TODO()

        is ListTopics -> {
            val topics = runSshCommand(
                Cmd.Ros.sourceBash,
                Cmd.Ros.listTopics(settings.domainId),
            )
                .split('\n')
                .filter { it.startsWith('/') }
                .map { id -> Topic(id, -1) }

            sendMessage(
                Topics(topics),
            )
        }

        is SubscribeTopic -> {
            subscriptions[command.id]?.cancel()

            subscriptions[command.id] = session.launch {
                streamSshCommand(
                    Cmd.Ros.sourceBash,
                    Cmd.Ros.subscribeTopic(settings.domainId, command.id),
                )
                    .onEach { sendMessage(TopicMessage(command.id, it)) }
                    .collect()
            }
        }

        is UnsubscribeTopic -> {
            subscriptions[command.id]?.cancel()
        }
    }
}
