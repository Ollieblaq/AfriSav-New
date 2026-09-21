package com.example.ui

import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.*
import androidx.compose.foundation.lazy.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.*
import com.example.ui.theme.*

data class RiderHistoryItem(
    val dateTime: String,
    val pickup: String,
    val dropoff: String,
    val status: String,
    val earnings: Double
)

data class RiderReviewItem(
    val reviewer: String,
    val rating: Int,
    val comment: String,
    val date: String
)

@Composable
fun RiderBottomNavBar(
    activeTab: String,
    onTabSelected: (String) -> Unit
) {
    Column {
        Divider(color = BorderSlate100, thickness = 1.dp)
        NavigationBar(
            containerColor = SurfaceBg,
            tonalElevation = 0.dp,
            modifier = Modifier.navigationBarsPadding()
        ) {
            NavigationBarItem(
                selected = activeTab == "dashboard",
                onClick = { onTabSelected("dashboard") },
                icon = { Icon(Icons.Default.Home, contentDescription = "Dashboard") },
                label = { Text("Dashboard", fontSize = 11.sp, fontWeight = FontWeight.Medium) },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = PrimaryGreen,
                    selectedTextColor = PrimaryGreen,
                    unselectedIconColor = TextSlate400,
                    unselectedTextColor = TextSlate400,
                    indicatorColor = PrimaryGreen.copy(alpha = 0.12f)
                )
            )
            NavigationBarItem(
                selected = activeTab == "history",
                onClick = { onTabSelected("history") },
                icon = { Icon(Icons.Default.History, contentDescription = "History") },
                label = { Text("History", fontSize = 11.sp, fontWeight = FontWeight.Medium) },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = PrimaryGreen,
                    selectedTextColor = PrimaryGreen,
                    unselectedIconColor = TextSlate400,
                    unselectedTextColor = TextSlate400,
                    indicatorColor = PrimaryGreen.copy(alpha = 0.12f)
                )
            )
            NavigationBarItem(
                selected = activeTab == "reviews",
                onClick = { onTabSelected("reviews") },
                icon = { Icon(Icons.Default.Star, contentDescription = "Reviews") },
                label = { Text("Reviews", fontSize = 11.sp, fontWeight = FontWeight.Medium) },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = PrimaryGreen,
                    selectedTextColor = PrimaryGreen,
                    unselectedIconColor = TextSlate400,
                    unselectedTextColor = TextSlate400,
                    indicatorColor = PrimaryGreen.copy(alpha = 0.12f)
                )
            )
            NavigationBarItem(
                selected = activeTab == "profile",
                onClick = { onTabSelected("profile") },
                icon = { Icon(Icons.Default.Person, contentDescription = "Profile") },
                label = { Text("Profile", fontSize = 11.sp, fontWeight = FontWeight.Medium) },
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
}

@Composable
fun RiderDashboardContent(
    viewModel: KoboViewModel,
    riderName: String,
    riderPhone: String,
    isOnline: Boolean,
    onOnlineToggle: (Boolean) -> Unit,
    todayEarnings: Double,
    completedTrips: Int,
    deliveries: List<RiderDeliveryItem>,
    onDeliveriesChange: (List<RiderDeliveryItem>) -> Unit,
    realDeliveries: List<RiderDeliveryItem>,
    riderType: String,
    dispatchCompany: String,
    riderBalance: Double,
    withdrawals: List<RiderWithdrawal>,
    activeDeliveriesInProgress: List<RiderDeliveryItem>,
    onActionClick: (RiderDeliveryItem) -> Unit,
    allReviews: List<ReviewRating>,
    onLogoutClick: () -> Unit,
    onNavigateToTab: (String) -> Unit
) {
    val scrollState = rememberScrollState()
    
    // Withdrawal states
    var showWithdrawalDialog by remember { mutableStateOf(false) }
    var withdrawAmount by remember { mutableStateOf("") }
    var bankName by remember { mutableStateOf("") }
    var accountNumber by remember { mutableStateOf("") }
    var withdrawError by remember { mutableStateOf("") }
    var showConfirmationDialog by remember { mutableStateOf(false) }

    // Aggregate Star Rating
    val ratingReviews = allReviews.filter { it.targetName.equals(riderName, ignoreCase = true) }
    val averageRating = if (ratingReviews.isEmpty()) 4.9 else ratingReviews.map { it.rating }.average()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
    ) {
        // Welcoming Premium Header Card
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFF0F172A))
                .statusBarsPadding()
                .padding(top = 20.dp, start = 20.dp, end = 20.dp, bottom = 28.dp)
        ) {
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .clip(CircleShape)
                                .background(PrimaryGreen.copy(alpha = 0.2f))
                                .clickable { onNavigateToTab("profile") }
                                .testTag("rider_avatar")
                        ) {
                            ProfileAvatar(
                                imageUrl = null,
                                role = "rider",
                                modifier = Modifier.fillMaxSize()
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = "Welcome back,",
                                fontSize = 12.sp,
                                color = Color(0xFF94A3B8)
                            )
                            Text(
                                text = riderName.ifEmpty { "AfriSav Rider" },
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }
                    }

                    // Online Pill Badge
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(20.dp))
                            .background(if (isOnline) Color(0xFF15803D) else Color(0xFFDC2626))
                            .padding(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .background(Color.White, CircleShape)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = if (isOnline) "ONLINE" else "OFFLINE",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))
                
                Text(
                    text = if (riderType == "Company") "🏢 Company Rider Assigned • $dispatchCompany" else "🚴 Independent Freelance Dispatcher",
                    fontSize = 12.sp,
                    color = Color(0xFF38BDF8),
                    fontWeight = FontWeight.SemiBold
                )
            }
        }

        // Online Toggle Strip
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            colors = CardDefaults.cardColors(containerColor = SurfaceBg),
            shape = RoundedCornerShape(12.dp),
            border = BorderStroke(1.dp, Color(0xFFE2E8F0))
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 14.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(
                        imageVector = if (isOnline) Icons.Default.DirectionsBike else Icons.Default.Warning,
                        contentDescription = null,
                        tint = if (isOnline) PrimaryGreen else Color(0xFFEF4444)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = "Availability Status",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF1E293B)
                        )
                        Text(
                            text = if (isOnline) "Receiving market orders" else "Not receiving requests",
                            fontSize = 11.sp,
                            color = Color(0xFF64748B)
                        )
                    }
                }
                Spacer(modifier = Modifier.width(8.dp))
                Switch(
                    checked = isOnline,
                    onCheckedChange = { onOnlineToggle(it) },
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = Color.White,
                        checkedTrackColor = PrimaryGreen,
                        uncheckedThumbColor = Color.White,
                        uncheckedTrackColor = Color(0xFFCBD5E1)
                    ),
                    modifier = Modifier.testTag("online_offline_toggle")
                )
            }
        }

        // Active Tasks Section (Steps Tracker UI)
        if (activeDeliveriesInProgress.isNotEmpty()) {
            Text(
                text = "ACTIVE TASKS",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF64748B),
                modifier = Modifier.padding(start = 18.dp, top = 8.dp, bottom = 4.dp)
            )

            activeDeliveriesInProgress.forEach { item ->
                val isInTransit = item.status == "In Transit"
                val isCompleted = item.status == "Delivered"

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 6.dp)
                        .testTag("active_delivery_card_${item.id}"),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFFFFBEB)), // Cream Amber Background
                    border = BorderStroke(1.5.dp, Color(0xFFFDE68A)),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.Top
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = item.item,
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = Color(0xFF78350F)
                                )
                                Text(
                                    text = "Payout: ₦${String.format("%,.2f", item.fee)}",
                                    fontSize = 13.sp,
                                    color = Color(0xFFB45309),
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(Color(0xFFFEF3C7))
                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                Text(
                                    text = item.status.uppercase(),
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFFD97706)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        // Route Addresses Info
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(18.dp)
                                        .background(Color(0xFFDEF7EC), CircleShape),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text("🏪", fontSize = 10.sp)
                                }
                                Spacer(modifier = Modifier.width(8.dp))
                                Column {
                                    Text("PICKUP FROM", fontSize = 8.sp, fontWeight = FontWeight.Bold, color = Color(0xFF92400E))
                                    Text(item.vendor, fontSize = 11.sp, color = Color(0xFF78350F))
                                }
                            }
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(18.dp)
                                        .background(Color(0xFFE1EFFE), CircleShape),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text("📍", fontSize = 10.sp)
                                }
                                Spacer(modifier = Modifier.width(8.dp))
                                Column {
                                    Text("DELIVER TO", fontSize = 8.sp, fontWeight = FontWeight.Bold, color = Color(0xFF92400E))
                                    Text(item.buyer, fontSize = 11.sp, color = Color(0xFF78350F))
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(18.dp))

                        // Modern Interactive Steps Tracker (Visual Line + Circles)
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Step 1: Accepted
                            Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.weight(1f)) {
                                Box(
                                    modifier = Modifier
                                        .size(24.dp)
                                        .background(PrimaryGreen, CircleShape),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(Icons.Default.Check, null, tint = Color.White, modifier = Modifier.size(12.dp))
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                                Text("Accepted", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = Color(0xFF78350F))
                            }

                            // Connecting Line 1
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .height(3.dp)
                                    .background(if (isInTransit || isCompleted) PrimaryGreen else Color(0xFFCBD5E1))
                            )

                            // Step 2: In Transit
                            Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.weight(1f)) {
                                Box(
                                    modifier = Modifier
                                        .size(24.dp)
                                        .background(if (isInTransit || isCompleted) PrimaryGreen else Color(0xFFCBD5E1), CircleShape),
                                    contentAlignment = Alignment.Center
                                ) {
                                    if (isCompleted) {
                                        Icon(Icons.Default.Check, null, tint = Color.White, modifier = Modifier.size(12.dp))
                                    } else {
                                        Text("2", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color.White)
                                    }
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                                Text("In Transit", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = Color(0xFF78350F))
                            }

                            // Connecting Line 2
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .height(3.dp)
                                    .background(if (isCompleted) PrimaryGreen else Color(0xFFCBD5E1))
                            )

                            // Step 3: Delivered
                            Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.weight(1f)) {
                                Box(
                                    modifier = Modifier
                                        .size(24.dp)
                                        .background(if (isCompleted) PrimaryGreen else Color(0xFFCBD5E1), CircleShape),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text("3", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color.White)
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                                Text("Delivered", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = Color(0xFF78350F))
                            }
                        }

                        Spacer(modifier = Modifier.height(20.dp))

                        // Large standard action touch target buttons (min 48dp height)
                        if (!isInTransit) {
                            Button(
                                onClick = { onActionClick(item) },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(48.dp)
                                    .testTag("pickup_cargo_button_${item.id}"),
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEA580C)),
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.LockOpen, null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("Pick Up Cargo (Requires PIN)", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                }
                            }
                        } else {
                            Button(
                                onClick = { onActionClick(item) },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(48.dp)
                                    .testTag("mark_delivered_button_${item.id}"),
                                colors = ButtonDefaults.buttonColors(containerColor = PrimaryGreen),
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.CheckCircle, null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("Mark Delivered (Requires PIN)", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                }
                            }
                        }
                    }
                }
            }
        }

        // Quick Stats Row Cards
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Card(
                modifier = Modifier
                    .weight(1f)
                    .clickable { onNavigateToTab("history") },
                colors = CardDefaults.cardColors(containerColor = SurfaceBg),
                border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text("Today's Earnings", fontSize = 10.sp, color = Color(0xFF64748B), fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "₦${String.format("%,.0f", todayEarnings)}",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = PrimaryGreen
                    )
                }
            }

            Card(
                modifier = Modifier
                    .weight(0.9f)
                    .clickable { onNavigateToTab("history") },
                colors = CardDefaults.cardColors(containerColor = SurfaceBg),
                border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text("Completed Rides", fontSize = 10.sp, color = Color(0xFF64748B), fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "$completedTrips Trips",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color(0xFF0F172A)
                    )
                }
            }

            Card(
                modifier = Modifier
                    .weight(0.9f)
                    .clickable { onNavigateToTab("reviews") },
                colors = CardDefaults.cardColors(containerColor = SurfaceBg),
                border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text("Rider Rating", fontSize = 10.sp, color = Color(0xFF64748B), fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Star, null, tint = SecondaryOrange, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = String.format("%.1f", averageRating),
                            fontSize = 17.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color(0xFF0F172A)
                        )
                    }
                }
            }
        }

        // Quick Tab Navigation Row
        Text(
            text = "QUICK NAVIGATION",
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF64748B),
            modifier = Modifier.padding(start = 18.dp, top = 14.dp, bottom = 4.dp)
        )
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Button(
                onClick = { onNavigateToTab("history") },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE2E8F0), contentColor = Color(0xFF334155)),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier
                    .weight(1f)
                    .height(40.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.History, null, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("History", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
            }

            Button(
                onClick = { onNavigateToTab("reviews") },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE2E8F0), contentColor = Color(0xFF334155)),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier
                    .weight(1f)
                    .height(40.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Star, null, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Reviews", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
            }

            Button(
                onClick = { onNavigateToTab("profile") },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE2E8F0), contentColor = Color(0xFF334155)),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier
                    .weight(1f)
                    .height(40.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Person, null, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Profile", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
            }
        }

        // Wallet & Withdrawal Section
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            colors = CardDefaults.cardColors(containerColor = SurfaceBg),
            shape = RoundedCornerShape(12.dp),
            border = BorderStroke(1.dp, Color(0xFFE2E8F0))
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "MY WALLET BALANCE",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF64748B)
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "₦${String.format("%,.2f", riderBalance)}",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color(0xFF0F172A)
                )

                Spacer(modifier = Modifier.height(14.dp))

                if (riderType == "Individual") {
                    Button(
                        onClick = { showWithdrawalDialog = true },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(44.dp)
                            .testTag("request_withdrawal_btn"),
                        colors = ButtonDefaults.buttonColors(containerColor = PrimaryGreen),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text("Secure Wallet Withdrawal 🔐", fontWeight = FontWeight.Bold)
                    }
                } else {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.Lock, null, tint = Color(0xFFEF4444), modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Payouts managed securely by dispatch company ($dispatchCompany).",
                            fontSize = 11.sp,
                            color = Color(0xFFEF4444),
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }
        }

        // Logistics Available Queue
        Text(
            text = "LOGISTICS ORDER QUEUE",
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF64748B),
            modifier = Modifier.padding(start = 18.dp, top = 8.dp, bottom = 4.dp)
        )

        if (!isOnline) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 6.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFFEF2F2)),
                border = BorderStroke(1.dp, Color(0xFFFCA5A5)),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text("⚠️ You are currently Offline", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color(0xFF991B1B))
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Turn on your availability status toggle above to view and accept active food delivery jobs in your area.",
                        fontSize = 11.sp,
                        color = Color(0xFF991B1B),
                        textAlign = TextAlign.Center
                    )
                }
            }
        } else {
            // Online available jobs
            val availableJobs = (realDeliveries + deliveries.filter { d -> d.status == "Available" && realDeliveries.none { r -> r.id == d.id } })
                .filter { it.status == "Available" }

            if (availableJobs.isEmpty()) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 6.dp),
                    colors = CardDefaults.cardColors(containerColor = SurfaceBg),
                    border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text("🔍 Scanning for Orders...", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color(0xFF475569))
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "No new logistics delivery requests available at the moment. Please wait for vendors to list shipments.",
                            fontSize = 11.sp,
                            color = Color(0xFF64748B),
                            textAlign = TextAlign.Center
                        )
                    }
                }
            } else {
                availableJobs.forEach { item ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 6.dp)
                            .testTag("available_order_card_${item.id}"),
                        colors = CardDefaults.cardColors(containerColor = SurfaceBg),
                        border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.Top
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = item.item,
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFF1E293B)
                                    )
                                    Text(
                                        text = "Base Offer: ₦${String.format("%,.0f", item.baseFee)}",
                                        fontSize = 11.sp,
                                        color = Color(0xFF64748B)
                                    )
                                }
                                Text(
                                    text = "₦${String.format("%,.0f", item.fee)}",
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = PrimaryGreen
                                )
                            }

                            Spacer(modifier = Modifier.height(10.dp))
                            Divider(color = Color(0xFFF1F5F9))
                            Spacer(modifier = Modifier.height(10.dp))

                            // Route details
                            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                Text(
                                    text = "🏪 FROM: ${item.vendor}",
                                    fontSize = 11.sp,
                                    color = Color(0xFF334155),
                                    fontWeight = FontWeight.Medium
                                )
                                Text(
                                    text = "📍 TO: ${item.buyer}",
                                    fontSize = 11.sp,
                                    color = Color(0xFF334155),
                                    fontWeight = FontWeight.Medium
                                )
                            }

                            Spacer(modifier = Modifier.height(14.dp))

                            // Interactive Bidding / Accept Section
                            if (item.riderProposalName == riderName) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(Color(0xFFEFF6FF))
                                        .padding(10.dp)
                                ) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = "Waiting for approval (Bid: ₦${String.format("%,.0f", item.riderProposedFee)})",
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.SemiBold,
                                            color = Color(0xFF1D4ED8)
                                        )
                                        TextButton(
                                            onClick = {
                                                if (item.isReal) {
                                                    viewModel.proposeRiderFee(item.id, 0.0) // withdraw bid
                                                } else {
                                                    onDeliveriesChange(deliveries.map { d ->
                                                        if (d.id == item.id) d.copy(riderProposedFee = 0.0, riderProposalName = null) else d
                                                    })
                                                }
                                            }
                                        ) {
                                            Text("Withdraw", color = Color.Red, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                        }
                                    }
                                }
                            } else {
                                // Bid & Accept Option Action Buttons (Generous spacing, 48dp targets)
                                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                    // Quick Bid Suggestion Buttons
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        listOf(150, 300, 500).forEach { addAmt ->
                                            val suggestFee = item.fee + addAmt
                                            Button(
                                                onClick = {
                                                    if (item.isReal) {
                                                        viewModel.proposeRiderFee(item.id, suggestFee)
                                                    } else {
                                                        onDeliveriesChange(deliveries.map { d ->
                                                            if (d.id == item.id) d.copy(riderProposedFee = suggestFee, riderProposalName = riderName) else d
                                                        })
                                                    }
                                                },
                                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFF1F5F9), contentColor = Color(0xFF475569)),
                                                shape = RoundedCornerShape(6.dp),
                                                modifier = Modifier.weight(1f).height(36.dp)
                                            ) {
                                                Text("+₦$addAmt", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                            }
                                        }
                                    }

                                    // Primary Actions (Accept, Custom Bid)
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        var customBidText by remember { mutableStateOf("") }
                                        OutlinedTextField(
                                            value = customBidText,
                                            onValueChange = { customBidText = it.filter { c -> c.isDigit() } },
                                            placeholder = { Text("Custom Bid ₦") },
                                            singleLine = true,
                                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                            modifier = Modifier.weight(1f).height(48.dp),
                                            colors = defaultTextFieldColors(),
                                            shape = RoundedCornerShape(8.dp)
                                        )

                                        Button(
                                            onClick = {
                                                val customAmt = customBidText.toDoubleOrNull() ?: 0.0
                                                if (customAmt > 0) {
                                                    if (item.isReal) {
                                                        viewModel.proposeRiderFee(item.id, customAmt)
                                                    } else {
                                                        onDeliveriesChange(deliveries.map { d ->
                                                            if (d.id == item.id) d.copy(riderProposedFee = customAmt, riderProposalName = riderName) else d
                                                        })
                                                    }
                                                }
                                            },
                                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF334155)),
                                            shape = RoundedCornerShape(8.dp),
                                            modifier = Modifier.height(48.dp)
                                        ) {
                                            Text("Send Bid", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                        }

                                        Button(
                                            onClick = {
                                                if (item.isReal) {
                                                    viewModel.acceptRiderJob(item.id)
                                                } else {
                                                    onDeliveriesChange(deliveries.map { d ->
                                                        if (d.id == item.id) d.copy(status = "Accepted") else d
                                                    })
                                                }
                                            },
                                            colors = ButtonDefaults.buttonColors(containerColor = PrimaryGreen),
                                            shape = RoundedCornerShape(8.dp),
                                            modifier = Modifier.weight(1.2f).height(48.dp).testTag("accept_order_btn_${item.id}")
                                        ) {
                                            Text("Accept Job", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // Withdrawal secure dialog flows (exact preservation from original)
    if (showWithdrawalDialog) {
        if (!showConfirmationDialog) {
            AlertDialog(
                onDismissRequest = { showWithdrawalDialog = false },
                title = { Text("Request Fund Withdrawal 🚴", fontSize = 16.sp, fontWeight = FontWeight.Bold) },
                text = {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Text(
                            text = "Enter your bank transfer credentials to securely withdraw funds from your AfriSav rider wallet.",
                            fontSize = 12.sp,
                            color = Color(0xFF64748B)
                        )

                        OutlinedTextField(
                            value = withdrawAmount,
                            onValueChange = { withdrawAmount = it.filter { c -> c.isDigit() } },
                            label = { Text("Amount (₦)") },
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.fillMaxWidth().testTag("withdraw_amount_input"),
                            colors = defaultTextFieldColors()
                        )

                        OutlinedTextField(
                            value = bankName,
                            onValueChange = { bankName = it },
                            label = { Text("Bank Name") },
                            placeholder = { Text("e.g. GTBank, Kuda, Zenith") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth().testTag("withdraw_bank_input"),
                            colors = defaultTextFieldColors()
                        )

                        OutlinedTextField(
                            value = accountNumber,
                            onValueChange = { accountNumber = it.filter { c -> c.isDigit() } },
                            label = { Text("Account Number") },
                            placeholder = { Text("10-Digit Account No.") },
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.fillMaxWidth().testTag("withdraw_account_input"),
                            colors = defaultTextFieldColors()
                        )

                        if (withdrawError.isNotEmpty()) {
                            Text(withdrawError, color = Color.Red, fontSize = 11.sp)
                        }
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            val amount = withdrawAmount.toDoubleOrNull() ?: 0.0
                            if (amount <= 0) {
                                withdrawError = "Please enter a valid amount."
                            } else if (amount > riderBalance) {
                                withdrawError = "Insufficient wallet balance (Max: ₦${String.format("%,.0f", riderBalance)})"
                            } else if (bankName.trim().isEmpty()) {
                                withdrawError = "Please enter bank name."
                            } else if (accountNumber.length != 10) {
                                withdrawError = "Account number must be exactly 10 digits."
                            } else {
                                showConfirmationDialog = true
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = PrimaryGreen),
                        modifier = Modifier.testTag("submit_withdraw_request")
                    ) {
                        Text("Withdraw")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showWithdrawalDialog = false }) {
                        Text("Cancel")
                    }
                }
            )
        } else {
            AlertDialog(
                onDismissRequest = { showConfirmationDialog = false },
                title = { Text("Confirm Withdrawal Transfer 🔐", fontSize = 16.sp, fontWeight = FontWeight.Bold) },
                text = {
                    Column {
                        Text(
                            text = "Please verify the transfer details below before executing this withdrawal:",
                            fontSize = 12.sp,
                            color = Color(0xFF64748B)
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(Color(0xFFF1F5F9), RoundedCornerShape(8.dp))
                                .padding(12.dp)
                        ) {
                            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                    Text("Withdrawal Amount:", fontSize = 11.sp, color = Color(0xFF64748B))
                                    Text("₦${String.format("%,.2f", withdrawAmount.toDoubleOrNull() ?: 0.0)}", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                }
                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                    Text("Recipient Bank:", fontSize = 11.sp, color = Color(0xFF64748B))
                                    Text(bankName, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                }
                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                    Text("Account Number:", fontSize = 11.sp, color = Color(0xFF64748B))
                                    Text(accountNumber, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            val amount = withdrawAmount.toDoubleOrNull() ?: 0.0
                            viewModel.withdrawRiderFunds(amount, bankName, accountNumber, riderName, "Rider")
                            showConfirmationDialog = false
                            showWithdrawalDialog = false
                            withdrawAmount = ""
                            bankName = ""
                            accountNumber = ""
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = PrimaryGreen),
                        modifier = Modifier.testTag("confirm_transfer_button")
                    ) {
                        Text("Confirm Transfer")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showConfirmationDialog = false }) {
                        Text("Back")
                    }
                }
            )
        }
    }
}

@Composable
fun RiderHistoryContent(
    riderName: String,
    allEscrowOrders: List<EscrowOrder>
) {
    // Simulated historical ledger data so fresh accounts also show beautiful profiles
    val simulatedHistory = remember {
        listOf(
            RiderHistoryItem("2026-07-18T14:30:00", "Mile 12 Potato Depot", "Tolani Alabi (Surulere)", "Completed", 1950.0),
            RiderHistoryItem("2026-07-18T10:15:00", "Salami Stores (Mushin)", "Bisi Adeoye (Gbagada)", "Completed", 2500.0),
            RiderHistoryItem("2026-07-17T16:45:00", "Mama Ngozi Rice Stall", "Efe Johnson (Lekki)", "Completed", 3200.0),
            RiderHistoryItem("2026-07-16T12:00:00", "Ibrahim Agro (Mile 12)", "Adebanjo Williams (Yaba)", "Completed", 1800.0),
            RiderHistoryItem("2026-07-15T15:30:00", "Oyingbo Modern Market", "Chidi Okafor (Ikeja)", "Completed", 2200.0),
            RiderHistoryItem("2026-07-14T11:00:00", "Salami Stores (Mushin)", "Tayo Animashaun (Maryland)", "Cancelled", 0.0),
            RiderHistoryItem("2026-07-13T09:30:00", "Mile 12 Agricultural Market", "Kemi Shonibare (Ikoyi)", "Completed", 3500.0),
            RiderHistoryItem("2026-07-12T14:00:00", "Oyingbo Yam Section", "Fatima Alao (Apapa)", "Completed", 2800.0),
            RiderHistoryItem("2026-07-11T16:20:00", "Alaba Frozen Foods", "Gift Nwachukwu (Victoria Island)", "Completed", 3000.0),
            RiderHistoryItem("2026-07-10T11:45:00", "Mama Ejima (Oyingbo)", "Abiodun Shittu (Magodo)", "Completed", 2400.0)
        )
    }

    // Dynamic completed orders mapping
    val dynamicHistory = allEscrowOrders
        .filter { it.riderName.equals(riderName, ignoreCase = true) && it.status == "COMPLETED" }
        .map { esc ->
            val formattedDate = try {
                java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", java.util.Locale.getDefault()).format(java.util.Date(esc.timestamp))
            } catch (e: Exception) {
                "2026-07-19T08:00:00"
            }
            RiderHistoryItem(
                dateTime = formattedDate,
                pickup = esc.vendorName,
                dropoff = "${esc.buyerName} (Buyer)",
                status = "Completed",
                earnings = esc.deliveryFee
            )
        }

    val combinedHistory = dynamicHistory + simulatedHistory

    // Filter and Sort states
    var statusFilter by remember { mutableStateOf("All") } // "All", "Completed", "Cancelled"
    var sortBy by remember { mutableStateOf("Newest First") } // "Newest First", "Oldest First", "Highest Earnings"
    var visibleCount by remember { mutableStateOf(5) } // Pagination state limit

    val filteredSortedHistory = combinedHistory
        .filter { item ->
            when (statusFilter) {
                "Completed" -> item.status == "Completed"
                "Cancelled" -> item.status == "Cancelled"
                else -> true
            }
        }
        .sortedWith { a, b ->
            when (sortBy) {
                "Oldest First" -> a.dateTime.compareTo(b.dateTime)
                "Highest Earnings" -> b.earnings.compareTo(a.earnings)
                else -> b.dateTime.compareTo(a.dateTime) // Default Newest First
            }
        }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .padding(start = 16.dp, end = 16.dp, top = 20.dp, bottom = 24.dp)
    ) {
        Text(
            text = "📁 My Delivery History",
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF0F172A)
        )
        Text(
            text = "Detailed audit ledger of past food dispatches and completed runs.",
            fontSize = 12.sp,
            color = Color(0xFF64748B)
        )

        Spacer(modifier = Modifier.height(14.dp))

        // Filtering chips row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            listOf("All", "Completed", "Cancelled").forEach { opt ->
                val isSel = statusFilter == opt
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(20.dp))
                        .background(if (isSel) PrimaryGreen else Color(0xFFE2E8F0))
                        .clickable { statusFilter = opt }
                        .padding(horizontal = 14.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = opt,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isSel) Color.White else Color(0xFF475569)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Sorting Option chip selection Row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Sort:", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFF64748B))
            listOf("Newest First", "Oldest First", "Highest Earnings").forEach { sortOpt ->
                val isSel = sortBy == sortOpt
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(if (isSel) Color(0xFF0F172A) else Color(0xFFF1F5F9))
                        .clickable { sortBy = sortOpt }
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = sortOpt,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isSel) Color.White else Color(0xFF475569)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // History Scrollable List
        LazyColumn(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            val visibleHistory = filteredSortedHistory.take(visibleCount)
            
            if (visibleHistory.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 40.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "No history entries found matching criteria.",
                            color = Color(0xFF64748B),
                            fontSize = 12.sp,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            } else {
                items(visibleHistory) { item ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = SurfaceBg),
                        border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    val formattedDate = try {
                                        val parseDate = java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", java.util.Locale.getDefault()).parse(item.dateTime)
                                        java.text.SimpleDateFormat("MMM dd, yyyy • hh:mm a", java.util.Locale.getDefault()).format(parseDate)
                                    } catch (e: Exception) {
                                        item.dateTime
                                    }
                                    Text(
                                        text = formattedDate,
                                        fontSize = 10.sp,
                                        color = Color(0xFF94A3B8),
                                        fontWeight = FontWeight.SemiBold
                                    )
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        text = "From: ${item.pickup}",
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFF1E293B)
                                    )
                                    Text(
                                        text = "To: ${item.dropoff}",
                                        fontSize = 11.sp,
                                        color = Color(0xFF64748B)
                                    )
                                }

                                Column(horizontalAlignment = Alignment.End) {
                                    Text(
                                        text = if (item.earnings > 0) "₦${String.format("%,.2f", item.earnings)}" else "—",
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = if (item.status == "Completed") PrimaryGreen else Color(0xFFEF4444)
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(4.dp))
                                            .background(
                                                if (item.status == "Completed") Color(0xFFDCFCE7) else Color(0xFFFEE2E2)
                                            )
                                            .padding(horizontal = 6.dp, vertical = 2.dp)
                                    ) {
                                        Text(
                                            text = item.status.uppercase(),
                                            fontSize = 8.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = if (item.status == "Completed") Color(0xFF15803D) else Color(0xFFB91C1C)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                // Pagination Load More button trigger
                if (filteredSortedHistory.size > visibleCount) {
                    item {
                        Button(
                            onClick = { visibleCount += 5 },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF334155)),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(44.dp)
                                .padding(vertical = 4.dp)
                        ) {
                            Text("Load More Completed Rides ⏳", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun RiderReviewsContent(
    riderName: String,
    allReviews: List<ReviewRating>
) {
    // Simulated premium reviews so that riders have detailed ratings on day one
    val simulatedReviews = remember {
        listOf(
            RiderReviewItem("Salami Stores (Mushin Market)", 5, "Delivered the honey beans in perfect condition. Very fast and extremely polite. Highly recommended dispatch rider!", "Yesterday"),
            RiderReviewItem("Adebayo Alao (Buyer)", 5, "Excellent service! Handled the fresh tomatoes with extreme care, none were squashed. Kept in touch throughout the ride.", "3 days ago"),
            RiderReviewItem("Ibrahim Agro (Mile 12)", 4, "On-time delivery and professional handling of bulk yam tubers. Will definitely request his dispatch services again.", "1 week ago"),
            RiderReviewItem("Mama Ngozi Foods (Vendor)", 5, "Very reliable and fast rider. Helped load the rice bags with care.", "2 weeks ago"),
            RiderReviewItem("Chioma Nze (Buyer)", 5, "Smooth delivery and verified successfully with PIN. Nice guy!", "3 weeks ago")
        )
    }

    // Map dynamic database reviews
    val dbReviewsForRider = allReviews
        .filter { it.targetName.equals(riderName, ignoreCase = true) }
        .map { rev ->
            val formattedDate = try {
                java.text.SimpleDateFormat("MMM dd, yyyy", java.util.Locale.getDefault()).format(java.util.Date(rev.timestamp))
            } catch (e: Exception) {
                "Today"
            }
            RiderReviewItem(
                reviewer = rev.reviewerName,
                rating = rev.rating,
                comment = rev.reviewText,
                date = formattedDate
            )
        }

    val combinedReviews = dbReviewsForRider + simulatedReviews
    val averageRating = if (combinedReviews.isEmpty()) 4.9 else combinedReviews.map { it.rating }.average()

    // Calculate rating stars distribution breakdown
    val ratingDistribution = remember(combinedReviews) {
        val total = combinedReviews.size.toDouble()
        if (total == 0.0) listOf(1.0, 0.0, 0.0, 0.0, 0.0)
        else {
            (5 downTo 1).map { star ->
                combinedReviews.count { it.rating == star } / total
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .padding(start = 16.dp, end = 16.dp, top = 20.dp, bottom = 24.dp)
    ) {
        Text(
            text = "⭐ Customer Reviews & Ratings",
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF0F172A)
        )
        Text(
            text = "Check what sellers and buyers are saying about your delivery speed & care.",
            fontSize = 12.sp,
            color = Color(0xFF64748B)
        )

        Spacer(modifier = Modifier.height(14.dp))

        // Aggregate Star Rating Breakdown card
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = SurfaceBg),
            border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
            shape = RoundedCornerShape(12.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = String.format("%.2f", averageRating),
                        fontSize = 36.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color(0xFF0F172A)
                    )
                    Row {
                        (1..5).forEach { star ->
                            Icon(
                                imageVector = Icons.Default.Star,
                                contentDescription = null,
                                tint = if (star <= averageRating.toInt()) SecondaryOrange else Color(0xFFCBD5E1),
                                modifier = Modifier.size(14.dp)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "${combinedReviews.size} Reviews total",
                        fontSize = 11.sp,
                        color = Color(0xFF64748B),
                        fontWeight = FontWeight.SemiBold
                    )
                }

                Divider(
                    modifier = Modifier
                        .height(80.dp)
                        .width(1.dp),
                    color = Color(0xFFE2E8F0)
                )

                // Distribution Progress bars
                Column(
                    modifier = Modifier
                        .weight(1.5f)
                        .padding(start = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    ratingDistribution.forEachIndexed { index, pct ->
                        val starVal = 5 - index
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("$starVal ★", fontSize = 10.sp, color = Color(0xFF64748B), modifier = Modifier.width(24.dp))
                            LinearProgressIndicator(
                                progress = pct.toFloat(),
                                modifier = Modifier
                                    .weight(1f)
                                    .height(6.dp)
                                    .clip(RoundedCornerShape(3.dp)),
                                color = SecondaryOrange,
                                trackColor = Color(0xFFE2E8F0)
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Reviews feed List
        LazyColumn(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            items(combinedReviews) { rev ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = SurfaceBg),
                    border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.Top
                        ) {
                            Column {
                                Text(
                                    text = rev.reviewer,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF1E293B)
                                )
                                Row {
                                    (1..5).forEach { s ->
                                        Icon(
                                            imageVector = Icons.Default.Star,
                                            contentDescription = null,
                                            tint = if (s <= rev.rating) SecondaryOrange else Color(0xFFE2E8F0),
                                            modifier = Modifier.size(12.dp)
                                        )
                                    }
                                }
                            }
                            Text(
                                text = rev.date,
                                fontSize = 10.sp,
                                color = Color(0xFF94A3B8),
                                fontWeight = FontWeight.SemiBold
                            )
                        }

                        if (rev.comment.isNotEmpty()) {
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = rev.comment,
                                fontSize = 12.sp,
                                color = Color(0xFF475569)
                            )
                        }
                    }
                }
            }

            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 10.dp, bottom = 16.dp)
                ) {
                    Text(
                        text = "Note: If you have suggestions or feedback about a customer/vendor review, please reach out to AfriSav Support. Dynamic review submission for vendors and buyers is currently handled in their respective order completion screens. Flagged for Rider review submission expansion.",
                        fontSize = 10.sp,
                        color = Color(0xFF94A3B8),
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
    }
}

@Composable
fun RiderProfileContent(
    viewModel: KoboViewModel,
    riderName: String,
    riderPhone: String,
    riderEmail: String,
    riderLocation: String,
    riderBio: String,
    vehicleType: String,
    licensePlate: String,
    securityPin: String,
    completedTrips: Int,
    allReviews: List<ReviewRating>,
    onLogoutClick: () -> Unit
) {
    val context = LocalContext.current
    
    // Editable state forms
    var editName by remember { mutableStateOf(riderName) }
    var editPhone by remember { mutableStateOf(riderPhone) }
    var editPlate by remember { mutableStateOf(licensePlate) }
    var selectedVehicle by remember { mutableStateOf(vehicleType) }

    // Security PIN changing states
    var curPinInput by remember { mutableStateOf("") }
    var newPinInput by remember { mutableStateOf("") }
    var confirmPinInput by remember { mutableStateOf("") }
    var pinMsg by remember { mutableStateOf("") }
    var isPinMsgError by remember { mutableStateOf(false) }

    // Calculate rating for aggregate
    val ratingReviews = allReviews.filter { it.targetName.equals(riderName, ignoreCase = true) }
    val averageRating = if (ratingReviews.isEmpty()) 4.9 else ratingReviews.map { it.rating }.average()

    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .statusBarsPadding()
            .padding(start = 16.dp, end = 16.dp, top = 20.dp, bottom = 24.dp)
    ) {
        // Summary Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = SurfaceBg),
            border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier
                        .size(64.dp)
                        .clip(CircleShape)
                        .background(PrimaryGreen.copy(alpha = 0.15f))
                        .border(1.5.dp, PrimaryGreen, CircleShape)
                ) {
                    ProfileAvatar(
                        imageUrl = null,
                        role = "rider",
                        modifier = Modifier.fillMaxSize()
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                Text(
                    text = riderName.ifEmpty { "AfriSav Rider" },
                    fontSize = 18.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color(0xFF0F172A)
                )
                Text(
                    text = "📞 $riderPhone • $riderEmail",
                    fontSize = 12.sp,
                    color = Color(0xFF64748B)
                )

                Spacer(modifier = Modifier.height(14.dp))
                Divider(color = Color(0xFFF1F5F9))
                Spacer(modifier = Modifier.height(12.dp))

                // Aggregate summary metrics row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Star, null, tint = SecondaryOrange, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(String.format("%.2f", averageRating), fontSize = 16.sp, fontWeight = FontWeight.ExtraBold, color = Color(0xFF0F172A))
                        }
                        Text("Average Rating", fontSize = 10.sp, color = Color(0xFF64748B))
                    }

                    Divider(modifier = Modifier.height(30.dp).width(1.dp), color = Color(0xFFE2E8F0))

                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("$completedTrips Runs", fontSize = 16.sp, fontWeight = FontWeight.ExtraBold, color = Color(0xFF0F172A))
                        Text("Completed Runs", fontSize = 10.sp, color = Color(0xFF64748B))
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Editable Details Card Form (spacing and min touch target size)
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = SurfaceBg),
            border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
            shape = RoundedCornerShape(12.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(
                    text = "✏️ Edit Rider Profile Details",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF0F172A)
                )

                OutlinedTextField(
                    value = editName,
                    onValueChange = { editName = it },
                    label = { Text("Full Name") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().testTag("edit_rider_name"),
                    colors = defaultTextFieldColors()
                )

                OutlinedTextField(
                    value = editPhone,
                    onValueChange = { editPhone = it.filter { c -> c.isDigit() } },
                    label = { Text("Phone Number") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth().testTag("edit_rider_phone"),
                    colors = defaultTextFieldColors()
                )

                // Vehicle Type Selection chips
                Text("Select Vehicle Type:", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color(0xFF475569))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    listOf("Motorcycle", "Tricycle", "Bicycle", "Mini-Van").forEach { veh ->
                        val isSel = selectedVehicle == veh
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(6.dp))
                                .background(if (isSel) PrimaryGreen else Color(0xFFF1F5F9))
                                .clickable { selectedVehicle = veh }
                                .padding(vertical = 8.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = veh,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isSel) Color.White else Color(0xFF475569)
                            )
                        }
                    }
                }

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable(
                            interactionSource = remember { androidx.compose.foundation.interaction.MutableInteractionSource() },
                            indication = null
                        ) {
                            android.widget.Toast.makeText(
                                context,
                                "Your plate number is verified and can only be updated by contacting support.",
                                android.widget.Toast.LENGTH_LONG
                            ).show()
                        }
                ) {
                    OutlinedTextField(
                        value = licensePlate,
                        onValueChange = {},
                        readOnly = true,
                        enabled = false,
                        label = { Text("License Plate Number (Verified)") },
                        singleLine = true,
                        trailingIcon = {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(end = 8.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Lock,
                                    contentDescription = "Verified & Locked",
                                    tint = PrimaryGreen,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "Verified",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = PrimaryGreen
                                )
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("edit_rider_plate"),
                        colors = OutlinedTextFieldDefaults.colors(
                            disabledTextColor = Color.Black,
                            disabledContainerColor = Color(0xFFF8FAFC),
                            disabledBorderColor = Color(0xFFE2E8F0),
                            disabledLabelColor = Color(0xFF64748B),
                            disabledPlaceholderColor = Color(0xFF64748B),
                            disabledTrailingIconColor = PrimaryGreen
                        )
                    )
                }

                // Validate and Save Profile updates button
                Button(
                    onClick = {
                        if (editName.trim().isEmpty()) {
                            android.widget.Toast.makeText(context, "Full Name cannot be empty.", android.widget.Toast.LENGTH_SHORT).show()
                        } else if (editPhone.trim().isEmpty()) {
                            android.widget.Toast.makeText(context, "Phone Number cannot be empty.", android.widget.Toast.LENGTH_SHORT).show()
                        } else {
                            viewModel.updateUserProfile(editName.trim(), editPhone.trim(), riderEmail, riderLocation, riderBio)
                            viewModel.updateRiderVehicleDetails(selectedVehicle, licensePlate)
                            android.widget.Toast.makeText(context, "Profile details updated successfully!", android.widget.Toast.LENGTH_SHORT).show()
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .testTag("save_rider_profile_updates"),
                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryGreen),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text("Save Profile Updates", fontWeight = FontWeight.Bold)
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Security settings & authentication password/PIN change Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = SurfaceBg),
            border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
            shape = RoundedCornerShape(12.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(
                    text = "🔒 Update Security Delivery PIN",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF0F172A)
                )
                Text(
                    text = "Configure your secure 4-digit verification PIN used to process simulated or real cargo delivery confirmations.",
                    fontSize = 11.sp,
                    color = Color(0xFF64748B)
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = confirmPinInput,
                        onValueChange = { if (it.length <= 4) confirmPinInput = it.filter { c -> c.isDigit() } },
                        label = { Text("New 4-Digit Security PIN") },
                        placeholder = { Text("e.g. 1234") },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.weight(1f).testTag("security_new_pin_input"),
                        colors = defaultTextFieldColors(),
                        shape = RoundedCornerShape(8.dp)
                    )

                    Button(
                        onClick = {
                            if (confirmPinInput.length != 4) {
                                pinMsg = "PIN must be exactly 4 digits."
                                isPinMsgError = true
                            } else {
                                viewModel.updateRiderPin(confirmPinInput)
                                pinMsg = "Security PIN updated successfully!"
                                isPinMsgError = false
                                confirmPinInput = ""
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF334155)),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.height(48.dp).testTag("save_rider_pin_btn")
                    ) {
                        Text("Update PIN", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }

                if (pinMsg.isNotEmpty()) {
                    Text(
                        text = pinMsg,
                        color = if (isPinMsgError) Color.Red else PrimaryGreen,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Large standard Red Log Out Option touch target
        Button(
            onClick = onLogoutClick,
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFEE2E2), contentColor = Color(0xFFB91C1C)),
            shape = RoundedCornerShape(8.dp),
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp)
                .testTag("rider_logout_button")
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.ExitToApp, null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("Log Out Profile Session", fontWeight = FontWeight.Bold, fontSize = 13.sp)
            }
        }

        Spacer(modifier = Modifier.height(16.dp))
    }
}
