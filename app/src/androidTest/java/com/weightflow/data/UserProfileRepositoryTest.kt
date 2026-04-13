package com.weightflow.data

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.time.LocalDate
import com.weightflow.domain.UserProfile

@RunWith(AndroidJUnit4::class)
class UserProfileRepositoryTest {

    private lateinit var db: AppDatabase
    private lateinit var repository: UserProfileRepository

    @Before
    fun setUp() {
        db = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            AppDatabase::class.java
        ).allowMainThreadQueries().build()
        repository = UserProfileRepository(db.userProfileDao())
    }

    @After
    fun tearDown() {
        db.close()
    }

    @Test
    fun getProfileReturnsNullWhenEmpty() = runTest {
        val profile = repository.getProfile().first()
        assertNull(profile)
    }

    @Test
    fun saveAndGetProfileRoundTripsBasicFields() = runTest {
        val profile = UserProfile(
            id = 1L,
            displayName = "Alice",
            goalWeightKg = 70.0,
            targetDate = null,
            heightCm = 165.0,
            maintenanceMode = false,
            maintenanceRangeKg = 1.0,
            maintenanceModeActivatedAt = null
        )
        repository.saveProfile(profile)
        val result = repository.getProfile().first()
        assertEquals("Alice", result?.displayName)
        assertEquals(70.0, result?.goalWeightKg ?: 0.0, 0.001)
        assertEquals(165.0, result?.heightCm ?: 0.0, 0.001)
        assertEquals(false, result?.maintenanceMode)
    }

    @Test
    fun saveProfileConvertsLocalDateToEpochDayAndBack() = runTest {
        val targetDate = LocalDate.of(2026, 12, 31)
        val profile = UserProfile(
            id = 1L,
            displayName = "Bob",
            goalWeightKg = 75.0,
            targetDate = targetDate,
            heightCm = null,
            maintenanceMode = false,
            maintenanceRangeKg = 1.0,
            maintenanceModeActivatedAt = null
        )
        repository.saveProfile(profile)
        val result = repository.getProfile().first()
        assertEquals(targetDate, result?.targetDate)
    }

    @Test
    fun saveProfileWithNullTargetDateRoundTrips() = runTest {
        val profile = UserProfile(
            id = 1L,
            displayName = "Carol",
            goalWeightKg = null,
            targetDate = null,
            heightCm = null,
            maintenanceMode = false,
            maintenanceRangeKg = 0.5,
            maintenanceModeActivatedAt = null
        )
        repository.saveProfile(profile)
        val result = repository.getProfile().first()
        assertNull(result?.targetDate)
        assertNull(result?.goalWeightKg)
    }

    @Test
    fun saveProfileTwiceUpdatesExistingProfile() = runTest {
        val original = UserProfile(
            id = 1L,
            displayName = "Dan",
            goalWeightKg = 80.0,
            targetDate = null,
            heightCm = 180.0,
            maintenanceMode = false,
            maintenanceRangeKg = 1.0,
            maintenanceModeActivatedAt = null
        )
        repository.saveProfile(original)
        repository.saveProfile(original.copy(displayName = "Daniel", goalWeightKg = 78.0))
        val result = repository.getProfile().first()
        assertEquals("Daniel", result?.displayName)
        assertEquals(78.0, result?.goalWeightKg ?: 0.0, 0.001)
    }

    @Test
    fun saveProfileWithMaintenanceModeRoundTrips() = runTest {
        val activatedAt = System.currentTimeMillis()
        val profile = UserProfile(
            id = 1L,
            displayName = "Eve",
            goalWeightKg = 65.0,
            targetDate = null,
            heightCm = 160.0,
            maintenanceMode = true,
            maintenanceRangeKg = 1.5,
            maintenanceModeActivatedAt = activatedAt
        )
        repository.saveProfile(profile)
        val result = repository.getProfile().first()
        assertTrue(result?.maintenanceMode == true)
        assertEquals(activatedAt, result?.maintenanceModeActivatedAt)
        assertEquals(1.5, result?.maintenanceRangeKg ?: 0.0, 0.001)
    }
}
