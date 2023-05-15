package nl.ordina.robotics.ssh

import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import mu.KotlinLogging
import net.harawata.appdirs.AppDirsFactory
import java.io.File

object SshSettingsLoader {
    private val logger = KotlinLogging.logger {}
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
        logger.debug { "Persisting settings to ${configFile.absolutePath}" }
        try {
            configFile.parentFile.mkdirs()
            configFile.writeText(Json.encodeToString(value))
        } catch (e: Exception) {
            logger.error("Error saving settings: ${e.message}")
        }
    }

    fun load(): SshSettings =
        if (configFile.exists()) {
            logger.debug { "Loading settings from filesystem" }
            json.decodeFromString<SshSettings>(configFile.readText())
        } else {
            logger.debug { "Using default settings" }
            SshSettings()
        }
}
