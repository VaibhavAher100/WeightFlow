package com.weightflow.ui.settings

sealed interface SettingsEvent {
    /**
     * Plaintext CSV export is ready for the system file picker.
     * [csvContent] is the raw CSV string to write to the chosen URI.
     */
    data class ExportCsvReady(val csvContent: String) : SettingsEvent

    /**
     * Encrypted ZIP export is ready for the system file picker.
     * [zipFile] is the temp file produced by [CsvExporter.exportEncryptedZip].
     * The UI must copy the bytes to the chosen URI and then delete the temp file.
     */
    data class ExportEncryptedZipReady(
        val zipFile: java.io.File,
        val suggestedFileName: String,
    ) : SettingsEvent

    /**
     * Encryption failed — password too weak, AES setup error, or CRC mismatch.
     * [reason] is a user-facing description (no stack trace, no password content).
     */
    data class ExportEncryptionFailed(val reason: String) : SettingsEvent

    /** ViewModel needs the UI to launch the POST_NOTIFICATIONS permission dialog. */
    data object RequestNotificationPermission : SettingsEvent

    /** Reminder was enabled successfully — UI should schedule the worker. */
    data object ReminderEnabled : SettingsEvent

    /** Permission was denied — UI should show the "grant in Settings" message. */
    data object NotificationPermissionDenied : SettingsEvent
}
