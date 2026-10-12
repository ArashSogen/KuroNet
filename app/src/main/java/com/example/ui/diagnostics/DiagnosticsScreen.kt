package com.example.ui.diagnostics

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.NetworkCheck
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.diagnostic.SocketPingHelper
import com.example.ui.MainViewModel
import com.example.ui.components.KanjiBadge
import com.example.ui.components.MetricBox
import com.example.ui.theme.BambooGreen
import com.example.ui.theme.LanternAmber
import com.example.ui.theme.PureWhite
import com.example.ui.theme.SpaceMonoFontFamily
import com.example.ui.theme.SumiBorder
import com.example.ui.theme.SumiCard
import com.example.ui.theme.SumiCardElevated
import com.example.ui.theme.ToriiRed
import com.example.ui.theme.ZenGothicFontFamily
import com.example.ui.theme.ZenGray
import com.example.ui.theme.ZenMuted
import com.example.ui.theme.ZenWhite
import com.example.util.HapticUtil
import kotlinx.coroutines.launch

@Composable
fun DiagnosticsScreen(
    viewModel: MainViewModel,
    modifier: Modifier = Modifier
) {
    val netStatus by viewModel.networkStatus.collectAsState()
    val scope = rememberCoroutineScope()
    val haptic = LocalHapticFeedback.current

    var isCheckingGateway by remember { mutableStateOf(false) }
    var gatewayPingMs by remember { mutableStateOf<Long?>(null) }
    var cfPingMs by remember { mutableStateOf<Long?>(null) }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .testTag("diagnostics_screen_list"),
        contentPadding = PaddingValues(bottom = 96.dp)
    ) {
        item {
            DiagnosticsHeader()
        }

        // Live Connection Telemetry Card
        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 6.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(SumiCard)
                    .border(0.5.dp, SumiBorder, RoundedCornerShape(12.dp))
                    .padding(16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(10.dp)
                                .clip(CircleShape)
                                .background(if (netStatus.isConnected) BambooGreen else ToriiRed)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = if (netStatus.isConnected) "Online (${netStatus.typeName})" else "Offline Mode Active",
                            fontFamily = ZenGothicFontFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp,
                            color = PureWhite
                        )
                    }

                    IconButton(
                        onClick = {
                            HapticUtil.performClickHaptic(haptic)
                            viewModel.refreshNetworkStatus()
                            scope.launch {
                                isCheckingGateway = true
                                val cfResult = SocketPingHelper.probeTcp("1.1.1.1", 53, 1500)
                                val gwResult = SocketPingHelper.probeTcp("8.8.8.8", 53, 1500)
                                cfPingMs = if (cfResult.success) cfResult.latencyMs else -1L
                                gatewayPingMs = if (gwResult.success) gwResult.latencyMs else -1L
                                isCheckingGateway = false
                            }
                        },
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(imageVector = Icons.Default.Refresh, contentDescription = "Refresh", tint = ToriiRed, modifier = Modifier.size(18.dp))
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    MetricBox(
                        label = "Cloudflare Edge",
                        value = if (cfPingMs != null && cfPingMs!! > 0) "${cfPingMs}ms" else if (isCheckingGateway) "..." else "--",
                        valueColor = if (cfPingMs != null && cfPingMs!! > 0) BambooGreen else ZenWhite,
                        modifier = Modifier.weight(1f)
                    )
                    MetricBox(
                        label = "Google Anycast",
                        value = if (gatewayPingMs != null && gatewayPingMs!! > 0) "${gatewayPingMs}ms" else if (isCheckingGateway) "..." else "--",
                        valueColor = if (gatewayPingMs != null && gatewayPingMs!! > 0) Color(0xFF64D2FF) else ZenWhite,
                        modifier = Modifier.weight(1f)
                    )
                    MetricBox(
                        label = "Metered",
                        value = if (netStatus.isMetered) "Yes" else "No",
                        valueColor = if (netStatus.isMetered) LanternAmber else ZenWhite,
                        modifier = Modifier.weight(1f)
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                Button(
                    onClick = {
                        HapticUtil.performClickHaptic(haptic)
                        scope.launch {
                            isCheckingGateway = true
                            val cfResult = SocketPingHelper.probeTcp("1.1.1.1", 53, 1500)
                            val gwResult = SocketPingHelper.probeTcp("8.8.8.8", 53, 1500)
                            cfPingMs = if (cfResult.success) cfResult.latencyMs else -1L
                            gatewayPingMs = if (gwResult.success) gwResult.latencyMs else -1L
                            isCheckingGateway = false
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = SumiCardElevated, contentColor = PureWhite),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    if (isCheckingGateway) {
                        CircularProgressIndicator(color = ToriiRed, strokeWidth = 2.dp, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Measuring Gateway Latency...", fontFamily = ZenGothicFontFamily, fontSize = 13.sp)
                    } else {
                        Icon(imageVector = Icons.Default.Speed, contentDescription = null, modifier = Modifier.size(16.dp), tint = ToriiRed)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Run Quick Gateway Probe", fontFamily = ZenGothicFontFamily, fontSize = 13.sp)
                    }
                }
            }
        }

        // MTU Optimization Card
        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 6.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(SumiCard)
                    .border(0.5.dp, SumiBorder, RoundedCornerShape(12.dp))
                    .padding(16.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    KanjiBadge(kanji = "調", accentColor = LanternAmber, size = 26.dp, fontSize = 12)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "MTU Optimization for Iran Networks",
                        fontFamily = ZenGothicFontFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        color = PureWhite
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "• Default Ethernet/Wi-Fi MTU is 1500 bytes.\n• On Iranian mobile carriers (Hamrah-e Aval / Irancell), DPI devices inspect packets and frequently drop large UDP packets.\n• Recommended WARP MTU: 1280 or 1360 bytes.\n• Setting MTU to 1280 prevents IP fragmentation and significantly reduces packet loss during severe network throttling.",
                    fontFamily = SpaceMonoFontFamily,
                    fontSize = 11.sp,
                    lineHeight = 18.sp,
                    color = ZenGray
                )
            }
        }

        // Censorship Circumvention Architecture Card
        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 6.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(SumiCard)
                    .border(0.5.dp, SumiBorder, RoundedCornerShape(12.dp))
                    .padding(16.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    KanjiBadge(kanji = "破", accentColor = ToriiRed, size = 26.dp, fontSize = 12)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Bypass Tactics: Clean IP + TLS Fragment",
                        fontFamily = ZenGothicFontFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        color = PureWhite
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "1. Clean Anycast IP: Directs traffic to unblacklisted Cloudflare edge nodes, eliminating IP-level connection resets.\n2. TLS ClientHello Fragmentation: Splits the initial TLS Handshake into small TCP segments (e.g. 1-3 bytes), making it impossible for DPI middleboxes to detect the SNI domain.\n3. DoT / DoH Encrypted DNS: Enforces DNS-over-TLS (port 853) to completely stop DNS poisoning and false redirection.",
                    fontFamily = ZenGothicFontFamily,
                    fontSize = 12.sp,
                    lineHeight = 18.sp,
                    color = ZenGray
                )
            }
        }

        // Japanese Aesthetic Card
        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 6.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(SumiCardElevated)
                    .border(0.5.dp, SumiBorder, RoundedCornerShape(12.dp))
                    .padding(16.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    KanjiBadge(kanji = "静", accentColor = ToriiRed, size = 26.dp, fontSize = 12)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Zen Philosophy (侘寂 & 簡素)",
                        fontFamily = ZenGothicFontFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        color = PureWhite
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "KuroNet is crafted according to 'Kanso' (simplicity) and 'Shibui' (subtle elegance) — stripping away superfluous clutter, delivering precise latency diagnostics in pure high-contrast monochrome, and keeping essential connection configs resilient during internet disruptions.",
                    fontFamily = ZenGothicFontFamily,
                    fontSize = 12.sp,
                    lineHeight = 18.sp,
                    color = ZenGray
                )
            }
        }
    }
}

@Composable
private fun DiagnosticsHeader() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
            .clip(RoundedCornerShape(14.dp))
            .background(SumiCard)
            .border(1.dp, SumiBorder, RoundedCornerShape(14.dp))
            .padding(16.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            KanjiBadge(kanji = "診", accentColor = ToriiRed, size = 32.dp, fontSize = 16)
            Spacer(modifier = Modifier.width(10.dp))
            Column {
                Text(
                    text = "Network Diagnostics (診断)",
                    fontFamily = ZenGothicFontFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = 17.sp,
                    color = PureWhite
                )
                Text(
                    text = "Telemetry, MTU Optimization & DPI Circumvention",
                    fontFamily = SpaceMonoFontFamily,
                    fontSize = 10.sp,
                    color = ZenGray
                )
            }
        }
    }
}
