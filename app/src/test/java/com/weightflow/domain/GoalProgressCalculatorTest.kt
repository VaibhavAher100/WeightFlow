package com.weightflow.domain

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDate

/**
 * TDD: These tests were written BEFORE GoalProgressCalculator exists.
 * Run them — they will all fail. Then implement GoalProgressCalculator to make them pass.
 */
class GoalProgressCalculatorTest {

    // ── Progress percentage ──────────────────────────────────────────────────

    @Test
    fun `50 percent progress when halfway to weight loss goal`() {
        val result = GoalProgressCalculator.calculate(
            startKg = 100.0,
            goalKg = 80.0,
            currentKg = 90.0,
            targetDate = null,
            maintenanceRangeKg = 1.0
        )
        assertEquals(50f, result.progressPercent, 0.1f)
    }

    @Test
    fun `0 percent progress at start weight`() {
        val result = GoalProgressCalculator.calculate(
            startKg = 100.0,
            goalKg = 80.0,
            currentKg = 100.0,
            targetDate = null,
            maintenanceRangeKg = 1.0
        )
        assertEquals(0f, result.progressPercent, 0.1f)
    }

    @Test
    fun `100 percent progress when goal reached`() {
        val result = GoalProgressCalculator.calculate(
            startKg = 100.0,
            goalKg = 80.0,
            currentKg = 80.0,
            targetDate = null,
            maintenanceRangeKg = 1.0
        )
        assertEquals(100f, result.progressPercent, 0.1f)
    }

    @Test
    fun `progress capped at 100 when weight goes below goal`() {
        val result = GoalProgressCalculator.calculate(
            startKg = 100.0,
            goalKg = 80.0,
            currentKg = 78.0,
            targetDate = null,
            maintenanceRangeKg = 1.0
        )
        assertEquals(100f, result.progressPercent, 0.1f)
    }

    // ── Goal reached ────────────────────────────────────────────────────────

    @Test
    fun `isGoalReached true when current equals goal`() {
        val result = GoalProgressCalculator.calculate(
            startKg = 100.0, goalKg = 80.0, currentKg = 80.0,
            targetDate = null, maintenanceRangeKg = 1.0
        )
        assertTrue(result.isGoalReached)
    }

    @Test
    fun `isGoalReached true when current goes below goal`() {
        val result = GoalProgressCalculator.calculate(
            startKg = 100.0, goalKg = 80.0, currentKg = 79.0,
            targetDate = null, maintenanceRangeKg = 1.0
        )
        assertTrue(result.isGoalReached)
    }

    @Test
    fun `isGoalReached false when not yet at goal`() {
        val result = GoalProgressCalculator.calculate(
            startKg = 100.0, goalKg = 80.0, currentKg = 85.0,
            targetDate = null, maintenanceRangeKg = 1.0
        )
        assertFalse(result.isGoalReached)
    }

    // ── Maintenance mode ────────────────────────────────────────────────────

    @Test
    fun `maintenance drift is IN_RANGE when within plus or minus 1kg`() {
        val result = GoalProgressCalculator.calculate(
            startKg = 100.0, goalKg = 80.0, currentKg = 80.5,
            targetDate = null, maintenanceRangeKg = 1.0
        )
        assertEquals(DriftDirection.IN_RANGE, result.driftDirection)
    }

    @Test
    fun `maintenance drift is ABOVE when over range`() {
        val result = GoalProgressCalculator.calculate(
            startKg = 100.0, goalKg = 80.0, currentKg = 81.5,
            targetDate = null, maintenanceRangeKg = 1.0
        )
        assertEquals(DriftDirection.ABOVE, result.driftDirection)
    }

    @Test
    fun `maintenance drift is BELOW when under range`() {
        val result = GoalProgressCalculator.calculate(
            startKg = 100.0, goalKg = 80.0, currentKg = 78.5,
            targetDate = null, maintenanceRangeKg = 1.0
        )
        assertEquals(DriftDirection.BELOW, result.driftDirection)
    }

    // ── Days remaining ───────────────────────────────────────────────────────

    @Test
    fun `daysRemaining is null when no target date set`() {
        val result = GoalProgressCalculator.calculate(
            startKg = 100.0, goalKg = 80.0, currentKg = 90.0,
            targetDate = null, maintenanceRangeKg = 1.0
        )
        assertNull(result.daysRemaining)
    }

    @Test
    fun `daysRemaining is positive when target date is in future`() {
        val futureDate = LocalDate.now().plusDays(60)
        val result = GoalProgressCalculator.calculate(
            startKg = 100.0, goalKg = 80.0, currentKg = 90.0,
            targetDate = futureDate, maintenanceRangeKg = 1.0
        )
        assertEquals(60, result.daysRemaining)
    }

    @Test
    fun `daysRemaining is 0 when target date is today`() {
        val result = GoalProgressCalculator.calculate(
            startKg = 100.0, goalKg = 80.0, currentKg = 90.0,
            targetDate = LocalDate.now(), maintenanceRangeKg = 1.0
        )
        assertEquals(0, result.daysRemaining)
    }

    // ── Weight gain goal ────────────────────────────────────────────────────

    @Test
    fun `handles weight gain goal correctly`() {
        val result = GoalProgressCalculator.calculate(
            startKg = 60.0,
            goalKg = 80.0,
            currentKg = 70.0,
            targetDate = null,
            maintenanceRangeKg = 1.0
        )
        assertEquals(50f, result.progressPercent, 0.1f)
        assertFalse(result.isGoalReached)
    }

    @Test
    fun `weight gain goal reached when current meets or exceeds goal`() {
        val result = GoalProgressCalculator.calculate(
            startKg = 60.0, goalKg = 80.0, currentKg = 80.0,
            targetDate = null, maintenanceRangeKg = 1.0
        )
        assertTrue(result.isGoalReached)
    }
}
