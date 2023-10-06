package nl.ordina.robotics.server.ssh

import org.springframework.stereotype.Repository

@Repository
class SettingsRepository {
    fun all(): List<SshSettings> = listOf(
        SshSettings(),
    )
}
