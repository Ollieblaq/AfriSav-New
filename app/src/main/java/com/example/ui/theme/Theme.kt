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

private val DarkColorScheme =
    darkColorScheme(
        primary = HarvestLime,         // Lime action colour on plum/purple ground
        secondary = Purple300,        // Safe secondary on plum
        tertiary = Purple200,
        background = DeepPlum,        // Dark ground (Deep Plum #32105F)
        surface = Color(0xFF260D4A),  // Deep tinted plum surface
        surfaceVariant = Purple900,
        outline = Purple700,
        outlineVariant = Purple800,
        onPrimary = Charcoal,         // Charcoal on Lime (11.89:1)
        onSecondary = DeepPlum,
        onTertiary = DeepPlum,
        onBackground = White,
        onSurface = White,
        onSurfaceVariant = Purple300
    )

private val LightColorScheme =
    lightColorScheme(
        primary = SavPurple,          // Sav Purple (#5B21B6) action colour on light ground
        secondary = DeepPlum,
        tertiary = HarvestLime,
        background = WarmCream,       // Warm Cream (#FFF8EA)
        surface = White,              // White surface (#FFFFFF)
        surfaceVariant = Purple50,
        outline = CardBorderLight,    // 1px #E8DCC6 border
        outlineVariant = Purple200,
        onPrimary = White,            // White on Sav Purple (8.98:1)
        onSecondary = White,
        onTertiary = Charcoal,
        onBackground = Charcoal,      // Charcoal ink (#171717)
        onSurface = Charcoal,
        onSurfaceVariant = Color(0xFF525252)
    )

@Composable
fun AfriSavTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false, // Set to false to strictly preserve AfriSav Brand Bible colors
    content: @Composable () -> Unit,
) {
    val colorScheme =
        when {
            dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
                val context = LocalContext.current
                if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
            }
            darkTheme -> DarkColorScheme
            else -> LightColorScheme
        }

    MaterialTheme(colorScheme = colorScheme, typography = Typography, content = content)
}
