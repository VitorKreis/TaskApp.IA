package com.example.myapplication.ui.theme

import android.app.Activity
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

// ── Gradients ──────────────────────────────────────────────────────────────

data class TaskAppGradients(
    val greenPurple: Brush = Brush.horizontalGradient(listOf(DarkGreen, Purple)),
    val greenPurpleVertical: Brush = Brush.verticalGradient(listOf(DarkGreen, Purple)),
    val greenPurpleDiagonal: Brush = Brush.linearGradient(listOf(DarkGreen, PurpleBright)),
    val subtleSurface: Brush = Brush.verticalGradient(
        listOf(SurfaceDark, Color(0xFF161625))
    )
)

val LocalGradients = staticCompositionLocalOf { TaskAppGradients() }

// ── Color Scheme ───────────────────────────────────────────────────────────

private val DarkColorScheme = darkColorScheme(
    primary = DarkGreen,
    onPrimary = TextPrimary,
    primaryContainer = DarkGreenDeep,
    onPrimaryContainer = TextPrimary,
    secondary = Purple,
    onSecondary = TextPrimary,
    secondaryContainer = PurpleBright,
    onSecondaryContainer = TextPrimary,
    background = DarkBg,
    onBackground = TextPrimary,
    surface = SurfaceDark,
    onSurface = TextPrimary,
    surfaceVariant = SurfaceVariantDark,
    onSurfaceVariant = TextSecondary,
    error = OverdueRed,
    onError = TextPrimary,
    outline = GlassBorder,
)

// ── Theme ──────────────────────────────────────────────────────────────────

@Composable
fun TaskAppTheme(content: @Composable () -> Unit) {
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            WindowCompat.getInsetsController(window, view).apply {
                isAppearanceLightStatusBars = false
                isAppearanceLightNavigationBars = false
            }
        }
    }

    MaterialTheme(
        colorScheme = DarkColorScheme,
        typography = TaskAppTypography,
        content = content
    )
}
