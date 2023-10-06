package nl.ordina.robotics.server.robot

import jakarta.annotation.PostConstruct
import org.springframework.stereotype.Service

@Service
class SettingsLoader(
    private val settingsRepository: SettingsRepository,
    private val robotRepository: RobotRepository,
) {
    @PostConstruct
    fun initRobots() {
        settingsRepository.all().forEach {
            robotRepository.create(it.id, it)
        }
    }
}
