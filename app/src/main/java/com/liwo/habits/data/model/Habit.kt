package com.liwo.habits.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "habits")
data class Habit(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,

    val pointsDone: Int = 1,
    val pointsMissed: Int = -1,

    val isActive: Boolean = true,
    val sortOrder: Int = 0,
    val daysMask: Int = 127
)