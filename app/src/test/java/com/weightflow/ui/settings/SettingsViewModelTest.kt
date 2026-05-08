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
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder

@OptIn(ExperimentalCoroutinesApi::class)
class SettingsViewModelTest {

    @get:Rule
    val tmpFolder = TemporaryFolder()

    private val testDispatcher = StandardTestDispatcher()
    private val userPrefsDataStore: UserPrefsDataStore = mockk()
    private val weightRepository: WeightRepository = mockk()
    private val paletteFlow = MutableStateFlow("lime")
    private val unitFlow = MutableStateFlow(WeightUnit.KG)
    private val reminderFlow = MutableStateFlow(false)
    private val entriesFlow = MutableStateFlow<List<WeightEntry>>(emptyList())

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        every { userPrefsDataStore.themePalette } returns paletteFlow
        every { userPrefsDataStore.weightUnit } returns unitFlow
        every { userPrefsDataStore.reminderEnabled } returns reminderFlow
        coEvery { userPrefsDataStore.setThemePalette(any()) } returns Unit
        coEvery { userPrefsDataStore.setWeightUnit(any()) } returns Unit
        coEvery { userPrefsDataStore.setReminderEnabled(any()) } returns Unit
        every { weightRepository.getEntriesOldestFirst() } returns entriesFlow
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun makeViewModel() = SettingsViewModel(
        userPrefsDataStore = userPrefsDataStore,
        weightRepository   = weightRepository,
        cacheDir           = tmpFolder.root,
    )

    // ── Initial state ─────────────────────────────────────────────────────────

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
    fun `initial export format is PLAINTEXT`() = runTest {
        val vm = makeViewModel()
        vm.uiState.test {
            advanceUntilIdle()
            assertEquals(ExportFormat.PLAINTEXT, expectMostRecentItem().selectedExportFormat)
        }
    }

    // ── Theme ─────────────────────────────────────────────────────────────────

    @Test
    fun `onThemeSelected updates themePalette in uiState`() = runTest {
        val vm = makeViewModel()
        vm.onThemeSelected("ocean")
        advanceUntilIdle()
        coVerify { userPrefsDataStore.setThemePalette("ocean") }
    }

    // ── Weight unit ───────────────────────────────────────────────────────────

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

    // ── Export format selection ───────────────────────────────────────────────

    @Test
    fun `onExportFormatChanged updates selectedExportFormat in uiState`() = runTest {
        val vm = makeViewModel()
        vm.uiState.test {
            advanceUntilIdle()
            expectMostRecentItem() // consume initial

            vm.onExportFormatChanged(ExportFormat.ENCRYPTED_ZIP)
            advanceUntilIdle()
            assertEquals(ExportFormat.ENCRYPTED_ZIP, expectMostRecentItem().selectedExportFormat)
        }
    }

    @Test
    fun `onExportFormatChanged to MINIMAL_CSV reflects in uiState`() = runTest {
        val vm = makeViewModel()
        vm.uiState.test {
            advanceUntilIdle()
            expectMostRecentItem()

            vm.onExportFormatChanged(ExportFormat.MINIMAL_CSV)
            advanceUntilIdle()
            assertEquals(ExportFormat.MINIMAL_CSV, expectMostRecentItem().selectedExportFormat)
        }
    }

    // ── Plaintext export ──────────────────────────────────────────────────────

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

    // ── Minimal CSV export via onExportCsv ────────────────────────────────────

    @Test
    fun `onExportCsv with MINIMAL_CSV format emits minimal headers`() = runTest {
        entriesFlow.value = listOf(
            WeightEntry(id = 1L, timestamp = 86_400_000L * 19723L, weightKg = 80.0, note = "notes"),
        )
        val vm = makeViewModel()
        vm.onExportFormatChanged(ExportFormat.MINIMAL_CSV)
        vm.events.test {
            vm.onExportCsv()
            advanceUntilIdle()
            val event = awaitItem() as SettingsEvent.ExportCsvReady
            assertTrue("Minimal CSV should start with date,weight_kg",
                event.csvContent.startsWith("date,weight_kg"))
            // Exactly two fields per line (one comma in header)
            assertEquals(1, event.csvContent.lines().first().count { it == ',' })
        }
    }

    // ── Encrypted ZIP export ──────────────────────────────────────────────────

    @Test
    fun `onExportEncryptedZip with valid password emits ExportEncryptedZipReady`() = runTest {
        entriesFlow.value = listOf(
            WeightEntry(id = 1L, timestamp = 86_400_000L * 19723L, weightKg = 80.0, note = ""),
        )
        val vm = makeViewModel()
        vm.events.test {
            vm.onExportEncryptedZip("StrongPassword99!".toCharArray())
            advanceUntilIdle()
            val event = awaitItem()
            assertTrue(
                "Expected ExportEncryptedZipReady but got $event",
                event is SettingsEvent.ExportEncryptedZipReady,
            )
            val ready = event as SettingsEvent.ExportEncryptedZipReady
            assertTrue("ZIP file should exist", ready.zipFile.exists())
            assertTrue("Suggested name should be a .zip file",
                ready.suggestedFileName.endsWith(".zip"))
            // Cleanup
            ready.zipFile.delete()
        }
    }

    @Test
    fun `onExportEncryptedZip with password shorter than 12 chars emits ExportEncryptionFailed`() = runTest {
        val vm = makeViewModel()
        vm.events.test {
            vm.onExportEncryptedZip("short1".toCharArray())
            advanceUntilIdle()
            val event = awaitItem()
            assertTrue(
                "Expected ExportEncryptionFailed but got $event",
                event is SettingsEvent.ExportEncryptionFailed,
            )
            val failed = event as SettingsEvent.ExportEncryptionFailed
            assertTrue("Reason should mention 12 characters",
                failed.reason.contains("12"))
        }
    }

    @Test
    fun `onExportEncryptedZip password is zeroed after ViewModel call`() = runTest {
        entriesFlow.value = emptyList()
        val vm = makeViewModel()
        val password = "StrongPassword99!".toCharArray()
        vm.events.test {
            vm.onExportEncryptedZip(password)
            advanceUntilIdle()
            val event = awaitItem()
            // Regardless of success/failure the array must be zeroed.
            assertTrue(
                "Password CharArray must be zeroed after onExportEncryptedZip",
                password.all { it == ' ' }
            )
            if (event is SettingsEvent.ExportEncryptedZipReady) {
                event.zipFile.delete()
            }
        }
    }

    @Test
    fun `onExportEncryptedZip with null cacheDir emits ExportEncryptionFailed`() = runTest {
        val vmNoCacheDir = SettingsViewModel(
            userPrefsDataStore = userPrefsDataStore,
            weightRepository   = weightRepository,
            cacheDir           = null,
        )
        vmNoCacheDir.events.test {
            vmNoCacheDir.onExportEncryptedZip("StrongPassword99!".toCharArray())
            advanceUntilIdle()
            val event = awaitItem()
            assertTrue(event is SettingsEvent.ExportEncryptionFailed)
        }
    }

    // ── Reminder ──────────────────────────────────────────────────────────────

    @Test
    fun `onReminderToggled true emits RequestNotificationPermission without touching DataStore`() = runTest {
        val vm = makeViewModel()
        vm.events.test {
            vm.onReminderToggled(true)
            advanceUntilIdle()
            assertEquals(SettingsEvent.RequestNotificationPermission, awaitItem())
        }
        coVerify(exactly = 0) { userPrefsDataStore.setReminderEnabled(any()) }
    }

    @Test
    fun `onReminderToggled false persists disabled state`() = runTest {
        val vm = makeViewModel()
        vm.onReminderToggled(false)
        advanceUntilIdle()
        coVerify { userPrefsDataStore.setReminderEnabled(false) }
    }

    @Test
    fun `onReminderPermissionResult granted true persists enabled and emits ReminderEnabled`() = runTest {
        val vm = makeViewModel()
        vm.events.test {
            vm.onReminderPermissionResult(granted = true)
            advanceUntilIdle()
            assertEquals(SettingsEvent.ReminderEnabled, awaitItem())
        }
        coVerify { userPrefsDataStore.setReminderEnabled(true) }
    }

    @Test
    fun `onReminderPermissionResult granted false keeps toggle OFF and emits NotificationPermissionDenied`() = runTest {
        val vm = makeViewModel()
        vm.events.test {
            vm.onReminderPermissionResult(granted = false)
            advanceUntilIdle()
            assertEquals(SettingsEvent.NotificationPermissionDenied, awaitItem())
        }
        coVerify { userPrefsDataStore.setReminderEnabled(false) }
    }

    @Test
    fun `uiState reflects reminderEnabled from DataStore`() = runTest {
        reminderFlow.value = true
        val vm = makeViewModel()
        vm.uiState.test {
            advanceUntilIdle()
            val state = expectMostRecentItem()
            assertTrue(state.reminderEnabled)
        }
    }
}
