package com.liwo.habits.ui

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.liwo.habits.ui.nav.BottomDest
import com.liwo.habits.ui.nav.bottomTabs
import com.liwo.habits.ui.screens.*

@Composable
fun AppScaffold() {
    val navController = rememberNavController()
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = backStackEntry?.destination?.route

    Scaffold(
        bottomBar = {
            NavigationBar {
                bottomTabs.forEach { dest ->
                    NavigationBarItem(
                        selected = currentRoute == dest.route,
                        onClick = {
                            navController.navigate(dest.route) {
                                launchSingleTop = true
                                restoreState = true
                                popUpTo(navController.graph.startDestinationId) { saveState = true }
                            }
                        },
                        icon = { Icon(dest.icon, contentDescription = dest.label) },
                        label = { Text(dest.label, maxLines = 1, overflow = TextOverflow.Ellipsis) },
                        alwaysShowLabel = false
                    )
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = BottomDest.Dashboard.route,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(BottomDest.Dashboard.route) { DashboardScreen() }
            composable(BottomDest.Habits.route)    { HabitsScreen() }
            composable(BottomDest.Rewards.route)   { RewardsScreen() }
            composable(BottomDest.Calendar.route)  { CalendarScreen() }
            composable(BottomDest.Settings.route)  { SettingsScreen() }
        }
    }
}
