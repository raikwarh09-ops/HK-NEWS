package com.example.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

private val NewsDarkColorScheme = darkColorScheme(
    primary = NewsRedPrimary,
    onPrimary = Color.White,
    primaryContainer = NewsRedDark,
    onPrimaryContainer = Color.White,
    secondary = NewsGold,
    onSecondary = Color.Black,
    secondaryContainer = Color(0xFF3E2723),
    onSecondaryContainer = NewsGold,
    tertiary = NewsCyanAccent,
    background = NewsDeepNavy,
    onBackground = TextPrimary,
    surface = NewsCardBg,
    onSurface = TextPrimary,
    surfaceVariant = Color(0xFF26334D),
    onSurfaceVariant = TextSecondary,
    outline = Color(0x44FFFFFF)
)

private val NewsLightColorScheme = lightColorScheme(
    primary = NewsRedPrimary,
    onPrimary = Color.White,
    primaryContainer = Color(0xFFFFEBEE),
    onPrimaryContainer = NewsRedDark,
    secondary = NewsAmber,
    onSecondary = Color.Black,
    tertiary = NewsCyanAccent,
    background = Color(0xFFF8FAFC),
    onBackground = Color(0xFF0F172A),
    surface = Color.White,
    onSurface = Color(0xFF0F172A),
    surfaceVariant = Color(0xFFF1F5F9),
    onSurfaceVariant = Color(0xFF475569)
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = true, // News studios thrive in rich dark mode by default
    dynamicColor: Boolean = false, // Keep signature broadcast styling
    content: @Composable () -> Unit,
) {
    val colorScheme = if (darkTheme) NewsDarkColorScheme else NewsLightColorScheme
    MaterialTheme(colorScheme = colorScheme, typography = Typography, content = content)
}
