package com.weightflow.ui.home

import com.weightflow.domain.WeightUnit

sealed class HomeUiState {

    data object Loading : HomeUiState()

    data class Empty(
        val goalWeightDisplay: String?,
    ) : HomeUiState()

    data class HasData(
        val latestWeightDisplay: String,
        val weightUnit: WeightUnit,
        val recentEntries: List<RecentEntryDisplay>,
        val goalWeightDisplay: String?,
    ) : HomeUiState()
}

data class RecentEntryDisplay(
    val id: Long,
    val weightDisplay: String,
    val dateDisplay: String,
    val timestamp: Long,
)
