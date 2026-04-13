package com.weightflow.ui.home

import app.cash.turbine.test
import com.weightflow.domain.HomeData
import com.weightflow.domain.HomeDataAggregator
import com.weightflow.domain.UserProfile
import com.weightflow.domain.WeightEntry
import com.weightflow.domain.WeightUnit
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.time.LocalDate

/**
 * TDD: These tests were written BEFORE HomeViewModel exists.
 * Run them — they will all fail. Then implement HomeViewModel + supporting classes to make them pass.
 *
 * Architecture per RFC #27 (StoredWeight + HomeUiStateMapper) and RFC #29 (HomeDataAggregator).
 *
 * NOTE on stateIn / WhileSubscribed behaviour in tests:
 *   vm.uiState collects two items under test:
 *     1. HomeUiState.Loading  — the stateIn initialValue (always first)
 *     2. The real state       — after the upstream coroutine runs
 *   Use awaitRealState() to skip the Loading item and get the meaningful state.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class HomeViewModelTest {

    private val testDispatcher = StandardTestDispatcher()

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    // ── Helpers ──────────────────────────────────────────────────────────────

    private fun fakeAggregator(
        entries: List<WeightEntry> = emptyList(),
        profile: UserProfile? = null,
        unit: WeightUnit = WeightUnit.KG,
    ): HomeDataAggregator = object : HomeDataAggregator {
        override val homeData = MutableStateFlow(HomeData(entries, profile, unit))
    }

    private fun entryAt(daysAgo: Int, weightKg: Double): WeightEntry = WeightEntry(
        id = daysAgo.toLong(),
        timestamp = LocalDate.now().minusDays(daysAgo.toLong()).toEpochDay() * 86_400_000L,
        weightKg = weightKg,
        note = "",
    )

    private fun baseProfile(goalWeightKg: Double? = 75.0) = UserProfile(
        id = 1,
        displayName = "Tester",
        goalWeightKg = goalWeightKg,
        targetDate = null,
        heightCm = null,
        maintenanceMode = false,
        maintenanceRangeKg = 2.0,
        maintenanceModeActivatedAt = null,
    )

    /**
     * Skips the initial Loading item and returns the first real HomeUiState.
     * stateIn with WhileSubscribed always emits Loading as the very first item
     * before the upstream mapping coroutine runs.
     */
    private suspend fun app.cash.turbine.TurbineTestContext<HomeUiState>.awaitRealState(): HomeUiState {
        val first = awaitItem()
        return if (first == HomeUiState.Loading) awaitItem() else first
    }

    // ── Initial state ─────────────────────────────────────────────────────────

    @Test
    fun `initial state is Loading before first emission`() = runTest {
        val aggregator = object : HomeDataAggregator {
            override val homeData = flow<HomeData> { /* never emits */ }
        }
        val vm = HomeViewModel(aggregator)
        assertEquals(HomeUiState.Loading, vm.uiState.value)
    }

    // ── Empty state ───────────────────────────────────────────────────────────

    @Test
    fun `emits Empty state when no entries exist`() = runTest {
        val vm = HomeViewModel(fakeAggregator(entries = emptyList()))
        vm.uiState.test {
            val state = awaitRealState()
            assertTrue("Expected Empty, got $state", state is HomeUiState.Empty)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `Empty state has null goal display when profile has no goal`() = runTest {
        val profile = baseProfile(goalWeightKg = null)
        val vm = HomeViewModel(fakeAggregator(profile = profile))
        vm.uiState.test {
            val state = awaitRealState() as HomeUiState.Empty
            assertEquals(null, state.goalWeightDisplay)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `Empty state has goal display when profile has a goal in kg`() = runTest {
        val profile = baseProfile(goalWeightKg = 75.0)
        val vm = HomeViewModel(fakeAggregator(profile = profile))
        vm.uiState.test {
            val state = awaitRealState() as HomeUiState.Empty
            assertNotNull(state.goalWeightDisplay)
            assertTrue(
                "Expected '75.0' in goal display, got '${state.goalWeightDisplay}'",
                state.goalWeightDisplay!!.contains("75.0"),
            )
            cancelAndIgnoreRemainingEvents()
        }
    }

    // ── HasData state ─────────────────────────────────────────────────────────

    @Test
    fun `emits HasData when entries exist`() = runTest {
        val entry = entryAt(0, 80.0)
        val vm = HomeViewModel(fakeAggregator(entries = listOf(entry)))
        vm.uiState.test {
            val state = awaitRealState()
            assertTrue("Expected HasData, got $state", state is HomeUiState.HasData)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `latest weight displayed as kg with one decimal`() = runTest {
        val entry = entryAt(0, 80.5)
        val vm = HomeViewModel(fakeAggregator(entries = listOf(entry), unit = WeightUnit.KG))
        vm.uiState.test {
            val state = awaitRealState() as HomeUiState.HasData
            assertEquals("80.5 kg", state.latestWeightDisplay)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `latest weight converted to lbs when unit is LBS`() = runTest {
        val entry = entryAt(0, 100.0)
        val vm = HomeViewModel(fakeAggregator(entries = listOf(entry), unit = WeightUnit.LBS))
        vm.uiState.test {
            val state = awaitRealState() as HomeUiState.HasData
            // 100 kg * 2.20462 = 220.462 lbs → "220.5 lbs"
            assertTrue(
                "Expected lbs display, got '${state.latestWeightDisplay}'",
                state.latestWeightDisplay.contains("lbs"),
            )
            assertTrue(
                "Expected ~220 lbs, got '${state.latestWeightDisplay}'",
                state.latestWeightDisplay.startsWith("220"),
            )
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `latest weight converted to stones when unit is ST`() = runTest {
        val entry = entryAt(0, 80.0)
        val vm = HomeViewModel(fakeAggregator(entries = listOf(entry), unit = WeightUnit.ST))
        vm.uiState.test {
            val state = awaitRealState() as HomeUiState.HasData
            // 80 kg ≈ 12st 8lb
            assertTrue(
                "Expected stones display, got '${state.latestWeightDisplay}'",
                state.latestWeightDisplay.contains("st"),
            )
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `recent entries list contains at most 5 items`() = runTest {
        val entries = (1..10).map { i -> entryAt(i, 80.0 - i) }
        val vm = HomeViewModel(fakeAggregator(entries = entries))
        vm.uiState.test {
            val state = awaitRealState() as HomeUiState.HasData
            assertTrue(
                "Expected at most 5 recent entries, got ${state.recentEntries.size}",
                state.recentEntries.size <= 5,
            )
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `recent entries are sorted newest-first`() = runTest {
        val entries = listOf(
            entryAt(0, 80.0),
            entryAt(1, 81.0),
            entryAt(2, 82.0),
        )
        val vm = HomeViewModel(fakeAggregator(entries = entries))
        vm.uiState.test {
            val state = awaitRealState() as HomeUiState.HasData
            assertTrue(
                "Expected newest first",
                state.recentEntries[0].timestamp >= state.recentEntries[1].timestamp,
            )
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `uiState updates reactively when aggregator emits new data`() = runTest {
        val dataFlow = MutableStateFlow(HomeData(emptyList(), null, WeightUnit.KG))
        val aggregator = object : HomeDataAggregator {
            override val homeData = dataFlow
        }
        val vm = HomeViewModel(aggregator)

        vm.uiState.test {
            awaitRealState() // Empty state

            val entry = entryAt(0, 75.0)
            dataFlow.value = HomeData(listOf(entry), null, WeightUnit.KG)
            advanceUntilIdle()

            val updated = awaitItem()
            assertTrue("Expected HasData after update, got $updated", updated is HomeUiState.HasData)
            cancelAndIgnoreRemainingEvents()
        }
    }
}
