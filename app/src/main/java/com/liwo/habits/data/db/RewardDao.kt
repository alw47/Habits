package com.liwo.habits.data.db

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.liwo.habits.data.model.Reward
import kotlinx.coroutines.flow.Flow

@Dao
interface RewardDao {

    @Query("SELECT * FROM rewards ORDER BY sortOrder ASC, id ASC")
    fun observeAll(): Flow<List<Reward>>

    @Query("SELECT * FROM rewards WHERE id = :id LIMIT 1")
    suspend fun getById(id: Long): Reward?

    @Query("SELECT MAX(sortOrder) FROM rewards")
    suspend fun maxSortOrder(): Int?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(reward: Reward)

    @Delete
    suspend fun delete(reward: Reward)

    @Query("UPDATE rewards SET isActive = :active WHERE id = :id")
    suspend fun setActive(id: Long, active: Boolean)

    @Query("SELECT * FROM rewards")
    suspend fun getAllRewards(): List<Reward>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(rewards: List<Reward>)

    @Query("DELETE FROM rewards")
    suspend fun deleteAll()
}