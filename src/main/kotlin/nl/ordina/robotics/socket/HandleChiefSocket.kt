package nl.ordina.robotics.socket

import io.ktor.server.websocket.DefaultWebSocketServerSession
import io.ktor.websocket.Frame
import io.ktor.websocket.readText
import io.ktor.websocket.send
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.joinAll
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import nl.ordina.robotics.ssh.Cmd
import nl.ordina.robotics.ssh.SshSettingsLoader
import nl.ordina.robotics.ssh.runSshCommand
import org.apache.sshd.common.SshException
import kotlin.time.Duration.Companion.milliseconds

private val socketSerializer = Json {
    encodeDefaults = true
}

private suspend fun DefaultWebSocketServerSession.sendMessage(message: Message) {
    send(socketSerializer.encodeToString(message))
}

@OptIn(DelicateCoroutinesApi::class)
suspend fun DefaultWebSocketServerSession.handleChiefSocket() {
    sendMessage(Message.Haling())
    val settingsLock = Mutex(false)
    var settings = SshSettingsLoader.load()

    sendMessage(Message.Settings(settings))

    val broadcaster = launch(Dispatchers.IO) {
        var lastValue: Message? = null

        while (this.isActive && !incoming.isClosedForReceive) {
            val result = try {
                settingsLock.withLock {
                    SshCommands.debug(settings)
                }
            } catch (e: Exception) {
                Message.CommandResult("debug", e.message ?: "")
            }
            if (lastValue != result) {
                sendMessage(result)
                lastValue = result
            }
            delay(500)
        }
    }

    val listener = launch(Dispatchers.IO) {
        for (frame in incoming) {
            frame as? Frame.Text ?: continue
            val receivedText = frame.readText()

            when (val command = Json.decodeFromString<Command>(receivedText)) {
                is Command.UpdateHost -> {
                    sendMessage(Message.Info("Updating host..."))
                    settingsLock.withLock {
                        settings = settings.copy(host = command.host.trim()).also(SshSettingsLoader::save)
                    }
                }

                is Command.UpdateController -> {
                    sendMessage(Message.Info("Updating controller..."))
                    settingsLock.withLock {
                        settings = settings.copy(controller = command.mac.trim()).also(SshSettingsLoader::save)
                    }
                }

                is Command.ConnectWifi -> {
                    sendMessage(Message.Info("Connecting to wifi network ${command.ssid}..."))
                    try {
                        val output = settings.runSshCommand(Cmd.Networking.connectWifi(command.ssid, command.password))

                        if (output.contains("successfully activated with")) {
                            sendMessage(
                                Message.CommandSuccess(
                                    command = "ConnectWifi",
                                    message = "Connected to ${command.ssid}",
                                ),
                            )
                        } else {
                            sendMessage(Message.CommandFailure(command = "ConnectWifi", message = output))
                        }
                    } catch (e: SshException) {
                        sendMessage(
                            Message.CommandFailure(
                                command = "ConnectWifi",
                                message = e.message ?: "Unknown error connecting to wifi.",
                            ),
                        )
                    }
                }

                is Command.ScanBluetooth -> {
                    try {
                        settings.runSshCommand(Cmd.Bluetooth.scan, timeout = 200.milliseconds)
                    } catch (e: Exception) {
                        sendMessage(
                            Message.CommandFailure(
                                command = "ScanBluetooth",
                                message = e.message ?: "Failed to scan.",
                            ),
                        )
                    }
                }

                is Command.GetBluetoothDevices -> {
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
                        sendMessage(Message.BluetoothDevices(devices))
                    } catch (e: Exception) {
                        sendMessage(
                            Message.CommandFailure(
                                command = "GetBluetoothDevices",
                                message = e.message ?: "Failed to list devices.",
                            ),
                        )
                    }
                }
            }
        }
    }

    joinAll(broadcaster, listener)
}
