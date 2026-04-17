package com.weightflow.domain

import java.time.LocalDate

data class UserProfile(
    val id: Long,
    val displayName: String,
    val goalWeightKg: Double?,
    val targetDate: LocalDate?,
    val heightCm: Double?,
    val maintenanceMode: Boolean,
    val maintenanceRangeKg: Double,
    val maintenanceModeActivatedAt: Long?,
    val achievedAt: LocalDate? = null,
)
