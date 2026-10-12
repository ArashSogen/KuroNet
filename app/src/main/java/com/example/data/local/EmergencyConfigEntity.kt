package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "emergency_configs")
data class EmergencyConfigEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val title: String,
    val category: String, // WARP, V2RAY, DOT_DNS, CLASH, SINGBOX, NOTES
    val content: String,
    val description: String,
    val isPreset: Boolean = false,
    val updatedAt: Long = System.currentTimeMillis()
)
