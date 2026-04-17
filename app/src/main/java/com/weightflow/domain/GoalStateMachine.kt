package com.weightflow.domain

import java.time.LocalDate

/**
 * RFC #26 — GoalStateMachine.
 * Pure domain object — no Android/Room imports.
 * Rehydrates from UserProfile on cold start; provides explicit transition functions.
 */
object GoalStateMachine {

    // ── Rehydration ──────────────────────────────────────────────────────────

    /**
     * Reconstruct the current [GoalState] from a persisted [UserProfile].
     * Called once on cold start by the ViewModel.
     */
    fun rehydrate(profile: UserProfile): GoalState {
        val goalKg = profile.goalWeightKg ?: return GoalState.NoGoal

        if (profile.maintenanceMode) {
            val achievedAt = profile.achievedAt ?: profile.targetDate ?: LocalDate.now()
            val maintenanceSince = profile.maintenanceModeActivatedAt
                ?.let { LocalDate.ofEpochDay(it / 86_400_000L) }
                ?: achievedAt
            return GoalState.Maintenance(goalKg, achievedAt, maintenanceSince)
        }

        val achievedAt = profile.achievedAt
        if (achievedAt != null) {
            return GoalState.Achieved(goalKg, achievedAt)
        }

        return GoalState.Active(goalKg, startWeightKg = null, targetDate = profile.targetDate)
    }

    // ── Transitions ──────────────────────────────────────────────────────────

    /** Set a new goal from any state. Returns [GoalState.Active]. */
    fun setGoal(
        goalWeightKg: Double,
        startWeightKg: Double,
        targetDate: LocalDate?,
    ): GoalState.Active = GoalState.Active(goalWeightKg, startWeightKg, targetDate)

    /** Goal weight reached. Transitions [GoalState.Active] → [GoalState.Achieved]. */
    fun markAchieved(
        state: GoalState.Active,
        achievedAt: LocalDate = LocalDate.now(),
    ): GoalState.Achieved = GoalState.Achieved(state.goalWeightKg, achievedAt)

    /** User chooses to maintain. Transitions [GoalState.Achieved] → [GoalState.Maintenance]. */
    fun enterMaintenance(
        state: GoalState.Achieved,
        maintenanceSince: LocalDate = LocalDate.now(),
    ): GoalState.Maintenance = GoalState.Maintenance(state.goalWeightKg, state.achievedAt, maintenanceSince)

    /** User clears their goal from any state. Returns [GoalState.NoGoal]. */
    fun clearGoal(): GoalState.NoGoal = GoalState.NoGoal

    /**
     * User sets a fresh goal (e.g. after maintenance).
     * [currentWeightKg] becomes the new starting point.
     */
    fun startNewGoal(
        goalWeightKg: Double,
        currentWeightKg: Double,
        targetDate: LocalDate?,
    ): GoalState.Active = GoalState.Active(goalWeightKg, currentWeightKg, targetDate)

    // ── Persistence ──────────────────────────────────────────────────────────

    /**
     * Apply [state] back onto [profile] for persistence via UserProfileRepository.
     * Returns a new UserProfile with FSM fields updated; other fields preserved.
     */
    fun applyToProfile(state: GoalState, profile: UserProfile): UserProfile = when (state) {
        is GoalState.NoGoal -> profile.copy(
            goalWeightKg = null,
            targetDate = null,
            maintenanceMode = false,
            achievedAt = null,
            maintenanceModeActivatedAt = null,
        )
        is GoalState.Active -> profile.copy(
            goalWeightKg = state.goalWeightKg,
            targetDate = state.targetDate,
            maintenanceMode = false,
            achievedAt = null,
            maintenanceModeActivatedAt = null,
        )
        is GoalState.Achieved -> profile.copy(
            goalWeightKg = state.goalWeightKg,
            maintenanceMode = false,
            achievedAt = state.achievedAt,
        )
        is GoalState.Maintenance -> profile.copy(
            goalWeightKg = state.goalWeightKg,
            maintenanceMode = true,
            achievedAt = state.achievedAt,
            maintenanceModeActivatedAt = state.maintenanceSince.toEpochDay() * 86_400_000L,
        )
    }
}
