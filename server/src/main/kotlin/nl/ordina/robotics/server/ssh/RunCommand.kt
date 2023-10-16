package nl.ordina.robotics.server.ssh

import io.github.oshai.kotlinlogging.KotlinLogging
import io.opentelemetry.instrumentation.annotations.WithSpan
import io.vertx.kotlin.coroutines.awaitBlocking
import org.apache.sshd.client.session.ClientSession
import java.rmi.RemoteException

private val logger = KotlinLogging.logger {}

suspend fun ClientSession.runCommand(command: String): String = awaitBlocking {
    try {
        execCommand(command)
    } catch (e: Exception) {
        logger.error { "Error running SSH command: ${e.message}" }
        e.printStackTrace()
        ""
    }
}

@WithSpan
private fun ClientSession.execCommand(command: String): String = try {
    executeRemoteCommand(command)
} catch (e: RemoteException) {
    e.detail.message ?: "Unknown error"
}
