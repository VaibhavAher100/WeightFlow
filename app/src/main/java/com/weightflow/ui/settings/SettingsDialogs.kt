package com.weightflow.ui.settings

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.weightflow.R
import com.weightflow.domain.CsvExporter
import com.weightflow.ui.i18n.LocaleManager

// ── Password strength ─────────────────────────────────────────────────────────

internal enum class PasswordStrength { WEAK, MEDIUM, STRONG }

@Composable
internal fun PasswordStrengthIndicator(strength: PasswordStrength) {
    val (label, color) = when (strength) {
        PasswordStrength.WEAK ->
            stringResource(R.string.settings_password_strength_weak) to MaterialTheme.colorScheme.error
        PasswordStrength.MEDIUM -> stringResource(R.string.settings_password_strength_medium) to Color(0xFFFF9800)
        PasswordStrength.STRONG -> stringResource(R.string.settings_password_strength_strong) to Color(0xFF4CAF50)
    }
    Text(
        text = stringResource(R.string.settings_password_strength, label),
        fontSize = 11.sp,
        color = color,
        fontWeight = FontWeight.Medium,
    )
}

// ── Export dialogs ────────────────────────────────────────────────────────────

@Composable
internal fun PlaintextExportConfirmDialog(
    format: ExportFormat,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
) {
    val (title, body) = when (format) {
        ExportFormat.MINIMAL_CSV ->
            stringResource(R.string.settings_dialog_export_minimal_title) to
                stringResource(R.string.settings_dialog_export_minimal_body)
        else ->
            stringResource(R.string.settings_dialog_export_full_title) to
                stringResource(R.string.settings_dialog_export_full_body)
    }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title) },
        text = { Text(body, style = MaterialTheme.typography.bodyMedium) },
        confirmButton = {
            TextButton(onClick = onConfirm) { Text(stringResource(R.string.common_export)) }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text(stringResource(R.string.common_cancel)) }
        },
    )
}

/**
 * Dialog for the ENCRYPTED_ZIP flow.
 *
 * Security notes (enforced here):
 * - Password is bound to a mutable String for TextField compatibility; a CharArray copy is
 *   passed to [onExport] and immediately zeroed there. The intermediate String cannot be
 *   zeroed (JVM limitation) — documented in SECURITY.md.
 * - No clipboard shortcut is offered (Security finding #8).
 * - Password is never surfaced in SettingsUiState or emitted in any event.
 * - [onExport] receives the CharArray; the ViewModel zeros it.
 */
@Composable
internal fun EncryptedExportDialog(
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
        title = { Text(stringResource(R.string.settings_dialog_encrypt_title)) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    stringResource(R.string.settings_dialog_encrypt_info),
                    style = MaterialTheme.typography.bodySmall,
                )
                Text(
                    stringResource(R.string.settings_dialog_encrypt_mac_note),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                OutlinedTextField(
                    value = passwordString,
                    onValueChange = { passwordString = it },
                    label = {
                        Text(
                            stringResource(
                                R.string.settings_dialog_password_label,
                                CsvExporter.MIN_PASSWORD_LENGTH,
                            )
                        )
                    },
                    visualTransformation = PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                    singleLine = true,
                    isError = passwordString.isNotEmpty() && !isValid,
                    supportingText = {
                        if (passwordString.isNotEmpty() && !isValid) {
                            Text(
                                stringResource(
                                    R.string.settings_dialog_password_too_short,
                                    CsvExporter.MIN_PASSWORD_LENGTH,
                                )
                            )
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
            ) { Text(stringResource(R.string.settings_dialog_encrypt_button)) }
        },
        dismissButton = {
            TextButton(onClick = {
                passwordString = ""
                onDismiss()
            }) { Text(stringResource(R.string.common_cancel)) }
        },
    )
}

// ── Language picker dialog ────────────────────────────────────────────────────

@Suppress("FunctionNaming") // PascalCase @Composable per Compose convention
@Composable
internal fun LanguagePickerDialog(
    selected: LocaleManager.AppLanguage,
    onSelected: (LocaleManager.AppLanguage) -> Unit,
    onDismiss: () -> Unit,
) {
    val options = listOf(
        LocaleManager.AppLanguage.SYSTEM to R.string.settings_language_system,
        LocaleManager.AppLanguage.ENGLISH to R.string.settings_language_english,
        LocaleManager.AppLanguage.GERMAN to R.string.settings_language_german,
    )
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.settings_language_title)) },
        text = {
            Column {
                options.forEach { (language, labelRes) ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onSelected(language) }
                            .padding(vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        RadioButton(
                            selected = selected == language,
                            onClick = { onSelected(language) },
                        )
                        Spacer(Modifier.width(8.dp))
                        Text(
                            text = stringResource(labelRes),
                            style = MaterialTheme.typography.bodyLarge,
                        )
                    }
                }
            }
        },
        confirmButton = {},
        dismissButton = {
            TextButton(onClick = onDismiss) { Text(stringResource(R.string.common_cancel)) }
        },
    )
}
