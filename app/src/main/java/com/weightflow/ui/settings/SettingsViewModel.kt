package com.weightflow.ui.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.weightflow.data.UserPrefsDataStore
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class SettingsViewModel(
    private val userPrefsDataStore: UserPrefsDataStore,
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

    fun onThemeSelected(palette: String) {
        viewModelScope.launch { userPrefsDataStore.setThemePalette(palette) }
    }

    fun onUnitChanged(unit: com.weightflow.domain.WeightUnit) {
        viewModelScope.launch { userPrefsDataStore.setWeightUnit(unit) }
    }
}
