package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Archive
import androidx.compose.material.icons.filled.NetworkCheck
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.MainViewModel
import com.example.ui.cleanip.CleanIpScreen
import com.example.ui.components.KanjiBadge
import com.example.ui.diagnostics.DiagnosticsScreen
import com.example.ui.dns.DnsScreen
import com.example.ui.theme.BambooGreen
import com.example.ui.theme.LanternAmber
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.theme.PureWhite
import com.example.ui.theme.SpaceMonoFontFamily
import com.example.ui.theme.SumiBlack
import com.example.ui.theme.SumiBorder
import com.example.ui.theme.SumiCard
import com.example.ui.theme.SumiCardElevated
import com.example.ui.theme.SumiSurface
import com.example.ui.theme.ToriiRed
import com.example.ui.theme.ZenGothicFontFamily
import com.example.ui.theme.ZenGray
import com.example.ui.theme.ZenMuted
import com.example.ui.theme.ZenWhite
import com.example.ui.vault.VaultScreen
import com.example.util.HapticUtil

enum class NavigationTab(val title: String, val kanji: String, val icon: ImageVector) {
    DNS("DNS 測", "測", Icons.Default.Speed),
    CLEAN_IP("Clean IP 境", "境", Icons.Default.Security),
    VAULT("Vault 存", "存", Icons.Default.Archive),
    DIAGNOSTICS("Diagnostics 静", "静", Icons.Default.NetworkCheck)
}

class MainActivity : ComponentActivity() {

    private val viewModel: MainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                KuroNetApp(viewModel = viewModel)
            }
        }
    }
}

@Composable
fun KuroNetApp(viewModel: MainViewModel) {
    var selectedTab by rememberSaveable { mutableStateOf(NavigationTab.DNS) }
    val networkStatus by viewModel.networkStatus.collectAsState()
    val haptic = LocalHapticFeedback.current

    // Handle back button to return to DNS tab
    if (selectedTab != NavigationTab.DNS) {
        BackHandler {
            selectedTab = NavigationTab.DNS
        }
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        contentWindowInsets = WindowInsets.safeDrawing,
        topBar = {
            KuroNetTopBar(
                selectedTab = selectedTab,
                isOnline = networkStatus.isConnected,
                networkType = networkStatus.typeName
            )
        },
        bottomBar = {
            KuroNetBottomBar(
                currentTab = selectedTab,
                onTabSelected = {
                    HapticUtil.performClickHaptic(haptic)
                    selectedTab = it
                }
            )
        },
        containerColor = SumiBlack
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            AnimatedContent(
                targetState = selectedTab,
                transitionSpec = {
                    (slideInHorizontally(initialOffsetX = { 40 }) + fadeIn()).togetherWith(
                        slideOutHorizontally(targetOffsetX = { -40 }) + fadeOut()
                    )
                },
                label = "tab_transition"
            ) { targetTab ->
                when (targetTab) {
                    NavigationTab.DNS -> DnsScreen(viewModel = viewModel)
                    NavigationTab.CLEAN_IP -> CleanIpScreen(viewModel = viewModel)
                    NavigationTab.VAULT -> VaultScreen(viewModel = viewModel)
                    NavigationTab.DIAGNOSTICS -> DiagnosticsScreen(viewModel = viewModel)
                }
            }
        }
    }
}

@Composable
private fun KuroNetTopBar(
    selectedTab: NavigationTab,
    isOnline: Boolean,
    networkType: String
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(SumiSurface)
            .border(0.5.dp, SumiBorder)
            .padding(horizontal = 16.dp, vertical = 10.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            KanjiBadge(
                kanji = "黒",
                accentColor = ToriiRed,
                size = 28.dp,
                fontSize = 14
            )
            Spacer(modifier = Modifier.width(10.dp))
            Column {
                Text(
                    text = "KuroNet",
                    fontFamily = ZenGothicFontFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = 17.sp,
                    color = PureWhite
                )
                Text(
                    text = "ZEN NETWORK BENCHMARK",
                    fontFamily = SpaceMonoFontFamily,
                    fontSize = 8.sp,
                    letterSpacing = 1.sp,
                    color = ZenGray
                )
            }
        }

        // Live Network Status Chip
        Row(
            modifier = Modifier
                .clip(RoundedCornerShape(12.dp))
                .background(if (isOnline) BambooGreen.copy(alpha = 0.12f) else LanternAmber.copy(alpha = 0.12f))
                .border(
                    0.5.dp,
                    if (isOnline) BambooGreen.copy(alpha = 0.4f) else LanternAmber.copy(alpha = 0.4f),
                    RoundedCornerShape(12.dp)
                )
                .padding(horizontal = 9.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(6.dp)
                    .clip(CircleShape)
                    .background(if (isOnline) BambooGreen else LanternAmber)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = if (isOnline) networkType else "Offline Vault",
                fontFamily = SpaceMonoFontFamily,
                fontSize = 10.sp,
                color = if (isOnline) BambooGreen else LanternAmber
            )
        }
    }
}

@Composable
private fun KuroNetBottomBar(
    currentTab: NavigationTab,
    onTabSelected: (NavigationTab) -> Unit
) {
    NavigationBar(
        modifier = Modifier
            .fillMaxWidth()
            .navigationBarsPadding(),
        containerColor = SumiSurface,
        tonalElevation = 0.dp
    ) {
        NavigationTab.values().forEach { tab ->
            val isSelected = currentTab == tab
            NavigationBarItem(
                selected = isSelected,
                onClick = { onTabSelected(tab) },
                icon = {
                    Icon(
                        imageVector = tab.icon,
                        contentDescription = tab.title,
                        modifier = Modifier.size(20.dp)
                    )
                },
                label = {
                    Text(
                        text = tab.title,
                        fontFamily = ZenGothicFontFamily,
                        fontSize = 11.sp,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                    )
                },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = PureWhite,
                    selectedTextColor = PureWhite,
                    indicatorColor = ToriiRed,
                    unselectedIconColor = ZenMuted,
                    unselectedTextColor = ZenMuted
                ),
                modifier = Modifier.testTag("nav_tab_${tab.name.lowercase()}")
            )
        }
    }
}
