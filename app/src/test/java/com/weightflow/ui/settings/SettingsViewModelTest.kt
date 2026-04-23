package com.weightflow.ui.settings

import app.cash.turbine.test
import com.weightflow.data.UserPrefsDataStore
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
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class SettingsViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private val userPrefsDataStore: UserPrefsDataStore = mockk()
    private val paletteFlow = MutableStateFlow("lime")
    private val unitFlow = MutableStateFlow(WeightUnit.KG)

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        every { userPrefsDataStore.themePalette } returns paletteFlow
        every { userPrefsDataStore.weightUnit } returns unitFlow
        coEvery { userPrefsDataStore.setThemePalette(any()) } returns Unit
        coEvery { userPrefsDataStore.setWeightUnit(any()) } returns Unit
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun makeViewModel() = SettingsViewModel(userPrefsDataStore)

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
}
