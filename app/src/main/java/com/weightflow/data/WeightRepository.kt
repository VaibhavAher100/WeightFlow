package com.weightflow.data

import com.weightflow.domain.WeightEntry
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class WeightRepository(private val dao: WeightEntryDao) {

    suspend fun addEntry(weightKg: Double, timestamp: Long, note: String = ""): Long {
        return dao.insert(WeightEntryEntity(timestamp = timestamp, weightKg = weightKg, note = note))
    }

    suspend fun removeEntry(id: Long): Int {
        val entity = dao.getById(id) ?: return 0
        return dao.delete(entity)
    }

    suspend fun getById(id: Long): WeightEntry? {
        return dao.getById(id)?.toDomain()
    }

    fun getEntriesNewestFirst(): Flow<List<WeightEntry>> {
        return dao.getEntriesNewestFirst().map { list -> list.map { it.toDomain() } }
    }

    fun getEntriesOldestFirst(): Flow<List<WeightEntry>> {
        return dao.getEntriesOldestFirst().map { list -> list.map { it.toDomain() } }
    }

    fun getEntriesBetween(from: Long, to: Long): Flow<List<WeightEntry>> {
        return dao.getEntriesBetween(from, to).map { list -> list.map { it.toDomain() } }
    }

    private fun WeightEntryEntity.toDomain() = WeightEntry(
        id = id,
        timestamp = timestamp,
        weightKg = weightKg,
        note = note
    )
}
