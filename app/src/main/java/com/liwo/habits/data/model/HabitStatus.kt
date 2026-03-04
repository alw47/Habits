package com.liwo.habits.data.model

enum class HabitStatus(val value: Int) {
    DONE(1), MISSED(-1), NONE(0);
    companion object {
        fun fromInt(v: Int): HabitStatus = entries.firstOrNull { it.value == v } ?: NONE
    }
}
