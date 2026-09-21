package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance

val Purple80 = Color(0xFFD0BCFF)
val PurpleGrey80 = Color(0xFFCCC2DC)
val Pink80 = Color(0xFFEFB8C8)

val Purple40 = Color(0xFF6650a4)
val PurpleGrey40 = Color(0xFF625b71)
val Pink40 = Color(0xFF7D5260)

// AfriSav Nigerian Food Savings Brand Colors
val PrimaryGreen = Color(0xFF0A8F3D)
val SecondaryOrange = Color(0xFFFF8C00)
val AccentGold = Color(0xFFF4C430)
val DarkGreen = Color(0xFF06632B)

val isDark: Boolean
    @Composable
    get() {
        val surface = MaterialTheme.colorScheme.surface
        return surface == Color(0xFF1E1E1E) || surface == Color(0xFF1E293B) || surface.luminance() < 0.5f
    }

// Neutral, soft fintech background base
val SoftBackground: Color
    @Composable
    get() = if (isDark) Color(0xFF0F172A) else Color(0xFFF8FAFC)

val SoftGray: Color
    @Composable
    get() = if (isDark) Color(0xFF1E293B) else Color(0xFFF1F5F9)

val TextDark: Color
    @Composable
    get() = if (isDark) Color(0xFFF8FAFC) else Color(0xFF0F172A)

val TextLight: Color
    @Composable
    get() = if (isDark) Color(0xFF94A3B8) else Color(0xFF64748B)

// Clean Minimalism Palette Extensions
val BorderSlate100: Color
    @Composable
    get() = if (isDark) Color(0xFF334155) else Color(0xFFE2E8F0)

val BgSlate50: Color
    @Composable
    get() = if (isDark) Color(0xFF1E293B) else Color(0xFFF8FAFC)

val TextSlate400: Color
    @Composable
    get() = if (isDark) Color(0xFF64748B) else Color(0xFF94A3B8)

val TextSlate500: Color
    @Composable
    get() = if (isDark) Color(0xFF94A3B8) else Color(0xFF64748B)

val TextSlate800: Color
    @Composable
    get() = if (isDark) Color(0xFFF1F5F9) else Color(0xFF1E293B)

val TipOrangeBg: Color
    @Composable
    get() = if (isDark) Color(0x2BFF8C00) else Color(0x0FFF8C00)

val TipOrangeBorder: Color
    @Composable
    get() = if (isDark) Color(0x40FF8C00) else Color(0x1AFF8C00)

val SurfaceBg: Color
    @Composable
    get() = if (isDark) Color(0xFF1E293B) else Color.White

val SurfaceWhite: Color
    @Composable
    get() = if (isDark) Color(0xFF1E293B) else Color.White

val SurfaceSubtle: Color
    @Composable
    get() = if (isDark) Color(0xFF273549) else Color(0xFFF1F5F9)

val BorderSubtle: Color
    @Composable
    get() = if (isDark) Color(0xFF1E293B) else Color(0xFFF1F5F9)



