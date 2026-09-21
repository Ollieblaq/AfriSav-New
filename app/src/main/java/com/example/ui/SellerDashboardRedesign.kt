package com.example.ui

import android.graphics.Bitmap
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.example.data.*
import com.example.ui.components.*
import com.example.ui.theme.*
import java.io.File
import java.io.FileOutputStream

@OptIn(ExperimentalLayoutApi::class, ExperimentalMaterial3Api::class)
@Composable
fun SellerDashboardScreen(
    viewModel: KoboViewModel,
    onLogoutClick: () -> Unit,
    onContactRider: ((EscrowOrder, String) -> Unit)? = null
) {
    val items by viewModel.marketItems.collectAsStateWithLifecycle()
    val sellerName by viewModel.currentUserName.collectAsStateWithLifecycle()
    val sellerPhone by viewModel.currentUserPhone.collectAsStateWithLifecycle()
    val sellerEmail by viewModel.currentUserEmail.collectAsStateWithLifecycle()
    val sellerLocation by viewModel.currentUserLocation.collectAsStateWithLifecycle()
    val sellerBio by viewModel.currentUserBio.collectAsStateWithLifecycle()
    val sellerPinVal by viewModel.sellerPin.collectAsStateWithLifecycle()

    // Filter items to show only ones listed by this seller (vendorName matches sellerName)
    val sellerItems = items.filter { it.vendorName.equals(sellerName, ignoreCase = true) }

    // Dynamic database streams
    val sellerEarningState by viewModel.sellerEarningState.collectAsStateWithLifecycle()
    val sellerSales by viewModel.sellerSales.collectAsStateWithLifecycle()
    val sellerWithdrawals by viewModel.sellerWithdrawals.collectAsStateWithLifecycle()
    val reviews by viewModel.allReviews.collectAsStateWithLifecycle()
    val myReviews = reviews.filter { it.targetName.equals(sellerName, ignoreCase = true) && it.isTargetSeller }
    val escrowOrdersForSeller by viewModel.escrowOrdersForSeller.collectAsStateWithLifecycle()

    val pendingEscrowBalance = escrowOrdersForSeller.filter { it.status == "PENDING" }.sumOf { it.amount }

    val earnings = sellerEarningState ?: SellerEarningState(
        vendorName = sellerName,
        availableBalance = 185000.0,
        totalRevenue = 450000.0,
        totalSalesCount = 12
    )

    // Form state for Listing New Items
    var foodName by remember { mutableStateOf("") }
    var priceStr by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf("Rice") }
    var selectedState by remember { mutableStateOf("Lagos") }
    var errorText by remember { mutableStateOf("") }
    var attachedImageUri by remember { mutableStateOf<Uri?>(null) }
    var isBundle by remember { mutableStateOf(false) }
    var bundleItems by remember { mutableStateOf("") }
    var allowPortions by remember { mutableStateOf(true) }
    var portionType by remember { mutableStateOf("Bag") }
    val context = LocalContext.current

    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let { attachedImageUri = it }
    }

    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicturePreview()
    ) { bitmap: Bitmap? ->
        bitmap?.let {
            try {
                val file = File(context.cacheDir, "seller_goods_${System.currentTimeMillis()}.jpg")
                FileOutputStream(file).use { out ->
                    bitmap.compress(Bitmap.CompressFormat.JPEG, 90, out)
                }
                attachedImageUri = Uri.fromFile(file)
            } catch (e: Exception) {
                errorText = "Failed to capture image: ${e.localizedMessage}"
            }
        }
    }

    val categories = listOf("Rice", "Beans", "Garri", "Tomatoes", "Yam", "Fish", "Bundle")
    val states = listOf("Lagos", "Ondo", "Oyo", "Kano", "Abuja")

    // Navigation selected tab (0=Listings, 1=Earnings, 2=Analytics, 3=Reviews, 4=Profile)
    var selectedTab by remember { mutableStateOf(0) }

    // Form states for Withdrawals
    var withdrawAmountStr by remember { mutableStateOf("") }
    var selectedBank by remember { mutableStateOf("Opay") }
    var withdrawAccountStr by remember { mutableStateOf("") }
    var withdrawErrorText by remember { mutableStateOf("") }
    var showSellerWithdrawConfirmDialog by remember { mutableStateOf(false) }
    var showWithdrawalCelebrationDialog by remember { mutableStateOf(false) }
    var lastWithdrawalAmount by remember { mutableStateOf(0.0) }
    var lastWithdrawalAccount by remember { mutableStateOf("") }
    var lastWithdrawalBank by remember { mutableStateOf("") }
    val banks = listOf("Opay", "Access Bank", "GTBank", "Zenith Bank", "Moniepoint", "Kuda")

    // Form states for Reviewing Buyers
    val uniqueBuyers = sellerSales.map { it.buyerName }.distinct()
    val buyerList = if (uniqueBuyers.isNotEmpty()) uniqueBuyers else listOf("Adebayo Alao", "Amina Musa", "Chidi Benson", "Fatima Yusuf")
    var selectedBuyerReview by remember { mutableStateOf(buyerList.firstOrNull() ?: "Adebayo Alao") }
    var buyerRating by remember { mutableStateOf(5) }
    var buyerReviewComment by remember { mutableStateOf("") }
    var buyerReviewSuccess by remember { mutableStateOf(false) }

    // Listing Tab Sub-View: "listings" (Active) or "add" (Add Listing)
    var listingsSubTab by remember { mutableStateOf("listings") }

    Scaffold(
        containerColor = WarmCream,
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        bottomBar = {
            Column {
                Divider(color = CardBorderLight, thickness = 1.dp)
                NavigationBar(
                    containerColor = SurfaceWhite,
                    tonalElevation = 0.dp,
                    modifier = Modifier.navigationBarsPadding()
                ) {
                    NavigationBarItem(
                        selected = selectedTab == 0,
                        onClick = { selectedTab = 0 },
                        icon = { Icon(Icons.Default.Storefront, contentDescription = "My Listings") },
                        label = {
                            Text(
                                text = "Listings",
                                style = AppTypography.label.copy(fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                            )
                        },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = SavPurple,
                            selectedTextColor = SavPurple,
                            unselectedIconColor = CharcoalMuted,
                            unselectedTextColor = CharcoalMuted,
                            indicatorColor = Purple100
                        )
                    )
                    NavigationBarItem(
                        selected = selectedTab == 1,
                        onClick = { selectedTab = 1 },
                        icon = { Icon(Icons.Default.AccountBalanceWallet, contentDescription = "Earnings") },
                        label = {
                            Text(
                                text = "Earnings",
                                style = AppTypography.label.copy(fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                            )
                        },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = SavPurple,
                            selectedTextColor = SavPurple,
                            unselectedIconColor = CharcoalMuted,
                            unselectedTextColor = CharcoalMuted,
                            indicatorColor = Purple100
                        )
                    )
                    NavigationBarItem(
                        selected = selectedTab == 2,
                        onClick = { selectedTab = 2 },
                        icon = { Icon(Icons.Default.BarChart, contentDescription = "Analytics") },
                        label = {
                            Text(
                                text = "Analytics",
                                style = AppTypography.label.copy(fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                            )
                        },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = SavPurple,
                            selectedTextColor = SavPurple,
                            unselectedIconColor = CharcoalMuted,
                            unselectedTextColor = CharcoalMuted,
                            indicatorColor = Purple100
                        )
                    )
                    NavigationBarItem(
                        selected = selectedTab == 3,
                        onClick = { selectedTab = 3 },
                        icon = { Icon(Icons.Default.RateReview, contentDescription = "Reviews") },
                        label = {
                            Text(
                                text = "Reviews",
                                style = AppTypography.label.copy(fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                            )
                        },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = SavPurple,
                            selectedTextColor = SavPurple,
                            unselectedIconColor = CharcoalMuted,
                            unselectedTextColor = CharcoalMuted,
                            indicatorColor = Purple100
                        )
                    )
                    NavigationBarItem(
                        selected = selectedTab == 4,
                        onClick = { selectedTab = 4 },
                        icon = { Icon(Icons.Default.Person, contentDescription = "Profile") },
                        label = {
                            Text(
                                text = "Profile",
                                style = AppTypography.label.copy(fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                            )
                        },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = SavPurple,
                            selectedTextColor = SavPurple,
                            unselectedIconColor = CharcoalMuted,
                            unselectedTextColor = CharcoalMuted,
                            indicatorColor = Purple100
                        )
                    )
                }
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(WarmCream)
        ) {
            // Standardized AfriSav Merchant Header (White on Warm Cream system)
            Surface(
                color = SurfaceWhite,
                border = BorderStroke(1.dp, CardBorderLight),
                modifier = Modifier.fillMaxWidth()
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .statusBarsPadding()
                        .padding(start = 16.dp, end = 16.dp, top = 12.dp, bottom = 12.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .clickable { selectedTab = 4 } // Tapping avatar navigates directly to Profile
                                .testTag("seller_avatar_profile_btn")
                                .padding(4.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(42.dp)
                                    .clip(CircleShape)
                                    .border(1.5.dp, SavPurple, CircleShape)
                            ) {
                                ProfileAvatar(
                                    imageUrl = null,
                                    role = "seller",
                                    modifier = Modifier.fillMaxSize()
                                )
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = sellerName,
                                    style = AppTypography.bodyMedium.copy(
                                        fontFamily = SoraFontFamily,
                                        fontWeight = FontWeight.Bold,
                                        color = Charcoal
                                    )
                                )
                                Text(
                                    text = "Merchant • $sellerPhone",
                                    style = AppTypography.caption.copy(color = CharcoalSecondary)
                                )
                            }
                        }
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            val isDarkMode by viewModel.isDarkMode.collectAsStateWithLifecycle()
                            ThemeToggle(
                                isDarkMode = isDarkMode,
                                onToggle = { viewModel.toggleDarkMode() }
                            )
                            IconButton(
                                onClick = onLogoutClick,
                                modifier = Modifier
                                    .background(Purple50, CircleShape)
                                    .border(1.dp, CardBorderLight, CircleShape)
                            ) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.Logout,
                                    contentDescription = "Logout",
                                    tint = SavPurple
                                )
                            }
                        }
                    }
                }
            }

            // Screen Content Router (Each screen strictly adheres to White on Warm Cream surface rules)
            when (selectedTab) {
                0 -> {
                    // =========================================================================
                    // --- SCREEN 0: MY PRODUCTS / LISTINGS TAB ---
                    // Surface: White on Warm Cream, Sav Purple actions
                    // =========================================================================
                    Column(modifier = Modifier.fillMaxSize()) {
                        // Sub-Tab Switcher
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 12.dp)
                                .background(SurfaceWhite, RoundedCornerShape(12.dp))
                                .border(1.dp, CardBorderLight, RoundedCornerShape(12.dp))
                                .padding(4.dp),
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .height(40.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(if (listingsSubTab == "listings") SavPurple else Color.Transparent)
                                    .clickable { listingsSubTab = "listings" }
                                    .testTag("seller_tab_listings_list"),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "My inventory (${sellerItems.size})",
                                    style = AppTypography.button.copy(
                                        fontSize = 12.sp,
                                        color = if (listingsSubTab == "listings") Color.White else CharcoalSecondary
                                    )
                                )
                            }
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .height(40.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(if (listingsSubTab == "add") SavPurple else Color.Transparent)
                                    .clickable { listingsSubTab = "add" }
                                    .testTag("seller_tab_listings_add"),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "+ Add product",
                                    style = AppTypography.button.copy(
                                        fontSize = 12.sp,
                                        color = if (listingsSubTab == "add") Color.White else CharcoalSecondary
                                    )
                                )
                            }
                        }

                        if (listingsSubTab == "listings") {
                            if (sellerItems.isEmpty()) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .padding(16.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    EmptyStateCard(
                                        message = "Your store is empty",
                                        subMessage = "Get started by adding high-quality local foodstuffs to your Soko digital storefront.",
                                        icon = Icons.Default.Storefront,
                                        iconColor = SavPurple,
                                        ground = SurfaceGround.LIGHT
                                    )
                                }
                            } else {
                                LazyColumn(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .padding(horizontal = 16.dp),
                                    verticalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    item {
                                        Text(
                                            text = "Active Listed Products",
                                            style = AppTypography.h3.copy(color = Charcoal),
                                            modifier = Modifier.padding(vertical = 4.dp)
                                        )
                                    }
                                    items(sellerItems) { item ->
                                        Card(
                                            colors = CardDefaults.cardColors(containerColor = SurfaceWhite),
                                            shape = AppShapes.card,
                                            border = BorderStroke(1.dp, CardBorderLight)
                                        ) {
                                            Row(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .padding(16.dp),
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Box(
                                                    modifier = Modifier
                                                        .size(52.dp)
                                                        .clip(RoundedCornerShape(12.dp))
                                                        .background(Purple50),
                                                    contentAlignment = Alignment.Center
                                                ) {
                                                    FoodItemImage(
                                                        imageUrl = item.imageUrl,
                                                        category = item.category,
                                                        contentDescription = item.name,
                                                        modifier = Modifier.fillMaxSize()
                                                    )
                                                }
                                                Spacer(modifier = Modifier.width(12.dp))
                                                Column(modifier = Modifier.weight(1f)) {
                                                    Text(
                                                        text = item.name,
                                                        style = AppTypography.bodyLarge.copy(
                                                            fontWeight = FontWeight.Bold,
                                                            color = Charcoal
                                                        )
                                                    )
                                                    Spacer(modifier = Modifier.height(4.dp))
                                                    Row(
                                                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                                                        verticalAlignment = Alignment.CenterVertically
                                                    ) {
                                                        AfriSavBadge(
                                                            text = item.category,
                                                            type = AfriSavBadgeType.INFO,
                                                            ground = SurfaceGround.LIGHT
                                                        )
                                                        AfriSavBadge(
                                                            text = "From ${item.state}",
                                                            type = AfriSavBadgeType.NEUTRAL,
                                                            ground = SurfaceGround.LIGHT
                                                        )
                                                    }
                                                    if (item.allowPortions) {
                                                        Text(
                                                            text = "Portions setup: ${item.portionType}",
                                                            style = AppTypography.caption.copy(color = CharcoalMuted),
                                                            modifier = Modifier.padding(top = 4.dp)
                                                        )
                                                    }
                                                }
                                                Text(
                                                    text = AppFormatters.formatNaira(item.price),
                                                    style = AppTypography.figure.copy(
                                                        fontSize = 17.sp,
                                                        color = SavPurple
                                                    )
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        } else {
                            // Sub-View: Add Product
                            Column(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .verticalScroll(rememberScrollState())
                                    .padding(16.dp),
                                verticalArrangement = Arrangement.spacedBy(16.dp)
                            ) {
                                Card(
                                    colors = CardDefaults.cardColors(containerColor = SurfaceWhite),
                                    shape = AppShapes.card,
                                    border = BorderStroke(1.dp, CardBorderLight)
                                ) {
                                    Column(
                                        modifier = Modifier.padding(18.dp),
                                        verticalArrangement = Arrangement.spacedBy(14.dp)
                                    ) {
                                        Text(
                                            text = "NEW LISTING DETAILS",
                                            style = AppTypography.label.copy(color = CharcoalMuted, letterSpacing = 1.sp)
                                        )

                                        SellerInputField(
                                            value = foodName,
                                            onValueChange = { foodName = it },
                                            label = "Foodstuff Name",
                                            placeholder = "e.g. Clean White Garri (1 Bag)",
                                            singleLine = true,
                                            modifier = Modifier.testTag("seller_add_item_name")
                                        )

                                        SellerInputField(
                                            value = priceStr,
                                            onValueChange = { priceStr = it },
                                            label = "Price (₦)",
                                            placeholder = "e.g. 24000",
                                            singleLine = true,
                                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                            modifier = Modifier.testTag("seller_add_item_price")
                                        )

                                        Column {
                                            Text(
                                                text = "Select Foodstuff Category",
                                                style = AppTypography.label.copy(fontWeight = FontWeight.Bold, color = CharcoalSecondary),
                                                modifier = Modifier.padding(bottom = 6.dp)
                                            )
                                            LazyRow(
                                                horizontalArrangement = Arrangement.spacedBy(6.dp),
                                                modifier = Modifier.fillMaxWidth()
                                            ) {
                                                items(categories) { cat ->
                                                    val isSelected = selectedCategory == cat
                                                    Box(
                                                        modifier = Modifier
                                                            .clip(RoundedCornerShape(8.dp))
                                                            .background(if (isSelected) SavPurple else SurfaceWhite)
                                                            .border(1.dp, if (isSelected) SavPurple else CardBorderLight, RoundedCornerShape(8.dp))
                                                            .clickable { selectedCategory = cat }
                                                            .padding(horizontal = 12.dp, vertical = 8.dp)
                                                    ) {
                                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                                            Text(getCategoryEmoji(cat), fontSize = 14.sp)
                                                            Spacer(modifier = Modifier.width(4.dp))
                                                            Text(
                                                                text = cat,
                                                                style = AppTypography.bodySmall.copy(
                                                                    fontWeight = FontWeight.SemiBold,
                                                                    color = if (isSelected) Color.White else CharcoalSecondary
                                                                )
                                                            )
                                                        }
                                                    }
                                                }
                                            }
                                        }

                                        Column {
                                            Text(
                                                text = "Sourcing State Origin",
                                                style = AppTypography.label.copy(fontWeight = FontWeight.Bold, color = CharcoalSecondary),
                                                modifier = Modifier.padding(bottom = 6.dp)
                                            )
                                            LazyRow(
                                                horizontalArrangement = Arrangement.spacedBy(6.dp),
                                                modifier = Modifier.fillMaxWidth()
                                            ) {
                                                items(states) { st ->
                                                    val isSelected = selectedState == st
                                                    Box(
                                                        modifier = Modifier
                                                            .clip(RoundedCornerShape(8.dp))
                                                            .background(if (isSelected) SavPurple else SurfaceWhite)
                                                            .border(1.dp, if (isSelected) SavPurple else CardBorderLight, RoundedCornerShape(8.dp))
                                                            .clickable { selectedState = st }
                                                            .padding(horizontal = 12.dp, vertical = 8.dp)
                                                    ) {
                                                        Text(
                                                            text = st,
                                                            style = AppTypography.bodySmall.copy(
                                                                fontWeight = FontWeight.SemiBold,
                                                                color = if (isSelected) Color.White else CharcoalSecondary
                                                            )
                                                        )
                                                    }
                                                }
                                            }
                                        }

                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.SpaceBetween
                                        ) {
                                            Column(modifier = Modifier.weight(1f)) {
                                                Text("Is this a food bundle?", style = AppTypography.bodyMedium.copy(fontWeight = FontWeight.Bold, color = Charcoal))
                                                Text("Group multiple foodstuffs into one single sale item", style = AppTypography.caption.copy(color = CharcoalMuted))
                                            }
                                            Spacer(modifier = Modifier.width(8.dp))
                                            Switch(
                                                checked = isBundle,
                                                onCheckedChange = { isBundle = it },
                                                colors = SwitchDefaults.colors(
                                                    checkedThumbColor = Color.White,
                                                    checkedTrackColor = SavPurple,
                                                    uncheckedTrackColor = BorderSubtle
                                                )
                                            )
                                        }

                                        if (isBundle) {
                                            SellerInputField(
                                                value = bundleItems,
                                                onValueChange = { bundleItems = it },
                                                label = "List items in bundle (comma separated)",
                                                placeholder = "e.g. 1 Tub of Yam, 1 Bottle of Palm Oil"
                                            )
                                        }

                                        Divider(color = CardBorderLight)

                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.SpaceBetween
                                        ) {
                                            Column(modifier = Modifier.weight(1f)) {
                                                Text("Allow portion breakdown?", style = AppTypography.bodyMedium.copy(fontWeight = FontWeight.Bold, color = Charcoal))
                                                Text("Let buyers buy in portions or smaller units", style = AppTypography.caption.copy(color = CharcoalMuted))
                                            }
                                            Spacer(modifier = Modifier.width(8.dp))
                                            Switch(
                                                checked = allowPortions,
                                                onCheckedChange = { allowPortions = it },
                                                colors = SwitchDefaults.colors(
                                                    checkedThumbColor = Color.White,
                                                    checkedTrackColor = SavPurple,
                                                    uncheckedTrackColor = BorderSubtle
                                                )
                                            )
                                        }

                                        if (allowPortions) {
                                            Column {
                                                Text(
                                                    text = "Select Portion Breakdown Type",
                                                    style = AppTypography.label.copy(fontWeight = FontWeight.Bold, color = CharcoalSecondary),
                                                    modifier = Modifier.padding(bottom = 6.dp)
                                                )
                                                Row(
                                                    modifier = Modifier.fillMaxWidth(),
                                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                                ) {
                                                    listOf("Bag", "Mudu", "Paint Bucket", "Tuber").forEach { pType ->
                                                        val isSelected = portionType == pType
                                                        Box(
                                                            modifier = Modifier
                                                                .weight(1f)
                                                                .clip(RoundedCornerShape(8.dp))
                                                                .background(if (isSelected) Purple100 else SurfaceWhite)
                                                                .border(1.dp, if (isSelected) SavPurple else CardBorderLight, RoundedCornerShape(8.dp))
                                                                .clickable { portionType = pType }
                                                                .padding(vertical = 10.dp),
                                                            contentAlignment = Alignment.Center
                                                        ) {
                                                            Text(
                                                                text = pType,
                                                                style = AppTypography.bodySmall.copy(
                                                                    fontWeight = FontWeight.Bold,
                                                                    color = if (isSelected) SavPurple else CharcoalSecondary
                                                                )
                                                            )
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }

                                Card(
                                    colors = CardDefaults.cardColors(containerColor = SurfaceWhite),
                                    shape = AppShapes.card,
                                    border = BorderStroke(1.dp, CardBorderLight)
                                ) {
                                    Column(modifier = Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                                        Text("FOODSTUFF IMAGE (OPTIONAL)", style = AppTypography.label.copy(color = CharcoalMuted, letterSpacing = 1.sp))

                                        if (attachedImageUri != null) {
                                            Box(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .height(140.dp)
                                                    .clip(RoundedCornerShape(12.dp))
                                                    .background(Purple50)
                                            ) {
                                                AsyncImage(
                                                    model = attachedImageUri,
                                                    contentDescription = "Food Image",
                                                    contentScale = ContentScale.Crop,
                                                    modifier = Modifier.fillMaxSize()
                                                )
                                                IconButton(
                                                    onClick = { attachedImageUri = null },
                                                    modifier = Modifier
                                                        .align(Alignment.TopEnd)
                                                        .padding(8.dp)
                                                        .background(Color.Black.copy(alpha = 0.5f), CircleShape)
                                                ) {
                                                    Icon(Icons.Default.Close, contentDescription = "Remove", tint = Color.White, modifier = Modifier.size(16.dp))
                                                }
                                            }
                                        } else {
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                                            ) {
                                                OutlinedButton(
                                                    onClick = { galleryLauncher.launch("image/*") },
                                                    modifier = Modifier.weight(1f).heightIn(min = 48.dp),
                                                    colors = ButtonDefaults.outlinedButtonColors(contentColor = SavPurple),
                                                    border = BorderStroke(1.5.dp, SavPurple),
                                                    shape = AppShapes.button
                                                ) {
                                                    Icon(Icons.Default.Photo, contentDescription = null, modifier = Modifier.size(16.dp))
                                                    Spacer(modifier = Modifier.width(6.dp))
                                                    Text("Gallery", style = AppTypography.button.copy(color = SavPurple))
                                                }
                                                OutlinedButton(
                                                    onClick = { cameraLauncher.launch(null) },
                                                    modifier = Modifier.weight(1f).heightIn(min = 48.dp),
                                                    colors = ButtonDefaults.outlinedButtonColors(contentColor = SavPurple),
                                                    border = BorderStroke(1.5.dp, SavPurple),
                                                    shape = AppShapes.button
                                                ) {
                                                    Icon(Icons.Default.CameraAlt, contentDescription = null, modifier = Modifier.size(16.dp))
                                                    Spacer(modifier = Modifier.width(6.dp))
                                                    Text("Camera", style = AppTypography.button.copy(color = SavPurple))
                                                }
                                            }
                                        }
                                    }
                                }

                                if (errorText.isNotEmpty()) {
                                    Text(
                                        text = errorText,
                                        color = SemanticErrorLight,
                                        style = AppTypography.caption.copy(fontWeight = FontWeight.SemiBold),
                                        modifier = Modifier.padding(horizontal = 4.dp)
                                    )
                                }

                                AfriSavPrimaryButton(
                                    text = "Publish to Soko market",
                                    onClick = {
                                        val priceVal = priceStr.toDoubleOrNull()
                                        if (foodName.isBlank()) {
                                            errorText = "Please enter foodstuff name"
                                        } else if (priceVal == null || priceVal <= 0.0) {
                                            errorText = "Please enter a valid price"
                                        } else {
                                            viewModel.addMarketItem(
                                                name = foodName.trim(),
                                                price = priceVal,
                                                category = selectedCategory,
                                                vendorName = sellerName,
                                                state = selectedState,
                                                imageUrl = attachedImageUri?.toString() ?: "ic_food_custom",
                                                isBundle = isBundle,
                                                bundleItems = if (isBundle) bundleItems.trim() else "",
                                                allowPortions = allowPortions,
                                                portionType = if (allowPortions) portionType else ""
                                            )
                                            foodName = ""
                                            priceStr = ""
                                            selectedCategory = "Rice"
                                            selectedState = "Lagos"
                                            isBundle = false
                                            bundleItems = ""
                                            allowPortions = true
                                            portionType = "Bag"
                                            attachedImageUri = null
                                            errorText = ""
                                            listingsSubTab = "listings"
                                            Toast.makeText(context, "New listing published successfully!", Toast.LENGTH_SHORT).show()
                                        }
                                    },
                                    ground = SurfaceGround.LIGHT,
                                    modifier = Modifier.testTag("seller_add_item_btn")
                                )
                            }
                        }
                    }
                }
                1 -> {
                    // =========================================================================
                    // --- SCREEN 1: EARNINGS & PAY TAB ---
                    // Surface: White on Warm Cream, Sav Purple actions with White labels
                    // =========================================================================
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        val sellBal = earnings.availableBalance
                        if (sellBal <= 2000.0) {
                            item {
                                Card(
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = CardDefaults.cardColors(
                                        containerColor = if (sellBal <= 0.0) Color(0xFFFEF2F2) else Color(0xFFFFFBEB)
                                    ),
                                    border = BorderStroke(
                                        1.dp,
                                        if (sellBal <= 0.0) Color(0xFFFEE2E2) else Color(0xFFFEF3C7)
                                    ),
                                    shape = AppShapes.card
                                ) {
                                    Row(
                                        modifier = Modifier.padding(16.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Warning,
                                            contentDescription = "Warning",
                                            tint = if (sellBal <= 0.0) SemanticErrorLight else SemanticWarningLight,
                                            modifier = Modifier.size(22.dp)
                                        )
                                        Spacer(modifier = Modifier.width(12.dp))
                                        Column {
                                            Text(
                                                text = if (sellBal <= 0.0) "No Earnings Available" else "Low Balance Warning",
                                                style = AppTypography.bodySmall.copy(
                                                    fontWeight = FontWeight.Bold,
                                                    color = if (sellBal <= 0.0) SemanticErrorLight else SemanticWarningLight
                                                )
                                            )
                                            Text(
                                                text = if (sellBal <= 0.0) "Once buyers confirm receipt, your escrow funds will be released." else "Your balance is low. Withdrawals require at least ₦1,000.",
                                                style = AppTypography.caption.copy(
                                                    color = if (sellBal <= 0.0) SemanticErrorLight.copy(alpha = 0.9f) else SemanticWarningLight.copy(alpha = 0.9f)
                                                )
                                            )
                                        }
                                    }
                                }
                            }
                        }

                        // Merchant Coffer Summary Card (White on Warm Cream)
                        item {
                            Card(
                                colors = CardDefaults.cardColors(containerColor = SurfaceWhite),
                                shape = AppShapes.card,
                                border = BorderStroke(1.dp, CardBorderLight)
                            ) {
                                Column(modifier = Modifier.padding(20.dp)) {
                                    Text(
                                        text = "MERCHANT COFFER SUMMARY",
                                        style = AppTypography.label.copy(color = CharcoalMuted, letterSpacing = 1.sp)
                                    )
                                    Spacer(modifier = Modifier.height(16.dp))

                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Column {
                                            Text("Available balance", style = AppTypography.caption.copy(color = CharcoalSecondary))
                                            Text(
                                                text = AppFormatters.formatNaira(earnings.availableBalance),
                                                style = AppTypography.figureLarge.copy(
                                                    fontSize = 26.sp,
                                                    color = SavPurple
                                                )
                                            )
                                            Spacer(modifier = Modifier.height(8.dp))
                                            Text("Escrow account (pending release)", style = AppTypography.caption.copy(color = CharcoalSecondary))
                                            Row(
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                                            ) {
                                                Text(
                                                    text = AppFormatters.formatNaira(pendingEscrowBalance),
                                                    style = AppTypography.figureMedium.copy(
                                                        fontSize = 18.sp,
                                                        color = Charcoal
                                                    )
                                                )
                                                AfriSavBadge(
                                                    text = "In escrow",
                                                    type = AfriSavBadgeType.INFO,
                                                    ground = SurfaceGround.LIGHT
                                                )
                                            }
                                        }
                                        Button(
                                            onClick = { showSellerWithdrawConfirmDialog = true },
                                            colors = ButtonDefaults.buttonColors(
                                                containerColor = SavPurple,
                                                contentColor = Color.White
                                            ),
                                            shape = AppShapes.button,
                                            contentPadding = PaddingValues(horizontal = 14.dp, vertical = 10.dp),
                                            modifier = Modifier.testTag("seller_withdraw_open_btn")
                                        ) {
                                            Icon(Icons.Default.AccountBalanceWallet, contentDescription = null, modifier = Modifier.size(16.dp))
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Text("Withdraw", style = AppTypography.button.copy(color = Color.White))
                                        }
                                    }
                                }
                            }
                        }

                        // Secondary Stats Row
                        item {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                Card(
                                    colors = CardDefaults.cardColors(containerColor = SurfaceWhite),
                                    shape = AppShapes.card,
                                    border = BorderStroke(1.dp, CardBorderLight),
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Column(modifier = Modifier.padding(16.dp)) {
                                        Text("Total revenue", style = AppTypography.caption.copy(color = CharcoalMuted))
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text(
                                            text = AppFormatters.formatNaira(earnings.totalRevenue),
                                            style = AppTypography.figureMedium.copy(
                                                fontSize = 17.sp,
                                                color = Charcoal
                                            )
                                        )
                                    }
                                }
                                Card(
                                    colors = CardDefaults.cardColors(containerColor = SurfaceWhite),
                                    shape = AppShapes.card,
                                    border = BorderStroke(1.dp, CardBorderLight),
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Column(modifier = Modifier.padding(16.dp)) {
                                        Text("Successful sales", style = AppTypography.caption.copy(color = CharcoalMuted))
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text(
                                            text = "${earnings.totalSalesCount} orders",
                                            style = AppTypography.figureMedium.copy(
                                                fontSize = 17.sp,
                                                color = Charcoal
                                            )
                                        )
                                    }
                                }
                            }
                        }

                        // Escrow Orders list
                        val escrowOrders = escrowOrdersForSeller.filter { it.status == "PENDING" }
                        if (escrowOrders.isNotEmpty()) {
                            item {
                                Text(
                                    text = "Active Escrow Orders (${escrowOrders.size})",
                                    style = AppTypography.h3.copy(color = Charcoal),
                                    modifier = Modifier.padding(top = 8.dp)
                                )
                            }
                            items(escrowOrders) { esc ->
                                Card(
                                    colors = CardDefaults.cardColors(containerColor = SurfaceWhite),
                                    shape = AppShapes.card,
                                    border = BorderStroke(1.dp, CardBorderLight)
                                ) {
                                    Column(modifier = Modifier.padding(16.dp)) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Column {
                                                Text(esc.itemName, style = AppTypography.bodyMedium.copy(fontWeight = FontWeight.Bold, color = Charcoal))
                                                Text("Buyer: ${esc.buyerName}", style = AppTypography.caption.copy(color = CharcoalSecondary))
                                            }
                                            Text(
                                                text = AppFormatters.formatNaira(esc.amount),
                                                style = AppTypography.figureSmall.copy(
                                                    fontSize = 15.sp,
                                                    color = SavPurple
                                                )
                                            )
                                        }

                                        if (esc.pickupPin.isNotEmpty()) {
                                            Spacer(modifier = Modifier.height(10.dp))
                                            Box(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .background(Purple50, RoundedCornerShape(10.dp))
                                                    .border(1.dp, CardBorderLight, RoundedCornerShape(10.dp))
                                                    .padding(10.dp)
                                            ) {
                                                Row(
                                                    modifier = Modifier.fillMaxWidth(),
                                                    horizontalArrangement = Arrangement.SpaceBetween,
                                                    verticalAlignment = Alignment.CenterVertically
                                                ) {
                                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                                        Icon(
                                                            imageVector = Icons.Default.Lock,
                                                            contentDescription = null,
                                                            tint = SavPurple,
                                                            modifier = Modifier.size(16.dp)
                                                        )
                                                        Spacer(modifier = Modifier.width(8.dp))
                                                        Column {
                                                            Text("SECURE PICKUP PIN", style = AppTypography.labelSmall.copy(fontWeight = FontWeight.Bold, color = SavPurple))
                                                            Text("Give this to the rider upon pickup", style = AppTypography.caption.copy(color = CharcoalMuted))
                                                        }
                                                    }
                                                    Text(
                                                        text = esc.pickupPin,
                                                        style = AppTypography.figureSmall.copy(
                                                            fontSize = 15.sp,
                                                            color = SavPurple
                                                        ),
                                                        modifier = Modifier
                                                            .background(SurfaceWhite, RoundedCornerShape(6.dp))
                                                            .border(1.dp, CardBorderLight, RoundedCornerShape(6.dp))
                                                            .padding(horizontal = 10.dp, vertical = 4.dp)
                                                    )
                                                }
                                            }
                                        }

                                        if (!esc.riderName.isNullOrEmpty()) {
                                            Spacer(modifier = Modifier.height(12.dp))
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.SpaceBetween,
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Text("Rider: ${esc.riderName}", style = AppTypography.caption.copy(fontWeight = FontWeight.Bold, color = Charcoal))
                                                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                                    OutlinedButton(
                                                        onClick = { onContactRider?.invoke(esc, "CALL") },
                                                        modifier = Modifier.heightIn(min = 34.dp),
                                                        colors = ButtonDefaults.outlinedButtonColors(contentColor = SavPurple),
                                                        border = BorderStroke(1.dp, SavPurple),
                                                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                                                        shape = AppShapes.button
                                                    ) {
                                                        Text("Call Rider", style = AppTypography.button.copy(fontSize = 11.sp, color = SavPurple))
                                                    }
                                                    OutlinedButton(
                                                        onClick = { onContactRider?.invoke(esc, "CHAT") },
                                                        modifier = Modifier.heightIn(min = 34.dp),
                                                        colors = ButtonDefaults.outlinedButtonColors(contentColor = SavPurple),
                                                        border = BorderStroke(1.dp, SavPurple),
                                                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                                                        shape = AppShapes.button
                                                    ) {
                                                        Text("Chat Rider", style = AppTypography.button.copy(fontSize = 11.sp, color = SavPurple))
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }

                        // Settlement Withdrawal Logs
                        item {
                            Text(
                                text = "Settlement Withdrawal Logs",
                                style = AppTypography.h3.copy(color = Charcoal),
                                modifier = Modifier.padding(top = 8.dp)
                            )
                        }

                        if (sellerWithdrawals.isEmpty()) {
                            item {
                                EmptyStateCard(
                                    message = "No settlement logs yet",
                                    subMessage = "Your bank transfer withdrawals will appear here chronologically.",
                                    icon = Icons.Default.History,
                                    iconColor = CharcoalMuted,
                                    ground = SurfaceGround.LIGHT
                                )
                            }
                        } else {
                            items(sellerWithdrawals) { w ->
                                Card(
                                    colors = CardDefaults.cardColors(containerColor = SurfaceWhite),
                                    shape = RoundedCornerShape(14.dp),
                                    border = BorderStroke(1.dp, CardBorderLight)
                                ) {
                                    Row(
                                        modifier = Modifier.padding(14.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(38.dp)
                                                .clip(CircleShape)
                                                .background(Color(0xFFFEF3C7)),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.TrendingDown,
                                                contentDescription = "Withdrawal",
                                                tint = SemanticWarningLight,
                                                modifier = Modifier.size(18.dp)
                                            )
                                        }
                                        Spacer(modifier = Modifier.width(12.dp))
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(
                                                text = "Withdrawal to ${w.bankName}",
                                                style = AppTypography.bodyMedium.copy(fontWeight = FontWeight.Bold, color = Charcoal)
                                            )
                                            Row(
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                                            ) {
                                                Text("Acc: ${w.accountNumber}", style = AppTypography.caption.copy(color = CharcoalMuted))
                                                AfriSavBadge(
                                                    text = "Processing",
                                                    type = AfriSavBadgeType.WARNING,
                                                    ground = SurfaceGround.LIGHT
                                                )
                                            }
                                        }
                                        Text(
                                            text = "-${AppFormatters.formatNaira(w.amount)}",
                                            style = AppTypography.figureSmall.copy(
                                                fontSize = 15.sp,
                                                color = Charcoal
                                            )
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
                2 -> {
                    // =========================================================================
                    // --- SCREEN 2: MONTHLY ANALYTICS TAB ---
                    // Surface: White on Warm Cream, Sav Purple actions with White labels
                    // =========================================================================
                    val months = listOf("January", "February", "March", "April", "May", "June", "July", "August", "September", "October", "November", "December")
                    var selectedMonth by remember { mutableStateOf("January") }

                    val monthSales = sellerSales.filter { it.month.equals(selectedMonth, ignoreCase = true) }
                    val groupedSales = monthSales.groupBy { it.itemName }

                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        item {
                            Text(
                                text = "Select Month Filter",
                                style = AppTypography.h3.copy(color = Charcoal)
                            )
                            Spacer(modifier = Modifier.height(8.dp))

                            LazyRow(
                                horizontalArrangement = Arrangement.spacedBy(6.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                items(months) { m ->
                                    val count = sellerSales.count { it.month.equals(m, ignoreCase = true) }
                                    val isSelected = selectedMonth == m
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(if (isSelected) SavPurple else SurfaceWhite)
                                            .border(1.dp, if (isSelected) SavPurple else CardBorderLight, RoundedCornerShape(8.dp))
                                            .clickable { selectedMonth = m }
                                            .padding(horizontal = 12.dp, vertical = 7.dp)
                                    ) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Text(
                                                text = m,
                                                style = AppTypography.bodySmall.copy(
                                                    fontWeight = FontWeight.SemiBold,
                                                    color = if (isSelected) Color.White else CharcoalSecondary
                                                )
                                            )
                                            if (count > 0) {
                                                Spacer(modifier = Modifier.width(6.dp))
                                                Box(
                                                    modifier = Modifier
                                                        .background(if (isSelected) HarvestLime else Purple100, CircleShape)
                                                        .padding(horizontal = 6.dp, vertical = 2.dp)
                                                ) {
                                                    Text(
                                                        text = "$count",
                                                        style = AppTypography.labelSmall.copy(
                                                            fontSize = 10.sp,
                                                            fontWeight = FontWeight.Bold,
                                                            color = if (isSelected) Charcoal else SavPurple
                                                        )
                                                    )
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }

                        // Sales Volume Analysis Card (White on Warm Cream)
                        item {
                            Card(
                                colors = CardDefaults.cardColors(containerColor = SurfaceWhite),
                                shape = AppShapes.card,
                                border = BorderStroke(1.dp, CardBorderLight)
                            ) {
                                Column(modifier = Modifier.padding(18.dp)) {
                                    Text(
                                        text = "SALES VOLUME ANALYSIS: ${selectedMonth.uppercase()}",
                                        style = AppTypography.label.copy(color = CharcoalMuted, letterSpacing = 1.sp)
                                    )
                                    Spacer(modifier = Modifier.height(16.dp))

                                    if (groupedSales.isEmpty()) {
                                        Box(
                                            modifier = Modifier.fillMaxWidth().padding(vertical = 24.dp),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                                Icon(Icons.Default.BarChart, contentDescription = null, tint = CharcoalMuted, modifier = Modifier.size(34.dp))
                                                Spacer(modifier = Modifier.height(6.dp))
                                                Text("No sales registered for this month", style = AppTypography.caption.copy(color = CharcoalMuted))
                                            }
                                        }
                                    } else {
                                        val maxCount = groupedSales.values.maxOfOrNull { it.size } ?: 1
                                        groupedSales.forEach { (itemName, salesList) ->
                                            val totalAmt = salesList.sumOf { it.amount }
                                            val count = salesList.size
                                            val fraction = count.toFloat() / maxCount.toFloat()
                                            val firstSale = salesList.first()

                                            Column(modifier = Modifier.padding(vertical = 8.dp)) {
                                                Row(
                                                    modifier = Modifier.fillMaxWidth(),
                                                    verticalAlignment = Alignment.CenterVertically
                                                ) {
                                                    Box(
                                                        modifier = Modifier
                                                            .size(34.dp)
                                                            .clip(RoundedCornerShape(8.dp))
                                                            .background(Purple50),
                                                        contentAlignment = Alignment.Center
                                                    ) {
                                                        Text(getCategoryEmoji(firstSale.category), fontSize = 16.sp)
                                                    }
                                                    Spacer(modifier = Modifier.width(10.dp))
                                                    Column(modifier = Modifier.weight(1f)) {
                                                        Text(itemName, style = AppTypography.bodyMedium.copy(fontWeight = FontWeight.Bold, color = Charcoal))
                                                        Text(
                                                            text = "Total: ${AppFormatters.formatNaira(totalAmt)}",
                                                            style = AppTypography.caption.copy(color = CharcoalSecondary)
                                                        )
                                                    }
                                                    Text(
                                                        text = "$count sold",
                                                        style = AppTypography.small.copy(fontWeight = FontWeight.Bold, color = SavPurple)
                                                    )
                                                }
                                                Spacer(modifier = Modifier.height(6.dp))
                                                // Standardized progress bar
                                                Box(
                                                    modifier = Modifier
                                                        .fillMaxWidth()
                                                        .height(8.dp)
                                                        .clip(RoundedCornerShape(4.dp))
                                                        .background(Purple50)
                                                ) {
                                                    Box(
                                                        modifier = Modifier
                                                            .fillMaxHeight()
                                                            .fillMaxWidth(fraction)
                                                            .background(SavPurple)
                                                    )
                                                }
                                            }
                                            Divider(color = CardBorderLight.copy(alpha = 0.5f))
                                        }
                                    }
                                }
                            }
                        }

                        // Combined Chronological Trade Logs with alternating shading
                        item {
                            Text(
                                text = "Chronological Transaction History",
                                style = AppTypography.h3.copy(color = Charcoal),
                                modifier = Modifier.padding(top = 8.dp)
                            )
                        }

                        val combinedHistory = (sellerSales.map {
                            HistoryItem(
                                isSale = true,
                                title = "Sale: ${it.itemName}",
                                subtitle = "Buyer: ${it.buyerName} • ${it.month}",
                                amount = it.amount
                            )
                        } + sellerWithdrawals.map {
                            HistoryItem(
                                isSale = false,
                                title = "Withdrawal to ${it.bankName}",
                                subtitle = "Acc: ${it.accountNumber} • Settlement",
                                amount = it.amount
                            )
                        })

                        if (combinedHistory.isEmpty()) {
                            item {
                                EmptyStateCard(
                                    message = "No trade logs registered",
                                    subMessage = "Sales and bank withdrawals will show up here chronologically.",
                                    icon = Icons.Default.History,
                                    iconColor = CharcoalMuted,
                                    ground = SurfaceGround.LIGHT
                                )
                            }
                        } else {
                            item {
                                Card(
                                    colors = CardDefaults.cardColors(containerColor = SurfaceWhite),
                                    shape = AppShapes.card,
                                    border = BorderStroke(1.dp, CardBorderLight)
                                ) {
                                    Column {
                                        // Header Row
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .background(WarmCream)
                                                .padding(horizontal = 14.dp, vertical = 10.dp),
                                            horizontalArrangement = Arrangement.SpaceBetween
                                        ) {
                                            Text("TRANSACTION DETAILS", style = AppTypography.label.copy(color = CharcoalMuted), modifier = Modifier.weight(1f))
                                            Text("AMOUNT", style = AppTypography.label.copy(color = CharcoalMuted), textAlign = TextAlign.End)
                                        }

                                        // Alternating data rows
                                        combinedHistory.forEachIndexed { idx, history ->
                                            val rowBg = if (idx % 2 == 0) SurfaceWhite else WarmCream.copy(alpha = 0.5f)
                                            Row(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .background(rowBg)
                                                    .padding(horizontal = 14.dp, vertical = 12.dp),
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Box(
                                                    modifier = Modifier
                                                        .size(32.dp)
                                                        .clip(CircleShape)
                                                        .background(if (history.isSale) Color(0xFFDCFCE7) else Color(0xFFFEF3C7)),
                                                    contentAlignment = Alignment.Center
                                                ) {
                                                    Icon(
                                                        imageVector = if (history.isSale) Icons.Default.TrendingUp else Icons.Default.TrendingDown,
                                                        contentDescription = if (history.isSale) "Sale" else "Withdrawal",
                                                        tint = if (history.isSale) SemanticSuccessLight else SemanticWarningLight,
                                                        modifier = Modifier.size(16.dp)
                                                    )
                                                }
                                                Spacer(modifier = Modifier.width(10.dp))
                                                Column(modifier = Modifier.weight(1f)) {
                                                    Row(
                                                        verticalAlignment = Alignment.CenterVertically,
                                                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                                                    ) {
                                                        Text(history.title, style = AppTypography.bodySmall.copy(fontWeight = FontWeight.Bold, color = Charcoal))
                                                        AfriSavBadge(
                                                            text = if (history.isSale) "Sale" else "Withdrawal",
                                                            type = if (history.isSale) AfriSavBadgeType.SUCCESS else AfriSavBadgeType.WARNING,
                                                            ground = SurfaceGround.LIGHT
                                                        )
                                                    }
                                                    Text(history.subtitle, style = AppTypography.caption.copy(color = CharcoalMuted))
                                                }
                                                Text(
                                                    text = "${if (history.isSale) "+" else "-"}${AppFormatters.formatNaira(history.amount)}",
                                                    style = AppTypography.figureSmall.copy(
                                                        fontSize = 14.sp,
                                                        color = if (history.isSale) SemanticSuccessLight else Charcoal
                                                    ),
                                                    textAlign = TextAlign.End
                                                )
                                            }
                                            Divider(color = CardBorderLight.copy(alpha = 0.5f))
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
                3 -> {
                    // =========================================================================
                    // --- SCREEN 3: BUYER REVIEWS TAB ---
                    // Surface: White on Warm Cream, Sav Purple actions
                    // =========================================================================
                    val buyersReviewedByMe = reviews.filter { it.reviewerName.equals(sellerName, ignoreCase = true) && !it.isTargetSeller }

                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        // Section 1: Confidential Feedback Card
                        item {
                            Card(
                                colors = CardDefaults.cardColors(containerColor = SurfaceWhite),
                                shape = AppShapes.card,
                                border = BorderStroke(1.dp, CardBorderLight)
                            ) {
                                Column(modifier = Modifier.padding(18.dp)) {
                                    Text("CONFIDENTIAL FEEDBACK FROM BUYERS", style = AppTypography.label.copy(color = CharcoalMuted, letterSpacing = 1.sp))
                                    Spacer(modifier = Modifier.height(14.dp))

                                    Column(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .background(WarmCream, RoundedCornerShape(14.dp))
                                            .border(1.dp, CardBorderLight, RoundedCornerShape(14.dp))
                                            .padding(18.dp),
                                        horizontalAlignment = Alignment.CenterHorizontally
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Lock,
                                            contentDescription = "Confidential Lock",
                                            tint = SavPurple,
                                            modifier = Modifier.size(32.dp)
                                        )
                                        Spacer(modifier = Modifier.height(10.dp))
                                        Text(
                                            text = "Buyer Feedback is Encrypted & Private",
                                            style = AppTypography.bodyLarge.copy(
                                                fontFamily = SoraFontFamily,
                                                fontWeight = FontWeight.Bold,
                                                color = Charcoal
                                            )
                                        )
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text(
                                            text = "To guarantee absolute shopping fairness on AfriSav, direct ratings and written comments left by buyers are kept strictly confidential from merchants.",
                                            style = AppTypography.caption.copy(color = CharcoalSecondary),
                                            textAlign = TextAlign.Center,
                                            lineHeight = 16.sp
                                        )
                                    }
                                }
                            }
                        }

                        // Section 2: Rate & Review Buyers form
                        item {
                            Card(
                                colors = CardDefaults.cardColors(containerColor = SurfaceWhite),
                                shape = AppShapes.card,
                                border = BorderStroke(1.dp, CardBorderLight)
                            ) {
                                Column(modifier = Modifier.padding(18.dp)) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(Icons.Default.RateReview, contentDescription = null, tint = SavPurple, modifier = Modifier.size(20.dp))
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text("Rate & Review Your Buyers", style = AppTypography.h3.copy(color = Charcoal))
                                    }
                                    Text(
                                        text = "Help local farmers and bulk circles identify trusted and friendly shoppers.",
                                        style = AppTypography.caption.copy(color = CharcoalMuted),
                                        modifier = Modifier.padding(top = 4.dp, bottom = 12.dp)
                                    )

                                    if (buyerReviewSuccess) {
                                        AfriSavBadge(
                                            text = "Review submitted successfully! Thank you for your feedback.",
                                            type = AfriSavBadgeType.SUCCESS,
                                            ground = SurfaceGround.LIGHT,
                                            modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp)
                                        )
                                    }

                                    Text("Select Buyer to Rate", style = AppTypography.label.copy(fontWeight = FontWeight.Bold, color = CharcoalSecondary))
                                    Spacer(modifier = Modifier.height(6.dp))
                                    LazyRow(
                                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        items(buyerList) { buyer ->
                                            val isSelected = selectedBuyerReview == buyer
                                            Box(
                                                modifier = Modifier
                                                    .clip(RoundedCornerShape(8.dp))
                                                    .background(if (isSelected) SavPurple else SurfaceWhite)
                                                    .border(1.dp, if (isSelected) SavPurple else CardBorderLight, RoundedCornerShape(8.dp))
                                                    .clickable { selectedBuyerReview = buyer }
                                                    .padding(horizontal = 12.dp, vertical = 7.dp)
                                            ) {
                                                Text(
                                                    text = buyer,
                                                    style = AppTypography.bodySmall.copy(
                                                        fontWeight = FontWeight.SemiBold,
                                                        color = if (isSelected) Color.White else CharcoalSecondary
                                                    )
                                                )
                                            }
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(14.dp))

                                    Text("Assign Rating Stars", style = AppTypography.label.copy(fontWeight = FontWeight.Bold, color = CharcoalSecondary))
                                    Row(
                                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier.padding(vertical = 6.dp)
                                    ) {
                                        for (i in 1..5) {
                                            Icon(
                                                imageVector = Icons.Default.Star,
                                                contentDescription = "Star $i",
                                                tint = if (i <= buyerRating) Color(0xFFEAB308) else Color(0xFFE2E8F0),
                                                modifier = Modifier
                                                    .size(30.dp)
                                                    .clickable { buyerRating = i }
                                            )
                                        }
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(
                                            text = "$buyerRating / 5 Stars",
                                            style = AppTypography.label.copy(fontWeight = FontWeight.Bold, color = CharcoalSecondary)
                                        )
                                    }

                                    Spacer(modifier = Modifier.height(12.dp))

                                    SellerInputField(
                                        value = buyerReviewComment,
                                        onValueChange = { buyerReviewComment = it },
                                        label = "Buyer Feedback Comment",
                                        placeholder = "Friendly buyer, picked up foods without delay!",
                                        modifier = Modifier.testTag("seller_buyer_review_comment")
                                    )

                                    Spacer(modifier = Modifier.height(16.dp))

                                    AfriSavPrimaryButton(
                                        text = "Submit buyer review",
                                        onClick = {
                                            if (buyerReviewComment.trim().isNotEmpty()) {
                                                viewModel.submitReviewRating(selectedBuyerReview, false, buyerRating, buyerReviewComment.trim())
                                                buyerReviewComment = ""
                                                buyerReviewSuccess = true
                                            }
                                        },
                                        ground = SurfaceGround.LIGHT,
                                        modifier = Modifier.testTag("seller_buyer_review_submit")
                                    )
                                }
                            }
                        }

                        // Section 3: History of Left Reviews
                        if (buyersReviewedByMe.isNotEmpty()) {
                            item {
                                Text(
                                    text = "Reviews You've Left for Buyers",
                                    style = AppTypography.h3.copy(color = Charcoal),
                                    modifier = Modifier.padding(top = 8.dp)
                                )
                            }
                            items(buyersReviewedByMe) { rev ->
                                Card(
                                    colors = CardDefaults.cardColors(containerColor = SurfaceWhite),
                                    shape = RoundedCornerShape(14.dp),
                                    border = BorderStroke(1.dp, CardBorderLight)
                                ) {
                                    Column(modifier = Modifier.padding(14.dp)) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text(rev.targetName, style = AppTypography.bodyMedium.copy(fontWeight = FontWeight.Bold, color = Charcoal))
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                for (i in 1..rev.rating) {
                                                    Icon(Icons.Default.Star, contentDescription = null, tint = Color(0xFFEAB308), modifier = Modifier.size(14.dp))
                                                }
                                            }
                                        }
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text(rev.reviewText, style = AppTypography.caption.copy(color = CharcoalSecondary))
                                    }
                                }
                            }
                        }
                    }
                }
                4 -> {
                    // =========================================================================
                    // --- SCREEN 4: SELLER PROFILE TAB ---
                    // Surface: White on Warm Cream, Sav Purple actions
                    // =========================================================================
                    var name by remember { mutableStateOf(sellerName) }
                    var phone by remember { mutableStateOf(sellerPhone) }
                    var email by remember { mutableStateOf(sellerEmail) }
                    var location by remember { mutableStateOf(sellerLocation) }
                    var bio by remember { mutableStateOf(sellerBio) }
                    var pin by remember { mutableStateOf(sellerPinVal) }
                    var isSuccessMsgVisible by remember { mutableStateOf(false) }

                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        // Profile Avatar Card
                        item {
                            Card(
                                colors = CardDefaults.cardColors(containerColor = SurfaceWhite),
                                shape = AppShapes.card,
                                border = BorderStroke(1.dp, CardBorderLight)
                            ) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(22.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(76.dp)
                                            .clip(CircleShape)
                                            .border(2.dp, SavPurple, CircleShape)
                                    ) {
                                        ProfileAvatar(
                                            imageUrl = null,
                                            role = "seller",
                                            modifier = Modifier.fillMaxSize()
                                        )
                                    }
                                    Spacer(modifier = Modifier.height(12.dp))
                                    Text(
                                        text = sellerName,
                                        style = AppTypography.h2.copy(fontSize = 20.sp, color = Charcoal)
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    AfriSavBadge(
                                        text = "Verified Soko Merchant Partner",
                                        type = AfriSavBadgeType.SUCCESS,
                                        ground = SurfaceGround.LIGHT
                                    )
                                }
                            }
                        }

                        // Summary Statistics Row
                        item {
                            val avgRating = if (myReviews.isEmpty()) 4.8 else myReviews.map { it.rating }.average()
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Card(
                                    colors = CardDefaults.cardColors(containerColor = SurfaceWhite),
                                    shape = RoundedCornerShape(14.dp),
                                    border = BorderStroke(1.dp, CardBorderLight),
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Column(
                                        modifier = Modifier.padding(12.dp),
                                        horizontalAlignment = Alignment.CenterHorizontally
                                    ) {
                                        Text("AVG RATING", style = AppTypography.label.copy(fontSize = 9.sp, color = CharcoalMuted))
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text(
                                            text = "★ ${String.format("%.1f", avgRating)}",
                                            style = AppTypography.figureSmall.copy(
                                                fontSize = 15.sp,
                                                color = Charcoal
                                            )
                                        )
                                    }
                                }
                                Card(
                                    colors = CardDefaults.cardColors(containerColor = SurfaceWhite),
                                    shape = RoundedCornerShape(14.dp),
                                    border = BorderStroke(1.dp, CardBorderLight),
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Column(
                                        modifier = Modifier.padding(12.dp),
                                        horizontalAlignment = Alignment.CenterHorizontally
                                    ) {
                                        Text("TOTAL PRODUCTS", style = AppTypography.label.copy(fontSize = 9.sp, color = CharcoalMuted))
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text(
                                            text = "${sellerItems.size} items",
                                            style = AppTypography.figureSmall.copy(
                                                fontSize = 15.sp,
                                                color = SavPurple
                                            )
                                        )
                                    }
                                }
                                Card(
                                    colors = CardDefaults.cardColors(containerColor = SurfaceWhite),
                                    shape = RoundedCornerShape(14.dp),
                                    border = BorderStroke(1.dp, CardBorderLight),
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Column(
                                        modifier = Modifier.padding(12.dp),
                                        horizontalAlignment = Alignment.CenterHorizontally
                                    ) {
                                        Text("TOTAL SALES", style = AppTypography.label.copy(fontSize = 9.sp, color = CharcoalMuted))
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text(
                                            text = "${earnings.totalSalesCount} orders",
                                            style = AppTypography.figureSmall.copy(
                                                fontSize = 15.sp,
                                                color = Charcoal
                                            )
                                        )
                                    }
                                }
                            }
                        }

                        // Success Indicator
                        if (isSuccessMsgVisible) {
                            item {
                                AfriSavBadge(
                                    text = "Your merchant profile has been saved securely!",
                                    type = AfriSavBadgeType.SUCCESS,
                                    ground = SurfaceGround.LIGHT,
                                    modifier = Modifier.fillMaxWidth()
                                )
                            }
                        }

                        // Editable Details Card
                        item {
                            Card(
                                colors = CardDefaults.cardColors(containerColor = SurfaceWhite),
                                shape = AppShapes.card,
                                border = BorderStroke(1.dp, CardBorderLight)
                            ) {
                                Column(modifier = Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
                                    Text("EDIT MERCHANT DETAILS", style = AppTypography.label.copy(color = CharcoalMuted, letterSpacing = 1.sp))

                                    SellerInputField(
                                        value = name,
                                        onValueChange = { name = it },
                                        label = "Business/Store Name",
                                        singleLine = true,
                                        modifier = Modifier.testTag("seller_profile_name")
                                    )

                                    SellerInputField(
                                        value = phone,
                                        onValueChange = { phone = it },
                                        label = "Contact Phone Number",
                                        singleLine = true,
                                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                                        modifier = Modifier.testTag("seller_profile_phone")
                                    )

                                    SellerInputField(
                                        value = email,
                                        onValueChange = { email = it },
                                        label = "Store Email Address",
                                        singleLine = true,
                                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                                        modifier = Modifier.testTag("seller_profile_email")
                                    )

                                    SellerInputField(
                                        value = location,
                                        onValueChange = { location = it },
                                        label = "Business Location LGA",
                                        singleLine = true,
                                        modifier = Modifier.testTag("seller_profile_location")
                                    )

                                    SellerInputField(
                                        value = bio,
                                        onValueChange = { bio = it },
                                        label = "Merchant Store Bio",
                                        modifier = Modifier.testTag("seller_profile_bio")
                                    )
                                }
                            }
                        }

                        // Security Card
                        item {
                            Card(
                                colors = CardDefaults.cardColors(containerColor = SurfaceWhite),
                                shape = AppShapes.card,
                                border = BorderStroke(1.dp, CardBorderLight)
                            ) {
                                Column(modifier = Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
                                    Text("ACCOUNT SECURITY & PASSWORD", style = AppTypography.label.copy(color = CharcoalMuted, letterSpacing = 1.sp))

                                    SellerInputField(
                                        value = pin,
                                        onValueChange = { pin = it },
                                        label = "Merchant Security PIN",
                                        singleLine = true,
                                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                                        visualTransformation = PasswordVisualTransformation(),
                                        modifier = Modifier.testTag("seller_profile_pin")
                                    )
                                }
                            }
                        }

                        // Submit & Logout buttons
                        item {
                            AfriSavPrimaryButton(
                                text = "Save profile changes",
                                onClick = {
                                    if (name.isNotEmpty() && phone.isNotEmpty()) {
                                        viewModel.updateUserProfile(name, phone, email, location, bio)
                                        if (pin.isNotEmpty()) {
                                            viewModel.updateSellerPin(pin)
                                        }
                                        isSuccessMsgVisible = true
                                    }
                                },
                                ground = SurfaceGround.LIGHT,
                                modifier = Modifier.testTag("seller_profile_save")
                            )
                        }

                        item {
                            OutlinedButton(
                                onClick = onLogoutClick,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .heightIn(min = 52.dp)
                                    .testTag("seller_profile_logout"),
                                colors = ButtonDefaults.outlinedButtonColors(contentColor = SemanticErrorLight),
                                border = BorderStroke(1.5.dp, SemanticErrorLight),
                                shape = AppShapes.button
                            ) {
                                Text("Sign out of Soko account", style = AppTypography.button.copy(color = SemanticErrorLight))
                            }
                        }
                    }
                }
            }
        }
    }

    // =========================================================================
    // MODAL WITHDRAWAL DIALOG (White on Warm Cream form)
    // =========================================================================
    if (showSellerWithdrawConfirmDialog) {
        AlertDialog(
            onDismissRequest = { showSellerWithdrawConfirmDialog = false },
            containerColor = SurfaceWhite,
            shape = AppShapes.card,
            title = {
                Text(
                    text = "Settlement Transfer",
                    style = AppTypography.h3.copy(color = Charcoal)
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(
                        text = "Transfer your earnings instantly to your verified bank account.",
                        style = AppTypography.caption.copy(color = CharcoalSecondary)
                    )

                    SellerInputField(
                        value = withdrawAmountStr,
                        onValueChange = { withdrawAmountStr = it },
                        label = "Withdrawal Amount (₦)",
                        placeholder = "e.g. 10000",
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.testTag("seller_withdraw_amt")
                    )

                    Column {
                        Text("Destination Bank", style = AppTypography.label.copy(fontWeight = FontWeight.Bold, color = CharcoalSecondary))
                        Spacer(modifier = Modifier.height(6.dp))
                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            items(banks) { b ->
                                val isSelected = selectedBank == b
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(if (isSelected) SavPurple else SurfaceWhite)
                                        .border(1.dp, if (isSelected) SavPurple else CardBorderLight, RoundedCornerShape(8.dp))
                                        .clickable { selectedBank = b }
                                        .padding(horizontal = 12.dp, vertical = 6.dp)
                                ) {
                                    Text(
                                        text = b,
                                        style = AppTypography.bodySmall.copy(
                                            fontWeight = FontWeight.SemiBold,
                                            color = if (isSelected) Color.White else CharcoalSecondary
                                        )
                                    )
                                }
                            }
                        }
                    }

                    SellerInputField(
                        value = withdrawAccountStr,
                        onValueChange = { withdrawAccountStr = it },
                        label = "10-Digit Account Number",
                        placeholder = "e.g. 0123456789",
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.testTag("seller_withdraw_account")
                    )

                    if (withdrawErrorText.isNotEmpty()) {
                        Text(
                            text = withdrawErrorText,
                            color = SemanticErrorLight,
                            style = AppTypography.caption.copy(fontWeight = FontWeight.SemiBold)
                        )
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val amt = withdrawAmountStr.toDoubleOrNull()
                        if (amt == null || amt <= 0) {
                            withdrawErrorText = "Please enter a valid amount"
                        } else if (amt > earnings.availableBalance) {
                            withdrawErrorText = "Withdrawal amount exceeds available balance"
                        } else if (withdrawAccountStr.length != 10) {
                            withdrawErrorText = "Please enter a valid 10-digit NUBAN account number"
                        } else {
                            viewModel.sellerWithdraw(amt, selectedBank, withdrawAccountStr)
                            lastWithdrawalAmount = amt
                            lastWithdrawalAccount = withdrawAccountStr
                            lastWithdrawalBank = selectedBank
                            withdrawAmountStr = ""
                            withdrawAccountStr = ""
                            withdrawErrorText = ""
                            showSellerWithdrawConfirmDialog = false
                            showWithdrawalCelebrationDialog = true
                        }
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = SavPurple,
                        contentColor = Color.White
                    ),
                    shape = AppShapes.button
                ) {
                    Text("Confirm transfer", style = AppTypography.button.copy(color = Color.White))
                }
            },
            dismissButton = {
                TextButton(onClick = {
                    showSellerWithdrawConfirmDialog = false
                    withdrawErrorText = ""
                }) {
                    Text("Cancel", style = AppTypography.button.copy(color = CharcoalSecondary))
                }
            }
        )
    }

    // =========================================================================
    // CELEBRATORY MOMENT DIALOG (Per Brand Bible rule:
    // "Any celebratory moment on the Seller side (e.g. a withdrawal success
    // confirmation, hitting a sales milestone) may use the Purple/Plum ground +
    // Lime action treatment, consistent with how celebration moments are
    // handled on the Buyer side.")
    // =========================================================================
    if (showWithdrawalCelebrationDialog) {
        AlertDialog(
            onDismissRequest = { showWithdrawalCelebrationDialog = false },
            containerColor = DeepPlumCard,
            shape = AppShapes.card,
            modifier = Modifier.border(BorderStroke(1.5.dp, DeepPlumBorder), AppShapes.card),
            title = {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Box(
                        modifier = Modifier
                            .size(56.dp)
                            .clip(CircleShape)
                            .background(SavPurple)
                            .border(1.5.dp, HarvestLime, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = "Success",
                            tint = HarvestLime,
                            modifier = Modifier.size(34.dp)
                        )
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "Transfer Executed!",
                        style = AppTypography.h2.copy(color = Color.White, textAlign = TextAlign.Center)
                    )
                }
            },
            text = {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = AppFormatters.formatNaira(lastWithdrawalAmount),
                        style = AppTypography.figureLarge.copy(
                            fontSize = 32.sp,
                            color = HarvestLime,
                            textAlign = TextAlign.Center
                        )
                    )
                    Text(
                        text = "Your settlement payout has been dispatched to $lastWithdrawalBank ($lastWithdrawalAccount).",
                        style = AppTypography.bodySmall.copy(color = Purple200, textAlign = TextAlign.Center)
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    AfriSavBadge(
                        text = "Settlement Confirmed",
                        type = AfriSavBadgeType.SUCCESS,
                        ground = SurfaceGround.DARK
                    )
                }
            },
            confirmButton = {
                AfriSavPrimaryButton(
                    text = "Done",
                    onClick = { showWithdrawalCelebrationDialog = false },
                    ground = SurfaceGround.DARK, // Purple/Plum ground + Lime action treatment
                    modifier = Modifier.fillMaxWidth()
                )
            }
        )
    }
}

/**
 * Standardized Input Field for Seller Screens, enforcing 52px height, 12px radius,
 * label always visible above the field, and SavPurple focus outline on light ground.
 */
@Composable
fun SellerInputField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    modifier: Modifier = Modifier,
    placeholder: String? = null,
    singleLine: Boolean = false,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    visualTransformation: VisualTransformation = VisualTransformation.None,
    isError: Boolean = false
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = label,
            style = AppTypography.label.copy(fontWeight = FontWeight.Bold, color = CharcoalSecondary),
            modifier = Modifier.padding(bottom = 6.dp)
        )
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            placeholder = if (placeholder != null) {
                { Text(placeholder, style = AppTypography.body.copy(color = CharcoalMuted)) }
            } else null,
            singleLine = singleLine,
            keyboardOptions = keyboardOptions,
            visualTransformation = visualTransformation,
            isError = isError,
            modifier = modifier
                .fillMaxWidth()
                .heightIn(min = 52.dp),
            textStyle = AppTypography.body.copy(color = Charcoal),
            shape = AppShapes.input,
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = SurfaceWhite,
                unfocusedContainerColor = SurfaceWhite,
                focusedBorderColor = SavPurple,
                unfocusedBorderColor = CardBorderLight,
                errorBorderColor = SemanticErrorLight,
                cursorColor = SavPurple
            )
        )
    }
}
