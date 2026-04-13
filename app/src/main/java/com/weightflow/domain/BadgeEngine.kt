package com.weightflow.domain

import java.time.LocalDate
import java.time.temporal.ChronoUnit

enum class Badge {
    FIRST_WEIGH_IN,
    GOAL_SET,
    SEVEN_DAY_STREAK,
    THIRTY_DAY_STREAK,
    HUNDRED_DAY_STREAK,
    TEN_LOGS,
    FIFTY_LOGS,
    THREE_SIXTY_FIVE_LOGS,
    HALFWAY_THERE,
    GOAL_CRUSHER,
    COMEBACK,
    STEADY_STATE
}

object BadgeEngine {

    fun evaluate(entries: List<WeightEntry>, profile: UserProfile): Set<Badge> {
        val awarded = mutableSetOf<Badge>()

        if (entries.isEmpty()) return awarded

        // Volume
        awarded += Badge.FIRST_WEIGH_IN
        if (entries.size >= 10) awarded += Badge.TEN_LOGS
        if (entries.size >= 50) awarded += Badge.FIFTY_LOGS
        if (entries.size >= 365) awarded += Badge.THREE_SIXTY_FIVE_LOGS

        // Goal metadata
        if (profile.goalWeightKg != null) awarded += Badge.GOAL_SET

        // Streaks
        val streak = maxConsecutiveStreak(entries)
        if (streak >= 7) awarded += Badge.SEVEN_DAY_STREAK
        if (streak >= 30) awarded += Badge.THIRTY_DAY_STREAK
        if (streak >= 100) awarded += Badge.HUNDRED_DAY_STREAK

        // Comeback: any gap between consecutive entries >= 14 days
        if (hasComeback(entries)) awarded += Badge.COMEBACK

        // Progress badges (require a goal weight and at least one prior entry)
        val goalKg = profile.goalWeightKg
        if (goalKg != null && entries.size >= 1) {
            val sortedEntries = entries.sortedBy { it.timestamp }
            val startKg = sortedEntries.first().weightKg
            val currentKg = sortedEntries.last().weightKg

            val progress = GoalProgressCalculator.calculate(
                startKg = startKg,
                goalKg = goalKg,
                currentKg = currentKg,
                targetDate = null,
                maintenanceRangeKg = profile.maintenanceRangeKg
            )

            if (progress.progressPercent >= 50f) awarded += Badge.HALFWAY_THERE
            if (progress.isGoalReached) awarded += Badge.GOAL_CRUSHER
        }

        // Steady state: maintenance mode active for 30+ days
        if (profile.maintenanceMode && profile.maintenanceModeActivatedAt != null) {
            val activatedDate = LocalDate.ofEpochDay(profile.maintenanceModeActivatedAt / 86_400_000L)
            val daysInMaintenance = ChronoUnit.DAYS.between(activatedDate, LocalDate.now())
            if (daysInMaintenance >= 30) awarded += Badge.STEADY_STATE
        }

        return awarded
    }

    private fun maxConsecutiveStreak(entries: List<WeightEntry>): Int {
        val dates = entries
            .map { LocalDate.ofEpochDay(it.timestamp / 86_400_000L) }
            .toSortedSet()

        if (dates.size < 2) return dates.size

        val dateList = dates.toList()
        var maxStreak = 1
        var currentStreak = 1

        for (i in 1 until dateList.size) {
            if (dateList[i].minusDays(1) == dateList[i - 1]) {
                currentStreak++
                if (currentStreak > maxStreak) maxStreak = currentStreak
            } else {
                currentStreak = 1
            }
        }

        return maxStreak
    }

    private fun hasComeback(entries: List<WeightEntry>): Boolean {
        val sorted = entries.sortedBy { it.timestamp }
        for (i in 1 until sorted.size) {
            val prev = LocalDate.ofEpochDay(sorted[i - 1].timestamp / 86_400_000L)
            val curr = LocalDate.ofEpochDay(sorted[i].timestamp / 86_400_000L)
            if (ChronoUnit.DAYS.between(prev, curr) >= 14) return true
        }
        return false
    }
}
