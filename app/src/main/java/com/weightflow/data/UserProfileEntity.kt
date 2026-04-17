package com.weightflow.data

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Single-row table — always id = 1. [targetDateEpochDay] stores LocalDate as epoch day (Long)
 * to keep the domain type out of Room. Mapping back to LocalDate is done in the repository.
 */
@Entity(tableName = "user_profile")
data class UserProfileEntity(
    @PrimaryKey val id: Long = 1L,
    val displayName: String,
    val goalWeightKg: Double?,
    val targetDateEpochDay: Long?,
    val heightCm: Double?,
    val maintenanceMode: Boolean,
    val maintenanceRangeKg: Double,
    val maintenanceModeActivatedAt: Long?,
    val achievedAtEpochDay: Long? = null,
)
