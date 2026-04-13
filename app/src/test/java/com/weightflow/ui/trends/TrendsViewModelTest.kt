package com.weightflow.ui.trends

import app.cash.turbine.test
import com.weightflow.data.UserPrefsDataStore
import com.weightflow.data.WeightRepository
import com.weightflow.domain.WeightEntry
import com.weightflow.domain.WeightUnit
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.time.LocalDate

/**
 * TDD: Written BEFORE TrendsViewModel exists. All tests should fail until the implementation is added.
 *
 * TrendsViewModel drives the Trends tab: line/area chart with time-range filter.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class TrendsViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private val weightRepository: WeightRepository = mockk()
    private val userPrefsDataStore: UserPrefsDataStore = mockk()
    private val entriesFlow = MutableStateFlow<List<WeightEntry>>(emptyList())
    private val unitFlow = MutableStateFlow(WeightUnit.KG)

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        every { weightRepository.getEntriesOldestFirst() } returns entriesFlow
        every { userPrefsDataStore.weightUnit } returns unitFlow
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun makeViewModel() = TrendsViewModel(weightRepository, userPrefsDataStore)

    private fun entryAt(daysAgo: Int, weightKg: Double) = WeightEntry(
        id = daysAgo.toLong(),
        timestamp = LocalDate.now().minusDays(daysAgo.toLong()).toEpochDay() * 86_400_000L,
        weightKg = weightKg,
        note = "",
    )

    private suspend fun app.cash.turbine.TurbineTestContext<TrendsUiState>.awaitRealState(): TrendsUiState {
        val first = awaitItem()
        return if (first == TrendsUiState.Loading) awaitItem() else first
    }

    // ── Initial state ─────────────────────────────────────────────────────────

    @Test
    fun `initial state is Loading`() = runTest {
        val vm = makeViewModel()
        assertEquals(TrendsUiState.Loading, vm.uiState.value)
    }

    @Test
    fun `default time range is 30 days`() = runTest {
        val vm = makeViewModel()
        advanceUntilIdle()
        assertEquals(TrendsTimeRange.DAYS_30, vm.selectedRange.value)
    }

    // ── Empty state ───────────────────────────────────────────────────────────

    @Test
    fun `emits Empty when no entries exist`() = runTest {
        entriesFlow.value = emptyList()
        val vm = makeViewModel()
        vm.uiState.test {
            val state = awaitRealState()
            assertTrue("Expected Empty, got $state", state is TrendsUiState.Empty)
            cancelAndIgnoreRemainingEvents()
        }
    }

    // ── HasData state ─────────────────────────────────────────────────────────

    @Test
    fun `emits HasData when entries exist`() = runTest {
        entriesFlow.value = listOf(entryAt(1, 80.0), entryAt(0, 79.5))
        val vm = makeViewModel()
        vm.uiState.test {
            val state = awaitRealState()
            assertTrue("Expected HasData, got $state", state is TrendsUiState.HasData)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `chart points are sorted oldest-first`() = runTest {
        entriesFlow.value = listOf(entryAt(2, 82.0), entryAt(1, 81.0), entryAt(0, 80.0))
        val vm = makeViewModel()
        vm.uiState.test {
            val state = awaitRealState() as TrendsUiState.HasData
            val timestamps = state.chartPoints.map { it.timestamp }
            assertEquals(timestamps, timestamps.sorted())
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `chart points use display weight in user unit`() = runTest {
        unitFlow.value = WeightUnit.KG
        entriesFlow.value = listOf(entryAt(0, 80.0))
        val vm = makeViewModel()
        vm.uiState.test {
            val state = awaitRealState() as TrendsUiState.HasData
            assertEquals(80.0f, state.chartPoints.first().displayValue, 0.01f)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `chart points convert to lbs when unit is LBS`() = runTest {
        unitFlow.value = WeightUnit.LBS
        entriesFlow.value = listOf(entryAt(0, 100.0))
        val vm = makeViewModel()
        vm.uiState.test {
            val state = awaitRealState() as TrendsUiState.HasData
            // 100 kg = 220.46 lbs
            assertTrue(
                "Expected ~220 lbs, got ${state.chartPoints.first().displayValue}",
                state.chartPoints.first().displayValue in 219f..222f,
            )
            cancelAndIgnoreRemainingEvents()
        }
    }

    // ── Time range filter ─────────────────────────────────────────────────────

    @Test
    fun `selecting 7D filters to last 7 days`() = runTest {
        entriesFlow.value = listOf(
            entryAt(10, 82.0), // outside 7D
            entryAt(5, 81.0),  // inside 7D
            entryAt(0, 80.0),  // inside 7D
        )
        val vm = makeViewModel()
        vm.onRangeSelected(TrendsTimeRange.DAYS_7)
        advanceUntilIdle()
        vm.uiState.test {
            val state = awaitRealState() as TrendsUiState.HasData
            assertEquals(2, state.chartPoints.size)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `selecting All shows all entries`() = runTest {
        entriesFlow.value = (1..20).map { entryAt(it * 10, 80.0) }
        val vm = makeViewModel()
        vm.onRangeSelected(TrendsTimeRange.ALL)
        advanceUntilIdle()
        vm.uiState.test {
            val state = awaitRealState() as TrendsUiState.HasData
            assertEquals(20, state.chartPoints.size)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `range selection is reflected in selectedRange`() = runTest {
        val vm = makeViewModel()
        vm.onRangeSelected(TrendsTimeRange.DAYS_90)
        advanceUntilIdle()
        assertEquals(TrendsTimeRange.DAYS_90, vm.selectedRange.value)
    }
}
