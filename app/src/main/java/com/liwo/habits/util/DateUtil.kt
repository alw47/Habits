package com.liwo.habits.util

import kotlinx.coroutines.flow.MutableStateFlow
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.util.Locale

object DateUtil {

    private val isoFormatter = DateTimeFormatter.ISO_LOCAL_DATE
    private val prettyFormatter = DateTimeFormatter.ofPattern("dd MMM yyyy")

    fun today(): String =
        LocalDate.now().format(isoFormatter)

    fun addDays(dateIso: String, delta: Int): String =
        LocalDate.parse(dateIso, isoFormatter)
            .plusDays(delta.toLong())
            .format(isoFormatter)

    fun pretty(dateIso: String): String =
        LocalDate.parse(dateIso, isoFormatter)
            .format(prettyFormatter)

    fun weekdayLabel(dateIso: String): String =
        LocalDate.parse(dateIso, isoFormatter)
            .dayOfWeek
            .getDisplayName(TextStyle.FULL, Locale.getDefault())

    fun mutableTodayState(): MutableStateFlow<String> =
        MutableStateFlow(today())
}