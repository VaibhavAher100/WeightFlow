package com.weightflow.ui.settings

sealed interface SettingsEvent {
    data class ExportCsvReady(val csvContent: String) : SettingsEvent
}
