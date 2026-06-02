package com.rodrigo.androidapp.futtrack.ui.theme

import android.app.Activity
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val BalColorScheme = darkColorScheme(
    primary = BalRed,
    secondary = BalYellow,
    background = PitchGreen,
    surface = SurfaceGreen,
    onPrimary = TextWhite,
    onSecondary = PitchGreen,
    onBackground = TextWhite,
    onSurface = TextWhite,
    surfaceVariant = PitchGreen,
    onSurfaceVariant = BalYellow,
    error = BalRed
)

@Composable
fun FutTrackTheme(
    content: @Composable () -> Unit
) {
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = BalColorScheme.background.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = false
        }
    }

    MaterialTheme(
        colorScheme = BalColorScheme,
        typography = Typography, // Mantém a tipografia que já existir no seu projeto
        content = content
    )
}