package nl.ordina.robotics.server.ssh

import jakarta.annotation.PostConstruct
import nl.ordina.robotics.server.robot.RobotRepository
import org.springframework.stereotype.Service

@Service
class SettingsLoader(
    private val settingsRepository: SettingsRepository,
    private val robotRepository: RobotRepository,
) {
    @PostConstruct
    fun initRobots() {
        println("TEST: ${settingsRepository.all()}")

        settingsRepository.all().forEach {
            robotRepository.create(it.id, it)
        }
    }
}
