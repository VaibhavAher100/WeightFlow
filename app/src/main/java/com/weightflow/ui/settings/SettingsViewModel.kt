package com.weightflow.ui.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.weightflow.data.UserPrefsDataStore
import com.weightflow.data.WeightRepository
import com.weightflow.domain.CsvExporter
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.io.File
import java.time.LocalDate

class SettingsViewModel(
    private val userPrefsDataStore: UserPrefsDataStore,
    private val weightRepository: WeightRepository,
    /** Writable temp directory for encrypted ZIP files. Defaults to null — callers on Android
     *  should inject [Context.cacheDir]. Tests inject a JVM temp dir. */
    private val cacheDir: File? = null,
) : ViewModel() {

    // ── UiState ───────────────────────────────────────────────────────────────

    private val _exportFormat = MutableStateFlow(ExportFormat.PLAINTEXT)

    val uiState: StateFlow<SettingsUiState> = combine(
        userPrefsDataStore.themePalette,
        userPrefsDataStore.weightUnit,
        userPrefsDataStore.reminderEnabled,
        _exportFormat,
    ) { palette, unit, reminder, format ->
        SettingsUiState(palette, unit, reminder, format)
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = SettingsUiState(),
    )

    // ── Events ────────────────────────────────────────────────────────────────

    private val _events = MutableSharedFlow<SettingsEvent>()
    val events: SharedFlow<SettingsEvent> = _events.asSharedFlow()

    // ── Settings actions ──────────────────────────────────────────────────────

    fun onThemeSelected(palette: String) {
        viewModelScope.launch { userPrefsDataStore.setThemePalette(palette) }
    }

    fun onUnitChanged(unit: com.weightflow.domain.WeightUnit) {
        viewModelScope.launch { userPrefsDataStore.setWeightUnit(unit) }
    }

    fun onExportFormatChanged(format: ExportFormat) {
        _exportFormat.update { format }
    }

    /**
     * Called when the user taps the reminder toggle.
     *
     * Turning OFF: persists immediately — no permission needed.
     * Turning ON:  emits [SettingsEvent.RequestNotificationPermission] so the UI
     *              can launch the system permission dialog; the DataStore write is
     *              deferred until [onReminderPermissionResult] is called with the result.
     */
    fun onReminderToggled(enabled: Boolean) {
        if (!enabled) {
            viewModelScope.launch { userPrefsDataStore.setReminderEnabled(false) }
        } else {
            viewModelScope.launch {
                _events.emit(SettingsEvent.RequestNotificationPermission)
            }
        }
    }

    /**
     * Called by the UI with the result of the POST_NOTIFICATIONS permission dialog
     * (or immediately with `true` on API < 33 where no runtime permission is needed).
     *
     * Granted → persists `true` and emits [SettingsEvent.ReminderEnabled] so the UI
     *           schedules the worker.
     * Denied  → persists `false` (keeps toggle OFF) and emits
     *           [SettingsEvent.NotificationPermissionDenied].
     */
    fun onReminderPermissionResult(granted: Boolean) {
        viewModelScope.launch {
            if (granted) {
                userPrefsDataStore.setReminderEnabled(true)
                _events.emit(SettingsEvent.ReminderEnabled)
            } else {
                userPrefsDataStore.setReminderEnabled(false)
                _events.emit(SettingsEvent.NotificationPermissionDenied)
            }
        }
    }

    // ── Export actions ────────────────────────────────────────────────────────

    /**
     * Plaintext CSV export — dispatches [SettingsEvent.ExportCsvReady] with the raw CSV string.
     * Used for [ExportFormat.PLAINTEXT] and [ExportFormat.MINIMAL_CSV] (file picker handles the
     * write; no temp file needed).
     */
    fun onExportCsv() {
        viewModelScope.launch {
            val entries = weightRepository.getEntriesOldestFirst().first()
            val unit    = uiState.value.weightUnit
            val csv     = when (uiState.value.selectedExportFormat) {
                ExportFormat.MINIMAL_CSV -> CsvExporter.exportMinimalCsv(entries)
                else                     -> CsvExporter.export(entries, unit)
            }
            _events.emit(SettingsEvent.ExportCsvReady(csv))
        }
    }

    /**
     * Encrypted ZIP export.
     *
     * Security contract:
     * - [password] is a [CharArray]. This method zeros it in a `finally` block after
     *   forwarding to [CsvExporter.exportEncryptedZip] — the array is unusable after return.
     * - Password is never stored in DataStore, never held in UiState, never logged.
     * - Minimum [CsvExporter.MIN_PASSWORD_LENGTH] characters required.
     * - On success, emits [SettingsEvent.ExportEncryptedZipReady] with the temp [File].
     * - On failure, emits [SettingsEvent.ExportEncryptionFailed] with a safe reason string.
     *
     * The temp file produced lives in [cacheDir] and must be deleted by the UI after copying
     * bytes to the chosen URI.
     *
     * @param password caller-owned CharArray — **will be zeroed** by this call, regardless
     *                 of success or failure.
     */
    fun onExportEncryptedZip(password: CharArray) {
        viewModelScope.launch {
            try {
                // Validate before any IO.
                if (password.size < CsvExporter.MIN_PASSWORD_LENGTH) {
                    _events.emit(
                        SettingsEvent.ExportEncryptionFailed(
                            "Password must be at least ${CsvExporter.MIN_PASSWORD_LENGTH} characters."
                        )
                    )
                    return@launch
                }

                val dir = cacheDir
                    ?: run {
                        _events.emit(
                            SettingsEvent.ExportEncryptionFailed(
                                "Export directory not available. Please try again."
                            )
                        )
                        return@launch
                    }

                val entries      = weightRepository.getEntriesOldestFirst().first()
                val unit         = uiState.value.weightUnit
                val dateSuffix   = LocalDate.now().toString()
                val suggestedName = "weightflow_export_$dateSuffix.zip"

                // password is zeroed inside exportEncryptedZip — do not access after this call.
                val result = CsvExporter.exportEncryptedZip(
                    entries    = entries,
                    unit       = unit,
                    password   = password,
                    outputDir  = dir,
                    dateSuffix = dateSuffix,
                )

                result.fold(
                    onSuccess = { zipFile ->
                        _events.emit(
                            SettingsEvent.ExportEncryptedZipReady(zipFile, suggestedName)
                        )
                    },
                    onFailure = { ex ->
                        _events.emit(
                            SettingsEvent.ExportEncryptionFailed(
                                ex.message ?: "Encryption failed. Please try again."
                            )
                        )
                    },
                )
            } finally {
                // Belt-and-suspenders: zero again if exportEncryptedZip threw before its own zero.
                password.fill(' ')
            }
        }
    }
}
