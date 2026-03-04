package com.liwo.habits.util

import java.time.DayOfWeek
import java.time.LocalDate
import java.time.YearMonth

internal const val CALENDAR_CELL_COUNT = 42 // 6 weeks × 7 days

/**
 * Returns a list of exactly 42 dates (6 weeks x 7 days),
 * starting on Monday, filling with previous/next month days.
 */
internal fun buildMonthGrid(month: YearMonth): List<LocalDate> {
    val firstOfMonth = month.atDay(1)

    val offsetToMonday = when (firstOfMonth.dayOfWeek) {
        DayOfWeek.MONDAY -> 0
        DayOfWeek.TUESDAY -> 1
        DayOfWeek.WEDNESDAY -> 2
        DayOfWeek.THURSDAY -> 3
        DayOfWeek.FRIDAY -> 4
        DayOfWeek.SATURDAY -> 5
        DayOfWeek.SUNDAY -> 6
    }

    val gridStart = firstOfMonth.minusDays(offsetToMonday.toLong())
    return List(CALENDAR_CELL_COUNT) { i -> gridStart.plusDays(i.toLong()) }
}
