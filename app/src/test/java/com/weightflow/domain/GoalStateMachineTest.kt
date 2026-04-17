package com.weightflow.domain

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDate

/**
 * TDD: Written BEFORE GoalState/GoalStateMachine exist. All tests fail until production code lands.
 * RFC #26 — sealed FSM with explicit transition functions + cold-start rehydration from UserProfile.
 */
class GoalStateMachineTest {

    private val baseProfile = UserProfile(
        id = 1L,
        displayName = "Vaibhav",
        goalWeightKg = null,
        targetDate = null,
        heightCm = 175.0,
        maintenanceMode = false,
        maintenanceRangeKg = 1.0,
        maintenanceModeActivatedAt = null,
        achievedAt = null,
    )

    // ── Rehydration ──────────────────────────────────────────────────────────

    @Test
    fun `rehydrate returns NoGoal when goalWeightKg is null`() {
        val state = GoalStateMachine.rehydrate(baseProfile)
        assertTrue(state is GoalState.NoGoal)
    }

    @Test
    fun `rehydrate returns Active when goal set, no achievedAt, not maintenance`() {
        val profile = baseProfile.copy(goalWeightKg = 75.0, targetDate = LocalDate.of(2026, 12, 31))
        val state = GoalStateMachine.rehydrate(profile)
        assertTrue(state is GoalState.Active)
        val active = state as GoalState.Active
        assertEquals(75.0, active.goalWeightKg, 0.001)
        assertEquals(LocalDate.of(2026, 12, 31), active.targetDate)
    }

    @Test
    fun `rehydrate returns Achieved when achievedAt is set and not maintenance`() {
        val achievedDate = LocalDate.of(2026, 6, 1)
        val profile = baseProfile.copy(goalWeightKg = 75.0, achievedAt = achievedDate)
        val state = GoalStateMachine.rehydrate(profile)
        assertTrue(state is GoalState.Achieved)
        val achieved = state as GoalState.Achieved
        assertEquals(75.0, achieved.goalWeightKg, 0.001)
        assertEquals(achievedDate, achieved.achievedAt)
    }

    @Test
    fun `rehydrate returns Maintenance when maintenanceMode is true`() {
        val achievedDate = LocalDate.of(2026, 5, 1)
        val maintenanceSince = LocalDate.of(2026, 5, 15)
        val profile = baseProfile.copy(
            goalWeightKg = 75.0,
            maintenanceMode = true,
            achievedAt = achievedDate,
            maintenanceModeActivatedAt = maintenanceSince.toEpochDay() * 86_400_000L,
        )
        val state = GoalStateMachine.rehydrate(profile)
        assertTrue(state is GoalState.Maintenance)
        val maintenance = state as GoalState.Maintenance
        assertEquals(75.0, maintenance.goalWeightKg, 0.001)
        assertEquals(achievedDate, maintenance.achievedAt)
        assertEquals(maintenanceSince, maintenance.maintenanceSince)
    }

    // ── Transitions ──────────────────────────────────────────────────────────

    @Test
    fun `setGoal returns Active with correct fields`() {
        val state = GoalStateMachine.setGoal(
            goalWeightKg = 70.0,
            startWeightKg = 85.0,
            targetDate = LocalDate.of(2027, 1, 1),
        )
        assertTrue(state is GoalState.Active)
        val active = state as GoalState.Active
        assertEquals(70.0, active.goalWeightKg, 0.001)
        assertEquals(85.0, active.startWeightKg ?: 0.0, 0.001)
        assertEquals(LocalDate.of(2027, 1, 1), active.targetDate)
    }

    @Test
    fun `markAchieved transitions Active to Achieved`() {
        val active = GoalState.Active(goalWeightKg = 70.0, startWeightKg = 85.0, targetDate = null)
        val achievedDate = LocalDate.of(2026, 8, 10)
        val state = GoalStateMachine.markAchieved(active, achievedAt = achievedDate)
        assertTrue(state is GoalState.Achieved)
        val achieved = state as GoalState.Achieved
        assertEquals(70.0, achieved.goalWeightKg, 0.001)
        assertEquals(achievedDate, achieved.achievedAt)
    }

    @Test
    fun `enterMaintenance transitions Achieved to Maintenance`() {
        val achieved = GoalState.Achieved(
            goalWeightKg = 70.0,
            achievedAt = LocalDate.of(2026, 8, 10),
        )
        val maintenanceSince = LocalDate.of(2026, 8, 20)
        val state = GoalStateMachine.enterMaintenance(achieved, maintenanceSince = maintenanceSince)
        assertTrue(state is GoalState.Maintenance)
        val maintenance = state as GoalState.Maintenance
        assertEquals(70.0, maintenance.goalWeightKg, 0.001)
        assertEquals(LocalDate.of(2026, 8, 10), maintenance.achievedAt)
        assertEquals(maintenanceSince, maintenance.maintenanceSince)
    }

    @Test
    fun `clearGoal returns NoGoal from any state`() {
        assertTrue(GoalStateMachine.clearGoal() is GoalState.NoGoal)
    }

    @Test
    fun `startNewGoal returns Active with new goal from Maintenance`() {
        val state = GoalStateMachine.startNewGoal(
            goalWeightKg = 65.0,
            currentWeightKg = 72.0,
            targetDate = null,
        )
        assertTrue(state is GoalState.Active)
        val active = state as GoalState.Active
        assertEquals(65.0, active.goalWeightKg, 0.001)
        assertEquals(72.0, active.startWeightKg ?: 0.0, 0.001)
        assertNull(active.targetDate)
    }

    // ── applyToProfile persistence round-trip ─────────────────────────────

    @Test
    fun `applyToProfile NoGoal clears all goal fields`() {
        val profile = baseProfile.copy(goalWeightKg = 75.0, maintenanceMode = true)
        val updated = GoalStateMachine.applyToProfile(GoalState.NoGoal, profile)
        assertNull(updated.goalWeightKg)
        assertNull(updated.achievedAt)
        assertNull(updated.maintenanceModeActivatedAt)
        assertEquals(false, updated.maintenanceMode)
    }

    @Test
    fun `applyToProfile Active sets goalWeightKg and clears maintenance fields`() {
        val active = GoalState.Active(goalWeightKg = 70.0, startWeightKg = 85.0, targetDate = LocalDate.of(2027, 3, 1))
        val updated = GoalStateMachine.applyToProfile(active, baseProfile)
        assertEquals(70.0, updated.goalWeightKg ?: 0.0, 0.001)
        assertEquals(false, updated.maintenanceMode)
        assertNull(updated.achievedAt)
    }

    @Test
    fun `applyToProfile Achieved sets achievedAt and keeps maintenanceMode false`() {
        val achievedDate = LocalDate.of(2026, 9, 5)
        val achieved = GoalState.Achieved(goalWeightKg = 70.0, achievedAt = achievedDate)
        val updated = GoalStateMachine.applyToProfile(achieved, baseProfile)
        assertEquals(70.0, updated.goalWeightKg ?: 0.0, 0.001)
        assertEquals(false, updated.maintenanceMode)
        assertEquals(achievedDate, updated.achievedAt)
    }

    @Test
    fun `applyToProfile Maintenance sets maintenanceMode true with timestamps`() {
        val maintenance = GoalState.Maintenance(
            goalWeightKg = 70.0,
            achievedAt = LocalDate.of(2026, 8, 10),
            maintenanceSince = LocalDate.of(2026, 8, 20),
        )
        val updated = GoalStateMachine.applyToProfile(maintenance, baseProfile)
        assertEquals(70.0, updated.goalWeightKg!!, 0.001)
        assertEquals(true, updated.maintenanceMode)
        assertEquals(LocalDate.of(2026, 8, 10), updated.achievedAt)
        val expectedMaintenanceSinceMs = LocalDate.of(2026, 8, 20).toEpochDay() * 86_400_000L
        assertEquals(expectedMaintenanceSinceMs, updated.maintenanceModeActivatedAt)
    }

    @Test
    fun `round-trip rehydrate after applyToProfile gives same Maintenance state`() {
        val original = GoalState.Maintenance(
            goalWeightKg = 70.0,
            achievedAt = LocalDate.of(2026, 8, 10),
            maintenanceSince = LocalDate.of(2026, 8, 20),
        )
        val profile = GoalStateMachine.applyToProfile(original, baseProfile)
        val rehydrated = GoalStateMachine.rehydrate(profile)
        assertTrue(rehydrated is GoalState.Maintenance)
        assertEquals(original, rehydrated)
    }
}
