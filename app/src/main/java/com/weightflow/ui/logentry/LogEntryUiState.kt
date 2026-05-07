package com.weightflow.ui.logentry

import com.weightflow.domain.WeightUnit
import java.time.LocalDate

data class LogEntryUiState(
    val weightInput: String = "",
    val isInputValid: Boolean = false,
    val selectedDate: LocalDate = LocalDate.now(),
    val weightUnit: WeightUnit = WeightUnit.KG,
    val isSaving: Boolean = false,
    val isSaved: Boolean = false,
    val isNewPersonalLow: Boolean = false,
    val lastLoggedWeightKg: Double? = null,
    val errorMessage: String? = null,
)

sealed class LogEntryEvent {
    data object Saved : LogEntryEvent()
    data object Dismissed : LogEntryEvent()
}
