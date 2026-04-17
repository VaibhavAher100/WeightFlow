package com.weightflow.domain

import java.time.LocalDate

/**
 * RFC #26 — Sealed FSM representing the user's goal lifecycle.
 * Transitions live in GoalStateMachine. This class is pure data.
 */
sealed class GoalState {
    /** No goal has been set. */
    data object NoGoal : GoalState()

    /** Goal set and actively being pursued. */
    data class Active(
        val goalWeightKg: Double,
        val startWeightKg: Double?,
        val targetDate: LocalDate?,
    ) : GoalState()

    /** Goal weight has been hit; user may choose to enter maintenance or set a new goal. */
    data class Achieved(
        val goalWeightKg: Double,
        val achievedAt: LocalDate,
    ) : GoalState()

    /** Maintaining goal weight within a tolerance band. */
    data class Maintenance(
        val goalWeightKg: Double,
        val achievedAt: LocalDate,
        val maintenanceSince: LocalDate,
    ) : GoalState()
}
