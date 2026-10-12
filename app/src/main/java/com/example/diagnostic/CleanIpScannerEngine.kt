package com.example.diagnostic

import android.content.Context
import com.example.data.model.CleanIpItem
import com.example.data.model.KnownCdnRanges
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.BufferedReader
import java.io.InputStreamReader
import kotlin.random.Random

data class ScanProgress(
    val currentIp: String,
    val scannedCount: Int,
    val totalCount: Int,
    val foundCleanCount: Int,
    val isScanning: Boolean
)

class CleanIpScannerEngine {

    @Volatile
    private var cachedSenPaiPool: List<String>? = null

    suspend fun testSingleIp(
        ip: String,
        port: Int = 443,
        provider: String = "Cloudflare"
    ): CleanIpItem = withContext(Dispatchers.IO) {
        val resolvedProvider = if (provider == "All Providers") {
            KnownCdnRanges.detectProviderForIp(ip)
        } else {
            provider
        }

        val probe = SocketPingHelper.runMultipleProbes(
            ip = ip,
            port = port,
            probesCount = 2,
            timeoutMs = 1400
        )

        val isClean = probe.successfulProbes > 0 && probe.packetLossPercent == 0
        CleanIpItem(
            ip = ip,
            port = port,
            provider = resolvedProvider,
            latencyMs = probe.avgLatencyMs,
            packetLossPercent = probe.packetLossPercent,
            jitterMs = probe.jitterMs,
            isClean = isClean,
            isTesting = false,
            testedAt = System.currentTimeMillis()
        )
    }

    fun loadSenPaiIpPool(context: Context): List<String> {
        cachedSenPaiPool?.let { return it }

        val list = mutableListOf<String>()
        try {
            context.assets.open("senpai_ips.txt").use { inputStream ->
                BufferedReader(InputStreamReader(inputStream)).useLines { lines ->
                    lines.forEach { line ->
                        val trimmed = line.trim()
                        if (trimmed.isNotEmpty() && !trimmed.startsWith("#")) {
                            list.add(trimmed)
                        }
                    }
                }
            }
        } catch (_: Exception) {
            // Fallback in case of asset read issue
            list.addAll(KnownCdnRanges.getAllSampleIps())
        }

        val result = list.distinct()
        cachedSenPaiPool = result
        return result
    }

    fun getSenPaiCandidates(
        context: Context,
        provider: String,
        limit: Int = 100
    ): List<String> {
        val pool = loadSenPaiIpPool(context)
        val filtered = if (provider == "All Providers") {
            pool
        } else {
            pool.filter { ip -> KnownCdnRanges.detectProviderForIp(ip) == provider }
        }

        val available = if (filtered.isEmpty()) {
            pool.take(limit)
        } else {
            filtered
        }

        return if (limit >= available.size) {
            available.shuffled()
        } else {
            available.shuffled().take(limit)
        }
    }

    fun parseCustomIpText(text: String): List<String> {
        val ipRegex = Regex("""\b(?:\d{1,3}\.){3}\d{1,3}\b""")
        val found = mutableListOf<String>()
        text.lines().forEach { line ->
            val trimmed = line.trim()
            if (trimmed.isNotEmpty() && !trimmed.startsWith("#")) {
                val match = ipRegex.find(trimmed)
                if (match != null) {
                    found.add(match.value)
                }
            }
        }
        return found.distinct()
    }

    fun generateIpsForSubnet(cidr: String, count: Int = 40): List<String> {
        val parts = cidr.split("/")
        if (parts.size != 2) return emptyList()
        val baseIp = parts[0]
        val prefix = parts[1].toIntOrNull() ?: 24
        val octets = baseIp.split(".").mapNotNull { it.toIntOrNull() }
        if (octets.size != 4) return emptyList()

        val generated = mutableSetOf<String>()
        val random = Random(System.currentTimeMillis())

        if (prefix <= 16) {
            val bBase = octets[1]
            while (generated.size < count) {
                val b = if (prefix < 16) bBase + random.nextInt(4) else bBase
                val c = random.nextInt(1, 254)
                val d = random.nextInt(1, 254)
                generated.add("${octets[0]}.$b.$c.$d")
            }
        } else if (prefix <= 20) {
            while (generated.size < count) {
                val c = octets[2] + random.nextInt(15)
                val d = random.nextInt(1, 254)
                generated.add("${octets[0]}.${octets[1]}.$c.$d")
            }
        } else {
            while (generated.size < count) {
                val d = random.nextInt(1, 254)
                generated.add("${octets[0]}.${octets[1]}.${octets[2]}.$d")
            }
        }

        return generated.toList()
    }
}
