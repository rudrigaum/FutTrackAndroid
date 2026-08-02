package com.rodrigo.androidapp.futtrack.ui.theme

import android.app.Activity
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val BalColorScheme = lightColorScheme(
    primary = BalRed,
    onPrimary = TextWhite,
    primaryContainer = Color(0xFFF2F2F2),
    onPrimaryContainer = Color(0xFF3F3F3F),

    secondary = BalYellow,
    onSecondary = PitchGreen,
    secondaryContainer = BalYellow.copy(alpha = 0.24f),
    onSecondaryContainer = PitchGreen,

    background = Color.White,
    onBackground = Color(0xFF1A1A1A),

    surface = Color.White,
    onSurface = Color(0xFF1A1A1A),

    surfaceVariant = Color(0xFFF3F3F3),
    onSurfaceVariant = Color(0xFF4A4A4A),

    error = BalRed,
    onError = TextWhite
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

            WindowCompat
                .getInsetsController(window, view)
                .isAppearanceLightStatusBars = true
        }
    }

    MaterialTheme(
        colorScheme = BalColorScheme,
        typography = Typography,
        content = content
    )
}