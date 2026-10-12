package com.example.diagnostic

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.net.InetSocketAddress
import java.net.Socket
import kotlin.math.abs
import kotlin.math.sqrt

data class PingProbeResult(
    val success: Boolean,
    val latencyMs: Long
)

data class ComprehensivePingResult(
    val avgLatencyMs: Long,
    val minLatencyMs: Long,
    val maxLatencyMs: Long,
    val jitterMs: Long,
    val packetLossPercent: Int,
    val successfulProbes: Int,
    val totalProbes: Int
)

object SocketPingHelper {

    suspend fun probeTcp(
        ip: String,
        port: Int = 53,
        timeoutMs: Int = 1800
    ): PingProbeResult = withContext(Dispatchers.IO) {
        var socket: Socket? = null
        try {
            val startTime = System.nanoTime()
            socket = Socket()
            socket.tcpNoDelay = true
            socket.connect(InetSocketAddress(ip, port), timeoutMs)
            val elapsedMs = (System.nanoTime() - startTime) / 1_000_000L
            PingProbeResult(success = true, latencyMs = elapsedMs.coerceAtLeast(1L))
        } catch (e: Exception) {
            PingProbeResult(success = false, latencyMs = -1L)
        } finally {
            try {
                socket?.close()
            } catch (_: Exception) {}
        }
    }

    suspend fun runMultipleProbes(
        ip: String,
        port: Int,
        probesCount: Int = 3,
        timeoutMs: Int = 1800
    ): ComprehensivePingResult = withContext(Dispatchers.IO) {
        val latencies = mutableListOf<Long>()
        var failedCount = 0

        for (i in 0 until probesCount) {
            val probe = probeTcp(ip, port, timeoutMs)
            if (probe.success) {
                latencies.add(probe.latencyMs)
            } else {
                failedCount++
            }
        }

        if (latencies.isEmpty()) {
            return@withContext ComprehensivePingResult(
                avgLatencyMs = -1L,
                minLatencyMs = -1L,
                maxLatencyMs = -1L,
                jitterMs = 0L,
                packetLossPercent = 100,
                successfulProbes = 0,
                totalProbes = probesCount
            )
        }

        val avg = latencies.average().toLong()
        val min = latencies.minOrNull() ?: avg
        val max = latencies.maxOrNull() ?: avg

        // Calculate jitter (mean absolute difference between consecutive packets, or std dev)
        val jitter = if (latencies.size > 1) {
            val diffs = mutableListOf<Long>()
            for (i in 0 until latencies.size - 1) {
                diffs.add(abs(latencies[i + 1] - latencies[i]))
            }
            diffs.average().toLong()
        } else {
            0L
        }

        val packetLoss = ((failedCount.toDouble() / probesCount.toDouble()) * 100).toInt()

        ComprehensivePingResult(
            avgLatencyMs = avg,
            minLatencyMs = min,
            maxLatencyMs = max,
            jitterMs = jitter,
            packetLossPercent = packetLoss,
            successfulProbes = latencies.size,
            totalProbes = probesCount
        )
    }
}
