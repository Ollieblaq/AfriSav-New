package com.example.ui.theme

import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.text.DecimalFormat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * AfriSav Design Tokens — Brand Bible (Edition One)
 * Clean, high-fidelity fintech design system.
 */

// ==============================================================================
// 1. SPACING TOKENS (8pt scale: 4, 8, 12, 16, 24, 32, 48, 64)
// ==============================================================================
object AppSpacing {
    val xxs: Dp = 2.dp
    val xs: Dp = 4.dp
    val sm: Dp = 8.dp
    val md: Dp = 12.dp
    val lg: Dp = 16.dp
    val xl: Dp = 24.dp
    val xxl: Dp = 32.dp
    val xxxl: Dp = 48.dp
    val huge: Dp = 64.dp

    // Brand Bible layout metrics
    val screenGutter: Dp = 20.dp
    val cardPadding: Dp = 20.dp
}

// ==============================================================================
// 2. CORNER RADIUS TOKENS
// ==============================================================================
object AppRadii {
    val none: Dp = 0.dp
    val xs: Dp = 4.dp
    val sm: Dp = 8.dp      // chips & small controls
    val md: Dp = 12.dp     // buttons & inputs (Brand Bible specification)
    val lg: Dp = 16.dp
    val xl: Dp = 20.dp     // cards & sheets (Brand Bible specification: 20px)
    val pill: Dp = 99.dp   // pills & progress bars (Brand Bible specification: 99px)
    val full: Dp = 999.dp
}

// Corner Shapes
object AppShapes {
    val none = RoundedCornerShape(AppRadii.none)
    val chip = RoundedCornerShape(AppRadii.sm)        // 8px
    val button = RoundedCornerShape(AppRadii.md)      // 12px
    val input = RoundedCornerShape(AppRadii.md)       // 12px
    val card = RoundedCornerShape(AppRadii.xl)        // 20px
    val sheet = RoundedCornerShape(topStart = AppRadii.xl, topEnd = AppRadii.xl) // 20px
    val pill = RoundedCornerShape(AppRadii.pill)      // 99px
    val full = CircleShape

    // Backward-compatible semantic shapes
    val xs = RoundedCornerShape(AppRadii.xs)
    val sm = chip
    val md = button
    val lg = card
    val xl = card
    val badge = pill
    val tag = chip
    val tab = chip
}

// ==============================================================================
// 3. ELEVATION & SHADOW TOKENS (Brand Bible: 0 8px 24px -16px rgba(50,16,95,.28))
// ==============================================================================
object AppElevation {
    val none: Dp = 0.dp
    val soft: Dp = 8.dp
    val subtle: Dp = 2.dp
    val low: Dp = 4.dp
    val medium: Dp = 8.dp
    val high: Dp = 16.dp

    // Soft Plum shadow color: rgba(50, 16, 95, 0.28)
    val shadowColor = Color(0x4732105F)
    val shadowAmbient = Color(0x1F32105F)
}

/**
 * Soft shadow matching AfriSav Brand Bible: 0 8px 24px -16px rgba(50,16,95,.28).
 * Note: Use either a border OR a shadow on an element, never both.
 */
fun Modifier.afriSavSoftShadow(
    shape: Shape = AppShapes.card,
    clip: Boolean = false
): Modifier = this.shadow(
    elevation = AppElevation.soft,
    shape = shape,
    clip = clip,
    ambientColor = AppElevation.shadowAmbient,
    spotColor = AppElevation.shadowColor
)

// ==============================================================================
// 4. ICON SIZE & TOUCH TARGET TOKENS
// ==============================================================================
object AppIconSize {
    val sm: Dp = 16.dp   // Brand Bible: 16px
    val md: Dp = 20.dp   // Brand Bible: 20px
    val lg: Dp = 24.dp   // Brand Bible: 24px (1.75px stroke standard)
    val xl: Dp = 32.dp   // Brand Bible: 32px
    val xs: Dp = 16.dp
    val xxl: Dp = 48.dp

    // Minimum touch target size (Never below 44x44px)
    val minTouchTarget: Dp = 48.dp
}

// ==============================================================================
// 5. TYPOGRAPHY SCALE (AfriSav Brand Bible Edition One)
// Sora (700/800): Display, headlines, screen titles, section headings, figures
// Plus Jakarta Sans (400/500/600/700): Body, UI, forms, captions, navigation, buttons
// ==============================================================================
object AppTypography {
    // Display (Sora 800, 40px)
    val display = TextStyle(
        fontFamily = SoraFontFamily,
        fontWeight = FontWeight.ExtraBold,
        fontSize = 40.sp,
        lineHeight = 48.sp,
        letterSpacing = (-0.5).sp
    )

    // H1 (Sora 800, 32px)
    val h1 = TextStyle(
        fontFamily = SoraFontFamily,
        fontWeight = FontWeight.ExtraBold,
        fontSize = 32.sp,
        lineHeight = 40.sp,
        letterSpacing = (-0.25).sp
    )

    // H2 (Sora 700, 24px)
    val h2 = TextStyle(
        fontFamily = SoraFontFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 24.sp,
        lineHeight = 32.sp,
        letterSpacing = (-0.2).sp
    )

    // H3 (Sora 700, 20px)
    val h3 = TextStyle(
        fontFamily = SoraFontFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 20.sp,
        lineHeight = 26.sp,
        letterSpacing = (-0.15).sp
    )

    // Figure (Sora 800 tabular numerals, 32–48px)
    val figure = TextStyle(
        fontFamily = SoraFontFamily,
        fontWeight = FontWeight.ExtraBold,
        fontSize = 40.sp,
        lineHeight = 48.sp,
        letterSpacing = (-0.5).sp,
        fontFeatureSettings = "tnum"
    )

    val figureLarge = TextStyle(
        fontFamily = SoraFontFamily,
        fontWeight = FontWeight.ExtraBold,
        fontSize = 48.sp,
        lineHeight = 56.sp,
        letterSpacing = (-0.5).sp,
        fontFeatureSettings = "tnum"
    )

    val figureMedium = TextStyle(
        fontFamily = SoraFontFamily,
        fontWeight = FontWeight.ExtraBold,
        fontSize = 36.sp,
        lineHeight = 44.sp,
        letterSpacing = (-0.3).sp,
        fontFeatureSettings = "tnum"
    )

    val figureSmall = TextStyle(
        fontFamily = SoraFontFamily,
        fontWeight = FontWeight.ExtraBold,
        fontSize = 32.sp,
        lineHeight = 40.sp,
        letterSpacing = (-0.25).sp,
        fontFeatureSettings = "tnum"
    )

    // Body L (Jakarta 400, 17px)
    val bodyL = TextStyle(
        fontFamily = PlusJakartaSansFontFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 17.sp,
        lineHeight = 24.sp,
        letterSpacing = 0.15.sp
    )

    // Body (Jakarta 400, 15px)
    val body = TextStyle(
        fontFamily = PlusJakartaSansFontFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 15.sp,
        lineHeight = 22.sp,
        letterSpacing = 0.1.sp
    )

    val bodyMedium = TextStyle(
        fontFamily = PlusJakartaSansFontFamily,
        fontWeight = FontWeight.Medium,
        fontSize = 15.sp,
        lineHeight = 22.sp,
        letterSpacing = 0.1.sp
    )

    // Small (Jakarta 500, 13px)
    val small = TextStyle(
        fontFamily = PlusJakartaSansFontFamily,
        fontWeight = FontWeight.Medium,
        fontSize = 13.sp,
        lineHeight = 18.sp
    )

    // Label (Jakarta 700 caps, 11px) — All-caps is reserved for this level only
    val label = TextStyle(
        fontFamily = PlusJakartaSansFontFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 11.sp,
        lineHeight = 16.sp,
        letterSpacing = 0.5.sp
    )

    // Button (Jakarta 700, 15px, sentence case)
    val button = TextStyle(
        fontFamily = PlusJakartaSansFontFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 15.sp,
        lineHeight = 20.sp
    )

    // Backward-compatible aliases
    val displayLarge = display
    val pageTitle = h2
    val screenTitle = h2
    val dialogTitle = h3
    val sectionHeader = h3
    val cardTitle = h3
    val bodyLarge = bodyL
    val bodySmall = small
    val labelLarge = button
    val labelSmall = label
    val caption = small
    val balanceLarge = figureMedium
    val balanceMedium = figureSmall
}

// ==============================================================================
// 6. COLOR TOKENS CONTAINER
// ==============================================================================
object AppColors {
    // Brand Anchors
    val savPurple = SavPurple
    val deepPlum = DeepPlum
    val harvestLime = HarvestLime
    val warmCream = WarmCream
    val white = White
    val charcoal = Charcoal

    // Purple Ramp
    val purple50 = Purple50
    val purple100 = Purple100
    val purple200 = Purple200
    val purple300 = Purple300
    val purple400 = Purple400
    val purple500 = Purple500
    val purple600 = Purple600
    val purple700 = Purple700
    val purple800 = Purple800
    val purple900 = Purple900
    val purple950 = Purple950

    // Lime Ramp
    val lime100 = Lime100
    val lime400 = Lime400
    val lime700 = Lime700

    // Semantic Colours
    val successLight = SemanticSuccessLight
    val successPlum = SemanticSuccessPlum
    val warningLight = SemanticWarningLight
    val warningPlum = SemanticWarningPlum
    val errorLight = SemanticErrorLight
    val errorPlum = SemanticErrorPlum
    val infoLight = SemanticInfoLight
    val infoPlum = SemanticInfoPlum

    // Card Borders
    val cardBorderLight = CardBorderLight
    val cardBorderDark = CardBorderDark

    // Legacy compatibility aliases
    val success = SemanticSuccessLight
    val successBgLight = Purple100
    val successBgDark = DeepPlum
    val warning = SemanticWarningLight
    val warningBgLight = Purple100
    val warningBgDark = DeepPlum
    val error = SemanticErrorLight
    val errorBgLight = Purple100
    val errorBgDark = DeepPlum
    val info = SemanticInfoLight
    val infoBgLight = Purple100
    val infoBgDark = DeepPlum
    val primaryGreen = SavPurple
    val primaryGreenDark = DeepPlum
    val secondaryOrange = HarvestLime
    val accentGold = Lime400
    val brandGreenBgLight = Purple100
    val brandGreenBgDark = DeepPlum
    val brandOrangeBgLight = Purple50
}

// ==============================================================================
// 7. BRAND BIBLE NUMBER, CURRENCY & DATE FORMATTERS
// ==============================================================================
object AppFormatters {
    /**
     * Naira sign, no space, thousands separated: ₦25,000.
     * Show kobo only when the amount isn't whole: ₦1,250.50 — never ₦25,000.00.
     * Abbreviate only in tight space above six figures: ₦1.2m, ₦250k (lowercase, no space).
     * Never round a figure in the user's favour.
     */
    fun formatNaira(amount: Double, abbreviate: Boolean = false): String {
        if (abbreviate) {
            if (amount >= 1_000_000.0) {
                val millions = kotlin.math.floor(amount / 100_000.0) / 10.0
                return if (millions % 1.0 == 0.0) "₦${millions.toInt()}m" else "₦${millions}m"
            } else if (amount >= 100_000.0) {
                val thousands = kotlin.math.floor(amount / 1_000.0).toInt()
                return "₦${thousands}k"
            }
        }

        val isWhole = (amount % 1.0 == 0.0)
        val formatter = if (isWhole) {
            DecimalFormat("#,##0")
        } else {
            DecimalFormat("#,##0.00")
        }
        return "₦${formatter.format(amount)}"
    }

    /**
     * Percentages are whole numbers unless precision matters: 75%, not 75.0%.
     */
    fun formatPercentage(percentage: Double, precision: Boolean = false): String {
        return if (!precision && percentage % 1.0 == 0.0) {
            "${percentage.toInt()}%"
        } else {
            String.format(Locale.US, "%.1f%%", percentage)
        }
    }

    /**
     * Dates as "12 Mar 2026"; relative time up to 7 days ("2 days ago"), then the full date.
     */
    fun formatDate(timestampMs: Long): String {
        val now = System.currentTimeMillis()
        val diffMs = now - timestampMs
        val diffDays = diffMs / (1000 * 60 * 60 * 24)

        return when {
            diffDays < 1 -> "Today"
            diffDays == 1L -> "1 day ago"
            diffDays in 2..7 -> "$diffDays days ago"
            else -> {
                val sdf = SimpleDateFormat("d MMM yyyy", Locale.US)
                sdf.format(Date(timestampMs))
            }
        }
    }

    /**
     * Helper to enforce sentence case on button text ("Start saving", not "START SAVING").
     */
    fun toSentenceCase(text: String): String {
        if (text.isBlank()) return text
        val trimmed = text.trim()
        return trimmed.substring(0, 1).uppercase(Locale.US) + trimmed.substring(1).lowercase(Locale.US)
    }
}
