package com.example.veil.ui.theme

import android.app.Activity
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val DarkColorScheme = darkColorScheme(
    primary = VeilRedBright,
    onPrimary = VeilBackground,
    primaryContainer = VeilRedDark,
    onPrimaryContainer = VeilTextPrimary,
    secondary = VeilRedSecondary,
    onSecondary = VeilTextPrimary,
    secondaryContainer = VeilSurfaceElevated,
    onSecondaryContainer = VeilTextSecondary,
    background = VeilBackground,
    onBackground = VeilTextPrimary,
    surface = VeilSurface,
    onSurface = VeilTextPrimary,
    surfaceVariant = VeilSurfaceElevated,
    onSurfaceVariant = VeilTextSecondary,
    outline = VeilBorder,
    outlineVariant = VeilBorderBright
)

@Composable
fun VeilTheme(
    content: @Composable () -> Unit
) {
    val colorScheme = DarkColorScheme
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as? Activity)?.window
            if (window != null) {
                window.statusBarColor = VeilBackground.toArgb()
                window.navigationBarColor = VeilBackground.toArgb()
                val controller = WindowCompat.getInsetsController(window, view)
                controller.isAppearanceLightStatusBars = false
                controller.isAppearanceLightNavigationBars = false
            }
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = VeilTypography,
        content = content
    )
}
