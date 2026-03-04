package com.liwo.habits.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material3.*
import androidx.compose.material3.HorizontalDivider
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.liwo.habits.ui.LocalAppSettings
import com.liwo.habits.ui.theme.AppThemeMode

@Composable
fun SettingsScreen() {
    val settings = LocalAppSettings.current

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

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = "More settings coming…",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(horizontal = 16.dp)
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