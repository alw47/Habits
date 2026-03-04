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
    version = 1, // <-- bump this when schema changes
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun habitDao(): HabitDao
    abstract fun habitLogDao(): HabitLogDao

    abstract fun rewardDao(): RewardDao
    abstract fun redemptionDao(): RedemptionDao

    companion object {
        @Volatile private var INSTANCE: AppDatabase? = null

        fun get(context: Context): AppDatabase =
            INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "habits.db"
                )
                    .fallbackToDestructiveMigration(dropAllTables = true)
                    .build()
                    .also { INSTANCE = it }
            }
    }
}