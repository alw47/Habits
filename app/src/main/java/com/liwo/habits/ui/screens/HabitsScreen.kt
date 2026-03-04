package com.liwo.habits.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.liwo.habits.data.model.Habit
import com.liwo.habits.util.WeekdayMask
import com.liwo.habits.vm.HabitsViewModel
import kotlin.math.max
import androidx.compose.ui.unit.Dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HabitsScreen() {
    val vm: HabitsViewModel = viewModel()
    val habits by vm.habits.collectAsState()

    var editing by remember { mutableStateOf<Habit?>(null) }
    var showAdd by remember { mutableStateOf(false) }

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(onClick = { showAdd = true }) {
                Icon(Icons.Filled.Add, contentDescription = "Add habit")
            }
        }
    ) { padding ->
        Column(
            Modifier
                .padding(padding)
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            if (habits.isEmpty()) {
                Text("No habits yet. Tap + to create one.")
            } else {
                LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    itemsIndexed(habits, key = { _, h -> h.id }) { idx, h ->
                        ElevatedCard {
                            Column(
                                Modifier.padding(14.dp),
                                verticalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Row(
                                    Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Column(Modifier.weight(1f)) {
                                        Text(h.name, style = MaterialTheme.typography.titleMedium)
                                        Text(
                                            "${fmtPoints(h.pointsDone)} / ${fmtPoints(h.pointsMissed)} pts • ${maskLabel(h.daysMask)}",
                                            style = MaterialTheme.typography.bodyMedium,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                    Switch(
                                        checked = h.isActive,
                                        onCheckedChange = { vm.setActive(h.id, it) }
                                    )
                                }

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    OutlinedButton(
                                        onClick = { vm.moveUp(habits, idx) },
                                        enabled = idx > 0
                                    ) { Icon(Icons.Filled.ArrowUpward, contentDescription = "Up") }

                                    OutlinedButton(
                                        onClick = { vm.moveDown(habits, idx) },
                                        enabled = idx < habits.lastIndex
                                    ) { Icon(Icons.Filled.ArrowDownward, contentDescription = "Down") }

                                    Spacer(Modifier.weight(1f))

                                    IconButton(onClick = { editing = h }) {
                                        Icon(Icons.Filled.Edit, contentDescription = "Edit")
                                    }

                                    IconButton(onClick = { vm.deleteHabit(h) }) {
                                        Icon(Icons.Filled.Delete, contentDescription = "Delete")
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    if (showAdd) {
        HabitEditorDialog(
            title = "Add habit",
            initial = null,
            onDismiss = { showAdd = false },
            onSave = { name, pointsDone, pointsMissed, active, mask ->
                vm.saveHabit(
                    name = name,
                    pointsDone = pointsDone,
                    pointsMissed = pointsMissed,
                    isActive = active,
                    daysMask = mask
                )
                showAdd = false
            }
        )
    }

    if (editing != null) {
        HabitEditorDialog(
            title = "Edit habit",
            initial = editing,
            onDismiss = { editing = null },
            onSave = { name, pointsDone, pointsMissed, active, mask ->
                val h = editing!!
                vm.saveHabit(
                    id = h.id,
                    name = name,
                    pointsDone = pointsDone,
                    pointsMissed = pointsMissed,
                    isActive = active,
                    daysMask = mask
                )
                editing = null
            }
        )
    }
}

@Composable
private fun HabitEditorDialog(
    title: String,
    initial: Habit?,
    onDismiss: () -> Unit,
    onSave: (name: String, pointsDone: Int, pointsMissed: Int, active: Boolean, daysMask: Int) -> Unit
) {
    var name by remember { mutableStateOf(initial?.name ?: "") }
    var pointsDoneText by remember { mutableStateOf((initial?.pointsDone ?: 1).toString()) }
    var pointsMissedText by remember { mutableStateOf((initial?.pointsMissed ?: -1).toString()) }
    var active by remember { mutableStateOf(initial?.isActive ?: true) }
    var mask by remember { mutableIntStateOf(initial?.daysMask ?: WeekdayMask.EVERYDAY) }

    val scroll = rememberScrollState()

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title) },
        confirmButton = {
            val done = pointsDoneText.toIntOrNull() ?: 1
            val missed = pointsMissedText.toIntOrNull() ?: -1
            TextButton(
                onClick = { onSave(name.trim(), done, missed, active, mask) },
                enabled = name.trim().isNotEmpty()
            ) { Text("Save") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(scroll),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Habit name") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = pointsDoneText,
                    onValueChange = { pointsDoneText = it.filter { c -> c.isDigit() || c == '-' } },
                    label = { Text("Points when done") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = pointsMissedText,
                    onValueChange = { pointsMissedText = it.filter { c -> c.isDigit() || c == '-' } },
                    label = { Text("Points when missed") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Active")
                    Switch(checked = active, onCheckedChange = { active = it })
                }

                Text("Available days")

                // Presets (wrap)
                WrapRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalSpacing = 8.dp,
                    verticalSpacing = 8.dp
                ) {
                    AssistChip(onClick = { mask = WeekdayMask.EVERYDAY }, label = { Text("Every day") })
                    AssistChip(onClick = { mask = WeekdayMask.WEEKDAYS }, label = { Text("Weekdays") })
                    AssistChip(onClick = { mask = WeekdayMask.WEEKENDS }, label = { Text("Weekends") })
                }

                // Weekday chips (wrap below)
                WeekdaySelector(mask = mask, onMaskChange = { mask = it })

                Spacer(Modifier.height(4.dp))
            }
        }
    )
}

@Composable
private fun WeekdaySelector(mask: Int, onMaskChange: (Int) -> Unit) {
    val days = listOf(
        "Mon" to WeekdayMask.MON,
        "Tue" to WeekdayMask.TUE,
        "Wed" to WeekdayMask.WED,
        "Thu" to WeekdayMask.THU,
        "Fri" to WeekdayMask.FRI,
        "Sat" to WeekdayMask.SAT,
        "Sun" to WeekdayMask.SUN
    )

    WrapRow(
        modifier = Modifier.fillMaxWidth(),
        horizontalSpacing = 8.dp,
        verticalSpacing = 8.dp
    ) {
        days.forEach { (label, bit) ->
            val selected = (mask and bit) != 0
            FilterChip(
                selected = selected,
                onClick = {
                    val next = if (selected) (mask and bit.inv()) else (mask or bit)
                    onMaskChange(if (next == 0) mask else next) // prevent 0 days
                },
                label = { Text(label) }
            )
        }
    }
}

/**
 * Simple wrapping row (stable) — no experimental APIs.
 */
@Composable
private fun WrapRow(
    modifier: Modifier = Modifier,
    horizontalSpacing: Dp = 8.dp,
    verticalSpacing: Dp = 8.dp,
    content: @Composable () -> Unit
) {
    Layout(
        modifier = modifier,
        content = content
    ) { measurables, constraints ->
        val hSpace = horizontalSpacing.roundToPx()
        val vSpace = verticalSpacing.roundToPx()

        val maxWidth = constraints.maxWidth
        var x = 0
        var y = 0
        var rowHeight = 0

        val placeables = measurables.map { it.measure(constraints.copy(minWidth = 0)) }

        val positions = ArrayList<Pair<Int, Int>>(placeables.size)

        placeables.forEach { p ->
            if (x > 0 && x + p.width > maxWidth) {
                x = 0
                y += rowHeight + vSpace
                rowHeight = 0
            }
            positions.add(x to y)
            x += p.width + hSpace
            rowHeight = max(rowHeight, p.height)
        }

        val height = (y + rowHeight).coerceIn(constraints.minHeight, constraints.maxHeight)

        layout(width = maxWidth, height = height) {
            placeables.forEachIndexed { i, p ->
                val (px, py) = positions[i]
                p.placeRelative(px, py)
            }
        }
    }
}

private fun maskLabel(mask: Int): String = when (mask) {
    WeekdayMask.EVERYDAY -> "Every day"
    WeekdayMask.WEEKDAYS -> "Weekdays"
    WeekdayMask.WEEKENDS -> "Weekends"
    else -> "Custom"
}

private fun fmtPoints(p: Int): String = if (p > 0) "+$p" else p.toString()