package com.liwo.habits

import com.liwo.habits.util.CALENDAR_CELL_COUNT
import com.liwo.habits.util.buildMonthGrid
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.YearMonth

class CalendarGridTest {

    @Test
    fun `grid always contains exactly 42 cells`() {
        listOf(
            YearMonth.of(2026, 1),
            YearMonth.of(2026, 2),
            YearMonth.of(2026, 3),
            YearMonth.of(2025, 12)
        ).forEach { month ->
            assertEquals(CALENDAR_CELL_COUNT, buildMonthGrid(month).size)
        }
    }

    @Test
    fun `first cell is always a Monday`() {
        listOf(
            YearMonth.of(2026, 1),
            YearMonth.of(2026, 2),
            YearMonth.of(2026, 3),
            YearMonth.of(2025, 12),
            YearMonth.of(2025, 6)
        ).forEach { month ->
            assertEquals(DayOfWeek.MONDAY, buildMonthGrid(month).first().dayOfWeek)
        }
    }

    @Test
    fun `grid for March 2026 starts on 23 Feb (Sunday first day)`() {
        // March 1, 2026 = Sunday → offset 6 → grid starts Feb 23
        val grid = buildMonthGrid(YearMonth.of(2026, 3))
        assertEquals(LocalDate.of(2026, 2, 23), grid.first())
    }

    @Test
    fun `grid for January 2026 starts on 29 Dec 2025 (Thursday first day)`() {
        // Jan 1, 2026 = Thursday → offset 3 → grid starts Dec 29, 2025
        val grid = buildMonthGrid(YearMonth.of(2026, 1))
        assertEquals(LocalDate.of(2025, 12, 29), grid.first())
    }

    @Test
    fun `grid for February 2021 starts on 1 Feb (Monday first day)`() {
        // Feb 1, 2021 = Monday → offset 0 → grid starts Feb 1
        val grid = buildMonthGrid(YearMonth.of(2021, 2))
        assertEquals(LocalDate.of(2021, 2, 1), grid.first())
    }

    @Test
    fun `grid cells are consecutive days`() {
        val grid = buildMonthGrid(YearMonth.of(2026, 3))
        for (i in 1 until grid.size) {
            assertEquals(grid[i - 1].plusDays(1), grid[i])
        }
    }

    @Test
    fun `grid contains all days of the requested month`() {
        val month = YearMonth.of(2026, 3)
        val grid = buildMonthGrid(month)
        val monthDays = (1..month.lengthOfMonth()).map { LocalDate.of(2026, 3, it) }
        assertTrue(grid.containsAll(monthDays))
    }
}
