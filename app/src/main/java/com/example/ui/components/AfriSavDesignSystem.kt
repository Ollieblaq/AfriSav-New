package com.example.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateDpAsState
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
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
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
// 1. BUTTONS (Primary, Secondary, Tertiary, Accent, Icon)
// ==============================================================================

/**
 * Primary action button: Nigerian emerald green container, white bold text,
 * 48dp minimum touch target, rounded 12dp corners.
 */
@Composable
fun AfriSavPrimaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    containerColor: Color = PrimaryGreen,
    contentColor: Color = Color.White,
    leadingIcon: ImageVector? = null,
    trailingIcon: ImageVector? = null,
    isLoading: Boolean = false,
    fullWidth: Boolean = true
) {
    Button(
        onClick = onClick,
        modifier = modifier
            .then(if (fullWidth) Modifier.fillMaxWidth() else Modifier)
            .heightIn(min = 48.dp),
        enabled = enabled && !isLoading,
        shape = AppShapes.button,
        colors = ButtonDefaults.buttonColors(
            containerColor = containerColor,
            contentColor = contentColor,
            disabledContainerColor = containerColor.copy(alpha = 0.38f),
            disabledContentColor = contentColor.copy(alpha = 0.7f)
        ),
        contentPadding = PaddingValues(horizontal = AppSpacing.lg, vertical = AppSpacing.md)
    ) {
        if (isLoading) {
            CircularProgressIndicator(
                color = contentColor,
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
                        modifier = Modifier.size(AppIconSize.md),
                        tint = contentColor
                    )
                    Spacer(modifier = Modifier.width(AppSpacing.sm))
                }
                Text(
                    text = text,
                    style = AppTypography.label.copy(fontWeight = FontWeight.SemiBold),
                    color = contentColor
                )
                if (trailingIcon != null) {
                    Spacer(modifier = Modifier.width(AppSpacing.sm))
                    Icon(
                        imageVector = trailingIcon,
                        contentDescription = null,
                        modifier = Modifier.size(AppIconSize.md),
                        tint = contentColor
                    )
                }
            }
        }
    }
}

/**
 * Secondary button: Crisp 1px border, surface background, high-contrast text.
 */
@Composable
fun AfriSavSecondaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    leadingIcon: ImageVector? = null,
    trailingIcon: ImageVector? = null,
    fullWidth: Boolean = true
) {
    AfriSavOutlinedButton(
        text = text,
        onClick = onClick,
        modifier = modifier,
        enabled = enabled,
        leadingIcon = leadingIcon,
        trailingIcon = trailingIcon,
        fullWidth = fullWidth
    )
}

/**
 * Outlined button: Standardized outlined button token.
 */
@Composable
fun AfriSavOutlinedButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    borderColor: Color = BorderSlate100,
    containerColor: Color = SurfaceBg,
    contentColor: Color = TextDark,
    leadingIcon: ImageVector? = null,
    trailingIcon: ImageVector? = null,
    fullWidth: Boolean = true
) {
    OutlinedButton(
        onClick = onClick,
        modifier = modifier
            .then(if (fullWidth) Modifier.fillMaxWidth() else Modifier)
            .heightIn(min = 48.dp),
        enabled = enabled,
        shape = AppShapes.button,
        border = BorderStroke(1.dp, borderColor),
        colors = ButtonDefaults.outlinedButtonColors(
            containerColor = containerColor,
            contentColor = contentColor,
            disabledContainerColor = containerColor.copy(alpha = 0.5f),
            disabledContentColor = TextSlate400
        ),
        contentPadding = PaddingValues(horizontal = AppSpacing.lg, vertical = AppSpacing.md)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            if (leadingIcon != null) {
                Icon(
                    imageVector = leadingIcon,
                    contentDescription = null,
                    modifier = Modifier.size(AppIconSize.md),
                    tint = contentColor
                )
                Spacer(modifier = Modifier.width(AppSpacing.sm))
            }
            Text(
                text = text,
                style = AppTypography.label.copy(fontWeight = FontWeight.SemiBold),
                color = contentColor
            )
            if (trailingIcon != null) {
                Spacer(modifier = Modifier.width(AppSpacing.sm))
                Icon(
                    imageVector = trailingIcon,
                    contentDescription = null,
                    modifier = Modifier.size(AppIconSize.md),
                    tint = contentColor
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
    color: Color = PrimaryGreen
) {
    TextButton(
        onClick = onClick,
        modifier = modifier.heightIn(min = 40.dp),
        enabled = enabled,
        shape = AppShapes.button,
        colors = ButtonDefaults.textButtonColors(
            contentColor = color,
            disabledContentColor = TextSlate400
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
                text = text,
                style = AppTypography.label.copy(fontWeight = FontWeight.SemiBold),
                color = color
            )
        }
    }
}

/**
 * Orange highlight button: Used for food savings goals, alerts, or key market actions.
 */
@Composable
fun AfriSavOrangeButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    leadingIcon: ImageVector? = null,
    isLoading: Boolean = false,
    fullWidth: Boolean = true
) {
    Button(
        onClick = onClick,
        modifier = modifier
            .then(if (fullWidth) Modifier.fillMaxWidth() else Modifier)
            .heightIn(min = 48.dp),
        enabled = enabled && !isLoading,
        shape = AppShapes.button,
        colors = ButtonDefaults.buttonColors(
            containerColor = SecondaryOrange,
            contentColor = Color.White,
            disabledContainerColor = SecondaryOrange.copy(alpha = 0.38f),
            disabledContentColor = Color.White.copy(alpha = 0.7f)
        ),
        contentPadding = PaddingValues(horizontal = AppSpacing.lg, vertical = AppSpacing.md)
    ) {
        if (isLoading) {
            CircularProgressIndicator(
                color = Color.White,
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
                        modifier = Modifier.size(AppIconSize.md),
                        tint = Color.White
                    )
                    Spacer(modifier = Modifier.width(AppSpacing.sm))
                }
                Text(
                    text = text,
                    style = AppTypography.label.copy(fontWeight = FontWeight.SemiBold),
                    color = Color.White
                )
            }
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
            .size(48.dp)
            .then(
                if (containerColor != Color.Transparent) Modifier.clip(CircleShape).background(containerColor)
                else Modifier
            )
            .then(
                if (borderColor != null) Modifier.border(1.dp, borderColor, CircleShape)
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
// 2. CARDS (Standard, Elevated, Balance, Clickable)
// ==============================================================================

/**
 * Clean fintech card: Neutral white/slate container, crisp 1px border, 16dp radius.
 */
@Composable
fun AfriSavCard(
    modifier: Modifier = Modifier,
    containerColor: Color = SurfaceBg,
    borderColor: Color = BorderSlate100,
    shape: RoundedCornerShape = AppShapes.card,
    contentPadding: PaddingValues = PaddingValues(AppSpacing.lg),
    content: @Composable ColumnScope.() -> Unit
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = shape,
        colors = CardDefaults.cardColors(containerColor = containerColor),
        border = BorderStroke(1.dp, borderColor)
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
 * Elevated card with subtle shadow for prominent modules.
 */
@Composable
fun AfriSavElevatedCard(
    modifier: Modifier = Modifier,
    containerColor: Color = SurfaceBg,
    borderColor: Color = BorderSlate100,
    shape: RoundedCornerShape = AppShapes.card,
    contentPadding: PaddingValues = PaddingValues(AppSpacing.lg),
    content: @Composable ColumnScope.() -> Unit
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .shadow(elevation = AppElevation.low, shape = shape, spotColor = Color.Black.copy(alpha = 0.05f)),
        shape = shape,
        colors = CardDefaults.cardColors(containerColor = containerColor),
        border = BorderStroke(1.dp, borderColor)
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
 * Clickable card with ripple and clean border.
 */
@Composable
fun AfriSavClickableCard(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    containerColor: Color = SurfaceBg,
    borderColor: Color = BorderSlate100,
    shape: RoundedCornerShape = AppShapes.card,
    contentPadding: PaddingValues = PaddingValues(AppSpacing.lg),
    content: @Composable RowScope.() -> Unit
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = shape,
        colors = CardDefaults.cardColors(containerColor = containerColor),
        border = BorderStroke(1.dp, borderColor)
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
 * Dedicated Fintech Balance Card: Prominently showcases balances with
 * high-contrast figures, currency symbol, and optional action buttons.
 */
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
    AfriSavCard(
        modifier = modifier,
        containerColor = SurfaceBg,
        borderColor = BorderSlate100,
        shape = AppShapes.xl,
        contentPadding = PaddingValues(AppSpacing.xl)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = title,
                style = AppTypography.label,
                color = TextSlate500
            )
            if (badgeText != null) {
                AfriSavBadge(
                    text = badgeText,
                    type = AfriSavBadgeType.BRAND_GREEN
                )
            }
        }

        Spacer(modifier = Modifier.height(AppSpacing.sm))

        Text(
            text = amount,
            style = AppTypography.balanceLarge,
            color = TextDark
        )

        if (subtitle != null) {
            Spacer(modifier = Modifier.height(AppSpacing.xs))
            Text(
                text = subtitle,
                style = AppTypography.caption,
                color = TextSlate400
            )
        }

        if (onPrimaryAction != null || onSecondaryAction != null) {
            Spacer(modifier = Modifier.height(AppSpacing.lg))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(AppSpacing.md)
            ) {
                if (onPrimaryAction != null) {
                    Box(modifier = Modifier.weight(1f)) {
                        AfriSavPrimaryButton(
                            text = primaryActionText,
                            onClick = onPrimaryAction,
                            fullWidth = true
                        )
                    }
                }
                if (onSecondaryAction != null) {
                    Box(modifier = Modifier.weight(1f)) {
                        AfriSavSecondaryButton(
                            text = secondaryActionText,
                            onClick = onSecondaryAction,
                            fullWidth = true
                        )
                    }
                }
            }
        }
    }
}

// ==============================================================================
// 3. INPUT FIELDS
// ==============================================================================

/**
 * Standardized Outlined Text Field with 12dp corner radius, unified tokens,
 * supporting text, and light/dark mode responsive colors.
 */
@Composable
fun AfriSavTextField(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    label: String? = null,
    placeholder: String? = null,
    leadingIcon: ImageVector? = null,
    trailingIcon: @Composable (() -> Unit)? = null,
    isError: Boolean = false,
    errorMessage: String? = null,
    singleLine: Boolean = true,
    visualTransformation: VisualTransformation = VisualTransformation.None,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    keyboardActions: KeyboardActions = KeyboardActions.Default,
    enabled: Boolean = true
) {
    Column(modifier = modifier.fillMaxWidth()) {
        if (label != null) {
            Text(
                text = label,
                style = AppTypography.label,
                color = TextDark,
                modifier = Modifier.padding(bottom = AppSpacing.xs)
            )
        }
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier.fillMaxWidth(),
            enabled = enabled,
            isError = isError,
            singleLine = singleLine,
            shape = AppShapes.input,
            placeholder = if (placeholder != null) {
                { Text(text = placeholder, style = AppTypography.body, color = TextSlate400) }
            } else null,
            leadingIcon = if (leadingIcon != null) {
                {
                    Icon(
                        imageVector = leadingIcon,
                        contentDescription = null,
                        tint = if (isError) AppColors.error else TextSlate400,
                        modifier = Modifier.size(AppIconSize.md)
                    )
                }
            } else null,
            trailingIcon = trailingIcon,
            visualTransformation = visualTransformation,
            keyboardOptions = keyboardOptions,
            keyboardActions = keyboardActions,
            colors = defaultTextFieldColors()
        )
        if (isError && !errorMessage.isNullOrBlank()) {
            Text(
                text = errorMessage,
                style = AppTypography.bodySmall,
                color = AppColors.error,
                modifier = Modifier.padding(top = AppSpacing.xs, start = AppSpacing.xs)
            )
        }
    }
}

// ==============================================================================
// 4. TOGGLES & SWITCHES
// ==============================================================================

/**
 * Fintech Switch: PrimaryGreen track when checked, clean white thumb, smooth animation.
 */
@Composable
fun AfriSavSwitch(
    checked: Boolean,
    onCheckedChange: ((Boolean) -> Unit)?,
    modifier: Modifier = Modifier,
    enabled: Boolean = true
) {
    Switch(
        checked = checked,
        onCheckedChange = onCheckedChange,
        modifier = modifier,
        enabled = enabled,
        colors = SwitchDefaults.colors(
            checkedThumbColor = Color.White,
            checkedTrackColor = PrimaryGreen,
            checkedBorderColor = PrimaryGreen,
            uncheckedThumbColor = TextSlate400,
            uncheckedTrackColor = BorderSlate100,
            uncheckedBorderColor = BorderSlate100,
            disabledCheckedThumbColor = Color.White.copy(alpha = 0.6f),
            disabledCheckedTrackColor = PrimaryGreen.copy(alpha = 0.4f)
        )
    )
}

/**
 * Standard row with label, optional description, and switch.
 */
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
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(SurfaceSubtle),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        modifier = Modifier.size(AppIconSize.md),
                        tint = if (checked) PrimaryGreen else TextSlate500
                    )
                }
                Spacer(modifier = Modifier.width(AppSpacing.md))
            }
            Column {
                Text(
                    text = title,
                    style = AppTypography.bodyMedium,
                    color = TextDark
                )
                if (subtitle != null) {
                    Text(
                        text = subtitle,
                        style = AppTypography.caption,
                        color = TextSlate400
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
// 5. TABS & SEGMENTED CONTROLS
// ==============================================================================

/**
 * Capsule Segmented Control (iOS / Fintech style): Soft container background
 * with a raised white active pill.
 */
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
            .clip(AppShapes.full)
            .background(SurfaceSubtle)
            .border(1.dp, BorderSlate100, AppShapes.full)
            .padding(4.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        items.forEachIndexed { index, title ->
            val isSelected = index == selectedIndex
            val animBg by animateColorAsState(
                targetValue = if (isSelected) SurfaceBg else Color.Transparent,
                animationSpec = tween(200, easing = FastOutSlowInEasing),
                label = "segBg"
            )
            val textColor by animateColorAsState(
                targetValue = if (isSelected) TextDark else TextSlate500,
                label = "segText"
            )

            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(AppShapes.full)
                    .background(animBg)
                    .then(
                        if (isSelected) Modifier.shadow(elevation = 1.dp, shape = AppShapes.full)
                        else Modifier
                    )
                    .clickable { onSelect(index) }
                    .padding(vertical = AppSpacing.sm),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = title,
                    style = AppTypography.label.copy(
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                    ),
                    color = textColor,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

// ==============================================================================
// 6. BADGES, TAGS & STATUS INDICATORS
// ==============================================================================

enum class AfriSavBadgeType {
    SUCCESS,
    WARNING,
    ERROR,
    INFO,
    NEUTRAL,
    BRAND_GREEN,
    BRAND_ORANGE
}

/**
 * Pill status indicator badge with soft tinted backgrounds and strong accessible text.
 */
@Composable
fun AfriSavBadge(
    text: String,
    type: AfriSavBadgeType,
    modifier: Modifier = Modifier,
    icon: ImageVector? = null,
    isPill: Boolean = true
) {
    val dark = isDark
    val (bgColor, contentColor) = when (type) {
        AfriSavBadgeType.SUCCESS -> if (dark) AppColors.successBgDark to Color(0xFF4ADE80)
                                     else AppColors.successBgLight to AppColors.success
        AfriSavBadgeType.WARNING -> if (dark) AppColors.warningBgDark to Color(0xFFFBBF24)
                                     else AppColors.warningBgLight to AppColors.warning
        AfriSavBadgeType.ERROR -> if (dark) AppColors.errorBgDark to Color(0xFFF87171)
                                   else AppColors.errorBgLight to AppColors.error
        AfriSavBadgeType.INFO -> if (dark) AppColors.infoBgDark to Color(0xFF60A5FA)
                                  else AppColors.infoBgLight to AppColors.info
        AfriSavBadgeType.NEUTRAL -> if (dark) Color(0xFF334155) to Color(0xFFCBD5E1)
                                     else Color(0xFFF1F5F9) to TextSlate500
        AfriSavBadgeType.BRAND_GREEN -> if (dark) AppColors.brandGreenBgDark to Color(0xFF86EFAC)
                                         else AppColors.brandGreenBgLight to PrimaryGreen
        AfriSavBadgeType.BRAND_ORANGE -> if (dark) AppColors.brandOrangeBgDark to Color(0xFFFDBA74)
                                          else AppColors.brandOrangeBgLight to SecondaryOrange
    }

    val shape = if (isPill) AppShapes.full else AppShapes.tag

    Row(
        modifier = modifier
            .clip(shape)
            .background(bgColor)
            .padding(horizontal = AppSpacing.sm, vertical = AppSpacing.xs),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center
    ) {
        if (icon != null) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(AppIconSize.xs),
                tint = contentColor
            )
            Spacer(modifier = Modifier.width(4.dp))
        }
        Text(
            text = text,
            style = AppTypography.labelSmall,
            color = contentColor
        )
    }
}

// ==============================================================================
// 7. ICONS (Unified Size & Weight Scale)
// ==============================================================================

/**
 * Standardized icon component using unified size scale and theme colors.
 */
@Composable
fun AfriSavIcon(
    imageVector: ImageVector,
    contentDescription: String?,
    modifier: Modifier = Modifier,
    size: Dp = AppIconSize.md,
    tint: Color = TextDark
) {
    Icon(
        imageVector = imageVector,
        contentDescription = contentDescription,
        modifier = modifier.size(size),
        tint = tint
    )
}

/**
 * Standardized Section Header with consistent typography, spacing, and optional action button.
 */
@Composable
fun AfriSavSectionHeader(
    title: String,
    modifier: Modifier = Modifier,
    actionText: String? = null,
    onActionClick: (() -> Unit)? = null
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = AppSpacing.xs),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = title,
            style = AppTypography.sectionHeader,
            color = TextDark
        )
        if (actionText != null && onActionClick != null) {
            TextButton(
                onClick = onActionClick,
                contentPadding = PaddingValues(horizontal = AppSpacing.xs, vertical = AppSpacing.xxs)
            ) {
                Text(
                    text = actionText,
                    style = AppTypography.label.copy(fontWeight = FontWeight.SemiBold),
                    color = PrimaryGreen
                )
            }
        }
    }
}

/**
 * Standardized Page Header with status bar padding, back button, title, and optional actions.
 */
@Composable
fun AfriSavPageHeader(
    title: String,
    modifier: Modifier = Modifier,
    subtitle: String? = null,
    onBackClick: (() -> Unit)? = null,
    backTestTag: String? = null,
    actions: @Composable (RowScope.() -> Unit)? = null
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        color = SurfaceBg,
        border = BorderStroke(1.dp, BorderSlate100)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(horizontal = AppSpacing.lg, vertical = AppSpacing.md)
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
                                .size(40.dp)
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Back",
                                tint = TextDark
                            )
                        }
                        Spacer(modifier = Modifier.width(AppSpacing.xs))
                    }
                    Column {
                        Text(
                            text = title,
                            style = AppTypography.sectionHeader.copy(fontWeight = FontWeight.Bold),
                            color = TextDark
                        )
                        if (subtitle != null) {
                            Text(
                                text = subtitle,
                                style = AppTypography.caption,
                                color = TextSlate500
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
// 8. DESIGN SYSTEM SHOWCASE / STORYBOOK SCREEN
// ==============================================================================

/**
 * Isolated Component Showcase / Storybook Screen to verify all tokens and
 * shared components in both Light and Dark mode.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DesignSystemShowcaseScreen(
    onBackClick: () -> Unit
) {
    var forceDarkMode by remember { mutableStateOf(false) }
    var selectedSegTab by remember { mutableStateOf(0) }
    var toggleState1 by remember { mutableStateOf(true) }
    var toggleState2 by remember { mutableStateOf(false) }
    var sampleInputValue by remember { mutableStateOf("Fresh Bell Peppers") }
    var sampleErrorValue by remember { mutableStateOf("Invalid amount") }

    val contentTheme = @Composable {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = {
                        Text(
                            "Design System Showcase",
                            style = AppTypography.sectionHeader,
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
                            modifier = Modifier.padding(end = AppSpacing.sm)
                        ) {
                            Text(
                                if (forceDarkMode) "Dark" else "Light",
                                style = AppTypography.caption,
                                color = TextDark
                            )
                            Spacer(modifier = Modifier.width(AppSpacing.xs))
                            Switch(
                                checked = forceDarkMode,
                                onCheckedChange = { forceDarkMode = it },
                                modifier = Modifier.size(36.dp)
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
                    .padding(AppSpacing.lg),
                verticalArrangement = Arrangement.spacedBy(AppSpacing.xl)
            ) {
                // Header introduction
                AfriSavCard(
                    containerColor = SurfaceBg,
                    shape = AppShapes.card
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        AfriSavBadge("AfriSav v2.0", AfriSavBadgeType.BRAND_GREEN)
                        Spacer(modifier = Modifier.width(AppSpacing.sm))
                        AfriSavBadge("Fintech Design Tokens", AfriSavBadgeType.BRAND_ORANGE)
                    }
                    Spacer(modifier = Modifier.height(AppSpacing.sm))
                    Text(
                        "Clean, minimal, fintech-inspired design system with Nigerian emerald green & market orange accents.",
                        style = AppTypography.body,
                        color = TextSlate500
                    )
                }

                // 1. TYPOGRAPHY SCALE
                Text("1. Typography Scale", style = AppTypography.sectionHeader, color = TextDark)
                AfriSavCard {
                    Text("Page Title (24sp Bold)", style = AppTypography.pageTitle, color = TextDark)
                    Spacer(modifier = Modifier.height(AppSpacing.xs))
                    Text("Section Header (18sp SemiBold)", style = AppTypography.sectionHeader, color = TextDark)
                    Spacer(modifier = Modifier.height(AppSpacing.xs))
                    Text("Card Title (15sp SemiBold)", style = AppTypography.cardTitle, color = TextDark)
                    Spacer(modifier = Modifier.height(AppSpacing.xs))
                    Text("Body Text (14sp Normal) — Regular conversational UI text and details.", style = AppTypography.body, color = TextDark)
                    Spacer(modifier = Modifier.height(AppSpacing.xs))
                    Text("Label / Action (13sp Medium)", style = AppTypography.label, color = TextDark)
                    Spacer(modifier = Modifier.height(AppSpacing.xs))
                    Text("Caption (11sp Normal) — Muted timestamps & secondary notes", style = AppTypography.caption, color = TextSlate400)
                    Spacer(modifier = Modifier.height(AppSpacing.sm))
                    Divider(color = BorderSlate100)
                    Spacer(modifier = Modifier.height(AppSpacing.sm))
                    Text("Balance Large (30sp Bold): ₦245,000.00", style = AppTypography.balanceLarge, color = PrimaryGreen)
                }

                // 2. BUTTONS
                Text("2. Buttons & Actions", style = AppTypography.sectionHeader, color = TextDark)
                AfriSavCard {
                    AfriSavPrimaryButton(
                        text = "Primary Emerald Action",
                        leadingIcon = Icons.Default.Savings,
                        onClick = { }
                    )
                    Spacer(modifier = Modifier.height(AppSpacing.md))
                    AfriSavSecondaryButton(
                        text = "Secondary Outlined Button",
                        leadingIcon = Icons.Default.AccountBalanceWallet,
                        onClick = { }
                    )
                    Spacer(modifier = Modifier.height(AppSpacing.md))
                    AfriSavOrangeButton(
                        text = "Orange Accent Action",
                        leadingIcon = Icons.Default.FlashOn,
                        onClick = { }
                    )
                    Spacer(modifier = Modifier.height(AppSpacing.md))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        AfriSavTertiaryButton(
                            text = "Ghost / Tertiary Button",
                            leadingIcon = Icons.Default.Check,
                            onClick = { }
                        )
                        Row(horizontalArrangement = Arrangement.spacedBy(AppSpacing.sm)) {
                            AfriSavIconButton(
                                icon = Icons.Default.Share,
                                contentDescription = "Share",
                                containerColor = SurfaceSubtle,
                                onClick = { }
                            )
                            AfriSavIconButton(
                                icon = Icons.Default.FavoriteBorder,
                                contentDescription = "Favorite",
                                containerColor = SurfaceSubtle,
                                iconTint = SecondaryOrange,
                                onClick = { }
                            )
                        }
                    }
                }

                // 3. CARDS & ELEVATION
                Text("3. Cards & Balance Display", style = AppTypography.sectionHeader, color = TextDark)
                AfriSavBalanceCard(
                    title = "Food Escrow Wallet",
                    amount = "₦184,500.00",
                    subtitle = "3 locked group orders in progress",
                    badgeText = "Active Escrow",
                    onPrimaryAction = { },
                    primaryActionText = "Top Up",
                    onSecondaryAction = { },
                    secondaryActionText = "History"
                )

                // 4. INPUT FIELDS
                Text("4. Input Fields", style = AppTypography.sectionHeader, color = TextDark)
                AfriSavCard {
                    AfriSavTextField(
                        value = sampleInputValue,
                        onValueChange = { sampleInputValue = it },
                        label = "Item or Produce Name",
                        placeholder = "e.g. 50kg Bag of Rice",
                        leadingIcon = Icons.Default.Search
                    )
                    Spacer(modifier = Modifier.height(AppSpacing.md))
                    AfriSavTextField(
                        value = sampleErrorValue,
                        onValueChange = { sampleErrorValue = it },
                        label = "Savings Target",
                        leadingIcon = Icons.Default.Warning,
                        isError = true,
                        errorMessage = "Target amount must be at least ₦1,000"
                    )
                }

                // 5. TABS & SEGMENTED CONTROLS
                Text("5. Segmented Controls & Tabs", style = AppTypography.sectionHeader, color = TextDark)
                AfriSavCard {
                    AfriSavSegmentedControl(
                        items = listOf("All Orders", "Ongoing", "Completed"),
                        selectedIndex = selectedSegTab,
                        onSelect = { selectedSegTab = it }
                    )
                }

                // 6. TOGGLES & SWITCHES
                Text("6. Toggles & Switches", style = AppTypography.sectionHeader, color = TextDark)
                AfriSavCard {
                    AfriSavSwitchRow(
                        title = "Order Tracking Notifications",
                        subtitle = "Receive live dispatch rider updates",
                        icon = Icons.Default.NotificationsActive,
                        checked = toggleState1,
                        onCheckedChange = { toggleState1 = it }
                    )
                    Divider(color = BorderSlate100)
                    AfriSavSwitchRow(
                        title = "Biometric Lock",
                        subtitle = "Require fingerprint/PIN before fund release",
                        icon = Icons.Default.Lock,
                        checked = toggleState2,
                        onCheckedChange = { toggleState2 = it }
                    )
                }

                // 7. STATUS BADGES & TAGS
                Text("7. Badges & Status Indicators", style = AppTypography.sectionHeader, color = TextDark)
                AfriSavCard {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(AppSpacing.sm)
                    ) {
                        AfriSavBadge("Delivered", AfriSavBadgeType.SUCCESS, icon = Icons.Default.CheckCircle)
                        AfriSavBadge("In Escrow", AfriSavBadgeType.WARNING, icon = Icons.Default.HourglassTop)
                        AfriSavBadge("Failed", AfriSavBadgeType.ERROR, icon = Icons.Default.ErrorOutline)
                    }
                    Spacer(modifier = Modifier.height(AppSpacing.sm))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(AppSpacing.sm)
                    ) {
                        AfriSavBadge("Verified Buyer", AfriSavBadgeType.BRAND_GREEN)
                        AfriSavBadge("Food Circle", AfriSavBadgeType.BRAND_ORANGE)
                        AfriSavBadge("Draft", AfriSavBadgeType.NEUTRAL)
                    }
                }

                // 8. SPACING & ICON SCALE
                Text("8. Icon Scale & Spacing Grid", style = AppTypography.sectionHeader, color = TextDark)
                AfriSavCard {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(AppSpacing.md)
                    ) {
                        AfriSavIcon(Icons.Default.Storefront, "Store XS", size = AppIconSize.xs, tint = PrimaryGreen)
                        AfriSavIcon(Icons.Default.Storefront, "Store SM", size = AppIconSize.sm, tint = PrimaryGreen)
                        AfriSavIcon(Icons.Default.Storefront, "Store MD", size = AppIconSize.md, tint = PrimaryGreen)
                        AfriSavIcon(Icons.Default.Storefront, "Store LG", size = AppIconSize.lg, tint = PrimaryGreen)
                        AfriSavIcon(Icons.Default.Storefront, "Store XL", size = AppIconSize.xl, tint = PrimaryGreen)
                    }
                    Spacer(modifier = Modifier.height(AppSpacing.md))
                    Text(
                        "Unified Spacing: 4px, 8px, 12px, 16px, 24px, 32px, 48px",
                        style = AppTypography.caption,
                        color = TextSlate500
                    )
                }

                Spacer(modifier = Modifier.height(AppSpacing.xxl))
            }
        }
    }

    // Wrap in dynamic theme toggle to inspect both Light and Dark mode visually
    AfriSavTheme(darkTheme = forceDarkMode) {
        contentTheme()
    }
}
