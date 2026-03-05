package com.liwo.habits.data.repo

import androidx.room.withTransaction
import com.liwo.habits.data.db.AppDatabase
import com.liwo.habits.data.model.Habit
import com.liwo.habits.data.model.HabitLog
import com.liwo.habits.data.model.HabitStatus
import com.liwo.habits.util.WeekdayMask
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class HabitRepository @Inject constructor(private val db: AppDatabase) {

    fun observeHabits(): Flow<List<Habit>> =
        db.habitDao().observeAllHabits()

    /** Earned points minus redeemed points, as a live Flow<Int>. */
    fun observePointsAvailable(): Flow<Int> =
        combine(
            db.habitLogDao().observeTotalPointsEarned(),
            db.redemptionDao().observeTotalSpent()
        ) { earned, spent ->
            (earned - spent).coerceIn(Int.MIN_VALUE.toLong(), Int.MAX_VALUE.toLong()).toInt()
        }

    fun observeLogsForDateRange(start: String, end: String): Flow<List<HabitLog>> =
        db.habitLogDao().observeLogsForDateRange(start, end)

    fun observeDailyState(date: String): Flow<DailyState> {
        val habitsFlow = db.habitDao().observeActiveHabits()
        val logsFlow = db.habitLogDao().observeLogsForDate(date)

        return combine(habitsFlow, logsFlow) { habits, logs ->
            val logMap = logs.associateBy { it.habitId }

            val visible = habits
                .filter { WeekdayMask.isAllowed(it.daysMask, date) }
                .sortedBy { it.sortOrder }
                .map { h ->
                    DailyHabitItem(
                        id = h.id,
                        name = h.name,
                        pointsDone = h.pointsDone,
                        pointsMissed = h.pointsMissed,
                        status = logMap[h.id]?.status ?: HabitStatus.NONE
                    )
                }

            DailyState(visible)
        }
    }

    suspend fun upsertHabit(
        id: Long = 0,
        name: String,
        pointsDone: Int,
        pointsMissed: Int,
        isActive: Boolean,
        daysMask: Int,
        sortOrder: Int? = null
    ) {
        val finalSort = sortOrder ?: ((db.habitDao().maxSortOrder() ?: 0) + 1)

        db.habitDao().upsertHabit(
            Habit(
                id = id,
                name = name.trim(),
                pointsDone = pointsDone,
                pointsMissed = pointsMissed,
                isActive = isActive,
                sortOrder = finalSort,
                daysMask = daysMask
            )
        )
    }

    suspend fun deleteHabit(habit: Habit) =
        db.habitDao().deleteHabit(habit)

    suspend fun setActive(id: Long, active: Boolean) =
        db.habitDao().setActive(id, active)

    suspend fun swapSort(a: Habit, b: Habit) {
        db.withTransaction {
            db.habitDao().upsertHabit(a.copy(sortOrder = b.sortOrder))
            db.habitDao().upsertHabit(b.copy(sortOrder = a.sortOrder))
        }
    }

    suspend fun setStatus(habitId: Long, date: String, status: HabitStatus) {
        if (status == HabitStatus.NONE) {
            db.habitLogDao().deleteLog(habitId, date)
        } else {
            db.habitLogDao().upsertLog(
                HabitLog(habitId = habitId, date = date, status = status)
            )
        }
    }

    /**
     * Creates a few starter habits ONLY if the database is empty.
     * Safe to call at startup.
     */
    suspend fun ensureSeedData() {
        if (db.habitDao().countHabits() > 0) return

        upsertHabit(
            name = "Exercise",
            pointsDone = 5,
            pointsMissed = -5,
            isActive = true,
            daysMask = WeekdayMask.WEEKDAYS,
            sortOrder = 1
        )

        upsertHabit(
            name = "Read 20 min",
            pointsDone = 3,
            pointsMissed = -2,
            isActive = true,
            daysMask = WeekdayMask.EVERYDAY,
            sortOrder = 2
        )

        upsertHabit(
            name = "No sugar",
            pointsDone = 4,
            pointsMissed = -4,
            isActive = true,
            daysMask = WeekdayMask.EVERYDAY,
            sortOrder = 3
        )
    }
}

data class DailyState(val habits: List<DailyHabitItem>)

data class DailyHabitItem(
    val id: Long,
    val name: String,
    val pointsDone: Int,
    val pointsMissed: Int,
    val status: HabitStatus
)
