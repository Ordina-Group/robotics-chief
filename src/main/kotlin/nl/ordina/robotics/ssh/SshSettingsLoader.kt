package nl.ordina.robotics.ssh

import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import net.harawata.appdirs.AppDirsFactory
import java.io.File

object SshSettingsLoader {
    private val json = Json {
        ignoreUnknownKeys = true
    }
    private val configDir: String = AppDirsFactory.getInstance().getUserConfigDir(
        "robochief",
        "0.0.1",
        "ordina",
    )

    private val configFile = File(configDir, "settings.json")

    fun save(value: SshSettings) {
        try {
            configFile.parentFile.mkdirs()
            configFile.writeText(Json.encodeToString(value))
        } catch (e: Exception) {
            println("Error saving settings: ${e.message}")
        }
    }

    fun load(): SshSettings =
        if (configFile.exists()) {
            json.decodeFromString<SshSettings>(configFile.readText()).also(::println)
        } else {
            SshSettings()
        }
}
