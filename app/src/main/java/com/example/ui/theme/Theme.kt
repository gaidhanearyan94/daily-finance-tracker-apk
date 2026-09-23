package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val LightColorScheme = lightColorScheme(
    primary = FinFlowNeonLime,
    onPrimary = FinFlowTextPrimary,
    primaryContainer = FinFlowNeonLimeLight,
    onPrimaryContainer = FinFlowTextPrimary,
    secondary = FinFlowPillDark,
    onSecondary = Color.White,
    secondaryContainer = FinFlowPillDarkElevated,
    onSecondaryContainer = Color.White,
    background = FinFlowBackground,
    onBackground = FinFlowTextPrimary,
    surface = FinFlowCardSurface,
    onSurface = FinFlowTextPrimary,
    surfaceVariant = FinFlowBackground,
    onSurfaceVariant = FinFlowTextSecondary,
    outline = FinFlowBorder,
    outlineVariant = FinFlowGridLine
)

private val DarkColorScheme = darkColorScheme(
    primary = FinFlowNeonLime,
    onPrimary = FinFlowTextPrimary,
    primaryContainer = FinFlowNeonLimeDark,
    onPrimaryContainer = Color.White,
    secondary = Color.White,
    onSecondary = FinFlowPillDark,
    secondaryContainer = FinFlowPillDarkElevated,
    onSecondaryContainer = Color.White,
    background = Color(0xFF101214),
    onBackground = Color(0xFFEDF2E8),
    surface = Color(0xFF181C20),
    onSurface = Color(0xFFEDF2E8),
    surfaceVariant = Color(0xFF22272E),
    onSurfaceVariant = Color(0xFFA5B0A2),
    outline = Color(0xFF2D343D),
    outlineVariant = Color(0xFF242A32)
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
