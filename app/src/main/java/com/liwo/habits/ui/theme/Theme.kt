package com.liwo.habits.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

// Extra app-specific colors not covered well by Material3
data class AppColors(
    val navSurface: Color,
    val border: Color,
    val accentBright: Color
)

val LocalAppColors = staticCompositionLocalOf {
    AppColors(
        navSurface = NavSurface_Dark,
        border = DividerBorder_Dark,
        accentBright = AccentBright_Dark
    )
}

private val DarkScheme = darkColorScheme(
    primary = Primary_Dark,
    onPrimary = Color(0xFF06130B),

    secondary = Secondary_Dark,
    onSecondary = Color(0xFF06130B),

    tertiary = Purple_Dark,
    onTertiary = BgPrimary_Dark,

    background = BgPrimary_Dark,
    onBackground = TextPrimary_Dark,

    surface = Surface_Dark,
    onSurface = TextPrimary_Dark,

    surfaceVariant = SurfaceVariant_Dark,
    onSurfaceVariant = TextSecondary_Dark,

    error = Error_Dark,
    onError = BgPrimary_Dark,

    outline = DividerBorder_Dark,
    outlineVariant = DividerBorder_Dark.copy(alpha = 0.7f)
)

private val LightScheme = lightColorScheme(
    primary = Primary_Light,
    onPrimary = Color.White,

    secondary = Secondary_Light,
    onSecondary = Color.White,

    tertiary = Purple_Light,
    onTertiary = Color.White,

    background = BgPrimary_Light,
    onBackground = TextPrimary_Light,

    surface = Surface_Light,
    onSurface = TextPrimary_Light,

    surfaceVariant = SurfaceVariant_Light,
    onSurfaceVariant = TextSecondary_Light,

    error = Error_Light,
    onError = Color.White,

    outline = DividerBorder_Light,
    outlineVariant = DividerBorder_Light.copy(alpha = 0.75f)
)

@Composable
fun HabitsTheme(
    mode: AppThemeMode,
    content: @Composable () -> Unit
) {
    val (scheme, appColors) = when (mode) {
        AppThemeMode.DarkGreen -> DarkScheme to AppColors(
            navSurface = NavSurface_Dark,
            border = DividerBorder_Dark,
            accentBright = AccentBright_Dark
        )

        AppThemeMode.LightBlue -> LightScheme to AppColors(
            navSurface = NavSurface_Light,
            border = DividerBorder_Light,
            accentBright = AccentBright_Light
        )
    }

    androidx.compose.runtime.CompositionLocalProvider(
        LocalAppColors provides appColors
    ) {
        MaterialTheme(
            colorScheme = scheme,
            typography = Typography,
            content = content
        )
    }
}