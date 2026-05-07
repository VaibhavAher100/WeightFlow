package com.weightflow.ui.home

import com.weightflow.domain.HomeData
import com.weightflow.domain.UserProfile
import com.weightflow.domain.WeightEntry
import com.weightflow.domain.WeightUnit
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDate
import java.time.ZoneId

class HomeUiStateMapperTest {

    private fun profile(goalWeightKg: Double) = UserProfile(
        id = 1,
        displayName = "Tester",
        goalWeightKg = goalWeightKg,
        targetDate = null,
        heightCm = null,
        maintenanceMode = false,
        maintenanceRangeKg = 2.0,
        maintenanceModeActivatedAt = null,
    )

    private fun entryDaysAgo(daysAgo: Long, weightKg: Double) = WeightEntry(
        id = daysAgo,
        timestamp = LocalDate.now().minusDays(daysAgo)
            .atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli(),
        weightKg = weightKg,
        note = "",
    )

    // ── goalProgress: gain goal ───────────────────────────────────────────────

    @Test
    fun `gain goal - regression below start yields zero progress`() {
        // start=60, goal=70, current=55 → user moved away from goal
        val data = HomeData(
            entries = listOf(
                entryDaysAgo(0, 55.0),  // current (latest first)
                entryDaysAgo(1, 60.0),  // start
            ),
            profile = profile(goalWeightKg = 70.0),
            unit = WeightUnit.KG,
        )
        val state = HomeUiStateMapper.map(data) as HomeUiState.HasData
        assertEquals("regression on gain goal must report 0 progress", 0f, state.goalProgress!!, 0.001f)
    }

    @Test
    fun `gain goal - progress toward goal yields positive progress`() {
        // start=60, goal=70, current=65 → 50% progress
        val data = HomeData(
            entries = listOf(
                entryDaysAgo(0, 65.0),  // current
                entryDaysAgo(1, 60.0),  // start
            ),
            profile = profile(goalWeightKg = 70.0),
            unit = WeightUnit.KG,
        )
        val state = HomeUiStateMapper.map(data) as HomeUiState.HasData
        assertEquals(0.5f, state.goalProgress!!, 0.001f)
    }

    @Test
    fun `loss goal - progress toward goal yields positive progress`() {
        // start=80, goal=70, current=75 → 50% progress
        val data = HomeData(
            entries = listOf(
                entryDaysAgo(0, 75.0),
                entryDaysAgo(1, 80.0),
            ),
            profile = profile(goalWeightKg = 70.0),
            unit = WeightUnit.KG,
        )
        val state = HomeUiStateMapper.map(data) as HomeUiState.HasData
        assertEquals(0.5f, state.goalProgress!!, 0.001f)
    }

    @Test
    fun `loss goal - regression above start yields zero progress`() {
        // start=80, goal=70, current=85 → user moved away from goal
        val data = HomeData(
            entries = listOf(
                entryDaysAgo(0, 85.0),
                entryDaysAgo(1, 80.0),
            ),
            profile = profile(goalWeightKg = 70.0),
            unit = WeightUnit.KG,
        )
        val state = HomeUiStateMapper.map(data) as HomeUiState.HasData
        assertEquals(0f, state.goalProgress!!, 0.001f)
    }

    // ── 7-day average: calendar-based window ─────────────────────────────────

    @Test
    fun `7-day avg includes entry from exactly 6 days ago`() {
        val data = HomeData(
            entries = listOf(
                entryDaysAgo(0, 80.0),
                entryDaysAgo(6, 74.0),  // oldest boundary — must be included
            ),
            profile = null,
            unit = WeightUnit.KG,
        )
        val state = HomeUiStateMapper.map(data) as HomeUiState.HasData
        assertNotNull("entry from 6 days ago must be included in 7-day avg", state.avgDisplay)
    }

    @Test
    fun `7-day avg excludes entry from 8 days ago`() {
        val data = HomeData(
            entries = listOf(
                entryDaysAgo(0, 80.0),
                entryDaysAgo(8, 74.0),  // outside window — only 1 entry in range → avg null
            ),
            profile = null,
            unit = WeightUnit.KG,
        )
        val state = HomeUiStateMapper.map(data) as HomeUiState.HasData
        assertNull("entry from 8 days ago must be excluded from 7-day avg", state.avgDisplay)
    }

    @Test
    fun `7-day avg at midnight boundary includes entry from 7th calendar day`() {
        // Entry timestamped at start-of-day 6 days ago should always be in window
        // regardless of the current time-of-day (tests the calendar-date fix)
        val startOfDay6DaysAgo = LocalDate.now().minusDays(6)
            .atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
        val data = HomeData(
            entries = listOf(
                WeightEntry(id = 1, timestamp = System.currentTimeMillis(), weightKg = 80.0, note = ""),
                WeightEntry(id = 2, timestamp = startOfDay6DaysAgo, weightKg = 74.0, note = ""),
            ),
            profile = null,
            unit = WeightUnit.KG,
        )
        val state = HomeUiStateMapper.map(data) as HomeUiState.HasData
        assertNotNull(
            "start-of-day 6 days ago must be included regardless of current time-of-day",
            state.avgDisplay
        )
    }

    // ── startDisplay ──────────────────────────────────────────────────────────

    @Test
    fun `startDisplay is oldest entry weight when 2 or more entries exist`() {
        // entries newest-first: [current=80.0 kg, oldest=85.0 kg]
        // startDisplay must format the oldest entry (entries.last())
        val data = HomeData(
            entries = listOf(
                entryDaysAgo(0, 80.0),  // current (newest)
                entryDaysAgo(1, 85.0),  // oldest
            ),
            profile = null,
            unit = WeightUnit.KG,
        )
        val state = HomeUiStateMapper.map(data) as HomeUiState.HasData
        assertEquals("85.0 kg", state.startDisplay)
    }

    // ── lostDisplay ───────────────────────────────────────────────────────────

    @Test
    fun `lostDisplay shows minus value when weight decreased`() {
        // start=85.0 kg, current=80.0 kg → diff=5.0 → lostDisplay = "−5.0"
        val data = HomeData(
            entries = listOf(
                entryDaysAgo(0, 80.0),  // current
                entryDaysAgo(1, 85.0),  // start
            ),
            profile = null,
            unit = WeightUnit.KG,
        )
        val state = HomeUiStateMapper.map(data) as HomeUiState.HasData
        assertEquals("−5.0", state.lostDisplay)
    }

    // ── isGoalAchieved ────────────────────────────────────────────────────────

    @Test
    fun `isGoalAchieved is true when current weight is at or below goal`() {
        // profile goal=75.0 kg, current weight=75.0 kg → goalState is Active → isGoalAchieved = true
        val data = HomeData(
            entries = listOf(
                entryDaysAgo(0, 75.0),  // current weight equals goal
                entryDaysAgo(1, 80.0),
            ),
            profile = profile(goalWeightKg = 75.0),
            unit = WeightUnit.KG,
        )
        val state = HomeUiStateMapper.map(data) as HomeUiState.HasData
        assertTrue("isGoalAchieved must be true when current weight <= goal", state.isGoalAchieved)
    }

    // ── sparklinePoints ───────────────────────────────────────────────────────

    @Test
    fun `sparklinePoints contains up to 30 float values oldest first`() {
        // 35 entries newest-first → takeLast(30) keeps entries 6..35 (oldest 30),
        // then reversed() → oldest first; size must be 30
        val entries = (0L until 35L).map { daysAgo -> entryDaysAgo(daysAgo, 70.0 + daysAgo) }
        val data = HomeData(
            entries = entries,
            profile = null,
            unit = WeightUnit.KG,
        )
        val state = HomeUiStateMapper.map(data) as HomeUiState.HasData
        assertEquals("sparklinePoints must be capped at 30 entries", 30, state.sparklinePoints.size)
        // oldest entry (highest daysAgo among retained) should be first
        assertTrue(
            "sparklinePoints must be oldest-first (first value >= last value for descending weight)",
            state.sparklinePoints.first() >= state.sparklinePoints.last()
        )
    }
}
