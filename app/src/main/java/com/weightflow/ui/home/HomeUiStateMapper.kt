package com.weightflow.ui.home

import com.weightflow.domain.GoalState
import com.weightflow.domain.GoalStateMachine
import com.weightflow.domain.HomeData
import com.weightflow.domain.WeightEntry
import com.weightflow.domain.WeightUnit
import com.weightflow.domain.computeStreak
import com.weightflow.ui.i18n.DateFormatters
import com.weightflow.ui.i18n.WeightFormatter
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId

// Cohesive mapper decomposed into small, single-purpose private helpers.
@Suppress("TooManyFunctions")
object HomeUiStateMapper {

    private const val MAX_RECENT_ENTRIES = 5
    private const val SPARKLINE_POINTS = 30
    private const val AVG_WINDOW_MILLIS = 7L * 24 * 60 * 60 * 1000

    fun map(data: HomeData, strings: HomeStrings): HomeUiState {
        val goalDisplay = data.profile?.goalWeightKg?.let { formatWeight(it, data.unit, strings) }
        return if (data.entries.isEmpty()) {
            HomeUiState.Empty(goalWeightDisplay = goalDisplay)
        } else {
            buildHasData(data, strings, goalDisplay)
        }
    }

    private fun buildHasData(
        data: HomeData,
        strings: HomeStrings,
        goalDisplay: String?,
    ): HomeUiState.HasData {
        val latest = data.entries.first()
        val deltaKg = data.entries.getOrNull(1)?.let { latest.weightKg - it.weightKg }
        val goalState = if (data.profile != null) {
            GoalStateMachine.rehydrate(data.profile)
        } else {
            GoalState.NoGoal
        }

        return HomeUiState.HasData(
            latestWeightDisplay = formatWeight(latest.weightKg, data.unit, strings),
            weightUnit = data.unit,
            recentEntries = buildRecentEntries(data, strings),
            goalWeightDisplay = goalDisplay,
            goalState = goalState,
            deltaDisplay = deltaKg?.let { formatDelta(it, data.unit, strings) },
            deltaIsDown = deltaKg?.let { it < 0 },
            streakDays = computeStreak(data.entries),
            avgDisplay = computeAvgDisplay(data, strings),
            goalProgress = computeGoalProgress(data, latest),
            startDisplay = computeStartDisplay(data, strings),
            lostDisplay = computeLostDisplay(data, strings),
            isGoalAchieved = goalState is GoalState.Active &&
                data.entries.first().weightKg <= goalState.goalWeightKg,
            // entries is newest-first: take() the most recent N, then reverse to
            // chronological order for the chart. takeLast() would freeze the chart
            // on the oldest N entries once the user logs more than N.
            sparklinePoints = data.entries
                .take(SPARKLINE_POINTS)
                .map { it.weightKg.toFloat() }
                .reversed(),
        )
    }

    private fun buildRecentEntries(data: HomeData, strings: HomeStrings): List<RecentEntryDisplay> =
        data.entries.take(MAX_RECENT_ENTRIES).mapIndexed { i, entry ->
            val entryDelta = data.entries.getOrNull(i + 1)?.let { entry.weightKg - it.weightKg }
            RecentEntryDisplay(
                id = entry.id,
                weightDisplay = formatWeight(entry.weightKg, data.unit, strings),
                dateDisplay = formatDate(entry.timestamp, strings),
                timestamp = entry.timestamp,
                deltaDisplay = entryDelta?.let { formatDelta(it, data.unit, strings) },
                deltaIsDown = entryDelta?.let { it < 0 },
            )
        }

    private fun computeAvgDisplay(data: HomeData, strings: HomeStrings): String? {
        val cutoff = System.currentTimeMillis() - AVG_WINDOW_MILLIS
        val recentForAvg = data.entries.filter { it.timestamp >= cutoff }
        return if (recentForAvg.size >= 2) {
            formatWeight(recentForAvg.map { it.weightKg }.average(), data.unit, strings)
        } else {
            null
        }
    }

    private fun computeGoalProgress(data: HomeData, latest: WeightEntry): Float? =
        data.profile?.goalWeightKg?.let { goal ->
            if (data.entries.size < 2) {
                null
            } else {
                val start = data.entries.last().weightKg
                if (start == goal) {
                    1f
                } else {
                    ((start - latest.weightKg) / (start - goal)).toFloat().coerceIn(0f, 1f)
                }
            }
        }

    private fun computeStartDisplay(data: HomeData, strings: HomeStrings): String? =
        if (data.entries.size >= 2) {
            formatWeight(data.entries.last().weightKg, data.unit, strings)
        } else {
            null
        }

    private fun computeLostDisplay(data: HomeData, strings: HomeStrings): String? {
        if (data.entries.size < 2) return null
        val diff = data.entries.last().weightKg - data.entries.first().weightKg
        return if (diff > 0) {
            "−${oneDecimal(diff, strings)}"
        } else {
            "+${oneDecimal(-diff, strings)}"
        }
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

    private fun formatDate(timestamp: Long, strings: HomeStrings): String {
        val date = Instant.ofEpochMilli(timestamp).atZone(ZoneId.systemDefault()).toLocalDate()
        return when (DateFormatters.relativeDay(date)) {
            DateFormatters.RelativeDay.TODAY -> strings.today
            DateFormatters.RelativeDay.YESTERDAY -> strings.yesterday
            DateFormatters.RelativeDay.OTHER -> date.format(DateFormatters.dayMonth(strings.locale))
        }
    }
}
