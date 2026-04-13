package com.weightflow.ui.profile

import app.cash.turbine.test
import com.weightflow.data.UserPrefsDataStore
import com.weightflow.data.UserProfileRepository
import com.weightflow.domain.UserProfile
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
 * TDD: Written BEFORE ProfileViewModel exists.
 *
 * ProfileViewModel drives the Profile tab: display name, goal, unit preference, theme.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class ProfileViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private val userProfileRepository: UserProfileRepository = mockk()
    private val userPrefsDataStore: UserPrefsDataStore = mockk()
    private val profileFlow = MutableStateFlow<UserProfile?>(null)
    private val unitFlow = MutableStateFlow(WeightUnit.KG)

    private fun baseProfile() = UserProfile(
        id = 1,
        displayName = "Alice",
        goalWeightKg = 70.0,
        targetDate = LocalDate.now().plusMonths(3),
        heightCm = 165.0,
        maintenanceMode = false,
        maintenanceRangeKg = 2.0,
        maintenanceModeActivatedAt = null,
    )

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        every { userProfileRepository.getProfile() } returns profileFlow
        every { userPrefsDataStore.weightUnit } returns unitFlow
        coEvery { userProfileRepository.saveProfile(any()) } returns Unit
        coEvery { userPrefsDataStore.setWeightUnit(any()) } returns Unit
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun makeViewModel() = ProfileViewModel(userProfileRepository, userPrefsDataStore)

    private suspend fun app.cash.turbine.TurbineTestContext<ProfileUiState>.awaitRealState(): ProfileUiState {
        val first = awaitItem()
        return if (first == ProfileUiState.Loading) awaitItem() else first
    }

    // ── Initial state ─────────────────────────────────────────────────────────

    @Test
    fun `initial state is Loading`() = runTest {
        val vm = makeViewModel()
        assertEquals(ProfileUiState.Loading, vm.uiState.value)
    }

    // ── Profile loaded ────────────────────────────────────────────────────────

    @Test
    fun `emits NoProfile when profile is null`() = runTest {
        profileFlow.value = null
        val vm = makeViewModel()
        vm.uiState.test {
            val state = awaitRealState()
            assertTrue("Expected NoProfile, got $state", state is ProfileUiState.NoProfile)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `emits Loaded when profile exists`() = runTest {
        profileFlow.value = baseProfile()
        val vm = makeViewModel()
        vm.uiState.test {
            val state = awaitRealState()
            assertTrue("Expected Loaded, got $state", state is ProfileUiState.Loaded)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `Loaded state contains display name from profile`() = runTest {
        profileFlow.value = baseProfile()
        val vm = makeViewModel()
        vm.uiState.test {
            val state = awaitRealState() as ProfileUiState.Loaded
            assertEquals("Alice", state.displayName)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `Loaded state contains weight unit`() = runTest {
        profileFlow.value = baseProfile()
        unitFlow.value = WeightUnit.LBS
        val vm = makeViewModel()
        vm.uiState.test {
            val state = awaitRealState() as ProfileUiState.Loaded
            assertEquals(WeightUnit.LBS, state.weightUnit)
            cancelAndIgnoreRemainingEvents()
        }
    }

    // ── Unit preference ───────────────────────────────────────────────────────

    @Test
    fun `onUnitChanged calls setWeightUnit`() = runTest {
        val vm = makeViewModel()
        vm.onUnitChanged(WeightUnit.LBS)
        advanceUntilIdle()
        coVerify { userPrefsDataStore.setWeightUnit(WeightUnit.LBS) }
    }
}
