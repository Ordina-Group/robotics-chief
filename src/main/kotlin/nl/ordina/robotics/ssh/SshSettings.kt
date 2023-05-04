package nl.ordina.robotics.ssh

import kotlinx.serialization.Serializable
import org.apache.sshd.client.session.ClientSession
import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds

@Serializable
data class SshSettings(
    val username: String = "jetson",
    val password: String = "jetson",
    val host: String = "192.168.55.1",
    val port: Int = 22,
    val controller: String = "20:21:06:16:1B:F3",
    val timeout: Duration = 5000.milliseconds,
    val workDir: String = "/home/jetson/robotics-workshop",
    internal var current: ClientSession? = null,
)
