package com.liwo.habits.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.*
import androidx.compose.material3.HorizontalDivider
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.liwo.habits.ui.LocalAppSettings
import com.liwo.habits.ui.theme.AppThemeMode
import com.liwo.habits.vm.ExportState
import com.liwo.habits.vm.SettingsViewModel

@Composable
fun SettingsScreen(vm: SettingsViewModel = viewModel()) {
    val settings = LocalAppSettings.current
    val context = LocalContext.current
    val exportState by vm.exportState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

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
                text = "Appearance",
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
            )

            ThemeRow(
                title = "Dark (Green)",
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
                title = "Light (Blue)",
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
                text = "Diagnostics",
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
            )

            ActionRow(
                title = "Save log to Downloads",
                icon = {
                    Icon(
                        imageVector = Icons.Filled.Download,
                        contentDescription = "Save log to Downloads",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                },
                onClick = { vm.exportToDownloads() }
            )

            HorizontalDivider(Modifier, DividerDefaults.Thickness, DividerDefaults.color)

            ActionRow(
                title = "Send log by email",
                icon = {
                    Icon(
                        imageVector = Icons.Filled.Share,
                        contentDescription = "Send log by email",
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
