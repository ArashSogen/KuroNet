package com.example.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface AppDao {
    // DNS Cache
    @Query("SELECT * FROM dns_cache ORDER BY score DESC, latencyMs ASC")
    fun getAllDnsCache(): Flow<List<DnsCacheEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateDnsCache(items: List<DnsCacheEntity>)

    @Query("DELETE FROM dns_cache")
    suspend fun clearDnsCache()

    // Clean IPs
    @Query("SELECT * FROM clean_ips ORDER BY isFavorite DESC, latencyMs ASC")
    fun getAllCleanIps(): Flow<List<CleanIpEntity>>

    @Query("SELECT * FROM clean_ips WHERE isFavorite = 1 ORDER BY latencyMs ASC")
    fun getFavoriteCleanIps(): Flow<List<CleanIpEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateCleanIp(ip: CleanIpEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateCleanIps(ips: List<CleanIpEntity>)

    @Query("UPDATE clean_ips SET isFavorite = :isFavorite WHERE ip = :ip")
    suspend fun setCleanIpFavorite(ip: String, isFavorite: Boolean)

    @Query("DELETE FROM clean_ips WHERE ip = :ip")
    suspend fun deleteCleanIp(ip: String)

    @Query("DELETE FROM clean_ips WHERE isFavorite = 0")
    suspend fun clearNonFavoriteCleanIps()

    // Emergency Configs
    @Query("SELECT * FROM emergency_configs ORDER BY isPreset DESC, updatedAt DESC")
    fun getAllConfigs(): Flow<List<EmergencyConfigEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertConfig(config: EmergencyConfigEntity)

    @Update
    suspend fun updateConfig(config: EmergencyConfigEntity)

    @Delete
    suspend fun deleteConfig(config: EmergencyConfigEntity)

    @Query("SELECT COUNT(*) FROM emergency_configs")
    suspend fun getConfigCount(): Int
}
