package com.cyingshi.app.ui

import androidx.compose.material3.lightColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

/** OK绿版主题色 */
val GreenTop = Color(0xFF8FD8B0) // 渐变顶部浅绿
val GreenBottom = Color(0xFF1E9E6A) // 渐变底部深绿
val GreenDeep = Color(0xFF146B4A) // 分类选中态深绿
val GreenFab = Color(0xFF2196F3) // 右下角换源 FAB 蓝
val YearBlue = Color(0xFF2E9BE6) // 年份角标蓝
val StatusGreen = Color(0xFF2FA968) // 状态条绿
val FavRed = Color(0xFFFF5C7A) // 收藏红心

/** 整屏绿色垂直渐变（上浅下深） */
val GreenGradient: Brush
    get() = Brush.verticalGradient(listOf(GreenTop, GreenBottom))

private val GreenScheme = lightColorScheme(
    primary = Color(0xFF1E9E6A),
    onPrimary = Color.White,
    primaryContainer = Color(0xFFD9F2E4),
    onPrimaryContainer = Color(0xFF0B3D28),
    secondary = Color(0xFF2E9BE6),
    background = Color(0xFFF4FAF6),
    onBackground = Color(0xFF1A2E24),
    surface = Color.White,
    onSurface = Color(0xFF1A2E24),
    surfaceVariant = Color(0xFFE8F3EC),
    onSurfaceVariant = Color(0xFF3D5A4C),
    error = Color(0xFFB3261E)
)

@Composable
fun CYingShiTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = GreenScheme,
        content = content
    )
}
