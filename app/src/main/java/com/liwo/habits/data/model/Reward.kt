package com.liwo.habits.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "rewards")
data class Reward(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val cost: Int,
    val description: String? = null,
    val isActive: Boolean = true,
    val isRecurring: Boolean = true,
    val sortOrder: Int = 0
)