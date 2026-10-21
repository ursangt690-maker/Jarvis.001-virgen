package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val JarvisColorScheme = darkColorScheme(
    primary = JarvisCyan,
    onPrimary = Color(0xFF001A2C),
    primaryContainer = Color(0xFF003855),
    onPrimaryContainer = Color(0xFFBAE6FD),
    secondary = JarvisNeonMint,
    onSecondary = Color(0xFF002920),
    secondaryContainer = Color(0xFF004D40),
    onSecondaryContainer = Color(0xFFA7F3D0),
    tertiary = JarvisHoloViolet,
    onTertiary = Color.White,
    tertiaryContainer = Color(0xFF3B1D7A),
    onTertiaryContainer = Color(0xFFDDD6FE),
    background = JarvisBackground,
    onBackground = JarvisTextPrimary,
    surface = JarvisSurface,
    onSurface = JarvisTextPrimary,
    surfaceVariant = JarvisSurfaceElevated,
    onSurfaceVariant = JarvisTextSecondary,
    outline = JarvisCardBorder,
    outlineVariant = Color(0xFF112236),
    error = JarvisLaserRed,
    onError = Color.White
)

@Composable
fun JarvisAITheme(
    content: @Composable () -> Unit
) {
    // Cinematic sci-fi dark theme is consistent across all versions
    MaterialTheme(
        colorScheme = JarvisColorScheme,
        typography = Typography,
        content = content
    )
}
