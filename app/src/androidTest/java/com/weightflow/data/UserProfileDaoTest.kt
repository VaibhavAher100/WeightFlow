package com.weightflow.data

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class UserProfileDaoTest {

    private lateinit var db: AppDatabase
    private lateinit var dao: UserProfileDao

    @Before
    fun setUp() {
        db = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            AppDatabase::class.java
        ).allowMainThreadQueries().build()
        dao = db.userProfileDao()
    }

    @After
    fun tearDown() {
        db.close()
    }

    @Test
    fun getProfileReturnsNullWhenDatabaseIsEmpty() = runTest {
        val profile = dao.getProfile().first()
        assertNull(profile)
    }

    @Test
    fun insertAndGetProfileReturnsInsertedProfile() = runTest {
        val profile = UserProfileEntity(
            id = 1L,
            displayName = "Alex",
            goalWeightKg = 75.0,
            targetDateEpochDay = null,
            heightCm = 175.0,
            maintenanceMode = false,
            maintenanceRangeKg = 1.0,
            maintenanceModeActivatedAt = null
        )
        dao.upsert(profile)
        val result = dao.getProfile().first()
        assertEquals("Alex", result?.displayName)
        assertEquals(75.0, result?.goalWeightKg ?: 0.0, 0.001)
    }

    @Test
    fun upsertUpdatesExistingProfile() = runTest {
        val profile = UserProfileEntity(
            id = 1L,
            displayName = "Alex",
            goalWeightKg = 75.0,
            targetDateEpochDay = null,
            heightCm = 175.0,
            maintenanceMode = false,
            maintenanceRangeKg = 1.0,
            maintenanceModeActivatedAt = null
        )
        dao.upsert(profile)
        dao.upsert(profile.copy(displayName = "Alex Updated", goalWeightKg = 72.0))
        val result = dao.getProfile().first()
        assertEquals("Alex Updated", result?.displayName)
        assertEquals(72.0, result?.goalWeightKg ?: 0.0, 0.001)
    }

    @Test
    fun maintenanceModePersistedCorrectly() = runTest {
        val profile = UserProfileEntity(
            id = 1L,
            displayName = "Alex",
            goalWeightKg = null,
            targetDateEpochDay = null,
            heightCm = null,
            maintenanceMode = true,
            maintenanceRangeKg = 2.0,
            maintenanceModeActivatedAt = 5000L
        )
        dao.upsert(profile)
        val result = dao.getProfile().first()!!
        assertEquals(true, result.maintenanceMode)
        assertEquals(2.0, result.maintenanceRangeKg, 0.001)
        assertEquals(5000L, result.maintenanceModeActivatedAt)
    }

    @Test
    fun nullableFieldsRoundTripCorrectly() = runTest {
        val profile = UserProfileEntity(
            id = 1L,
            displayName = "Sam",
            goalWeightKg = null,
            targetDateEpochDay = 19000L,
            heightCm = null,
            maintenanceMode = false,
            maintenanceRangeKg = 1.0,
            maintenanceModeActivatedAt = null
        )
        dao.upsert(profile)
        val result = dao.getProfile().first()!!
        assertNull(result.goalWeightKg)
        assertEquals(19000L, result.targetDateEpochDay)
        assertNull(result.heightCm)
    }
}
