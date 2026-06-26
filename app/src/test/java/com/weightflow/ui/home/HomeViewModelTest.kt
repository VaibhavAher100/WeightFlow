package com.weightflow.ui.home

import app.cash.turbine.test
import com.weightflow.domain.Badge
import com.weightflow.domain.BadgeObserver
import com.weightflow.domain.GoalState
import com.weightflow.domain.HomeData
import com.weightflow.domain.HomeDataAggregator
import com.weightflow.domain.UserProfile
import com.weightflow.domain.WeightEntry
import com.weightflow.domain.WeightUnit
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableSharedFlow
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
private val EN_STRINGS = HomeStrings(
    locale = java.util.Locale.ENGLISH,
    kgSuffix = "kg", lbsSuffix = "lbs", stSuffix = "st", lbSuffix = "lb",
    today = "Today", yesterday = "Yesterday",
)

@OptIn(ExperimentalCoroutinesApi::class)
class HomeViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private val badgeObserver: BadgeObserver = mockk(relaxed = true)
    private val newlyUnlockedFlow = MutableSharedFlow<Set<Badge>>(replay = 0)
    private val allEarnedFlow = MutableStateFlow<Set<Badge>>(emptySet())

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        every { badgeObserver.newlyUnlockedBadges } returns newlyUnlockedFlow
        every { badgeObserver.allEarnedBadges } returns allEarnedFlow
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

    private fun makeViewModel(aggregator: HomeDataAggregator = fakeAggregator()): HomeViewModel =
        HomeViewModel(aggregator, badgeObserver).apply { setStrings(EN_STRINGS) }

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
        val vm = HomeViewModel(aggregator, badgeObserver)
        assertEquals(HomeUiState.Loading, vm.uiState.value)
    }

    // ── Empty state ───────────────────────────────────────────────────────────

    @Test
    fun `emits Empty state when no entries exist`() = runTest {
        val vm = makeViewModel(fakeAggregator(entries = emptyList()))
        vm.uiState.test {
            val state = awaitRealState()
            assertTrue("Expected Empty, got $state", state is HomeUiState.Empty)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `Empty state has null goal display when profile has no goal`() = runTest {
        val profile = baseProfile(goalWeightKg = null)
        val vm = makeViewModel(fakeAggregator(profile = profile))
        vm.uiState.test {
            val state = awaitRealState() as HomeUiState.Empty
            assertEquals(null, state.goalWeightDisplay)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `Empty state has goal display when profile has a goal in kg`() = runTest {
        val profile = baseProfile(goalWeightKg = 75.0)
        val vm = makeViewModel(fakeAggregator(profile = profile))
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
        val vm = makeViewModel(fakeAggregator(entries = listOf(entry)))
        vm.uiState.test {
            val state = awaitRealState()
            assertTrue("Expected HasData, got $state", state is HomeUiState.HasData)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `latest weight displayed as kg with one decimal`() = runTest {
        val entry = entryAt(0, 80.5)
        val vm = makeViewModel(fakeAggregator(entries = listOf(entry), unit = WeightUnit.KG))
        vm.uiState.test {
            val state = awaitRealState() as HomeUiState.HasData
            assertEquals("80.5 kg", state.latestWeightDisplay)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `latest weight converted to lbs when unit is LBS`() = runTest {
        val entry = entryAt(0, 100.0)
        val vm = makeViewModel(fakeAggregator(entries = listOf(entry), unit = WeightUnit.LBS))
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
        val vm = makeViewModel(fakeAggregator(entries = listOf(entry), unit = WeightUnit.ST))
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
        val vm = makeViewModel(fakeAggregator(entries = entries))
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
        val vm = makeViewModel(fakeAggregator(entries = entries))
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
        val vm = HomeViewModel(aggregator, badgeObserver).apply { setStrings(EN_STRINGS) }

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

    // ── Goal state ────────────────────────────────────────────────────────────

    @Test
    fun `HasData goalState is Active when goal set and not reached`() = runTest {
        val profile = baseProfile(goalWeightKg = 75.0) // achievedAt = null → Active
        val entry = entryAt(0, 80.0) // heavier than goal, not reached
        val vm = makeViewModel(fakeAggregator(entries = listOf(entry), profile = profile))
        vm.uiState.test {
            val state = awaitRealState() as HomeUiState.HasData
            assertTrue("Expected GoalState.Active, got ${state.goalState}", state.goalState is GoalState.Active)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `HasData goalState is Achieved when goal reached`() = runTest {
        val profile = baseProfile(goalWeightKg = 75.0).copy(achievedAt = java.time.LocalDate.now())
        val entry = entryAt(0, 75.0)
        val vm = makeViewModel(fakeAggregator(entries = listOf(entry), profile = profile))
        vm.uiState.test {
            val state = awaitRealState() as HomeUiState.HasData
            assertTrue("Expected GoalState.Achieved, got ${state.goalState}", state.goalState is GoalState.Achieved)
            cancelAndIgnoreRemainingEvents()
        }
    }

    // ── Badge events ──────────────────────────────────────────────────────────

    @Test
    fun `badgeEvents emits when observer has newly unlocked badges`() = runTest {
        val vm = makeViewModel()
        vm.badgeEvents.test {
            advanceUntilIdle() // let init coroutine subscribe
            newlyUnlockedFlow.emit(setOf(Badge.FIRST_WEIGH_IN))
            advanceUntilIdle()
            val emitted = awaitItem()
            assertEquals(setOf(Badge.FIRST_WEIGH_IN), emitted)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `badgeEvents does not emit when newly unlocked set is empty`() = runTest {
        val vm = makeViewModel()
        vm.badgeEvents.test {
            advanceUntilIdle() // let init coroutine subscribe
            newlyUnlockedFlow.emit(emptySet())
            advanceUntilIdle()
            expectNoEvents()
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `onBadgeShown calls markSeen`() = runTest {
        val vm = makeViewModel()
        val badges = setOf(Badge.FIRST_WEIGH_IN)
        vm.onBadgeShown(badges)
        advanceUntilIdle()
        coVerify { badgeObserver.markSeen(badges) }
    }
}
