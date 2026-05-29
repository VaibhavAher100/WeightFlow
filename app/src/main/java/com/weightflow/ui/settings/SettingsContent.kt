package com.weightflow.ui.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.weightflow.domain.WeightUnit

// ── Section wrappers (called from SettingsScreen) ─────────────────────────────

@Composable
internal fun SettingsUnitSection(
    selectedUnit: WeightUnit,
    accent: Color,
    onUnitChanged: (WeightUnit) -> Unit,
) {
    SectionLabel("Weight Unit")
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        WeightUnit.entries.forEach { unit ->
            val selected = selectedUnit == unit
            UnitChip(label = unit.name, selected = selected, accent = accent) {
                onUnitChanged(unit)
            }
        }
    }
}

@Composable
internal fun SettingsThemeSection(
    selectedPalette: String,
    accent: Color,
    onThemeSelected: (String) -> Unit,
) {
    SectionLabel("Theme")
    ThemeGrid(
        selectedPalette = selectedPalette,
        onThemeSelected = onThemeSelected,
        accent = accent,
    )
}

@Composable
internal fun SettingsNotificationsSection(
    reminderEnabled: Boolean,
    showPermissionDeniedBanner: Boolean,
    accent: Color,
    onReminderToggled: (Boolean) -> Unit,
    onPermissionBannerDismiss: () -> Unit,
) {
    SectionLabel("Notifications")
    SettingsToggleRow(
        title = "Daily reminder",
        subtitle = "Reminds you to log each day",
        checked = reminderEnabled,
        accent = accent,
        onCheckedChange = onReminderToggled,
    )
    if (showPermissionDeniedBanner) {
        PermissionDeniedBanner(onDismiss = onPermissionBannerDismiss)
    }
}

@Composable
internal fun SettingsDataSection(
    selectedExportFormat: ExportFormat,
    accent: Color,
    onFormatChanged: (ExportFormat) -> Unit,
    onShowExportConfirm: () -> Unit,
    onShowEncryptedExportDialog: () -> Unit,
) {
    SectionLabel("Data")
    ExportFormatSelector(
        selected = selectedExportFormat,
        accent = accent,
        onFormatChanged = onFormatChanged,
    )
    when (selectedExportFormat) {
        ExportFormat.PLAINTEXT -> {
            PlaintextWarningBanner()
            SettingsActionRow("Export weight history", "Download as CSV") {
                onShowExportConfirm()
            }
        }

        ExportFormat.ENCRYPTED_ZIP -> {
            EncryptedExportInfoBanner()
            SettingsActionRow("Export & Encrypt", "AES-256 protected ZIP") {
                onShowEncryptedExportDialog()
            }
        }

        ExportFormat.MINIMAL_CSV -> {
            MinimalCsvWarningBanner()
            SettingsActionRow("Export Minimal CSV", "Date + weight only") {
                onShowExportConfirm()
            }
        }
    }
}

@Composable
internal fun SettingsLegalSection(
    onPrivacyClick: () -> Unit,
    onTermsClick: () -> Unit,
) {
    SectionLabel("Legal")
    SettingsActionRow("Privacy Policy", "How we handle your data") {
        onPrivacyClick()
    }
    SettingsActionRow("Terms of Service", "Usage terms") {
        onTermsClick()
    }
}
