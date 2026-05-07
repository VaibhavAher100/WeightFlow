package com.weightflow.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface WeightEntryDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entry: WeightEntryEntity): Long

    @Delete
    suspend fun delete(entry: WeightEntryEntity): Int

    @Update
    suspend fun update(entry: WeightEntryEntity): Int

    @Query("SELECT * FROM weight_entries WHERE id = :id LIMIT 1")
    suspend fun getById(id: Long): WeightEntryEntity?

    @Query("SELECT * FROM weight_entries ORDER BY timestamp DESC, id DESC")
    fun getEntriesNewestFirst(): Flow<List<WeightEntryEntity>>

    @Query("SELECT * FROM weight_entries ORDER BY timestamp ASC, id ASC")
    fun getEntriesOldestFirst(): Flow<List<WeightEntryEntity>>

    @Query("SELECT * FROM weight_entries WHERE timestamp BETWEEN :fromTimestamp AND :toTimestamp ORDER BY timestamp ASC, id ASC")
    fun getEntriesBetween(fromTimestamp: Long, toTimestamp: Long): Flow<List<WeightEntryEntity>>

    @Query("DELETE FROM weight_entries")
    suspend fun deleteAll(): Int
}
