package com.liwo.habits

import com.liwo.habits.data.db.AppDatabase
import com.liwo.habits.data.db.HabitDao
import com.liwo.habits.data.db.HabitLogDao
import com.liwo.habits.data.model.HabitLog
import com.liwo.habits.data.model.HabitStatus
import com.liwo.habits.data.repo.HabitRepository
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.never
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever

class HabitRepositoryTest {

    private lateinit var db: AppDatabase
    private lateinit var logDao: HabitLogDao
    private lateinit var habitDao: HabitDao
    private lateinit var repo: HabitRepository

    @Before
    fun setUp() {
        db = mock()
        logDao = mock()
        habitDao = mock()
        whenever(db.habitLogDao()).thenReturn(logDao)
        whenever(db.habitDao()).thenReturn(habitDao)
        repo = HabitRepository(db)
    }

    @Test
    fun `setStatus DONE upserts a log with correct fields`() = runTest {
        repo.setStatus(habitId = 1L, date = "2026-03-04", status = HabitStatus.DONE)

        verify(logDao).upsertLog(
            HabitLog(habitId = 1L, date = "2026-03-04", status = HabitStatus.DONE)
        )
        verify(logDao, never()).deleteLog(org.mockito.kotlin.any(), org.mockito.kotlin.any())
    }

    @Test
    fun `setStatus MISSED upserts a log with correct fields`() = runTest {
        repo.setStatus(habitId = 2L, date = "2026-03-04", status = HabitStatus.MISSED)

        verify(logDao).upsertLog(
            HabitLog(habitId = 2L, date = "2026-03-04", status = HabitStatus.MISSED)
        )
        verify(logDao, never()).deleteLog(org.mockito.kotlin.any(), org.mockito.kotlin.any())
    }

    @Test
    fun `setStatus NONE deletes the log and never upserts`() = runTest {
        repo.setStatus(habitId = 3L, date = "2026-03-04", status = HabitStatus.NONE)

        verify(logDao).deleteLog(habitId = 3L, date = "2026-03-04")
        verify(logDao, never()).upsertLog(org.mockito.kotlin.any())
    }

    @Test
    fun `setStatus uses the exact date provided`() = runTest {
        val date = "2025-12-31"
        repo.setStatus(habitId = 5L, date = date, status = HabitStatus.DONE)

        verify(logDao).upsertLog(
            HabitLog(habitId = 5L, date = date, status = HabitStatus.DONE)
        )
    }
}
