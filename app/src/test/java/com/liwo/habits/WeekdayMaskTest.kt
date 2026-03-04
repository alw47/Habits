package com.liwo.habits

import com.liwo.habits.util.WeekdayMask
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class WeekdayMaskTest {

    // 2026-03-02 = Monday, 2026-03-07 = Saturday, 2026-03-08 = Sunday

    @Test
    fun `WEEKDAYS allows Monday through Friday`() {
        assertTrue(WeekdayMask.isAllowed(WeekdayMask.WEEKDAYS, "2026-03-02")) // Mon
        assertTrue(WeekdayMask.isAllowed(WeekdayMask.WEEKDAYS, "2026-03-03")) // Tue
        assertTrue(WeekdayMask.isAllowed(WeekdayMask.WEEKDAYS, "2026-03-04")) // Wed
        assertTrue(WeekdayMask.isAllowed(WeekdayMask.WEEKDAYS, "2026-03-05")) // Thu
        assertTrue(WeekdayMask.isAllowed(WeekdayMask.WEEKDAYS, "2026-03-06")) // Fri
    }

    @Test
    fun `WEEKDAYS blocks Saturday and Sunday`() {
        assertFalse(WeekdayMask.isAllowed(WeekdayMask.WEEKDAYS, "2026-03-07")) // Sat
        assertFalse(WeekdayMask.isAllowed(WeekdayMask.WEEKDAYS, "2026-03-08")) // Sun
    }

    @Test
    fun `WEEKENDS allows Saturday and Sunday`() {
        assertTrue(WeekdayMask.isAllowed(WeekdayMask.WEEKENDS, "2026-03-07")) // Sat
        assertTrue(WeekdayMask.isAllowed(WeekdayMask.WEEKENDS, "2026-03-08")) // Sun
    }

    @Test
    fun `WEEKENDS blocks Monday through Friday`() {
        assertFalse(WeekdayMask.isAllowed(WeekdayMask.WEEKENDS, "2026-03-02")) // Mon
        assertFalse(WeekdayMask.isAllowed(WeekdayMask.WEEKENDS, "2026-03-06")) // Fri
    }

    @Test
    fun `EVERYDAY allows all days`() {
        assertTrue(WeekdayMask.isAllowed(WeekdayMask.EVERYDAY, "2026-03-02")) // Mon
        assertTrue(WeekdayMask.isAllowed(WeekdayMask.EVERYDAY, "2026-03-07")) // Sat
        assertTrue(WeekdayMask.isAllowed(WeekdayMask.EVERYDAY, "2026-03-08")) // Sun
    }

    @Test
    fun `mask 0 blocks every day`() {
        assertFalse(WeekdayMask.isAllowed(0, "2026-03-02"))
        assertFalse(WeekdayMask.isAllowed(0, "2026-03-07"))
        assertFalse(WeekdayMask.isAllowed(0, "2026-03-08"))
    }

    @Test
    fun `individual day bits allow only that day`() {
        assertTrue(WeekdayMask.isAllowed(WeekdayMask.MON, "2026-03-02"))
        assertFalse(WeekdayMask.isAllowed(WeekdayMask.MON, "2026-03-03"))

        assertTrue(WeekdayMask.isAllowed(WeekdayMask.SUN, "2026-03-08"))
        assertFalse(WeekdayMask.isAllowed(WeekdayMask.SUN, "2026-03-07"))
    }
}
