package nl.ordina.robotics.server.robot

class SettingsLoader(
    private val settingsRepository: SettingsRepository,
    private val robotRepository: RobotRepository,
) {
//    @PostConstruct
    fun initRobots() {
        settingsRepository.all().forEach {
            robotRepository.create(it.id, it)
        }
    }
}
