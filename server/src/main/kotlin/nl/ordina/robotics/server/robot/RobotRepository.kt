package nl.ordina.robotics.server.robot

class RobotRepository {
    private val robots: MutableList<Robot> = mutableListOf()

    fun create(id: String, settings: Settings): Robot {
        require(get(id) == null) {
            "Robot with id $id already exists"
        }

        return Robot(RobotId(id), settings)
            .also { robots.add(it) }
    }

    fun get(id: String): Robot? = robots.find { it.id == RobotId(id) }

    fun all(): List<Robot> = robots.toList()
}
