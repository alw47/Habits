package com.liwo.habits.vm

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.liwo.habits.data.model.HabitLog
import com.liwo.habits.data.model.HabitStatus
import com.liwo.habits.data.repo.DailyState
import com.liwo.habits.data.repo.HabitRepository
import com.liwo.habits.util.AppLogger
import dagger.hilt.android.lifecycle.HiltViewModel
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
import java.time.format.TextStyle
import java.util.Locale
import javax.inject.Inject

@HiltViewModel
class CalendarViewModel @Inject constructor(
    private val repo: HabitRepository
) : ViewModel() {

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
                repo.observeLogsForDateRange(start, end)
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
        val monthName = ym.month.getDisplayName(TextStyle.FULL_STANDALONE, Locale.getDefault())
            .replaceFirstChar { it.uppercaseChar() }
        return "$monthName ${ym.year}"
    }

    fun setHabitStatus(habitId: Long, status: HabitStatus) {
        val date = _selectedDate.value
        viewModelScope.launch {
            try {
                repo.setStatus(habitId = habitId, date = date, status = status)
                AppLogger.i("Calendar", "Status set: habit=$habitId status=$status date=$date")
            } catch (t: Throwable) {
                AppLogger.e("Calendar", "Failed to set status: habit=$habitId", t)
            }
        }
    }
}
