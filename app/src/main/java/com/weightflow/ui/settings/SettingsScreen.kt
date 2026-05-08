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
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.weightflow.domain.CsvExporter
import com.weightflow.domain.WeightUnit
import com.weightflow.ui.theme.WFTokens
import com.weightflow.worker.WeightReminderWorker
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.time.LocalDate

private const val PRIVACY_URL = "https://vaibhavaher100.github.io/WeightFlow/privacy-policy/"
private const val TERMS_URL   = "https://vaibhavaher100.github.io/WeightFlow/terms-of-service/"

private val THEME_OPTIONS = listOf(
    Triple("lime",   "Lime",   Color(0xFFC8FF00)),
    Triple("rose",   "Rose",   Color(0xFFFF4081)),
    Triple("forest", "Forest", Color(0xFF4CAF50)),
    Triple("violet", "Violet", Color(0xFFBB86FC)),
    Triple("ocean",  "Ocean",  Color(0xFF00BCD4)),
    Triple("gold",   "Gold",   Color(0xFFFFD700)),
    Triple("sunset", "Sunset", Color(0xFFFF6B35)),
    Triple("ice",    "Ice",    Color(0xFF80DEEA)),
)

@Composable
fun SettingsScreen(viewModel: SettingsViewModel, onBack: () -> Unit) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val uriHandler = LocalUriHandler.current
    val scope = rememberCoroutineScope()

    // ── Permission / reminder state ───────────────────────────────────────────
    var showPermissionDeniedBanner by remember { mutableStateOf(false) }
    val requestNotificationPermission = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted -> viewModel.onReminderPermissionResult(granted) }

    // ── Plaintext / minimal CSV export state ──────────────────────────────────
    var pendingCsv by remember { mutableStateOf<String?>(null) }
    var showExportConfirm by remember { mutableStateOf(false) }

    // ── Encrypted ZIP export state ────────────────────────────────────────────
    var showEncryptedExportDialog by remember { mutableStateOf(false) }
    var encryptionErrorMessage by remember { mutableStateOf<String?>(null) }

    val accent = MaterialTheme.colorScheme.primary

    // ── File-picker launchers ─────────────────────────────────────────────────

    val csvExportLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.CreateDocument("text/csv"),
    ) { uri ->
        val csv = pendingCsv ?: return@rememberLauncherForActivityResult
        uri ?: return@rememberLauncherForActivityResult
        scope.launch(Dispatchers.IO) {
            context.contentResolver.openOutputStream(uri)?.use { it.write(csv.toByteArray()) }
        }
        pendingCsv = null
    }

    // Temp file for encrypted ZIP — held here so the zip picker callback can copy it.
    var pendingZipFile by remember { mutableStateOf<java.io.File?>(null) }
    var pendingZipFileName by remember { mutableStateOf("weightflow_export.zip") }

    val zipPickerLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.CreateDocument("application/zip"),
    ) { uri ->
        val zipFile = pendingZipFile ?: return@rememberLauncherForActivityResult
        uri ?: return@rememberLauncherForActivityResult
        scope.launch(Dispatchers.IO) {
            try {
                context.contentResolver.openOutputStream(uri)?.use { out ->
                    zipFile.inputStream().use { input -> input.copyTo(out) }
                }
            } finally {
                zipFile.delete()
                pendingZipFile = null
            }
        }
    }

    // ── Event collection ──────────────────────────────────────────────────────

    LaunchedEffect(Unit) {
        viewModel.events.collect { event ->
            when (event) {
                is SettingsEvent.ExportCsvReady -> {
                    val isMimimal = state.selectedExportFormat == ExportFormat.MINIMAL_CSV
                    pendingCsv = event.csvContent
                    val prefix = if (isMimimal) "weightflow_minimal_" else "weightflow_export_"
                    csvExportLauncher.launch("$prefix${LocalDate.now()}.csv")
                }

                is SettingsEvent.ExportEncryptedZipReady -> {
                    pendingZipFile = event.zipFile
                    pendingZipFileName = event.suggestedFileName
                    zipPickerLauncher.launch(event.suggestedFileName)
                }

                is SettingsEvent.ExportEncryptionFailed -> {
                    encryptionErrorMessage = event.reason
                }

                SettingsEvent.RequestNotificationPermission -> {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                        requestNotificationPermission.launch(Manifest.permission.POST_NOTIFICATIONS)
                    } else {
                        viewModel.onReminderPermissionResult(granted = true)
                    }
                }

                SettingsEvent.ReminderEnabled -> {
                    showPermissionDeniedBanner = false
                    WeightReminderWorker.schedule(context)
                }

                SettingsEvent.NotificationPermissionDenied -> {
                    showPermissionDeniedBanner = true
                }
            }
        }
    }

    // ── Dialogs ───────────────────────────────────────────────────────────────

    if (showExportConfirm) {
        PlaintextExportConfirmDialog(
            format = state.selectedExportFormat,
            onConfirm = {
                showExportConfirm = false
                viewModel.onExportCsv()
            },
            onDismiss = { showExportConfirm = false },
        )
    }

    if (showEncryptedExportDialog) {
        EncryptedExportDialog(
            onExport = { password ->
                showEncryptedExportDialog = false
                viewModel.onExportEncryptedZip(password)
            },
            onDismiss = { showEncryptedExportDialog = false },
        )
    }

    encryptionErrorMessage?.let { msg ->
        AlertDialog(
            onDismissRequest = { encryptionErrorMessage = null },
            title = { Text("Export failed") },
            text = { Text(msg, style = MaterialTheme.typography.bodyMedium) },
            confirmButton = {
                TextButton(onClick = { encryptionErrorMessage = null }) { Text("OK") }
            },
        )
    }

    // ── Main layout ───────────────────────────────────────────────────────────

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
                    if (!enabled) WeightReminderWorker.cancel(context)
                    viewModel.onReminderToggled(enabled)
                },
            )

            if (showPermissionDeniedBanner) {
                PermissionDeniedBanner(onDismiss = { showPermissionDeniedBanner = false })
            }

            // Data — export format selector
            SectionLabel("Data")
            ExportFormatSelector(
                selected = state.selectedExportFormat,
                accent = accent,
                onFormatChanged = viewModel::onExportFormatChanged,
            )

            // Export action row — context-aware per format
            when (state.selectedExportFormat) {
                ExportFormat.PLAINTEXT -> {
                    PlaintextWarningBanner()
                    SettingsActionRow("Export weight history", "Download as CSV") {
                        showExportConfirm = true
                    }
                }

                ExportFormat.ENCRYPTED_ZIP -> {
                    EncryptedExportInfoBanner()
                    SettingsActionRow("Export & Encrypt", "AES-256 protected ZIP") {
                        showEncryptedExportDialog = true
                    }
                }

                ExportFormat.MINIMAL_CSV -> {
                    MinimalCsvWarningBanner()
                    SettingsActionRow("Export Minimal CSV", "Date + weight only") {
                        showExportConfirm = true
                    }
                }
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

// ── Export dialogs ────────────────────────────────────────────────────────────

@Composable
private fun PlaintextExportConfirmDialog(
    format: ExportFormat,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
) {
    val (title, body) = when (format) {
        ExportFormat.MINIMAL_CSV ->
            "Export Minimal CSV?" to
                "This file contains dates and weights only. No profile or goal data is included. " +
                "Even so, a date+weight series may identify you when combined with other information. " +
                "Only share with apps you trust."
        else ->
            "Export weight history?" to
                "This file contains your full weight history. Once saved, the app you choose can " +
                "read this data. Only share with apps you trust."
    }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title) },
        text = { Text(body, style = MaterialTheme.typography.bodyMedium) },
        confirmButton = {
            TextButton(onClick = onConfirm) { Text("Export") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        },
    )
}

/**
 * Dialog for the ENCRYPTED_ZIP flow.
 *
 * Security notes (enforced here):
 * - Password is held in a [CharArray] backed by mutable local state — never a String.
 * - No clipboard shortcut is offered (Security finding #8).
 * - Password is never surfaced in SettingsUiState or emitted in any event.
 * - [onExport] receives the CharArray; the ViewModel zeros it.
 */
@Composable
private fun EncryptedExportDialog(
    onExport: (CharArray) -> Unit,
    onDismiss: () -> Unit,
) {
    var passwordString by remember { mutableStateOf("") }
    val isValid = passwordString.length >= CsvExporter.MIN_PASSWORD_LENGTH

    val strength = when {
        passwordString.length < CsvExporter.MIN_PASSWORD_LENGTH -> PasswordStrength.WEAK
        passwordString.length < 16 -> PasswordStrength.MEDIUM
        else -> PasswordStrength.STRONG
    }

    AlertDialog(
        onDismissRequest = {
            passwordString = ""
            onDismiss()
        },
        title = { Text("Set export password") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    "Your export will be protected with AES-256 encryption.",
                    style = MaterialTheme.typography.bodySmall,
                )
                Text(
                    "Note: This format is NOT supported by macOS Archive Utility. " +
                        "Use 7-Zip, WinRAR, or The Unarchiver to open the file.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                OutlinedTextField(
                    value = passwordString,
                    onValueChange = { passwordString = it },
                    label = { Text("Password (${CsvExporter.MIN_PASSWORD_LENGTH}+ characters)") },
                    visualTransformation = PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                    singleLine = true,
                    isError = passwordString.isNotEmpty() && !isValid,
                    supportingText = {
                        if (passwordString.isNotEmpty() && !isValid) {
                            Text("Must be at least ${CsvExporter.MIN_PASSWORD_LENGTH} characters")
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                )
                if (passwordString.isNotEmpty()) {
                    PasswordStrengthIndicator(strength)
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    if (isValid) {
                        val arr = passwordString.toCharArray()
                        passwordString = "" // clear local String copy
                        onExport(arr)
                    }
                },
                enabled = isValid,
            ) { Text("Export & Encrypt") }
        },
        dismissButton = {
            TextButton(onClick = {
                passwordString = ""
                onDismiss()
            }) { Text("Cancel") }
        },
    )
}

// ── Info/warning banners ──────────────────────────────────────────────────────

@Composable
private fun PlaintextWarningBanner() {
    InfoBanner(
        text = "This export is not encrypted. Keep the file secure and only share with apps you trust.",
        containerColor = MaterialTheme.colorScheme.errorContainer,
        textColor = MaterialTheme.colorScheme.onErrorContainer,
    )
}

@Composable
private fun EncryptedExportInfoBanner() {
    InfoBanner(
        text = "AES-256 encrypted ZIP. Readable by 7-Zip, WinRAR, The Unarchiver. " +
            "Not supported by macOS Archive Utility.",
        containerColor = MaterialTheme.colorScheme.secondaryContainer,
        textColor = MaterialTheme.colorScheme.onSecondaryContainer,
    )
}

@Composable
private fun MinimalCsvWarningBanner() {
    InfoBanner(
        text = "Removes profile, goal, and note data. Date and weight alone may still identify " +
            "you when combined with other information.",
        containerColor = MaterialTheme.colorScheme.tertiaryContainer,
        textColor = MaterialTheme.colorScheme.onTertiaryContainer,
    )
}

@Composable
private fun InfoBanner(text: String, containerColor: Color, textColor: Color) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(containerColor)
            .padding(horizontal = 14.dp, vertical = 10.dp),
    ) {
        Text(text = text, fontSize = 12.sp, color = textColor)
    }
}

// ── Export format selector ────────────────────────────────────────────────────

private data class FormatOption(
    val format: ExportFormat,
    val label: String,
    val description: String,
)

private val FORMAT_OPTIONS = listOf(
    FormatOption(
        ExportFormat.PLAINTEXT,
        "Plaintext CSV",
        "Full history, no encryption. All columns.",
    ),
    FormatOption(
        ExportFormat.ENCRYPTED_ZIP,
        "Encrypted ZIP",
        "AES-256 password-protected. 12+ character password required.",
    ),
    FormatOption(
        ExportFormat.MINIMAL_CSV,
        "Minimal CSV",
        "Date + weight only. No profile or notes. Still quasi-identifying.",
    ),
)

@Composable
private fun ExportFormatSelector(
    selected: ExportFormat,
    accent: Color,
    onFormatChanged: (ExportFormat) -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(WFTokens.Card)
            .border(1.dp, WFTokens.Border, RoundedCornerShape(14.dp))
            .padding(vertical = 4.dp),
    ) {
        FORMAT_OPTIONS.forEach { option ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(
                        indication = null,
                        interactionSource = remember { MutableInteractionSource() },
                    ) { onFormatChanged(option.format) }
                    .padding(horizontal = 12.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                RadioButton(
                    selected = selected == option.format,
                    onClick = { onFormatChanged(option.format) },
                    colors = RadioButtonDefaults.colors(selectedColor = accent),
                    modifier = Modifier.size(20.dp),
                )
                Spacer(Modifier.width(12.dp))
                Column {
                    Text(
                        option.label,
                        fontSize = 14.sp,
                        fontWeight = if (selected == option.format) FontWeight.Bold else FontWeight.Normal,
                        color = MaterialTheme.colorScheme.onBackground,
                    )
                    Text(option.description, fontSize = 11.sp, color = WFTokens.Text2)
                }
            }
        }
    }
}

// ── Password strength ─────────────────────────────────────────────────────────

private enum class PasswordStrength { WEAK, MEDIUM, STRONG }

@Composable
private fun PasswordStrengthIndicator(strength: PasswordStrength) {
    val (label, color) = when (strength) {
        PasswordStrength.WEAK   -> "Weak"   to MaterialTheme.colorScheme.error
        PasswordStrength.MEDIUM -> "Medium" to Color(0xFFFF9800)
        PasswordStrength.STRONG -> "Strong" to Color(0xFF4CAF50)
    }
    Text(
        text = "Password strength: $label",
        fontSize = 11.sp,
        color = color,
        fontWeight = FontWeight.Medium,
    )
}

// ── Shared components ─────────────────────────────────────────────────────────

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
private fun PermissionDeniedBanner(onDismiss: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(MaterialTheme.colorScheme.errorContainer)
            .border(1.dp, MaterialTheme.colorScheme.error.copy(alpha = 0.4f), RoundedCornerShape(12.dp))
            .padding(horizontal = 14.dp, vertical = 10.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = "Notifications require permission. Grant in Settings.",
            fontSize = 12.sp,
            color = MaterialTheme.colorScheme.onErrorContainer,
            modifier = Modifier.weight(1f),
        )
        Spacer(Modifier.width(8.dp))
        Text(
            text = "Dismiss",
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.error,
            modifier = Modifier
                .clickable(
                    indication = null,
                    interactionSource = remember { MutableInteractionSource() },
                    onClick = onDismiss,
                )
                .padding(4.dp),
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
