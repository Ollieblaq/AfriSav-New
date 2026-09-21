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
    primary = PrimaryGreen,
    secondary = SecondaryOrange,
    tertiary = AccentGold,
    background = Color(0xFF0F172A),
    surface = Color(0xFF1E293B),
    surfaceVariant = Color(0xFF334155),
    outline = Color(0xFF334155),
    outlineVariant = Color(0xFF1E293B),
    onPrimary = Color.White,
    onSecondary = Color.White,
    onTertiary = Color.DarkGray,
    onBackground = Color(0xFFF8FAFC),
    onSurface = Color(0xFFF1F5F9),
    onSurfaceVariant = Color(0xFF94A3B8)
  )

private val LightColorScheme =
  lightColorScheme(
    primary = PrimaryGreen,
    secondary = SecondaryOrange,
    tertiary = AccentGold,
    background = Color(0xFFF8FAFC), // Soft slate-50 neutral base
    surface = Color.White,
    surfaceVariant = Color(0xFFF1F5F9),
    outline = Color(0xFFE2E8F0),
    outlineVariant = Color(0xFFCBD5E1),
    onPrimary = Color.White,
    onSecondary = Color.White,
    onTertiary = Color.DarkGray,
    onBackground = Color(0xFF0F172A), // TextDark
    onSurface = Color(0xFF0F172A),
    onSurfaceVariant = Color(0xFF64748B)
  )

@Composable
fun AfriSavTheme(
  darkTheme: Boolean = isSystemInDarkTheme(),
  dynamicColor: Boolean = false, // Set to false to preserve AfriSav premium branding
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

