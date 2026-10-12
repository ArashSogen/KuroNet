package com.example.ui.vault

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.StarBorder
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
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
import com.example.data.local.CleanIpEntity
import com.example.data.local.EmergencyConfigEntity
import com.example.ui.MainViewModel
import com.example.ui.components.KanjiBadge
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

@Composable
fun VaultScreen(
    viewModel: MainViewModel,
    modifier: Modifier = Modifier
) {
    val savedIps by viewModel.savedCleanIps.collectAsState()
    val configs by viewModel.emergencyConfigs.collectAsState()
    val context = LocalContext.current
    val haptic = LocalHapticFeedback.current

    var selectedTab by remember { mutableStateOf(0) } // 0: Emergency Configs, 1: Saved Clean IPs
    var showAddConfigDialog by remember { mutableStateOf(false) }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .testTag("vault_screen_list"),
        contentPadding = PaddingValues(bottom = 96.dp)
    ) {
        // Vault Header
        item {
            VaultHeaderCard()
        }

        // Tab Selector
        item {
            TabRow(
                selectedTabIndex = selectedTab,
                containerColor = SumiCard,
                contentColor = PureWhite,
                indicator = { tabPositions ->
                    TabRowDefaults.SecondaryIndicator(
                        Modifier.tabIndicatorOffset(tabPositions[selectedTab]),
                        color = ToriiRed
                    )
                },
                modifier = Modifier
                    .padding(horizontal = 16.dp, vertical = 8.dp)
                    .clip(RoundedCornerShape(8.dp))
            ) {
                Tab(
                    selected = selectedTab == 0,
                    onClick = {
                        HapticUtil.performClickHaptic(haptic)
                        selectedTab = 0
                    },
                    text = {
                        Text(
                            text = "Emergency Configs (${configs.size})",
                            fontFamily = ZenGothicFontFamily,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 12.sp
                        )
                    }
                )
                Tab(
                    selected = selectedTab == 1,
                    onClick = {
                        HapticUtil.performClickHaptic(haptic)
                        selectedTab = 1
                    },
                    text = {
                        Text(
                            text = "Cached Clean IPs (${savedIps.size})",
                            fontFamily = ZenGothicFontFamily,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 12.sp
                        )
                    }
                )
            }
        }

        if (selectedTab == 0) {
            // Add Config Button
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 4.dp),
                    horizontalArrangement = Arrangement.End
                ) {
                    Button(
                        onClick = {
                            HapticUtil.performClickHaptic(haptic)
                            showAddConfigDialog = true
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = ToriiRed,
                            contentColor = PureWhite
                        ),
                        shape = RoundedCornerShape(6.dp),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Icon(imageVector = Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Add Custom Config", fontFamily = ZenGothicFontFamily, fontSize = 12.sp)
                    }
                }
            }

            // Config Items
            items(items = configs, key = { it.id }) { config ->
                EmergencyConfigCard(
                    config = config,
                    onCopy = {
                        HapticUtil.performClickHaptic(haptic)
                        copyToClipboard(context, config.content, "Config copied: ${config.title}")
                    },
                    onDelete = if (!config.isPreset) {
                        {
                            HapticUtil.performClickHaptic(haptic)
                            viewModel.deleteEmergencyConfig(config)
                        }
                    } else null
                )
            }
        } else {
            // Saved Clean IPs Tab
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Stored on-device for offline blackouts",
                        fontFamily = SpaceMonoFontFamily,
                        fontSize = 10.sp,
                        color = ZenMuted
                    )

                    TextButton(
                        onClick = {
                            HapticUtil.performClickHaptic(haptic)
                            viewModel.clearNonFavoriteCleanIps()
                        }
                    ) {
                        Text("Clear Non-Starred", color = ToriiRed, fontSize = 11.sp, fontFamily = SpaceMonoFontFamily)
                    }
                }
            }

            if (savedIps.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(32.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "No saved Clean IPs yet.\nRun the Clean IP scanner to cache discovered IPs.",
                            fontFamily = ZenGothicFontFamily,
                            fontSize = 13.sp,
                            color = ZenMuted,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )
                    }
                }
            } else {
                items(items = savedIps, key = { it.ip }) { ipEntity ->
                    SavedCleanIpCard(
                        entity = ipEntity,
                        onToggleFavorite = {
                            HapticUtil.performClickHaptic(haptic)
                            viewModel.toggleEntityFavorite(ipEntity)
                        },
                        onCopyIp = {
                            HapticUtil.performClickHaptic(haptic)
                            copyToClipboard(context, "${ipEntity.ip}:${ipEntity.port}", "Copied: ${ipEntity.ip}")
                        },
                        onDelete = {
                            HapticUtil.performClickHaptic(haptic)
                            viewModel.deleteCleanIp(ipEntity.ip)
                        }
                    )
                }
            }
        }
    }

    if (showAddConfigDialog) {
        AddConfigDialog(
            onDismiss = { showAddConfigDialog = false },
            onAdd = { title, cat, content, desc ->
                HapticUtil.performClickHaptic(haptic)
                viewModel.addEmergencyConfig(title, cat, content, desc)
                showAddConfigDialog = false
            }
        )
    }
}

@Composable
private fun VaultHeaderCard() {
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
            KanjiBadge(kanji = "存", accentColor = ToriiRed, size = 32.dp, fontSize = 16)
            Spacer(modifier = Modifier.width(10.dp))
            Column {
                Text(
                    text = "Offline Vault & Emergency Configs (非常金庫)",
                    fontFamily = ZenGothicFontFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = 17.sp,
                    color = PureWhite
                )
                Text(
                    text = "Room Cached Profiles for Blackout Scenarios",
                    fontFamily = SpaceMonoFontFamily,
                    fontSize = 10.sp,
                    color = ZenGray
                )
            }
        }
    }
}

@Composable
private fun EmergencyConfigCard(
    config: EmergencyConfigEntity,
    onCopy: () -> Unit,
    onDelete: (() -> Unit)?
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp)
            .clip(RoundedCornerShape(10.dp))
            .background(SumiCard)
            .border(0.5.dp, SumiBorder, RoundedCornerShape(10.dp))
            .padding(14.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                KanjiBadge(kanji = config.category.take(1), accentColor = LanternAmber, size = 22.dp, fontSize = 11)
                Spacer(modifier = Modifier.width(8.dp))
                Column {
                    Text(
                        text = config.title,
                        fontFamily = ZenGothicFontFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = PureWhite
                    )
                    Text(
                        text = config.category,
                        fontFamily = SpaceMonoFontFamily,
                        fontSize = 10.sp,
                        color = LanternAmber
                    )
                }
            }

            Row {
                IconButton(onClick = onCopy, modifier = Modifier.size(32.dp)) {
                    Icon(imageVector = Icons.Default.ContentCopy, contentDescription = "Copy", tint = ZenGray, modifier = Modifier.size(16.dp))
                }
                if (onDelete != null) {
                    IconButton(onClick = onDelete, modifier = Modifier.size(32.dp)) {
                        Icon(imageVector = Icons.Default.Delete, contentDescription = "Delete", tint = ToriiRed, modifier = Modifier.size(16.dp))
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = config.description,
            fontFamily = ZenGothicFontFamily,
            fontSize = 12.sp,
            color = ZenGray
        )

        Spacer(modifier = Modifier.height(10.dp))

        // Code snippet box
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(6.dp))
                .background(SumiCardElevated)
                .padding(10.dp)
        ) {
            Text(
                text = config.content.take(280) + if (config.content.length > 280) "..." else "",
                fontFamily = SpaceMonoFontFamily,
                fontSize = 11.sp,
                lineHeight = 16.sp,
                color = ZenWhite
            )
        }
    }
}

@Composable
private fun SavedCleanIpCard(
    entity: CleanIpEntity,
    onToggleFavorite: () -> Unit,
    onCopyIp: () -> Unit,
    onDelete: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(SumiCard)
            .border(0.5.dp, SumiBorder, RoundedCornerShape(8.dp))
            .padding(12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column {
            Text(
                text = "${entity.ip}:${entity.port}",
                fontFamily = SpaceMonoFontFamily,
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp,
                color = PureWhite
            )
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "${entity.latencyMs} ms",
                    fontFamily = SpaceMonoFontFamily,
                    fontSize = 12.sp,
                    color = BambooGreen
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = entity.provider,
                    fontFamily = ZenGothicFontFamily,
                    fontSize = 10.sp,
                    color = ZenGray
                )
            }
        }

        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onToggleFavorite, modifier = Modifier.size(32.dp)) {
                Icon(
                    imageVector = if (entity.isFavorite) Icons.Default.Star else Icons.Default.StarBorder,
                    contentDescription = null,
                    tint = if (entity.isFavorite) LanternAmber else ZenMuted,
                    modifier = Modifier.size(18.dp)
                )
            }
            IconButton(onClick = onCopyIp, modifier = Modifier.size(32.dp)) {
                Icon(imageVector = Icons.Default.ContentCopy, contentDescription = null, tint = ZenGray, modifier = Modifier.size(16.dp))
            }
            IconButton(onClick = onDelete, modifier = Modifier.size(32.dp)) {
                Icon(imageVector = Icons.Default.Delete, contentDescription = null, tint = ToriiRed, modifier = Modifier.size(16.dp))
            }
        }
    }
}

@Composable
private fun AddConfigDialog(
    onDismiss: () -> Unit,
    onAdd: (String, String, String, String) -> Unit
) {
    var title by remember { mutableStateOf("") }
    var category by remember { mutableStateOf("V2RAY") }
    var description by remember { mutableStateOf("") }
    var content by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = SumiCardElevated,
        title = {
            Text("Add Emergency Configuration", fontFamily = ZenGothicFontFamily, fontWeight = FontWeight.Bold, color = PureWhite, fontSize = 16.sp)
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Config Title") },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = PureWhite,
                        unfocusedTextColor = PureWhite,
                        focusedBorderColor = ToriiRed,
                        unfocusedBorderColor = SumiBorder
                    )
                )

                OutlinedTextField(
                    value = category,
                    onValueChange = { category = it },
                    label = { Text("Category (e.g. V2RAY, WARP, DNS)") },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = PureWhite,
                        unfocusedTextColor = PureWhite,
                        focusedBorderColor = ToriiRed,
                        unfocusedBorderColor = SumiBorder
                    )
                )

                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Description / Notes") },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = PureWhite,
                        unfocusedTextColor = PureWhite,
                        focusedBorderColor = ToriiRed,
                        unfocusedBorderColor = SumiBorder
                    )
                )

                OutlinedTextField(
                    value = content,
                    onValueChange = { content = it },
                    label = { Text("Configuration Payload / Keys / IP") },
                    minLines = 4,
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
                onClick = {
                    if (title.isNotBlank() && content.isNotBlank()) {
                        onAdd(title, category, content, description)
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = ToriiRed, contentColor = PureWhite)
            ) {
                Text("Save to Vault")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = ZenGray)
            }
        }
    )
}

private fun copyToClipboard(context: Context, text: String, toastMessage: String) {
    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
    val clip = ClipData.newPlainText("KuroNet", text)
    clipboard.setPrimaryClip(clip)
    Toast.makeText(context, toastMessage, Toast.LENGTH_SHORT).show()
}
