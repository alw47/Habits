package com.liwo.habits.data.db

import androidx.room.*
import com.liwo.habits.data.model.Habit
import kotlinx.coroutines.flow.Flow

@Dao
interface HabitDao {

    @Query("SELECT * FROM habits ORDER BY sortOrder ASC")
    fun observeAllHabits(): Flow<List<Habit>>

    @Query("SELECT * FROM habits WHERE isActive = 1 ORDER BY sortOrder ASC")
    fun observeActiveHabits(): Flow<List<Habit>>

    @Upsert
    suspend fun upsertHabit(habit: Habit)

    @Delete
    suspend fun deleteHabit(habit: Habit)

    @Query("UPDATE habits SET isActive = :active WHERE id = :id")
    suspend fun setActive(id: Long, active: Boolean)

    @Query("SELECT MAX(sortOrder) FROM habits")
    suspend fun maxSortOrder(): Int?

    @Query("SELECT COUNT(*) FROM habits")
    suspend fun countHabits(): Int
}