package com.liwo.habits.vm

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.liwo.habits.data.db.AppDatabase
import com.liwo.habits.data.model.Habit
import com.liwo.habits.data.repo.HabitRepository
import com.liwo.habits.util.AppLogger
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class HabitsViewModel(app: Application) : AndroidViewModel(app) {

    private val repo = HabitRepository(AppDatabase.get(app))

    val habits: StateFlow<List<Habit>> =
        repo.observeHabits()
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    fun saveHabit(
        id: Long = 0,
        name: String,
        pointsDone: Int,
        pointsMissed: Int,
        isActive: Boolean,
        daysMask: Int
    ) {
        viewModelScope.launch {
            repo.upsertHabit(
                id = id,
                name = name,
                pointsDone = pointsDone,
                pointsMissed = pointsMissed,
                isActive = isActive,
                daysMask = daysMask
            )
            AppLogger.i("Habits", "Habit saved: $name")
        }
    }

    fun deleteHabit(habit: Habit) {
        viewModelScope.launch {
            repo.deleteHabit(habit)
            AppLogger.i("Habits", "Habit deleted: ${habit.name}")
        }
    }

    fun setActive(habitId: Long, active: Boolean) {
        viewModelScope.launch {
            repo.setActive(habitId, active)
            AppLogger.i("Habits", "Habit $habitId active=$active")
        }
    }

    fun moveUp(habits: List<Habit>, index: Int) {
        if (index <= 0) return
        viewModelScope.launch { repo.swapSort(habits[index], habits[index - 1]) }
    }

    fun moveDown(habits: List<Habit>, index: Int) {
        if (index >= habits.lastIndex) return
        viewModelScope.launch { repo.swapSort(habits[index], habits[index + 1]) }
    }
}