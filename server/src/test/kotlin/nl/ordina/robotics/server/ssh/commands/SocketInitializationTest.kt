package nl.ordina.robotics.server.ssh.commands

import io.ktor.client.plugins.websocket.webSocket
import io.ktor.websocket.Frame
import nl.ordina.robotics.socket.Info
import nl.ordina.robotics.socket.Settings
import nl.ordina.robotics.socket.UpdateHost
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertNotNull

class SocketInitializationTest {
    @Test
    fun testGreetsAndGivesSettingsOnConnect() = withApp { client ->
        client.webSocket("/subscribe") {
            val greetingText = receiveText()
            assertNotNull(greetingText)
            assertEquals(Info("Chief says hi!").toJson(), greetingText)

            val settings = receiveMessage()
            assertNotNull(settings)
            assertIs<Settings>(settings)
        }
    }

    @Test
    fun testUpdatesTheHost() = withReadySocketSession {
        outgoing.send(Frame.Text(UpdateHost("10.0.0.1").toJson()))

        val info = receiveMessageOfType<Info>()
        assertNotNull(info)
        assertEquals("Updating host...", info.message)

        val settings = receiveMessageOfType<Settings>()
        assertNotNull(settings)
        assertEquals("10.0.0.1", settings.value.host)
    }
}
