package com.weightflow.ui.settings

import com.weightflow.domain.WeightUnit

/**
 * Three export formats available from Settings → Data.
 *
 * Note on security properties:
 * - [PLAINTEXT]      — full CSV, no encryption. All weight entries + unit.
 * - [ENCRYPTED_ZIP]  — AES-256 (WinZip AES v2) ZIP. Requires 12+ char password.
 *                      Readable by 7-Zip, WinRAR, The Unarchiver.
 *                      NOT readable by macOS Archive Utility (no AES-ZIP support).
 * - [MINIMAL_CSV]    — date + weight_kg only, no profile/goal/notes, no encryption.
 *                      Quasi-identifying: date+weight series can identify an individual
 *                      when combined with other information.
 */
enum class ExportFormat {
    PLAINTEXT,
    ENCRYPTED_ZIP,
    MINIMAL_CSV,
}

data class SettingsUiState(
    val themePalette: String = "lime",
    val weightUnit: WeightUnit = WeightUnit.KG,
    val reminderEnabled: Boolean = false,
    val selectedExportFormat: ExportFormat = ExportFormat.PLAINTEXT,
)
