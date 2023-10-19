package nl.ordina.robotics.server.transport.cli.checks

import nl.ordina.robotics.server.socket.LaunchApp
import nl.ordina.robotics.server.socket.StatusLine
import nl.ordina.robotics.server.transport.cli.Cmd
import nl.ordina.robotics.server.transport.cli.Instruction
import nl.ordina.robotics.server.transport.cli.InstructionExecutor

suspend fun runningCheck(execute: InstructionExecutor): StatusLine {
    val running = execute(Instruction(Cmd.Ros.running)).resultOrError
    val runningParts = running.isNotEmpty()
    val runningMainProcess = running.contains(Cmd.Ros.mainCmdRunning)
    val runningMainAndController = runningMainProcess && running.split("\n").size >= 2

    return StatusLine(
        name = "Running",
        message = "PID $running",
        success = runningMainAndController,
        pending = runningParts && !runningMainAndController,
        commandLabel = if (runningParts) "Restart" else "Start",
        command = LaunchApp(restart = runningParts),
    )
}
