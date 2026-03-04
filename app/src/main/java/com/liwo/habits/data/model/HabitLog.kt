package com.liwo.habits.data.model

import androidx.room.Entity
import androidx.room.Index

@Entity(
    tableName = "habit_logs",
    primaryKeys = ["habitId", "date"],
    indices = [Index("date")]
)
data class HabitLog(
    val habitId: Long,
    val date: String, // "YYYY-MM-DD"
    val status: Int   // 1 done, -1 missed, 0 none (0 means row removed)
)