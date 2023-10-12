package nl.ordina.robotics.server.ssh.commands

import nl.ordina.robotics.socket.WifiInfo
import kotlin.test.Test
import kotlin.test.assertEquals

class WifiTest {
    @Test
    fun testConnectionInfoParsing() {
        val output = """
            wlan0     IEEE 802.11  ESSID:"KPN79F826."  
                      Mode:Managed  Frequency:5.56 GHz  Access Point: 6C:BA:B8:79:F8:2A   
                      Bit Rate=433.3 Mb/s   Tx-Power=22 dBm   
                      Retry short limit:7   RTS thr:off   Fragment thr:off
                      Power Management:on
                      Link Quality=52/70  Signal level=-58 dBm  
                      Rx invalid nwid:0  Rx invalid crypt:0  Rx invalid frag:0
                      Tx excessive retries:0  Invalid misc:79   Missed beacon:0

        """.trimIndent()

        val info = output.parseWifiInfo()

        assertEquals("KPN79F826.", info.ssid)
        assertEquals("-58 dBm", info.signal)
        assertEquals("433.3 Mb/s", info.rate)
    }

    @Test
    fun testWifiNetworksOutputParsing() {
        val output = """
             :B4\:B0\:24\:65\:81\:A6:Ordina-Robotics:Infra:10:405 Mbit/s:100:▂▄▆█:WPA1 WPA2
             :88\:46\:87\:BF\:D8\:12:OrdinaNL:Infra:11:195 Mbit/s:72:▂▄▆_:WPA2 802.1X
             :9D\:46\:87\:BF\:D8\:12:OrdinaNLGuest:Infra:11:195 Mbit/s:72:▂▄▆_:WPA2
             :86\:46\:87\:BF\:D8\:12:OrdinaNLSonos:Infra:11:195 Mbit/s:72:▂▄▆_:WPA2 WPA3
             :B6\:46\:87\:BF\:D8\:12::Infra:11:195 Mbit/s:70:▂▄▆_:WPA2
             :B6\:46\:87\:BF\:D8\:45::Infra:1:195 Mbit/s:69:▂▄▆_:WPA2
             :88\:46\:87\:BF\:D8\:45:OrdinaNL:Infra:1:195 Mbit/s:64:▂▄▆_:WPA2 802.1X
             :86\:46\:87\:BF\:D8\:45:OrdinaNLSonos:Infra:1:195 Mbit/s:64:▂▄▆_:WPA2 WPA3
             :9D\:46\:87\:BF\:D8\:45:OrdinaNLGuest:Infra:1:195 Mbit/s:64:▂▄▆_:WPA2
            *:8A\:46\:54\:BF\:D8\:45:OrdinaNL:Infra:87:195 Mbit/s:64:▂▄▆_:WPA2 802.1X
             :9D\:46\:54\:BF\:D8\:45:OrdinaNLGuest:Infra:87:195 Mbit/s:64:▂▄▆_:WPA2
             :86\:46\:54\:BF\:D8\:45:OrdinaNLSonos:Infra:87:195 Mbit/s:64:▂▄▆_:WPA2 WPA3
             :8A\:46\:54\:BF\:D8\:12:OrdinaNL:Infra:40:195 Mbit/s:54:▂▄__:WPA2 802.1X
             :9D\:46\:54\:BF\:D8\:12:OrdinaNLGuest:Infra:40:195 Mbit/s:54:▂▄__:WPA2
             :86\:46\:54\:BF\:D8\:12:OrdinaNLSonos:Infra:40:195 Mbit/s:54:▂▄__:WPA2 WPA3
             :B6\:46\:87\:BF\:D6\:AA::Infra:1:195 Mbit/s:49:▂▄__:WPA2
             :70\:4C\:A5\:A0\:D0\:39:SternWPL:Infra:6:130 Mbit/s:35:▂▄__:WPA2
             :70\:4C\:A5\:A0\:D0\:38:Office:Infra:6:130 Mbit/s:35:▂▄__:WPA2
             :B6\:46\:54\:BF\:D6\:AA::Infra:52:195 Mbit/s:35:▂▄__:WPA2
             :B6\:46\:54\:BF\:D4\:0F::Infra:56:195 Mbit/s:35:▂▄__:WPA2
        """.trimIndent()

        val info = output.parseWifiNetworks(listOf("OrdinaNL"))

        val rows = listOf(
            WifiInfo(
                ssid = "Ordina-Robotics",
                signal = "▂▄▆█",
                rate = "405 Mbit/s",
                connected = false,
                protocol = "WPA1 WPA2",
                known = false,
            ),
            WifiInfo(
                ssid = "OrdinaNL",
                signal = "▂▄▆_",
                rate = "195 Mbit/s",
                connected = true,
                protocol = "WPA2 802.1X",
                known = true,
            ),
            WifiInfo(
                ssid = "OrdinaNLGuest",
                signal = "▂▄▆_",
                rate = "195 Mbit/s",
                connected = false,
                protocol = "WPA2",
                known = false,
            ),
            WifiInfo(
                ssid = "OrdinaNLSonos",
                signal = "▂▄▆_",
                rate = "195 Mbit/s",
                connected = false,
                protocol = "WPA2 WPA3",
                known = false,
            ),
            WifiInfo(
                ssid = "SternWPL",
                signal = "▂▄__",
                rate = "130 Mbit/s",
                connected = false,
                protocol = "WPA2",
                known = false,
            ),
            WifiInfo(
                ssid = "Office",
                signal = "▂▄__",
                rate = "130 Mbit/s",
                connected = false,
                protocol = "WPA2",
                known = false,
            ),
        )

        assertEquals(rows, info.networks)
    }
}
