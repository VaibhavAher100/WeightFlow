package com.weightflow.domain

/**
 * Aggregate data for the Home screen, produced by HomeDataAggregator (RFC #29).
 */
data class HomeData(
    val entries: List<WeightEntry>,
    val profile: UserProfile?,
    val unit: WeightUnit,
)
