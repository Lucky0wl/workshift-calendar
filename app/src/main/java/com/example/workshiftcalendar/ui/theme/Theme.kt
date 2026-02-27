package com.example.workshiftcalendar.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

enum class AppStyle {
    MODERN_BLUE,
    DARK_AMOLED,
    WARM_PASTEL
}

private val ModernBlueLight = lightColorScheme(
    primary = Color(0xFF1565C0),
    secondary = Color(0xFF26A69A),
    background = Color(0xFFF1F5FB),
    surface = Color(0xFFFFFFFF),
    onPrimary = Color.White,
    onSecondary = Color.White,
    onBackground = Color(0xFF111827),
    onSurface = Color(0xFF111827)
)

private val ModernBlueDark = darkColorScheme(
    primary = Color(0xFF90CAF9),
    secondary = Color(0xFF80CBC4),
    background = Color(0xFF0B1020),
    surface = Color(0xFF111827),
    onPrimary = Color(0xFF02101F),
    onSecondary = Color(0xFF02110E),
    onBackground = Color(0xFFE5E7EB),
    onSurface = Color(0xFFE5E7EB)
)

private val AmoledDark = darkColorScheme(
    primary = Color(0xFFBB86FC),
    secondary = Color(0xFF03DAC5),
    background = Color.Black,
    surface = Color(0xFF050509),
    onPrimary = Color.Black,
    onSecondary = Color.Black,
    onBackground = Color.White,
    onSurface = Color.White
)

private val WarmPastelLight = lightColorScheme(
    primary = Color(0xFFF97316),
    secondary = Color(0xFFEC4899),
    background = Color(0xFFFFFBF5),
    surface = Color(0xFFFFFBF5),
    onPrimary = Color.White,
    onSecondary = Color.White,
    onBackground = Color(0xFF1F2933),
    onSurface = Color(0xFF1F2933)
)

@Composable
fun WorkshiftTheme(
    style: AppStyle,
    useDarkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colors = when (style) {
        AppStyle.MODERN_BLUE ->
            if (useDarkTheme) ModernBlueDark else ModernBlueLight
        AppStyle.DARK_AMOLED ->
            AmoledDark
        AppStyle.WARM_PASTEL ->
            WarmPastelLight
    }

    MaterialTheme(
        colorScheme = colors,
        typography = Typography,
        shapes = Shapes,
        content = content
    )
}

