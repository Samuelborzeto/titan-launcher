package com.titanlauncher.ui

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

object TitanColors {
    val Black = Color(0xFF0B0B0F)
    val Graphite = Color(0xFF1C1C24)
    val Purple = Color(0xFF8B5CF6)
    val DeepPurple = Color(0xFF1A1030)
}

private val Scheme = darkColorScheme(
    primary = TitanColors.Purple,
    secondary = TitanColors.Purple,
    background = TitanColors.Black,
    surface = TitanColors.Graphite,
    surfaceVariant = TitanColors.Graphite,
    onPrimary = Color.White,
    onBackground = Color.White,
    onSurface = Color.White,
)

@Composable
fun TitanTheme(content: @Composable () -> Unit) {
    MaterialTheme(colorScheme = Scheme, content = content)
}
