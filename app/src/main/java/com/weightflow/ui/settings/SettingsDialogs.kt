package com.weightflow.ui.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.weightflow.domain.CsvExporter

// ── Password strength ─────────────────────────────────────────────────────────

internal enum class PasswordStrength { WEAK, MEDIUM, STRONG }

@Composable
internal fun PasswordStrengthIndicator(strength: PasswordStrength) {
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

// ── Export dialogs ────────────────────────────────────────────────────────────

@Composable
internal fun PlaintextExportConfirmDialog(
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
