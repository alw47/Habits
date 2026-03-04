package com.liwo.habits.data.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.liwo.habits.data.model.Redemption
import kotlinx.coroutines.flow.Flow

@Dao
interface RedemptionDao {

    @Query("SELECT * FROM redemptions ORDER BY createdAtMillis DESC LIMIT :limit")
    fun observeRecent(limit: Int): Flow<List<Redemption>>

    @Query("SELECT COALESCE(SUM(cost), 0) FROM redemptions")
    fun observeTotalSpent(): Flow<Long>

    @Query("SELECT COALESCE(SUM(cost), 0) FROM redemptions")
    suspend fun getTotalSpent(): Long

    @Query("SELECT COUNT(*) FROM redemptions WHERE rewardId = :rewardId")
    suspend fun countForReward(rewardId: Long): Int

    @Insert
    suspend fun insert(redemption: Redemption)

    @Query("DELETE FROM redemptions WHERE id = :id")
    suspend fun deleteById(id: Long)
}