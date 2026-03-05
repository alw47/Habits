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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.liwo.habits.R
import com.liwo.habits.data.model.Habit
import com.liwo.habits.util.DateUtil
import com.liwo.habits.util.WeekdayMask
import com.liwo.habits.vm.HabitsViewModel
import kotlin.math.max
import androidx.compose.ui.unit.Dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HabitsScreen() {
    val vm: HabitsViewModel = hiltViewModel()
    val habits by vm.habits.collectAsState()
    val showAdd by vm.showAddDialog.collectAsState()
    val editing by vm.editingHabit.collectAsState()
    var confirmDelete by remember { mutableStateOf<Habit?>(null) }

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick = { vm.openAdd() },
                containerColor = MaterialTheme.colorScheme.primary
            ) {
                Icon(Icons.Filled.Add, contentDescription = stringResource(R.string.cd_add_habit))
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
                Text(stringResource(R.string.label_no_habits_empty))
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
                                            "${DateUtil.fmtPoints(h.pointsDone)} / ${DateUtil.fmtPoints(h.pointsMissed)} pts • ${maskLabel(h.daysMask)}",
                                            style = MaterialTheme.typography.bodyMedium,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                    Switch(
                                        checked = h.isActive,
                                        onCheckedChange = { vm.setActive(h.id, it) },
                                        modifier = Modifier.semantics {
                                            contentDescription = if (h.isActive) "${h.name}: active" else "${h.name}: inactive"
                                        }
                                    )
                                }

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    OutlinedButton(
                                        onClick = { vm.moveUp(habits, idx) },
                                        enabled = idx > 0
                                    ) { Icon(Icons.Filled.ArrowUpward, contentDescription = stringResource(R.string.cd_move_up)) }

                                    OutlinedButton(
                                        onClick = { vm.moveDown(habits, idx) },
                                        enabled = idx < habits.lastIndex
                                    ) { Icon(Icons.Filled.ArrowDownward, contentDescription = stringResource(R.string.cd_move_down)) }

                                    Spacer(Modifier.weight(1f))

                                    IconButton(onClick = { vm.openEdit(h) }) {
                                        Icon(Icons.Filled.Edit, contentDescription = stringResource(R.string.cd_edit))
                                    }

                                    IconButton(onClick = { confirmDelete = h }) {
                                        Icon(Icons.Filled.Delete, contentDescription = stringResource(R.string.cd_delete))
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
            title = stringResource(R.string.dialog_add_habit),
            initial = null,
            onDismiss = { vm.dismissAdd() },
            onSave = { name, pointsDone, pointsMissed, active, mask ->
                vm.saveHabit(
                    name = name,
                    pointsDone = pointsDone,
                    pointsMissed = pointsMissed,
                    isActive = active,
                    daysMask = mask
                )
            }
        )
    }

    if (editing != null) {
        HabitEditorDialog(
            title = stringResource(R.string.dialog_edit_habit),
            initial = editing,
            onDismiss = { vm.dismissEdit() },
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
            }
        )
    }

    if (confirmDelete != null) {
        val h = confirmDelete!!
        AlertDialog(
            onDismissRequest = { confirmDelete = null },
            title = { Text(stringResource(R.string.dialog_delete_habit_title)) },
            text = { Text(stringResource(R.string.dialog_delete_habit_text, h.name)) },
            confirmButton = {
                TextButton(onClick = {
                    vm.deleteHabit(h)
                    confirmDelete = null
                }) { Text(stringResource(R.string.btn_delete)) }
            },
            dismissButton = {
                TextButton(onClick = { confirmDelete = null }) { Text(stringResource(R.string.btn_cancel)) }
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
            ) { Text(stringResource(R.string.btn_save)) }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text(stringResource(R.string.btn_cancel)) } },
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
                    label = { Text(stringResource(R.string.field_habit_name)) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = pointsDoneText,
                    onValueChange = { pointsDoneText = it.filter { c -> c.isDigit() || c == '-' } },
                    label = { Text(stringResource(R.string.field_points_done)) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = pointsMissedText,
                    onValueChange = { pointsMissedText = it.filter { c -> c.isDigit() || c == '-' } },
                    label = { Text(stringResource(R.string.field_points_missed)) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(stringResource(R.string.label_active))
                    Switch(checked = active, onCheckedChange = { active = it })
                }

                Text(stringResource(R.string.label_available_days))

                // Presets (wrap)
                WrapRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalSpacing = 8.dp,
                    verticalSpacing = 8.dp
                ) {
                    AssistChip(onClick = { mask = WeekdayMask.EVERYDAY }, label = { Text(stringResource(R.string.preset_every_day)) })
                    AssistChip(onClick = { mask = WeekdayMask.WEEKDAYS }, label = { Text(stringResource(R.string.preset_weekdays)) })
                    AssistChip(onClick = { mask = WeekdayMask.WEEKENDS }, label = { Text(stringResource(R.string.preset_weekends)) })
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
        stringResource(R.string.day_mon) to WeekdayMask.MON,
        stringResource(R.string.day_tue) to WeekdayMask.TUE,
        stringResource(R.string.day_wed) to WeekdayMask.WED,
        stringResource(R.string.day_thu) to WeekdayMask.THU,
        stringResource(R.string.day_fri) to WeekdayMask.FRI,
        stringResource(R.string.day_sat) to WeekdayMask.SAT,
        stringResource(R.string.day_sun) to WeekdayMask.SUN
    )

    WrapRow(
        modifier = Modifier.fillMaxWidth(),
        horizontalSpacing = 8.dp,
        verticalSpacing = 8.dp
    ) {
        days.forEach { (label, bit) ->
            val selected = WeekdayMask.contains(mask, bit)
            FilterChip(
                selected = selected,
                onClick = {
                    val next = WeekdayMask.toggle(mask, bit)
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

private fun maskLabel(mask: Int): String = WeekdayMask.label(mask)
