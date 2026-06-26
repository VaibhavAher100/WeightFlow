package com.weightflow.data

import android.content.Context
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.weightflow.domain.WeightUnit
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.io.File

@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(AndroidJUnit4::class)
class UserPrefsDataStoreTest {

    private val testDispatcher = UnconfinedTestDispatcher()
    private val testScope = TestScope(testDispatcher)
    private lateinit var testFile: File
    private lateinit var userPrefs: UserPrefsDataStore

    @Before
    fun setUp() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        testFile = File(context.cacheDir, "test_prefs_${System.nanoTime()}.preferences_pb")
        val dataStore = PreferenceDataStoreFactory.create(
            scope = testScope,
            produceFile = { testFile }
        )
        userPrefs = UserPrefsDataStore(dataStore)
    }

    @After
    fun tearDown() {
        testFile.delete()
    }

    @Test
    fun defaultWeightUnitIsKg() = runTest(testDispatcher) {
        assertEquals(WeightUnit.KG, userPrefs.weightUnit.first())
    }

    @Test
    fun setWeightUnitToLbsPersistsValue() = runTest(testDispatcher) {
        userPrefs.setWeightUnit(WeightUnit.LBS)
        assertEquals(WeightUnit.LBS, userPrefs.weightUnit.first())
    }

    @Test
    fun setWeightUnitToStonesPersistsValue() = runTest(testDispatcher) {
        userPrefs.setWeightUnit(WeightUnit.ST)
        assertEquals(WeightUnit.ST, userPrefs.weightUnit.first())
    }

    @Test
    fun defaultThemeIsLime() = runTest(testDispatcher) {
        assertEquals("lime", userPrefs.themePalette.first())
    }

    @Test
    fun setThemePalettePersistsValue() = runTest(testDispatcher) {
        userPrefs.setThemePalette("ocean")
        assertEquals("ocean", userPrefs.themePalette.first())
    }

    @Test
    fun defaultOnboardingCompleteIsFalse() = runTest(testDispatcher) {
        assertEquals(false, userPrefs.onboardingState.first())
    }

    @Test
    fun setOnboardingCompletePersistsTrue() = runTest(testDispatcher) {
        userPrefs.setOnboardingComplete()
        assertEquals(true, userPrefs.onboardingState.first())
    }
}
