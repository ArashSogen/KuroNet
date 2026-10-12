package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.data.model.DnsResult
import com.example.data.model.ServerStatus
import com.example.diagnostic.CleanIpScannerEngine
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

  @Test
  fun `read string from context`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val appName = context.getString(R.string.app_name)
    assertEquals("KuroNet", appName)
  }

  @Test
  fun `verify dns score calculation`() {
    val optimalScore = DnsResult.calculateScore(latencyMs = 25, jitterMs = 3, packetLoss = 0)
    assertTrue("Optimal score should be >= 80", optimalScore >= 80)

    val blockedScore = DnsResult.calculateScore(latencyMs = -1, jitterMs = 0, packetLoss = 100)
    assertEquals(0, blockedScore)
  }

  @Test
  fun `verify clean ip subnet generation`() {
    val engine = CleanIpScannerEngine()
    val ips = engine.generateIpsForSubnet("104.16.0.0/12", count = 5)
    assertEquals(5, ips.size)
    ips.forEach { ip ->
      assertTrue(ip.startsWith("104."))
    }
  }
}
