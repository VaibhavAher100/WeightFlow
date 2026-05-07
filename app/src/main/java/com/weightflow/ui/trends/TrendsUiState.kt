package com.weightflow.ui.trends

import com.weightflow.domain.WeightUnit

enum class TrendsTimeRange(val labelDays: Int?) {
    DAYS_7(7),
    DAYS_30(30),
    DAYS_90(90),
    DAYS_180(180),
    DAYS_365(365),
    ALL(null),
}

data class ChartPoint(
    val timestamp: Long,
    val displayValue: Float,
)

data class StatsSection(
    val allTimeHighDisplay: Float,
    val allTimeLowDisplay: Float,
    val allTimeAvgDisplay: Float,
    val totalEntries: Int,
    val change7DDisplay: Float?,
    val change30DDisplay: Float?,
    val avgChangePerWeekDisplay: Float,
    val avgChangePerMonthDisplay: Float,
    val estimatedDaysToGoal: Int?,
)

sealed class TrendsUiState {
    data object Loading : TrendsUiState()
    data object Empty : TrendsUiState()
    data class HasData(
        val chartPoints: List<ChartPoint>,
        val weightUnit: WeightUnit,
        val minDisplay: Float,
        val maxDisplay: Float,
        val statsSection: StatsSection? = null,
        val coachingSentence: String? = null,
    ) : TrendsUiState()
}
