package com.liwo.habits.data.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.liwo.habits.data.model.Habit
import com.liwo.habits.data.model.HabitLog
import com.liwo.habits.data.model.Redemption
import com.liwo.habits.data.model.Reward

@TypeConverters(Converters::class)
@Database(
    entities = [
        Habit::class,
        HabitLog::class,
        Reward::class,
        Redemption::class
    ],
    version = 1,
    exportSchema = true
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun habitDao(): HabitDao
    abstract fun habitLogDao(): HabitLogDao

    abstract fun rewardDao(): RewardDao
    abstract fun redemptionDao(): RedemptionDao

    companion object {
        @Volatile private var INSTANCE: AppDatabase? = null

        // The singleton is intentionally never closed. Room is designed to live for the
        // full application lifetime; the OS reclaims all SQLite resources when the process
        // exits. Calling close() mid-session would invalidate all active DAOs and flows.
        fun get(context: Context): AppDatabase =
            INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "habits.db"
                )
                    .build()
                    .also { INSTANCE = it }
            }
    }
}
