package com.example.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val DarkColorScheme = darkColorScheme(
    primary = CirqubitDarkPrimary,
    onPrimary = CirqubitDarkOnPrimary,
    primaryContainer = CirqubitDarkSurfaceVariant,
    onPrimaryContainer = CirqubitDarkPrimary,
    secondary = CirqubitDarkSecondary,
    onSecondary = CirqubitDarkOnSecondary,
    secondaryContainer = CirqubitDarkSurfaceVariant,
    onSecondaryContainer = CirqubitDarkSecondary,
    tertiary = CirqubitDarkTertiary,
    background = CirqubitDarkBackground,
    surface = CirqubitDarkSurface,
    surfaceVariant = CirqubitDarkSurfaceVariant,
    onBackground = CirqubitDarkTextPrimary,
    onSurface = CirqubitDarkTextPrimary,
    onSurfaceVariant = CirqubitDarkTextSecondary,
    outline = CirqubitDarkOutline
)

private val LightColorScheme = lightColorScheme(
    primary = CirqubitLightPrimary,
    onPrimary = CirqubitLightOnPrimary,
    primaryContainer = CirqubitLightSurfaceVariant,
    onPrimaryContainer = CirqubitLightPrimary,
    secondary = CirqubitLightSecondary,
    onSecondary = CirqubitLightOnSecondary,
    secondaryContainer = CirqubitLightSurfaceVariant,
    onSecondaryContainer = CirqubitLightSecondary,
    tertiary = CirqubitLightTertiary,
    background = CirqubitLightBackground,
    surface = CirqubitLightSurface,
    surfaceVariant = CirqubitLightSurfaceVariant,
    onBackground = CirqubitLightTextPrimary,
    onSurface = CirqubitLightTextPrimary,
    onSurfaceVariant = CirqubitLightTextSecondary,
    outline = CirqubitLightOutline
)

@Composable
fun CirqubitTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as? Activity)?.window
            if (window != null) {
                window.statusBarColor = colorScheme.background.toArgb()
                window.navigationBarColor = colorScheme.surface.toArgb()
                WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme
                WindowCompat.getInsetsController(window, view).isAppearanceLightNavigationBars = !darkTheme
            }
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
