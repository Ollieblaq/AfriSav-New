package com.example.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.defaultTextFieldColors
import com.example.ui.theme.*

// ==============================================================================
// AfriSav Brand Bible (Edition One) Shared Component Library
// ==============================================================================

/**
 * Surface Ground descriptor for the AfriSav Button Rule:
 * - LIGHT: White (#FFFFFF) or Warm Cream (#FFF8EA) ground
 *          -> Sav Purple action button (#5B21B6), White label (#FFFFFF) (8.98:1 contrast)
 * - DARK:  Sav Purple (#5B21B6) or Deep Plum (#32105F) ground
 *          -> Harvest Lime action button (#A3E635), Charcoal label (#171717) (11.89:1 contrast)
 */
enum class SurfaceGround {
    LIGHT,
    DARK
}

// ==============================================================================
// 1. BUTTONS (Brand Bible Specifications)
// Metrics: 52px tall, 12px radius, Jakarta 700 at 15px, sentence case.
// ==============================================================================

/**
 * Primary action button:
 * Follows the AfriSav Button Rule strictly:
 * - White/Warm Cream ground -> Sav Purple container (#5B21B6), White label (#FFFFFF)
 * - Purple/Deep Plum ground -> Harvest Lime container (#A3E635), Charcoal label (#171717)
 * Hover: Purple 600 (#7C3AED). Pressed: Deep Plum (#32105F).
 * Disabled: 38% opacity, no colour change.
 * Buttons are sentence case ("Start saving," not "START SAVING").
 */
@Composable
fun AfriSavPrimaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    ground: SurfaceGround = if (isDark) SurfaceGround.DARK else SurfaceGround.LIGHT,
    containerColor: Color? = null,
    contentColor: Color? = null,
    leadingIcon: ImageVector? = null,
    trailingIcon: ImageVector? = null,
    isLoading: Boolean = false,
    fullWidth: Boolean = true
) {
    val resolvedContainerColor = containerColor ?: when (ground) {
        SurfaceGround.DARK -> HarvestLime
        SurfaceGround.LIGHT -> SavPurple
    }

    val resolvedContentColor = contentColor ?: when (ground) {
        SurfaceGround.DARK -> Charcoal
        SurfaceGround.LIGHT -> White
    }

    val formattedText = AppFormatters.toSentenceCase(text)

    Button(
        onClick = onClick,
        modifier = modifier
            .then(if (fullWidth) Modifier.fillMaxWidth() else Modifier)
            .heightIn(min = 52.dp),
        enabled = enabled && !isLoading,
        shape = AppShapes.button, // 12px radius
        colors = ButtonDefaults.buttonColors(
            containerColor = resolvedContainerColor,
            contentColor = resolvedContentColor,
            disabledContainerColor = resolvedContainerColor.copy(alpha = 0.38f),
            disabledContentColor = resolvedContentColor.copy(alpha = 0.38f)
        ),
        contentPadding = PaddingValues(horizontal = AppSpacing.xl, vertical = AppSpacing.md)
    ) {
        if (isLoading) {
            CircularProgressIndicator(
                color = resolvedContentColor,
                strokeWidth = 2.dp,
                modifier = Modifier.size(20.dp)
            )
        } else {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                if (leadingIcon != null) {
                    Icon(
                        imageVector = leadingIcon,
                        contentDescription = null,
                        modifier = Modifier.size(AppIconSize.lg),
                        tint = resolvedContentColor
                    )
                    Spacer(modifier = Modifier.width(AppSpacing.sm))
                }
                Text(
                    text = formattedText,
                    style = AppTypography.button,
                    color = resolvedContentColor
                )
                if (trailingIcon != null) {
                    Spacer(modifier = Modifier.width(AppSpacing.sm))
                    Icon(
                        imageVector = trailingIcon,
                        contentDescription = null,
                        modifier = Modifier.size(AppIconSize.lg),
                        tint = resolvedContentColor
                    )
                }
            }
        }
    }
}

/**
 * Secondary button:
 * Same metrics (52px tall, 12px radius, Jakarta 700 at 15px, sentence case).
 * Transparent fill, 1.5px border in the ink colour of its surface.
 * - On light ground: Charcoal (#171717) border and label
 * - On dark/plum ground: Purple 300 (#C4B5FD) or White border and label
 */
@Composable
fun AfriSavSecondaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    ground: SurfaceGround = if (isDark) SurfaceGround.DARK else SurfaceGround.LIGHT,
    leadingIcon: ImageVector? = null,
    trailingIcon: ImageVector? = null,
    fullWidth: Boolean = true
) {
    val inkColor = when (ground) {
        SurfaceGround.DARK -> Purple300
        SurfaceGround.LIGHT -> Charcoal
    }

    val formattedText = AppFormatters.toSentenceCase(text)

    OutlinedButton(
        onClick = onClick,
        modifier = modifier
            .then(if (fullWidth) Modifier.fillMaxWidth() else Modifier)
            .heightIn(min = 52.dp),
        enabled = enabled,
        shape = AppShapes.button, // 12px radius
        border = BorderStroke(1.5.dp, if (enabled) inkColor else inkColor.copy(alpha = 0.38f)),
        colors = ButtonDefaults.outlinedButtonColors(
            containerColor = Color.Transparent,
            contentColor = inkColor,
            disabledContainerColor = Color.Transparent,
            disabledContentColor = inkColor.copy(alpha = 0.38f)
        ),
        contentPadding = PaddingValues(horizontal = AppSpacing.xl, vertical = AppSpacing.md)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            if (leadingIcon != null) {
                Icon(
                    imageVector = leadingIcon,
                    contentDescription = null,
                    modifier = Modifier.size(AppIconSize.lg),
                    tint = inkColor
                )
                Spacer(modifier = Modifier.width(AppSpacing.sm))
            }
            Text(
                text = formattedText,
                style = AppTypography.button,
                color = inkColor
            )
            if (trailingIcon != null) {
                Spacer(modifier = Modifier.width(AppSpacing.sm))
                Icon(
                    imageVector = trailingIcon,
                    contentDescription = null,
                    modifier = Modifier.size(AppIconSize.lg),
                    tint = inkColor
                )
            }
        }
    }
}

/**
 * Outlined button: Alias conforming to secondary button specifications.
 */
@Composable
fun AfriSavOutlinedButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    borderColor: Color? = null,
    containerColor: Color = Color.Transparent,
    contentColor: Color? = null,
    leadingIcon: ImageVector? = null,
    trailingIcon: ImageVector? = null,
    fullWidth: Boolean = true
) {
    val defaultInk = if (isDark) Purple300 else Charcoal
    val resolvedBorder = borderColor ?: defaultInk
    val resolvedContent = contentColor ?: defaultInk
    val formattedText = AppFormatters.toSentenceCase(text)

    OutlinedButton(
        onClick = onClick,
        modifier = modifier
            .then(if (fullWidth) Modifier.fillMaxWidth() else Modifier)
            .heightIn(min = 52.dp),
        enabled = enabled,
        shape = AppShapes.button,
        border = BorderStroke(1.5.dp, if (enabled) resolvedBorder else resolvedBorder.copy(alpha = 0.38f)),
        colors = ButtonDefaults.outlinedButtonColors(
            containerColor = containerColor,
            contentColor = resolvedContent,
            disabledContainerColor = containerColor,
            disabledContentColor = resolvedContent.copy(alpha = 0.38f)
        ),
        contentPadding = PaddingValues(horizontal = AppSpacing.xl, vertical = AppSpacing.md)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            if (leadingIcon != null) {
                Icon(
                    imageVector = leadingIcon,
                    contentDescription = null,
                    modifier = Modifier.size(AppIconSize.lg),
                    tint = resolvedContent
                )
                Spacer(modifier = Modifier.width(AppSpacing.sm))
            }
            Text(
                text = formattedText,
                style = AppTypography.button,
                color = resolvedContent
            )
            if (trailingIcon != null) {
                Spacer(modifier = Modifier.width(AppSpacing.sm))
                Icon(
                    imageVector = trailingIcon,
                    contentDescription = null,
                    modifier = Modifier.size(AppIconSize.lg),
                    tint = resolvedContent
                )
            }
        }
    }
}

/**
 * Tertiary ghost button: Clean text button for secondary actions / dismiss.
 */
@Composable
fun AfriSavTertiaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    leadingIcon: ImageVector? = null,
    color: Color = if (isDark) Purple300 else SavPurple
) {
    val formattedText = AppFormatters.toSentenceCase(text)

    TextButton(
        onClick = onClick,
        modifier = modifier.heightIn(min = 44.dp),
        enabled = enabled,
        shape = AppShapes.button,
        colors = ButtonDefaults.textButtonColors(
            contentColor = color,
            disabledContentColor = color.copy(alpha = 0.38f)
        ),
        contentPadding = PaddingValues(horizontal = AppSpacing.md, vertical = AppSpacing.xs)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            if (leadingIcon != null) {
                Icon(
                    imageVector = leadingIcon,
                    contentDescription = null,
                    modifier = Modifier.size(AppIconSize.sm),
                    tint = color
                )
                Spacer(modifier = Modifier.width(AppSpacing.xs))
            }
            Text(
                text = formattedText,
                style = AppTypography.button,
                color = color
            )
        }
    }
}

/**
 * Standardized icon button with 48x48dp minimum interactive touch area.
 */
@Composable
fun AfriSavIconButton(
    icon: ImageVector,
    contentDescription: String?,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    iconSize: Dp = AppIconSize.lg,
    containerColor: Color = Color.Transparent,
    iconTint: Color = TextDark,
    borderColor: Color? = null
) {
    Box(
        modifier = modifier
            .size(AppIconSize.xxl) // 48dp minimum touch target
            .then(
                if (containerColor != Color.Transparent) Modifier.clip(CircleShape).background(containerColor)
                else Modifier
            )
            .then(
                if (borderColor != null) Modifier.border(1.5.dp, borderColor, CircleShape)
                else Modifier
            )
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = contentDescription,
            modifier = Modifier.size(iconSize),
            tint = iconTint
        )
    }
}

// ==============================================================================
// 2. CARDS (Brand Bible Specifications)
// White on Cream, 20px radius, 20px padding, 1px #E8DCC6 border.
// Shadow only when interactive: 0 8px 24px -16px rgba(50,16,95,.28).
// Rule: Use either a border OR a shadow on an element, never both.
// ==============================================================================

/**
 * Standard non-interactive card:
 * White on Cream, 20px radius, 20px padding, 1px #E8DCC6 border, 0 elevation.
 */
@Composable
fun AfriSavCard(
    modifier: Modifier = Modifier,
    containerColor: Color = SurfaceBg,
    borderColor: Color = if (isDark) CardBorderDark else CardBorderLight,
    shape: RoundedCornerShape = AppShapes.card, // 20px radius
    contentPadding: PaddingValues = PaddingValues(AppSpacing.cardPadding), // 20px padding
    ground: SurfaceGround = if (isDark) SurfaceGround.DARK else SurfaceGround.LIGHT,
    content: @Composable ColumnScope.() -> Unit
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = shape,
        colors = CardDefaults.cardColors(containerColor = containerColor),
        border = BorderStroke(1.dp, borderColor),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(contentPadding),
            content = content
        )
    }
}

/**
 * Interactive Clickable card:
 * 20px radius, 20px padding, soft shadow ONLY (no border, per Brand Bible rule).
 */
@Composable
fun AfriSavClickableCard(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    containerColor: Color = SurfaceBg,
    shape: RoundedCornerShape = AppShapes.card,
    contentPadding: PaddingValues = PaddingValues(AppSpacing.cardPadding),
    content: @Composable RowScope.() -> Unit
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .afriSavSoftShadow(shape = shape) // Soft shadow, NO border
            .clickable(onClick = onClick),
        shape = shape,
        colors = CardDefaults.cardColors(containerColor = containerColor),
        border = null,
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(contentPadding),
            verticalAlignment = Alignment.CenterVertically,
            content = content
        )
    }
}

/**
 * Dedicated Fintech Balance Card:
 * Prominently showcases balance using Sora 800 tabular figures with
 * high-contrast styling and the Button Rule.
 */
@Composable
fun AfriSavBalanceCard(
    title: String,
    amount: Double,
    modifier: Modifier = Modifier,
    subtitle: String? = null,
    badgeText: String? = null,
    onPrimaryAction: (() -> Unit)? = null,
    primaryActionText: String = "Add Money",
    onSecondaryAction: (() -> Unit)? = null,
    secondaryActionText: String = "Withdraw",
    ground: SurfaceGround = if (isDark) SurfaceGround.DARK else SurfaceGround.LIGHT
) {
    val cardBg = when (ground) {
        SurfaceGround.DARK -> DeepPlum
        SurfaceGround.LIGHT -> White
    }
    val cardBorder = when (ground) {
        SurfaceGround.DARK -> CardBorderDark
        SurfaceGround.LIGHT -> CardBorderLight
    }

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = AppShapes.card, // 20px radius
        colors = CardDefaults.cardColors(containerColor = cardBg),
        border = BorderStroke(1.dp, cardBorder),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(AppSpacing.cardPadding)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = title,
                    style = AppTypography.bodyMedium,
                    color = if (ground == SurfaceGround.DARK) Purple300 else TextSlate500
                )
                if (badgeText != null) {
                    AfriSavBadge(
                        text = badgeText,
                        type = AfriSavBadgeType.INFO,
                        icon = Icons.Default.Info
                    )
                }
            }

            Spacer(modifier = Modifier.height(AppSpacing.sm))

            AfriSavAmountDisplay(
                amount = amount,
                color = if (ground == SurfaceGround.DARK) White else Charcoal,
                size = AmountDisplaySize.LARGE
            )

            if (subtitle != null) {
                Spacer(modifier = Modifier.height(AppSpacing.xs))
                Text(
                    text = subtitle,
                    style = AppTypography.small,
                    color = if (ground == SurfaceGround.DARK) Purple300 else TextSlate400
                )
            }

            if (onPrimaryAction != null || onSecondaryAction != null) {
                Spacer(modifier = Modifier.height(AppSpacing.xl))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(AppSpacing.md)
                ) {
                    if (onPrimaryAction != null) {
                        Box(modifier = Modifier.weight(1f)) {
                            AfriSavPrimaryButton(
                                text = primaryActionText,
                                onClick = onPrimaryAction,
                                ground = ground,
                                fullWidth = true
                            )
                        }
                    }
                    if (onSecondaryAction != null) {
                        Box(modifier = Modifier.weight(1f)) {
                            AfriSavSecondaryButton(
                                text = secondaryActionText,
                                onClick = onSecondaryAction,
                                ground = ground,
                                fullWidth = true
                            )
                        }
                    }
                }
            }
        }
    }
}

// Backward-compatible overload accepting formatted string amount
@Composable
fun AfriSavBalanceCard(
    title: String,
    amount: String,
    modifier: Modifier = Modifier,
    subtitle: String? = null,
    badgeText: String? = null,
    onPrimaryAction: (() -> Unit)? = null,
    primaryActionText: String = "Add Money",
    onSecondaryAction: (() -> Unit)? = null,
    secondaryActionText: String = "Withdraw"
) {
    val numericAmount = amount.replace("[^0.0-9]".toRegex(), "").toDoubleOrNull() ?: 0.0
    AfriSavBalanceCard(
        title = title,
        amount = numericAmount,
        modifier = modifier,
        subtitle = subtitle,
        badgeText = badgeText,
        onPrimaryAction = onPrimaryAction,
        primaryActionText = primaryActionText,
        onSecondaryAction = onSecondaryAction,
        secondaryActionText = secondaryActionText
    )
}

// ==============================================================================
// 3. PROGRESS BARS (Brand Bible Specifications)
// 10px pill. Track: Purple 100 (light) / Purple 700 (dark).
// Fill: Sav Purple (light) / Harvest Lime (dark).
// Always paired with a figure and a percentage — never shown alone.
// ==============================================================================

@Composable
fun AfriSavProgressBar(
    progress: Float, // 0.0f to 1.0f
    modifier: Modifier = Modifier,
    label: String? = null,
    figure: String? = null,
    percentage: Double? = null,
    ground: SurfaceGround = if (isDark) SurfaceGround.DARK else SurfaceGround.LIGHT
) {
    val clampedProgress = progress.coerceIn(0f, 1f)
    val calculatedPercentage = percentage ?: (clampedProgress * 100.0)

    val trackColor = when (ground) {
        SurfaceGround.DARK -> Purple700
        SurfaceGround.LIGHT -> Purple100
    }
    val fillColor = when (ground) {
        SurfaceGround.DARK -> HarvestLime
        SurfaceGround.LIGHT -> SavPurple
    }
    val textColor = when (ground) {
        SurfaceGround.DARK -> White
        SurfaceGround.LIGHT -> Charcoal
    }
    val subTextColor = when (ground) {
        SurfaceGround.DARK -> Purple300
        SurfaceGround.LIGHT -> TextSlate500
    }

    Column(modifier = modifier.fillMaxWidth()) {
        // Pairing: Always shown with a figure and a percentage
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                if (label != null) {
                    Text(
                        text = label,
                        style = AppTypography.small,
                        color = subTextColor
                    )
                    Spacer(modifier = Modifier.width(AppSpacing.xs))
                }
                if (figure != null) {
                    Text(
                        text = figure,
                        style = AppTypography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                        color = textColor
                    )
                }
            }
            Text(
                text = AppFormatters.formatPercentage(calculatedPercentage),
                style = AppTypography.small.copy(fontWeight = FontWeight.Bold),
                color = if (ground == SurfaceGround.DARK) HarvestLime else SavPurple
            )
        }

        Spacer(modifier = Modifier.height(AppSpacing.xs))

        // 10px pill progress track
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(10.dp)
                .clip(AppShapes.pill)
                .background(trackColor)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .fillMaxWidth(clampedProgress)
                    .clip(AppShapes.pill)
                    .background(fillColor)
            )
        }
    }
}

// ==============================================================================
// 4. AMOUNT DISPLAY (Brand Bible Specifications)
// Sora 800, tabular numerals, ₦ sign at the same size as digits (never superscript).
// ==============================================================================

enum class AmountDisplaySize {
    LARGE,   // 48sp
    MEDIUM,  // 36sp
    SMALL    // 24sp
}

@Composable
fun AfriSavAmountDisplay(
    amount: Double,
    modifier: Modifier = Modifier,
    size: AmountDisplaySize = AmountDisplaySize.LARGE,
    color: Color = TextDark,
    abbreviate: Boolean = false,
    subtitle: String? = null
) {
    val textStyle = when (size) {
        AmountDisplaySize.LARGE -> AppTypography.figureLarge
        AmountDisplaySize.MEDIUM -> AppTypography.figureMedium
        AmountDisplaySize.SMALL -> AppTypography.h2
    }

    val formattedAmount = AppFormatters.formatNaira(amount, abbreviate = abbreviate)

    Column(modifier = modifier) {
        Text(
            text = formattedAmount,
            style = textStyle,
            color = color
        )
        if (subtitle != null) {
            Spacer(modifier = Modifier.height(AppSpacing.xxs))
            Text(
                text = subtitle,
                style = AppTypography.small,
                color = if (isDark) Purple300 else TextSlate400
            )
        }
    }
}

// ==============================================================================
// 5. INPUT FIELDS (Brand Bible Specifications)
// 52px tall, 12px radius, 1.5px border, label always visible above the field.
// ==============================================================================

@Composable
fun AfriSavTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String, // Label is mandatory and always visible above the field
    modifier: Modifier = Modifier,
    placeholder: String? = null,
    leadingIcon: ImageVector? = null,
    trailingIcon: @Composable (() -> Unit)? = null,
    isError: Boolean = false,
    errorMessage: String? = null,
    singleLine: Boolean = true,
    visualTransformation: VisualTransformation = VisualTransformation.None,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    keyboardActions: KeyboardActions = KeyboardActions.Default,
    enabled: Boolean = true,
    ground: SurfaceGround = if (isDark) SurfaceGround.DARK else SurfaceGround.LIGHT
) {
    val isDarkGround = ground == SurfaceGround.DARK
    val labelColor = if (isDarkGround) White else Charcoal
    val borderColor = if (isDarkGround) Purple700 else CardBorderLight
    val focusedBorderColor = if (isDarkGround) HarvestLime else SavPurple

    Column(modifier = modifier.fillMaxWidth()) {
        // Label always visible above the field
        Text(
            text = label,
            style = AppTypography.label, // 11px Bold caps
            color = labelColor,
            modifier = Modifier.padding(bottom = AppSpacing.xs)
        )

        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 52.dp), // 52px tall specification
            enabled = enabled,
            isError = isError,
            singleLine = singleLine,
            textStyle = AppTypography.body.copy(color = TextDark),
            shape = AppShapes.input, // 12px radius
            placeholder = if (placeholder != null) {
                { Text(text = placeholder, style = AppTypography.body, color = TextSlate400) }
            } else null,
            leadingIcon = if (leadingIcon != null) {
                {
                    Icon(
                        imageVector = leadingIcon,
                        contentDescription = null,
                        tint = if (isError) SemanticErrorLight else TextSlate400,
                        modifier = Modifier.size(AppIconSize.lg)
                    )
                }
            } else null,
            trailingIcon = trailingIcon,
            visualTransformation = visualTransformation,
            keyboardOptions = keyboardOptions,
            keyboardActions = keyboardActions,
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = SurfaceBg,
                unfocusedContainerColor = SurfaceBg,
                disabledContainerColor = SurfaceBg.copy(alpha = 0.5f),
                focusedBorderColor = focusedBorderColor,
                unfocusedBorderColor = borderColor,
                errorBorderColor = SemanticErrorLight,
                cursorColor = if (isDark) HarvestLime else SavPurple
            )
        )

        if (isError && !errorMessage.isNullOrBlank()) {
            Row(
                modifier = Modifier.padding(top = AppSpacing.xs, start = AppSpacing.xs),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.ErrorOutline,
                    contentDescription = "Error",
                    modifier = Modifier.size(AppIconSize.sm),
                    tint = SemanticErrorLight
                )
                Spacer(modifier = Modifier.width(AppSpacing.xs))
                Text(
                    text = errorMessage,
                    style = AppTypography.small,
                    color = SemanticErrorLight
                )
            }
        }
    }
}

// ==============================================================================
// 6. TOGGLES & SWITCHES
// ==============================================================================

@Composable
fun AfriSavSwitch(
    checked: Boolean,
    onCheckedChange: ((Boolean) -> Unit)?,
    modifier: Modifier = Modifier,
    enabled: Boolean = true
) {
    val checkedTrackColor = if (isDark) HarvestLime else SavPurple
    val checkedThumbColor = if (isDark) Charcoal else White

    Switch(
        checked = checked,
        onCheckedChange = onCheckedChange,
        modifier = modifier,
        enabled = enabled,
        colors = SwitchDefaults.colors(
            checkedThumbColor = checkedThumbColor,
            checkedTrackColor = checkedTrackColor,
            checkedBorderColor = checkedTrackColor,
            uncheckedThumbColor = TextSlate400,
            uncheckedTrackColor = if (isDark) Purple900 else Purple100,
            uncheckedBorderColor = if (isDark) Purple700 else Purple200,
            disabledCheckedThumbColor = checkedThumbColor.copy(alpha = 0.6f),
            disabledCheckedTrackColor = checkedTrackColor.copy(alpha = 0.38f)
        )
    )
}

@Composable
fun AfriSavSwitchRow(
    title: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
    subtitle: String? = null,
    icon: ImageVector? = null,
    enabled: Boolean = true
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(AppShapes.card)
            .clickable(enabled = enabled) { onCheckedChange(!checked) }
            .padding(vertical = AppSpacing.sm),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(
            modifier = Modifier.weight(1f),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (icon != null) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(if (isDark) DeepPlum else Purple50),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        modifier = Modifier.size(AppIconSize.md),
                        tint = if (checked) (if (isDark) HarvestLime else SavPurple) else TextSlate500
                    )
                }
                Spacer(modifier = Modifier.width(AppSpacing.md))
            }
            Column {
                Text(
                    text = title,
                    style = AppTypography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                    color = TextDark
                )
                if (subtitle != null) {
                    Text(
                        text = subtitle,
                        style = AppTypography.small,
                        color = if (isDark) Purple300 else TextSlate400
                    )
                }
            }
        }
        AfriSavSwitch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            enabled = enabled
        )
    }
}

// ==============================================================================
// 7. TABS & SEGMENTED CONTROLS
// ==============================================================================

@Composable
fun AfriSavSegmentedControl(
    items: List<String>,
    selectedIndex: Int,
    onSelect: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(AppShapes.pill)
            .background(if (isDark) DeepPlum else Purple100)
            .border(1.dp, if (isDark) Purple800 else Purple200, AppShapes.pill)
            .padding(4.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        items.forEachIndexed { index, title ->
            val isSelected = index == selectedIndex
            val activeBg = if (isDark) Purple800 else White
            val activeText = if (isDark) White else Charcoal
            val inactiveText = if (isDark) Purple300 else TextSlate500

            Box(
                modifier = Modifier
                    .weight(1f)
                    .height(40.dp)
                    .clip(AppShapes.pill)
                    .background(if (isSelected) activeBg else Color.Transparent)
                    .then(
                        if (isSelected) Modifier.afriSavSoftShadow(AppShapes.pill)
                        else Modifier
                    )
                    .clickable { onSelect(index) },
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = title,
                    style = AppTypography.button.copy(
                        fontSize = 13.sp,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                    ),
                    color = if (isSelected) activeText else inactiveText,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

// ==============================================================================
// 8. SEMANTIC BADGES & TAGS
// Rule: Every semantic state must always pair colour with an icon and a word — never colour alone.
// ==============================================================================

enum class AfriSavBadgeType {
    SUCCESS,
    WARNING,
    ERROR,
    INFO,
    NEUTRAL
}

@Composable
fun AfriSavBadge(
    text: String,
    type: AfriSavBadgeType,
    modifier: Modifier = Modifier,
    icon: ImageVector? = null,
    isPill: Boolean = true,
    ground: SurfaceGround = if (isDark) SurfaceGround.DARK else SurfaceGround.LIGHT
) {
    val dark = ground == SurfaceGround.DARK

    // Semantic tokens paired with dedicated icons
    val (bgColor, contentColor, defaultIcon) = when (type) {
        AfriSavBadgeType.SUCCESS -> {
            val bg = if (dark) DeepPlum else Color(0xFFDCFCE7)
            val fg = if (dark) SemanticSuccessPlum else SemanticSuccessLight
            Triple(bg, fg, Icons.Default.CheckCircle)
        }
        AfriSavBadgeType.WARNING -> {
            val bg = if (dark) DeepPlum else Color(0xFFFEF3C7)
            val fg = if (dark) SemanticWarningPlum else SemanticWarningLight
            Triple(bg, fg, Icons.Default.Warning)
        }
        AfriSavBadgeType.ERROR -> {
            val bg = if (dark) DeepPlum else Color(0xFFFEE2E2)
            val fg = if (dark) SemanticErrorPlum else SemanticErrorLight
            Triple(bg, fg, Icons.Default.ErrorOutline)
        }
        AfriSavBadgeType.INFO -> {
            val bg = if (dark) DeepPlum else Purple100
            val fg = if (dark) SemanticInfoPlum else SemanticInfoLight
            Triple(bg, fg, Icons.Default.Info)
        }
        AfriSavBadgeType.NEUTRAL -> {
            val bg = if (dark) Purple900 else Purple50
            val fg = if (dark) Purple200 else Charcoal
            Triple(bg, fg, Icons.Default.Circle)
        }
    }

    val resolvedIcon = icon ?: defaultIcon
    val shape = if (isPill) AppShapes.pill else AppShapes.chip

    Row(
        modifier = modifier
            .clip(shape)
            .background(bgColor)
            .padding(horizontal = AppSpacing.sm, vertical = AppSpacing.xs),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center
    ) {
        // Colour is ALWAYS paired with an icon and a word
        Icon(
            imageVector = resolvedIcon,
            contentDescription = null,
            modifier = Modifier.size(AppIconSize.sm), // 16px icon
            tint = contentColor
        )
        Spacer(modifier = Modifier.width(4.dp))
        Text(
            text = text,
            style = AppTypography.label, // 11px Bold caps
            color = contentColor
        )
    }
}

// ==============================================================================
// 9. ICONS (Rounded-line, consistent 1.75px stroke at 24px)
// Sizes: 16, 20, 24, 32px. No emoji anywhere in UI.
// Excluded motifs: piggy bank, shopping cart, dollar/naira symbol, Africa outline, plate/fork, coin, shield.
// ==============================================================================

@Composable
fun AfriSavIcon(
    imageVector: ImageVector,
    contentDescription: String?,
    modifier: Modifier = Modifier,
    size: Dp = AppIconSize.lg, // 24px default
    tint: Color = TextDark
) {
    Icon(
        imageVector = imageVector,
        contentDescription = contentDescription,
        modifier = modifier.size(size),
        tint = tint
    )
}

// ==============================================================================
// 10. SECTION & PAGE HEADERS
// ==============================================================================

@Composable
fun AfriSavSectionHeader(
    title: String,
    modifier: Modifier = Modifier,
    actionText: String? = null,
    onActionClick: (() -> Unit)? = null,
    ground: SurfaceGround = if (isDark) SurfaceGround.DARK else SurfaceGround.LIGHT
) {
    val isDarkGround = ground == SurfaceGround.DARK
    val textColor = if (isDarkGround) White else Charcoal
    val actionColor = if (isDarkGround) HarvestLime else SavPurple

    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = AppSpacing.xs),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = title,
            style = AppTypography.h3, // Sora 700, 20px
            color = textColor
        )
        if (actionText != null && onActionClick != null) {
            TextButton(
                onClick = onActionClick,
                contentPadding = PaddingValues(horizontal = AppSpacing.xs, vertical = AppSpacing.xxs)
            ) {
                Text(
                    text = AppFormatters.toSentenceCase(actionText),
                    style = AppTypography.button.copy(fontSize = 13.sp),
                    color = actionColor
                )
            }
        }
    }
}

@Composable
fun AfriSavPageHeader(
    title: String,
    modifier: Modifier = Modifier,
    subtitle: String? = null,
    onBackClick: (() -> Unit)? = null,
    backTestTag: String? = null,
    ground: SurfaceGround = if (isDark) SurfaceGround.DARK else SurfaceGround.LIGHT,
    actions: @Composable (RowScope.() -> Unit)? = null
) {
    val isDarkGround = ground == SurfaceGround.DARK
    val bg = if (isDarkGround) DeepPlum else SurfaceBg
    val borderCol = if (isDarkGround) CardBorderDark else CardBorderLight
    val textCol = if (isDarkGround) White else Charcoal
    val subCol = if (isDarkGround) Purple300 else TextSlate400

    Surface(
        modifier = modifier.fillMaxWidth(),
        color = bg,
        border = BorderStroke(1.dp, borderCol)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(horizontal = AppSpacing.screenGutter, vertical = AppSpacing.md)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f, fill = false)
                ) {
                    if (onBackClick != null) {
                        IconButton(
                            onClick = onBackClick,
                            modifier = Modifier
                                .then(if (backTestTag != null) Modifier.testTag(backTestTag) else Modifier)
                                .size(AppIconSize.xxl) // 48dp minimum target
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Back",
                                tint = textCol
                            )
                        }
                        Spacer(modifier = Modifier.width(AppSpacing.xs))
                    }
                    Column {
                        Text(
                            text = title,
                            style = AppTypography.h2, // Sora 700, 24px
                            color = textCol
                        )
                        if (subtitle != null) {
                            Text(
                                text = subtitle,
                                style = AppTypography.small,
                                color = subCol
                            )
                        }
                    }
                }
                if (actions != null) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        content = actions
                    )
                }
            }
        }
    }
}

// ==============================================================================
// 11. DESIGN SYSTEM SHOWCASE SCREEN
// Isolated verification for all tokens, ramps, button rules, and WCAG AA contrast.
// ==============================================================================

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DesignSystemShowcaseScreen(
    onBackClick: () -> Unit
) {
    var forceDarkMode by remember { mutableStateOf(false) }
    var selectedSegTab by remember { mutableStateOf(0) }
    var toggleState1 by remember { mutableStateOf(true) }
    var toggleState2 by remember { mutableStateOf(false) }
    var sampleInputValue by remember { mutableStateOf("Rice & Grains Vault") }
    var sampleErrorValue by remember { mutableStateOf("800") }

    val contentTheme = @Composable {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = {
                        Text(
                            "AfriSav Brand Bible System",
                            style = AppTypography.h3,
                            color = TextDark
                        )
                    },
                    navigationIcon = {
                        IconButton(onClick = onBackClick) {
                            Icon(
                                Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Back",
                                tint = TextDark
                            )
                        }
                    },
                    actions = {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(end = AppSpacing.md)
                        ) {
                            Text(
                                if (forceDarkMode) "Deep Plum Mode" else "Light Cream Mode",
                                style = AppTypography.small,
                                color = TextDark
                            )
                            Spacer(modifier = Modifier.width(AppSpacing.xs))
                            Switch(
                                checked = forceDarkMode,
                                onCheckedChange = { forceDarkMode = it },
                                colors = SwitchDefaults.colors(
                                    checkedThumbColor = Charcoal,
                                    checkedTrackColor = HarvestLime,
                                    uncheckedThumbColor = White,
                                    uncheckedTrackColor = SavPurple
                                )
                            )
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = SurfaceBg,
                        titleContentColor = TextDark
                    )
                )
            },
            containerColor = SoftBackground
        ) { paddingValues ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .verticalScroll(rememberScrollState())
                    .padding(AppSpacing.screenGutter),
                verticalArrangement = Arrangement.spacedBy(AppSpacing.xl)
            ) {
                // Intro Card
                AfriSavCard {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            "Brand Bible (Edition One)",
                            style = AppTypography.h3,
                            color = TextDark
                        )
                        AfriSavBadge("WCAG 2.1 AA", AfriSavBadgeType.SUCCESS)
                    }
                    Spacer(modifier = Modifier.height(AppSpacing.xs))
                    Text(
                        "Sav Purple (#5B21B6), Deep Plum (#32105F), Harvest Lime (#A3E635), Warm Cream (#FFF8EA), White, Charcoal. Sora + Plus Jakarta Sans.",
                        style = AppTypography.body,
                        color = if (forceDarkMode) Purple300 else TextSlate500
                    )
                }

                // 1. COLOUR RAMPS & TOKENS
                Text("1. Colour Tokens & Ramps", style = AppTypography.h2, color = TextDark)
                AfriSavCard {
                    Text("Core Palette", style = AppTypography.h3, color = TextDark)
                    Spacer(modifier = Modifier.height(AppSpacing.sm))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(AppSpacing.xs)
                    ) {
                        ColorSwatchBox("Sav Purple", SavPurple, White, Modifier.weight(1f))
                        ColorSwatchBox("Deep Plum", DeepPlum, White, Modifier.weight(1f))
                        ColorSwatchBox("Lime Accent", HarvestLime, Charcoal, Modifier.weight(1f))
                        ColorSwatchBox("Warm Cream", WarmCream, Charcoal, Modifier.weight(1f))
                    }

                    Spacer(modifier = Modifier.height(AppSpacing.md))
                    Text("Purple Tint Ramp (50–950)", style = AppTypography.small.copy(fontWeight = FontWeight.Bold), color = TextDark)
                    Spacer(modifier = Modifier.height(AppSpacing.xs))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(2.dp)
                    ) {
                        val purpleRamp = listOf(Purple50, Purple100, Purple200, Purple300, Purple400, Purple500, Purple600, Purple700, Purple800, Purple900, Purple950)
                        purpleRamp.forEach { color ->
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .height(24.dp)
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(color)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(AppSpacing.md))
                    Text("Lime Ramp (100 / 400 / 700)", style = AppTypography.small.copy(fontWeight = FontWeight.Bold), color = TextDark)
                    Spacer(modifier = Modifier.height(AppSpacing.xs))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(AppSpacing.xs)
                    ) {
                        ColorSwatchBox("Lime 100", Lime100, Charcoal, Modifier.weight(1f))
                        ColorSwatchBox("Lime 400", Lime400, Charcoal, Modifier.weight(1f))
                        ColorSwatchBox("Lime 700 (Light Ink)", Lime700, White, Modifier.weight(1f))
                    }
                }

                // 2. THE BUTTON RULE SHOWCASE
                Text("2. The Button Rule (Ground Decides Action Colour)", style = AppTypography.h2, color = TextDark)

                // Light Ground Container (White / Warm Cream)
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = AppShapes.card,
                    colors = CardDefaults.cardColors(containerColor = WarmCream),
                    border = BorderStroke(1.dp, CardBorderLight)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(AppSpacing.cardPadding),
                        verticalArrangement = Arrangement.spacedBy(AppSpacing.md)
                    ) {
                        Text(
                            "Light Ground (Warm Cream / White)",
                            style = AppTypography.h3,
                            color = Charcoal
                        )
                        Text(
                            "Sav Purple button (#5B21B6), White label (#FFFFFF) — 8.98:1 contrast.",
                            style = AppTypography.small,
                            color = Charcoal
                        )
                        AfriSavPrimaryButton(
                            text = "Start saving (Sav Purple)",
                            ground = SurfaceGround.LIGHT,
                            onClick = { }
                        )
                        AfriSavSecondaryButton(
                            text = "View details (Charcoal border)",
                            ground = SurfaceGround.LIGHT,
                            onClick = { }
                        )
                    }
                }

                // Dark Ground Container (Deep Plum #32105F)
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = AppShapes.card,
                    colors = CardDefaults.cardColors(containerColor = DeepPlum),
                    border = BorderStroke(1.dp, Purple800)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(AppSpacing.cardPadding),
                        verticalArrangement = Arrangement.spacedBy(AppSpacing.md)
                    ) {
                        Text(
                            "Dark Ground (Deep Plum #32105F)",
                            style = AppTypography.h3,
                            color = White
                        )
                        Text(
                            "Harvest Lime button (#A3E635), Charcoal label (#171717) — 11.89:1 contrast.",
                            style = AppTypography.small,
                            color = Purple300
                        )
                        AfriSavPrimaryButton(
                            text = "Confirm transaction (Lime)",
                            ground = SurfaceGround.DARK,
                            onClick = { }
                        )
                        AfriSavSecondaryButton(
                            text = "Cancel (Purple 300 border)",
                            ground = SurfaceGround.DARK,
                            onClick = { }
                        )
                    }
                }

                // Disabled State
                AfriSavCard {
                    Text("Disabled State (38% Opacity, No Colour Shift)", style = AppTypography.h3, color = TextDark)
                    Spacer(modifier = Modifier.height(AppSpacing.sm))
                    AfriSavPrimaryButton(
                        text = "Disabled action",
                        enabled = false,
                        onClick = { }
                    )
                }

                // 3. TYPOGRAPHY SCALE
                Text("3. Typography Scale (Sora & Plus Jakarta Sans)", style = AppTypography.h2, color = TextDark)
                AfriSavCard {
                    Text("Display (Sora 800, 40px)", style = AppTypography.display, color = TextDark)
                    Spacer(modifier = Modifier.height(AppSpacing.xs))
                    Text("H1 (Sora 800, 32px)", style = AppTypography.h1, color = TextDark)
                    Spacer(modifier = Modifier.height(AppSpacing.xs))
                    Text("H2 (Sora 700, 24px)", style = AppTypography.h2, color = TextDark)
                    Spacer(modifier = Modifier.height(AppSpacing.xs))
                    Text("H3 (Sora 700, 20px)", style = AppTypography.h3, color = TextDark)
                    Spacer(modifier = Modifier.height(AppSpacing.xs))
                    Text("Body L (Jakarta 400, 17px) — Leading editorial intro copy.", style = AppTypography.bodyL, color = TextDark)
                    Spacer(modifier = Modifier.height(AppSpacing.xs))
                    Text("Body (Jakarta 400, 15px) — Standard readable UI and description text.", style = AppTypography.body, color = TextDark)
                    Spacer(modifier = Modifier.height(AppSpacing.xs))
                    Text("Small (Jakarta 500, 13px) — Secondary timestamps and captions.", style = AppTypography.small, color = if (forceDarkMode) Purple300 else TextSlate400)
                    Spacer(modifier = Modifier.height(AppSpacing.xs))
                    Text("LABEL LEVEL (JAKARTA 700 CAPS, 11PX)", style = AppTypography.label, color = if (forceDarkMode) HarvestLime else SavPurple)
                }

                // 4. AMOUNT DISPLAY & CURRENCY RULES
                Text("4. Amount Display & Currency Formatting", style = AppTypography.h2, color = TextDark)
                AfriSavCard {
                    Text("Standard Whole Amount (No .00 in UI)", style = AppTypography.small, color = if (forceDarkMode) Purple300 else TextSlate400)
                    AfriSavAmountDisplay(amount = 25000.0, size = AmountDisplaySize.LARGE)

                    Spacer(modifier = Modifier.height(AppSpacing.md))
                    Text("Non-Whole Amount (With Kobo)", style = AppTypography.small, color = if (forceDarkMode) Purple300 else TextSlate400)
                    AfriSavAmountDisplay(amount = 1250.50, size = AmountDisplaySize.MEDIUM)

                    Spacer(modifier = Modifier.height(AppSpacing.md))
                    Text("Abbreviated in Tight Space (>6 figures)", style = AppTypography.small, color = if (forceDarkMode) Purple300 else TextSlate400)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(AppSpacing.lg)
                    ) {
                        AfriSavAmountDisplay(amount = 1200000.0, size = AmountDisplaySize.SMALL, abbreviate = true)
                        AfriSavAmountDisplay(amount = 250000.0, size = AmountDisplaySize.SMALL, abbreviate = true)
                    }
                }

                // 5. PROGRESS BAR (Always Paired With Figure & Percentage)
                Text("5. Progress Bar (10px Pill, Paired with Figure)", style = AppTypography.h2, color = TextDark)
                AfriSavCard {
                    AfriSavProgressBar(
                        progress = 0.75f,
                        label = "Goal:",
                        figure = "₦187,500 of ₦250,000",
                        percentage = 75.0
                    )
                    Spacer(modifier = Modifier.height(AppSpacing.lg))
                    AfriSavProgressBar(
                        progress = 0.40f,
                        label = "Locked:",
                        figure = "₦40,000 of ₦100,000",
                        percentage = 40.0
                    )
                }

                // 6. CARDS & ELEVATION
                Text("6. Cards & Shadows (Border OR Shadow Rule)", style = AppTypography.h2, color = TextDark)
                AfriSavCard {
                    Text("Static Card (1px #E8DCC6 Border, 0 Shadow)", style = AppTypography.h3, color = TextDark)
                    Text("20px radius, 20px padding. Content sits comfortably with ample negative space.", style = AppTypography.body, color = if (forceDarkMode) Purple300 else TextSlate500)
                }
                AfriSavClickableCard(onClick = { }) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Interactive Clickable Card (Soft Shadow, 0 Border)", style = AppTypography.h3, color = TextDark)
                        Text("Shadow: 0 8px 24px -16px rgba(50,16,95,.28). Never border AND shadow together.", style = AppTypography.small, color = if (forceDarkMode) Purple300 else TextSlate400)
                    }
                    Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null, tint = if (forceDarkMode) HarvestLime else SavPurple)
                }

                // 7. INPUT FIELDS
                Text("7. Input Fields (52px, 12px Radius, Visible Label)", style = AppTypography.h2, color = TextDark)
                AfriSavCard {
                    AfriSavTextField(
                        value = sampleInputValue,
                        onValueChange = { sampleInputValue = it },
                        label = "Produce Savings Target Name",
                        placeholder = "e.g. Rice & Grains Vault",
                        leadingIcon = Icons.Default.Search
                    )
                    Spacer(modifier = Modifier.height(AppSpacing.md))
                    AfriSavTextField(
                        value = sampleErrorValue,
                        onValueChange = { sampleErrorValue = it },
                        label = "Monthly Deposit (₦)",
                        leadingIcon = Icons.Default.AccountBalanceWallet,
                        isError = true,
                        errorMessage = "Deposit cannot be less than ₦1,000"
                    )
                }

                // 8. TABS & SEGMENTED CONTROLS
                Text("8. Segmented Controls & Tabs", style = AppTypography.h2, color = TextDark)
                AfriSavCard {
                    AfriSavSegmentedControl(
                        items = listOf("Daily", "Weekly", "Monthly"),
                        selectedIndex = selectedSegTab,
                        onSelect = { selectedSegTab = it }
                    )
                }

                // 9. TOGGLES & SWITCHES
                Text("9. Toggles & Switches", style = AppTypography.h2, color = TextDark)
                AfriSavCard {
                    AfriSavSwitchRow(
                        title = "Automated Vault Stash",
                        subtitle = "Round up purchases and lock to vault",
                        icon = Icons.Default.Lock,
                        checked = toggleState1,
                        onCheckedChange = { toggleState1 = it }
                    )
                    Divider(color = if (forceDarkMode) Purple800 else CardBorderLight)
                    AfriSavSwitchRow(
                        title = "Escrow Payout Alerts",
                        subtitle = "SMS and instant app notification",
                        icon = Icons.Default.Notifications,
                        checked = toggleState2,
                        onCheckedChange = { toggleState2 = it }
                    )
                }

                // 10. SEMANTIC BADGES (Never Colour Alone)
                Text("10. Semantic Badges (Colour + Icon + Word)", style = AppTypography.h2, color = TextDark)
                AfriSavCard {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(AppSpacing.sm)
                    ) {
                        AfriSavBadge("Verified", AfriSavBadgeType.SUCCESS)
                        AfriSavBadge("In Escrow", AfriSavBadgeType.WARNING)
                        AfriSavBadge("Failed", AfriSavBadgeType.ERROR)
                    }
                    Spacer(modifier = Modifier.height(AppSpacing.sm))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(AppSpacing.sm)
                    ) {
                        AfriSavBadge("Notice", AfriSavBadgeType.INFO)
                        AfriSavBadge("Standard", AfriSavBadgeType.NEUTRAL)
                    }
                }

                // 11. ICONOGRAPHY RULES
                Text("11. Iconography (1.75px Stroke, Minimal Detail, No Emoji)", style = AppTypography.h2, color = TextDark)
                AfriSavCard {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(AppSpacing.lg)
                    ) {
                        AfriSavIcon(Icons.Default.AccountBalanceWallet, "Wallet 16px", size = AppIconSize.sm)
                        AfriSavIcon(Icons.Default.AccountBalanceWallet, "Wallet 20px", size = AppIconSize.md)
                        AfriSavIcon(Icons.Default.AccountBalanceWallet, "Wallet 24px", size = AppIconSize.lg)
                        AfriSavIcon(Icons.Default.AccountBalanceWallet, "Wallet 32px", size = AppIconSize.xl)
                    }
                    Spacer(modifier = Modifier.height(AppSpacing.sm))
                    Text(
                        "Icons inherit ink colour. No emojis. Excluded motifs (piggy bank, cart, naira symbol, plate/fork, coins, shields) avoided.",
                        style = AppTypography.small,
                        color = if (forceDarkMode) Purple300 else TextSlate400
                    )
                }

                Spacer(modifier = Modifier.height(AppSpacing.xxl))
            }
        }
    }

    AfriSavTheme(darkTheme = forceDarkMode) {
        contentTheme()
    }
}

@Composable
private fun ColorSwatchBox(
    name: String,
    bg: Color,
    textColor: Color,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .height(56.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(bg)
            .border(1.dp, CardBorderLight.copy(alpha = 0.4f), RoundedCornerShape(8.dp))
            .padding(4.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = name,
            style = AppTypography.small.copy(fontSize = 10.sp, fontWeight = FontWeight.Bold),
            color = textColor,
            textAlign = TextAlign.Center
        )
    }
}
