package com.example.ui.dns

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
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
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
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
import com.example.data.model.DnsCategory
import com.example.data.model.DnsResult
import com.example.data.model.ServerStatus
import com.example.ui.DnsSortOption
import com.example.ui.MainViewModel
import com.example.ui.components.KanjiBadge
import com.example.ui.components.LatencyBar
import com.example.ui.components.PulsingRadarIndicator
import com.example.ui.components.StatusTag
import com.example.ui.components.ZenSectionHeader
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

@Composable
fun DnsScreen(
    viewModel: MainViewModel,
    modifier: Modifier = Modifier
) {
    val state by viewModel.dnsState.collectAsState()
    val context = LocalContext.current
    val haptic = LocalHapticFeedback.current

    // Filter and sort
    val filteredResults = state.results
        .filter { res ->
            state.selectedCategory == null || res.server.category == state.selectedCategory
        }
        .sortedWith { a, b ->
            when (state.sortOption) {
                DnsSortOption.BEST_SCORE -> b.score.compareTo(a.score)
                DnsSortOption.LOWEST_LATENCY -> {
                    val latA = if (a.latencyMs <= 0) Long.MAX_VALUE else a.latencyMs
                    val latB = if (b.latencyMs <= 0) Long.MAX_VALUE else b.latencyMs
                    latA.compareTo(latB)
                }
                DnsSortOption.LOWEST_JITTER -> a.jitterMs.compareTo(b.jitterMs)
                DnsSortOption.NAME -> a.server.name.compareTo(b.server.name)
            }
        }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .testTag("dns_screen_list"),
        contentPadding = PaddingValues(bottom = 96.dp)
    ) {
        // Hero benchmark card
        item {
            HeroBenchmarkHeader(
                isTestingAll = state.isTestingAll,
                bestServer = state.bestServer,
                onTestAll = {
                    HapticUtil.performClickHaptic(haptic)
                    viewModel.benchmarkAllDns()
                }
            )
        }

        // Category filter chips
        item {
            CategoryFilterBar(
                selectedCategory = state.selectedCategory,
                onSelectCategory = {
                    HapticUtil.performClickHaptic(haptic)
                    viewModel.setDnsCategoryFilter(it)
                }
            )
        }

        // Sort Options & Count
        item {
            SortOptionsBar(
                selectedOption = state.sortOption,
                serverCount = filteredResults.size,
                onSelectSort = {
                    HapticUtil.performClickHaptic(haptic)
                    viewModel.setDnsSortOption(it)
                }
            )
        }

        // DNS Server Cards
        items(
            items = filteredResults,
            key = { it.server.id }
        ) { result ->
            DnsServerCard(
                result = result,
                isCurrentlyTesting = state.currentlyTestingId == result.server.id,
                onTestSingle = {
                    HapticUtil.performClickHaptic(haptic)
                    viewModel.benchmarkSingleDns(result.server)
                },
                onCopyIp = { ip ->
                    HapticUtil.performClickHaptic(haptic)
                    copyToClipboard(context, ip, "DNS IP copied: $ip")
                },
                onCopyDot = { dot ->
                    HapticUtil.performClickHaptic(haptic)
                    copyToClipboard(context, dot, "Private DNS hostname copied: $dot")
                }
            )
        }
    }
}

@Composable
private fun HeroBenchmarkHeader(
    isTestingAll: Boolean,
    bestServer: DnsResult?,
    onTestAll: () -> Unit
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
                KanjiBadge(kanji = "測", accentColor = ToriiRed, size = 32.dp, fontSize = 16)
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                    Text(
                        text = "DNS Benchmark (全域計測)",
                        fontFamily = ZenGothicFontFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 17.sp,
                        color = PureWhite
                    )
                    Text(
                        text = "Real-time RTT, Jitter & Packet Loss Engine",
                        fontFamily = SpaceMonoFontFamily,
                        fontSize = 10.sp,
                        color = ZenGray
                    )
                }
            }

            if (isTestingAll) {
                PulsingRadarIndicator(color = ToriiRed)
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Best Server Spotlight
        if (bestServer != null && bestServer.status != ServerStatus.UNTESTED) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .background(SumiCardElevated)
                    .border(0.5.dp, BambooGreen.copy(alpha = 0.5f), RoundedCornerShape(8.dp))
                    .padding(12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "RECOMMENDED",
                                fontFamily = SpaceMonoFontFamily,
                                fontWeight = FontWeight.Bold,
                                fontSize = 9.sp,
                                color = BambooGreen
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Score ${bestServer.score}/100",
                                fontFamily = SpaceMonoFontFamily,
                                fontSize = 10.sp,
                                color = PureWhite
                            )
                        }
                        Text(
                            text = bestServer.server.name,
                            fontFamily = ZenGothicFontFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp,
                            color = PureWhite
                        )
                        Text(
                            text = "${bestServer.server.primaryIp} • ${bestServer.server.region}",
                            fontFamily = SpaceMonoFontFamily,
                            fontSize = 11.sp,
                            color = ZenGray
                        )
                    }

                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            text = "${bestServer.latencyMs} ms",
                            fontFamily = SpaceMonoFontFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp,
                            color = BambooGreen
                        )
                        Text(
                            text = "±${bestServer.jitterMs}ms jitter",
                            fontFamily = SpaceMonoFontFamily,
                            fontSize = 10.sp,
                            color = ZenMuted
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.height(14.dp))
        }

        // Test All Button
        Button(
            onClick = onTestAll,
            enabled = !isTestingAll,
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp)
                .testTag("dns_test_all_button"),
            colors = ButtonDefaults.buttonColors(
                containerColor = ToriiRed,
                contentColor = PureWhite,
                disabledContainerColor = SumiCardElevated,
                disabledContentColor = ZenMuted
            ),
            shape = RoundedCornerShape(8.dp)
        ) {
            if (isTestingAll) {
                CircularProgressIndicator(
                    color = ToriiRed,
                    strokeWidth = 2.dp,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                    text = "Testing All Resolvers...",
                    fontFamily = ZenGothicFontFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp
                )
            } else {
                Icon(
                    imageVector = Icons.Default.PlayArrow,
                    contentDescription = "Test All",
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Benchmark All Servers (全計測)",
                    fontFamily = ZenGothicFontFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp
                )
            }
        }
    }
}

@Composable
private fun CategoryFilterBar(
    selectedCategory: DnsCategory?,
    onSelectCategory: (DnsCategory?) -> Unit
) {
    val scrollState = rememberScrollState()
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(scrollState)
            .padding(horizontal = 16.dp, vertical = 4.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        FilterChip(
            selected = selectedCategory == null,
            onClick = { onSelectCategory(null) },
            label = {
                Text(
                    text = "All (全)",
                    fontFamily = ZenGothicFontFamily,
                    fontSize = 12.sp,
                    color = if (selectedCategory == null) PureWhite else ZenGray
                )
            },
            colors = FilterChipDefaults.filterChipColors(
                selectedContainerColor = ToriiRed.copy(alpha = 0.2f),
                containerColor = SumiCard
            ),
            border = FilterChipDefaults.filterChipBorder(
                borderColor = if (selectedCategory == null) ToriiRed else SumiBorder,
                selectedBorderColor = ToriiRed,
                enabled = true,
                selected = selectedCategory == null
            )
        )

        DnsCategory.values().forEach { cat ->
            val isSelected = selectedCategory == cat
            FilterChip(
                selected = isSelected,
                onClick = { onSelectCategory(cat) },
                label = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = cat.kanji,
                            fontFamily = ZenGothicFontFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp,
                            color = if (isSelected) ToriiRed else ZenMuted
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = cat.title,
                            fontFamily = ZenGothicFontFamily,
                            fontSize = 12.sp,
                            color = if (isSelected) PureWhite else ZenGray
                        )
                    }
                },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = ToriiRed.copy(alpha = 0.15f),
                    containerColor = SumiCard
                ),
                border = FilterChipDefaults.filterChipBorder(
                    borderColor = if (isSelected) ToriiRed else SumiBorder,
                    selectedBorderColor = ToriiRed,
                    enabled = true,
                    selected = isSelected
                )
            )
        }
    }
}

@Composable
private fun SortOptionsBar(
    selectedOption: DnsSortOption,
    serverCount: Int,
    onSelectSort: (DnsSortOption) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "$serverCount Servers",
            fontFamily = SpaceMonoFontFamily,
            fontSize = 11.sp,
            color = ZenMuted
        )

        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            DnsSortOption.values().forEach { opt ->
                val isSelected = selectedOption == opt
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(4.dp))
                        .background(if (isSelected) SumiCardElevated else Color.Transparent)
                        .border(0.5.dp, if (isSelected) ToriiRed else Color.Transparent, RoundedCornerShape(4.dp))
                        .clickable { onSelectSort(opt) }
                        .padding(horizontal = 6.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = opt.label,
                        fontFamily = SpaceMonoFontFamily,
                        fontSize = 10.sp,
                        color = if (isSelected) PureWhite else ZenMuted
                    )
                }
            }
        }
    }
}

@Composable
private fun DnsServerCard(
    result: DnsResult,
    isCurrentlyTesting: Boolean,
    onTestSingle: () -> Unit,
    onCopyIp: (String) -> Unit,
    onCopyDot: (String) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 5.dp)
            .clip(RoundedCornerShape(10.dp))
            .background(SumiCard)
            .border(0.5.dp, SumiBorder, RoundedCornerShape(10.dp))
            .padding(14.dp)
            .testTag("dns_card_${result.server.id}")
    ) {
        // Top line: Name, Category Kanji, and Status Tag
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                KanjiBadge(
                    kanji = result.server.category.kanji,
                    accentColor = if (result.server.category == DnsCategory.IRAN_UNBLOCK) ToriiRed else LanternAmber,
                    size = 22.dp,
                    fontSize = 11
                )
                Spacer(modifier = Modifier.width(8.dp))
                Column {
                    Text(
                        text = result.server.name,
                        fontFamily = ZenGothicFontFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        color = PureWhite
                    )
                    Text(
                        text = result.server.region,
                        fontFamily = ZenGothicFontFamily,
                        fontSize = 11.sp,
                        color = ZenGray
                    )
                }
            }

            if (isCurrentlyTesting) {
                CircularProgressIndicator(
                    color = ToriiRed,
                    strokeWidth = 2.dp,
                    modifier = Modifier.size(16.dp)
                )
            } else {
                StatusTag(status = result.status)
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Latency bar
        LatencyBar(latencyMs = result.latencyMs)

        Spacer(modifier = Modifier.height(10.dp))

        // Metrics Grid (Latency, Jitter, Loss, Score)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            MetricItem(
                label = "Latency",
                value = if (result.latencyMs > 0) "${result.latencyMs} ms" else "--",
                color = when {
                    result.latencyMs <= 0 -> ZenMuted
                    result.latencyMs < 60 -> BambooGreen
                    result.latencyMs < 140 -> Color(0xFF64D2FF)
                    else -> LanternAmber
                }
            )
            MetricItem(
                label = "Jitter",
                value = if (result.jitterMs >= 0 && result.latencyMs > 0) "±${result.jitterMs} ms" else "--",
                color = ZenWhite
            )
            MetricItem(
                label = "Loss",
                value = if (result.latencyMs > 0) "${result.packetLossPercent}%" else "--",
                color = if (result.packetLossPercent > 0) ToriiRed else ZenWhite
            )
            MetricItem(
                label = "Score",
                value = if (result.score > 0) "${result.score}" else "--",
                color = if (result.score >= 70) BambooGreen else ZenWhite
            )
        }

        Spacer(modifier = Modifier.height(10.dp))

        // IP Address & Copy actions
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(6.dp))
                .background(SumiCardElevated)
                .padding(horizontal = 10.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "PRIMARY: ${result.server.primaryIp}",
                    fontFamily = SpaceMonoFontFamily,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = ZenWhite
                )
                if (result.server.dotHostname.isNotEmpty()) {
                    Text(
                        text = "DoT: ${result.server.dotHostname}",
                        fontFamily = SpaceMonoFontFamily,
                        fontSize = 10.sp,
                        color = ZenMuted
                    )
                }
            }

            Row {
                IconButton(
                    onClick = { onCopyIp(result.server.primaryIp) },
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.ContentCopy,
                        contentDescription = "Copy IP",
                        tint = ZenGray,
                        modifier = Modifier.size(16.dp)
                    )
                }

                IconButton(
                    onClick = onTestSingle,
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Refresh,
                        contentDescription = "Test Server",
                        tint = ToriiRed,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun MetricItem(
    label: String,
    value: String,
    color: Color
) {
    Column {
        Text(
            text = label,
            fontFamily = ZenGothicFontFamily,
            fontSize = 10.sp,
            color = ZenMuted
        )
        Text(
            text = value,
            fontFamily = SpaceMonoFontFamily,
            fontWeight = FontWeight.Bold,
            fontSize = 13.sp,
            color = color
        )
    }
}

private fun copyToClipboard(context: Context, text: String, toastMessage: String) {
    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
    val clip = ClipData.newPlainText("KuroNet", text)
    clipboard.setPrimaryClip(clip)
    Toast.makeText(context, toastMessage, Toast.LENGTH_SHORT).show()
}
