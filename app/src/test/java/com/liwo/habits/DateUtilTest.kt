package com.liwo.habits

import com.liwo.habits.util.DateUtil
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDate

class DateUtilTest {

    @Test
    fun `addDays adds positive delta`() {
        assertEquals("2026-03-05", DateUtil.addDays("2026-03-04", 1))
        assertEquals("2026-03-14", DateUtil.addDays("2026-03-04", 10))
    }

    @Test
    fun `addDays subtracts negative delta`() {
        assertEquals("2026-03-03", DateUtil.addDays("2026-03-04", -1))
        assertEquals("2026-02-25", DateUtil.addDays("2026-03-04", -7))
    }

    @Test
    fun `addDays crosses month boundary`() {
        assertEquals("2026-04-01", DateUtil.addDays("2026-03-31", 1))
        assertEquals("2026-02-28", DateUtil.addDays("2026-03-01", -1))
    }

    @Test
    fun `addDays crosses year boundary`() {
        assertEquals("2027-01-01", DateUtil.addDays("2026-12-31", 1))
        assertEquals("2025-12-31", DateUtil.addDays("2026-01-01", -1))
    }

    @Test
    fun `addDays with delta 0 returns same date`() {
        assertEquals("2026-03-04", DateUtil.addDays("2026-03-04", 0))
    }

    @Test
    fun `pretty formats date correctly`() {
        assertEquals("04 Mar 2026", DateUtil.pretty("2026-03-04"))
        assertEquals("01 Jan 2025", DateUtil.pretty("2025-01-01"))
        assertEquals("31 Dec 2026", DateUtil.pretty("2026-12-31"))
    }

    @Test
    fun `today returns a valid ISO date string`() {
        val today = DateUtil.today()
        val parsed = LocalDate.parse(today)
        assertEquals(LocalDate.now(), parsed)
    }

    @Test
    fun `today is consistent with addDays delta 0`() {
        val today = DateUtil.today()
        assertEquals(today, DateUtil.addDays(today, 0))
    }

    @Test
    fun `weekdayLabel returns non-empty string`() {
        assertTrue(DateUtil.weekdayLabel("2026-03-02").isNotEmpty()) // Monday
        assertTrue(DateUtil.weekdayLabel("2026-03-08").isNotEmpty()) // Sunday
    }
}
