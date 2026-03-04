package com.liwo.habits.data.model

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "redemptions",
    indices = [Index("rewardId"), Index("createdAtMillis")]
)
data class Redemption(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val rewardId: Long,
    val rewardName: String,
    val cost: Int,
    val createdAtMillis: Long
)