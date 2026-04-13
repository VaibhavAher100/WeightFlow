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
class WeightEntryDaoTest {

    private lateinit var db: AppDatabase
    private lateinit var dao: WeightEntryDao

    @Before
    fun setUp() {
        db = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            AppDatabase::class.java
        ).allowMainThreadQueries().build()
        dao = db.weightEntryDao()
    }

    @After
    fun tearDown() {
        db.close()
    }

    @Test
    fun insertAndGetByIdReturnsEntry() = runTest {
        val entry = WeightEntryEntity(timestamp = 1000L, weightKg = 80.0, note = "")
        val id = dao.insert(entry)
        val result = dao.getById(id)
        assertEquals(80.0, result?.weightKg ?: 0.0, 0.001)
    }

    @Test
    fun getEntriesNewestFirstReturnsDescendingOrder() = runTest {
        dao.insert(WeightEntryEntity(timestamp = 1000L, weightKg = 80.0, note = ""))
        dao.insert(WeightEntryEntity(timestamp = 3000L, weightKg = 82.0, note = ""))
        dao.insert(WeightEntryEntity(timestamp = 2000L, weightKg = 81.0, note = ""))
        val entries = dao.getEntriesNewestFirst().first()
        assertEquals(3, entries.size)
        assertEquals(3000L, entries[0].timestamp)
        assertEquals(2000L, entries[1].timestamp)
        assertEquals(1000L, entries[2].timestamp)
    }

    @Test
    fun getEntriesOldestFirstReturnsAscendingOrder() = runTest {
        dao.insert(WeightEntryEntity(timestamp = 3000L, weightKg = 82.0, note = ""))
        dao.insert(WeightEntryEntity(timestamp = 1000L, weightKg = 80.0, note = ""))
        dao.insert(WeightEntryEntity(timestamp = 2000L, weightKg = 81.0, note = ""))
        val entries = dao.getEntriesOldestFirst().first()
        assertEquals(3, entries.size)
        assertEquals(1000L, entries[0].timestamp)
        assertEquals(2000L, entries[1].timestamp)
        assertEquals(3000L, entries[2].timestamp)
    }

    @Test
    fun getEntriesBetweenReturnsOnlyEntriesInRange() = runTest {
        dao.insert(WeightEntryEntity(timestamp = 1000L, weightKg = 80.0, note = ""))
        dao.insert(WeightEntryEntity(timestamp = 2000L, weightKg = 81.0, note = ""))
        dao.insert(WeightEntryEntity(timestamp = 3000L, weightKg = 82.0, note = ""))
        val entries = dao.getEntriesBetween(1500L, 2500L).first()
        assertEquals(1, entries.size)
        assertEquals(2000L, entries[0].timestamp)
    }

    @Test
    fun deleteEntryRemovesItFromDb() = runTest {
        val entry = WeightEntryEntity(timestamp = 1000L, weightKg = 80.0, note = "")
        val id = dao.insert(entry)
        val inserted = dao.getById(id)!!
        dao.delete(inserted)
        val result = dao.getById(id)
        assertNull(result)
    }

    @Test
    fun insertReplacesEntryWithSameId() = runTest {
        val entry = WeightEntryEntity(id = 1L, timestamp = 1000L, weightKg = 80.0, note = "")
        dao.insert(entry)
        dao.insert(entry.copy(weightKg = 85.0))
        val entries = dao.getEntriesNewestFirst().first()
        assertEquals(1, entries.size)
        assertEquals(85.0, entries[0].weightKg, 0.001)
    }

    @Test
    fun emptyDatabaseReturnsEmptyList() = runTest {
        val entries = dao.getEntriesNewestFirst().first()
        assertEquals(0, entries.size)
    }
}
