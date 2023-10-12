package nl.ordina.robotics.server.robot

import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient
import org.apache.sshd.client.session.ClientSession
import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds

@Serializable
class Settings {
    val id: String = "3"
    val username: String = "jetson"
    val password: String = "jetson"
    val host: String = "192.168.55.1"
    val port: Int = 22
    val domainId: Int = 8
    val timeout: Duration = 500.milliseconds
    val workDir: String = "/home/jetson/robotics-workshop"

    @Transient
    internal var current: ClientSession? = null
}
