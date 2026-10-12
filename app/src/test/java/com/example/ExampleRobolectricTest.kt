package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.data.model.DnsResult
import com.example.data.model.KnownCdnRanges
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

  @Test
  fun `verify all providers candidates generation`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val engine = CleanIpScannerEngine()
    val allCandidates = engine.getSenPaiCandidates(context, "All Providers", limit = 100)
    assertTrue("Should generate candidates for All Providers", allCandidates.isNotEmpty())
    assertTrue("Should include multiple providers", KnownCdnRanges.providers.size >= 8)
  }

  @Test
  fun `verify senpai pool contains over 1000 ips`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val engine = CleanIpScannerEngine()
    val pool = engine.loadSenPaiIpPool(context)
    assertTrue("SenPai Pool should have >= 1000 IPs, found ${pool.size}", pool.size >= 1000)
  }
}
