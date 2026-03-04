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
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.liwo.habits.data.repo.DailyHabitItem
import com.liwo.habits.ui.components.StatusSelector
import com.liwo.habits.vm.DashboardViewModel

@Composable
fun DashboardScreen() {

    val vm: DashboardViewModel = viewModel()
    val state by vm.state.collectAsState()

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {

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

        item {
            Text("Today", style = MaterialTheme.typography.titleMedium)
        }

        if (state.daily.habits.isEmpty()) {
            item {
                Text(
                    "No habits scheduled for today.",
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        } else {

            itemsIndexed(
                items = state.daily.habits,
                key = { index, item -> "${item.id}-$index" } // unique key (prevents LazyColumn crashes)
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
    onSetStatus: (Int) -> Unit
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
                    1 -> "Done (${fmtPoints(item.pointsDone)})"
                    -1 -> "Missed (${fmtPoints(item.pointsMissed)})"
                    else -> "None"
                }

            Text(subtitle, color = MaterialTheme.colorScheme.onSurfaceVariant)

            // Shared selector (same one Calendar uses)
            StatusSelector(
                status = item.status,
                onChange = { newStatus -> onSetStatus(newStatus) }
            )
        }
    }
}

private fun fmtPoints(p: Int): String =
    if (p > 0) "+$p" else p.toString()