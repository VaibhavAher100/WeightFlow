package com.weightflow.ui.profile

import com.weightflow.domain.Badge
import com.weightflow.domain.WeightUnit
import java.time.LocalDate

sealed class ProfileUiState {
    data object Loading : ProfileUiState()

    data object NoProfile : ProfileUiState()

    data class Loaded(
        val displayName: String,
        val goalWeightKg: Double?,
        val goalWeightDisplay: String?,
        val targetDate: LocalDate?,
        val heightCm: Double?,
        val weightUnit: WeightUnit,
        val maintenanceMode: Boolean,
        val earnedBadges: Set<Badge> = emptySet(),
        val streakDays: Int = 0,
        val currentWeightDisplay: String? = null,
        val startWeightDisplay: String? = null,
        val totalEntriesCount: Int = 0,
        val goalProgressPercent: Float? = null,
        val goalSummaryLabel: String? = null,
        val bmiDisplay: String? = null,
    ) : ProfileUiState()
}
