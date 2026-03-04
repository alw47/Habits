package com.liwo.habits

import android.app.Application
import app.cash.turbine.test
import com.liwo.habits.data.db.AppDatabase
import com.liwo.habits.data.db.HabitDao
import com.liwo.habits.data.db.HabitLogDao
import com.liwo.habits.data.db.RedemptionDao
import com.liwo.habits.data.model.HabitLog
import com.liwo.habits.data.model.HabitStatus
import com.liwo.habits.util.DateUtil
import com.liwo.habits.vm.DashboardViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever

class DashboardViewModelTest {

    private val testDispatcher = StandardTestDispatcher()

    private lateinit var app: Application
    private lateinit var db: AppDatabase
    private lateinit var logDao: HabitLogDao
    private lateinit var habitDao: HabitDao
    private lateinit var redemptionDao: RedemptionDao

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        app = mock()
        db = mock()
        logDao = mock()
        habitDao = mock()
        redemptionDao = mock()
        whenever(db.habitLogDao()).thenReturn(logDao)
        whenever(db.habitDao()).thenReturn(habitDao)
        whenever(db.redemptionDao()).thenReturn(redemptionDao)
        whenever(logDao.observeTotalPointsEarned()).thenReturn(flowOf(0L))
        whenever(redemptionDao.observeTotalSpent()).thenReturn(flowOf(0L))
        whenever(habitDao.observeActiveHabits()).thenReturn(flowOf(emptyList()))
        whenever(logDao.observeLogsForDate(any())).thenReturn(flowOf(emptyList()))
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun buildVm() = DashboardViewModel(app, db)

    @Test
    fun `initial selectedDate is today`() = runTest {
        val vm = buildVm()
        val today = DateUtil.today()

        vm.state.test {
            assertEquals(today, awaitItem().selectedDate)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `prevDay decrements selectedDate by one`() = runTest {
        val vm = buildVm()
        val yesterday = DateUtil.addDays(DateUtil.today(), -1)

        vm.state.test {
            awaitItem() // initial
            vm.prevDay()
            assertEquals(yesterday, awaitItem().selectedDate)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `nextDay increments selectedDate by one`() = runTest {
        val vm = buildVm()
        val tomorrow = DateUtil.addDays(DateUtil.today(), 1)

        vm.state.test {
            awaitItem() // initial
            vm.nextDay()
            assertEquals(tomorrow, awaitItem().selectedDate)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `goToday resets to today after navigation`() = runTest {
        val vm = buildVm()
        val today = DateUtil.today()

        vm.state.test {
            awaitItem() // today
            vm.prevDay()
            awaitItem() // yesterday
            vm.goToday()
            assertEquals(today, awaitItem().selectedDate)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `setStatus uses selected date not hardcoded today`() = runTest {
        val vm = buildVm()
        val yesterday = DateUtil.addDays(DateUtil.today(), -1)

        vm.state.test {
            awaitItem()
            vm.prevDay()
            awaitItem() // wait for state to update to yesterday

            vm.setStatus(habitId = 1L, status = HabitStatus.DONE)
            testDispatcher.scheduler.advanceUntilIdle()

            verify(logDao).upsertLog(
                HabitLog(habitId = 1L, date = yesterday, status = HabitStatus.DONE)
            )
            cancelAndIgnoreRemainingEvents()
        }
    }
}
