package com.liwo.habits.vm

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.liwo.habits.data.db.AppDatabase
import com.liwo.habits.data.model.HabitStatus
import com.liwo.habits.data.repo.DailyState
import com.liwo.habits.data.repo.HabitRepository
import com.liwo.habits.util.AppLogger
import com.liwo.habits.data.model.HabitLog
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter

class CalendarViewModel(app: Application) : AndroidViewModel(app) {

    private val db = AppDatabase.get(app)
    private val repo = HabitRepository(db)

    private val iso = DateTimeFormatter.ISO_LOCAL_DATE

    private val _visibleMonth = MutableStateFlow(YearMonth.now())
    val visibleMonth: StateFlow<YearMonth> = _visibleMonth

    private val _selectedDate = MutableStateFlow(LocalDate.now().format(iso))
    val selectedDate: StateFlow<String> = _selectedDate

    val monthLogs: StateFlow<Map<String, List<HabitLog>>> =
        _visibleMonth
            .flatMapLatest { month ->
                val start = month.atDay(1).toString()
                val end = month.atEndOfMonth().toString()
                db.habitLogDao().observeLogsForDateRange(start, end)
            }
            .map { logs -> logs.groupBy { it.date } }
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyMap())

    // Habits/logs for the selected day
    val dailyState: StateFlow<DailyState> =
        _selectedDate
            .flatMapLatest { repo.observeDailyState(it) }
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), DailyState(emptyList()))

    fun setSelectedDate(isoDate: String) {
        _selectedDate.value = isoDate
        val d = LocalDate.parse(isoDate, iso)
        _visibleMonth.value = YearMonth.from(d)
    }

    fun prevMonth() {
        _visibleMonth.value = _visibleMonth.value.minusMonths(1)
    }

    fun nextMonth() {
        _visibleMonth.value = _visibleMonth.value.plusMonths(1)
    }

    fun goToday() {
        val today = LocalDate.now()
        _visibleMonth.value = YearMonth.from(today)
        _selectedDate.value = today.format(iso)
    }

    fun monthLabel(): String {
        val ym = _visibleMonth.value
        // "March 2026"
        val monthName = ym.month.name.lowercase().replaceFirstChar { it.uppercase() }
        return "$monthName ${ym.year}"
    }

    fun setHabitStatus(habitId: Long, status: HabitStatus) {
        val date = _selectedDate.value
        viewModelScope.launch {
            repo.setStatus(habitId = habitId, date = date, status = status)
            AppLogger.i("Calendar", "Status set: habit=$habitId status=$status date=$date")
        }
    }
}