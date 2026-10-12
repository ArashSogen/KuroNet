package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "clean_ips")
data class CleanIpEntity(
    @PrimaryKey
    val ip: String,
    val port: Int = 443,
    val provider: String, // Cloudflare, Gcore, ArvanCloud, etc.
    val latencyMs: Long,
    val packetLossPercent: Int,
    val jitterMs: Long = 0,
    val isFavorite: Boolean = false,
    val notes: String = "",
    val testedAt: Long = System.currentTimeMillis()
)
