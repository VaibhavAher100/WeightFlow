package com.weightflow.ui.settings

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

@OptIn(ExperimentalCoroutinesApi::class)
class SettingsViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private val userPrefsDataStore: UserPrefsDataStore = mockk()
    private val weightRepository: WeightRepository = mockk()
    private val paletteFlow = MutableStateFlow("lime")
    private val unitFlow = MutableStateFlow(WeightUnit.KG)
    private val entriesFlow = MutableStateFlow<List<WeightEntry>>(emptyList())

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        every { userPrefsDataStore.themePalette } returns paletteFlow
        every { userPrefsDataStore.weightUnit } returns unitFlow
        coEvery { userPrefsDataStore.setThemePalette(any()) } returns Unit
        coEvery { userPrefsDataStore.setWeightUnit(any()) } returns Unit
        every { weightRepository.getEntriesOldestFirst() } returns entriesFlow
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun makeViewModel() = SettingsViewModel(userPrefsDataStore, weightRepository)

    @Test
    fun `initial state defaults to lime palette`() = runTest {
        val vm = makeViewModel()
        vm.uiState.test {
            advanceUntilIdle()
            val state = expectMostRecentItem()
            assertEquals("lime", state.themePalette)
        }
    }

    @Test
    fun `onThemeSelected updates themePalette in uiState`() = runTest {
        val vm = makeViewModel()
        vm.onThemeSelected("ocean")
        advanceUntilIdle()
        coVerify { userPrefsDataStore.setThemePalette("ocean") }
    }

    @Test
    fun `uiState reflects weightUnit from DataStore`() = runTest {
        unitFlow.value = WeightUnit.LBS
        val vm = makeViewModel()
        vm.uiState.test {
            advanceUntilIdle()
            val state = expectMostRecentItem()
            assertEquals(WeightUnit.LBS, state.weightUnit)
        }
    }

    @Test
    fun `onExportCsv emits ExportCsvReady with formatted csv content`() = runTest {
        val epochDayMs = 86_400_000L * 19723L // 2024-01-01
        entriesFlow.value = listOf(
            WeightEntry(id = 1L, timestamp = epochDayMs, weightKg = 80.0, note = ""),
            WeightEntry(id = 2L, timestamp = epochDayMs + 86_400_000L, weightKg = 79.5, note = ""),
        )
        val vm = makeViewModel()
        vm.events.test {
            vm.onExportCsv()
            advanceUntilIdle()
            val event = awaitItem()
            assertTrue(event is SettingsEvent.ExportCsvReady)
            val csv = (event as SettingsEvent.ExportCsvReady).csvContent
            assertTrue(csv.startsWith("date,weight_kg"))
            assertTrue(csv.contains("80.0"))
            assertTrue(csv.contains("79.5"))
        }
    }

    @Test
    fun `onExportCsv with empty entries emits ExportCsvReady with header only`() = runTest {
        entriesFlow.value = emptyList()
        val vm = makeViewModel()
        vm.events.test {
            vm.onExportCsv()
            advanceUntilIdle()
            val event = awaitItem()
            assertTrue(event is SettingsEvent.ExportCsvReady)
            val csv = (event as SettingsEvent.ExportCsvReady).csvContent
            assertEquals("date,weight_kg", csv.trim())
        }
    }
}
