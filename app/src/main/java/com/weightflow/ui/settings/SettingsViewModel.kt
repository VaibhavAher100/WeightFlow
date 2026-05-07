package com.weightflow.ui.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.weightflow.data.UserPrefsDataStore
import com.weightflow.data.WeightRepository
import com.weightflow.domain.CsvExporter
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class SettingsViewModel(
    private val userPrefsDataStore: UserPrefsDataStore,
    private val weightRepository: WeightRepository,
) : ViewModel() {

    val uiState: StateFlow<SettingsUiState> = combine(
        userPrefsDataStore.themePalette,
        userPrefsDataStore.weightUnit,
    ) { palette, unit -> SettingsUiState(palette, unit) }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = SettingsUiState(),
        )

    private val _events = MutableSharedFlow<SettingsEvent>()
    val events: SharedFlow<SettingsEvent> = _events.asSharedFlow()

    fun onThemeSelected(palette: String) {
        viewModelScope.launch { userPrefsDataStore.setThemePalette(palette) }
    }

    fun onUnitChanged(unit: com.weightflow.domain.WeightUnit) {
        viewModelScope.launch { userPrefsDataStore.setWeightUnit(unit) }
    }

    fun onExportCsv() {
        viewModelScope.launch {
            val entries = weightRepository.getEntriesOldestFirst().first()
            val unit = uiState.value.weightUnit
            val csv = CsvExporter.export(entries, unit)
            _events.emit(SettingsEvent.ExportCsvReady(csv))
        }
    }
}
