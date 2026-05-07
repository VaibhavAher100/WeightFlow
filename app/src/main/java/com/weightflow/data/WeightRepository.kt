package com.weightflow.data

import com.weightflow.domain.WeightEntry
import com.weightflow.domain.isValidWeightKg
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class WeightRepository(private val dao: WeightEntryDao) {

    suspend fun addEntry(weightKg: Double, timestamp: Long, note: String = ""): Long {
        require(weightKg.isValidWeightKg()) { "Invalid weight: $weightKg" }
        return dao.insert(WeightEntryEntity(timestamp = timestamp, weightKg = weightKg, note = note))
    }

    suspend fun removeEntry(id: Long): Int {
        val entity = dao.getById(id) ?: return 0
        return dao.delete(entity)
    }

    suspend fun updateEntry(id: Long, weightKg: Double): Int {
        require(weightKg.isValidWeightKg()) { "Invalid weight: $weightKg" }
        val entity = dao.getById(id) ?: return 0
        return dao.update(entity.copy(weightKg = weightKg))
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
        require(from <= to) { "from must be <= to" }
        return dao.getEntriesBetween(from, to).map { list -> list.map { it.toDomain() } }
    }

    suspend fun deleteAllEntries(): Int = dao.deleteAll()

    private fun WeightEntryEntity.toDomain() = WeightEntry(
        id = id,
        timestamp = timestamp,
        weightKg = weightKg,
        note = note
    )
}
