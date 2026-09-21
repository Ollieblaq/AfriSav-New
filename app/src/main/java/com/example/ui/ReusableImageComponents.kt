package com.example.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.ui.theme.PrimaryGreen
import com.example.ui.theme.TextSlate400

/**
 * Utility functions to map food categories, user roles, and empty state types 
 * to high-quality, lightweight curated Unsplash images.
 */
fun isRealImageUrl(url: String?): Boolean {
    if (url.isNullOrBlank()) return false
    val trimmed = url.trim()
    return trimmed.startsWith("http://") || 
           trimmed.startsWith("https://") || 
           trimmed.startsWith("content://") || 
           trimmed.startsWith("file://") || 
           trimmed.startsWith("data:image/")
}

fun getPlaceholderFoodImageUrl(category: String?): String {
    val cat = category?.lowercase() ?: ""
    return when {
        cat.contains("rice") -> "https://images.unsplash.com/photo-1586201375761-83865001e31c?auto=format&fit=crop&w=400&q=80"
        cat.contains("beans") -> "https://images.unsplash.com/photo-1551462147-ff29053bfc14?auto=format&fit=crop&w=400&q=80"
        cat.contains("garri") || cat.contains("cassava") -> "https://images.unsplash.com/photo-1590779033100-9f60a05a013d?auto=format&fit=crop&w=400&q=80"
        cat.contains("tomato") -> "https://images.unsplash.com/photo-1597362925123-77861d3fbac7?auto=format&fit=crop&w=400&q=80"
        cat.contains("pepper") || cat.contains("chili") -> "https://images.unsplash.com/photo-1518110168406-d7486f0c608a?auto=format&fit=crop&w=400&q=80"
        cat.contains("yam") || cat.contains("potato") || cat.contains("tuber") -> "https://images.unsplash.com/photo-1590779033100-9f60a05a013d?auto=format&fit=crop&w=400&q=80"
        cat.contains("fish") || cat.contains("seafood") || cat.contains("meat") -> "https://images.unsplash.com/photo-1534482421-64566f976cfa?auto=format&fit=crop&w=400&q=80"
        cat.contains("veg") || cat.contains("onion") -> "https://images.unsplash.com/photo-1597362925123-77861d3fbac7?auto=format&fit=crop&w=400&q=80"
        cat.contains("bundle") || cat.contains("pack") || cat.contains("basket") -> "https://images.unsplash.com/photo-1498837167922-ddd27525d352?auto=format&fit=crop&w=400&q=80"
        else -> "https://images.unsplash.com/photo-1498837167922-ddd27525d352?auto=format&fit=crop&w=400&q=80"
    }
}

fun getPlaceholderAvatarUrl(role: String?): String {
    return when (role?.lowercase() ?: "") {
        "buyer" -> "https://images.unsplash.com/photo-1534528741775-53994a69daeb?auto=format&fit=crop&w=200&q=80"
        "seller", "merchant" -> "https://images.unsplash.com/photo-1507003211169-0a1dd7228f2d?auto=format&fit=crop&w=200&q=80"
        "rider", "courier" -> "https://images.unsplash.com/photo-1500648767791-00dcc994a43e?auto=format&fit=crop&w=200&q=80"
        else -> "https://images.unsplash.com/photo-1535713875002-d1d0cf377fde?auto=format&fit=crop&w=200&q=80"
    }
}

fun getPlaceholderEmptyStateUrl(type: String?): String {
    return when (type?.lowercase() ?: "") {
        "cart" -> "https://images.unsplash.com/photo-1555529669-e69e7aa0ba9a?auto=format&fit=crop&w=400&q=80"
        "history", "orders" -> "https://images.unsplash.com/photo-1454165804606-c3d57bc86b40?auto=format&fit=crop&w=400&q=80"
        "goals" -> "https://images.unsplash.com/photo-1579621970563-ebec7560ff3e?auto=format&fit=crop&w=400&q=80"
        "reviews" -> "https://images.unsplash.com/photo-1516321318423-f06f85e504b3?auto=format&fit=crop&w=400&q=80"
        else -> "https://images.unsplash.com/photo-1555529669-e69e7aa0ba9a?auto=format&fit=crop&w=400&q=80"
    }
}

/**
 * Reusable image component for Food Items.
 * Checks for a valid URL first; if absent or invalid, loads a high-quality category placeholder.
 */
@Composable
fun FoodItemImage(
    imageUrl: String?,
    category: String?,
    contentDescription: String?,
    modifier: Modifier = Modifier,
    contentScale: ContentScale = ContentScale.Crop
) {
    val model = if (isRealImageUrl(imageUrl)) imageUrl!! else getPlaceholderFoodImageUrl(category)
    
    AsyncImage(
        model = model,
        contentDescription = contentDescription ?: "Food Item Image",
        contentScale = contentScale,
        modifier = modifier
    )
}

/**
 * Reusable image component for User Avatars (Buyer, Seller, Rider).
 * Checks for custom user-uploaded photo URL first; if absent, loads a tailored avatar.
 */
@Composable
fun ProfileAvatar(
    imageUrl: String?,
    role: String?,
    modifier: Modifier = Modifier,
    contentScale: ContentScale = ContentScale.Crop,
    contentDescription: String? = "Profile Avatar"
) {
    val model = if (isRealImageUrl(imageUrl)) imageUrl!! else getPlaceholderAvatarUrl(role)
    
    AsyncImage(
        model = model,
        contentDescription = contentDescription,
        contentScale = contentScale,
        modifier = modifier
    )
}

/**
 * Reusable image component for empty states across the application.
 */
@Composable
fun EmptyStateIllustration(
    type: String?,
    modifier: Modifier = Modifier,
    contentScale: ContentScale = ContentScale.Crop,
    contentDescription: String? = "Empty State Illustration"
) {
    val model = getPlaceholderEmptyStateUrl(type)
    AsyncImage(
        model = model,
        contentDescription = contentDescription,
        contentScale = contentScale,
        modifier = modifier
    )
}

/**
 * Reusable Price Display component.
 * Ensures strikethrough prices ONLY appear when there is an actual discount (originalPrice > price),
 * and always displays alongside the discounted price.
 */
@Composable
fun PriceDisplay(
    price: Double,
    originalPrice: Double? = null,
    modifier: Modifier = Modifier,
    priceFontSize: TextUnit = 14.sp,
    originalPriceFontSize: TextUnit = 11.sp,
    priceColor: Color = PrimaryGreen,
    fontWeight: FontWeight = FontWeight.Bold
) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        if (originalPrice != null && originalPrice > price) {
            Text(
                text = "₦${String.format("%,.0f", originalPrice)}",
                fontSize = originalPriceFontSize,
                color = TextSlate400,
                textDecoration = TextDecoration.LineThrough,
                maxLines = 1,
                softWrap = false
            )
        }
        Text(
            text = "₦${String.format("%,.0f", price)}",
            fontSize = priceFontSize,
            color = priceColor,
            fontWeight = fontWeight,
            maxLines = 1,
            softWrap = false
        )
    }
}
