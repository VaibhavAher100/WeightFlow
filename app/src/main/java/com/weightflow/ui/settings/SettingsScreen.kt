package com.weightflow.ui.settings

import android.Manifest
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.weightflow.domain.WeightUnit
import com.weightflow.ui.theme.WFTokens
import com.weightflow.worker.WeightReminderWorker
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.time.LocalDate

private const val PRIVACY_URL = "https://vaibhavaher100.github.io/WeightFlow/privacy-policy/"
private const val TERMS_URL   = "https://vaibhavaher100.github.io/WeightFlow/terms-of-service/"

private val THEME_OPTIONS = listOf(
    Triple("lime", "Lime", Color(0xFFC8FF00)),
    Triple("rose", "Rose", Color(0xFFFF4081)),
    Triple("forest", "Forest", Color(0xFF4CAF50)),
    Triple("violet", "Violet", Color(0xFFBB86FC)),
    Triple("ocean", "Ocean", Color(0xFF00BCD4)),
    Triple("gold", "Gold", Color(0xFFFFD700)),
    Triple("sunset", "Sunset", Color(0xFFFF6B35)),
    Triple("ice", "Ice", Color(0xFF80DEEA)),
)

@Composable
fun SettingsScreen(viewModel: SettingsViewModel, onBack: () -> Unit) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val uriHandler = LocalUriHandler.current
    val scope = rememberCoroutineScope()
    val requestNotificationPermission = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { /* gracefully handle grant/deny */ }
    var pendingCsv by remember { mutableStateOf<String?>(null) }
    var showExportConfirm by remember { mutableStateOf(false) }
    val accent = MaterialTheme.colorScheme.primary

    val exportLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.CreateDocument("text/csv"),
    ) { uri ->
        val csv = pendingCsv ?: return@rememberLauncherForActivityResult
        uri ?: return@rememberLauncherForActivityResult
        scope.launch(Dispatchers.IO) {
            context.contentResolver.openOutputStream(uri)?.use { it.write(csv.toByteArray()) }
        }
        pendingCsv = null
    }

    LaunchedEffect(Unit) {
        viewModel.events.collect { event ->
            when (event) {
                is SettingsEvent.ExportCsvReady -> {
                    pendingCsv = event.csvContent
                    exportLauncher.launch("weightflow_export_${LocalDate.now()}.csv")
                }
            }
        }
    }

    if (showExportConfirm) {
        AlertDialog(
            onDismissRequest = { showExportConfirm = false },
            title = { Text("Export weight history?") },
            text = {
                Text(
                    "This file contains your full weight history. Once saved, the app you choose can read this data. Only share with apps you trust.",
                    style = MaterialTheme.typography.bodyMedium,
                )
            },
            confirmButton = {
                TextButton(onClick = { showExportConfirm = false; viewModel.onExportCsv() }) {
                    Text("Export")
                }
            },
            dismissButton = {
                TextButton(onClick = { showExportConfirm = false }) { Text("Cancel") }
            },
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
    ) {
        // Header
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 6.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            IconButton(onClick = onBack, modifier = Modifier.size(44.dp)) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back", tint = WFTokens.Text2)
            }
            Text(
                "Settings",
                fontSize = 22.sp,
                fontWeight = FontWeight.ExtraBold,
                color = MaterialTheme.colorScheme.onBackground,
            )
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            // Weight Unit
            SectionLabel("Weight Unit")
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                WeightUnit.entries.forEach { unit ->
                    val selected = state.weightUnit == unit
                    UnitChip(label = unit.name, selected = selected, accent = accent) {
                        viewModel.onUnitChanged(unit)
                    }
                }
            }

            // Theme
            SectionLabel("Theme")
            ThemeGrid(
                selectedPalette = state.themePalette,
                onThemeSelected = viewModel::onThemeSelected,
                accent = accent,
            )

            // Notifications
            SectionLabel("Notifications")
            SettingsToggleRow(
                title = "Daily reminder",
                subtitle = "Reminds you to log each day",
                checked = state.reminderEnabled,
                accent = accent,
                onCheckedChange = { enabled ->
                    viewModel.onReminderToggled(enabled)
                    if (enabled) {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                            requestNotificationPermission.launch(Manifest.permission.POST_NOTIFICATIONS)
                        }
                        WeightReminderWorker.schedule(context)
                    } else {
                        WeightReminderWorker.cancel(context)
                    }
                },
            )

            // Data
            SectionLabel("Data")
            SettingsActionRow("Export weight history", "Download as CSV") {
                showExportConfirm = true
            }

            // Legal
            SectionLabel("Legal")
            SettingsActionRow("Privacy Policy", "How we handle your data") {
                runCatching { uriHandler.openUri(PRIVACY_URL) }
            }
            SettingsActionRow("Terms of Service", "Usage terms") {
                runCatching { uriHandler.openUri(TERMS_URL) }
            }

            Spacer(Modifier.height(32.dp))
        }
    }
}

// ── Components ────────────────────────────────────────────────────────────────

@Composable
private fun SectionLabel(text: String) {
    Text(
        text = text.uppercase(),
        fontSize = 10.sp,
        fontWeight = FontWeight.Bold,
        letterSpacing = 1.5.sp,
        color = WFTokens.Text3,
        modifier = Modifier.padding(top = 10.dp, start = 2.dp),
    )
}

@Composable
private fun UnitChip(label: String, selected: Boolean, accent: Color, onClick: () -> Unit) {
    val bg     = if (selected) accent else WFTokens.Card
    val fg     = if (selected) MaterialTheme.colorScheme.onPrimary else WFTokens.Text2
    val border = if (selected) accent else WFTokens.Border
    Text(
        text = label,
        fontSize = 13.sp,
        fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal,
        color = fg,
        modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .background(bg)
            .border(1.dp, border, RoundedCornerShape(8.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 8.dp),
    )
}

@Composable
private fun ThemeGrid(
    selectedPalette: String,
    onThemeSelected: (String) -> Unit,
    accent: Color,
) {
    LazyVerticalGrid(
        columns = GridCells.Fixed(2),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 0.dp)
            .heightIn(max = 320.dp),
    ) {
        items(THEME_OPTIONS) { (key, name, dotColor) ->
            val isSelected = key == selectedPalette
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        if (isSelected) WFTokens.accentSoft(accent) else WFTokens.Card,
                        RoundedCornerShape(12.dp),
                    )
                    .border(
                        1.dp,
                        if (isSelected) WFTokens.accentBorder(accent) else WFTokens.Border,
                        RoundedCornerShape(12.dp),
                    )
                    .clickable { onThemeSelected(key) }
                    .padding(horizontal = 12.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Box(
                    Modifier
                        .size(14.dp)
                        .background(dotColor, RoundedCornerShape(999.dp))
                )
                Spacer(Modifier.width(8.dp))
                Text(
                    text = name,
                    fontSize = 12.sp,
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                    color = if (isSelected) MaterialTheme.colorScheme.onBackground else WFTokens.Text2,
                )
            }
        }
    }
}

@Composable
private fun SettingsToggleRow(
    title: String,
    subtitle: String,
    checked: Boolean,
    accent: Color,
    onCheckedChange: (Boolean) -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(WFTokens.Card)
            .border(1.dp, WFTokens.Border, RoundedCornerShape(14.dp))
            .padding(horizontal = 16.dp, vertical = 14.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(title, fontSize = 14.sp, fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onBackground)
            Text(subtitle, fontSize = 12.sp, color = WFTokens.Text2)
        }
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = MaterialTheme.colorScheme.onPrimary,
                checkedTrackColor = accent,
                uncheckedTrackColor = WFTokens.Elevated,
                uncheckedBorderColor = WFTokens.Border,
            ),
        )
    }
}

@Composable
private fun SettingsActionRow(title: String, subtitle: String, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(WFTokens.Card)
            .border(1.dp, WFTokens.Border, RoundedCornerShape(14.dp))
            .clickable(
                indication = null,
                interactionSource = remember { MutableInteractionSource() },
                onClick = onClick,
            )
            .padding(horizontal = 16.dp, vertical = 14.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column {
            Text(title, fontSize = 14.sp, fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onBackground)
            Text(subtitle, fontSize = 12.sp, color = WFTokens.Text2)
        }
        Text("›", fontSize = 20.sp, color = WFTokens.Text3)
    }
}
