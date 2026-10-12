package com.example.diagnostic

import com.example.data.model.DnsResult
import com.example.data.model.DnsServer
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class DnsBenchmarkEngine {

    suspend fun testServer(server: DnsServer): DnsResult = withContext(Dispatchers.IO) {
        // Try testing primary IP on port 53, fallback to port 853 or 443 if port 53 blocked
        val primaryProbe = SocketPingHelper.runMultipleProbes(
            ip = server.primaryIp,
            port = 53,
            probesCount = 3,
            timeoutMs = 1600
        )

        val finalProbe = if (primaryProbe.successfulProbes > 0) {
            primaryProbe
        } else if (server.secondaryIp.isNotEmpty()) {
            // Try secondary IP
            val secondaryProbe = SocketPingHelper.runMultipleProbes(
                ip = server.secondaryIp,
                port = 53,
                probesCount = 2,
                timeoutMs = 1500
            )
            if (secondaryProbe.successfulProbes > 0) secondaryProbe else primaryProbe
        } else {
            primaryProbe
        }

        val latency = finalProbe.avgLatencyMs
        val jitter = finalProbe.jitterMs
        val packetLoss = finalProbe.packetLossPercent

        val score = DnsResult.calculateScore(latency, jitter, packetLoss)
        val status = DnsResult.determineStatus(latency, packetLoss)

        DnsResult(
            server = server,
            latencyMs = latency,
            minLatencyMs = finalProbe.minLatencyMs,
            maxLatencyMs = finalProbe.maxLatencyMs,
            jitterMs = jitter,
            packetLossPercent = packetLoss,
            score = score,
            status = status,
            isTesting = false,
            testedAt = System.currentTimeMillis()
        )
    }
}
