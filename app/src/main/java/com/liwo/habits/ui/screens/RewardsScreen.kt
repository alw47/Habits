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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.liwo.habits.R
import com.liwo.habits.data.model.Reward
import com.liwo.habits.vm.RewardsViewModel
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

@Composable
fun RewardsScreen() {
    val vm: RewardsViewModel = hiltViewModel()
    val state by vm.state.collectAsState()
    val history by vm.history.collectAsState()

    var showAdd by remember { mutableStateOf(false) }
    var editing by remember { mutableStateOf<Reward?>(null) }
    var confirmRedeem by remember { mutableStateOf<Reward?>(null) }
    var confirmDeleteReward by remember { mutableStateOf<Reward?>(null) }

    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    fun showMsg(msg: String) {
        scope.launch { snackbarHostState.showSnackbar(msg) }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        floatingActionButton = {
            FloatingActionButton(onClick = { showAdd = true }) {
                Icon(Icons.Filled.Add, contentDescription = stringResource(R.string.cd_add_reward))
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
                                text = stringResource(R.string.label_points_available),
                                style = MaterialTheme.typography.labelLarge,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = "${state.pointsAvailable}",
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
                                text = stringResource(R.string.label_total_earned),
                                style = MaterialTheme.typography.labelLarge,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = "${state.pointsEarned}",
                                style = MaterialTheme.typography.headlineMedium
                            )
                        }
                    }
                }
            }

            item {
                Text(stringResource(R.string.label_rewards), style = MaterialTheme.typography.titleMedium)
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

                                val mode = if (r.isRecurring)
                                    stringResource(R.string.label_recurring)
                                else
                                    stringResource(R.string.label_single_redeem)
                                Text(
                                    text = stringResource(R.string.label_pts_mode, r.cost, mode),
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
                                        text = stringResource(R.string.label_already_redeemed),
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                } else if (!item.canRedeem) {
                                    Text(
                                        text = if (item.needMore > 0)
                                            stringResource(R.string.label_need_more_points, item.needMore)
                                        else
                                            stringResource(R.string.label_unavailable),
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
                                Text(stringResource(R.string.btn_redeem))
                            }

                            Spacer(Modifier.weight(1f))

                            IconButton(onClick = { editing = r }) {
                                Icon(Icons.Filled.Edit, contentDescription = stringResource(R.string.cd_edit))
                            }

                            IconButton(onClick = { confirmDeleteReward = r }) {
                                Icon(Icons.Filled.Delete, contentDescription = stringResource(R.string.cd_delete))
                            }
                        }
                    }
                }
            }

            item {
                Spacer(Modifier.height(6.dp))
                Text(stringResource(R.string.label_history), style = MaterialTheme.typography.titleMedium)
            }

            if (history.isEmpty()) {
                item {
                    Text(
                        stringResource(R.string.label_no_redemptions),
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
                                Icon(Icons.AutoMirrored.Filled.Undo, contentDescription = stringResource(R.string.cd_undo))
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
        val redeemSuccessMsg = stringResource(R.string.msg_redeemed, r.name)
        AlertDialog(
            onDismissRequest = { confirmRedeem = null },
            title = { Text(stringResource(R.string.dialog_redeem_title)) },
            text = { Text(stringResource(R.string.dialog_redeem_text, r.cost, r.name)) },
            confirmButton = {
                TextButton(
                    onClick = {
                        vm.redeem(
                            reward = r,
                            onError = { msg -> showMsg(msg) },
                            onSuccess = { showMsg(redeemSuccessMsg) }
                        )
                        confirmRedeem = null
                    }
                ) { Text(stringResource(R.string.btn_redeem)) }
            },
            dismissButton = {
                TextButton(onClick = { confirmRedeem = null }) { Text(stringResource(R.string.btn_cancel)) }
            }
        )
    }

    if (confirmDeleteReward != null) {
        val r = confirmDeleteReward!!
        AlertDialog(
            onDismissRequest = { confirmDeleteReward = null },
            title = { Text(stringResource(R.string.dialog_delete_reward_title)) },
            text = { Text(stringResource(R.string.dialog_delete_reward_text, r.name)) },
            confirmButton = {
                TextButton(onClick = {
                    vm.deleteReward(r) { msg -> showMsg(msg) }
                    confirmDeleteReward = null
                }) { Text(stringResource(R.string.btn_delete)) }
            },
            dismissButton = {
                TextButton(onClick = { confirmDeleteReward = null }) { Text(stringResource(R.string.btn_cancel)) }
            }
        )
    }

    if (showAdd) {
        RewardEditorDialog(
            title = stringResource(R.string.dialog_add_reward),
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
            title = stringResource(R.string.dialog_edit_reward),
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

    val recurringStr = stringResource(R.string.label_recurring)
    val singleRedeemStr = stringResource(R.string.label_single_redeem)

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
            ) { Text(stringResource(R.string.btn_save)) }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text(stringResource(R.string.btn_cancel)) } },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text(stringResource(R.string.field_reward_name)) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = costText,
                    onValueChange = { costText = it.filter { c -> c.isDigit() } },
                    label = { Text(stringResource(R.string.field_reward_cost)) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = desc,
                    onValueChange = { desc = it },
                    label = { Text(stringResource(R.string.field_reward_description)) },
                    modifier = Modifier.fillMaxWidth()
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(stringResource(R.string.label_active))
                    Switch(checked = active, onCheckedChange = { active = it })
                }

                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(stringResource(R.string.label_redeem_type), color = MaterialTheme.colorScheme.onSurfaceVariant)

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        RadioButton(
                            selected = recurring,
                            onClick = { recurring = true },
                            modifier = Modifier.semantics { contentDescription = recurringStr }
                        )
                        Spacer(Modifier.width(8.dp))
                        Text(recurringStr)
                    }

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        RadioButton(
                            selected = !recurring,
                            onClick = { recurring = false },
                            modifier = Modifier.semantics { contentDescription = singleRedeemStr }
                        )
                        Spacer(Modifier.width(8.dp))
                        Text(singleRedeemStr)
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
