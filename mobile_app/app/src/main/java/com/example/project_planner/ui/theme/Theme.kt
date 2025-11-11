package com.example.project_planner.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val LightColorPalette = lightColorScheme(
    primary = Color(0xFFFFFFFF),
    onPrimary = Color(0xff7194B0),
    secondary = Color(0xFFD7E8F6),
    onSecondary = Color(0xff1B4065),
    background = Color(0xFFA2C6D9),
    onBackground = Color(0xff1E1E1E),
    surface = Color.White,
    onSurface = Color.Black
)

private val DarkColorPalette = darkColorScheme(
    primary = Color(0Xff0B0F14),
    onPrimary = Color(0xff7499C5),
    secondary = Color(0xff192233) ,
    onSecondary = Color(0xFFC1DCFD),
    background = Color(0xFF344B77),
    onBackground = Color(0xFFE0E1DD),
    surface = Color(0xFF23233A),
    onSurface = Color.White,
//    // Дополнительные цвета
//    tertiary = Color(0xFF2D3C8F),  // Дополнительный цвет для выделения
//    onTertiary = Color.White,      // Цвет для текста или элементов, использующих tertiary
//    error = Color(0xFFB00020),     // Цвет для ошибки
//    onError = Color.White,         // Цвет для текста или иконок на фоне ошибки
//    surfaceVariant = Color(0xFF6775C2), // Цвет для альтернативных поверхностей
//    onSurfaceVariant = Color.Gray // Цвет для текста или элементо

)

@Composable
fun Project_PlannerTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = when {
      darkTheme -> DarkColorPalette
        else -> LightColorPalette
    }
    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}