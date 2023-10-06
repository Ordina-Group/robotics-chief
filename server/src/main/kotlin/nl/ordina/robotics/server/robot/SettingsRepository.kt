package nl.ordina.robotics.server.robot

import org.springframework.stereotype.Repository

@Repository
class SettingsRepository {
    fun all(): List<Settings> = listOf(
        Settings(),
    )
}
