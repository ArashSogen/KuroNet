package com.example.data.repository

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import com.example.data.local.AppDao
import com.example.data.local.CleanIpEntity
import com.example.data.local.DnsCacheEntity
import com.example.data.local.EmergencyConfigEntity
import com.example.data.model.CleanIpItem
import com.example.data.model.DefaultDnsList
import com.example.data.model.DnsResult
import com.example.data.model.PresetConfigs
import com.example.data.model.ServerStatus
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext

data class NetworkStatusInfo(
    val isConnected: Boolean,
    val typeName: String, // Wi-Fi, Cellular, Ethernet, Offline
    val isMetered: Boolean
)

class NetworkRepository(
    private val appDao: AppDao,
    private val context: Context
) {

    val allDnsCache: Flow<List<DnsCacheEntity>> = appDao.getAllDnsCache()
    val allCleanIps: Flow<List<CleanIpEntity>> = appDao.getAllCleanIps()
    val emergencyConfigs: Flow<List<EmergencyConfigEntity>> = appDao.getAllConfigs()

    suspend fun initializePresets() = withContext(Dispatchers.IO) {
        val count = appDao.getConfigCount()
        if (count == 0) {
            PresetConfigs.presets.forEach {
                appDao.insertConfig(it)
            }
        }
    }

    suspend fun saveDnsResults(results: List<DnsResult>) = withContext(Dispatchers.IO) {
        val entities = results.map { res ->
            DnsCacheEntity(
                serverId = res.server.id,
                name = res.server.name,
                primaryIp = res.server.primaryIp,
                secondaryIp = res.server.secondaryIp,
                dohUrl = res.server.dohUrl,
                dotHostname = res.server.dotHostname,
                region = res.server.region,
                category = res.server.category.name,
                latencyMs = res.latencyMs,
                jitterMs = res.jitterMs,
                packetLossPercent = res.packetLossPercent,
                score = res.score,
                status = res.status.name,
                testedAt = res.testedAt
            )
        }
        appDao.insertOrUpdateDnsCache(entities)
    }

    suspend fun saveCleanIp(item: CleanIpItem, isFavorite: Boolean = false) = withContext(Dispatchers.IO) {
        appDao.insertOrUpdateCleanIp(
            CleanIpEntity(
                ip = item.ip,
                port = item.port,
                provider = item.provider,
                latencyMs = item.latencyMs,
                packetLossPercent = item.packetLossPercent,
                jitterMs = item.jitterMs,
                isFavorite = isFavorite,
                notes = item.notes,
                testedAt = item.testedAt
            )
        )
    }

    suspend fun toggleCleanIpFavorite(ip: String, currentFavorite: Boolean) = withContext(Dispatchers.IO) {
        appDao.setCleanIpFavorite(ip, !currentFavorite)
    }

    suspend fun deleteCleanIp(ip: String) = withContext(Dispatchers.IO) {
        appDao.deleteCleanIp(ip)
    }

    suspend fun clearNonFavoriteCleanIps() = withContext(Dispatchers.IO) {
        appDao.clearNonFavoriteCleanIps()
    }

    suspend fun addCustomConfig(title: String, category: String, content: String, description: String) = withContext(Dispatchers.IO) {
        appDao.insertConfig(
            EmergencyConfigEntity(
                title = title,
                category = category,
                content = content,
                description = description,
                isPreset = false,
                updatedAt = System.currentTimeMillis()
            )
        )
    }

    suspend fun deleteConfig(config: EmergencyConfigEntity) = withContext(Dispatchers.IO) {
        appDao.deleteConfig(config)
    }

    fun getNetworkStatus(): NetworkStatusInfo {
        val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as? ConnectivityManager
            ?: return NetworkStatusInfo(false, "Unknown", false)
        val activeNetwork = cm.activeNetwork ?: return NetworkStatusInfo(false, "Offline", false)
        val caps = cm.getNetworkCapabilities(activeNetwork) ?: return NetworkStatusInfo(false, "Offline", false)

        val isConnected = caps.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
        val typeName = when {
            caps.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> "Wi-Fi"
            caps.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> "Cellular LTE/5G"
            caps.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET) -> "Ethernet"
            caps.hasTransport(NetworkCapabilities.TRANSPORT_VPN) -> "VPN Active"
            else -> "Connected"
        }
        val isMetered = !caps.hasCapability(NetworkCapabilities.NET_CAPABILITY_NOT_METERED)
        return NetworkStatusInfo(isConnected, typeName, isMetered)
    }
}
