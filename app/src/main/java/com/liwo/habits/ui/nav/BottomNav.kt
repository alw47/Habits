package com.liwo.habits.ui.nav

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.CardGiftcard
import androidx.compose.material.icons.filled.Checklist
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Settings
import androidx.compose.ui.graphics.vector.ImageVector

sealed class BottomDest(
    val route: String,
    val label: String,
    val icon: ImageVector
) {
    data object Dashboard : BottomDest("dashboard", "Dashboard", Icons.Filled.Home)
    data object Habits : BottomDest("habits", "Habits", Icons.Filled.Checklist)
    data object Rewards : BottomDest("rewards", "Rewards", Icons.Filled.CardGiftcard)
    data object Calendar : BottomDest("calendar", "Calendar", Icons.Filled.CalendarMonth)
    data object Settings : BottomDest("settings", "Settings", Icons.Filled.Settings)
}

val bottomTabs = listOf(
    BottomDest.Dashboard,
    BottomDest.Habits,
    BottomDest.Rewards,
    BottomDest.Calendar,
    BottomDest.Settings
)