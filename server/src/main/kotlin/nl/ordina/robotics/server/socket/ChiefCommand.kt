package nl.ordina.robotics.server.socket

import kotlinx.serialization.Serializable
import nl.ordina.robotics.server.robot.RobotSettings

@Serializable
sealed class ChiefCommand

@Serializable
class AddRobot(val robotSettings: RobotSettings) : ChiefCommand()

@Serializable
class UpdateRobot(val robotSettings: RobotSettings) : ChiefCommand()

@Serializable
class RemoveRobot(val robotId: String) : ChiefCommand()
