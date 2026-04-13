package com.weightflow.domain

import java.time.LocalDate
import java.time.temporal.ChronoUnit
import kotlin.math.abs

enum class DriftDirection { IN_RANGE, ABOVE, BELOW }

data class GoalProgress(
    val progressPercent: Float,
    val isGoalReached: Boolean,
    val driftDirection: DriftDirection,
    val daysRemaining: Int?
)

object GoalProgressCalculator {

    fun calculate(
        startKg: Double,
        goalKg: Double,
        currentKg: Double,
        targetDate: LocalDate?,
        maintenanceRangeKg: Double
    ): GoalProgress {
        val totalDistance = abs(goalKg - startKg)
        val coveredDistance = if (goalKg <= startKg) startKg - currentKg else currentKg - startKg

        val progressPercent = if (totalDistance == 0.0) 100f
        else (coveredDistance / totalDistance * 100.0).coerceIn(0.0, 100.0).toFloat()

        val isGoalReached = if (goalKg <= startKg) currentKg <= goalKg else currentKg >= goalKg

        val driftDirection = when {
            currentKg > goalKg + maintenanceRangeKg -> DriftDirection.ABOVE
            currentKg < goalKg - maintenanceRangeKg -> DriftDirection.BELOW
            else -> DriftDirection.IN_RANGE
        }

        val daysRemaining = targetDate?.let {
            ChronoUnit.DAYS.between(LocalDate.now(), it).coerceAtLeast(0).toInt()
        }

        return GoalProgress(progressPercent, isGoalReached, driftDirection, daysRemaining)
    }
}
