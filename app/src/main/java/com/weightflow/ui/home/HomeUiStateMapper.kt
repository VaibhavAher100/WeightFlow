package com.weightflow.ui.home

import com.weightflow.domain.GoalState
import com.weightflow.domain.GoalStateMachine
import com.weightflow.domain.HomeData
import com.weightflow.domain.WeightEntry
import com.weightflow.domain.WeightUnit
import com.weightflow.ui.i18n.DateFormatters
import com.weightflow.ui.i18n.WeightFormatter
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId

object HomeUiStateMapper {

    private const val MAX_RECENT_ENTRIES = 5

    fun map(data: HomeData, strings: HomeStrings): HomeUiState {
        val goalDisplay = data.profile?.goalWeightKg?.let { formatWeight(it, data.unit, strings) }

        if (data.entries.isEmpty()) return HomeUiState.Empty(goalWeightDisplay = goalDisplay)

        val latest = data.entries.first()
        val previous = data.entries.getOrNull(1)

        val deltaKg = previous?.let { latest.weightKg - it.weightKg }
        val deltaDisplay = deltaKg?.let { formatDelta(it, data.unit, strings) }
        val deltaIsDown = deltaKg?.let { it < 0 }

        val recentEntries = data.entries.take(MAX_RECENT_ENTRIES).mapIndexed { i, entry ->
            val prev = data.entries.getOrNull(i + 1)
            val entryDelta = prev?.let { entry.weightKg - it.weightKg }
            RecentEntryDisplay(
                id = entry.id,
                weightDisplay = formatWeight(entry.weightKg, data.unit, strings),
                dateDisplay = formatDate(entry.timestamp, strings),
                timestamp = entry.timestamp,
                deltaDisplay = entryDelta?.let { formatDelta(it, data.unit, strings) },
                deltaIsDown = entryDelta?.let { it < 0 },
            )
        }

        val streakDays = computeStreak(data.entries)

        val cutoff = System.currentTimeMillis() - 7L * 24 * 60 * 60 * 1000
        val recentForAvg = data.entries.filter { it.timestamp >= cutoff }
        val avgDisplay = if (recentForAvg.size >= 2) {
            formatWeight(recentForAvg.map { it.weightKg }.average(), data.unit, strings)
        } else null

        val goalProgress = data.profile?.goalWeightKg?.let { goal ->
            if (data.entries.size < 2) null
            else {
                val start = data.entries.last().weightKg
                val current = latest.weightKg
                if (start == goal) 1f
                else ((start - current) / (start - goal)).toFloat().coerceIn(0f, 1f)
            }
        }

        val goalState = if (data.profile != null) GoalStateMachine.rehydrate(data.profile)
        else GoalState.NoGoal

        val entries = data.entries
        val startDisplay = if (entries.size >= 2) {
            formatWeight(entries.last().weightKg, data.unit, strings)
        } else null
        val lostDisplay = if (entries.size >= 2) {
            val startKg = entries.last().weightKg
            val currentKg = entries.first().weightKg
            val diff = startKg - currentKg
            if (diff > 0) "−${oneDecimal(diff, strings)}" else "+${oneDecimal(-diff, strings)}"
        } else null
        val isGoalAchieved = goalState is GoalState.Active &&
            entries.isNotEmpty() &&
            entries.first().weightKg <= goalState.goalWeightKg
        val sparklinePoints = entries
            .takeLast(30)
            .map { it.weightKg.toFloat() }
            .reversed()

        return HomeUiState.HasData(
            latestWeightDisplay = formatWeight(latest.weightKg, data.unit, strings),
            weightUnit = data.unit,
            recentEntries = recentEntries,
            goalWeightDisplay = goalDisplay,
            goalState = goalState,
            deltaDisplay = deltaDisplay,
            deltaIsDown = deltaIsDown,
            streakDays = streakDays,
            avgDisplay = avgDisplay,
            goalProgress = goalProgress,
            startDisplay = startDisplay,
            lostDisplay = lostDisplay,
            isGoalAchieved = isGoalAchieved,
            sparklinePoints = sparklinePoints,
        )
    }

    private fun formatWeight(kg: Double, unit: WeightUnit, strings: HomeStrings): String =
        WeightFormatter.format(
            kg = kg,
            unit = unit,
            locale = strings.locale,
            kgSuffix = strings.kgSuffix,
            lbsSuffix = strings.lbsSuffix,
            stSuffix = strings.stSuffix,
            lbSuffix = strings.lbSuffix,
        )

    private fun formatDelta(deltaKg: Double, unit: WeightUnit, strings: HomeStrings): String {
        val sign = if (deltaKg < 0) "−" else "+"
        return "$sign${formatWeight(Math.abs(deltaKg), unit, strings)}"
    }

    private fun oneDecimal(value: Double, strings: HomeStrings): String =
        String.format(strings.locale, "%.1f", value)

    private fun computeStreak(entries: List<WeightEntry>): Int {
        if (entries.isEmpty()) return 0
        val days = entries.map {
            Instant.ofEpochMilli(it.timestamp).atZone(ZoneId.systemDefault()).toLocalDate()
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

    private fun formatDate(timestamp: Long, strings: HomeStrings): String {
        val date = Instant.ofEpochMilli(timestamp).atZone(ZoneId.systemDefault()).toLocalDate()
        return when (DateFormatters.relativeDay(date)) {
            DateFormatters.RelativeDay.TODAY -> strings.today
            DateFormatters.RelativeDay.YESTERDAY -> strings.yesterday
            DateFormatters.RelativeDay.OTHER -> date.format(DateFormatters.dayMonth(strings.locale))
        }
    }
}
