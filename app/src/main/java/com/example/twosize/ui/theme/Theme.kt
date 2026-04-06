package com.example.twosize.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable

private val colorScheme = darkColorScheme(
    primary = SoftOrange,
    onPrimary = DarkBackground,
    secondary = WhitePrimary,
    onSecondary = DarkBackground,
    background = DarkBackground,
    onBackground = WhitePrimary,
    surface = DarkSurface,
    onSurface = WhitePrimary
)

@Composable
fun TwinScaleTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = colorScheme,
        content = content
    )
}
