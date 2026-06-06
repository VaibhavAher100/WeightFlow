package com.weightflow.ui.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.weightflow.data.UserPrefsDataStore
import com.weightflow.data.UserProfileRepository
import com.weightflow.data.WeightRepository
import com.weightflow.domain.Badge
import com.weightflow.domain.BadgeObserver
import com.weightflow.domain.WeightEntry
import com.weightflow.domain.WeightUnit
import com.weightflow.ui.i18n.WeightFormatter
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.filterNotNull
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

    private val strings = MutableStateFlow<ProfileStrings?>(null)

    /** Called by the UI with locale-resolved strings; updates live on locale change. */
    fun setStrings(value: ProfileStrings) { strings.value = value }

    val uiState: StateFlow<ProfileUiState> = combine(
        userProfileRepository.getProfile(),
        userPrefsDataStore.weightUnit,
        weightRepository.getEntriesNewestFirst(),
        badgeObserver.allEarnedBadges,
        strings.filterNotNull(),
    ) { profile, unit, entries, badges, s ->
        if (profile == null) return@combine ProfileUiState.NoProfile

        val newestEntry = entries.firstOrNull()
        val oldestEntry = entries.lastOrNull()

        val currentWeightDisplay = newestEntry?.let { formatWeight(it.weightKg, unit, s) }
        val startWeightDisplay = oldestEntry?.let { formatWeight(it.weightKg, unit, s) }

        val goalProgressPercent = computeGoalProgress(
            profile.goalWeightKg, oldestEntry, newestEntry,
        )

        val goalSummaryLabel = buildGoalSummaryLabel(
            oldestEntry, newestEntry, unit, goalProgressPercent, s,
        )

        val bmiDisplay = if (profile.heightCm != null && newestEntry != null) {
            val hM = profile.heightCm / 100.0
            String.format(s.locale, "%.1f", newestEntry.weightKg / (hM * hM))
        } else null

        val bmiCategoryKind = if (profile.heightCm != null && newestEntry != null) {
            val hM  = profile.heightCm / 100.0
            val bmi = newestEntry.weightKg / (hM * hM)
            when {
                bmi < 18.5 -> BmiCategoryKind.UNDERWEIGHT
                bmi < 25.0 -> BmiCategoryKind.NORMAL
                bmi < 30.0 -> BmiCategoryKind.OVERWEIGHT
                else       -> BmiCategoryKind.OBESE
            }
        } else null

        val bmiCategory = bmiCategoryKind?.let {
            when (it) {
                BmiCategoryKind.UNDERWEIGHT -> s.bmiUnderweight
                BmiCategoryKind.NORMAL      -> s.bmiNormal
                BmiCategoryKind.OVERWEIGHT  -> s.bmiOverweight
                BmiCategoryKind.OBESE       -> s.bmiObese
            }
        }

        val (bmiNormalRangeLow, bmiNormalRangeHigh) = if (profile.heightCm != null) {
            val hSq = (profile.heightCm / 100.0).let { it * it }
            Pair(18.5 * hSq, 25.0 * hSq)
        } else Pair(null, null)

        val bmiDifferenceFromNormal = if (profile.heightCm != null && newestEntry != null) {
            val hSq     = (profile.heightCm / 100.0).let { it * it }
            val bmi     = newestEntry.weightKg / hSq
            val minKg   = 18.5 * hSq
            val maxKg   = 25.0 * hSq
            when {
                bmi < 18.5 -> newestEntry.weightKg - minKg  // negative (below range)
                bmi > 25.0 -> newestEntry.weightKg - maxKg  // positive (above range)
                else       -> 0.0
            }
        } else null

        ProfileUiState.Loaded(
            displayName = profile.displayName,
            goalWeightKg = profile.goalWeightKg,
            goalWeightDisplay = profile.goalWeightKg?.let { formatWeight(it, unit, s) },
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
            bmiCategory = bmiCategory,
            bmiCategoryKind = bmiCategoryKind,
            bmiNormalRangeLow = bmiNormalRangeLow,
            bmiNormalRangeHigh = bmiNormalRangeHigh,
            bmiDifferenceFromNormal = bmiDifferenceFromNormal,
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

    fun deleteAllData() {
        viewModelScope.launch {
            weightRepository.deleteAllEntries()
            userProfileRepository.deleteProfile()
            userPrefsDataStore.clearAllPreferences()
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

    private fun formatWeight(kg: Double, unit: WeightUnit, s: ProfileStrings): String =
        WeightFormatter.format(
            kg = kg,
            unit = unit,
            locale = s.locale,
            kgSuffix = s.kgSuffix,
            lbsSuffix = s.lbsSuffix,
            stSuffix = s.stSuffix,
            lbSuffix = s.lbSuffix,
        )

    private fun buildGoalSummaryLabel(
        oldestEntry: WeightEntry?,
        newestEntry: WeightEntry?,
        unit: WeightUnit,
        progressPercent: Float?,
        s: ProfileStrings,
    ): String? {
        val lostKg = if (oldestEntry != null && newestEntry != null) {
            oldestEntry.weightKg - newestEntry.weightKg
        } else {
            0.0
        }
        return if (oldestEntry == null || newestEntry == null || lostKg <= 0.0) {
            null
        } else {
            val lostDisplay = formatWeight(lostKg, unit, s)
            val days = ChronoUnit.DAYS.between(
                Instant.ofEpochMilli(oldestEntry.timestamp).atZone(ZoneId.systemDefault()).toLocalDate(),
                Instant.ofEpochMilli(newestEntry.timestamp).atZone(ZoneId.systemDefault()).toLocalDate(),
            )
            val pct = progressPercent?.let { "${(it * 100).toInt()}%" } ?: ""
            String.format(s.locale, s.goalSummaryTemplate, lostDisplay, days, pct).trim()
        }
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
