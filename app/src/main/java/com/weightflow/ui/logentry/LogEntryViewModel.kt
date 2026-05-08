package com.weightflow.ui.logentry

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.weightflow.data.UserPrefsDataStore
import com.weightflow.data.WeightRepository
import com.weightflow.domain.WeightConverter
import com.weightflow.domain.WeightUnit
import com.weightflow.domain.isValidWeightKg
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.ZoneId

class LogEntryViewModel(
    private val weightRepository: WeightRepository,
    private val userPrefsDataStore: UserPrefsDataStore,
) : ViewModel() {

    private val _uiState = MutableStateFlow(LogEntryUiState())
    private val _events = Channel<LogEntryEvent>(Channel.BUFFERED)
    val events = _events.receiveAsFlow()

    val uiState: StateFlow<LogEntryUiState> = combine(
        _uiState,
        userPrefsDataStore.weightUnit,
        weightRepository.getEntriesNewestFirst(),
    ) { state, unit, entries ->
        val lastKg = entries.firstOrNull()?.weightKg
        state.copy(
            weightUnit = unit,
            lastLoggedWeightKg = lastKg,
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = LogEntryUiState(),
    )

    init {
        // Ensure the combined flow stays active even when not subscribed to (e.g., in tests)
        uiState.launchIn(viewModelScope)
    }

    fun onWeightInput(input: String) {
        val raw = input.toDoubleOrNull()
        // Read unit from the combined uiState so it reflects the DataStore value
        val currentUnit = uiState.value.weightUnit
        val weightKg = raw?.let { v ->
            when (currentUnit) {
                WeightUnit.KG  -> v
                WeightUnit.LBS -> WeightConverter.lbsToKg(v)
                WeightUnit.ST  -> WeightConverter.stToKg(v)
            }
        }
        val valid = weightKg?.isValidWeightKg() ?: false
        _uiState.update { it.copy(weightInput = input, isInputValid = valid) }
    }

    fun onDateSelected(date: LocalDate) {
        _uiState.update { it.copy(selectedDate = date) }
    }

    fun onSave() {
        val state = _uiState.value
        // Read unit from the combined uiState so it reflects the DataStore value
        val currentUnit = uiState.value.weightUnit
        if (!state.isInputValid || state.isSaving) return
        _uiState.update { it.copy(isSaving = true, errorMessage = null) }
        viewModelScope.launch {
            try {
                val raw = state.weightInput.toDouble()
                val weightKg = when (currentUnit) {
                    WeightUnit.KG  -> raw
                    WeightUnit.LBS -> WeightConverter.lbsToKg(raw)
                    WeightUnit.ST  -> WeightConverter.stToKg(raw)
                }
                val timestamp = state.selectedDate
                    .atStartOfDay(ZoneId.systemDefault())
                    .toInstant()
                    .toEpochMilli()
                weightRepository.addEntry(weightKg, timestamp)

                val prevMin = uiState.value.lastLoggedWeightKg
                val isNewLow = prevMin != null && weightKg < prevMin

                _uiState.update {
                    it.copy(isSaving = false, isSaved = true, isNewPersonalLow = isNewLow)
                }
                val lingerMs = if (isNewLow) 1200L else 600L
                delay(lingerMs)
                _events.send(LogEntryEvent.Saved)
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isSaving = false,
                        errorMessage = "Failed to save — please try again",
                    )
                }
            }
        }
    }

    fun onDismiss() {
        viewModelScope.launch { _events.send(LogEntryEvent.Dismissed) }
    }

    fun reset() {
        _uiState.update { LogEntryUiState() }
    }
}
