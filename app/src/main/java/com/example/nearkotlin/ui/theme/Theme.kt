package com.example.nearkotlin.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColorScheme = darkColorScheme(
    primary = Color(0xFF00C08B),
    secondary = Color(0xFF0072CE),
    tertiary = Color(0xFFFFB800),
    background = Color(0xFF111618),
    surface = Color(0xFF1C2124),
    onPrimary = Color.White,
    onSecondary = Color.White,
    onTertiary = Color.Black,
    onBackground = Color(0xFFE6E6E6),
    onSurface = Color(0xFFE6E6E6)
)

private val LightColorScheme = lightColorScheme(
    primary = Color(0xFF00C08B),
    secondary = Color(0xFF0072CE),
    tertiary = Color(0xFFFFB800),
    background = Color(0xFFFAFAFA),
    surface = Color.White,
    onPrimary = Color.White,
    onSecondary = Color.White,
    onTertiary = Color.Black,
    onBackground = Color(0xFF1C1B1F),
    onSurface = Color(0xFF1C1B1F)
)

@Composable
fun NearKotlinTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        content = content
    )
}