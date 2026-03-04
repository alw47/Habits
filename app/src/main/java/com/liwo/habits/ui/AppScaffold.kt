package com.liwo.habits.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.TaskAlt
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import com.liwo.habits.ui.screens.*

enum class BottomTab {
    Dashboard, Habits, Rewards, Calendar, Settings
}

@Composable
fun AppScaffold() {
    var tab by remember { mutableStateOf(BottomTab.Dashboard) }

    Scaffold(
        bottomBar = {
            NavigationBar {
                fun tabLabel(text: String) = @Composable {
                    Text(
                        text = text,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                NavigationBarItem(
                    selected = tab == BottomTab.Dashboard,
                    onClick = { tab = BottomTab.Dashboard },
                    icon = { Icon(Icons.Filled.Home, contentDescription = "Dashboard") },
                    label = tabLabel("Dashboard"),
                    alwaysShowLabel = false
                )
                NavigationBarItem(
                    selected = tab == BottomTab.Habits,
                    onClick = { tab = BottomTab.Habits },
                    icon = { Icon(Icons.Filled.TaskAlt, contentDescription = "Habits") },
                    label = tabLabel("Habits"),
                    alwaysShowLabel = false
                )
                NavigationBarItem(
                    selected = tab == BottomTab.Rewards,
                    onClick = { tab = BottomTab.Rewards },
                    icon = { Icon(Icons.Filled.Star, contentDescription = "Rewards") },
                    label = tabLabel("Rewards"),
                    alwaysShowLabel = false
                )
                NavigationBarItem(
                    selected = tab == BottomTab.Calendar,
                    onClick = { tab = BottomTab.Calendar },
                    icon = { Icon(Icons.Filled.CalendarMonth, contentDescription = "Calendar") },
                    label = tabLabel("Calendar"),
                    alwaysShowLabel = false
                )
                NavigationBarItem(
                    selected = tab == BottomTab.Settings,
                    onClick = { tab = BottomTab.Settings },
                    icon = { Icon(Icons.Filled.Settings, contentDescription = "Settings") },
                    label = tabLabel("Settings"),
                    alwaysShowLabel = false
                )
            }
        }
    ) { innerPadding ->
        Box(Modifier.padding(innerPadding)) {
            when (tab) {
                BottomTab.Dashboard -> DashboardScreen()
                BottomTab.Habits -> HabitsScreen()
                BottomTab.Rewards -> RewardsScreen()
                BottomTab.Calendar -> CalendarScreen()
                BottomTab.Settings -> SettingsScreen()
            }
        }
    }
}