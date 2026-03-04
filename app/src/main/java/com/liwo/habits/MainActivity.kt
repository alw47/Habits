package com.liwo.habits

import android.content.Context
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalContext
import com.liwo.habits.ui.AppScaffold
import com.liwo.habits.ui.AppSettings
import com.liwo.habits.ui.LocalAppSettings
import com.liwo.habits.ui.theme.AppThemeMode
import com.liwo.habits.ui.theme.HabitsTheme
import com.liwo.habits.util.AppLogger

private const val PREFS_FILE = "habits_prefs"
private const val KEY_THEME_MODE = "theme_mode" // "DarkGreen" or "LightBlue"

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        AppLogger.init(this)
        AppLogger.i("App", "App started")
        enableEdgeToEdge()

        setContent {
            val ctx = LocalContext.current

            var mode by remember { mutableStateOf(loadThemeMode(ctx)) }

            val setMode: (AppThemeMode) -> Unit = { newMode ->
                mode = newMode
                saveThemeMode(ctx, newMode)
            }

            CompositionLocalProvider(
                LocalAppSettings provides AppSettings(
                    themeMode = mode,
                    setThemeMode = setMode
                )
            ) {
                HabitsTheme(mode = mode) {
                    AppScaffold()
                }
            }
        }
    }
}

private fun loadThemeMode(ctx: Context): AppThemeMode {
    val prefs = ctx.getSharedPreferences(PREFS_FILE, Context.MODE_PRIVATE)
    val raw = prefs.getString(KEY_THEME_MODE, AppThemeMode.DarkGreen.name) ?: AppThemeMode.DarkGreen.name
    return runCatching { AppThemeMode.valueOf(raw) }.getOrElse { AppThemeMode.DarkGreen }
}

private fun saveThemeMode(ctx: Context, mode: AppThemeMode) {
    val prefs = ctx.getSharedPreferences(PREFS_FILE, Context.MODE_PRIVATE)
    prefs.edit().putString(KEY_THEME_MODE, mode.name).apply()
}