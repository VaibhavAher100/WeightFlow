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

@RunWith(AndroidJUnit4::class)
class WeightRepositoryTest {

    private lateinit var db: AppDatabase
    private lateinit var repository: WeightRepository

    @Before
    fun setUp() {
        db = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            AppDatabase::class.java
        ).allowMainThreadQueries().build()
        repository = WeightRepository(db.weightEntryDao())
    }

    @After
    fun tearDown() {
        db.close()
    }

    @Test
    fun addEntryReturnsDomainEntryId() = runTest {
        val id = repository.addEntry(weightKg = 80.0, timestamp = 1000L)
        assertTrue(id > 0)
    }

    @Test
    fun addEntryAppearsInNewestFirstFlow() = runTest {
        repository.addEntry(weightKg = 80.0, timestamp = 1000L)
        val entries = repository.getEntriesNewestFirst().first()
        assertEquals(1, entries.size)
        assertEquals(80.0, entries[0].weightKg, 0.001)
        assertEquals(1000L, entries[0].timestamp)
    }

    @Test
    fun getEntriesNewestFirstReturnsDomainObjectsDescending() = runTest {
        repository.addEntry(weightKg = 80.0, timestamp = 1000L)
        repository.addEntry(weightKg = 82.0, timestamp = 3000L)
        repository.addEntry(weightKg = 81.0, timestamp = 2000L)
        val entries = repository.getEntriesNewestFirst().first()
        assertEquals(3, entries.size)
        assertEquals(3000L, entries[0].timestamp)
        assertEquals(2000L, entries[1].timestamp)
        assertEquals(1000L, entries[2].timestamp)
    }

    @Test
    fun getEntriesOldestFirstReturnsDomainObjectsAscending() = runTest {
        repository.addEntry(weightKg = 82.0, timestamp = 3000L)
        repository.addEntry(weightKg = 80.0, timestamp = 1000L)
        repository.addEntry(weightKg = 81.0, timestamp = 2000L)
        val entries = repository.getEntriesOldestFirst().first()
        assertEquals(3, entries.size)
        assertEquals(1000L, entries[0].timestamp)
        assertEquals(2000L, entries[1].timestamp)
        assertEquals(3000L, entries[2].timestamp)
    }

    @Test
    fun getEntriesBetweenFiltersCorrectly() = runTest {
        repository.addEntry(weightKg = 80.0, timestamp = 1000L)
        repository.addEntry(weightKg = 81.0, timestamp = 2000L)
        repository.addEntry(weightKg = 82.0, timestamp = 3000L)
        val entries = repository.getEntriesBetween(from = 1500L, to = 2500L).first()
        assertEquals(1, entries.size)
        assertEquals(2000L, entries[0].timestamp)
    }

    @Test
    fun getByIdReturnsDomainEntry() = runTest {
        val id = repository.addEntry(weightKg = 75.5, timestamp = 5000L)
        val entry = repository.getById(id)
        assertEquals(75.5, entry?.weightKg ?: 0.0, 0.001)
        assertEquals(5000L, entry?.timestamp)
    }

    @Test
    fun getByIdReturnsNullForMissingEntry() = runTest {
        val entry = repository.getById(999L)
        assertNull(entry)
    }

    @Test
    fun removeEntryDeletesItFromDb() = runTest {
        val id = repository.addEntry(weightKg = 80.0, timestamp = 1000L)
        val rowsAffected = repository.removeEntry(id)
        assertEquals(1, rowsAffected)
        assertNull(repository.getById(id))
    }

    @Test
    fun removeNonExistentEntryReturnsZero() = runTest {
        val rowsAffected = repository.removeEntry(999L)
        assertEquals(0, rowsAffected)
    }

    @Test
    fun emptyRepositoryReturnsEmptyFlow() = runTest {
        val entries = repository.getEntriesNewestFirst().first()
        assertEquals(0, entries.size)
    }

    @Test
    fun addEntryWithNotePreservesNote() = runTest {
        val id = repository.addEntry(weightKg = 80.0, timestamp = 1000L, note = "morning")
        val entry = repository.getById(id)
        assertEquals("morning", entry?.note)
    }
}
