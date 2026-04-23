package com.weightflow.ui.home

import com.weightflow.domain.HomeData
import com.weightflow.domain.WeightConverter
import com.weightflow.domain.WeightEntry
import com.weightflow.domain.WeightUnit
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter

object HomeUiStateMapper {

    private const val MAX_RECENT_ENTRIES = 5
    private val DATE_FMT = DateTimeFormatter.ofPattern("d MMM")

    fun map(data: HomeData): HomeUiState {
        val goalDisplay = data.profile?.goalWeightKg?.let { WeightConverter.format(it, data.unit) }

        if (data.entries.isEmpty()) return HomeUiState.Empty(goalWeightDisplay = goalDisplay)

        val latest = data.entries.first()
        val previous = data.entries.getOrNull(1)

        val deltaKg = previous?.let { latest.weightKg - it.weightKg }
        val deltaDisplay = deltaKg?.let { formatDelta(it, data.unit) }
        val deltaIsDown = deltaKg?.let { it < 0 }

        val recentEntries = data.entries.take(MAX_RECENT_ENTRIES).mapIndexed { i, entry ->
            val prev = data.entries.getOrNull(i + 1)
            val entryDelta = prev?.let { entry.weightKg - it.weightKg }
            RecentEntryDisplay(
                id = entry.id,
                weightDisplay = WeightConverter.format(entry.weightKg, data.unit),
                dateDisplay = formatDate(entry.timestamp),
                timestamp = entry.timestamp,
                deltaDisplay = entryDelta?.let { formatDelta(it, data.unit) },
                deltaIsDown = entryDelta?.let { it < 0 },
            )
        }

        val streakDays = computeStreak(data.entries)

        val cutoff = System.currentTimeMillis() - 7L * 24 * 60 * 60 * 1000
        val recentForAvg = data.entries.filter { it.timestamp >= cutoff }
        val avgDisplay = if (recentForAvg.size >= 2) {
            WeightConverter.format(recentForAvg.map { it.weightKg }.average(), data.unit)
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

        return HomeUiState.HasData(
            latestWeightDisplay = WeightConverter.format(latest.weightKg, data.unit),
            weightUnit = data.unit,
            recentEntries = recentEntries,
            goalWeightDisplay = goalDisplay,
            deltaDisplay = deltaDisplay,
            deltaIsDown = deltaIsDown,
            streakDays = streakDays,
            avgDisplay = avgDisplay,
            goalProgress = goalProgress,
        )
    }

    private fun formatDelta(deltaKg: Double, unit: WeightUnit): String {
        val sign = if (deltaKg < 0) "\u2212" else "+"
        return "$sign${WeightConverter.format(Math.abs(deltaKg), unit)}"
    }

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

    private fun formatDate(timestamp: Long): String {
        val date = Instant.ofEpochMilli(timestamp).atZone(ZoneId.systemDefault()).toLocalDate()
        return if (date == LocalDate.now()) "Today" else date.format(DATE_FMT)
    }
}
