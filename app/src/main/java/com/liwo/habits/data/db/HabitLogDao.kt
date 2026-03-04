package com.liwo.habits.data.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.liwo.habits.data.model.HabitLog
import kotlinx.coroutines.flow.Flow

@Dao
interface HabitLogDao {

    @Query("SELECT * FROM habit_logs WHERE date = :date")
    fun observeLogsForDate(date: String): Flow<List<HabitLog>>

    @Query("SELECT * FROM habit_logs WHERE date >= :start AND date <= :end")
    fun observeLogsForDateRange(start: String, end: String): Flow<List<HabitLog>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertLog(log: HabitLog)

    @Query("DELETE FROM habit_logs WHERE habitId = :habitId AND date = :date")
    suspend fun deleteLog(habitId: Long, date: String)

    /**
     * Total earned points across all logged days.
     *
     * status =  1  -> +habits.pointsDone
     * status = -1  -> +habits.pointsMissed  (usually negative)
     *
     * SUM returns Long in SQLite.
     */
    @Query(
        """
        SELECT COALESCE(SUM(
            CASE
                WHEN hl.status = 1  THEN h.pointsDone
                WHEN hl.status = -1 THEN h.pointsMissed
                ELSE 0
            END
        ), 0)
        FROM habit_logs hl
        JOIN habits h ON h.id = hl.habitId
        """
    )
    fun observeTotalPointsEarned(): Flow<Long>

    @Query(
        """
        SELECT COALESCE(SUM(
            CASE
                WHEN hl.status = 1  THEN h.pointsDone
                WHEN hl.status = -1 THEN h.pointsMissed
                ELSE 0
            END
        ), 0)
        FROM habit_logs hl
        JOIN habits h ON h.id = hl.habitId
        """
    )
    suspend fun getTotalPointsEarned(): Long
}