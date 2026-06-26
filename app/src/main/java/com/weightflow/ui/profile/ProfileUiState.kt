package com.weightflow.ui.profile

import com.weightflow.domain.Badge
import com.weightflow.domain.WeightUnit
import java.time.LocalDate

/** Stable, locale-independent BMI bucket used by the UI for colour decisions. */
enum class BmiCategoryKind { UNDERWEIGHT, NORMAL, OVERWEIGHT, OBESE }

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
        val bmiCategory: String? = null,
        val bmiCategoryKind: BmiCategoryKind? = null,
        val bmiNormalRangeLow: Double? = null,
        val bmiNormalRangeHigh: Double? = null,
        val bmiDifferenceFromNormal: Double? = null,
        val etaDays: Int? = null,
    ) : ProfileUiState()
}
