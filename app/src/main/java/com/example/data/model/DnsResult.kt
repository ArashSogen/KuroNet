package com.example.data.model

enum class ServerStatus(val label: String, val kanji: String) {
    OPTIMAL("Optimal", "極"),
    STABLE("Stable", "安"),
    MODERATE("Moderate", "並"),
    SLOW("Slow", "遅"),
    BLOCKED("Blocked / Timeout", "断"),
    UNTESTED("Untested", "未")
}

data class DnsResult(
    val server: DnsServer,
    val latencyMs: Long = -1,
    val minLatencyMs: Long = -1,
    val maxLatencyMs: Long = -1,
    val jitterMs: Long = 0,
    val packetLossPercent: Int = 0,
    val score: Int = 0,
    val status: ServerStatus = ServerStatus.UNTESTED,
    val isTesting: Boolean = false,
    val testedAt: Long = 0
) {
    companion object {
        fun calculateScore(latencyMs: Long, jitterMs: Long, packetLoss: Int): Int {
            if (latencyMs <= 0 || packetLoss >= 100) return 0
            // Base score out of 100
            // Latency component: 0-50ms -> 60 pts, 50-150ms -> 40 pts, 150-300ms -> 20 pts
            val latencyScore = when {
                latencyMs < 30 -> 60
                latencyMs < 60 -> 55
                latencyMs < 100 -> 48
                latencyMs < 160 -> 38
                latencyMs < 250 -> 25
                latencyMs < 400 -> 12
                else -> 4
            }
            // Jitter component (max 20 pts)
            val jitterScore = when {
                jitterMs < 5 -> 20
                jitterMs < 15 -> 16
                jitterMs < 35 -> 10
                jitterMs < 70 -> 5
                else -> 1
            }
            // Loss penalty: 0% -> 20 pts, 33% -> 10 pts, 66% -> 2 pts
            val lossScore = when {
                packetLoss == 0 -> 20
                packetLoss < 35 -> 10
                packetLoss < 70 -> 2
                else -> 0
            }
            return (latencyScore + jitterScore + lossScore).coerceIn(0, 100)
        }

        fun determineStatus(latencyMs: Long, packetLoss: Int): ServerStatus {
            if (latencyMs <= 0 || packetLoss == 100) return ServerStatus.BLOCKED
            if (packetLoss > 30) return ServerStatus.SLOW
            return when {
                latencyMs < 60 -> ServerStatus.OPTIMAL
                latencyMs < 140 -> ServerStatus.STABLE
                latencyMs < 250 -> ServerStatus.MODERATE
                else -> ServerStatus.SLOW
            }
        }
    }
}
