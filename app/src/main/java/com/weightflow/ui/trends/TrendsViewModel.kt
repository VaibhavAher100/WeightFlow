package com.weightflow.ui.trends

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.weightflow.data.UserPrefsDataStore
import com.weightflow.data.UserProfileRepository
import com.weightflow.data.WeightRepository
import com.weightflow.domain.WeightConverter
import com.weightflow.domain.WeightEntry
import com.weightflow.domain.WeightUnit
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import java.time.LocalDate
import java.time.ZoneId

class TrendsViewModel(
    private val weightRepository: WeightRepository,
    private val userPrefsDataStore: UserPrefsDataStore,
    private val userProfileRepository: UserProfileRepository,
) : ViewModel() {

    private val _selectedRange = MutableStateFlow(TrendsTimeRange.DAYS_30)
    val selectedRange: StateFlow<TrendsTimeRange> = _selectedRange.asStateFlow()

    private val _selectedChart = MutableStateFlow(ChartType.LINE)
    val selectedChart: StateFlow<ChartType> = _selectedChart.asStateFlow()

    val uiState: StateFlow<TrendsUiState> = combine(
        weightRepository.getEntriesOldestFirst(),
        userPrefsDataStore.weightUnit,
        _selectedRange,
        userProfileRepository.getProfile(),
    ) { entries, unit, range, profile ->
        if (entries.isEmpty()) return@combine TrendsUiState.Empty

        val filtered = filterByRange(entries, range)
        if (filtered.isEmpty()) return@combine TrendsUiState.Empty

        val points = filtered.map { entry ->
            ChartPoint(
                timestamp = entry.timestamp,
                displayValue = toDisplayValue(entry.weightKg, unit),
            )
        }

        val stats = computeStatsSection(entries, unit, profile?.goalWeightKg)
        TrendsUiState.HasData(
            chartPoints = points,
            weightUnit = unit,
            minDisplay = points.minOf { it.displayValue },
            maxDisplay = points.maxOf { it.displayValue },
            statsSection = stats,
            coachingSentence = buildCoachingSentence(
                goalWeightKg = profile?.goalWeightKg,
                etaDays = stats.estimatedDaysToGoal,
                unit = unit,
            ),
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = TrendsUiState.Loading,
    )

    fun onRangeSelected(range: TrendsTimeRange) {
        _selectedRange.value = range
    }

    fun onChartTypeSelected(type: ChartType) {
        _selectedChart.value = type
    }

    private fun filterByRange(
        entries: List<WeightEntry>,
        range: TrendsTimeRange,
    ): List<WeightEntry> {
        val days = range.labelDays ?: return entries
        val cutoff = LocalDate.now()
            .minusDays(days.toLong())
            .atStartOfDay(ZoneId.systemDefault())
            .toInstant()
            .toEpochMilli()
        return entries.filter { it.timestamp >= cutoff }
    }

    private fun toDisplayValue(kg: Double, unit: WeightUnit): Float = when (unit) {
        WeightUnit.KG  -> kg.toFloat()
        WeightUnit.LBS -> WeightConverter.kgToLbs(kg).toFloat()
        WeightUnit.ST  -> WeightConverter.kgToLbs(kg).toFloat()
    }

    private fun toDisplayRate(kgPerWeek: Double, unit: WeightUnit): Float = when (unit) {
        WeightUnit.KG  -> kgPerWeek.toFloat()
        WeightUnit.LBS -> (kgPerWeek * 2.20462).toFloat()
        WeightUnit.ST  -> (kgPerWeek * 2.20462).toFloat()
    }

    private fun computeStatsSection(
        allEntries: List<WeightEntry>,
        unit: WeightUnit,
        goalWeightKg: Double?,
    ): StatsSection {
        val allDisplay = allEntries.map { toDisplayValue(it.weightKg, unit) }

        val now = System.currentTimeMillis()
        val ms7D  = 7L  * 86_400_000L
        val ms30D = 30L * 86_400_000L

        val entries7D  = allEntries.filter { it.timestamp >= now - ms7D }
        val entries30D = allEntries.filter { it.timestamp >= now - ms30D }

        val change7D = if (entries7D.size >= 2)
            toDisplayValue(entries7D.last().weightKg, unit) - toDisplayValue(entries7D.first().weightKg, unit)
        else null

        val change30D = if (entries30D.size >= 2)
            toDisplayValue(entries30D.last().weightKg, unit) - toDisplayValue(entries30D.first().weightKg, unit)
        else null

        val weeklyRateKg = if (allEntries.size >= 2) {
            val spanMs = allEntries.last().timestamp - allEntries.first().timestamp
            val weeks  = spanMs.toDouble() / ms7D.toDouble()
            if (weeks < 1.0) 0.0
            else (allEntries.last().weightKg - allEntries.first().weightKg) / weeks
        } else 0.0

        val weeklyRateDisplay  = toDisplayRate(weeklyRateKg, unit)
        val monthlyRateDisplay = weeklyRateDisplay * 4.33f

        val etaDays = if (goalWeightKg != null && weeklyRateKg < 0.0) {
            val currentKg   = allEntries.last().weightKg
            val remainingKg = currentKg - goalWeightKg
            if (remainingKg > 0.0) {
                val rawDays = (remainingKg / (-weeklyRateKg)) * 7.0
                if (rawDays.isFinite() && rawDays < Int.MAX_VALUE.toDouble()) rawDays.toInt() else null
            } else null
        } else null

        return StatsSection(
            allTimeHighDisplay      = allDisplay.max(),
            allTimeLowDisplay       = allDisplay.min(),
            allTimeAvgDisplay       = allDisplay.average().toFloat(),
            totalEntries            = allEntries.size,
            change7DDisplay         = change7D,
            change30DDisplay        = change30D,
            avgChangePerWeekDisplay = weeklyRateDisplay,
            avgChangePerMonthDisplay = monthlyRateDisplay,
            estimatedDaysToGoal     = etaDays,
        )
    }

    private fun buildCoachingSentence(
        goalWeightKg: Double?,
        etaDays: Int?,
        unit: WeightUnit,
    ): String? {
        if (goalWeightKg == null || etaDays == null) return null
        val goalDisplay = when (unit) {
            WeightUnit.KG  -> "%.1f kg".format(goalWeightKg)
            WeightUnit.LBS -> "%.1f lbs".format(WeightConverter.kgToLbs(goalWeightKg))
            WeightUnit.ST  -> "%.1f kg".format(goalWeightKg)
        }
        return if (etaDays < 14) "You're almost there — goal $goalDisplay is within reach."
        else "At this rate you'll reach $goalDisplay in about ${etaDays / 7} weeks."
    }
}
