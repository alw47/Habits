package com.liwo.habits.vm

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.liwo.habits.data.db.AppDatabase
import com.liwo.habits.data.repo.DailyState
import com.liwo.habits.data.repo.HabitRepository
import com.liwo.habits.util.DateUtil
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class DashboardState(
    val date: String,
    val pointsAvailable: Int,
    val dayTotal: Int,
    val daily: DailyState
)

class DashboardViewModel(app: Application) : AndroidViewModel(app) {

    private val db = AppDatabase.get(app)
    private val repo = HabitRepository(db)

    private val today = DateUtil.today()

    private val pointsAvailableFlow =
        combine(
            db.habitLogDao().observeTotalPointsEarned(),
            db.redemptionDao().observeTotalSpent()
        ) { earned, spent ->
            val e = (earned ?: 0L).toInt()
            val s = (spent ?: 0L).toInt()
            e - s
        }

    private val dailyFlow = repo.observeDailyState(today)

    val state: StateFlow<DashboardState> =
        combine(pointsAvailableFlow, dailyFlow) { available, daily ->

            val dayTotal = daily.habits.sumOf {
                when (it.status) {
                    1 -> it.pointsDone
                    -1 -> it.pointsMissed
                    else -> 0
                }
            }

            DashboardState(
                date = today,
                pointsAvailable = available,
                dayTotal = dayTotal,
                daily = daily
            )
        }.stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5_000),
            DashboardState(
                date = today,
                pointsAvailable = 0,
                dayTotal = 0,
                daily = DailyState(emptyList())
            )
        )

    fun setStatus(habitId: Long, status: Int) {
        viewModelScope.launch {
            repo.setStatus(habitId = habitId, date = today, status = status)
        }
    }
}