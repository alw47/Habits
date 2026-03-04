package com.liwo.habits.ui

import com.liwo.habits.ui.theme.AppThemeMode

data class AppSettings(
    val themeMode: AppThemeMode,
    val setThemeMode: (AppThemeMode) -> Unit
)