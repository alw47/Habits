package com.liwo.habits.vm

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.liwo.habits.data.db.AppDatabase
import com.liwo.habits.data.model.HabitStatus
import com.liwo.habits.data.repo.DailyState
import com.liwo.habits.data.repo.HabitRepository
import com.liwo.habits.util.AppLogger
import com.liwo.habits.util.DateUtil
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class DashboardState(
    val date: String,
    val selectedDate: String,
    val pointsAvailable: Int,
    val dayTotal: Int,
    val daily: DailyState
)

class DashboardViewModel(
    app: Application,
    private val db: AppDatabase
) : AndroidViewModel(app) {

    constructor(app: Application) : this(app, AppDatabase.get(app))

    private val repo = HabitRepository(db)

    private val today = DateUtil.today()

    private val _selectedDate = MutableStateFlow(today)
    val selectedDate: StateFlow<String> = _selectedDate

    fun prevDay() { _selectedDate.value = DateUtil.addDays(_selectedDate.value, -1) }
    fun nextDay() { _selectedDate.value = DateUtil.addDays(_selectedDate.value, 1) }
    fun goToday() { _selectedDate.value = today }

    private val pointsAvailableFlow =
        combine(
            db.habitLogDao().observeTotalPointsEarned(),
            db.redemptionDao().observeTotalSpent()
        ) { earned, spent ->
            val e = earned.toInt()
            val s = spent.toInt()
            e - s
        }

    private val dailyFlow = _selectedDate.flatMapLatest { repo.observeDailyState(it) }

    val state: StateFlow<DashboardState> =
        combine(pointsAvailableFlow, dailyFlow, _selectedDate) { available, daily, date ->

            val dayTotal = daily.habits.sumOf {
                when (it.status) {
                    HabitStatus.DONE -> it.pointsDone
                    HabitStatus.MISSED -> it.pointsMissed
                    HabitStatus.NONE -> 0
                }
            }

            DashboardState(
                date = date,
                selectedDate = date,
                pointsAvailable = available,
                dayTotal = dayTotal,
                daily = daily
            )
        }.stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5_000),
            DashboardState(
                date = today,
                selectedDate = today,
                pointsAvailable = 0,
                dayTotal = 0,
                daily = DailyState(emptyList())
            )
        )

    fun setStatus(habitId: Long, status: HabitStatus) {
        val date = _selectedDate.value
        viewModelScope.launch {
            repo.setStatus(habitId = habitId, date = date, status = status)
            AppLogger.i("Dashboard", "Status set: habit=$habitId status=$status date=$date")
        }
    }
}
