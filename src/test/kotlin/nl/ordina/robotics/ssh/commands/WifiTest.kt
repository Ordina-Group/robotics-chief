package nl.ordina.robotics.ssh.commands

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
}
