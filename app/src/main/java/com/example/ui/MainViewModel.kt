package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.AppDatabase
import com.example.data.local.CleanIpEntity
import com.example.data.local.DnsCacheEntity
import com.example.data.local.EmergencyConfigEntity
import com.example.data.model.CleanIpItem
import com.example.data.model.DnsCategory
import com.example.data.model.DnsResult
import com.example.data.model.DnsServer
import com.example.data.model.DefaultDnsList
import com.example.data.model.KnownCdnRanges
import com.example.data.model.ServerStatus
import com.example.data.repository.NetworkRepository
import com.example.data.repository.NetworkStatusInfo
import com.example.diagnostic.CleanIpScannerEngine
import com.example.diagnostic.DnsBenchmarkEngine
import com.example.diagnostic.ScanProgress
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Semaphore
import kotlinx.coroutines.sync.withPermit
import java.util.concurrent.atomic.AtomicInteger

enum class DnsSortOption(val label: String) {
    BEST_SCORE("Best Score"),
    LOWEST_LATENCY("Lowest Latency"),
    LOWEST_JITTER("Lowest Jitter"),
    NAME("Name")
}

data class DnsUiState(
    val results: List<DnsResult> = emptyList(),
    val isTestingAll: Boolean = false,
    val currentlyTestingId: String? = null,
    val selectedCategory: DnsCategory? = null,
    val sortOption: DnsSortOption = DnsSortOption.BEST_SCORE,
    val bestServer: DnsResult? = null,
    val lastBenchmarkTime: Long = 0
)

data class CleanIpScanState(
    val isScanning: Boolean = false,
    val selectedProvider: String = "All Providers",
    val selectedPort: Int = 443,
    val selectedCidr: String = "104.16.0.0/12",
    val scanMode: String = "SenPai Pool (2800+)", // "SenPai Pool (2800+)", "Subnet Scan", "Custom ips.txt"
    val scanTargetCount: Int = 100, // 50, 100, 250, 500, 1000, 2865
    val customTargetsText: String = "",
    val progress: ScanProgress = ScanProgress("", 0, 0, 0, false),
    val discoveredIps: List<CleanIpItem> = emptyList(),
    val bestCleanIp: CleanIpItem? = null
)

class MainViewModel(application: Application) : AndroidViewModel(application) {

    private val db = AppDatabase.getInstance(application)
    private val repository = NetworkRepository(db.appDao(), application)
    private val dnsEngine = DnsBenchmarkEngine()
    val cleanIpEngine = CleanIpScannerEngine()

    // Offline data flows
    val cachedDns: StateFlow<List<DnsCacheEntity>> = repository.allDnsCache
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val savedCleanIps: StateFlow<List<CleanIpEntity>> = repository.allCleanIps
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val emergencyConfigs: StateFlow<List<EmergencyConfigEntity>> = repository.emergencyConfigs
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // In-memory UI states
    private val _dnsState = MutableStateFlow(DnsUiState())
    val dnsState: StateFlow<DnsUiState> = _dnsState.asStateFlow()

    private val _cleanIpState = MutableStateFlow(CleanIpScanState())
    val cleanIpState: StateFlow<CleanIpScanState> = _cleanIpState.asStateFlow()

    private val _networkStatus = MutableStateFlow(repository.getNetworkStatus())
    val networkStatus: StateFlow<NetworkStatusInfo> = _networkStatus.asStateFlow()

    private var scanJob: Job? = null
    private var benchmarkJob: Job? = null

    init {
        viewModelScope.launch {
            repository.initializePresets()
        }
        initializeDnsList()
        refreshNetworkStatus()
    }

    private fun initializeDnsList() {
        val initialList = DefaultDnsList.servers.map { server ->
            DnsResult(server = server)
        }
        _dnsState.value = _dnsState.value.copy(results = initialList)

        viewModelScope.launch {
            cachedDns.collect { cacheList ->
                if (cacheList.isNotEmpty() && _dnsState.value.results.all { it.status == ServerStatus.UNTESTED }) {
                    val merged = _dnsState.value.results.map { res ->
                        val cached = cacheList.find { it.serverId == res.server.id }
                        if (cached != null) {
                            res.copy(
                                latencyMs = cached.latencyMs,
                                jitterMs = cached.jitterMs,
                                packetLossPercent = cached.packetLossPercent,
                                score = cached.score,
                                status = try { ServerStatus.valueOf(cached.status) } catch (_: Exception) { ServerStatus.STABLE },
                                testedAt = cached.testedAt
                            )
                        } else {
                            res
                        }
                    }
                    val best = merged.filter { it.status != ServerStatus.UNTESTED && it.status != ServerStatus.BLOCKED }
                        .maxByOrNull { it.score }
                    _dnsState.value = _dnsState.value.copy(
                        results = merged,
                        bestServer = best,
                        lastBenchmarkTime = cacheList.maxOfOrNull { it.testedAt } ?: 0L
                    )
                }
            }
        }
    }

    fun refreshNetworkStatus() {
        _networkStatus.value = repository.getNetworkStatus()
    }

    fun setDnsCategoryFilter(category: DnsCategory?) {
        _dnsState.value = _dnsState.value.copy(selectedCategory = category)
    }

    fun setDnsSortOption(option: DnsSortOption) {
        _dnsState.value = _dnsState.value.copy(sortOption = option)
    }

    fun benchmarkAllDns() {
        if (_dnsState.value.isTestingAll) return
        benchmarkJob?.cancel()

        benchmarkJob = viewModelScope.launch(Dispatchers.Default) {
            _dnsState.value = _dnsState.value.copy(isTestingAll = true)
            val currentList = _dnsState.value.results.toMutableList()
            val semaphore = Semaphore(4)

            val updatedResults = mutableListOf<DnsResult>()

            val jobs = currentList.map { item ->
                launch {
                    semaphore.withPermit {
                        _dnsState.value = _dnsState.value.copy(currentlyTestingId = item.server.id)
                        val tested = dnsEngine.testServer(item.server)
                        synchronized(updatedResults) {
                            updatedResults.add(tested)
                            val updatedList = _dnsState.value.results.map {
                                if (it.server.id == tested.server.id) tested else it
                            }
                            val best = updatedList.filter { it.status != ServerStatus.UNTESTED && it.status != ServerStatus.BLOCKED }
                                .maxByOrNull { it.score }
                            _dnsState.value = _dnsState.value.copy(
                                results = updatedList,
                                bestServer = best
                            )
                        }
                    }
                }
            }
            jobs.forEach { it.join() }

            val finalBest = _dnsState.value.results.filter { it.status != ServerStatus.UNTESTED && it.status != ServerStatus.BLOCKED }
                .maxByOrNull { it.score }

            _dnsState.value = _dnsState.value.copy(
                isTestingAll = false,
                currentlyTestingId = null,
                bestServer = finalBest,
                lastBenchmarkTime = System.currentTimeMillis()
            )

            repository.saveDnsResults(_dnsState.value.results)
        }
    }

    fun benchmarkSingleDns(server: DnsServer) {
        viewModelScope.launch {
            _dnsState.value = _dnsState.value.copy(currentlyTestingId = server.id)
            val tested = dnsEngine.testServer(server)
            val updated = _dnsState.value.results.map {
                if (it.server.id == server.id) tested else it
            }
            val best = updated.filter { it.status != ServerStatus.UNTESTED && it.status != ServerStatus.BLOCKED }
                .maxByOrNull { it.score }
            _dnsState.value = _dnsState.value.copy(
                results = updated,
                currentlyTestingId = null,
                bestServer = best
            )
            repository.saveDnsResults(listOf(tested))
        }
    }

    // Clean IP Scanner Controls
    fun setScanProvider(provider: String) {
        val cidr = KnownCdnRanges.cdnRanges.firstOrNull { it.provider == provider }?.cidr ?: "104.16.0.0/12"
        _cleanIpState.value = _cleanIpState.value.copy(
            selectedProvider = provider,
            selectedCidr = cidr
        )
    }

    fun scanAllTogether() {
        setScanProvider("All Providers")
        startCleanIpScan()
    }

    fun setScanPort(port: Int) {
        _cleanIpState.value = _cleanIpState.value.copy(selectedPort = port)
    }

    fun setScanCidr(cidr: String) {
        _cleanIpState.value = _cleanIpState.value.copy(selectedCidr = cidr)
    }

    fun setScanMode(mode: String) {
        _cleanIpState.value = _cleanIpState.value.copy(scanMode = mode)
    }

    fun setScanTargetCount(count: Int) {
        _cleanIpState.value = _cleanIpState.value.copy(scanTargetCount = count)
    }

    fun setCustomTargetsText(text: String) {
        _cleanIpState.value = _cleanIpState.value.copy(customTargetsText = text)
    }

    fun startCleanIpScan() {
        if (_cleanIpState.value.isScanning) return
        scanJob?.cancel()

        scanJob = viewModelScope.launch(Dispatchers.Default) {
            val provider = _cleanIpState.value.selectedProvider
            val port = _cleanIpState.value.selectedPort
            val mode = _cleanIpState.value.scanMode
            val cidr = _cleanIpState.value.selectedCidr
            val targetCount = _cleanIpState.value.scanTargetCount

            val candidates = when (mode) {
                "Custom ips.txt" -> {
                    cleanIpEngine.parseCustomIpText(_cleanIpState.value.customTargetsText)
                }
                "Subnet Scan" -> {
                    cleanIpEngine.generateIpsForSubnet(cidr, count = targetCount.coerceAtMost(250))
                }
                else -> {
                    // SenPai Pool (2800+)
                    cleanIpEngine.getSenPaiCandidates(getApplication(), provider, targetCount)
                }
            }

            if (candidates.isEmpty()) {
                _cleanIpState.value = _cleanIpState.value.copy(isScanning = false)
                return@launch
            }

            _cleanIpState.value = _cleanIpState.value.copy(
                isScanning = true,
                discoveredIps = emptyList(),
                progress = ScanProgress("", 0, candidates.size, 0, true)
            )

            val discovered = mutableListOf<CleanIpItem>()
            val processedCount = AtomicInteger(0)
            val semaphore = Semaphore(8) // Concurrency of 8 workers

            val jobs = candidates.map { ip ->
                launch {
                    semaphore.withPermit {
                        if (!_cleanIpState.value.isScanning) return@withPermit

                        val currentIdx = processedCount.incrementAndGet()
                        _cleanIpState.value = _cleanIpState.value.copy(
                            progress = _cleanIpState.value.progress.copy(
                                currentIp = ip,
                                scannedCount = currentIdx
                            )
                        )

                        val tested = cleanIpEngine.testSingleIp(ip, port, provider)
                        if (tested.isClean) {
                            synchronized(discovered) {
                                discovered.add(tested)
                                discovered.sortBy { it.latencyMs }
                                _cleanIpState.value = _cleanIpState.value.copy(
                                    discoveredIps = discovered.toList(),
                                    bestCleanIp = discovered.firstOrNull(),
                                    progress = _cleanIpState.value.progress.copy(foundCleanCount = discovered.size)
                                )
                            }
                            repository.saveCleanIp(tested, isFavorite = false)
                        }
                    }
                }
            }

            jobs.forEach { it.join() }

            _cleanIpState.value = _cleanIpState.value.copy(
                isScanning = false,
                progress = _cleanIpState.value.progress.copy(isScanning = false)
            )
        }
    }

    fun stopCleanIpScan() {
        scanJob?.cancel()
        _cleanIpState.value = _cleanIpState.value.copy(
            isScanning = false,
            progress = _cleanIpState.value.progress.copy(isScanning = false)
        )
    }

    fun toggleCleanIpFavorite(item: CleanIpItem) {
        viewModelScope.launch {
            repository.saveCleanIp(item.copy(isFavorite = !item.isFavorite), isFavorite = !item.isFavorite)
        }
    }

    fun toggleEntityFavorite(entity: CleanIpEntity) {
        viewModelScope.launch {
            repository.toggleCleanIpFavorite(entity.ip, entity.isFavorite)
        }
    }

    fun deleteCleanIp(ip: String) {
        viewModelScope.launch {
            repository.deleteCleanIp(ip)
        }
    }

    fun clearNonFavoriteCleanIps() {
        viewModelScope.launch {
            repository.clearNonFavoriteCleanIps()
        }
    }

    fun addEmergencyConfig(title: String, category: String, content: String, description: String) {
        viewModelScope.launch {
            repository.addCustomConfig(title, category, content, description)
        }
    }

    fun deleteEmergencyConfig(config: EmergencyConfigEntity) {
        viewModelScope.launch {
            repository.deleteConfig(config)
        }
    }
}
