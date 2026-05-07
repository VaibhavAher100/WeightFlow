package com.weightflow.ui.onboarding

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.weightflow.data.UserPrefsDataStore
import com.weightflow.data.UserProfileRepository
import com.weightflow.domain.UserProfile
import com.weightflow.domain.WeightConverter
import com.weightflow.domain.WeightUnit
import com.weightflow.domain.isValidWeightKg
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDate

class OnboardingViewModel(
    private val userProfileRepository: UserProfileRepository,
    private val userPrefsDataStore: UserPrefsDataStore,
) : ViewModel() {

    private val _uiState = MutableStateFlow(OnboardingUiState())
    val uiState: StateFlow<OnboardingUiState> = _uiState.asStateFlow()

    private val _events = MutableSharedFlow<OnboardingEvent>()
    val events: SharedFlow<OnboardingEvent> = _events.asSharedFlow()

    fun onBirthYearInput(year: String) {
        val currentYear = LocalDate.now().year
        val birthYear = if (year.length == 4) year.toIntOrNull() else null
        val isOldEnough = birthYear != null && birthYear in 1900..(currentYear - 13)
        _uiState.update { it.copy(birthYearInput = year, canAdvance = isOldEnough) }
    }

    fun onUnitSelected(unit: WeightUnit) {
        _uiState.update { it.copy(selectedUnit = unit) }
    }

    fun onWeightInput(input: String) {
        val raw = input.toDoubleOrNull()
        val weightKg = raw?.let { v ->
            when (_uiState.value.selectedUnit) {
                WeightUnit.KG  -> v
                WeightUnit.LBS -> WeightConverter.lbsToKg(v)
                WeightUnit.ST  -> v
            }
        }
        val valid = weightKg?.isValidWeightKg() ?: false
        _uiState.update { state ->
            val canAdvance = when (state.currentStep) {
                OnboardingStep.CURRENT_WEIGHT -> valid
                else -> state.canAdvance
            }
            state.copy(weightInput = input, canAdvance = canAdvance)
        }
    }

    fun onGoalInput(input: String) {
        val valid = if (input.isBlank()) {
            true
        } else {
            val raw = input.toDoubleOrNull()
            val goalKg = raw?.let { v ->
                when (_uiState.value.selectedUnit) {
                    WeightUnit.KG  -> v
                    WeightUnit.LBS -> WeightConverter.lbsToKg(v)
                    WeightUnit.ST  -> v
                }
            }
            goalKg?.isValidWeightKg() ?: false
        }
        _uiState.update { it.copy(goalInput = input, canAdvance = valid) }
    }

    fun onNextStep() {
        val state = _uiState.value
        val steps = OnboardingStep.entries
        val currentIndex = steps.indexOf(state.currentStep)

        when (state.currentStep) {
            OnboardingStep.AGE_GATE -> {
                val currentYear = LocalDate.now().year
                val birthYear = if (state.birthYearInput.length == 4) state.birthYearInput.toIntOrNull() else null
                if (birthYear == null || birthYear !in 1900..(currentYear - 13)) {
                    viewModelScope.launch { _events.emit(OnboardingEvent.AgeDeclined) }
                    return
                }
                _uiState.update { it.copy(currentStep = OnboardingStep.UNIT, canAdvance = true) }
            }
            OnboardingStep.UNIT -> {
                _uiState.update {
                    it.copy(
                        currentStep = OnboardingStep.CURRENT_WEIGHT,
                        canAdvance = it.weightInput.toDoubleOrNull()?.let { v -> v > 0 } ?: false,
                    )
                }
            }
            OnboardingStep.CURRENT_WEIGHT -> {
                if (!state.canAdvance) return
                _uiState.update { it.copy(currentStep = OnboardingStep.GOAL, canAdvance = true) }
            }
            OnboardingStep.GOAL -> {
                // Use onComplete for final step
            }
        }
    }

    fun onBack() {
        val steps = OnboardingStep.entries
        val currentIndex = steps.indexOf(_uiState.value.currentStep)
        if (currentIndex == 0) return
        _uiState.update { it.copy(currentStep = steps[currentIndex - 1]) }
    }

    fun onComplete() {
        val state = _uiState.value
        viewModelScope.launch {
            userPrefsDataStore.setWeightUnit(state.selectedUnit)

            val weightKg = state.weightInput.toDoubleOrNull()?.let { input ->
                when (state.selectedUnit) {
                    WeightUnit.KG  -> input
                    WeightUnit.LBS -> WeightConverter.lbsToKg(input)
                    WeightUnit.ST  -> input
                }
            } ?: 0.0
            if (!weightKg.isValidWeightKg()) return@launch

            val goalKg = state.goalInput.takeIf { it.isNotBlank() }?.toDoubleOrNull()?.let { input ->
                val kg = when (state.selectedUnit) {
                    WeightUnit.KG  -> input
                    WeightUnit.LBS -> WeightConverter.lbsToKg(input)
                    WeightUnit.ST  -> input
                }
                kg.takeIf { it.isValidWeightKg() }
            }

            userProfileRepository.saveProfile(
                UserProfile(
                    id = 1,
                    displayName = "",
                    goalWeightKg = goalKg,
                    targetDate = null,
                    heightCm = null,
                    maintenanceMode = false,
                    maintenanceRangeKg = 2.0,
                    maintenanceModeActivatedAt = null,
                ),
            )

            userPrefsDataStore.setOnboardingComplete()
            _events.emit(OnboardingEvent.Finished)
        }
    }
}
