package com.example.diagnostic

import com.example.data.model.CleanIpItem
import com.example.data.model.KnownCdnRanges
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlin.random.Random

data class ScanProgress(
    val currentIp: String,
    val scannedCount: Int,
    val totalCount: Int,
    val foundCleanCount: Int,
    val isScanning: Boolean
)

class CleanIpScannerEngine {

    suspend fun testSingleIp(
        ip: String,
        port: Int = 443,
        provider: String = "Cloudflare"
    ): CleanIpItem = withContext(Dispatchers.IO) {
        val probe = SocketPingHelper.runMultipleProbes(
            ip = ip,
            port = port,
            probesCount = 2,
            timeoutMs = 1500
        )

        val isClean = probe.successfulProbes > 0 && probe.packetLossPercent == 0
        CleanIpItem(
            ip = ip,
            port = port,
            provider = provider,
            latencyMs = probe.avgLatencyMs,
            packetLossPercent = probe.packetLossPercent,
            jitterMs = probe.jitterMs,
            isClean = isClean,
            isTesting = false,
            testedAt = System.currentTimeMillis()
        )
    }

    fun generateIpsForSubnet(cidr: String, count: Int = 20): List<String> {
        val parts = cidr.split("/")
        if (parts.size != 2) return emptyList()
        val baseIp = parts[0]
        val prefix = parts[1].toIntOrNull() ?: 24
        val octets = baseIp.split(".").mapNotNull { it.toIntOrNull() }
        if (octets.size != 4) return emptyList()

        val generated = mutableSetOf<String>()
        val random = Random(System.currentTimeMillis())

        if (prefix <= 16) {
            // E.g. 104.16.0.0/12 or 162.159.0.0/16
            // Vary 2nd, 3rd and 4th octet
            val bBase = octets[1]
            while (generated.size < count) {
                val b = if (prefix < 16) bBase + random.nextInt(4) else bBase
                val c = random.nextInt(1, 254)
                val d = random.nextInt(1, 254)
                generated.add("${octets[0]}.$b.$c.$d")
            }
        } else if (prefix <= 20) {
            // E.g. 188.114.96.0/20
            while (generated.size < count) {
                val c = octets[2] + random.nextInt(15)
                val d = random.nextInt(1, 254)
                generated.add("${octets[0]}.${octets[1]}.$c.$d")
            }
        } else {
            // /24
            while (generated.size < count) {
                val d = random.nextInt(1, 254)
                generated.add("${octets[0]}.${octets[1]}.${octets[2]}.$d")
            }
        }

        return generated.toList()
    }

    fun getQuickScanCandidates(provider: String): List<String> {
        return when (provider) {
            "Cloudflare" -> KnownCdnRanges.cloudflareRanges.filter { it.provider == "Cloudflare" }
                .flatMap { it.sampleIps }
                .shuffled()
                .take(24)
            "Gcore" -> KnownCdnRanges.cloudflareRanges.firstOrNull { it.provider == "Gcore" }
                ?.sampleIps ?: listOf("92.223.84.1", "92.223.88.2", "92.223.100.5")
            "ArvanCloud" -> KnownCdnRanges.cloudflareRanges.firstOrNull { it.provider == "ArvanCloud" }
                ?.sampleIps ?: listOf("185.143.232.1", "185.143.233.2")
            else -> KnownCdnRanges.getAllSampleIps().take(20)
        }
    }
}
