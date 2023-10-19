package nl.ordina.robotics.server.robot

class RobotSettingsRepository {
    private val items: MutableList<RobotSettings> = mutableListOf()

    fun create(id: String, settings: RobotSettings): RobotSettings {
        require(get(id) == null) {
            "Robot with id $id already exists"
        }

        return settings
            .also { items.add(it) }
    }

    fun get(id: String): RobotSettings? = items.find { it.id == id }

    fun all(): List<RobotSettings> = items.toList()
}
