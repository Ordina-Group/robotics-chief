package nl.ordina.robotics.server.robot

import org.springframework.stereotype.Repository

@Repository
class RobotRepository {
    private val robots: MutableList<Robot> = mutableListOf()

    fun create(id: String, settings: Settings) {
        require(get(id) == null) {
            "Robot with id $id already exists"
        }

        robots.add(Robot(RobotId(id), settings))
    }

    fun get(id: String): Robot? = robots.find { it.id == RobotId(id) }

    fun all(): List<Robot> = robots.toList()
}
