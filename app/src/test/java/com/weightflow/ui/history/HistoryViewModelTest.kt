package com.weightflow.ui.history

import app.cash.turbine.test
import com.weightflow.data.UserPrefsDataStore
import com.weightflow.data.WeightRepository
import com.weightflow.domain.WeightEntry
import com.weightflow.domain.WeightUnit
import io.mockk.coEvery
import io.mockk.coVerify
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
 * TDD: Written BEFORE HistoryViewModel exists.
 *
 * HistoryViewModel drives the History tab: full paginated list with delete support.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class HistoryViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private val weightRepository: WeightRepository = mockk()
    private val userPrefsDataStore: UserPrefsDataStore = mockk()
    private val entriesFlow = MutableStateFlow<List<WeightEntry>>(emptyList())
    private val unitFlow = MutableStateFlow(WeightUnit.KG)

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        every { weightRepository.getEntriesNewestFirst() } returns entriesFlow
        every { userPrefsDataStore.weightUnit } returns unitFlow
        coEvery { weightRepository.removeEntry(any()) } returns 1
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun makeViewModel() = HistoryViewModel(weightRepository, userPrefsDataStore)

    private fun entryAt(daysAgo: Int, weightKg: Double) = WeightEntry(
        id = daysAgo.toLong(),
        timestamp = LocalDate.now().minusDays(daysAgo.toLong()).toEpochDay() * 86_400_000L,
        weightKg = weightKg,
        note = "",
    )

    private suspend fun app.cash.turbine.TurbineTestContext<HistoryUiState>.awaitRealState(): HistoryUiState {
        val first = awaitItem()
        return if (first == HistoryUiState.Loading) awaitItem() else first
    }

    // ── Initial state ─────────────────────────────────────────────────────────

    @Test
    fun `initial state is Loading`() = runTest {
        val vm = makeViewModel()
        assertEquals(HistoryUiState.Loading, vm.uiState.value)
    }

    // ── Empty state ───────────────────────────────────────────────────────────

    @Test
    fun `emits Empty when no entries`() = runTest {
        entriesFlow.value = emptyList()
        val vm = makeViewModel()
        vm.uiState.test {
            val state = awaitRealState()
            assertTrue("Expected Empty, got $state", state is HistoryUiState.Empty)
            cancelAndIgnoreRemainingEvents()
        }
    }

    // ── HasData state ─────────────────────────────────────────────────────────

    @Test
    fun `emits HasData with converted weights`() = runTest {
        entriesFlow.value = listOf(entryAt(0, 80.0))
        unitFlow.value = WeightUnit.KG
        val vm = makeViewModel()
        vm.uiState.test {
            val state = awaitRealState() as HistoryUiState.HasData
            assertEquals("80.0 kg", state.entries.first().weightDisplay)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `entries are newest-first`() = runTest {
        entriesFlow.value = listOf(entryAt(0, 80.0), entryAt(1, 81.0), entryAt(2, 82.0))
        val vm = makeViewModel()
        vm.uiState.test {
            val state = awaitRealState() as HistoryUiState.HasData
            assertTrue(
                "Expected newest first",
                state.entries[0].timestamp >= state.entries[1].timestamp,
            )
            cancelAndIgnoreRemainingEvents()
        }
    }

    // ── Delete ────────────────────────────────────────────────────────────────

    @Test
    fun `onDelete calls removeEntry with correct id`() = runTest {
        entriesFlow.value = listOf(entryAt(0, 80.0))
        val vm = makeViewModel()
        advanceUntilIdle()
        vm.onDelete(entryId = 0L)
        advanceUntilIdle()
        coVerify { weightRepository.removeEntry(id = 0L) }
    }
}
