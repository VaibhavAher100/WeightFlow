package com.weightflow.ui.logentry

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.weightflow.data.UserPrefsDataStore
import com.weightflow.data.WeightRepository
import com.weightflow.domain.WeightConverter
import com.weightflow.domain.WeightUnit
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.ZoneId

class LogEntryViewModel(
    private val weightRepository: WeightRepository,
    private val userPrefsDataStore: UserPrefsDataStore,
) : ViewModel() {

    private val _uiState = MutableStateFlow(LogEntryUiState())
    val uiState: StateFlow<LogEntryUiState> = _uiState.asStateFlow()

    private val _events = MutableSharedFlow<LogEntryEvent>()
    val events: SharedFlow<LogEntryEvent> = _events.asSharedFlow()

    init {
        viewModelScope.launch {
            userPrefsDataStore.weightUnit.collect { unit ->
                _uiState.update { it.copy(weightUnit = unit) }
            }
        }
    }

    fun onWeightInput(input: String) {
        val valid = input.toDoubleOrNull()?.let { it > 0.0 } ?: false
        _uiState.update { it.copy(weightInput = input, isInputValid = valid) }
    }

    fun onDateSelected(date: LocalDate) {
        _uiState.update { it.copy(selectedDate = date) }
    }

    fun onSave() {
        val state = _uiState.value
        if (!state.isInputValid) return

        val inputValue = state.weightInput.toDoubleOrNull() ?: return
        val weightKg = when (state.weightUnit) {
            WeightUnit.KG -> inputValue
            WeightUnit.LBS -> WeightConverter.lbsToKg(inputValue)
            WeightUnit.ST -> {
                // Input in stones (decimal) — treat as kg fallback; full st/lb input handled in Phase 3
                inputValue
            }
        }
        val timestamp = state.selectedDate
            .atStartOfDay(ZoneId.systemDefault())
            .toInstant()
            .toEpochMilli()

        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true, errorMessage = null) }
            try {
                weightRepository.addEntry(weightKg = weightKg, timestamp = timestamp)
                _uiState.update { it.copy(isSaving = false) }
                _events.emit(LogEntryEvent.Saved)
            } catch (e: Exception) {
                _uiState.update { it.copy(isSaving = false, errorMessage = "Failed to save — please try again") }
            }
        }
    }

    fun onDismiss() {
        viewModelScope.launch {
            _events.emit(LogEntryEvent.Dismissed)
        }
    }
}
