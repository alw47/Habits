package com.liwo.habits.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.liwo.habits.data.model.HabitStatus
import com.liwo.habits.data.repo.DailyHabitItem
import com.liwo.habits.ui.components.StatusSelector
import com.liwo.habits.util.DateUtil
import com.liwo.habits.vm.DashboardViewModel

@Composable
fun DashboardScreen() {

    val vm: DashboardViewModel = viewModel()
    val state by vm.state.collectAsState()

    val isToday = state.selectedDate == DateUtil.today()
    val dateLabel = if (isToday) "Today" else DateUtil.pretty(state.selectedDate)

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {

        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                IconButton(onClick = { vm.prevDay() }) {
                    Icon(
                        Icons.AutoMirrored.Filled.KeyboardArrowLeft,
                        contentDescription = "Previous day"
                    )
                }

                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(dateLabel, style = MaterialTheme.typography.titleMedium)
                    if (!isToday) {
                        Spacer(Modifier.padding(top = 4.dp))
                        OutlinedButton(onClick = { vm.goToday() }) {
                            Text("Today")
                        }
                    }
                }

                IconButton(onClick = { vm.nextDay() }) {
                    Icon(
                        Icons.AutoMirrored.Filled.KeyboardArrowRight,
                        contentDescription = "Next day"
                    )
                }
            }
        }

        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {

                ElevatedCard(modifier = Modifier.weight(1f)) {
                    Column(
                        modifier = Modifier.padding(14.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(
                            "Current points",
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            "${state.pointsAvailable}",
                            style = MaterialTheme.typography.headlineMedium
                        )
                    }
                }

                ElevatedCard(modifier = Modifier.weight(1f)) {
                    Column(
                        modifier = Modifier.padding(14.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(
                            "Total for the day",
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            "${state.dayTotal}",
                            style = MaterialTheme.typography.headlineMedium
                        )
                    }
                }
            }
        }

        if (state.daily.habits.isEmpty()) {
            item {
                Text(
                    "No habits scheduled for this day.",
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        } else {

            itemsIndexed(
                items = state.daily.habits,
                key = { index, item -> "${item.id}-$index" }
            ) { _, item ->
                HabitStatusCard(
                    item = item,
                    onSetStatus = { status -> vm.setStatus(item.id, status) }
                )
            }
        }
    }
}

@Composable
private fun HabitStatusCard(
    item: DailyHabitItem,
    onSetStatus: (HabitStatus) -> Unit
) {
    ElevatedCard {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text(item.name, style = MaterialTheme.typography.titleMedium)

            val subtitle =
                when (item.status) {
                    HabitStatus.DONE -> "Done (${fmtPoints(item.pointsDone)})"
                    HabitStatus.MISSED -> "Missed (${fmtPoints(item.pointsMissed)})"
                    HabitStatus.NONE -> "None"
                }

            Text(subtitle, color = MaterialTheme.colorScheme.onSurfaceVariant)

            StatusSelector(
                status = item.status,
                onChange = { newStatus -> onSetStatus(newStatus) }
            )
        }
    }
}

private fun fmtPoints(p: Int): String =
    if (p > 0) "+$p" else p.toString()
