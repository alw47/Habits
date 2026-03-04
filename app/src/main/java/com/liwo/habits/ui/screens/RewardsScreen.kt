package com.liwo.habits.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Undo
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Redeem
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.liwo.habits.data.model.Reward
import com.liwo.habits.vm.RewardsViewModel
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

@Composable
fun RewardsScreen() {
    val vm: RewardsViewModel = viewModel()
    val state by vm.state.collectAsState()
    val history by vm.history.collectAsState()

    var showAdd by remember { mutableStateOf(false) }
    var editing by remember { mutableStateOf<Reward?>(null) }
    var confirmRedeem by remember { mutableStateOf<Reward?>(null) }

    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    fun showMsg(msg: String) {
        scope.launch { snackbarHostState.showSnackbar(msg) }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        floatingActionButton = {
            FloatingActionButton(onClick = { showAdd = true }) {
                Icon(Icons.Filled.Add, contentDescription = "Add reward")
            }
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {

            item {
                ElevatedCard {
                    Column(
                        modifier = Modifier.padding(14.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(
                            text = "Points available",
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = "${state.pointsAvailable}",
                            style = MaterialTheme.typography.headlineMedium
                        )
                    }
                }
            }

            item {
                Text("Rewards", style = MaterialTheme.typography.titleMedium)
            }

            itemsIndexed(
                items = state.rewards,
                key = { index, item -> "${item.reward.id}-$index" }
            ) { _, item ->
                val r = item.reward

                ElevatedCard {
                    Column(
                        modifier = Modifier.padding(14.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(r.name, style = MaterialTheme.typography.titleMedium)

                                val mode = if (r.isRecurring) "Recurring" else "Single redeem"
                                Text(
                                    text = "${r.cost} pts • $mode",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )

                                val desc = r.description
                                if (!desc.isNullOrBlank()) {
                                    Text(
                                        text = desc,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }

                                if (item.alreadyRedeemed && !r.isRecurring) {
                                    Text(
                                        text = "Already redeemed",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                } else if (!item.canRedeem) {
                                    Text(
                                        text = if (item.needMore > 0) "Need ${item.needMore} more points" else "Unavailable",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }

                            Switch(
                                checked = r.isActive,
                                onCheckedChange = { active ->
                                    vm.setActive(r.id, active) { msg -> showMsg(msg) }
                                },
                                modifier = Modifier.semantics {
                                    contentDescription = if (r.isActive) "${r.name}: active" else "${r.name}: inactive"
                                }
                            )
                        }

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Button(
                                onClick = { confirmRedeem = r },
                                enabled = item.canRedeem
                            ) {
                                Icon(Icons.Filled.Redeem, contentDescription = null)
                                Spacer(Modifier.width(8.dp))
                                Text("Redeem")
                            }

                            Spacer(Modifier.weight(1f))

                            IconButton(onClick = { editing = r }) {
                                Icon(Icons.Filled.Edit, contentDescription = "Edit")
                            }

                            IconButton(onClick = { vm.deleteReward(r) { msg -> showMsg(msg) } }) {
                                Icon(Icons.Filled.Delete, contentDescription = "Delete")
                            }
                        }
                    }
                }
            }

            item {
                Spacer(Modifier.height(6.dp))
                Text("History", style = MaterialTheme.typography.titleMedium)
            }

            if (history.isEmpty()) {
                item {
                    Text(
                        "No redemptions yet.",
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else {
                items(history, key = { it.id }) { h ->
                    ElevatedCard {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(h.rewardName, style = MaterialTheme.typography.titleSmall)
                                Text(
                                    "-${h.cost} pts",
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    formatDateTime(h.createdAtMillis),
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }

                            IconButton(
                                onClick = { vm.undo(h.id) { msg -> showMsg(msg) } }
                            ) {
                                Icon(Icons.AutoMirrored.Filled.Undo, contentDescription = "Undo")
                            }
                        }
                    }
                }
            }
        }
    }

    // Confirm redeem
    if (confirmRedeem != null) {
        val r = confirmRedeem!!
        AlertDialog(
            onDismissRequest = { confirmRedeem = null },
            title = { Text("Redeem reward") },
            text = { Text("Spend ${r.cost} points on “${r.name}”?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        vm.redeem(
                            reward = r,
                            onError = { msg -> showMsg(msg) },
                            onSuccess = { showMsg("Redeemed: ${r.name}") }
                        )
                        confirmRedeem = null
                    }
                ) { Text("Redeem") }
            },
            dismissButton = {
                TextButton(onClick = { confirmRedeem = null }) { Text("Cancel") }
            }
        )
    }

    if (showAdd) {
        RewardEditorDialog(
            title = "Add reward",
            initial = null,
            onDismiss = { showAdd = false },
            onSave = { name, cost, desc, active, recurring ->
                vm.saveReward(
                    name = name,
                    cost = cost,
                    description = desc,
                    isActive = active,
                    isRecurring = recurring
                )
                showAdd = false
            }
        )
    }

    if (editing != null) {
        RewardEditorDialog(
            title = "Edit reward",
            initial = editing,
            onDismiss = { editing = null },
            onSave = { name, cost, desc, active, recurring ->
                val r = editing!!
                vm.saveReward(
                    id = r.id,
                    name = name,
                    cost = cost,
                    description = desc,
                    isActive = active,
                    isRecurring = recurring
                )
                editing = null
            }
        )
    }
}

@Composable
private fun RewardEditorDialog(
    title: String,
    initial: Reward?,
    onDismiss: () -> Unit,
    onSave: (name: String, cost: Int, description: String?, active: Boolean, recurring: Boolean) -> Unit
) {
    var name by remember { mutableStateOf(initial?.name ?: "") }
    var costText by remember { mutableStateOf((initial?.cost ?: 10).toString()) }
    var desc by remember { mutableStateOf(initial?.description ?: "") }
    var active by remember { mutableStateOf(initial?.isActive ?: true) }
    var recurring by remember { mutableStateOf(initial?.isRecurring ?: true) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title) },
        confirmButton = {
            val cost = costText.toIntOrNull() ?: 1
            TextButton(
                onClick = {
                    onSave(
                        name.trim(),
                        cost.coerceAtLeast(1),
                        desc.trim().ifBlank { null },
                        active,
                        recurring
                    )
                },
                enabled = name.trim().isNotEmpty()
            ) { Text("Save") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Reward name") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = costText,
                    onValueChange = { costText = it.filter { c -> c.isDigit() } },
                    label = { Text("Cost (points)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = desc,
                    onValueChange = { desc = it },
                    label = { Text("Description (optional)") },
                    modifier = Modifier.fillMaxWidth()
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Active")
                    Switch(checked = active, onCheckedChange = { active = it })
                }

                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text("Redeem type", color = MaterialTheme.colorScheme.onSurfaceVariant)

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        RadioButton(
                            selected = recurring,
                            onClick = { recurring = true },
                            modifier = Modifier.semantics { contentDescription = "Recurring" }
                        )
                        Spacer(Modifier.width(8.dp))
                        Text("Recurring")
                    }

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        RadioButton(
                            selected = !recurring,
                            onClick = { recurring = false },
                            modifier = Modifier.semantics { contentDescription = "Single redeem" }
                        )
                        Spacer(Modifier.width(8.dp))
                        Text("Single redeem")
                    }
                }
            }
        }
    )
}

private fun formatDateTime(ms: Long): String {
    val fmt = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")
    return Instant.ofEpochMilli(ms).atZone(ZoneId.systemDefault()).format(fmt)
}