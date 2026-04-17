package com.weightflow.ui.home

import com.weightflow.domain.GoalState
import com.weightflow.domain.GoalStateMachine
import com.weightflow.domain.HomeData
import com.weightflow.domain.WeightConverter
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter

/**
 * RFC #27: Single conversion point from domain data to UI display strings.
 * All WeightConverter.format() calls for the Home screen go through here.
 */
object HomeUiStateMapper {

    private const val MAX_RECENT_ENTRIES = 5
    private val DATE_FMT = DateTimeFormatter.ofPattern("d MMM")

    fun map(data: HomeData): HomeUiState {
        val goalDisplay = data.profile?.goalWeightKg?.let { goalKg ->
            WeightConverter.format(goalKg, data.unit)
        }

        if (data.entries.isEmpty()) {
            return HomeUiState.Empty(goalWeightDisplay = goalDisplay)
        }

        val latest = data.entries.first()
        val recentEntries = data.entries
            .take(MAX_RECENT_ENTRIES)
            .map { entry ->
                RecentEntryDisplay(
                    id = entry.id,
                    weightDisplay = WeightConverter.format(entry.weightKg, data.unit),
                    dateDisplay = formatDate(entry.timestamp),
                    timestamp = entry.timestamp,
                )
            }

        val goalState = if (data.profile != null) GoalStateMachine.rehydrate(data.profile)
        else GoalState.NoGoal

        return HomeUiState.HasData(
            latestWeightDisplay = WeightConverter.format(latest.weightKg, data.unit),
            weightUnit = data.unit,
            recentEntries = recentEntries,
            goalWeightDisplay = goalDisplay,
            goalState = goalState,
        )
    }

    private fun formatDate(timestamp: Long): String {
        val date = Instant.ofEpochMilli(timestamp)
            .atZone(ZoneId.systemDefault())
            .toLocalDate()
        return if (date == LocalDate.now()) "Today" else date.format(DATE_FMT)
    }
}
