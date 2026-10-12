package com.example.ui.cleanip

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.PlayCircle
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.StarBorder
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.CleanIpItem
import com.example.data.model.KnownCdnRanges
import com.example.ui.MainViewModel
import com.example.ui.components.KanjiBadge
import com.example.ui.components.PulsingRadarIndicator
import com.example.ui.theme.BambooGreen
import com.example.ui.theme.LanternAmber
import com.example.ui.theme.PureWhite
import com.example.ui.theme.SpaceMonoFontFamily
import com.example.ui.theme.SumiBorder
import com.example.ui.theme.SumiBorderSubtle
import com.example.ui.theme.SumiCard
import com.example.ui.theme.SumiCardElevated
import com.example.ui.theme.ToriiRed
import com.example.ui.theme.ZenGothicFontFamily
import com.example.ui.theme.ZenGray
import com.example.ui.theme.ZenMuted
import com.example.ui.theme.ZenWhite
import com.example.util.HapticUtil

fun getProviderBrandColor(provider: String): Color {
    return when (provider) {
        "Cloudflare" -> Color(0xFFF38020)
        "Fastly" -> Color(0xFFFF334B)
        "Gcore" -> Color(0xFF2375FF)
        "CloudFront" -> Color(0xFFFF9900)
        "Akamai" -> Color(0xFF00A2FF)
        "BunnyCDN" -> Color(0xFFFF7A00)
        "ArvanCloud" -> Color(0xFF00A859)
        "DerakCloud" -> Color(0xFFAB47BC)
        "CDN77 / CacheFly" -> Color(0xFF26C6DA)
        else -> LanternAmber
    }
}

@Composable
fun CleanIpScreen(
    viewModel: MainViewModel,
    modifier: Modifier = Modifier
) {
    val state by viewModel.cleanIpState.collectAsState()
    val context = LocalContext.current
    val haptic = LocalHapticFeedback.current

    var showExportDialog by remember { mutableStateOf<CleanIpItem?>(null) }
    var showCustomIpsDialog by remember { mutableStateOf(false) }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .testTag("clean_ip_screen_list"),
        contentPadding = PaddingValues(bottom = 96.dp)
    ) {
        // Hero Scanner Configurator
        item {
            ScannerConfigCard(
                state = state,
                onSelectProvider = {
                    HapticUtil.performClickHaptic(haptic)
                    viewModel.setScanProvider(it)
                },
                onSelectPort = {
                    HapticUtil.performClickHaptic(haptic)
                    viewModel.setScanPort(it)
                },
                onSelectCidr = {
                    HapticUtil.performClickHaptic(haptic)
                    viewModel.setScanCidr(it)
                },
                onSelectMode = { mode ->
                    HapticUtil.performClickHaptic(haptic)
                    if (mode == "Custom ips.txt") {
                        showCustomIpsDialog = true
                    }
                    viewModel.setScanMode(mode)
                },
                onSelectTargetCount = { count ->
                    HapticUtil.performClickHaptic(haptic)
                    viewModel.setScanTargetCount(count)
                },
                onStartScan = {
                    HapticUtil.performClickHaptic(haptic)
                    viewModel.startCleanIpScan()
                },
                onStopScan = {
                    HapticUtil.performClickHaptic(haptic)
                    viewModel.stopCleanIpScan()
                },
                onScanAllTogether = {
                    HapticUtil.performClickHaptic(haptic)
                    viewModel.scanAllTogether()
                }
            )
        }

        // Live Scanning Progress Indicator
        if (state.isScanning) {
            item {
                ScanProgressSection(state = state)
            }
        }

        // Best Clean IP Banner & Summary
        if (state.discoveredIps.isNotEmpty()) {
            item {
                DiscoveredSummaryBar(
                    discoveredCount = state.discoveredIps.size,
                    bestIp = state.bestCleanIp,
                    onCopyAll = {
                        HapticUtil.performClickHaptic(haptic)
                        val allIps = state.discoveredIps.joinToString("\n") { "${it.ip}:${it.port} [${it.provider}] (${it.latencyMs}ms)" }
                        copyToClipboard(context, allIps, "Copied ${state.discoveredIps.size} Clean IPs")
                    }
                )
            }
        }

        // Clean IPs List
        items(
            items = state.discoveredIps,
            key = { it.ip }
        ) { item ->
            CleanIpCard(
                item = item,
                onFavoriteToggle = {
                    HapticUtil.performClickHaptic(haptic)
                    viewModel.toggleCleanIpFavorite(item)
                },
                onCopyIp = {
                    HapticUtil.performClickHaptic(haptic)
                    copyToClipboard(context, item.ip, "Copied Clean IP: ${item.ip}")
                },
                onExportConfig = {
                    HapticUtil.performClickHaptic(haptic)
                    showExportDialog = item
                }
            )
        }

        // Empty state instruction
        if (!state.isScanning && state.discoveredIps.isEmpty()) {
            item {
                CensorshipBypassInfoCard()
            }
        }
    }

    // Export Config Dialog
    showExportDialog?.let { targetIp ->
        ExportConfigDialog(
            cleanIp = targetIp,
            onDismiss = { showExportDialog = null },
            onCopy = { snippet, label ->
                HapticUtil.performClickHaptic(haptic)
                copyToClipboard(context, snippet, "$label copied to clipboard")
                showExportDialog = null
            }
        )
    }

    // Custom ips.txt Input Dialog
    if (showCustomIpsDialog) {
        CustomIpsDialog(
            initialText = state.customTargetsText,
            onDismiss = { showCustomIpsDialog = false },
            onSave = { text ->
                HapticUtil.performClickHaptic(haptic)
                viewModel.setCustomTargetsText(text)
                viewModel.setScanMode("Custom ips.txt")
                showCustomIpsDialog = false
            }
        )
    }
}

@Composable
private fun ScannerConfigCard(
    state: com.example.ui.CleanIpScanState,
    onSelectProvider: (String) -> Unit,
    onSelectPort: (Int) -> Unit,
    onSelectCidr: (String) -> Unit,
    onSelectMode: (String) -> Unit,
    onSelectTargetCount: (Int) -> Unit,
    onStartScan: () -> Unit,
    onStopScan: () -> Unit,
    onScanAllTogether: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
            .clip(RoundedCornerShape(14.dp))
            .background(SumiCard)
            .border(1.dp, SumiBorder, RoundedCornerShape(14.dp))
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                KanjiBadge(kanji = "境", accentColor = ToriiRed, size = 32.dp, fontSize = 16)
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                    Text(
                        text = "Clean IP Finder (白IP探索)",
                        fontFamily = ZenGothicFontFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 17.sp,
                        color = PureWhite
                    )
                    Text(
                        text = "SenPai Anycast Engine (2,865 IP Pool)",
                        fontFamily = SpaceMonoFontFamily,
                        fontSize = 10.sp,
                        color = ToriiRed
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Quick Action: Scan All Providers Together
        OutlinedButton(
            onClick = onScanAllTogether,
            enabled = !state.isScanning,
            modifier = Modifier
                .fillMaxWidth()
                .height(44.dp),
            colors = ButtonDefaults.outlinedButtonColors(
                contentColor = PureWhite
            ),
            border = ButtonDefaults.outlinedButtonBorder.copy(
                brush = androidx.compose.ui.graphics.SolidColor(ToriiRed)
            ),
            shape = RoundedCornerShape(8.dp)
        ) {
            Icon(
                imageVector = Icons.Default.PlayCircle,
                contentDescription = null,
                tint = ToriiRed,
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "⚡ Scan All Providers Together (${state.scanTargetCount} IPs)",
                fontFamily = ZenGothicFontFamily,
                fontWeight = FontWeight.Bold,
                fontSize = 13.sp,
                color = PureWhite
            )
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Providers Selector (Horizontal Scroll with All Providers)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "CDN PROVIDER",
                fontFamily = SpaceMonoFontFamily,
                fontSize = 10.sp,
                color = ZenMuted
            )
            Text(
                text = "${KnownCdnRanges.providers.size - 1} CDNs Included",
                fontFamily = SpaceMonoFontFamily,
                fontSize = 10.sp,
                color = ToriiRed
            )
        }
        Spacer(modifier = Modifier.height(6.dp))

        val providerScroll = rememberScrollState()
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(providerScroll),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            KnownCdnRanges.providers.forEach { prov ->
                val isSelected = state.selectedProvider == prov
                val brandColor = if (prov == "All Providers") ToriiRed else getProviderBrandColor(prov)

                FilterChip(
                    selected = isSelected,
                    onClick = { onSelectProvider(prov) },
                    label = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(6.dp)
                                    .clip(CircleShape)
                                    .background(brandColor)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = prov,
                                fontFamily = ZenGothicFontFamily,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                fontSize = 12.sp,
                                color = if (isSelected) PureWhite else ZenGray
                            )
                        }
                    },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = brandColor.copy(alpha = 0.2f),
                        containerColor = SumiCardElevated
                    ),
                    border = FilterChipDefaults.filterChipBorder(
                        borderColor = if (isSelected) brandColor else SumiBorder,
                        selectedBorderColor = brandColor,
                        enabled = true,
                        selected = isSelected
                    )
                )
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Target IP Pool Limit Chips (50, 100, 250, 500, 1000, 2865)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "SCAN BATCH LIMIT",
                fontFamily = SpaceMonoFontFamily,
                fontSize = 10.sp,
                color = ZenMuted
            )
            Text(
                text = "Pool: 2,865 IPs",
                fontFamily = SpaceMonoFontFamily,
                fontSize = 10.sp,
                color = BambooGreen
            )
        }
        Spacer(modifier = Modifier.height(6.dp))
        val limitScroll = rememberScrollState()
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(limitScroll),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            listOf(50, 100, 250, 500, 1000, 2865).forEach { count ->
                val isSelected = state.scanTargetCount == count
                val label = if (count == 2865) "All (2,865)" else "$count IPs"
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(if (isSelected) ToriiRed.copy(alpha = 0.2f) else SumiCardElevated)
                        .border(0.5.dp, if (isSelected) ToriiRed else SumiBorder, RoundedCornerShape(6.dp))
                        .clickable { onSelectTargetCount(count) }
                        .padding(horizontal = 9.dp, vertical = 5.dp)
                ) {
                    Text(
                        text = label,
                        fontFamily = SpaceMonoFontFamily,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                        fontSize = 11.sp,
                        color = if (isSelected) PureWhite else ZenGray
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Port & Scan Mode Row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // Port Chips
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "TARGET PORT",
                    fontFamily = SpaceMonoFontFamily,
                    fontSize = 10.sp,
                    color = ZenMuted
                )
                Spacer(modifier = Modifier.height(4.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    listOf(443, 80, 2408).forEach { port ->
                        val isSelected = state.selectedPort == port
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(if (isSelected) ToriiRed.copy(alpha = 0.2f) else SumiCardElevated)
                                .border(0.5.dp, if (isSelected) ToriiRed else SumiBorder, RoundedCornerShape(6.dp))
                                .clickable { onSelectPort(port) }
                                .padding(horizontal = 9.dp, vertical = 5.dp)
                        ) {
                            Text(
                                text = "$port",
                                fontFamily = SpaceMonoFontFamily,
                                fontWeight = FontWeight.Bold,
                                fontSize = 11.sp,
                                color = if (isSelected) PureWhite else ZenGray
                            )
                        }
                    }
                }
            }

            // Mode Selector
            Column(modifier = Modifier.weight(1.3f)) {
                Text(
                    text = "SCAN MODE",
                    fontFamily = SpaceMonoFontFamily,
                    fontSize = 10.sp,
                    color = ZenMuted
                )
                Spacer(modifier = Modifier.height(4.dp))
                val modeScroll = rememberScrollState()
                Row(
                    modifier = Modifier.horizontalScroll(modeScroll),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    listOf("SenPai Pool (2800+)", "Subnet Scan", "Custom ips.txt").forEach { mode ->
                        val isSelected = state.scanMode == mode
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(if (isSelected) ToriiRed.copy(alpha = 0.2f) else SumiCardElevated)
                                .border(0.5.dp, if (isSelected) ToriiRed else SumiBorder, RoundedCornerShape(6.dp))
                                .clickable { onSelectMode(mode) }
                                .padding(horizontal = 8.dp, vertical = 5.dp)
                        ) {
                            Text(
                                text = when (mode) {
                                    "SenPai Pool (2800+)" -> "SenPai (2800+)"
                                    "Custom ips.txt" -> "ips.txt"
                                    else -> "Subnet"
                                },
                                fontFamily = ZenGothicFontFamily,
                                fontSize = 11.sp,
                                color = if (isSelected) PureWhite else ZenGray
                            )
                        }
                    }
                }
            }
        }

        // Subnet Selector (if Subnet mode)
        if (state.scanMode == "Subnet Scan") {
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = "SUBNET CIDR RANGE",
                fontFamily = SpaceMonoFontFamily,
                fontSize = 10.sp,
                color = ZenMuted
            )
            Spacer(modifier = Modifier.height(4.dp))
            val scroll = rememberScrollState()
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(scroll),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                val availableRanges = if (state.selectedProvider == "All Providers") {
                    KnownCdnRanges.cdnRanges
                } else {
                    KnownCdnRanges.cdnRanges.filter { it.provider == state.selectedProvider }
                }

                availableRanges.forEach { range ->
                    val isSelected = state.selectedCidr == range.cidr
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(if (isSelected) ToriiRed.copy(alpha = 0.2f) else SumiCardElevated)
                            .border(0.5.dp, if (isSelected) ToriiRed else SumiBorder, RoundedCornerShape(6.dp))
                            .clickable { onSelectCidr(range.cidr) }
                            .padding(horizontal = 8.dp, vertical = 5.dp)
                    ) {
                        Column {
                            Text(
                                text = range.cidr,
                                fontFamily = SpaceMonoFontFamily,
                                fontSize = 10.sp,
                                color = if (isSelected) PureWhite else ZenGray
                            )
                            if (state.selectedProvider == "All Providers") {
                                Text(
                                    text = range.provider,
                                    fontFamily = ZenGothicFontFamily,
                                    fontSize = 8.sp,
                                    color = ZenMuted
                                )
                            }
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Start / Stop Button
        Button(
            onClick = if (state.isScanning) onStopScan else onStartScan,
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp)
                .testTag("clean_ip_scan_button"),
            colors = ButtonDefaults.buttonColors(
                containerColor = if (state.isScanning) LanternAmber else ToriiRed,
                contentColor = PureWhite
            ),
            shape = RoundedCornerShape(8.dp)
        ) {
            Icon(
                imageVector = if (state.isScanning) Icons.Default.Stop else Icons.Default.PlayArrow,
                contentDescription = null,
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = if (state.isScanning) "Stop Scanner (停止)" else "Scan ${state.selectedProvider} (${state.scanTargetCount} Targets)",
                fontFamily = ZenGothicFontFamily,
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp
            )
        }
    }
}

@Composable
private fun ScanProgressSection(state: com.example.ui.CleanIpScanState) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp)
            .clip(RoundedCornerShape(10.dp))
            .background(SumiCard)
            .border(0.5.dp, ToriiRed.copy(alpha = 0.4f), RoundedCornerShape(10.dp))
            .padding(14.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                PulsingRadarIndicator(color = ToriiRed)
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                    Text(
                        text = "Probing Edge Handshake (${state.selectedProvider})...",
                        fontFamily = ZenGothicFontFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = PureWhite
                    )
                    Text(
                        text = state.progress.currentIp.ifEmpty { "Resolving Edge Targets..." },
                        fontFamily = SpaceMonoFontFamily,
                        fontSize = 11.sp,
                        color = ToriiRed
                    )
                }
            }

            Text(
                text = "${state.progress.scannedCount}/${state.progress.totalCount}",
                fontFamily = SpaceMonoFontFamily,
                fontWeight = FontWeight.Bold,
                fontSize = 13.sp,
                color = PureWhite
            )
        }

        Spacer(modifier = Modifier.height(10.dp))

        val progressFraction = if (state.progress.totalCount > 0) {
            (state.progress.scannedCount.toFloat() / state.progress.totalCount.toFloat()).coerceIn(0f, 1f)
        } else 0f

        LinearProgressIndicator(
            progress = { progressFraction },
            modifier = Modifier
                .fillMaxWidth()
                .height(4.dp)
                .clip(RoundedCornerShape(2.dp)),
            color = ToriiRed,
            trackColor = SumiBorderSubtle,
        )
    }
}

@Composable
private fun DiscoveredSummaryBar(
    discoveredCount: Int,
    bestIp: CleanIpItem?,
    onCopyAll: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .clip(CircleShape)
                    .background(BambooGreen)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = "$discoveredCount Clean IPs Found (0% Loss)",
                fontFamily = SpaceMonoFontFamily,
                fontWeight = FontWeight.Bold,
                fontSize = 12.sp,
                color = BambooGreen
            )
        }

        TextButton(
            onClick = onCopyAll,
            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp)
        ) {
            Icon(
                imageVector = Icons.Default.ContentCopy,
                contentDescription = null,
                tint = ZenGray,
                modifier = Modifier.size(14.dp)
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = "Copy All",
                fontFamily = SpaceMonoFontFamily,
                fontSize = 11.sp,
                color = ZenGray
            )
        }
    }
}

@Composable
private fun CleanIpCard(
    item: CleanIpItem,
    onFavoriteToggle: () -> Unit,
    onCopyIp: () -> Unit,
    onExportConfig: () -> Unit
) {
    val brandColor = getProviderBrandColor(item.provider)

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp)
            .clip(RoundedCornerShape(10.dp))
            .background(SumiCard)
            .border(0.5.dp, SumiBorder, RoundedCornerShape(10.dp))
            .padding(12.dp)
            .testTag("clean_ip_card_${item.ip}"),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
            KanjiBadge(kanji = "白", accentColor = brandColor, size = 26.dp, fontSize = 12)
            Spacer(modifier = Modifier.width(10.dp))
            Column {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = item.ip,
                        fontFamily = SpaceMonoFontFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = PureWhite
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = ":${item.port}",
                        fontFamily = SpaceMonoFontFamily,
                        fontSize = 11.sp,
                        color = ZenMuted
                    )
                }
                Spacer(modifier = Modifier.height(2.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "${item.latencyMs} ms",
                        fontFamily = SpaceMonoFontFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp,
                        color = BambooGreen
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "0% loss",
                        fontFamily = SpaceMonoFontFamily,
                        fontSize = 10.sp,
                        color = ZenGray
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    // Provider brand chip
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .background(brandColor.copy(alpha = 0.15f))
                            .border(0.5.dp, brandColor.copy(alpha = 0.5f), RoundedCornerShape(4.dp))
                            .padding(horizontal = 5.dp, vertical = 1.dp)
                    ) {
                        Text(
                            text = item.provider,
                            fontFamily = SpaceMonoFontFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 9.sp,
                            color = brandColor
                        )
                    }
                }
            }
        }

        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onFavoriteToggle, modifier = Modifier.size(32.dp)) {
                Icon(
                    imageVector = if (item.isFavorite) Icons.Default.Star else Icons.Default.StarBorder,
                    contentDescription = "Favorite",
                    tint = if (item.isFavorite) LanternAmber else ZenMuted,
                    modifier = Modifier.size(18.dp)
                )
            }

            IconButton(onClick = onCopyIp, modifier = Modifier.size(32.dp)) {
                Icon(
                    imageVector = Icons.Default.ContentCopy,
                    contentDescription = "Copy IP",
                    tint = ZenGray,
                    modifier = Modifier.size(16.dp)
                )
            }

            IconButton(onClick = onExportConfig, modifier = Modifier.size(32.dp)) {
                Icon(
                    imageVector = Icons.Default.Share,
                    contentDescription = "Export Config",
                    tint = ToriiRed,
                    modifier = Modifier.size(16.dp)
                )
            }
        }
    }
}

@Composable
private fun CensorshipBypassInfoCard() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(SumiCard)
            .border(0.5.dp, SumiBorder, RoundedCornerShape(12.dp))
            .padding(16.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            KanjiBadge(kanji = "解", accentColor = ToriiRed, size = 26.dp, fontSize = 13)
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "SenPai Anycast Engine (2,865 Edge IPs)",
                fontFamily = ZenGothicFontFamily,
                fontWeight = FontWeight.Bold,
                fontSize = 15.sp,
                color = PureWhite
            )
        }
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "KuroNet embeds over 2,800 Anycast IPs mirroring SenPaiScanner across Cloudflare (104.16 - 104.27, 172.64 - 172.71, 162.158, 162.159, 198.41, 188.114, 141.101, 108.162, 173.245), Fastly, Gcore, CloudFront, Akamai, and BunnyCDN. Use 'All Providers' to sweep up to 2,865 IPs concurrently with zero packet loss filtering.",
            fontFamily = ZenGothicFontFamily,
            fontSize = 12.sp,
            lineHeight = 18.sp,
            color = ZenGray
        )
    }
}

@Composable
private fun CustomIpsDialog(
    initialText: String,
    onDismiss: () -> Unit,
    onSave: (String) -> Unit
) {
    var text by remember { mutableStateOf(initialText) }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = SumiCardElevated,
        title = {
            Text("Import / Paste ips.txt", fontFamily = ZenGothicFontFamily, fontWeight = FontWeight.Bold, color = PureWhite, fontSize = 16.sp)
        },
        text = {
            Column {
                Text(
                    text = "Paste IP addresses or CIDRs (one per line):",
                    fontFamily = SpaceMonoFontFamily,
                    fontSize = 11.sp,
                    color = ZenGray
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = text,
                    onValueChange = { text = it },
                    placeholder = { Text("104.16.12.1\n162.159.192.1\n172.64.32.1", color = ZenMuted) },
                    minLines = 6,
                    maxLines = 10,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = PureWhite,
                        unfocusedTextColor = PureWhite,
                        focusedBorderColor = ToriiRed,
                        unfocusedBorderColor = SumiBorder
                    )
                )
            }
        },
        confirmButton = {
            Button(
                onClick = { onSave(text) },
                colors = ButtonDefaults.buttonColors(containerColor = ToriiRed, contentColor = PureWhite)
            ) {
                Text("Use Custom IPs")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = ZenGray)
            }
        }
    )
}

@Composable
private fun ExportConfigDialog(
    cleanIp: CleanIpItem,
    onDismiss: () -> Unit,
    onCopy: (String, String) -> Unit
) {
    val warpEndpoint = "${cleanIp.ip}:2408"
    val v2raySnippet = """
"address": "${cleanIp.ip}",
"port": 443
    """.trimIndent()
    val clashNode = "server: ${cleanIp.ip} # ${cleanIp.provider}"

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = SumiCardElevated,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                KanjiBadge(kanji = "出", accentColor = ToriiRed, size = 24.dp, fontSize = 12)
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Export Clean IP Config",
                    fontFamily = ZenGothicFontFamily,
                    fontWeight = FontWeight.Bold,
                    color = PureWhite,
                    fontSize = 16.sp
                )
            }
        },
        text = {
            Column {
                Text(
                    text = "Selected: ${cleanIp.ip} (${cleanIp.provider}) • ${cleanIp.latencyMs}ms",
                    fontFamily = SpaceMonoFontFamily,
                    fontSize = 12.sp,
                    color = BambooGreen
                )
                Spacer(modifier = Modifier.height(14.dp))

                // WARP
                ExportOptionRow(
                    title = "Cloudflare WARP Endpoint",
                    snippet = warpEndpoint,
                    onCopy = { onCopy(warpEndpoint, "WARP Endpoint") }
                )
                Spacer(modifier = Modifier.height(10.dp))

                // V2Ray
                ExportOptionRow(
                    title = "V2Ray / Xray Address",
                    snippet = cleanIp.ip,
                    onCopy = { onCopy(cleanIp.ip, "V2Ray Address") }
                )
                Spacer(modifier = Modifier.height(10.dp))

                // Clash
                ExportOptionRow(
                    title = "Clash YAML Server",
                    snippet = clashNode,
                    onCopy = { onCopy(clashNode, "Clash Config") }
                )
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Close", color = ToriiRed, fontFamily = ZenGothicFontFamily)
            }
        }
    )
}

@Composable
private fun ExportOptionRow(
    title: String,
    snippet: String,
    onCopy: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(SumiCard)
            .border(0.5.dp, SumiBorder, RoundedCornerShape(8.dp))
            .padding(10.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = title,
                fontFamily = ZenGothicFontFamily,
                fontWeight = FontWeight.SemiBold,
                fontSize = 12.sp,
                color = PureWhite
            )
            IconButton(onClick = onCopy, modifier = Modifier.size(24.dp)) {
                Icon(
                    imageVector = Icons.Default.ContentCopy,
                    contentDescription = "Copy",
                    tint = ToriiRed,
                    modifier = Modifier.size(14.dp)
                )
            }
        }
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = snippet,
            fontFamily = SpaceMonoFontFamily,
            fontSize = 11.sp,
            color = ZenGray
        )
    }
}

private fun copyToClipboard(context: Context, text: String, toastMessage: String) {
    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
    val clip = ClipData.newPlainText("KuroNet", text)
    clipboard.setPrimaryClip(clip)
    Toast.makeText(context, toastMessage, Toast.LENGTH_SHORT).show()
}
