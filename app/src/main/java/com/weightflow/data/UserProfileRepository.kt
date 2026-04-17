package com.weightflow.data

import com.weightflow.domain.UserProfile
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.time.LocalDate

class UserProfileRepository(private val dao: UserProfileDao) {

    fun getProfile(): Flow<UserProfile?> {
        return dao.getProfile().map { it?.toDomain() }
    }

    suspend fun saveProfile(profile: UserProfile) {
        dao.upsert(profile.toEntity())
    }

    private fun UserProfileEntity.toDomain() = UserProfile(
        id = id,
        displayName = displayName,
        goalWeightKg = goalWeightKg,
        targetDate = targetDateEpochDay?.let { LocalDate.ofEpochDay(it) },
        heightCm = heightCm,
        maintenanceMode = maintenanceMode,
        maintenanceRangeKg = maintenanceRangeKg,
        maintenanceModeActivatedAt = maintenanceModeActivatedAt,
        achievedAt = achievedAtEpochDay?.let { LocalDate.ofEpochDay(it) },
    )

    private fun UserProfile.toEntity() = UserProfileEntity(
        id = 1L,
        displayName = displayName,
        goalWeightKg = goalWeightKg,
        targetDateEpochDay = targetDate?.toEpochDay(),
        heightCm = heightCm,
        maintenanceMode = maintenanceMode,
        maintenanceRangeKg = maintenanceRangeKg,
        maintenanceModeActivatedAt = maintenanceModeActivatedAt,
        achievedAtEpochDay = achievedAt?.toEpochDay(),
    )
}
