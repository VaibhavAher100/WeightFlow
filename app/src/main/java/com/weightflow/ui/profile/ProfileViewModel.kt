package com.weightflow.ui.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.weightflow.data.UserPrefsDataStore
import com.weightflow.data.UserProfileRepository
import com.weightflow.data.WeightRepository
import com.weightflow.domain.Badge
import com.weightflow.domain.BadgeObserver
import com.weightflow.domain.WeightConverter
import com.weightflow.domain.WeightEntry
import com.weightflow.domain.WeightUnit
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.temporal.ChronoUnit

class ProfileViewModel(
    private val userProfileRepository: UserProfileRepository,
    private val userPrefsDataStore: UserPrefsDataStore,
    private val weightRepository: WeightRepository,
    private val badgeObserver: BadgeObserver,
) : ViewModel() {

    val uiState: StateFlow<ProfileUiState> = combine(
        userProfileRepository.getProfile(),
        userPrefsDataStore.weightUnit,
        weightRepository.getEntriesNewestFirst(),
        badgeObserver.allEarnedBadges,
    ) { profile, unit, entries, badges ->
        if (profile == null) return@combine ProfileUiState.NoProfile

        val newestEntry = entries.firstOrNull()
        val oldestEntry = entries.lastOrNull()

        val currentWeightDisplay = newestEntry?.let { WeightConverter.format(it.weightKg, unit) }
        val startWeightDisplay = oldestEntry?.let { WeightConverter.format(it.weightKg, unit) }

        val goalProgressPercent = computeGoalProgress(
            profile.goalWeightKg, oldestEntry, newestEntry,
        )

        val goalSummaryLabel = buildGoalSummaryLabel(
            oldestEntry, newestEntry, unit, goalProgressPercent,
        )

        val bmiDisplay = if (profile.heightCm != null && newestEntry != null) {
            val hM = profile.heightCm / 100.0
            "%.1f".format(newestEntry.weightKg / (hM * hM))
        } else null

        ProfileUiState.Loaded(
            displayName = profile.displayName,
            goalWeightKg = profile.goalWeightKg,
            goalWeightDisplay = profile.goalWeightKg?.let { WeightConverter.format(it, unit) },
            targetDate = profile.targetDate,
            heightCm = profile.heightCm,
            weightUnit = unit,
            maintenanceMode = profile.maintenanceMode,
            earnedBadges = badges,
            streakDays = computeStreak(entries),
            currentWeightDisplay = currentWeightDisplay,
            startWeightDisplay = startWeightDisplay,
            totalEntriesCount = entries.size,
            goalProgressPercent = goalProgressPercent,
            goalSummaryLabel = goalSummaryLabel,
            bmiDisplay = bmiDisplay,
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = ProfileUiState.Loading,
    )

    fun onUnitChanged(unit: WeightUnit) {
        viewModelScope.launch {
            userPrefsDataStore.setWeightUnit(unit)
        }
    }

    private fun computeGoalProgress(
        goalKg: Double?,
        oldestEntry: WeightEntry?,
        newestEntry: WeightEntry?,
    ): Float? {
        if (goalKg == null || oldestEntry == null || newestEntry == null) return null
        val total = oldestEntry.weightKg - goalKg
        if (total == 0.0) return null
        return ((oldestEntry.weightKg - newestEntry.weightKg) / total).toFloat().coerceIn(0f, 1f)
    }

    private fun buildGoalSummaryLabel(
        oldestEntry: WeightEntry?,
        newestEntry: WeightEntry?,
        unit: WeightUnit,
        progressPercent: Float?,
    ): String? {
        if (oldestEntry == null || newestEntry == null) return null
        val lostKg = oldestEntry.weightKg - newestEntry.weightKg
        if (lostKg <= 0.0) return null
        val lostDisplay = WeightConverter.format(lostKg, unit)
        val days = ChronoUnit.DAYS.between(
            Instant.ofEpochMilli(oldestEntry.timestamp).atZone(ZoneId.systemDefault()).toLocalDate(),
            Instant.ofEpochMilli(newestEntry.timestamp).atZone(ZoneId.systemDefault()).toLocalDate(),
        )
        val pct = progressPercent?.let { "${(it * 100).toInt()}%" } ?: ""
        return "−$lostDisplay over $days days  $pct".trim()
    }

    private fun computeStreak(entries: List<WeightEntry>): Int {
        if (entries.isEmpty()) return 0
        val zone = ZoneId.systemDefault()
        val days = entries.map {
            Instant.ofEpochMilli(it.timestamp).atZone(zone).toLocalDate()
        }.toSet()
        var streak = 0
        var current = LocalDate.now()
        if (!days.contains(current)) current = current.minusDays(1)
        while (days.contains(current)) {
            streak++
            current = current.minusDays(1)
        }
        return streak
    }
}
