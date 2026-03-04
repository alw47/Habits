package com.liwo.habits.util

import java.time.LocalDate
import java.time.format.DateTimeFormatter

object WeekdayMask {

    const val MON = 1 shl 0
    const val TUE = 1 shl 1
    const val WED = 1 shl 2
    const val THU = 1 shl 3
    const val FRI = 1 shl 4
    const val SAT = 1 shl 5
    const val SUN = 1 shl 6

    const val EVERYDAY = MON or TUE or WED or THU or FRI or SAT or SUN
    const val WEEKDAYS = MON or TUE or WED or THU or FRI
    const val WEEKENDS = SAT or SUN

    private val formatter = DateTimeFormatter.ISO_LOCAL_DATE

    fun isAllowed(mask: Int, dateIso: String): Boolean {
        val day = LocalDate.parse(dateIso, formatter).dayOfWeek
        val bit = when (day.value) {
            1 -> MON
            2 -> TUE
            3 -> WED
            4 -> THU
            5 -> FRI
            6 -> SAT
            7 -> SUN
            else -> MON
        }
        return (mask and bit) != 0
    }
}