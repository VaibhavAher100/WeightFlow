package com.weightflow.ui.onboarding

import app.cash.turbine.test
import com.weightflow.data.UserPrefsDataStore
import com.weightflow.data.UserProfileRepository
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
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

/**
 * TDD: Written BEFORE OnboardingViewModel exists.
 *
 * 4-step onboarding: AgeGate → Unit → CurrentWeight → Goal
 * COPPA: user must confirm they are 13+. No data is saved if user declines.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class OnboardingViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private val userProfileRepository: UserProfileRepository = mockk()
    private val userPrefsDataStore: UserPrefsDataStore = mockk()

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        coEvery { userProfileRepository.saveProfile(any()) } returns Unit
        coEvery { userPrefsDataStore.setWeightUnit(any()) } returns Unit
        coEvery { userPrefsDataStore.setOnboardingComplete() } returns Unit
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun makeViewModel() = OnboardingViewModel(userProfileRepository, userPrefsDataStore)

    // ── Initial state ─────────────────────────────────────────────────────────

    @Test
    fun `initial step is AgeGate`() = runTest {
        val vm = makeViewModel()
        assertEquals(OnboardingStep.AGE_GATE, vm.uiState.value.currentStep)
    }

    // ── Age gate (COPPA / DPDP — 18+ year-of-birth picker) ───────────────────

    @Test
    fun `initial birth year input is empty`() = runTest {
        val vm = makeViewModel()
        assertEquals("", vm.uiState.value.birthYearInput)
    }

    @Test
    fun `entering birth year with age 18+ sets canAdvance true`() = runTest {
        val vm = makeViewModel()
        vm.onBirthYearInput("2000") // 26 years old in 2026
        advanceUntilIdle()
        assertTrue(vm.uiState.value.canAdvance)
    }

    @Test
    fun `entering birth year with age under 18 keeps canAdvance false`() = runTest {
        val vm = makeViewModel()
        vm.onBirthYearInput("2015") // 11 years old in 2026
        advanceUntilIdle()
        assertFalse(vm.uiState.value.canAdvance)
    }

    @Test
    fun `entering invalid text keeps canAdvance false`() = runTest {
        val vm = makeViewModel()
        vm.onBirthYearInput("abc")
        advanceUntilIdle()
        assertFalse(vm.uiState.value.canAdvance)
    }

    @Test
    fun `advancing from AgeGate with age under 18 stays on AgeGate`() = runTest {
        val vm = makeViewModel()
        vm.onBirthYearInput("2015")
        vm.onNextStep()
        advanceUntilIdle()
        assertEquals(OnboardingStep.AGE_GATE, vm.uiState.value.currentStep)
    }

    @Test
    fun `advancing from AgeGate with valid age moves to Unit step`() = runTest {
        val vm = makeViewModel()
        vm.onBirthYearInput("2000")
        vm.onNextStep()
        advanceUntilIdle()
        assertEquals(OnboardingStep.UNIT, vm.uiState.value.currentStep)
    }

    // ── Unit step ─────────────────────────────────────────────────────────────

    @Test
    fun `default unit selection is KG`() = runTest {
        val vm = makeViewModel()
        assertEquals(WeightUnit.KG, vm.uiState.value.selectedUnit)
    }

    @Test
    fun `selecting LBS updates unit in state`() = runTest {
        val vm = makeViewModel()
        vm.onUnitSelected(WeightUnit.LBS)
        advanceUntilIdle()
        assertEquals(WeightUnit.LBS, vm.uiState.value.selectedUnit)
    }

    @Test
    fun `advancing from Unit step moves to CurrentWeight`() = runTest {
        val vm = makeViewModel()
        vm.onBirthYearInput("2000")
        vm.onNextStep()
        advanceUntilIdle()
        vm.onNextStep()
        advanceUntilIdle()
        assertEquals(OnboardingStep.CURRENT_WEIGHT, vm.uiState.value.currentStep)
    }

    // ── Current weight step ───────────────────────────────────────────────────

    @Test
    fun `initial weight input is empty`() = runTest {
        val vm = makeViewModel()
        assertEquals("", vm.uiState.value.weightInput)
    }

    @Test
    fun `valid weight input makes canAdvance true on CurrentWeight step`() = runTest {
        val vm = makeViewModel()
        navigateTo(vm, OnboardingStep.CURRENT_WEIGHT)
        vm.onWeightInput("80.0")
        advanceUntilIdle()
        assertTrue(vm.uiState.value.canAdvance)
    }

    @Test
    fun `empty weight input keeps canAdvance false on CurrentWeight step`() = runTest {
        val vm = makeViewModel()
        navigateTo(vm, OnboardingStep.CURRENT_WEIGHT)
        vm.onWeightInput("")
        advanceUntilIdle()
        assertFalse(vm.uiState.value.canAdvance)
    }

    @Test
    fun `advancing from CurrentWeight with valid input moves to Goal`() = runTest {
        val vm = makeViewModel()
        navigateTo(vm, OnboardingStep.CURRENT_WEIGHT)
        vm.onWeightInput("80.0")
        advanceUntilIdle()
        vm.onNextStep()
        advanceUntilIdle()
        assertEquals(OnboardingStep.GOAL, vm.uiState.value.currentStep)
    }

    // ── Goal step ─────────────────────────────────────────────────────────────

    @Test
    fun `goal weight input is optional — can advance with empty goal`() = runTest {
        val vm = makeViewModel()
        navigateTo(vm, OnboardingStep.GOAL)
        vm.onGoalInput("") // skip goal
        advanceUntilIdle()
        assertTrue(vm.uiState.value.canAdvance)
    }

    // ── Completion ────────────────────────────────────────────────────────────

    @Test
    fun `completing onboarding calls setOnboardingComplete`() = runTest {
        val vm = makeViewModel()
        navigateTo(vm, OnboardingStep.GOAL)
        vm.onWeightInput("80.0") // current weight carried through
        vm.onComplete()
        advanceUntilIdle()
        coVerify { userPrefsDataStore.setOnboardingComplete() }
    }

    @Test
    fun `completing onboarding saves unit preference`() = runTest {
        val vm = makeViewModel()
        vm.onUnitSelected(WeightUnit.LBS)
        navigateTo(vm, OnboardingStep.GOAL)
        vm.onComplete()
        advanceUntilIdle()
        coVerify { userPrefsDataStore.setWeightUnit(WeightUnit.LBS) }
    }

    @Test
    fun `completing onboarding emits Finished event`() = runTest {
        val vm = makeViewModel()
        navigateTo(vm, OnboardingStep.GOAL)
        vm.events.test {
            vm.onComplete()
            advanceUntilIdle()
            val event = awaitItem()
            assertTrue("Expected Finished, got $event", event is OnboardingEvent.Finished)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `back navigation from Unit returns to AgeGate`() = runTest {
        val vm = makeViewModel()
        vm.onBirthYearInput("2000")
        vm.onNextStep()
        advanceUntilIdle()
        vm.onBack()
        advanceUntilIdle()
        assertEquals(OnboardingStep.AGE_GATE, vm.uiState.value.currentStep)
    }

    // ── Helper ────────────────────────────────────────────────────────────────

    // onNextStep() is synchronous — no advanceUntilIdle needed inside the loop
    private fun navigateTo(vm: OnboardingViewModel, target: OnboardingStep) {
        val steps = OnboardingStep.entries
        val targetIndex = steps.indexOf(target)
        if (targetIndex == 0) return

        vm.onBirthYearInput("2000") // age 26 in 2026 — always valid
        vm.onWeightInput("80.0")
        for (i in 0 until targetIndex) {
            vm.onNextStep()
        }
    }

    @Test
    fun `trying to advance from AgeGate with underage birth year emits AgeDeclined`() = runTest {
        val vm = makeViewModel()
        vm.onBirthYearInput("2015") // age 11
        vm.events.test {
            vm.onNextStep()
            advanceUntilIdle()
            val event = awaitItem()
            assertTrue("Expected AgeDeclined, got $event", event is OnboardingEvent.AgeDeclined)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `trying to advance from AgeGate with empty input emits AgeDeclined`() = runTest {
        val vm = makeViewModel()
        vm.events.test {
            vm.onNextStep()
            advanceUntilIdle()
            val event = awaitItem()
            assertTrue("Expected AgeDeclined, got $event", event is OnboardingEvent.AgeDeclined)
            cancelAndIgnoreRemainingEvents()
        }
    }
}
