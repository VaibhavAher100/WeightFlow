package com.weightflow.ui.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.weightflow.data.UserPrefsDataStore
import com.weightflow.data.UserProfileRepository
import com.weightflow.domain.WeightConverter
import com.weightflow.domain.WeightUnit
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class ProfileViewModel(
    private val userProfileRepository: UserProfileRepository,
    private val userPrefsDataStore: UserPrefsDataStore,
) : ViewModel() {

    val uiState: StateFlow<ProfileUiState> = combine(
        userProfileRepository.getProfile(),
        userPrefsDataStore.weightUnit,
    ) { profile, unit ->
        if (profile == null) return@combine ProfileUiState.NoProfile

        ProfileUiState.Loaded(
            displayName = profile.displayName,
            goalWeightKg = profile.goalWeightKg,
            goalWeightDisplay = profile.goalWeightKg?.let { WeightConverter.format(it, unit) },
            targetDate = profile.targetDate,
            heightCm = profile.heightCm,
            weightUnit = unit,
            maintenanceMode = profile.maintenanceMode,
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = ProfileUiState.Loading,
    )

    fun onUnitChanged(unit: WeightUnit) {
        viewModelScope.launch {
            userPrefsDataStore.setWeightUnit(unit)
        }
    }
}
