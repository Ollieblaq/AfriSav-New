package com.example.ui.theme

import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * AfriSav Design Tokens
 * Clean, minimal, fintech-inspired design token system.
 */

// 1. SPACING SCALE (4 / 8 / 12 / 16 / 24 / 32 / 48 px system)
object AppSpacing {
    val xxs: Dp = 2.dp
    val xs: Dp = 4.dp
    val sm: Dp = 8.dp
    val md: Dp = 12.dp
    val lg: Dp = 16.dp
    val xl: Dp = 24.dp
    val xxl: Dp = 32.dp
    val xxxl: Dp = 48.dp
}

// 2. CORNER RADIUS SCALE
object AppRadii {
    val none: Dp = 0.dp
    val xs: Dp = 4.dp      // micro tags, progress bars
    val sm: Dp = 8.dp      // compact badges, chips, mini buttons
    val md: Dp = 12.dp     // text fields, buttons, segmented tabs
    val lg: Dp = 16.dp     // cards, dialogs, containers
    val xl: Dp = 24.dp     // balance hero cards, sheets
    val full: Dp = 999.dp  // pill badges, avatars, floating pills
}

// Corner Shapes
object AppShapes {
    val none = RoundedCornerShape(AppRadii.none)
    val xs = RoundedCornerShape(AppRadii.xs)
    val sm = RoundedCornerShape(AppRadii.sm)
    val md = RoundedCornerShape(AppRadii.md)
    val lg = RoundedCornerShape(AppRadii.lg)
    val xl = RoundedCornerShape(AppRadii.xl)
    val full = CircleShape

    // Semantic mappings
    val button = md
    val card = lg
    val input = md
    val badge = full
    val pill = CircleShape
    val tag = sm
    val tab = md
    val sheet = RoundedCornerShape(topStart = AppRadii.xl, topEnd = AppRadii.xl)
}

// 3. ELEVATION & SHADOW TOKENS
object AppElevation {
    val none: Dp = 0.dp
    val subtle: Dp = 1.dp
    val low: Dp = 2.dp
    val medium: Dp = 4.dp
    val high: Dp = 8.dp
}

// 4. ICON SIZE SCALE
object AppIconSize {
    val xs: Dp = 14.dp
    val sm: Dp = 16.dp
    val md: Dp = 20.dp
    val lg: Dp = 24.dp
    val xl: Dp = 32.dp
    val xxl: Dp = 48.dp
}

// 5. TYPOGRAPHY SCALE
object AppTypography {
    val pageTitle = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Bold,
        fontSize = 24.sp,
        lineHeight = 30.sp,
        letterSpacing = (-0.3).sp
    )

    val sectionHeader = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.SemiBold,
        fontSize = 18.sp,
        lineHeight = 24.sp,
        letterSpacing = (-0.2).sp
    )

    val cardTitle = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.SemiBold,
        fontSize = 15.sp,
        lineHeight = 20.sp
    )

    val body = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Normal,
        fontSize = 14.sp,
        lineHeight = 20.sp,
        letterSpacing = 0.1.sp
    )

    val bodyMedium = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Medium,
        fontSize = 14.sp,
        lineHeight = 20.sp,
        letterSpacing = 0.1.sp
    )

    val bodySmall = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Normal,
        fontSize = 12.sp,
        lineHeight = 16.sp
    )

    val label = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Medium,
        fontSize = 13.sp,
        lineHeight = 18.sp
    )

    val labelSmall = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.SemiBold,
        fontSize = 11.sp,
        lineHeight = 14.sp,
        letterSpacing = 0.4.sp
    )

    val caption = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Normal,
        fontSize = 11.sp,
        lineHeight = 14.sp
    )

    // Fintech Balance Display Numbers
    val balanceLarge = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Bold,
        fontSize = 30.sp,
        lineHeight = 36.sp,
        letterSpacing = (-0.5).sp
    )

    val balanceMedium = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Bold,
        fontSize = 22.sp,
        lineHeight = 28.sp,
        letterSpacing = (-0.3).sp
    )
}

// 6. SEMANTIC COLOR TOKENS
object AppColors {
    // Brand Anchors
    val primaryGreen = PrimaryGreen
    val primaryGreenDark = DarkGreen
    val secondaryOrange = SecondaryOrange
    val accentGold = AccentGold

    // Status Colors
    val success = Color(0xFF16A34A)
    val successBgLight = Color(0xFFDCFCE7)
    val successBgDark = Color(0xFF14532D)

    val warning = Color(0xFFD97706)
    val warningBgLight = Color(0xFFFEF3C7)
    val warningBgDark = Color(0xFF78350F)

    val error = Color(0xFFDC2626)
    val errorBgLight = Color(0xFFFEE2E2)
    val errorBgDark = Color(0xFF7F1D1D)

    val info = Color(0xFF2563EB)
    val infoBgLight = Color(0xFFDBEAFE)
    val infoBgDark = Color(0xFF1E3A8A)

    val brandGreenBgLight = Color(0xFFE8F5E9)
    val brandGreenBgDark = Color(0xFF132E1B)

    val brandOrangeBgLight = Color(0xFFFFF3E0)
    val brandOrangeBgDark = Color(0xFF3E2706)
}
