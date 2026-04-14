package com.smarttasks.official.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

private val LightColors = lightColorScheme(
    primary          = OrangePrimary,
    onPrimary        = Color.White,
    secondary        = OrangeLight,
    onSecondary      = Color.White,
    background       = GrayBackground,
    surface          = GraySurface,
    onSurface        = GrayText,
    onBackground     = GrayText,
    surfaceVariant   = GrayDivider,
    onSurfaceVariant = GrayTextLight
)

private val DarkColors = darkColorScheme(
    primary          = OrangePrimary,
    onPrimary        = Color.Black,
    secondary        = OrangeLight,
    onSecondary      = Color.Black,
    background       = DarkBackground,
    surface          = DarkSurface,
    onSurface        = DarkOnSurface,
    onBackground     = DarkOnSurface,
    surfaceVariant   = DarkSurfaceVariant,
    onSurfaceVariant = Color(0xFF9090B0)
)

@Composable
fun ToDoListTheme(
    darkTheme: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColors else LightColors

    MaterialTheme(
        colorScheme = colorScheme,
        typography  = Typography,
        shapes = Shapes(
            extraSmall = RoundedCornerShape(8.dp),
            small      = RoundedCornerShape(12.dp),
            medium     = RoundedCornerShape(16.dp),
            large      = RoundedCornerShape(24.dp)
        ),
        content = content
    )
}


