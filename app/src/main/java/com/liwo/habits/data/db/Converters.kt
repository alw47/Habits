package com.liwo.habits.data.db

import androidx.room.TypeConverter
import com.liwo.habits.data.model.HabitStatus

class Converters {
    @TypeConverter fun fromHabitStatus(s: HabitStatus): Int = s.value
    @TypeConverter fun toHabitStatus(v: Int): HabitStatus = HabitStatus.fromInt(v)
}
