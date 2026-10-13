package com.cyingshi.app.ui

import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val Bg = Color(0xFF0A0A0F)
private val Surface = Color(0xFF141419)
private val Accent = Color(0xFF00C8FF)

private val DarkScheme = darkColorScheme(
    primary = Accent,
    secondary = Accent,
    background = Bg,
    surface = Surface,
    surfaceVariant = Color(0xFF1E1E26),
    onBackground = Color.White,
    onSurface = Color.White
)

@Composable
fun CYingShiTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = DarkScheme,
        content = content
    )
}
