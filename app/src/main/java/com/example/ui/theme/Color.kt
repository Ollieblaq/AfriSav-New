package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance

// ==============================================================================
// AfriSav Brand Bible (Edition One) Core Palette
// ==============================================================================

val SavPurple = Color(0xFF5B21B6)     // Primary
val DeepPlum = Color(0xFF32105F)      // Secondary / Dark ground
val HarvestLime = Color(0xFFA3E635)   // Accent (roughly 5% of surface, never large field/background)
val WarmCream = Color(0xFFFFF8EA)     // Background (Light)
val White = Color(0xFFFFFFFF)         // Surface
val Charcoal = Color(0xFF171717)      // Ink (Primary text on light)

// ==============================================================================
// Purple Tint Ramp (50–950)
// ==============================================================================

val Purple50 = Color(0xFFF5F3FF)
val Purple100 = Color(0xFFEDE9FE)
val Purple200 = Color(0xFFDDD6FE)
val Purple300 = Color(0xFFC4B5FD)     // Safe text/accent on Deep Plum
val Purple400 = Color(0xFFA78BFA)
val Purple500 = Color(0xFF8B5CF6)
val Purple600 = Color(0xFF7C3AED)     // Primary button Hover state
val Purple700 = Color(0xFF6D28D9)     // Progress track on dark
val Purple800 = Color(0xFF5B21B6)     // Sav Purple
val Purple900 = Color(0xFF4C1D95)
val Purple950 = Color(0xFF32105F)     // Deep Plum

// ==============================================================================
// Lime Ramp (100 / 400 / 700)
// ==============================================================================

val Lime100 = Color(0xFFECFCCB)
val Lime400 = Color(0xFFA3E635)       // Harvest Lime
val Lime700 = Color(0xFF4D7C0F)       // Only legal Lime for text on light grounds

// ==============================================================================
// Semantic Colours (Separate token set, never reused as brand colours)
// ==============================================================================

val SemanticSuccessLight = Color(0xFF15803D)
val SemanticSuccessPlum = Color(0xFF4ADE80)

val SemanticWarningLight = Color(0xFFB45309)
val SemanticWarningPlum = Color(0xFFFBBF24)

val SemanticErrorLight = Color(0xFFB91C1C)
val SemanticErrorPlum = Color(0xFFFCA5A5)

val SemanticInfoLight = Color(0xFF7C3AED)
val SemanticInfoPlum = Color(0xFFC4B5FD)

// Card & border tokens
val CardBorderLight = Color(0xFFE8DCC6)
val CardBorderDark = Color(0xFF4C1D95)
val DeepPlumCard = Color(0xFF260D4A)
val DeepPlumBorder = CardBorderDark

// Neutral & Text tokens
val TextMuted = Color(0xFF737373)
val CharcoalSecondary = Color(0xFF525252)
val CharcoalMuted = Color(0xFF737373)
val NeutralLight = WarmCream
val NeutralLightBorder = CardBorderLight

// Semantic aliases
val StatusSuccessLight = SemanticSuccessLight
val StatusSuccessDark = SemanticSuccessPlum
val StatusWarningLight = SemanticWarningLight
val StatusWarningDark = SemanticWarningPlum
val StatusErrorLight = SemanticErrorLight
val StatusErrorDark = SemanticErrorPlum
val StatusInfoLight = SemanticInfoLight
val StatusInfoDark = SemanticInfoPlum

val HarvestLime400 = Lime400
val SuccessGreenLight = SemanticSuccessLight
val SuccessGreen = SemanticSuccessLight
val ErrorRedLight = SemanticErrorLight
val ErrorRed = SemanticErrorLight
val WarningAmber = SemanticWarningLight
val WarningAmberLight = SemanticWarningPlum
val InfoLilac = SemanticInfoPlum
val InfoBlue = SemanticInfoLight

// ==============================================================================
// Dynamic Theme Tokens & Backward-Compatible Aliases
// ==============================================================================

val isDark: Boolean
    @Composable
    get() {
        val surface = MaterialTheme.colorScheme.surface
        return surface == DeepPlum || surface == Color(0xFF1E1E1E) || surface == Color(0xFF1E293B) || surface.luminance() < 0.5f
    }

val SoftBackground: Color
    @Composable
    get() = if (isDark) DeepPlum else WarmCream

val SoftGray: Color
    @Composable
    get() = if (isDark) Purple900 else Purple50

val TextDark: Color
    @Composable
    get() = if (isDark) White else Charcoal

val TextLight: Color
    @Composable
    get() = if (isDark) Purple300 else Color(0xFF525252)

val BorderSlate100: Color
    @Composable
    get() = if (isDark) Purple900 else CardBorderLight

val BgSlate50: Color
    @Composable
    get() = if (isDark) DeepPlum else WarmCream

val TextSlate400: Color
    @Composable
    get() = if (isDark) Purple300 else Color(0xFF737373)

val TextSlate500: Color
    @Composable
    get() = if (isDark) Purple200 else Color(0xFF525252)

val TextSlate800: Color
    @Composable
    get() = if (isDark) White else Charcoal

val TipOrangeBg: Color
    @Composable
    get() = if (isDark) Color(0x2B32105F) else Purple50

val TipOrangeBorder: Color
    @Composable
    get() = if (isDark) Purple700 else Purple200

val SurfaceBg: Color
    @Composable
    get() = if (isDark) Color(0xFF260D4A) else White

val SurfaceWhite: Color
    @Composable
    get() = if (isDark) Color(0xFF260D4A) else White

val SurfaceSubtle: Color
    @Composable
    get() = if (isDark) DeepPlum else Purple50

val BorderSubtle: Color
    @Composable
    get() = if (isDark) Purple900 else CardBorderLight

// Legacy compatibility aliases for unmodified screens
val PrimaryGreen = Color(0xFF0A8F3D)
val SecondaryOrange = Color(0xFFFF8C00)
val AccentGold = Color(0xFFF4C430)
val DarkGreen = Color(0xFF06632B)

val Purple80 = Color(0xFFD0BCFF)
val PurpleGrey80 = Color(0xFFCCC2DC)
val Pink80 = Color(0xFFEFB8C8)
val Purple40 = Color(0xFF6650a4)
val PurpleGrey40 = Color(0xFF625b71)
val Pink40 = Color(0xFF7D5260)
