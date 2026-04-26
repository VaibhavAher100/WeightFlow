package com.weightflow.data

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import kotlinx.coroutines.flow.Flow

@Dao
interface UserProfileDao {

    @Upsert
    suspend fun upsert(profile: UserProfileEntity): Long

    @Query("SELECT * FROM user_profile WHERE id = 1 LIMIT 1")
    fun getProfile(): Flow<UserProfileEntity?>

    @Query("DELETE FROM user_profile")
    suspend fun deleteAll(): Int
}
