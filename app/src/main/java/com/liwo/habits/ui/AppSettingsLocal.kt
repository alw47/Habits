package com.liwo.habits.ui

import androidx.compose.runtime.staticCompositionLocalOf

val LocalAppSettings = staticCompositionLocalOf<AppSettings> {
    error("LocalAppSettings not provided")
}