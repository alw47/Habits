package com.liwo.habits.vm

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.liwo.habits.data.model.Habit
import com.liwo.habits.data.repo.HabitRepository
import com.liwo.habits.util.AppLogger
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HabitsViewModel @Inject constructor(
    private val repo: HabitRepository
) : ViewModel() {

    val habits: StateFlow<List<Habit>> =
        repo.observeHabits()
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    private val _showAddDialog = MutableStateFlow(false)
    val showAddDialog: StateFlow<Boolean> = _showAddDialog

    private val _editingHabit = MutableStateFlow<Habit?>(null)
    val editingHabit: StateFlow<Habit?> = _editingHabit

    fun openAdd() { _showAddDialog.value = true }
    fun dismissAdd() { _showAddDialog.value = false }
    fun openEdit(habit: Habit) { _editingHabit.value = habit }
    fun dismissEdit() { _editingHabit.value = null }

    fun saveHabit(
        id: Long = 0,
        name: String,
        pointsDone: Int,
        pointsMissed: Int,
        isActive: Boolean,
        daysMask: Int
    ) {
        viewModelScope.launch {
            try {
                repo.upsertHabit(
                    id = id,
                    name = name,
                    pointsDone = pointsDone,
                    pointsMissed = pointsMissed,
                    isActive = isActive,
                    daysMask = daysMask
                )
                AppLogger.i("Habits", "Habit saved: $name")
                dismissAdd()
                dismissEdit()
            } catch (t: Throwable) {
                AppLogger.e("Habits", "Failed to save habit: $name", t)
            }
        }
    }

    fun deleteHabit(habit: Habit) {
        viewModelScope.launch {
            try {
                repo.deleteHabit(habit)
                AppLogger.i("Habits", "Habit deleted: ${habit.name}")
            } catch (t: Throwable) {
                AppLogger.e("Habits", "Failed to delete habit: ${habit.name}", t)
            }
        }
    }

    fun setActive(habitId: Long, active: Boolean) {
        viewModelScope.launch {
            try {
                repo.setActive(habitId, active)
                AppLogger.i("Habits", "Habit $habitId active=$active")
            } catch (t: Throwable) {
                AppLogger.e("Habits", "Failed to set active: habit=$habitId", t)
            }
        }
    }

    fun moveUp(habits: List<Habit>, index: Int) {
        if (index <= 0) return
        viewModelScope.launch {
            try {
                repo.swapSort(habits[index], habits[index - 1])
            } catch (t: Throwable) {
                AppLogger.e("Habits", "Failed to move habit up at index=$index", t)
            }
        }
    }

    fun moveDown(habits: List<Habit>, index: Int) {
        if (index >= habits.lastIndex) return
        viewModelScope.launch {
            try {
                repo.swapSort(habits[index], habits[index + 1])
            } catch (t: Throwable) {
                AppLogger.e("Habits", "Failed to move habit down at index=$index", t)
            }
        }
    }
}
