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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.example.data.*
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
        contentWindowInsets = androidx.compose.foundation.layout.WindowInsets(0, 0, 0, 0),
        bottomBar = {
            NavigationBar(
                containerColor = SurfaceBg,
                tonalElevation = 0.dp,
                modifier = Modifier.navigationBarsPadding()
            ) {
                NavigationBarItem(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    icon = { Icon(Icons.Default.Storefront, contentDescription = "My Listings") },
                    label = { Text("My Listings", fontSize = 10.sp, fontWeight = FontWeight.Bold) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = PrimaryGreen,
                        selectedTextColor = PrimaryGreen,
                        unselectedIconColor = TextSlate400,
                        unselectedTextColor = TextSlate400,
                        indicatorColor = PrimaryGreen.copy(alpha = 0.12f)
                    )
                )
                NavigationBarItem(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    icon = { Icon(Icons.Default.AccountBalanceWallet, contentDescription = "Earnings") },
                    label = { Text("Earnings", fontSize = 10.sp, fontWeight = FontWeight.Bold) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = PrimaryGreen,
                        selectedTextColor = PrimaryGreen,
                        unselectedIconColor = TextSlate400,
                        unselectedTextColor = TextSlate400,
                        indicatorColor = PrimaryGreen.copy(alpha = 0.12f)
                    )
                )
                NavigationBarItem(
                    selected = selectedTab == 2,
                    onClick = { selectedTab = 2 },
                    icon = { Icon(Icons.Default.BarChart, contentDescription = "Analytics") },
                    label = { Text("Analytics", fontSize = 10.sp, fontWeight = FontWeight.Bold) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = PrimaryGreen,
                        selectedTextColor = PrimaryGreen,
                        unselectedIconColor = TextSlate400,
                        unselectedTextColor = TextSlate400,
                        indicatorColor = PrimaryGreen.copy(alpha = 0.12f)
                    )
                )
                NavigationBarItem(
                    selected = selectedTab == 3,
                    onClick = { selectedTab = 3 },
                    icon = { Icon(Icons.Default.RateReview, contentDescription = "Reviews") },
                    label = { Text("Reviews", fontSize = 10.sp, fontWeight = FontWeight.Bold) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = PrimaryGreen,
                        selectedTextColor = PrimaryGreen,
                        unselectedIconColor = TextSlate400,
                        unselectedTextColor = TextSlate400,
                        indicatorColor = PrimaryGreen.copy(alpha = 0.12f)
                    )
                )
                NavigationBarItem(
                    selected = selectedTab == 4,
                    onClick = { selectedTab = 4 },
                    icon = { Icon(Icons.Default.Person, contentDescription = "Profile") },
                    label = { Text("Profile", fontSize = 10.sp, fontWeight = FontWeight.Bold) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = PrimaryGreen,
                        selectedTextColor = PrimaryGreen,
                        unselectedIconColor = TextSlate400,
                        unselectedTextColor = TextSlate400,
                        indicatorColor = PrimaryGreen.copy(alpha = 0.12f)
                    )
                )
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(BgSlate50)
        ) {
            // Seller Header
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(PrimaryGreen)
                    .statusBarsPadding()
                    .padding(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 14.dp)
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
                            .clickable { selectedTab = 4 } // Tapping avatar navigates directly to Profile screen
                            .testTag("seller_avatar_profile_btn")
                            .padding(4.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .border(1.dp, Color.White.copy(alpha = 0.4f), CircleShape)
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
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                            Text(
                                text = "Merchant • $sellerPhone",
                                fontSize = 10.sp,
                                color = Color.White.copy(alpha = 0.8f)
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
                            modifier = Modifier.background(Color.White.copy(alpha = 0.15f), CircleShape)
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.Logout,
                                contentDescription = "Logout",
                                tint = Color.White
                            )
                        }
                    }
                }
            }

            // Screen Content Router
            when (selectedTab) {
                0 -> {
                    // --- MY LISTINGS TAB ---
                    Column(modifier = Modifier.fillMaxSize()) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 12.dp)
                                .background(Color.White, RoundedCornerShape(12.dp))
                                .padding(4.dp),
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .height(38.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(if (listingsSubTab == "listings") PrimaryGreen else Color.Transparent)
                                    .clickable { listingsSubTab = "listings" }
                                    .testTag("seller_tab_listings_list"),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "My Inventory (${sellerItems.size})",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (listingsSubTab == "listings") Color.White else TextSlate500
                                )
                            }
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .height(38.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(if (listingsSubTab == "add") PrimaryGreen else Color.Transparent)
                                    .clickable { listingsSubTab = "add" }
                                    .testTag("seller_tab_listings_add"),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "➕ Add Product",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (listingsSubTab == "add") Color.White else TextSlate500
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
                                        message = "Your Store is Empty",
                                        subMessage = "Get started by adding high-quality local foodstuffs to your Soko digital storefront.",
                                        icon = Icons.Default.Storefront,
                                        iconColor = PrimaryGreen
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
                                            fontSize = 14.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = TextSlate800,
                                            modifier = Modifier.padding(vertical = 4.dp)
                                        )
                                    }
                                    items(sellerItems) { item ->
                                        Card(
                                            colors = CardDefaults.cardColors(containerColor = SurfaceBg),
                                            shape = RoundedCornerShape(16.dp),
                                            border = BorderStroke(1.dp, BorderSlate100)
                                        ) {
                                            Row(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .padding(12.dp),
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Box(
                                                    modifier = Modifier
                                                        .size(50.dp)
                                                        .clip(RoundedCornerShape(12.dp))
                                                        .background(PrimaryGreen.copy(alpha = 0.08f)),
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
                                                        fontWeight = FontWeight.Bold,
                                                        fontSize = 14.sp,
                                                        color = TextSlate800
                                                    )
                                                    Spacer(modifier = Modifier.height(2.dp))
                                                    Row(
                                                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                                                        verticalAlignment = Alignment.CenterVertically
                                                    ) {
                                                        Box(
                                                            modifier = Modifier
                                                                .background(PrimaryGreen.copy(alpha = 0.1f), RoundedCornerShape(4.dp))
                                                                .padding(horizontal = 6.dp, vertical = 2.dp)
                                                        ) {
                                                            Text(item.category, fontSize = 9.sp, fontWeight = FontWeight.Bold, color = PrimaryGreen)
                                                        }
                                                        Box(
                                                            modifier = Modifier
                                                                .background(SecondaryOrange.copy(alpha = 0.1f), RoundedCornerShape(4.dp))
                                                                .padding(horizontal = 6.dp, vertical = 2.dp)
                                                        ) {
                                                            Text("From ${item.state}", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = SecondaryOrange)
                                                        }
                                                    }
                                                    if (item.allowPortions) {
                                                        Text(
                                                            text = "Portions setup: ${item.portionType}",
                                                            fontSize = 11.sp,
                                                            color = TextSlate400,
                                                            modifier = Modifier.padding(top = 4.dp)
                                                        )
                                                    }
                                                }
                                                Text(
                                                    text = "₦${String.format("%,.0f", item.price)}",
                                                    fontWeight = FontWeight.Black,
                                                    fontSize = 16.sp,
                                                    color = PrimaryGreen
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        } else {
                            Column(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .verticalScroll(rememberScrollState())
                                    .padding(16.dp),
                                verticalArrangement = Arrangement.spacedBy(16.dp)
                            ) {
                                Card(
                                    colors = CardDefaults.cardColors(containerColor = SurfaceBg),
                                    shape = RoundedCornerShape(16.dp),
                                    border = BorderStroke(1.dp, BorderSlate100)
                                ) {
                                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                                        Text("NEW LISTING DETAILS", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = TextSlate400, letterSpacing = 1.sp)
                                        
                                        OutlinedTextField(
                                            value = foodName,
                                            onValueChange = { foodName = it },
                                            label = { Text("Foodstuff Name") },
                                            placeholder = { Text("e.g. Clean White Garri (1 Bag)") },
                                            singleLine = true,
                                            modifier = Modifier.fillMaxWidth().testTag("seller_add_item_name"),
                                            colors = defaultTextFieldColors(),
                                            shape = RoundedCornerShape(10.dp)
                                        )

                                        OutlinedTextField(
                                            value = priceStr,
                                            onValueChange = { priceStr = it },
                                            label = { Text("Price (₦)") },
                                            placeholder = { Text("e.g. 24000") },
                                            singleLine = true,
                                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                            modifier = Modifier.fillMaxWidth().testTag("seller_add_item_price"),
                                            colors = defaultTextFieldColors(),
                                            shape = RoundedCornerShape(10.dp)
                                        )

                                        Text("Select Foodstuff Category", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = TextSlate500)
                                        LazyRow(
                                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                                            modifier = Modifier.fillMaxWidth()
                                        ) {
                                            items(categories) { cat ->
                                                Box(
                                                    modifier = Modifier
                                                        .clip(RoundedCornerShape(8.dp))
                                                        .background(if (selectedCategory == cat) PrimaryGreen else BgSlate50)
                                                        .border(1.dp, if (selectedCategory == cat) PrimaryGreen else BorderSlate100, RoundedCornerShape(8.dp))
                                                        .clickable { selectedCategory = cat }
                                                        .padding(horizontal = 12.dp, vertical = 6.dp)
                                                ) {
                                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                                        Text(getCategoryEmoji(cat), fontSize = 14.sp)
                                                        Spacer(modifier = Modifier.width(4.dp))
                                                        Text(
                                                            text = cat,
                                                            fontSize = 12.sp,
                                                            fontWeight = FontWeight.SemiBold,
                                                            color = if (selectedCategory == cat) Color.White else TextSlate500
                                                        )
                                                    }
                                                }
                                            }
                                        }

                                        Text("Sourcing State Origin", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = TextSlate500)
                                        LazyRow(
                                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                                            modifier = Modifier.fillMaxWidth()
                                        ) {
                                            items(states) { st ->
                                                Box(
                                                    modifier = Modifier
                                                        .clip(RoundedCornerShape(8.dp))
                                                        .background(if (selectedState == st) SecondaryOrange else BgSlate50)
                                                        .border(1.dp, if (selectedState == st) SecondaryOrange else BorderSlate100, RoundedCornerShape(8.dp))
                                                        .clickable { selectedState = st }
                                                        .padding(horizontal = 12.dp, vertical = 6.dp)
                                                ) {
                                                    Text(
                                                        text = st,
                                                        fontSize = 12.sp,
                                                        fontWeight = FontWeight.SemiBold,
                                                        color = if (selectedState == st) Color.White else TextSlate500
                                                    )
                                                }
                                            }
                                        }

                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.SpaceBetween
                                        ) {
                                            Column(modifier = Modifier.weight(1f)) {
                                                Text("Is this a Food Bundle?", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = TextSlate800)
                                                Text("Group multiple foodstuffs into one single sale item", fontSize = 11.sp, color = TextSlate400)
                                            }
                                            Spacer(modifier = Modifier.width(8.dp))
                                            Switch(
                                                checked = isBundle,
                                                onCheckedChange = { isBundle = it },
                                                colors = SwitchDefaults.colors(checkedThumbColor = Color.White, checkedTrackColor = PrimaryGreen)
                                            )
                                        }

                                        if (isBundle) {
                                            OutlinedTextField(
                                                value = bundleItems,
                                                onValueChange = { bundleItems = it },
                                                label = { Text("List items in bundle (comma separated)") },
                                                placeholder = { Text("e.g. 1 Tub of Yam, 1 Bottle of Palm Oil") },
                                                modifier = Modifier.fillMaxWidth(),
                                                colors = defaultTextFieldColors(),
                                                shape = RoundedCornerShape(10.dp)
                                            )
                                        }

                                        Divider(color = BorderSlate100)

                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.SpaceBetween
                                        ) {
                                            Column(modifier = Modifier.weight(1f)) {
                                                Text("Allow Portion Breakdown?", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = TextSlate800)
                                                Text("Let buyers buy in portions/smaller units", fontSize = 11.sp, color = TextSlate400)
                                            }
                                            Spacer(modifier = Modifier.width(8.dp))
                                            Switch(
                                                checked = allowPortions,
                                                onCheckedChange = { allowPortions = it },
                                                colors = SwitchDefaults.colors(checkedThumbColor = Color.White, checkedTrackColor = PrimaryGreen)
                                            )
                                        }

                                        if (allowPortions) {
                                            Text("Select Portion Breakdown Type", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = TextSlate500)
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                                            ) {
                                                listOf("Bag", "Mudu", "Paint Bucket", "Tuber").forEach { pType ->
                                                    Box(
                                                        modifier = Modifier
                                                            .weight(1f)
                                                            .clip(RoundedCornerShape(8.dp))
                                                            .background(if (portionType == pType) PrimaryGreen.copy(alpha = 0.1f) else BgSlate50)
                                                            .border(1.dp, if (portionType == pType) PrimaryGreen else BorderSlate100, RoundedCornerShape(8.dp))
                                                            .clickable { portionType = pType }
                                                            .padding(vertical = 8.dp),
                                                        contentAlignment = Alignment.Center
                                                    ) {
                                                        Text(
                                                            text = pType,
                                                            fontSize = 11.sp,
                                                            fontWeight = FontWeight.Bold,
                                                            color = if (portionType == pType) PrimaryGreen else TextSlate500
                                                        )
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }

                                Card(
                                    colors = CardDefaults.cardColors(containerColor = SurfaceBg),
                                    shape = RoundedCornerShape(16.dp),
                                    border = BorderStroke(1.dp, BorderSlate100)
                                ) {
                                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                        Text("FOODSTUFF IMAGE (OPTIONAL)", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = TextSlate400, letterSpacing = 1.sp)
                                        
                                        if (attachedImageUri != null) {
                                            Box(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .height(140.dp)
                                                    .clip(RoundedCornerShape(12.dp))
                                                    .background(Color(0xFFF1F5F9))
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
                                                    modifier = Modifier.weight(1f).height(48.dp),
                                                    colors = ButtonDefaults.outlinedButtonColors(contentColor = PrimaryGreen),
                                                    border = BorderStroke(1.dp, PrimaryGreen),
                                                    shape = RoundedCornerShape(8.dp)
                                                ) {
                                                    Icon(Icons.Default.Photo, contentDescription = null, modifier = Modifier.size(16.dp))
                                                    Spacer(modifier = Modifier.width(6.dp))
                                                    Text("Gallery", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                                }
                                                OutlinedButton(
                                                    onClick = { cameraLauncher.launch(null) },
                                                    modifier = Modifier.weight(1f).height(48.dp),
                                                    colors = ButtonDefaults.outlinedButtonColors(contentColor = PrimaryGreen),
                                                    border = BorderStroke(1.dp, PrimaryGreen),
                                                    shape = RoundedCornerShape(8.dp)
                                                ) {
                                                    Icon(Icons.Default.CameraAlt, contentDescription = null, modifier = Modifier.size(16.dp))
                                                    Spacer(modifier = Modifier.width(6.dp))
                                                    Text("Camera", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                                }
                                            }
                                        }
                                    }
                                }

                                if (errorText.isNotEmpty()) {
                                    Text(
                                        text = errorText,
                                        color = MaterialTheme.colorScheme.error,
                                        fontSize = 12.sp,
                                        modifier = Modifier.padding(horizontal = 4.dp)
                                    )
                                }

                                Button(
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
                                            Toast.makeText(context, "New Listing Published Successfully!", Toast.LENGTH_SHORT).show()
                                        }
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryGreen),
                                    shape = RoundedCornerShape(12.dp),
                                    modifier = Modifier.fillMaxWidth().height(48.dp).testTag("seller_add_item_btn")
                                ) {
                                    Text("Publish to Soko Market", fontWeight = FontWeight.Bold, color = Color.White)
                                }
                            }
                        }
                    }
                }
                1 -> {
                    // --- EARNINGS & PAY TAB ---
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
                                    colors = CardDefaults.cardColors(containerColor = if (sellBal <= 0.0) Color(0xFFFEF2F2) else Color(0xFFFFFBEB)),
                                    border = BorderStroke(1.dp, if (sellBal <= 0.0) Color(0xFFFEE2E2) else Color(0xFFFEF3C7)),
                                    shape = RoundedCornerShape(16.dp)
                                ) {
                                    Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            imageVector = Icons.Default.Warning,
                                            contentDescription = "Warning",
                                            tint = if (sellBal <= 0.0) Color(0xFFEF4444) else Color(0xFFD97706),
                                            modifier = Modifier.size(20.dp)
                                        )
                                        Spacer(modifier = Modifier.width(12.dp))
                                        Column {
                                            Text(
                                                text = if (sellBal <= 0.0) "No Earnings Available" else "Low Balance Warning",
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 13.sp,
                                                color = if (sellBal <= 0.0) Color(0xFF991B1B) else Color(0xFF92400E)
                                            )
                                            Text(
                                                text = if (sellBal <= 0.0) "Once buyers confirm receipt, your escrow funds will be released." else "Your balance is low. Withdrawals require at least ₦1,000.",
                                                fontSize = 11.sp,
                                                color = if (sellBal <= 0.0) Color(0xFFB91C1C) else Color(0xFFB45309)
                                            )
                                        }
                                    }
                                }
                            }
                        }

                        // Earnings Top Summary Card
                        item {
                            Card(
                                colors = CardDefaults.cardColors(containerColor = SurfaceBg),
                                shape = RoundedCornerShape(20.dp),
                                border = BorderStroke(1.dp, BorderSlate100)
                            ) {
                                Column(modifier = Modifier.padding(20.dp)) {
                                    Text("MERCHANT COFFER SUMMARY", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = TextSlate400, letterSpacing = 1.sp)
                                    Spacer(modifier = Modifier.height(16.dp))
                                    
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Column {
                                            Text("Available Balance", fontSize = 11.sp, color = TextSlate500)
                                            Text(
                                                text = "₦${String.format("%,.2f", earnings.availableBalance)}",
                                                fontSize = 24.sp,
                                                fontWeight = FontWeight.Black,
                                                color = PrimaryGreen
                                            )
                                            Spacer(modifier = Modifier.height(8.dp))
                                            Text("Escrow Account (Pending Release)", fontSize = 11.sp, color = TextSlate500)
                                            Text(
                                                text = "₦${String.format("%,.2f", pendingEscrowBalance)}",
                                                fontSize = 18.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = SecondaryOrange
                                            )
                                        }
                                        Button(
                                            onClick = { showSellerWithdrawConfirmDialog = true },
                                            colors = ButtonDefaults.buttonColors(containerColor = SecondaryOrange),
                                            shape = RoundedCornerShape(10.dp),
                                            modifier = Modifier.testTag("seller_withdraw_open_btn")
                                        ) {
                                            Icon(Icons.Default.AccountBalanceWallet, contentDescription = null, modifier = Modifier.size(14.dp))
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Text("Withdraw", fontSize = 12.sp, fontWeight = FontWeight.Bold)
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
                                    colors = CardDefaults.cardColors(containerColor = SurfaceBg),
                                    shape = RoundedCornerShape(16.dp),
                                    border = BorderStroke(1.dp, BorderSlate100),
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Column(modifier = Modifier.padding(16.dp)) {
                                        Text("Total Revenue", fontSize = 11.sp, color = TextSlate400)
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text("₦${String.format("%,.0f", earnings.totalRevenue)}", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = TextSlate800)
                                    }
                                }
                                Card(
                                    colors = CardDefaults.cardColors(containerColor = SurfaceBg),
                                    shape = RoundedCornerShape(16.dp),
                                    border = BorderStroke(1.dp, BorderSlate100),
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Column(modifier = Modifier.padding(16.dp)) {
                                        Text("Successful Sales", fontSize = 11.sp, color = TextSlate400)
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text("${earnings.totalSalesCount} Orders", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = TextSlate800)
                                    }
                                }
                            }
                        }

                        // Escrow Orders list if any
                        val escrowOrders = escrowOrdersForSeller.filter { it.status == "PENDING" }
                        if (escrowOrders.isNotEmpty()) {
                            item {
                                Text(
                                    text = "Active Escrow Orders (${escrowOrders.size})",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 15.sp,
                                    color = TextSlate800,
                                    modifier = Modifier.padding(top = 8.dp)
                                )
                            }
                            items(escrowOrders) { esc ->
                                Card(
                                    colors = CardDefaults.cardColors(containerColor = SurfaceBg),
                                    shape = RoundedCornerShape(16.dp),
                                    border = BorderStroke(1.dp, BorderSlate100)
                                ) {
                                    Column(modifier = Modifier.padding(16.dp)) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Column {
                                                Text(esc.itemName, fontWeight = FontWeight.Bold, fontSize = 13.sp, color = TextSlate800)
                                                Text("Buyer: ${esc.buyerName}", fontSize = 11.sp, color = TextSlate500)
                                            }
                                            Text(
                                                text = "₦${String.format("%,.0f", esc.amount)}",
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 13.sp,
                                                color = PrimaryGreen
                                            )
                                        }
                                        
                                        if (esc.pickupPin.isNotEmpty()) {
                                            Spacer(modifier = Modifier.height(10.dp))
                                            Box(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .background(Color(0xFFF0FDF4), RoundedCornerShape(8.dp))
                                                    .border(1.dp, Color(0xFFDCFCE7), RoundedCornerShape(8.dp))
                                                    .padding(8.dp)
                                            ) {
                                                Row(
                                                    modifier = Modifier.fillMaxWidth(),
                                                    horizontalArrangement = Arrangement.SpaceBetween,
                                                    verticalAlignment = Alignment.CenterVertically
                                                ) {
                                                    Column {
                                                        Text("SECURE PICKUP PIN", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = Color(0xFF14532D))
                                                        Text("Give this to the rider upon pickup", fontSize = 9.sp, color = Color(0xFF14532D).copy(alpha = 0.8f))
                                                    }
                                                    Text(
                                                        text = esc.pickupPin,
                                                        fontSize = 14.sp,
                                                        fontWeight = FontWeight.Black,
                                                        color = Color(0xFF166534),
                                                        modifier = Modifier
                                                            .background(Color.White, RoundedCornerShape(4.dp))
                                                            .padding(horizontal = 8.dp, vertical = 4.dp)
                                                    )
                                                }
                                            }
                                        }

                                        if (!esc.riderName.isNullOrEmpty()) {
                                            Spacer(modifier = Modifier.height(10.dp))
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.SpaceBetween,
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Text("Rider: ${esc.riderName}", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = TextSlate800)
                                                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                                    OutlinedButton(
                                                        onClick = { onContactRider?.invoke(esc, "CALL") },
                                                        modifier = Modifier.height(30.dp),
                                                        colors = ButtonDefaults.outlinedButtonColors(contentColor = PrimaryGreen),
                                                        border = BorderStroke(1.dp, PrimaryGreen),
                                                        contentPadding = PaddingValues(horizontal = 8.dp)
                                                    ) {
                                                        Text("Call Rider", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                                    }
                                                    OutlinedButton(
                                                        onClick = { onContactRider?.invoke(esc, "CHAT") },
                                                        modifier = Modifier.height(30.dp),
                                                        colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFF1D4ED8)),
                                                        border = BorderStroke(1.dp, Color(0xFF1D4ED8)),
                                                        contentPadding = PaddingValues(horizontal = 8.dp)
                                                    ) {
                                                        Text("Chat Rider", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }

                        // Withdrawal History Section Header
                        item {
                            Text(
                                text = "Settlement Withdrawal Logs",
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp,
                                color = TextSlate800,
                                modifier = Modifier.padding(top = 8.dp)
                            )
                        }

                        if (sellerWithdrawals.isEmpty()) {
                            item {
                                EmptyStateCard(
                                    message = "No Settlement Logs Yet",
                                    subMessage = "Your bank transfer withdrawals will appear here chronologically.",
                                    icon = Icons.Default.History,
                                    iconColor = TextSlate400
                                )
                            }
                        } else {
                            items(sellerWithdrawals) { w ->
                                Card(
                                    colors = CardDefaults.cardColors(containerColor = SurfaceBg),
                                    shape = RoundedCornerShape(12.dp),
                                    border = BorderStroke(1.dp, BorderSlate100)
                                ) {
                                    Row(
                                        modifier = Modifier.padding(14.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(36.dp)
                                                .clip(CircleShape)
                                                .background(SecondaryOrange.copy(alpha = 0.08f)),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Icon(Icons.Default.TrendingDown, contentDescription = null, tint = SecondaryOrange, modifier = Modifier.size(16.dp))
                                        }
                                        Spacer(modifier = Modifier.width(12.dp))
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text("Withdrawal to ${w.bankName}", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = TextSlate800)
                                            Text("Acc: ${w.accountNumber} • Processing", fontSize = 11.sp, color = TextSlate400)
                                        }
                                        Text(
                                            text = "-₦${String.format("%,.0f", w.amount)}",
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 14.sp,
                                            color = SecondaryOrange
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
                2 -> {
                    // --- MONTHLY ANALYTICS TAB ---
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
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp,
                                color = TextSlate800
                            )
                            Spacer(modifier = Modifier.height(8.dp))

                            LazyRow(
                                horizontalArrangement = Arrangement.spacedBy(6.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                items(months) { m ->
                                    val count = sellerSales.count { it.month.equals(m, ignoreCase = true) }
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(if (selectedMonth == m) PrimaryGreen else BgSlate50)
                                            .border(1.dp, if (selectedMonth == m) PrimaryGreen else BorderSlate100, RoundedCornerShape(8.dp))
                                            .clickable { selectedMonth = m }
                                            .padding(horizontal = 12.dp, vertical = 6.dp)
                                    ) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Text(
                                                text = m,
                                                fontSize = 12.sp,
                                                fontWeight = FontWeight.SemiBold,
                                                color = if (selectedMonth == m) Color.White else TextSlate500
                                            )
                                            if (count > 0) {
                                                Spacer(modifier = Modifier.width(4.dp))
                                                Box(
                                                    modifier = Modifier
                                                        .background(if (selectedMonth == m) Color.White else PrimaryGreen, CircleShape)
                                                        .padding(horizontal = 5.dp, vertical = 2.dp)
                                                ) {
                                                    Text(
                                                        text = "$count",
                                                        fontSize = 9.sp,
                                                        fontWeight = FontWeight.Bold,
                                                        color = if (selectedMonth == m) PrimaryGreen else Color.White
                                                    )
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }

                        // Visual Representation Bar Chart
                        item {
                            Card(
                                colors = CardDefaults.cardColors(containerColor = SurfaceBg),
                                shape = RoundedCornerShape(16.dp),
                                border = BorderStroke(1.dp, BorderSlate100)
                            ) {
                                Column(modifier = Modifier.padding(16.dp)) {
                                    Text("SALES VOLUME ANALYSIS: ${selectedMonth.uppercase()}", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = TextSlate400, letterSpacing = 1.sp)
                                    Spacer(modifier = Modifier.height(16.dp))

                                    if (groupedSales.isEmpty()) {
                                        Box(
                                            modifier = Modifier.fillMaxWidth().padding(vertical = 20.dp),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                                Icon(Icons.Default.BarChart, contentDescription = null, tint = TextSlate400, modifier = Modifier.size(32.dp))
                                                Spacer(modifier = Modifier.height(4.dp))
                                                Text("No Sales Registered", fontSize = 12.sp, color = TextSlate400)
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
                                                            .size(32.dp)
                                                            .clip(RoundedCornerShape(6.dp))
                                                            .background(PrimaryGreen.copy(alpha = 0.08f)),
                                                        contentAlignment = Alignment.Center
                                                    ) {
                                                        Text(getCategoryEmoji(firstSale.category), fontSize = 16.sp)
                                                    }
                                                    Spacer(modifier = Modifier.width(10.dp))
                                                    Column(modifier = Modifier.weight(1f)) {
                                                        Text(itemName, fontWeight = FontWeight.Bold, fontSize = 13.sp, color = TextSlate800)
                                                        Text("Total: ₦${String.format("%,.0f", totalAmt)}", fontSize = 10.sp, color = TextSlate500)
                                                    }
                                                    Text("$count Sold", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = PrimaryGreen)
                                                }
                                                Spacer(modifier = Modifier.height(6.dp))
                                                // Premium horizontal custom bar chart
                                                Box(
                                                    modifier = Modifier
                                                        .fillMaxWidth()
                                                        .height(8.dp)
                                                        .clip(RoundedCornerShape(4.dp))
                                                        .background(BorderSlate100)
                                                ) {
                                                    Box(
                                                        modifier = Modifier
                                                            .fillMaxHeight()
                                                            .fillMaxWidth(fraction)
                                                            .background(PrimaryGreen)
                                                    )
                                                }
                                            }
                                            Divider(color = Color(0xFFF1F5F9))
                                        }
                                    }
                                }
                            }
                        }

                        // Combined Chronological Trade Logs with ALTERNATING shading
                        item {
                            Text(
                                text = "Chronological Transaction History",
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp,
                                color = TextSlate800,
                                modifier = Modifier.padding(top = 8.dp)
                            )
                        }

                        val combinedHistory = (sellerSales.map {
                            HistoryItem(
                                isSale = true,
                                title = "Sale: ${it.itemName}",
                                subtitle = "Buyer: ${it.buyerName} • Month: ${it.month}",
                                amount = it.amount
                            )
                        } + sellerWithdrawals.map {
                            HistoryItem(
                                isSale = false,
                                title = "Withdrawal to ${it.bankName}",
                                subtitle = "Acc: ${it.accountNumber} • Processing",
                                amount = it.amount
                            )
                        })

                        if (combinedHistory.isEmpty()) {
                            item {
                                EmptyStateCard(
                                    message = "No trade logs registered",
                                    subMessage = "Sales and bank withdrawals will show up here chronologically.",
                                    icon = Icons.Default.History,
                                    iconColor = TextSlate400
                                )
                            }
                        } else {
                            item {
                                Card(
                                    colors = CardDefaults.cardColors(containerColor = SurfaceBg),
                                    shape = RoundedCornerShape(16.dp),
                                    border = BorderStroke(1.dp, BorderSlate100)
                                ) {
                                    Column {
                                        // Header Row
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .background(Color(0xFFF1F5F9))
                                                .padding(horizontal = 14.dp, vertical = 10.dp),
                                            horizontalArrangement = Arrangement.SpaceBetween
                                        ) {
                                            Text("TRANSACTION DETAILS", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = TextSlate500, modifier = Modifier.weight(1f))
                                            Text("AMOUNT", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = TextSlate500, textAlign = TextAlign.End)
                                        }

                                        // Data Rows with alternating backgrounds
                                        combinedHistory.forEachIndexed { idx, history ->
                                            val rowBg = if (idx % 2 == 0) Color.White else Color(0xFFF8FAFC)
                                            Row(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .background(rowBg)
                                                    .padding(horizontal = 14.dp, vertical = 12.dp),
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Box(
                                                    modifier = Modifier
                                                        .size(28.dp)
                                                        .clip(CircleShape)
                                                        .background(if (history.isSale) PrimaryGreen.copy(alpha = 0.08f) else SecondaryOrange.copy(alpha = 0.08f)),
                                                    contentAlignment = Alignment.Center
                                                ) {
                                                    Icon(
                                                        imageVector = if (history.isSale) Icons.Default.TrendingUp else Icons.Default.TrendingDown,
                                                        contentDescription = null,
                                                        tint = if (history.isSale) PrimaryGreen else SecondaryOrange,
                                                        modifier = Modifier.size(14.dp)
                                                    )
                                                }
                                                Spacer(modifier = Modifier.width(10.dp))
                                                Column(modifier = Modifier.weight(1f)) {
                                                    Text(history.title, fontWeight = FontWeight.Bold, fontSize = 12.sp, color = TextSlate800)
                                                    Text(history.subtitle, fontSize = 10.sp, color = TextSlate400)
                                                }
                                                Text(
                                                    text = "${if (history.isSale) "+" else "-"}₦${String.format("%,.0f", history.amount)}",
                                                    fontWeight = FontWeight.Bold,
                                                    fontSize = 13.sp,
                                                    color = if (history.isSale) PrimaryGreen else SecondaryOrange,
                                                    textAlign = TextAlign.End
                                                )
                                            }
                                            Divider(color = Color(0xFFF1F5F9))
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
                3 -> {
                    // --- BUYER REVIEWS TAB ---
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
                                colors = CardDefaults.cardColors(containerColor = SurfaceBg),
                                shape = RoundedCornerShape(16.dp),
                                border = BorderStroke(1.dp, BorderSlate100)
                            ) {
                                Column(modifier = Modifier.padding(16.dp)) {
                                    Text("CONFIDENTIAL FEEDBACK FROM BUYERS", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = TextSlate400, letterSpacing = 1.sp)
                                    Spacer(modifier = Modifier.height(14.dp))

                                    Column(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .background(Color(0xFFF8FAFC), RoundedCornerShape(12.dp))
                                            .border(1.dp, Color(0xFFE2E8F0), RoundedCornerShape(12.dp))
                                            .padding(18.dp),
                                        horizontalAlignment = Alignment.CenterHorizontally
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Lock,
                                            contentDescription = "Confidential Lock",
                                            tint = Color(0xFF64748B),
                                            modifier = Modifier.size(32.dp)
                                        )
                                        Spacer(modifier = Modifier.height(10.dp))
                                        Text(
                                            text = "Buyer Feedback is Encrypted & Private",
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 13.sp,
                                            color = TextSlate800
                                        )
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text(
                                            text = "To guarantee absolute shopping fairness on AfriSav, direct ratings and written comments left by buyers are kept strictly confidential from merchants.",
                                            fontSize = 11.sp,
                                            color = TextSlate500,
                                            textAlign = TextAlign.Center,
                                            lineHeight = 15.sp
                                        )
                                    }
                                }
                            }
                        }

                        // Section 2: Rate & Review Buyers form
                        item {
                            Card(
                                colors = CardDefaults.cardColors(containerColor = SurfaceBg),
                                shape = RoundedCornerShape(20.dp),
                                border = BorderStroke(1.dp, BorderSlate100)
                            ) {
                                Column(modifier = Modifier.padding(18.dp)) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(Icons.Default.RateReview, contentDescription = null, tint = PrimaryGreen, modifier = Modifier.size(18.dp))
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text("Rate & Review Your Buyers", fontWeight = FontWeight.Bold, fontSize = 15.sp, color = TextSlate800)
                                    }
                                    Text(
                                        "Help local farmers and bulk circles identify trusted and friendly shoppers.",
                                        fontSize = 11.sp,
                                        color = TextSlate400,
                                        modifier = Modifier.padding(top = 4.dp, bottom = 12.dp)
                                    )

                                    if (buyerReviewSuccess) {
                                        Box(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .background(Color(0xFFECFDF5), RoundedCornerShape(8.dp))
                                                .border(1.dp, Color(0xFFA7F3D0), RoundedCornerShape(8.dp))
                                                .padding(10.dp)
                                        ) {
                                            Text("Review submitted successfully! Thank you for your feedback.", fontSize = 11.sp, color = Color(0xFF065F46), fontWeight = FontWeight.Medium)
                                        }
                                        Spacer(modifier = Modifier.height(12.dp))
                                    }

                                    Text("Select Buyer to Rate", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = TextSlate500)
                                    Spacer(modifier = Modifier.height(4.dp))
                                    LazyRow(
                                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        items(buyerList) { buyer ->
                                            Box(
                                                modifier = Modifier
                                                    .clip(RoundedCornerShape(8.dp))
                                                    .background(if (selectedBuyerReview == buyer) PrimaryGreen else BgSlate50)
                                                    .border(1.dp, if (selectedBuyerReview == buyer) PrimaryGreen else BorderSlate100, RoundedCornerShape(8.dp))
                                                    .clickable { selectedBuyerReview = buyer }
                                                    .padding(horizontal = 12.dp, vertical = 6.dp)
                                            ) {
                                                Text(
                                                    text = buyer,
                                                    fontSize = 11.sp,
                                                    fontWeight = FontWeight.SemiBold,
                                                    color = if (selectedBuyerReview == buyer) Color.White else TextSlate500
                                                )
                                            }
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(12.dp))

                                    Text("Assign Rating stars", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = TextSlate500)
                                    Row(
                                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                                        modifier = Modifier.padding(vertical = 6.dp)
                                    ) {
                                        for (i in 1..5) {
                                            Icon(
                                                imageVector = Icons.Default.Star,
                                                contentDescription = "Star $i",
                                                tint = if (i <= buyerRating) SecondaryOrange else Color(0xFFCBD5E1),
                                                modifier = Modifier
                                                    .size(28.dp)
                                                    .clickable { buyerRating = i }
                                            )
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(10.dp))

                                    OutlinedTextField(
                                        value = buyerReviewComment,
                                        onValueChange = { buyerReviewComment = it },
                                        label = { Text("Buyer Feedback Comment") },
                                        placeholder = { Text("Friendly buyer, picked up foods without delay!") },
                                        modifier = Modifier.fillMaxWidth().testTag("seller_buyer_review_comment"),
                                        colors = defaultTextFieldColors(),
                                        shape = RoundedCornerShape(10.dp)
                                    )

                                    Spacer(modifier = Modifier.height(14.dp))

                                    Button(
                                        onClick = {
                                            if (buyerReviewComment.trim().isNotEmpty()) {
                                                viewModel.submitReviewRating(selectedBuyerReview, false, buyerRating, buyerReviewComment.trim())
                                                buyerReviewComment = ""
                                                buyerReviewSuccess = true
                                            }
                                        },
                                        colors = ButtonDefaults.buttonColors(containerColor = PrimaryGreen),
                                        shape = RoundedCornerShape(10.dp),
                                        modifier = Modifier.fillMaxWidth().testTag("seller_buyer_review_submit")
                                    ) {
                                        Text("Submit Buyer Review", fontWeight = FontWeight.Bold, color = Color.White)
                                    }
                                }
                            }
                        }

                        // Section 3: History of Left Reviews
                        if (buyersReviewedByMe.isNotEmpty()) {
                            item {
                                Text(
                                    text = "Reviews You've Left for Buyers",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 15.sp,
                                    color = TextSlate800,
                                    modifier = Modifier.padding(top = 8.dp)
                                )
                            }
                            items(buyersReviewedByMe) { rev ->
                                Card(
                                    colors = CardDefaults.cardColors(containerColor = SurfaceBg),
                                    shape = RoundedCornerShape(12.dp),
                                    border = BorderStroke(1.dp, BorderSlate100)
                                ) {
                                    Column(modifier = Modifier.padding(14.dp)) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text(rev.targetName, fontWeight = FontWeight.Bold, fontSize = 13.sp, color = TextSlate800)
                                            Row {
                                                for (i in 1..rev.rating) {
                                                    Icon(Icons.Default.Star, contentDescription = null, tint = SecondaryOrange, modifier = Modifier.size(12.dp))
                                                }
                                            }
                                        }
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text(rev.reviewText, fontSize = 12.sp, color = TextSlate500)
                                    }
                                }
                            }
                        }
                    }
                }
                4 -> {
                    // --- NEW SELLER PROFILE SCREEN ---
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
                                colors = CardDefaults.cardColors(containerColor = SurfaceBg),
                                shape = RoundedCornerShape(20.dp),
                                border = BorderStroke(1.dp, BorderSlate100)
                            ) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(20.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(72.dp)
                                            .clip(CircleShape)
                                            .border(1.5.dp, PrimaryGreen, CircleShape)
                                    ) {
                                        ProfileAvatar(
                                            imageUrl = null,
                                            role = "seller",
                                            modifier = Modifier.fillMaxSize()
                                        )
                                    }
                                    Spacer(modifier = Modifier.height(10.dp))
                                    Text(sellerName, fontWeight = FontWeight.Bold, fontSize = 17.sp, color = TextSlate800)
                                    Text("Verified Soko Merchant Partner", fontSize = 11.sp, color = TextSlate500)
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
                                    colors = CardDefaults.cardColors(containerColor = SurfaceBg),
                                    shape = RoundedCornerShape(12.dp),
                                    border = BorderStroke(1.dp, BorderSlate100),
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Column(
                                        modifier = Modifier.padding(12.dp),
                                        horizontalAlignment = Alignment.CenterHorizontally
                                    ) {
                                        Text("AVG RATING", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = TextSlate400)
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text("★ ${String.format("%.1f", avgRating)}", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = SecondaryOrange)
                                    }
                                }
                                Card(
                                    colors = CardDefaults.cardColors(containerColor = SurfaceBg),
                                    shape = RoundedCornerShape(12.dp),
                                    border = BorderStroke(1.dp, BorderSlate100),
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Column(
                                        modifier = Modifier.padding(12.dp),
                                        horizontalAlignment = Alignment.CenterHorizontally
                                    ) {
                                        Text("TOTAL PRODUCTS", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = TextSlate400)
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text("${sellerItems.size} items", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = PrimaryGreen)
                                    }
                                }
                                Card(
                                    colors = CardDefaults.cardColors(containerColor = SurfaceBg),
                                    shape = RoundedCornerShape(12.dp),
                                    border = BorderStroke(1.dp, BorderSlate100),
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Column(
                                        modifier = Modifier.padding(12.dp),
                                        horizontalAlignment = Alignment.CenterHorizontally
                                    ) {
                                        Text("TOTAL SALES", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = TextSlate400)
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text("${earnings.totalSalesCount} orders", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = TextSlate800)
                                    }
                                }
                            }
                        }

                        // Success Indicator
                        if (isSuccessMsgVisible) {
                            item {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .background(Color(0xFFECFDF5), RoundedCornerShape(8.dp))
                                        .border(1.dp, Color(0xFFA7F3D0), RoundedCornerShape(8.dp))
                                        .padding(12.dp)
                                ) {
                                    Text("Your Merchant Profile has been saved securely!", fontSize = 12.sp, color = Color(0xFF065F46), fontWeight = FontWeight.Bold)
                                }
                            }
                        }

                        // Editable Details Card
                        item {
                            Card(
                                colors = CardDefaults.cardColors(containerColor = SurfaceBg),
                                shape = RoundedCornerShape(20.dp),
                                border = BorderStroke(1.dp, BorderSlate100)
                            ) {
                                Column(modifier = Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                                    Text("EDIT MERCHANT DETAILS", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = TextSlate400, letterSpacing = 1.sp)
                                    
                                    OutlinedTextField(
                                        value = name,
                                        onValueChange = { name = it },
                                        label = { Text("Business/Store Name") },
                                        singleLine = true,
                                        modifier = Modifier.fillMaxWidth().testTag("seller_profile_name"),
                                        colors = defaultTextFieldColors(),
                                        shape = RoundedCornerShape(10.dp)
                                    )

                                    OutlinedTextField(
                                        value = phone,
                                        onValueChange = { phone = it },
                                        label = { Text("Contact Phone Number") },
                                        singleLine = true,
                                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                                        modifier = Modifier.fillMaxWidth().testTag("seller_profile_phone"),
                                        colors = defaultTextFieldColors(),
                                        shape = RoundedCornerShape(10.dp)
                                    )

                                    OutlinedTextField(
                                        value = email,
                                        onValueChange = { email = it },
                                        label = { Text("Store Email Address") },
                                        singleLine = true,
                                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                                        modifier = Modifier.fillMaxWidth().testTag("seller_profile_email"),
                                        colors = defaultTextFieldColors(),
                                        shape = RoundedCornerShape(10.dp)
                                    )

                                    OutlinedTextField(
                                        value = location,
                                        onValueChange = { location = it },
                                        label = { Text("Business Location LGA") },
                                        singleLine = true,
                                        modifier = Modifier.fillMaxWidth().testTag("seller_profile_location"),
                                        colors = defaultTextFieldColors(),
                                        shape = RoundedCornerShape(10.dp)
                                    )

                                    OutlinedTextField(
                                        value = bio,
                                        onValueChange = { bio = it },
                                        label = { Text("Merchant Store Bio") },
                                        modifier = Modifier.fillMaxWidth().testTag("seller_profile_bio"),
                                        colors = defaultTextFieldColors(),
                                        shape = RoundedCornerShape(10.dp)
                                    )
                                }
                            }
                        }

                        // Security Card
                        item {
                            Card(
                                colors = CardDefaults.cardColors(containerColor = SurfaceBg),
                                shape = RoundedCornerShape(20.dp),
                                border = BorderStroke(1.dp, BorderSlate100)
                            ) {
                                Column(modifier = Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                                    Text("ACCOUNT SECURITY & PASSWORD", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = TextSlate400, letterSpacing = 1.sp)
                                    
                                    OutlinedTextField(
                                        value = pin,
                                        onValueChange = { pin = it },
                                        label = { Text("Merchant Security PIN") },
                                        singleLine = true,
                                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                                        visualTransformation = PasswordVisualTransformation(),
                                        modifier = Modifier.fillMaxWidth().testTag("seller_profile_pin"),
                                        colors = defaultTextFieldColors(),
                                        shape = RoundedCornerShape(10.dp)
                                    )
                                }
                            }
                        }

                        // Submit & Logout buttons
                        item {
                            Button(
                                onClick = {
                                    if (name.isNotEmpty() && phone.isNotEmpty()) {
                                        viewModel.updateUserProfile(name, phone, email, location, bio)
                                        if (pin.isNotEmpty()) {
                                            viewModel.updateSellerPin(pin)
                                        }
                                        isSuccessMsgVisible = true
                                    }
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = PrimaryGreen),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.fillMaxWidth().height(48.dp).testTag("seller_profile_save")
                            ) {
                                Text("Save Profile Changes", fontWeight = FontWeight.Bold, color = Color.White)
                            }
                        }

                        item {
                            OutlinedButton(
                                onClick = onLogoutClick,
                                modifier = Modifier.fillMaxWidth().height(48.dp).testTag("seller_profile_logout"),
                                colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFFEF4444)),
                                border = BorderStroke(1.dp, Color(0xFFEF4444)),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Text("Sign Out of Soko Account", fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }
    }

    // Modal Withdrawal Dialog Triggered via available balances
    if (showSellerWithdrawConfirmDialog) {
        AlertDialog(
            onDismissRequest = { showSellerWithdrawConfirmDialog = false },
            title = { Text("Secure Settlement Transfer 🔐", fontSize = 16.sp, fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        text = "Transfer earnings instantly to your verified bank account.",
                        fontSize = 12.sp,
                        color = TextSlate500
                    )

                    OutlinedTextField(
                        value = withdrawAmountStr,
                        onValueChange = { withdrawAmountStr = it },
                        label = { Text("Withdrawal Amount (₦)") },
                        placeholder = { Text("e.g. 10000") },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth().testTag("seller_withdraw_amt"),
                        colors = defaultTextFieldColors(),
                        shape = RoundedCornerShape(8.dp)
                    )

                    Text("Destination Bank", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = TextSlate500)
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        items(banks) { b ->
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(if (selectedBank == b) SecondaryOrange else BgSlate50)
                                    .border(1.dp, if (selectedBank == b) SecondaryOrange else BorderSlate100, RoundedCornerShape(8.dp))
                                    .clickable { selectedBank = b }
                                    .padding(horizontal = 12.dp, vertical = 6.dp)
                            ) {
                                Text(
                                    text = b,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = if (selectedBank == b) Color.White else TextSlate500
                                )
                            }
                        }
                    }

                    OutlinedTextField(
                        value = withdrawAccountStr,
                        onValueChange = { withdrawAccountStr = it },
                        label = { Text("10-Digit Account Number") },
                        placeholder = { Text("e.g. 0123456789") },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth().testTag("seller_withdraw_account"),
                        colors = defaultTextFieldColors(),
                        shape = RoundedCornerShape(8.dp)
                    )

                    if (withdrawErrorText.isNotEmpty()) {
                        Text(
                            text = withdrawErrorText,
                            color = MaterialTheme.colorScheme.error,
                            fontSize = 11.sp
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
                            withdrawAmountStr = ""
                            withdrawAccountStr = ""
                            withdrawErrorText = ""
                            showSellerWithdrawConfirmDialog = false
                            Toast.makeText(context, "Withdrawal Transfer Executed!", Toast.LENGTH_SHORT).show()
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = SecondaryOrange)
                ) {
                    Text("Confirm Transfer", color = Color.White)
                }
            },
            dismissButton = {
                TextButton(onClick = { 
                    showSellerWithdrawConfirmDialog = false 
                    withdrawErrorText = ""
                }) {
                    Text("Cancel")
                }
            }
        )
    }
}
