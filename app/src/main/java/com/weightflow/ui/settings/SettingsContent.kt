package com.weightflow.ui.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.weightflow.R
import com.weightflow.domain.WeightUnit
import com.weightflow.ui.i18n.LocaleManager

// ── Section wrappers (called from SettingsScreen) ─────────────────────────────

@Composable
internal fun SettingsUnitSection(
    selectedUnit: WeightUnit,
    accent: Color,
    onUnitChanged: (WeightUnit) -> Unit,
) {
    SectionLabel(stringResource(R.string.settings_unit_section))
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
    SectionLabel(stringResource(R.string.settings_theme_section))
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
    SectionLabel(stringResource(R.string.settings_notifications_section))
    SettingsToggleRow(
        title = stringResource(R.string.settings_notifications_reminder_title),
        subtitle = stringResource(R.string.settings_notifications_reminder_subtitle),
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
    SectionLabel(stringResource(R.string.settings_data_section))
    ExportFormatSelector(
        selected = selectedExportFormat,
        accent = accent,
        onFormatChanged = onFormatChanged,
    )
    when (selectedExportFormat) {
        ExportFormat.PLAINTEXT -> {
            PlaintextWarningBanner()
            SettingsActionRow(
                stringResource(R.string.settings_data_export_history_title),
                stringResource(R.string.settings_data_export_csv_subtitle),
            ) {
                onShowExportConfirm()
            }
        }

        ExportFormat.ENCRYPTED_ZIP -> {
            EncryptedExportInfoBanner()
            SettingsActionRow(
                stringResource(R.string.settings_data_export_encrypt_title),
                stringResource(R.string.settings_data_export_encrypt_subtitle),
            ) {
                onShowEncryptedExportDialog()
            }
        }

        ExportFormat.MINIMAL_CSV -> {
            MinimalCsvWarningBanner()
            SettingsActionRow(
                stringResource(R.string.settings_data_export_minimal_title),
                stringResource(R.string.settings_data_export_minimal_subtitle),
            ) {
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
    SectionLabel(stringResource(R.string.settings_legal_section))
    SettingsActionRow(
        stringResource(R.string.settings_legal_privacy_title),
        stringResource(R.string.settings_legal_privacy_subtitle),
    ) {
        onPrivacyClick()
    }
    SettingsActionRow(
        stringResource(R.string.settings_legal_terms_title),
        stringResource(R.string.settings_legal_terms_subtitle),
    ) {
        onTermsClick()
    }
}

// ── Language section + picker ─────────────────────────────────────────────────

@Composable
internal fun languageLabelRes(language: LocaleManager.AppLanguage): Int = when (language) {
    LocaleManager.AppLanguage.SYSTEM  -> R.string.settings_language_system
    LocaleManager.AppLanguage.ENGLISH -> R.string.settings_language_english
    LocaleManager.AppLanguage.GERMAN  -> R.string.settings_language_german
}

@Composable
internal fun SettingsLanguageSection() {
    var showDialog by remember { mutableStateOf(false) }
    var selected by remember { mutableStateOf(LocaleManager.currentLanguage()) }

    SectionLabel(stringResource(R.string.settings_language_title))
    SettingsActionRow(
        title = stringResource(R.string.settings_language_title),
        subtitle = stringResource(languageLabelRes(selected)),
    ) {
        showDialog = true
    }

    if (showDialog) {
        LanguagePickerDialog(
            selected = selected,
            onSelected = { choice ->
                selected = choice
                showDialog = false
                LocaleManager.setLanguage(choice)
            },
            onDismiss = { showDialog = false },
        )
    }
}
