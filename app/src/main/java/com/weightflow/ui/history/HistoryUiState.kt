package com.weightflow.ui.history

import com.weightflow.domain.WeightUnit

data class HistoryEntryDisplay(
    val id: Long,
    val weightKg: Double,
    val weightDisplay: String,
    val dateDisplay: String,
    val timestamp: Long,
    val deltaDisplay: String? = null,
    val deltaIsDown: Boolean? = null,
)

sealed class HistoryUiState {
    data object Loading : HistoryUiState()
    data object Empty : HistoryUiState()
    data class HasData(
        val entries: List<HistoryEntryDisplay>,
        val weightUnit: WeightUnit,
    ) : HistoryUiState()
}
