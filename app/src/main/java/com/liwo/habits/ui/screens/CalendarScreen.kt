package com.liwo.habits.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Today
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.liwo.habits.data.model.HabitLog
import com.liwo.habits.data.model.HabitStatus
import com.liwo.habits.data.repo.DailyHabitItem
import com.liwo.habits.ui.components.StatusSelector
import com.liwo.habits.util.DateUtil
import com.liwo.habits.vm.CalendarViewModel
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CalendarScreen() {
    val vm: CalendarViewModel = viewModel()

    val visibleMonth by vm.visibleMonth.collectAsState()
    val selectedIso by vm.selectedDate.collectAsState()
    val dailyState by vm.dailyState.collectAsState()
    val monthLogs by vm.monthLogs.collectAsState()

    val iso = remember { DateTimeFormatter.ISO_LOCAL_DATE }
    val selectedDate = remember(selectedIso) { LocalDate.parse(selectedIso, iso) }

    val days = remember(visibleMonth) { buildMonthGrid(visibleMonth) }

    var showSheet by remember { mutableStateOf(false) }
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    Scaffold { padding ->
        Column(
            Modifier
                .padding(padding)
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {

            ElevatedCard {
                Row(
                    Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {

                    IconButton(onClick = { vm.prevMonth() }) {
                        Icon(
                            Icons.AutoMirrored.Filled.KeyboardArrowLeft,
                            contentDescription = "Previous month"
                        )
                    }

                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(vm.monthLabel(), style = MaterialTheme.typography.titleMedium)

                        Text(
                            DateUtil.pretty(selectedIso),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        Spacer(Modifier.height(6.dp))

                        OutlinedButton(onClick = { vm.goToday() }) {
                            Icon(Icons.Filled.Today, null)
                            Spacer(Modifier.width(6.dp))
                            Text("Today")
                        }
                    }

                    IconButton(onClick = { vm.nextMonth() }) {
                        Icon(
                            Icons.AutoMirrored.Filled.KeyboardArrowRight,
                            contentDescription = "Next month"
                        )
                    }
                }
            }

            Row(Modifier.fillMaxWidth()) {
                listOf("Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun").forEach {
                    Text(
                        text = it,
                        modifier = Modifier.weight(1f),
                        textAlign = TextAlign.Center,
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                for (row in 0 until 6) {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        for (col in 0 until 7) {
                            val idx = row * 7 + col
                            val day = days[idx]
                            val inMonth = (day.month == visibleMonth.month && day.year == visibleMonth.year)

                            DayCell(
                                day = day,
                                selected = day == selectedDate,
                                inMonth = inMonth,
                                logs = monthLogs[day.format(iso)] ?: emptyList(),
                                onClick = {
                                    vm.setSelectedDate(day.format(iso))
                                    showSheet = true
                                },
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }
            }
        }
    }

    // Bottom sheet: edit habits for selected day (USES SAME SELECTOR AS DASHBOARD)
    if (showSheet) {
        ModalBottomSheet(
            onDismissRequest = { showSheet = false },
            sheetState = sheetState
        ) {
            SheetHeader(
                title = DateUtil.pretty(selectedIso),
                onClose = { showSheet = false }
            )

            if (dailyState.habits.isEmpty()) {
                Text(
                    "No habits scheduled for this day.",
                    modifier = Modifier.padding(16.dp),
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            } else {
                Column(
                    Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    dailyState.habits.forEach { item ->
                        HabitStatusCardInSheet(
                            item = item,
                            onSet = { status -> vm.setHabitStatus(item.id, status) }
                        )
                    }
                }
            }

            Spacer(Modifier.height(24.dp))
        }
    }
}

@Composable
private fun SheetHeader(title: String, onClose: () -> Unit) {
    Row(
        Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            title,
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.weight(1f)
        )
        IconButton(onClick = onClose) {
            Icon(Icons.Filled.Close, contentDescription = "Close")
        }
    }
}

@Composable
private fun HabitStatusCardInSheet(
    item: DailyHabitItem,
    onSet: (HabitStatus) -> Unit
) {
    ElevatedCard {
        Column(
            Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {

            Text(item.name, style = MaterialTheme.typography.titleSmall)

            Text(
                "Done: ${fmtPoints(item.pointsDone)}  •  Missed: ${fmtPoints(item.pointsMissed)}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            // SAME segmented selector as Dashboard (Done / None / Missed, centered)
            StatusSelector(
                status = item.status,
                onChange = { newStatus -> onSet(newStatus) }
            )
        }
    }
}

@Composable
private fun DayCell(
    day: LocalDate,
    selected: Boolean,
    inMonth: Boolean,
    logs: List<HabitLog>,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val shape = RoundedCornerShape(12.dp)

    val bgColor =
        if (selected)
            MaterialTheme.colorScheme.primary.copy(alpha = 0.18f)
        else
            MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f)

    val textColor =
        when {
            selected -> MaterialTheme.colorScheme.primary
            inMonth -> MaterialTheme.colorScheme.onSurface
            else -> MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.55f)
        }

    val dotColor = when {
        logs.isEmpty() -> null
        logs.all { it.status == HabitStatus.DONE } -> MaterialTheme.colorScheme.primary
        logs.all { it.status == HabitStatus.MISSED } -> MaterialTheme.colorScheme.error
        else -> Color(0xFFFACC15)
    }

    Box(
        modifier = modifier
            .aspectRatio(1f)
            .background(bgColor, shape)
            .clickable { onClick() }
            .padding(8.dp),
        contentAlignment = Alignment.TopEnd
    ) {
        Text(
            text = day.dayOfMonth.toString(),
            style = MaterialTheme.typography.titleSmall,
            color = textColor
        )
        if (dotColor != null) {
            Box(
                Modifier
                    .size(6.dp)
                    .background(dotColor, CircleShape)
                    .align(Alignment.BottomCenter)
            )
        }
    }
}

/**
 * Returns a list of exactly 42 dates (6 weeks x 7 days),
 * starting on Monday, filling with previous/next month days.
 */
private fun buildMonthGrid(month: YearMonth): List<LocalDate> {
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

private const val CALENDAR_CELL_COUNT = 42 // 6 weeks × 7 days

private fun fmtPoints(p: Int): String =
    if (p > 0) "+$p" else p.toString()