package nl.ordina.robotics.server.network.mock

import io.github.oshai.kotlinlogging.KotlinLogging
import io.vertx.kotlin.coroutines.CoroutineVerticle
import nl.ordina.robotics.server.bus.Addresses
import nl.ordina.robotics.server.socket.RobotConnection
import nl.ordina.robotics.server.socket.publishMessage
import nl.ordina.robotics.server.transport.cli.Cmd
import nl.ordina.robotics.server.transport.cli.Instruction
import nl.ordina.robotics.server.transport.cli.InstructionResult
import kotlin.random.Random

class MockNetworkVerticle : CoroutineVerticle() {
    private val logger = KotlinLogging.logger {}
    private var lastStrength = 94
    private lateinit var robotId: String

    override suspend fun start() {
        val eb = vertx.eventBus()
        robotId = config.getString("robot.id")
        val domainId = config.getString("robot.domainId")

        logger.info { "[ROBOT $robotId] Starting mock network" }

        eb.consumer(Addresses.Network.executeInstruction(robotId)) { msg ->
            val instruction = msg.body()

            msg.reply(getResult(instruction, domainId))
        }

        vertx.setPeriodic(200) {
            val connected = Random.nextFloat() < 0.99

            eb.publishMessage(Addresses.Network.message(robotId), RobotConnection(connected))
        }
    }

    private fun getResult(instruction: Instruction, domainId: String): InstructionResult {
        if (Random.nextFloat() > 0.99) {
            logger.error { "Mock error for $instruction" }
            return InstructionResult(null, "Mock error")
        }

        return when (instruction.value) {
            listOf(Cmd.Unix.osInfo) -> InstructionResult("Mock Linux", null)
            listOf(Cmd.Unix.userInfo) -> InstructionResult("Mockabee", null)
            listOf(Cmd.Unix.hostname) -> InstructionResult("Mockchine", null)
            listOf(Cmd.Networking.connectionInfo) -> InstructionResult(fakeWifiConnection(), null)
            listOf(Cmd.Networking.listStoredNetworks) -> InstructionResult("MockAir:", null)
            listOf(Cmd.Networking.listWirelessNetworks) -> InstructionResult(
                "*:00\\:00\\:00\\:00\\:00\\:00:MockAir:Infra:87:2 Gbit/s:64:▂▄▆_:WPA2 802.1X",
                null,
            )

            listOf(
                Cmd.Ros.sourceBash,
                Cmd.Ros.listTopics(domainId.toInt()),
            ),
            -> InstructionResult("/MockTopic\n/CameraTopic", null)

            listOf(Cmd.Networking.ipAddresses) -> InstructionResult(
                """
                    lo               UNKNOWN        127.0.0.1/8 
                    l4tbr0           UP             192.168.55.1/24 
                    docker0          DOWN           172.17.0.1/16 
                    eth0@if16        UP             fc00::bad:c0f:fee/64
                """.trimIndent(),
            )

            listOf(Cmd.Git.status) -> InstructionResult(
                """
                    On branch master
                    Your branch is up to date with 'origin/master'.
                    
                    nothing to commit, working tree clean
                """.trimIndent(),
            )

            listOf("git --no-pager log --decorate --oneline -1") -> InstructionResult(
                "0a1b2c3 (HEAD -> master, origin/master, origin/HEAD) Mock commit",
            )

            listOf(Cmd.Unix.list(".")) -> InstructionResult(
                """
                    total 8.0K
                    drwxr-xr-x  2 root root 4.0K Mar  1  2021 .
                    drwxr-xr-x 18 root root 4.0K Mar  1  2021 ..
                    -rw-r--r--  1 root root  151 Mar  1  2021 README.md
                """.trimIndent(),
            )

            listOf(Cmd.Unix.list("./build")) -> InstructionResult("")

            listOf("pgrep -f /usr/bin/colcon") -> InstructionResult("4", null)

            listOf(Cmd.Ros.running) -> InstructionResult("1234")

            listOf(Cmd.Bluetooth.list) -> InstructionResult(
                """
                    Device 00:00:00:00:00:00 MockDevice
                    Device 12:34:56:78:90:ab MockDevice2
                    Device af:02:5b:3c:4d:ef MockDevice3
                """.trimIndent(),
            )

            listOf(Cmd.Bluetooth.paired) -> InstructionResult(
                """
                    Device 00:00:00:00:00:00 MockAir
                """.trimIndent(),
            )

            listOf(Cmd.Bluetooth.info("00:00:00:00:00:00")) -> InstructionResult(
                deviceInfo("00:00:00:00:00:00", "MockAir", true, false),
            )
            listOf(Cmd.Bluetooth.info("12:34:56:78:90:ab")) -> InstructionResult(
                deviceInfo("12:34:56:78:90:ab", "MockAir2", true, true),
            )
            listOf(Cmd.Bluetooth.info("af:02:5b:3c:4d:ef")) -> InstructionResult(
                deviceInfo("af:02:5b:3c:4d:ef", "MockAir3", false, false),
            )

            else -> InstructionResult(null, "Not implemented")
                .also { logger.warn { "Not implemented: $instruction" } }
        }
    }

    private fun fakeWifiConnection(): String = buildString {
        val strength = System.currentTimeMillis().toInt() % 100
        if (strength in 70..99) {
            lastStrength = strength
        }

        appendLine("ESSID:MockAir\n")
        appendLine("Bit Rate=2Gb/s\n\n")
        appendLine("Signal level= -$lastStrength dbm")
    }

    private fun deviceInfo(mac: String, name: String, paired: Boolean, connected: Boolean): String =
        """
            Device $mac (public)
                Name: $name
                Alias: $name
                Class: 0x000000
                Icon: audio-card
                Paired: ${if (paired) "yes" else "no"}
                Trusted: yes
                Blocked: no
                Connected: ${if (connected) "yes" else "no"}
        """.trimIndent()
}
