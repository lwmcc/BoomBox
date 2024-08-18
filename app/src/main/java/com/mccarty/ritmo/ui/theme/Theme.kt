package com.mccarty.ritmo.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.ui.material.MaterialTheme.colors

private val LightColorPalette = lightColorScheme(
    primary = Blue700,
    secondary = Blue100,
    background = Color.White,
    surface = Color.White,
    onPrimary = Color.White,
    onSecondary = Color.Black,
    onBackground = Color.Black,
    onSurface = Color.Black,
)

@Composable
fun BoomBoxTheme(darkTheme: Boolean = isSystemInDarkTheme(), content: @Composable () -> Unit) {
    //val colors = if (darkTheme) {
        // DarkColorPalette to implement
    //} else {
        LightColorPalette
    //}

    MaterialTheme(
        colorScheme = LightColorPalette,
        content = content
    )
}