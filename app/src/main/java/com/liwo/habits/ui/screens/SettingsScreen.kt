package com.liwo.habits.ui.screens

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Backup
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.DeleteForever
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.Restore
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.*
import androidx.compose.material3.HorizontalDivider
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.liwo.habits.R
import com.liwo.habits.ui.LocalAppSettings
import com.liwo.habits.ui.theme.AppThemeMode
import com.liwo.habits.vm.ExportState
import com.liwo.habits.vm.SettingsViewModel

@Composable
fun SettingsScreen(vm: SettingsViewModel = hiltViewModel()) {
    val settings = LocalAppSettings.current
    val context = LocalContext.current
    val exportState by vm.exportState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    var clearStep by remember { mutableStateOf(0) }
    var pendingImportUri by remember { mutableStateOf<Uri?>(null) }

    if (clearStep == 1) {
        AlertDialog(
            onDismissRequest = { clearStep = 0 },
            title = { Text(stringResource(R.string.dialog_clear_title_1)) },
            text = { Text(stringResource(R.string.dialog_clear_text_1)) },
            confirmButton = {
                TextButton(onClick = { clearStep = 2 }) {
                    Text(stringResource(R.string.btn_continue))
                }
            },
            dismissButton = {
                TextButton(onClick = { clearStep = 0 }) {
                    Text(stringResource(R.string.btn_cancel))
                }
            }
        )
    }

    if (clearStep == 2) {
        AlertDialog(
            onDismissRequest = { clearStep = 0 },
            title = { Text(stringResource(R.string.dialog_clear_title_2)) },
            text = { Text(stringResource(R.string.dialog_clear_text_2)) },
            confirmButton = {
                TextButton(onClick = {
                    vm.clearDatabase()
                    clearStep = 0
                }) { Text(stringResource(R.string.btn_clear_all)) }
            },
            dismissButton = {
                TextButton(onClick = { clearStep = 0 }) {
                    Text(stringResource(R.string.btn_cancel))
                }
            }
        )
    }

    val importLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri -> if (uri != null) pendingImportUri = uri }

    if (pendingImportUri != null) {
        AlertDialog(
            onDismissRequest = { pendingImportUri = null },
            title = { Text(stringResource(R.string.dialog_import_title)) },
            text = { Text(stringResource(R.string.dialog_import_text)) },
            confirmButton = {
                TextButton(onClick = {
                    vm.importBackup(pendingImportUri!!)
                    pendingImportUri = null
                }) { Text(stringResource(R.string.btn_restore)) }
            },
            dismissButton = {
                TextButton(onClick = { pendingImportUri = null }) {
                    Text(stringResource(R.string.btn_cancel))
                }
            }
        )
    }

    LaunchedEffect(exportState) {
        val state = exportState ?: return@LaunchedEffect
        val msg = when (state) {
            is ExportState.Success -> state.message
            is ExportState.Error -> state.message
        }
        snackbarHostState.showSnackbar(msg)
        vm.clearExportState()
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {

            Text(
                text = stringResource(R.string.section_appearance),
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
            )

            ThemeRow(
                title = stringResource(R.string.theme_dark_green),
                icon = {
                    Icon(
                        imageVector = Icons.Filled.DarkMode,
                        contentDescription = null,
                        tint = if (settings.themeMode == AppThemeMode.DarkGreen)
                            MaterialTheme.colorScheme.primary
                        else
                            MaterialTheme.colorScheme.onSurfaceVariant
                    )
                },
                selected = settings.themeMode == AppThemeMode.DarkGreen,
                onClick = { settings.setThemeMode(AppThemeMode.DarkGreen) }
            )

            HorizontalDivider(Modifier, DividerDefaults.Thickness, DividerDefaults.color)

            ThemeRow(
                title = stringResource(R.string.theme_light_blue),
                icon = {
                    Icon(
                        imageVector = Icons.Filled.LightMode,
                        contentDescription = null,
                        tint = if (settings.themeMode == AppThemeMode.LightBlue)
                            MaterialTheme.colorScheme.primary
                        else
                            MaterialTheme.colorScheme.onSurfaceVariant
                    )
                },
                selected = settings.themeMode == AppThemeMode.LightBlue,
                onClick = { settings.setThemeMode(AppThemeMode.LightBlue) }
            )

            HorizontalDivider(Modifier, DividerDefaults.Thickness, DividerDefaults.color)

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = stringResource(R.string.section_data),
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
            )

            ActionRow(
                title = stringResource(R.string.action_export_backup),
                icon = {
                    Icon(
                        imageVector = Icons.Filled.Backup,
                        contentDescription = stringResource(R.string.action_export_backup),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                },
                onClick = { vm.exportBackup() }
            )

            HorizontalDivider(Modifier, DividerDefaults.Thickness, DividerDefaults.color)

            ActionRow(
                title = stringResource(R.string.action_import_backup),
                icon = {
                    Icon(
                        imageVector = Icons.Filled.Restore,
                        contentDescription = stringResource(R.string.action_import_backup),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                },
                onClick = { importLauncher.launch(arrayOf("application/json")) }
            )

            HorizontalDivider(Modifier, DividerDefaults.Thickness, DividerDefaults.color)

            ActionRow(
                title = stringResource(R.string.action_clear_data),
                icon = {
                    Icon(
                        imageVector = Icons.Filled.DeleteForever,
                        contentDescription = stringResource(R.string.action_clear_data),
                        tint = MaterialTheme.colorScheme.error
                    )
                },
                onClick = { clearStep = 1 }
            )

            HorizontalDivider(Modifier, DividerDefaults.Thickness, DividerDefaults.color)

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = stringResource(R.string.section_diagnostics),
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
            )

            ActionRow(
                title = stringResource(R.string.action_save_log),
                icon = {
                    Icon(
                        imageVector = Icons.Filled.Download,
                        contentDescription = stringResource(R.string.action_save_log),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                },
                onClick = { vm.exportToDownloads() }
            )

            HorizontalDivider(Modifier, DividerDefaults.Thickness, DividerDefaults.color)

            ActionRow(
                title = stringResource(R.string.action_send_log),
                icon = {
                    Icon(
                        imageVector = Icons.Filled.Share,
                        contentDescription = stringResource(R.string.action_send_log),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                },
                onClick = {
                    val intent = vm.buildShareIntent()
                    if (intent != null) {
                        context.startActivity(intent)
                    }
                }
            )

            HorizontalDivider(Modifier, DividerDefaults.Thickness, DividerDefaults.color)
        }

        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier.align(Alignment.BottomCenter)
        )
    }
}

@Composable
private fun ThemeRow(
    title: String,
    icon: @Composable () -> Unit,
    selected: Boolean,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        icon()

        Spacer(Modifier.width(16.dp))

        Text(
            text = title,
            style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier.weight(1f)
        )

        RadioButton(
            selected = selected,
            onClick = onClick
        )
    }
}

@Composable
private fun ActionRow(
    title: String,
    icon: @Composable () -> Unit,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        icon()

        Spacer(Modifier.width(16.dp))

        Text(
            text = title,
            style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier.weight(1f)
        )
    }
}
