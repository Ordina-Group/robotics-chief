package nl.ordina.robotics.server.network.ssh

import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import net.harawata.appdirs.AppDirsFactory
import nl.ordina.robotics.server.robot.RobotSettings
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

    fun save(value: RobotSettings) {
        logger.info { "Saving settings" }
        logger.debug { "Persisting settings to ${configFile.absolutePath}" }
        try {
            configFile.parentFile.mkdirs()
            configFile.writeText(Json.encodeToString(value))
        } catch (e: Exception) {
            logger.error("Error saving settings: ${e.message}")
        }
    }

    fun load(): RobotSettings =
        if (configFile.exists()) {
            logger.debug { "Loading settings from filesystem" }
            json.decodeFromString<RobotSettings>(configFile.readText())
        } else {
            logger.debug { "Using default settings" }
            RobotSettings()
        }
}
