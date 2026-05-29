package com.weightflow.ui.settings

import android.Manifest
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.weightflow.ui.theme.WFTokens
import com.weightflow.worker.WeightReminderWorker
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.time.LocalDate

internal const val PRIVACY_URL = "https://vaibhavaher100.github.io/WeightFlow/privacy-policy/"
internal const val TERMS_URL   = "https://vaibhavaher100.github.io/WeightFlow/terms-of-service/"

internal val THEME_OPTIONS = listOf(
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
        if (uri == null) {
            scope.launch(Dispatchers.IO) {
                zipFile.delete()
                pendingZipFile = null
            }
            return@rememberLauncherForActivityResult
        }
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
            IconButton(onClick = onBack, modifier = Modifier.size(48.dp)) {
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
            SettingsUnitSection(
                selectedUnit = state.weightUnit,
                accent = accent,
                onUnitChanged = viewModel::onUnitChanged,
            )

            SettingsThemeSection(
                selectedPalette = state.themePalette,
                accent = accent,
                onThemeSelected = viewModel::onThemeSelected,
            )

            SettingsNotificationsSection(
                reminderEnabled = state.reminderEnabled,
                showPermissionDeniedBanner = showPermissionDeniedBanner,
                accent = accent,
                onReminderToggled = { enabled ->
                    if (!enabled) WeightReminderWorker.cancel(context)
                    viewModel.onReminderToggled(enabled)
                },
                onPermissionBannerDismiss = { showPermissionDeniedBanner = false },
            )

            SettingsDataSection(
                selectedExportFormat = state.selectedExportFormat,
                accent = accent,
                onFormatChanged = viewModel::onExportFormatChanged,
                onShowExportConfirm = { showExportConfirm = true },
                onShowEncryptedExportDialog = { showEncryptedExportDialog = true },
            )

            SettingsLegalSection(
                onPrivacyClick = { runCatching { uriHandler.openUri(PRIVACY_URL) } },
                onTermsClick = { runCatching { uriHandler.openUri(TERMS_URL) } },
            )

            Spacer(Modifier.height(32.dp))
        }
    }
}

