package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "dns_cache")
data class DnsCacheEntity(
    @PrimaryKey
    val serverId: String,
    val name: String,
    val primaryIp: String,
    val secondaryIp: String,
    val dohUrl: String,
    val dotHostname: String,
    val region: String,
    val category: String,
    val latencyMs: Long,
    val jitterMs: Long,
    val packetLossPercent: Int,
    val score: Int,
    val status: String, // OPTIMAL, STABLE, SLOW, BLOCKED, UNTESTED
    val testedAt: Long
)
