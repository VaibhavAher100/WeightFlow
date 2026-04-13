package com.weightflow.domain

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDate

/**
 * TDD: These tests were written BEFORE BadgeEngine exists.
 * Run them — they will all fail. Then implement BadgeEngine to make them pass.
 *
 * BadgeEngine.evaluate() is a pure function: same input → same output, always.
 */
class BadgeEngineTest {

    // ── Helpers ──────────────────────────────────────────────────────────────

    private fun entryOnDay(daysAgo: Int, weightKg: Double = 80.0) = WeightEntry(
        id = daysAgo.toLong(),
        timestamp = LocalDate.now().minusDays(daysAgo.toLong())
            .toEpochDay() * 86_400_000L,
        weightKg = weightKg,
        note = ""
    )

    private fun consecutiveDays(count: Int) =
        (0 until count).map { entryOnDay(count - 1 - it) }

    private fun baseProfile(
        goalWeightKg: Double? = 75.0,
        maintenanceMode: Boolean = false,
        maintenanceModeActivatedAt: Long? = null
    ) = UserProfile(
        id = 1,
        displayName = "Test",
        goalWeightKg = goalWeightKg,
        targetDate = null,
        heightCm = null,
        maintenanceMode = maintenanceMode,
        maintenanceRangeKg = 1.0,
        maintenanceModeActivatedAt = maintenanceModeActivatedAt
    )

    // ── FIRST_WEIGH_IN ───────────────────────────────────────────────────────

    @Test
    fun `FIRST_WEIGH_IN awarded after first log`() {
        val badges = BadgeEngine.evaluate(listOf(entryOnDay(0)), baseProfile())
        assertTrue(Badge.FIRST_WEIGH_IN in badges)
    }

    @Test
    fun `FIRST_WEIGH_IN not awarded with empty log`() {
        val badges = BadgeEngine.evaluate(emptyList(), baseProfile())
        assertFalse(Badge.FIRST_WEIGH_IN in badges)
    }

    // ── GOAL_SET ─────────────────────────────────────────────────────────────

    @Test
    fun `GOAL_SET awarded when profile has a goal weight`() {
        val badges = BadgeEngine.evaluate(listOf(entryOnDay(0)), baseProfile(goalWeightKg = 75.0))
        assertTrue(Badge.GOAL_SET in badges)
    }

    @Test
    fun `GOAL_SET not awarded when no goal weight set`() {
        val badges = BadgeEngine.evaluate(listOf(entryOnDay(0)), baseProfile(goalWeightKg = null))
        assertFalse(Badge.GOAL_SET in badges)
    }

    // ── Streak badges ────────────────────────────────────────────────────────

    @Test
    fun `SEVEN_DAY_STREAK awarded after 7 consecutive days`() {
        val badges = BadgeEngine.evaluate(consecutiveDays(7), baseProfile())
        assertTrue(Badge.SEVEN_DAY_STREAK in badges)
    }

    @Test
    fun `SEVEN_DAY_STREAK not awarded after only 6 consecutive days`() {
        val badges = BadgeEngine.evaluate(consecutiveDays(6), baseProfile())
        assertFalse(Badge.SEVEN_DAY_STREAK in badges)
    }

    @Test
    fun `SEVEN_DAY_STREAK not awarded when days are not consecutive`() {
        val nonConsecutive = listOf(entryOnDay(10), entryOnDay(8), entryOnDay(6),
            entryOnDay(4), entryOnDay(2), entryOnDay(1), entryOnDay(0))
        val badges = BadgeEngine.evaluate(nonConsecutive, baseProfile())
        assertFalse(Badge.SEVEN_DAY_STREAK in badges)
    }

    @Test
    fun `THIRTY_DAY_STREAK awarded after 30 consecutive days`() {
        val badges = BadgeEngine.evaluate(consecutiveDays(30), baseProfile())
        assertTrue(Badge.THIRTY_DAY_STREAK in badges)
    }

    @Test
    fun `THIRTY_DAY_STREAK not awarded after only 29 consecutive days`() {
        val badges = BadgeEngine.evaluate(consecutiveDays(29), baseProfile())
        assertFalse(Badge.THIRTY_DAY_STREAK in badges)
    }

    @Test
    fun `HUNDRED_DAY_STREAK awarded after 100 consecutive days`() {
        val badges = BadgeEngine.evaluate(consecutiveDays(100), baseProfile())
        assertTrue(Badge.HUNDRED_DAY_STREAK in badges)
    }

    // ── Volume badges ────────────────────────────────────────────────────────

    @Test
    fun `TEN_LOGS awarded after 10 total entries`() {
        val entries = (0 until 10).map { entryOnDay(it) }
        val badges = BadgeEngine.evaluate(entries, baseProfile())
        assertTrue(Badge.TEN_LOGS in badges)
    }

    @Test
    fun `TEN_LOGS not awarded with only 9 entries`() {
        val entries = (0 until 9).map { entryOnDay(it) }
        val badges = BadgeEngine.evaluate(entries, baseProfile())
        assertFalse(Badge.TEN_LOGS in badges)
    }

    @Test
    fun `FIFTY_LOGS awarded after 50 total entries`() {
        val entries = (0 until 50).map { entryOnDay(it) }
        val badges = BadgeEngine.evaluate(entries, baseProfile())
        assertTrue(Badge.FIFTY_LOGS in badges)
    }

    @Test
    fun `THREE_SIXTY_FIVE_LOGS awarded after 365 total entries`() {
        val entries = (0 until 365).map { entryOnDay(it) }
        val badges = BadgeEngine.evaluate(entries, baseProfile())
        assertTrue(Badge.THREE_SIXTY_FIVE_LOGS in badges)
    }

    // ── Progress badges ──────────────────────────────────────────────────────

    @Test
    fun `HALFWAY_THERE awarded when 50 percent to goal`() {
        val entries = listOf(entryOnDay(1, weightKg = 100.0), entryOnDay(0, weightKg = 90.0))
        val profile = baseProfile(goalWeightKg = 80.0)
        val badges = BadgeEngine.evaluate(entries, profile)
        assertTrue(Badge.HALFWAY_THERE in badges)
    }

    @Test
    fun `HALFWAY_THERE not awarded when less than 50 percent progress`() {
        val entries = listOf(entryOnDay(1, weightKg = 100.0), entryOnDay(0, weightKg = 95.0))
        val profile = baseProfile(goalWeightKg = 80.0)
        val badges = BadgeEngine.evaluate(entries, profile)
        assertFalse(Badge.HALFWAY_THERE in badges)
    }

    @Test
    fun `GOAL_CRUSHER awarded when current weight reaches goal`() {
        val entries = listOf(entryOnDay(1, weightKg = 100.0), entryOnDay(0, weightKg = 80.0))
        val profile = baseProfile(goalWeightKg = 80.0)
        val badges = BadgeEngine.evaluate(entries, profile)
        assertTrue(Badge.GOAL_CRUSHER in badges)
    }

    // ── COMEBACK ─────────────────────────────────────────────────────────────

    @Test
    fun `COMEBACK awarded when logging after a 14 day gap`() {
        val entries = listOf(entryOnDay(20), entryOnDay(0))
        val badges = BadgeEngine.evaluate(entries, baseProfile())
        assertTrue(Badge.COMEBACK in badges)
    }

    @Test
    fun `COMEBACK not awarded when gap is less than 14 days`() {
        val entries = listOf(entryOnDay(10), entryOnDay(0))
        val badges = BadgeEngine.evaluate(entries, baseProfile())
        assertFalse(Badge.COMEBACK in badges)
    }

    @Test
    fun `COMEBACK not awarded with only one entry`() {
        val badges = BadgeEngine.evaluate(listOf(entryOnDay(0)), baseProfile())
        assertFalse(Badge.COMEBACK in badges)
    }

    // ── STEADY_STATE ─────────────────────────────────────────────────────────

    @Test
    fun `STEADY_STATE awarded after 30 days in maintenance mode`() {
        val activatedAt = LocalDate.now().minusDays(30).toEpochDay() * 86_400_000L
        val entries = (0 until 30).map { entryOnDay(it, weightKg = 80.0) }
        val profile = baseProfile(maintenanceMode = true, maintenanceModeActivatedAt = activatedAt)
        val badges = BadgeEngine.evaluate(entries, profile)
        assertTrue(Badge.STEADY_STATE in badges)
    }

    @Test
    fun `STEADY_STATE not awarded when maintenance mode not active`() {
        val entries = (0 until 30).map { entryOnDay(it) }
        val profile = baseProfile(maintenanceMode = false)
        val badges = BadgeEngine.evaluate(entries, profile)
        assertFalse(Badge.STEADY_STATE in badges)
    }

    @Test
    fun `STEADY_STATE not awarded when fewer than 30 days in maintenance`() {
        val activatedAt = LocalDate.now().minusDays(15).toEpochDay() * 86_400_000L
        val entries = (0 until 15).map { entryOnDay(it) }
        val profile = baseProfile(maintenanceMode = true, maintenanceModeActivatedAt = activatedAt)
        val badges = BadgeEngine.evaluate(entries, profile)
        assertFalse(Badge.STEADY_STATE in badges)
    }

    // ── Idempotency ───────────────────────────────────────────────────────────

    @Test
    fun `evaluate is idempotent - same input always returns same output`() {
        val entries = consecutiveDays(7)
        val profile = baseProfile()
        val first = BadgeEngine.evaluate(entries, profile)
        val second = BadgeEngine.evaluate(entries, profile)
        assertEquals(first, second)
    }
}
