package com.example.ui

import com.example.ui.components.*
import com.example.ui.theme.AfriSavTheme
import com.example.R
import android.app.Application
import androidx.core.content.ContextCompat
import android.content.pm.PackageManager
import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.*
import androidx.compose.foundation.lazy.grid.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.*
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.unit.Dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.data.*
import com.example.ui.theme.*
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import kotlinx.coroutines.delay
import androidx.compose.animation.core.*
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import android.graphics.Bitmap
import android.net.Uri
import java.io.File
import java.io.FileOutputStream
import coil.compose.AsyncImage
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.nativeCanvas
import android.graphics.Paint
import android.graphics.Typeface
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.ui.input.pointer.pointerInput

enum class ScreenState {
    SPLASH,
    ONBOARDING,
    LOGIN,
    REGISTER,
    MAIN,
    SELLER_DASHBOARD,
    RIDER_DASHBOARD,
    DISPATCH_COMPANY_DASHBOARD,
    PIN_LOCK
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun KoboApp(
    viewModel: KoboViewModel = viewModel(factory = KoboViewModelFactory(LocalContext.current.applicationContext as Application))
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    var currentScreen by remember { mutableStateOf(ScreenState.SPLASH) }
    val activeTab by viewModel.activeTab.collectAsStateWithLifecycle()
    val successModalState by viewModel.successModalState.collectAsStateWithLifecycle()
    val isDarkMode by viewModel.isDarkMode.collectAsStateWithLifecycle()

    // Dialog state controllers
    var showFundWalletDialog by remember { mutableStateOf(false) }
    var showTransferDialog by remember { mutableStateOf(false) }
    var showCreateGoalDialog by remember { mutableStateOf(false) }
    var showCreateCircleDialog by remember { mutableStateOf(false) }
    var selectedCheckoutItem by remember { mutableStateOf<MarketItem?>(null) }
    var selectedPortionItem by remember { mutableStateOf<MarketItem?>(null) }
    var selectedPortionName by remember { mutableStateOf("Full Unit") }
    var selectedPortionFraction by remember { mutableStateOf(1.0) }
    var selectedSaveTowardsItem by remember { mutableStateOf<MarketItem?>(null) }
    var showEditProfileDialog by remember { mutableStateOf(false) }
    var showPriceWarningDialog by remember { mutableStateOf(false) }
    var emergencyWithdrawalGoalId by remember { mutableStateOf<Int?>(null) }
    var emergencyBankWithdrawalData by remember { mutableStateOf<String?>(null) }
    var contactRiderOrder by remember { mutableStateOf<EscrowOrder?>(null) }
    var showSimulatedCall by remember { mutableStateOf(false) }
    var showSimulatedChat by remember { mutableStateOf(false) }

    var selectedTransactionForReceipt by remember { mutableStateOf<WalletTransaction?>(null) }

    var showOnboardingTour by remember { mutableStateOf(false) }
    var onboardingTourStep by remember { mutableStateOf(0) }


    // Listen to ViewModel SharedFlow events (toasts/notifications)
    LaunchedEffect(key1 = true) {
        viewModel.uiEvent.collectLatest { event ->
            if (event.startsWith("GOAL_INCOMPLETE_WITHDRAWAL_CONFIRM:")) {
                val goalId = event.substringAfter("GOAL_INCOMPLETE_WITHDRAWAL_CONFIRM:").toIntOrNull()
                emergencyWithdrawalGoalId = goalId
            } else if (event.startsWith("GOAL_INCOMPLETE_BANK_WITHDRAWAL_CONFIRM:")) {
                val data = event.substringAfter("GOAL_INCOMPLETE_BANK_WITHDRAWAL_CONFIRM:")
                emergencyBankWithdrawalData = data
            } else {
                scope.launch {
                    snackbarHostState.showSnackbar(
                        message = event,
                        duration = SnackbarDuration.Short
                    )
                }
            }
        }
    }

    AfriSavTheme(darkTheme = isDarkMode) {
        Scaffold(
            contentWindowInsets = androidx.compose.foundation.layout.WindowInsets(0, 0, 0, 0),
            snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        bottomBar = {
            if (currentScreen == ScreenState.MAIN) {
                KoboBottomNavBar(
                    activeTab = activeTab,
                    onTabSelected = { viewModel.setActiveTab(it) }
                )
            }
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            when (currentScreen) {
                ScreenState.SPLASH -> {
                    SplashScreen(
                        onSplashComplete = {
                            if (viewModel.hasSavedSession()) {
                                currentScreen = ScreenState.PIN_LOCK
                            } else {
                                currentScreen = ScreenState.ONBOARDING
                            }
                        }
                    )
                }
                ScreenState.ONBOARDING -> {
                    OnboardingScreen(
                        onGetStarted = { currentScreen = ScreenState.LOGIN }
                    )
                }
                ScreenState.LOGIN -> {
                    LoginScreen(
                        viewModel = viewModel,
                        onLoginSuccess = { role ->
                            showPriceWarningDialog = (role != "Seller" && role != "Rider" && role != "Dispatch Company")
                            currentScreen = when (role) {
                                "Seller" -> ScreenState.SELLER_DASHBOARD
                                "Rider" -> ScreenState.RIDER_DASHBOARD
                                "Dispatch Company" -> ScreenState.DISPATCH_COMPANY_DASHBOARD
                                else -> ScreenState.MAIN
                            }
                        },
                        onNavigateToRegister = { currentScreen = ScreenState.REGISTER }
                    )
                }
                ScreenState.REGISTER -> {
                    RegisterScreen(
                        viewModel = viewModel,
                        onRegisterSuccess = { role ->
                            showPriceWarningDialog = (role != "Seller" && role != "Rider" && role != "Dispatch Company")
                            currentScreen = when (role) {
                                "Seller" -> ScreenState.SELLER_DASHBOARD
                                "Rider" -> ScreenState.RIDER_DASHBOARD
                                "Dispatch Company" -> ScreenState.DISPATCH_COMPANY_DASHBOARD
                                else -> {
                                    showOnboardingTour = true
                                    onboardingTourStep = 0
                                    ScreenState.MAIN
                                }
                            }
                        },
                        onNavigateToLogin = { currentScreen = ScreenState.LOGIN }
                    )
                }
                ScreenState.PIN_LOCK -> {
                    PinLockScreen(
                        viewModel = viewModel,
                        onUnlockSuccess = { role ->
                            showPriceWarningDialog = (role != "Seller" && role != "Rider" && role != "Dispatch Company")
                            currentScreen = when (role) {
                                "Seller" -> ScreenState.SELLER_DASHBOARD
                                "Rider" -> ScreenState.RIDER_DASHBOARD
                                "Dispatch Company" -> ScreenState.DISPATCH_COMPANY_DASHBOARD
                                else -> ScreenState.MAIN
                            }
                        },
                        onUseAnotherAccount = {
                            viewModel.logoutUser()
                            currentScreen = ScreenState.LOGIN
                        }
                    )
                }
                ScreenState.SELLER_DASHBOARD -> {
                    SellerDashboardScreen(
                        viewModel = viewModel,
                        onLogoutClick = {
                            viewModel.logoutUser()
                            currentScreen = ScreenState.LOGIN
                        },
                        onContactRider = { order, method ->
                            contactRiderOrder = order
                            if (method == "CALL") {
                                showSimulatedCall = true
                            } else {
                                showSimulatedChat = true
                            }
                        }
                    )
                }
                ScreenState.RIDER_DASHBOARD -> {
                    RiderDashboardScreen(
                        viewModel = viewModel,
                        onLogoutClick = {
                            viewModel.logoutUser()
                            currentScreen = ScreenState.LOGIN
                        }
                    )
                }
                ScreenState.DISPATCH_COMPANY_DASHBOARD -> {
                    DispatchCompanyDashboardScreen(
                        viewModel = viewModel,
                        onLogoutClick = {
                            viewModel.logoutUser()
                            currentScreen = ScreenState.LOGIN
                        }
                    )
                }
                ScreenState.MAIN -> {
                    Crossfade(targetState = activeTab, label = "tab_crossfade") { tab ->
                        when (tab) {
                            "home" -> HomeScreen(
                                viewModel = viewModel,
                                onFundWalletClick = { showFundWalletDialog = true },
                                onCreateGoalClick = { showCreateGoalDialog = true },
                                onBuyFoodClick = { viewModel.setActiveTab("market") },
                                onTransferClick = { showTransferDialog = true },
                                onViewVendorsClick = { viewModel.setActiveTab("market") },
                                onLogoutClick = {
                                    viewModel.logoutUser()
                                    currentScreen = ScreenState.LOGIN
                                },
                                onEditProfileClick = { viewModel.setActiveTab("profile") },
                                onReceiptClick = { selectedTransactionForReceipt = it },
                                onStartTourClick = {
                                    showOnboardingTour = true
                                    onboardingTourStep = 0
                                    viewModel.setActiveTab("home")
                                }
                            )
                            "wallet" -> WalletScreen(
                                viewModel = viewModel,
                                onFundClick = { showFundWalletDialog = true },
                                onTransferClick = { showTransferDialog = true },
                                onReceiptClick = { selectedTransactionForReceipt = it }
                            )
                            "goals" -> GoalsScreen(
                                viewModel = viewModel,
                                onCreateGoalClick = { showCreateGoalDialog = true }
                            )
                            "market" -> MarketScreen(
                                viewModel = viewModel,
                                onBuyNowClick = { item ->
                                    if (item.allowPortions || item.isBundle) {
                                        selectedPortionItem = item
                                    } else {
                                        selectedCheckoutItem = item
                                    }
                                },
                                onSaveTowardsClick = { selectedSaveTowardsItem = it },
                                onAddToCartClick = { selectedPortionItem = it }
                            )
                            "circles" -> CirclesScreen(
                                viewModel = viewModel,
                                onCreateCircleClick = { showCreateCircleDialog = true }
                            )
                            "orders" -> OrdersScreen(
                                viewModel = viewModel,
                                onBackClick = { viewModel.setActiveTab("home") },
                                onContactRider = { order, method ->
                                    contactRiderOrder = order
                                    if (method == "CALL") {
                                        showSimulatedCall = true
                                    } else {
                                        showSimulatedChat = true
                                    }
                                }
                            )
                            "assistant" -> AssistantScreen(
                                viewModel = viewModel
                            )
                            "profile" -> BuyerProfileScreen(
                                viewModel = viewModel,
                                onBackClick = { viewModel.setActiveTab("home") },
                                onContactRider = { order, method ->
                                    contactRiderOrder = order
                                    if (method == "CALL") {
                                        showSimulatedCall = true
                                    } else {
                                        showSimulatedChat = true
                                    }
                                },
                                onLogoutClick = {
                                    viewModel.logoutUser()
                                    currentScreen = ScreenState.LOGIN
                                }
                            )
                        }
                    }
                    if (activeTab != "assistant") {
                        MamaChatWidget(
                            viewModel = viewModel,
                            modifier = Modifier.align(Alignment.BottomEnd)
                        )
                    }
                    
                    if (showOnboardingTour) {
                        val currentUserNameVal by viewModel.currentUserName.collectAsStateWithLifecycle()
                        OnboardingTourOverlay(
                            currentStep = onboardingTourStep,
                            userName = currentUserNameVal.ifEmpty { "Adebayo" },
                            onNext = {
                                if (onboardingTourStep < 3) {
                                    onboardingTourStep++
                                    when (onboardingTourStep) {
                                        1 -> viewModel.setActiveTab("wallet")
                                        2 -> viewModel.setActiveTab("circles")
                                        3 -> viewModel.setActiveTab("circles")
                                    }
                                } else {
                                    showOnboardingTour = false
                                    onboardingTourStep = 0
                                    viewModel.setActiveTab("home")
                                }
                            },
                            onPrev = {
                                if (onboardingTourStep > 0) {
                                    onboardingTourStep--
                                    when (onboardingTourStep) {
                                        0 -> viewModel.setActiveTab("home")
                                        1 -> viewModel.setActiveTab("wallet")
                                        2 -> viewModel.setActiveTab("circles")
                                        3 -> viewModel.setActiveTab("circles")
                                    }
                                }
                            },
                            onSkip = {
                                showOnboardingTour = false
                                onboardingTourStep = 0
                                viewModel.setActiveTab("home")
                            }
                        )
                    }
                }
            }
        }
    }

    // --- OVERLAY DIALOGS ---

    if (selectedTransactionForReceipt != null) {
        DigitalReceiptDialog(
            tx = selectedTransactionForReceipt!!,
            onDismiss = { selectedTransactionForReceipt = null },
            viewModel = viewModel
        )
    }

    if (showFundWalletDialog) {
        val userName by viewModel.currentUserName.collectAsStateWithLifecycle()
        FundWalletDialog(
            currentUserName = userName,
            onDismiss = { showFundWalletDialog = false },
            onConfirm = { amount ->
                viewModel.fundWallet(amount)
                showFundWalletDialog = false
            }
        )
    }

    if (showTransferDialog) {
        TransferFundsDialog(
            onDismiss = { showTransferDialog = false },
            onConfirm = { recipient, amount ->
                viewModel.transferFunds(amount, recipient)
                showTransferDialog = false
            }
        )
    }

    if (showCreateGoalDialog) {
        CreateGoalDialog(
            onDismiss = { showCreateGoalDialog = false },
            onConfirm = { title, target, category, autoSaveAmt, autoSaveEnabled ->
                viewModel.createGoal(title, target, category, autoSaveAmt, autoSaveEnabled)
                showCreateGoalDialog = false
            }
        )
    }

    if (showCreateCircleDialog) {
        CreateCircleDialog(
            onDismiss = { showCreateCircleDialog = false },
            onConfirm = { title, desc, target, contrib, isPrivate ->
                viewModel.createCircle(title, desc, target, contrib, isPrivate)
                showCreateCircleDialog = false
            }
        )
    }

    if (showEditProfileDialog) {
        EditProfileDialog(
            viewModel = viewModel,
            onDismiss = { showEditProfileDialog = false },
            onLogoutClick = {
                viewModel.logoutUser()
                currentScreen = ScreenState.LOGIN
                showEditProfileDialog = false
            }
        )
    }

    successModalState?.let { state ->
        if (state.isOpen) {
            SuccessModalDialog(
                state = state,
                onDismiss = { viewModel.dismissSuccessModal() }
            )
        }
    }

    selectedCheckoutItem?.let { item ->
        CheckoutDialog(
            item = item,
            viewModel = viewModel,
            onDismiss = {
                selectedCheckoutItem = null
                selectedPortionName = "Full Unit"
                selectedPortionFraction = 1.0
            },
            portionName = selectedPortionName,
            portionFraction = selectedPortionFraction,
            onConfirm = { paymentMethod ->
                viewModel.buyProductNow(item, paymentMethod, selectedPortionName, item.price * selectedPortionFraction)
                selectedCheckoutItem = null
                selectedPortionName = "Full Unit"
                selectedPortionFraction = 1.0
            }
        )
    }

    selectedPortionItem?.let { item ->
        PortionSelectionDialog(
            item = item,
            onDismiss = { selectedPortionItem = null },
            onConfirmPortion = { portionName, portionFraction, triggerBuyNow ->
                selectedPortionItem = null
                if (triggerBuyNow) {
                    selectedPortionName = portionName
                    selectedPortionFraction = portionFraction
                    selectedCheckoutItem = item
                } else {
                    viewModel.addToCart(item, portionName, portionFraction)
                }
            }
        )
    }

    selectedSaveTowardsItem?.let { item ->
        SaveTowardsProductDialog(
            item = item,
            onDismiss = { selectedSaveTowardsItem = null },
            onConfirm = { initialSave ->
                viewModel.saveTowardsProduct(item, initialSave)
                selectedSaveTowardsItem = null
            }
        )
    }

    if (showPriceWarningDialog) {
        PriceFluctuationWarningDialog(
            onDismiss = { showPriceWarningDialog = false }
        )
    }



    emergencyWithdrawalGoalId?.let { goalId ->
        val goals by viewModel.savingsGoals.collectAsStateWithLifecycle()
        val goal = goals.find { it.id == goalId }
        if (goal != null) {
            AlertDialog(
                onDismissRequest = { emergencyWithdrawalGoalId = null },
                icon = { Icon(Icons.Default.Warning, contentDescription = null, tint = Color(0xFFEA580C)) },
                title = { Text("Emergency Early Refund?", fontWeight = FontWeight.Bold, color = Color(0xFF1E293B)) },
                text = {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(
                            text = "This food savings goal is not complete yet. Normal withdrawals are only allowed when the goal is complete.",
                            fontSize = 13.sp,
                            color = Color(0xFF64748B)
                        )
                        Text(
                            text = "If this is an emergency and you must withdraw early, there is a 5% penalty fee (₦${String.format("%,.2f", goal.savedAmount * 0.05)}) deducted from the refunded amount.",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFEF4444)
                        )
                        Text(
                            text = "Amount to save: ₦${String.format("%,.0f", goal.targetAmount)}\n" +
                                   "Current savings: ₦${String.format("%,.0f", goal.savedAmount)}\n" +
                                   "Estimated refund: ₦${String.format("%,.2f", goal.savedAmount * 0.95)}",
                            fontSize = 12.sp,
                            color = Color(0xFF1E293B),
                            fontWeight = FontWeight.Medium
                        )
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            viewModel.withdrawGoalFunds(goalId, isEmergency = true)
                            emergencyWithdrawalGoalId = null
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEF4444)),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text("Emergency Refund (5% fee)", fontWeight = FontWeight.Bold, color = Color.White)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { emergencyWithdrawalGoalId = null }) {
                        Text("Cancel", color = Color(0xFF64748B))
                    }
                }
            )
        } else {
            emergencyWithdrawalGoalId = null
        }
    }

    emergencyBankWithdrawalData?.let { data ->
        val parts = data.split("|")
        if (parts.size >= 3) {
            val goalId = parts[0].toIntOrNull() ?: 0
            val bankName = parts[1]
            val accountNumber = parts[2]
            
            val goals by viewModel.savingsGoals.collectAsStateWithLifecycle()
            val goal = goals.find { it.id == goalId }
            if (goal != null) {
                AlertDialog(
                    onDismissRequest = { emergencyBankWithdrawalData = null },
                    icon = { Icon(Icons.Default.Warning, contentDescription = null, tint = Color(0xFFEA580C)) },
                    title = { Text("Emergency Bank Cashout?", fontWeight = FontWeight.Bold, color = Color(0xFF1E293B)) },
                    text = {
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text(
                                text = "This food savings goal is not complete yet. Normal withdrawals are only allowed when the goal is complete.",
                                fontSize = 13.sp,
                                color = Color(0xFF64748B)
                            )
                            Text(
                                text = "If this is an emergency and you must withdraw early directly to your bank account, there is a 5% penalty fee (₦${String.format("%,.2f", goal.savedAmount * 0.05)}) deducted from the refunded amount.",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFFEF4444)
                            )
                            Text(
                                text = "Net payout: ₦${String.format("%,.2f", goal.savedAmount * 0.95)} sent to $bankName ($accountNumber).",
                                fontSize = 12.sp,
                                color = Color(0xFF1E293B),
                                fontWeight = FontWeight.Medium
                            )
                        }
                    },
                    confirmButton = {
                        Button(
                            onClick = {
                                viewModel.withdrawGoalFundsToBank(goalId, bankName, accountNumber, isEmergency = true)
                                emergencyBankWithdrawalData = null
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEF4444)),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text("Emergency Bank Cashout", fontWeight = FontWeight.Bold, color = Color.White)
                        }
                    },
                    dismissButton = {
                        TextButton(onClick = { emergencyBankWithdrawalData = null }) {
                            Text("Cancel", color = Color(0xFF64748B))
                        }
                    }
                )
            } else {
                emergencyBankWithdrawalData = null
            }
        } else {
            emergencyBankWithdrawalData = null
        }
    }

    if (showSimulatedCall) {
        contactRiderOrder?.let { esc ->
            SimulatedCallOverlay(
                riderName = esc.riderName ?: "Unknown Rider",
                onDismiss = {
                    showSimulatedCall = false
                    contactRiderOrder = null
                }
            )
        }
    }

    if (showSimulatedChat) {
        contactRiderOrder?.let { esc ->
            SimulatedChatDialog(
                riderName = esc.riderName ?: "Unknown Rider",
                onDismiss = {
                    showSimulatedChat = false
                    contactRiderOrder = null
                }
            )
        }
    }
    }
}

// --- ANIMATED SPLASH SCREEN ---
@Composable
fun SplashScreen(onSplashComplete: () -> Unit) {
    // Animation states
    val scale = remember { Animatable(0.5f) }
    val alpha = remember { Animatable(0f) }
    val textAlpha = remember { Animatable(0f) }
    val textTranslationY = remember { Animatable(30f) }

    // Pulsing/glow effect transition
    val infiniteTransition = rememberInfiniteTransition(label = "pulse_glow")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.15f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseScale"
    )
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.15f,
        targetValue = 0.45f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseAlpha"
    )

    LaunchedEffect(key1 = true) {
        // Run entry animations in parallel
        launch {
            scale.animateTo(
                targetValue = 1f,
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioMediumBouncy,
                    stiffness = Spring.StiffnessLow
                )
            )
        }
        launch {
            alpha.animateTo(
                targetValue = 1f,
                animationSpec = tween(800, easing = LinearOutSlowInEasing)
            )
        }

        // Slightly delayed text fade-in & slide up
        delay(300)
        launch {
            textAlpha.animateTo(
                targetValue = 1f,
                animationSpec = tween(1000, easing = LinearOutSlowInEasing)
            )
        }
        launch {
            textTranslationY.animateTo(
                targetValue = 0f,
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioNoBouncy,
                    stiffness = Spring.StiffnessLow
                )
            )
        }

        // Wait for splash duration, then navigate to onboarding
        delay(2500)
        onSplashComplete()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF0F172A)), // Deep slate dark background
        contentAlignment = Alignment.Center
    ) {
        // Ambient background glow
        Box(
            modifier = Modifier
                .size(280.dp)
                .graphicsLayer {
                    scaleX = pulseScale
                    scaleY = pulseScale
                    this.alpha = pulseAlpha
                }
                .background(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            Color(0xFF4ADE80).copy(alpha = 0.3f),
                            Color.Transparent
                        )
                    ),
                    shape = CircleShape
                )
        )

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Animated Logo Container
            Box(
                modifier = Modifier
                    .size(140.dp)
                    .graphicsLayer {
                        scaleX = scale.value
                        scaleY = scale.value
                        this.alpha = alpha.value
                    }
                    .background(
                        brush = Brush.verticalGradient(
                            colors = listOf(Color(0xFF0A8F3D), Color(0xFF16A34A))
                        ),
                        shape = CircleShape
                    )
                    .border(2.dp, Color(0xFF4ADE80).copy(alpha = 0.5f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Filled.ShoppingBasket,
                    contentDescription = "Logo",
                    tint = Color.White,
                    modifier = Modifier.size(64.dp)
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Animated App Name
            Text(
                text = "AfriSav",
                fontSize = 38.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                modifier = Modifier.graphicsLayer {
                    this.alpha = textAlpha.value
                    this.translationY = textTranslationY.value
                }
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Animated Tagline
            Text(
                text = "Save Small in Kobo, Feed Better.",
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                color = Color(0xFF94A3B8), // Slate 400
                modifier = Modifier.graphicsLayer {
                    this.alpha = textAlpha.value
                    this.translationY = textTranslationY.value * 0.7f
                }
            )
        }

        // Subtle animated progress indicator at the bottom
        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 64.dp)
                .graphicsLayer {
                    this.alpha = textAlpha.value
                },
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator(
                color = Color(0xFF4ADE80),
                strokeWidth = 3.dp,
                modifier = Modifier.size(28.dp)
            )
        }
    }
}

// --- ONBOARDING SCREEN ---
@Composable
fun OnboardingScreen(onGetStarted: () -> Unit) {
    var currentPage by remember { mutableStateOf(0) }
    val pages = listOf(
        OnboardingPageData(
            title = "Save Small in Kobo,\nFeed Your Family Better.",
            description = "Save ₦100, ₦500 or ₦1,000 daily towards a bag of rice, oil, or monthly food supplies. Seamlessly build food security.",
            icon = Icons.Outlined.Savings,
            bgColor = Color(0xFF0A8F3D)
        ),
        OnboardingPageData(
            title = "Real-Time Food Prices,\nDirect from Farms.",
            description = "Compare direct market rates from Lagos Mile 12 to Alaba. Discover the cheapest local vendors in real-time.",
            icon = Icons.Outlined.Storefront,
            bgColor = Color(0xFFFF8C00)
        ),
        OnboardingPageData(
            title = "Food Circles,\nBulk Buy with Neighbors.",
            description = "Form Circles with friends or neighbors. Combine savings to buy in bulk directly from wholesale distributors. Split shipping costs!",
            icon = Icons.Outlined.Groups,
            bgColor = Color(0xFFF4C430)
        )
    )

    val page = pages[currentPage]

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF0F172A)) // Deep slate dark background for readability & high contrast
            .statusBarsPadding()
            .padding(start = 24.dp, end = 24.dp, top = 20.dp, bottom = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        // Top Brand Bar
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Filled.ShoppingBasket,
                contentDescription = "Logo",
                tint = Color(0xFF4ADE80), // Vibrant green logo
                modifier = Modifier.size(32.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "AfriSav",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White // High contrast white brand text
            )
        }

        // Feature Illustration
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.Center
        ) {
            Box(
                modifier = Modifier
                    .size(160.dp)
                    .background(page.bgColor.copy(alpha = 0.2f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = page.icon,
                    contentDescription = null,
                    tint = if (page.bgColor == Color(0xFFF4C430)) Color(0xFFFBBF24) else page.bgColor, // Vibrant color accent
                    modifier = Modifier.size(80.dp)
                )
            }
            Spacer(modifier = Modifier.height(32.dp))
            Text(
                text = page.title,
                fontSize = 26.sp,
                lineHeight = 32.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                color = Color.White // White, highly readable header
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = page.description,
                fontSize = 15.sp,
                lineHeight = 22.sp,
                textAlign = TextAlign.Center,
                color = Color(0xFFCBD5E1), // Light gray/slate, comfortable to read
                modifier = Modifier.padding(horizontal = 16.dp)
            )
        }

        // Bottom Nav Actions
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Indicator dots
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.padding(bottom = 24.dp)
            ) {
                pages.forEachIndexed { index, _ ->
                    Box(
                        modifier = Modifier
                            .size(if (index == currentPage) 24.dp else 8.dp, 8.dp)
                            .clip(CircleShape)
                            .background(
                                if (index == currentPage) {
                                    if (page.bgColor == Color(0xFFF4C430)) Color(0xFFFBBF24) else page.bgColor
                                } else {
                                    Color(0xFF334155) // Dark slate for low-contrast inactive indicator on dark mode
                                }
                            )
                    )
                }
            }

            // Buttons
            Button(
                onClick = {
                    if (currentPage < pages.size - 1) {
                        currentPage++
                    } else {
                        onGetStarted()
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp)
                    .testTag("onboarding_next_button"),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (currentPage == 2) Color(0xFF0A8F3D) else {
                        if (page.bgColor == Color(0xFFF4C430)) Color(0xFFF59E0B) else page.bgColor
                    }
                ),
                shape = RoundedCornerShape(16.dp)
            ) {
                Text(
                    text = if (currentPage == pages.size - 1) "Start Saving Now" else "Next",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }

            if (currentPage < pages.size - 1) {
                TextButton(
                    onClick = onGetStarted,
                    modifier = Modifier.padding(top = 8.dp)
                ) {
                    Text("Skip", color = Color(0xFF94A3B8), fontSize = 14.sp) // High contrast skip text
                }
            } else {
                Spacer(modifier = Modifier.height(28.dp))
            }
        }
    }
}

data class OnboardingPageData(
    val title: String,
    val description: String,
    val icon: androidx.compose.ui.graphics.vector.ImageVector,
    val bgColor: Color
)

@Composable
fun defaultTextFieldColors() = OutlinedTextFieldDefaults.colors(
    focusedTextColor = TextDark,
    unfocusedTextColor = TextDark,
    focusedContainerColor = SurfaceBg,
    unfocusedContainerColor = SurfaceBg,
    focusedBorderColor = PrimaryGreen,
    unfocusedBorderColor = BorderSlate100,
    cursorColor = PrimaryGreen,
    focusedLabelColor = PrimaryGreen,
    unfocusedLabelColor = TextSlate500,
    focusedPlaceholderColor = TextSlate400,
    unfocusedPlaceholderColor = TextSlate400,
    focusedLeadingIconColor = PrimaryGreen,
    unfocusedLeadingIconColor = TextSlate400,
    errorBorderColor = AppColors.error,
    errorContainerColor = SurfaceBg
)

// --- LOGIN SCREEN ---
@Composable
fun LoginScreen(
    viewModel: KoboViewModel,
    onLoginSuccess: (String) -> Unit,
    onNavigateToRegister: () -> Unit
) {
    var phone by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var selectedRole by remember { mutableStateOf("Buyer") } // "Buyer" or "Seller"
    var showRoleDropdown by remember { mutableStateOf(false) }
    var errorText by remember { mutableStateOf("") }

    val isDarkMode by viewModel.isDarkMode.collectAsStateWithLifecycle()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.surface)
            .verticalScroll(rememberScrollState())
            .statusBarsPadding()
            .padding(start = 24.dp, end = 24.dp, top = 20.dp, bottom = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.End,
            verticalAlignment = Alignment.CenterVertically
        ) {
            ThemeToggle(
                isDarkMode = isDarkMode,
                onToggle = { viewModel.toggleDarkMode() }
            )
        }
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Spacer(modifier = Modifier.height(16.dp))
            Icon(
                imageVector = Icons.Filled.ShoppingBasket,
                contentDescription = null,
                tint = PrimaryGreen,
                modifier = Modifier.size(64.dp)
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "Welcome Back",
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                color = TextSlate800
            )
            Text(
                text = "Save in Kobo. Buy by Basket.",
                fontSize = 14.sp,
                color = TextSlate500
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Select Account Type",
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = TextSlate800,
                modifier = Modifier.align(Alignment.Start)
            )
            Spacer(modifier = Modifier.height(6.dp))

            Box(modifier = Modifier.fillMaxWidth().testTag("user_type_dropdown_container")) {
                OutlinedButton(
                    onClick = { showRoleDropdown = true },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(54.dp)
                        .testTag("user_type_dropdown_trigger"),
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(1.dp, Color(0xFFCBD5E1)),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFF1E293B))
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = when (selectedRole) {
                                "Buyer" -> "🛍️ Buyer"
                                "Seller" -> "👨‍🌾 Seller Hub"
                                "Rider" -> "🚴 Rider"
                                else -> "🏢 Logistics Co"
                            },
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 14.sp,
                            color = TextSlate800
                        )
                        Icon(
                            imageVector = Icons.Default.ArrowDropDown,
                            contentDescription = "Select User Type dropdown",
                            tint = PrimaryGreen
                        )
                    }
                }

                DropdownMenu(
                    expanded = showRoleDropdown,
                    onDismissRequest = { showRoleDropdown = false },
                    modifier = Modifier
                        .fillMaxWidth(0.9f)
                        .background(MaterialTheme.colorScheme.surface)
                        .testTag("user_type_dropdown_menu")
                ) {
                    listOf("Buyer", "Seller", "Rider", "Dispatch Company").forEach { role ->
                        DropdownMenuItem(
                            text = {
                                Text(
                                    text = when (role) {
                                        "Buyer" -> "🛍️ Buyer"
                                        "Seller" -> "👨‍🌾 Seller Hub"
                                        "Rider" -> "🚴 Rider"
                                        else -> "🏢 Logistics Co"
                                    },
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp,
                                    color = TextSlate800
                                )
                            },
                            onClick = {
                                selectedRole = role
                                showRoleDropdown = false
                            },
                            modifier = Modifier.testTag("user_type_option_$role")
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Phone Field
            OutlinedTextField(
                value = phone,
                onValueChange = { phone = it },
                label = { Text("Phone Number") },
                placeholder = { Text("e.g. 08012345678") },
                prefix = { Text("+234 ", color = Color.Black) },
                leadingIcon = { Icon(Icons.Default.Phone, contentDescription = null) },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("login_phone_input"),
                shape = RoundedCornerShape(12.dp),
                colors = defaultTextFieldColors()
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Password Field
            OutlinedTextField(
                value = password,
                onValueChange = { password = it },
                label = { Text("Secret PIN / Password") },
                leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null) },
                singleLine = true,
                visualTransformation = PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("login_password_input"),
                shape = RoundedCornerShape(12.dp),
                colors = defaultTextFieldColors()
            )

            if (errorText.isNotEmpty()) {
                Text(
                    text = errorText,
                    color = MaterialTheme.colorScheme.error,
                    fontSize = 13.sp,
                    modifier = Modifier.padding(top = 12.dp)
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                TextButton(onClick = { }) {
                    Text("Forgot Password?", color = SecondaryOrange, fontSize = 13.sp)
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = {
                    // DEMO LOGIN — restricted to designated test credentials
                    val demoPhone = "08160745664"
                    val demoPin = "8443"

                    if (phone != demoPhone || password != demoPin) {
                        errorText = "Invalid credentials. Please enter the correct demo phone number and PIN."
                    } else {
                        val name = when (selectedRole) {
                            "Seller" -> "Salami Stores"
                            "Rider" -> "Rider Ade"
                            else -> "Adebayo Alao"
                        }
                        viewModel.loginUser(phone, name, selectedRole, password)
                        onLoginSuccess(selectedRole)
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp)
                    .testTag("login_submit_button"),
                colors = ButtonDefaults.buttonColors(containerColor = PrimaryGreen),
                shape = RoundedCornerShape(16.dp)
            ) {
                Text(
                    text = when (selectedRole) {
                        "Seller" -> "Login to Seller Hub"
                        "Rider" -> "Login as Dispatch Rider"
                        else -> "Login to My Basket"
                    },
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }
        }

        Row(
            modifier = Modifier.padding(bottom = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Don't have an account?", color = TextSlate500)
            TextButton(onClick = onNavigateToRegister) {
                Text("Register Here", color = SecondaryOrange, fontWeight = FontWeight.Bold)
            }
        }
    }
}

// --- PIN LOCK SCREEN ---
@Composable
fun PinLockScreen(
    viewModel: KoboViewModel,
    onUnlockSuccess: (String) -> Unit,
    onUseAnotherAccount: () -> Unit
) {
    val savedName = viewModel.getSavedName() ?: "User"
    val savedRole = viewModel.getSavedRole() ?: "Buyer"
    val savedPin = viewModel.getSavedPin() ?: ""
    val isDarkMode by viewModel.isDarkMode.collectAsStateWithLifecycle()

    var enteredPin by remember { mutableStateOf("") }
    var errorText by remember { mutableStateOf("") }
    var failedAttempts by remember { mutableStateOf(0) }
    var showBiometricDialog by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.surface)
            .statusBarsPadding()
            .padding(start = 24.dp, end = 24.dp, top = 20.dp, bottom = 24.dp)
            .testTag("pin_lock_screen"),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        // Theme toggle and top spacing
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.End,
            verticalAlignment = Alignment.CenterVertically
        ) {
            ThemeToggle(
                isDarkMode = isDarkMode,
                onToggle = { viewModel.toggleDarkMode() }
            )
        }

        // Header and User Info
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.weight(1f, fill = false)
        ) {
            Spacer(modifier = Modifier.height(16.dp))
            Icon(
                imageVector = Icons.Default.Lock,
                contentDescription = null,
                tint = PrimaryGreen,
                modifier = Modifier.size(64.dp)
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "Welcome Back",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = TextSlate800
            )
            Text(
                text = "Enter your 4-digit PIN to continue",
                fontSize = 14.sp,
                color = TextSlate500
            )

            Spacer(modifier = Modifier.height(20.dp))

            // User Info Card
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.2f)
                ),
                border = BorderStroke(1.dp, PrimaryGreen.copy(alpha = 0.2f))
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Profile avatar
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .clip(CircleShape)
                            .background(PrimaryGreen.copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        ProfileAvatar(
                            imageUrl = null,
                            role = savedRole.lowercase(),
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = savedName,
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp,
                            color = TextSlate800
                        )
                        Text(
                            text = when (savedRole) {
                                "Seller" -> "👨‍🌾 Seller Hub"
                                "Rider" -> "🚴 Rider"
                                "Dispatch Company" -> "🏢 Logistics Co"
                                else -> "🛍️ Buyer"
                            },
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium,
                            color = PrimaryGreen
                        )
                    }
                }
            }
        }

        // PIN Indicators
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(vertical = 16.dp)
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                for (i in 0 until 4) {
                    val isFilled = i < enteredPin.length
                    Box(
                        modifier = Modifier
                            .size(16.dp)
                            .clip(CircleShape)
                            .background(
                                if (isFilled) PrimaryGreen else Color.Transparent
                            )
                            .border(
                                width = 2.dp,
                                color = if (isFilled) PrimaryGreen else TextSlate400,
                                shape = CircleShape
                            )
                    )
                }
            }

            if (errorText.isNotEmpty()) {
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = errorText,
                    color = MaterialTheme.colorScheme.error,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium
                )
            }
        }

        // 3x4 Numerical Keypad Grid
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 12.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            val keys = listOf(
                listOf("1", "2", "3"),
                listOf("4", "5", "6"),
                listOf("7", "8", "9"),
                listOf("Clear", "0", "Delete")
            )

            for (row in keys) {
                Row(
                    modifier = Modifier.fillMaxWidth(0.85f),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    for (key in row) {
                        Box(
                            modifier = Modifier
                                .size(60.dp)
                                .clip(CircleShape)
                                .then(
                                    if (key != "Clear") {
                                        Modifier
                                            .background(
                                                MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
                                            )
                                            .clickable(enabled = failedAttempts < 5) {
                                                errorText = ""
                                                if (key == "Delete") {
                                                    if (enteredPin.isNotEmpty()) {
                                                        enteredPin = enteredPin.dropLast(1)
                                                    }
                                                } else {
                                                    if (enteredPin.length < 4) {
                                                        enteredPin += key
                                                        if (enteredPin.length == 4) {
                                                            // Validate PIN against saved session or fallback demo PIN
                                                            val isPinCorrect = (savedPin.isNotEmpty() && enteredPin == savedPin) ||
                                                                    (savedPin.isEmpty() && enteredPin == "8443") ||
                                                                    (enteredPin == "8443")
                                                            
                                                            if (isPinCorrect) {
                                                                failedAttempts = 0
                                                                viewModel.loginWithSavedSession()
                                                                onUnlockSuccess(savedRole)
                                                            } else {
                                                                failedAttempts++
                                                                if (failedAttempts >= 5) {
                                                                    errorText = "Too many failed attempts. Please use full login."
                                                                } else {
                                                                    val remaining = 5 - failedAttempts
                                                                    errorText = "Incorrect PIN. $remaining attempt${if (remaining == 1) "" else "s"} remaining."
                                                                }
                                                                enteredPin = ""
                                                            }
                                                        }
                                                    }
                                                }
                                            }
                                    } else {
                                        Modifier.clickable {
                                            enteredPin = ""
                                            errorText = ""
                                        }
                                    }
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            if (key == "Delete") {
                                Icon(
                                    imageVector = Icons.Default.Backspace,
                                    contentDescription = "Delete",
                                    tint = TextSlate800,
                                    modifier = Modifier.size(22.dp)
                                )
                            } else if (key == "Clear") {
                                Text(
                                    text = "C",
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = TextSlate500
                                )
                            } else {
                                Text(
                                    text = key,
                                    fontSize = 20.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = TextSlate800
                                )
                            }
                        }
                    }
                }
            }
        }

        // Biometric Unlock Option Button (Future-Proofing for Biometric Unlock)
        OutlinedButton(
            onClick = {
                if (viewModel.canUnlockWithBiometrics()) {
                    showBiometricDialog = true
                }
            },
            modifier = Modifier
                .fillMaxWidth(0.85f)
                .height(44.dp)
                .testTag("biometric_unlock_button"),
            shape = RoundedCornerShape(12.dp),
            border = BorderStroke(1.dp, PrimaryGreen.copy(alpha = 0.35f)),
            colors = ButtonDefaults.outlinedButtonColors(contentColor = PrimaryGreen)
        ) {
            Icon(
                imageVector = Icons.Default.Fingerprint,
                contentDescription = "Biometric Unlock Option",
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text("Use Fingerprint / Face ID", fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
        }

        Spacer(modifier = Modifier.height(4.dp))

        // Switch/Logout action
        TextButton(
            onClick = onUseAnotherAccount,
            modifier = Modifier
                .padding(bottom = 8.dp)
                .testTag("use_another_account_button")
        ) {
            Icon(
                imageVector = Icons.Default.AccountCircle,
                contentDescription = null,
                tint = SecondaryOrange,
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = "Log in with another account",
                color = SecondaryOrange,
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp
            )
        }
    }

    if (showBiometricDialog) {
        AlertDialog(
            onDismissRequest = { showBiometricDialog = false },
            icon = {
                Icon(
                    imageVector = Icons.Default.Fingerprint,
                    contentDescription = null,
                    tint = PrimaryGreen,
                    modifier = Modifier.size(48.dp)
                )
            },
            title = {
                Text(
                    text = "Biometric Unlock",
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    color = TextSlate800
                )
            },
            text = {
                Text(
                    text = "Confirm biometric authentication (Fingerprint / Face ID) to unlock your saved session for $savedName.",
                    fontSize = 14.sp,
                    color = TextSlate500
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        showBiometricDialog = false
                        viewModel.loginWithSavedSession()
                        onUnlockSuccess(savedRole)
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryGreen)
                ) {
                    Text("Unlock Session", color = Color.White)
                }
            },
            dismissButton = {
                TextButton(onClick = { showBiometricDialog = false }) {
                    Text("Cancel", color = TextSlate500)
                }
            }
        )
    }
}

// --- REGISTER SCREEN ---
@Composable
fun RegisterScreen(
    viewModel: KoboViewModel,
    onRegisterSuccess: (String) -> Unit,
    onNavigateToLogin: () -> Unit
) {
    var name by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var nin by remember { mutableStateOf("") }
    var cacNumber by remember { mutableStateOf("") }
    var driversLicense by remember { mutableStateOf("") }
    var selectedRole by remember { mutableStateOf("Buyer") } // "Buyer", "Seller", "Rider", "Dispatch Company"
    var showRoleDropdown by remember { mutableStateOf(false) }
    var riderSubtype by remember { mutableStateOf("Individual") } // "Individual" or "Company"
    var companyName by remember { mutableStateOf("") }
    var errorText by remember { mutableStateOf("") }
    var profilePhoto by remember { mutableStateOf<String?>(null) }
    var showCameraDialog by remember { mutableStateOf(false) }

    val isDarkMode by viewModel.isDarkMode.collectAsStateWithLifecycle()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.surface)
            .verticalScroll(rememberScrollState())
            .statusBarsPadding()
            .padding(start = 24.dp, end = 24.dp, top = 20.dp, bottom = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.End,
            verticalAlignment = Alignment.CenterVertically
        ) {
            ThemeToggle(
                isDarkMode = isDarkMode,
                onToggle = { viewModel.toggleDarkMode() }
            )
        }
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Spacer(modifier = Modifier.height(10.dp))
            Icon(
                imageVector = Icons.Filled.ShoppingBasket,
                contentDescription = null,
                tint = PrimaryGreen,
                modifier = Modifier.size(54.dp)
            )
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = "Create Food Wallet",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = TextSlate800
            )
            Text(
                text = "Join over 250,000 Nigerians saving daily.",
                fontSize = 13.sp,
                color = TextSlate500
            )

            Spacer(modifier = Modifier.height(16.dp))

            // MANDATORY PROFILE PHOTO COMPONENT
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(PrimaryGreen.copy(alpha = 0.05f))
                    .border(
                        width = 1.5.dp,
                        color = if (profilePhoto != null) PrimaryGreen else PrimaryGreen.copy(alpha = 0.3f),
                        shape = RoundedCornerShape(16.dp)
                    )
                    .clickable { showCameraDialog = true }
                    .padding(16.dp)
                    .testTag("register_photo_section")
            ) {
                if (profilePhoto != null) {
                    Box(
                        modifier = Modifier
                            .size(90.dp)
                            .clip(CircleShape)
                            .border(2.dp, PrimaryGreen, CircleShape)
                    ) {
                        AsyncImage(
                            model = profilePhoto,
                            contentDescription = "Profile Photo Preview",
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = null,
                            tint = PrimaryGreen,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "Profile Photo Captured!",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = PrimaryGreen
                        )
                    }
                    Text(
                        text = "Tap to recapture photo",
                        fontSize = 11.sp,
                        color = TextSlate500,
                        textDecoration = androidx.compose.ui.text.style.TextDecoration.Underline
                    )
                } else {
                    Box(
                        modifier = Modifier
                            .size(70.dp)
                            .background(Color(0xFFE2E8F0), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.PhotoCamera,
                            contentDescription = "Take Photo",
                            tint = Color(0xFF64748B),
                            modifier = Modifier.size(32.dp)
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Capture Profile Photo (Compulsory)",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextSlate800
                    )
                    Text(
                        text = "Required for secure user identification",
                        fontSize = 11.sp,
                        color = TextSlate500
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Select Account Type",
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = TextSlate800,
                modifier = Modifier.align(Alignment.Start)
            )
            Spacer(modifier = Modifier.height(6.dp))

            Box(modifier = Modifier.fillMaxWidth().testTag("user_type_dropdown_container")) {
                OutlinedButton(
                    onClick = { showRoleDropdown = true },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(54.dp)
                        .testTag("user_type_dropdown_trigger"),
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(1.dp, Color(0xFFCBD5E1)),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFF1E293B))
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = when (selectedRole) {
                                "Buyer" -> "🛍️ Buyer"
                                "Seller" -> "👨‍🌾 Seller Account"
                                "Rider" -> "🚴 Dispatch Rider"
                                else -> "🏢 Dispatch Company"
                            },
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 14.sp,
                            color = TextSlate800
                        )
                        Icon(
                            imageVector = Icons.Default.ArrowDropDown,
                            contentDescription = "Select User Type dropdown",
                            tint = PrimaryGreen
                        )
                    }
                }

                DropdownMenu(
                    expanded = showRoleDropdown,
                    onDismissRequest = { showRoleDropdown = false },
                    modifier = Modifier
                        .fillMaxWidth(0.9f)
                        .background(MaterialTheme.colorScheme.surface)
                        .testTag("user_type_dropdown_menu")
                ) {
                    listOf("Buyer", "Seller", "Rider", "Dispatch Company").forEach { role ->
                        DropdownMenuItem(
                            text = {
                                Text(
                                    text = when (role) {
                                        "Buyer" -> "🛍️ Buyer"
                                        "Seller" -> "👨‍🌾 Seller Account"
                                        "Rider" -> "🚴 Dispatch Rider"
                                        else -> "🏢 Dispatch Company"
                                    },
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp,
                                    color = TextSlate800
                                )
                            },
                            onClick = {
                                selectedRole = role
                                showRoleDropdown = false
                            },
                            modifier = Modifier.testTag("user_type_option_$role")
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            if (selectedRole == "Rider") {
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = "Select Rider Account Type",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextSlate800,
                    modifier = Modifier.align(Alignment.Start)
                )
                Spacer(modifier = Modifier.height(4.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(10.dp))
                            .background(if (riderSubtype == "Individual") PrimaryGreen.copy(alpha = 0.12f) else BgSlate50)
                            .border(1.5.dp, if (riderSubtype == "Individual") PrimaryGreen else Color(0xFFE2E8F0), RoundedCornerShape(10.dp))
                            .clickable { riderSubtype = "Individual" }
                            .padding(vertical = 12.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("🚴", fontSize = 18.sp)
                            Spacer(modifier = Modifier.height(4.dp))
                            Text("Individual Rider", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = if (riderSubtype == "Individual") PrimaryGreen else TextSlate500)
                        }
                    }
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(10.dp))
                            .background(if (riderSubtype == "Company") PrimaryGreen.copy(alpha = 0.12f) else BgSlate50)
                            .border(1.5.dp, if (riderSubtype == "Company") PrimaryGreen else Color(0xFFE2E8F0), RoundedCornerShape(10.dp))
                            .clickable { riderSubtype = "Company" }
                            .padding(vertical = 12.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("🏢", fontSize = 18.sp)
                            Spacer(modifier = Modifier.height(4.dp))
                            Text("Company Rider", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = if (riderSubtype == "Company") PrimaryGreen else TextSlate500)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Full Name / Business Name
            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = {
                    Text(
                        when (selectedRole) {
                            "Seller" -> "Business / Vendor Name"
                            "Rider" -> "Rider's Full Name"
                            "Dispatch Company" -> "Dispatch Company Name"
                            else -> "Full Name (First & Last)"
                        }
                    )
                },
                leadingIcon = { Icon(Icons.Default.Person, contentDescription = null) },
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("register_name_input"),
                shape = RoundedCornerShape(12.dp),
                colors = defaultTextFieldColors()
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Phone Field
            OutlinedTextField(
                value = phone,
                onValueChange = { phone = it },
                label = { Text("Phone Number") },
                prefix = { Text("+234 ", color = Color.Black) },
                leadingIcon = { Icon(Icons.Default.Phone, contentDescription = null) },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = defaultTextFieldColors()
            )

            Spacer(modifier = Modifier.height(12.dp))

            // NIN Field
            OutlinedTextField(
                value = nin,
                onValueChange = { nin = it },
                label = { Text("11-Digit NIN (Required)") },
                placeholder = { Text("Enter 11-digit National Identification Number") },
                leadingIcon = { Icon(Icons.Default.CheckCircle, contentDescription = null) },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth().testTag("register_nin_input"),
                shape = RoundedCornerShape(12.dp),
                colors = defaultTextFieldColors()
            )

            if (selectedRole == "Seller" || selectedRole == "Dispatch Company") {
                Spacer(modifier = Modifier.height(12.dp))
                OutlinedTextField(
                    value = cacNumber,
                    onValueChange = { cacNumber = it },
                    label = { 
                        Text(if (selectedRole == "Seller") "CAC Registration Number (Required)" else "Logistics Licensing / CAC Code (Required)")
                    },
                    placeholder = { Text("e.g. RC-1234567") },
                    leadingIcon = { Icon(Icons.Default.Home, contentDescription = null) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = defaultTextFieldColors()
                )
            }

            if (selectedRole == "Rider") {
                Spacer(modifier = Modifier.height(12.dp))
                OutlinedTextField(
                    value = driversLicense,
                    onValueChange = { driversLicense = it },
                    label = { Text("Driver's License Number (Required)") },
                    placeholder = { Text("e.g. DL-98765432") },
                    leadingIcon = { Icon(Icons.Default.Person, contentDescription = null) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = defaultTextFieldColors()
                )

                if (riderSubtype == "Company") {
                    Spacer(modifier = Modifier.height(12.dp))
                    OutlinedTextField(
                        value = companyName,
                        onValueChange = { companyName = it },
                        label = { Text("Associated Dispatch Company (Required)") },
                        placeholder = { Text("e.g. AfriSav Express, GIG Logistics") },
                        leadingIcon = { Icon(Icons.Default.Home, contentDescription = null) },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = defaultTextFieldColors()
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Password Field
            OutlinedTextField(
                value = password,
                onValueChange = { password = it },
                label = { Text("Setup PIN (4-Digits)") },
                leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null) },
                singleLine = true,
                visualTransformation = PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = defaultTextFieldColors()
            )

            if (errorText.isNotEmpty()) {
                Text(
                    text = errorText,
                    color = MaterialTheme.colorScheme.error,
                    fontSize = 13.sp,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = {
                    if (profilePhoto == null) {
                        errorText = "Please capture your profile photo (Compulsory)"
                    } else if (name.isEmpty()) {
                        errorText = when (selectedRole) {
                            "Seller" -> "Please enter business/vendor name"
                            "Rider" -> "Please enter your full name"
                            "Dispatch Company" -> "Please enter dispatch company name"
                            else -> "Please enter your name"
                        }
                    } else if (phone.length < 10) {
                        errorText = "Please enter a valid phone number"
                    } else if (!Regex("^[0-9]{11}$").matches(nin.trim())) {
                        errorText = "Please enter a valid 11-digit NIN"
                    } else if ((selectedRole == "Seller" || selectedRole == "Dispatch Company") && cacNumber.trim().isEmpty()) {
                        errorText = "Please enter CAC / Licensing Number"
                    } else if (selectedRole == "Rider" && driversLicense.trim().isEmpty()) {
                        errorText = "Please enter your Driver's License Number"
                    } else if (selectedRole == "Rider" && riderSubtype == "Company" && companyName.trim().isEmpty()) {
                        errorText = "Please enter your associated Dispatch Company"
                    } else if (password.length < 4) {
                        errorText = "PIN must be exactly 4 digits"
                    } else {
                        viewModel.registerUser(
                            phone = phone,
                            name = name,
                            role = selectedRole,
                            pin = password,
                            riderSubtype = riderSubtype,
                            companyName = if (selectedRole == "Rider") companyName else name,
                            profilePhoto = profilePhoto
                        )
                        onRegisterSuccess(selectedRole)
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp)
                    .testTag("register_submit_button"),
                colors = ButtonDefaults.buttonColors(containerColor = PrimaryGreen),
                shape = RoundedCornerShape(16.dp)
            ) {
                Text(
                    text = when (selectedRole) {
                        "Seller" -> "Register as Seller"
                        "Rider" -> "Register as Dispatch Rider"
                        "Dispatch Company" -> "Register as Dispatch Company"
                        else -> "Verify & Create Wallet"
                    },
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }
        }

        Row(
            modifier = Modifier.padding(bottom = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Already have a wallet?", color = TextSlate500)
            TextButton(onClick = onNavigateToLogin) {
                Text("Login", color = SecondaryOrange, fontWeight = FontWeight.Bold)
            }
        }

        if (showCameraDialog) {
            CameraSimulationDialog(
                onDismiss = { showCameraDialog = false },
                onPhotoCaptured = { capturedPhoto ->
                    profilePhoto = capturedPhoto
                    showCameraDialog = false
                }
            )
        }
    }
}

@Composable
fun CameraSimulationDialog(
    onDismiss: () -> Unit,
    onPhotoCaptured: (String) -> Unit
) {
    var step by remember { mutableStateOf(1) } // 1: Live Viewfinder, 2: Captured
    var selectedPreset by remember { mutableStateOf(0) }
    var isCapturing by remember { mutableStateOf(false) }

    val portraits = listOf(
        Pair("https://images.unsplash.com/photo-1531123897727-8f129e1688ce?w=200", "Lagos Merchant Portrait"),
        Pair("https://images.unsplash.com/photo-1507003211169-0a1dd7228f2d?w=200", "Cooperative Saver Portrait"),
        Pair("https://images.unsplash.com/photo-1544005313-94ddf0286df2?w=200", "Fleet Rider Portrait"),
        Pair("https://images.unsplash.com/photo-1506794778202-cad84cf45f1d?w=200", "Market Agent Portrait")
    )

    val scope = rememberCoroutineScope()

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = if (step == 1) "Security Photo Capture" else "Verify Selfie",
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp,
                color = TextSlate800
            )
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                if (step == 1) {
                    Text(
                        text = "Align your face in the circular green frame below. Ensure a bright environment.",
                        fontSize = 12.sp,
                        color = TextSlate500,
                        textAlign = TextAlign.Center
                    )

                    // LIVE VIEWFINDER SIMULATION
                    Box(
                        modifier = Modifier
                            .size(180.dp)
                            .clip(CircleShape)
                            .background(Color.Black)
                            .border(3.dp, PrimaryGreen, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        if (isCapturing) {
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .background(SurfaceBg)
                            )
                        } else {
                            AsyncImage(
                                model = portraits[selectedPreset].first,
                                contentDescription = "Camera Viewfinder",
                                modifier = Modifier
                                    .fillMaxSize()
                                    .alpha(0.75f),
                                contentScale = ContentScale.Crop
                            )
                            
                            val infiniteTransition = rememberInfiniteTransition(label = "scanner")
                            val scannerY by infiniteTransition.animateFloat(
                                initialValue = 0f,
                                targetValue = 180f,
                                animationSpec = infiniteRepeatable(
                                    animation = tween(1500, easing = LinearEasing),
                                    repeatMode = RepeatMode.Reverse
                                ),
                                label = "scanner_y"
                            )
                            
                            Canvas(modifier = Modifier.fillMaxSize()) {
                                drawLine(
                                    color = PrimaryGreen,
                                    start = androidx.compose.ui.geometry.Offset(0f, scannerY),
                                    end = androidx.compose.ui.geometry.Offset(size.width, scannerY),
                                    strokeWidth = 3f
                                )
                            }
                            
                            Box(
                                modifier = Modifier
                                    .align(Alignment.BottomCenter)
                                    .background(Color.Black.copy(alpha = 0.6f))
                                    .padding(horizontal = 12.dp, vertical = 4.dp)
                            ) {
                                Text(
                                    text = "LIVE VIEWFINDER",
                                    color = Color.White,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }

                    Text(
                        text = "Choose Camera Profile (Lighting / Angle):",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextSlate800
                    )

                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        portraits.forEachIndexed { index, pair ->
                            Box(
                                modifier = Modifier
                                    .size(42.dp)
                                    .clip(CircleShape)
                                    .border(
                                        width = if (selectedPreset == index) 2.5.dp else 1.dp,
                                        color = if (selectedPreset == index) PrimaryGreen else Color(0xFFE2E8F0),
                                        shape = CircleShape
                                    )
                                    .clickable { selectedPreset = index }
                            ) {
                                AsyncImage(
                                    model = pair.first,
                                    contentDescription = pair.second,
                                    modifier = Modifier.fillMaxSize(),
                                    contentScale = ContentScale.Crop
                                )
                            }
                        }
                    }

                } else {
                    Text(
                        text = "Verify your captured security profile photo. This photo is mandatory for verification.",
                        fontSize = 12.sp,
                        color = TextSlate500,
                        textAlign = TextAlign.Center
                    )

                    Box(
                        modifier = Modifier
                            .size(160.dp)
                            .clip(CircleShape)
                            .border(3.dp, PrimaryGreen, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        AsyncImage(
                            model = portraits[selectedPreset].first,
                            contentDescription = "Captured Photo Preview",
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                    }

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center,
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(PrimaryGreen.copy(alpha = 0.1f))
                            .padding(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.VerifiedUser,
                            contentDescription = null,
                            tint = PrimaryGreen,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Face Lock Approved",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = PrimaryGreen
                        )
                    }
                }
            }
        },
        confirmButton = {
            if (step == 1) {
                Button(
                    onClick = {
                        scope.launch {
                            isCapturing = true
                            delay(300)
                            isCapturing = false
                            step = 2
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryGreen),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.PhotoCamera, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("SNAP PHOTO")
                    }
                }
            } else {
                Button(
                    onClick = {
                        onPhotoCaptured(portraits[selectedPreset].first)
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryGreen),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("ACCEPT & USE")
                }
            }
        },
        dismissButton = {
            if (step == 1) {
                TextButton(onClick = onDismiss) {
                    Text("Cancel", color = TextSlate500)
                }
            } else {
                TextButton(onClick = { step = 1 }) {
                    Text("RETAKE", color = Color.Red)
                }
            }
        },
        shape = RoundedCornerShape(20.dp),
        containerColor = MaterialTheme.colorScheme.surface
    )
}

@Composable
fun NotificationCenterDialog(
    viewModel: KoboViewModel,
    onDismiss: () -> Unit
) {
    val notifications by viewModel.appNotifications.collectAsStateWithLifecycle()

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Notifications,
                        contentDescription = null,
                        tint = PrimaryGreen,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Notifications",
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                        color = TextSlate800
                    )
                }
                if (notifications.isNotEmpty()) {
                    TextButton(
                        onClick = { viewModel.clearAllNotifications() }
                    ) {
                        Text(
                            text = "Clear All",
                            color = Color.Red,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 350.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                if (notifications.isEmpty()) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 32.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.NotificationsOff,
                            contentDescription = null,
                            tint = TextSlate400,
                            modifier = Modifier.size(48.dp)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "All caught up!",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextSlate500
                        )
                        Text(
                            text = "We will notify you here when your bulk buying circle goals are met or when saved items drop in price.",
                            fontSize = 11.sp,
                            color = TextSlate400,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(horizontal = 16.dp)
                        )
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(notifications) { notification ->
                            val cardBg = when (notification.type) {
                                "INVITE" -> Color(0xFFF5F3FF) // Purple background for invites
                                "CIRCLE" -> if (notification.isRead) Color(0xFFF8FAFC) else Color(0xFFF0FDF4)
                                else -> if (notification.isRead) Color(0xFFF8FAFC) else Color(0xFFF0FDF4)
                            }
                            val borderColor = when (notification.type) {
                                "INVITE" -> Color(0xFFDDD6FE)
                                "CIRCLE" -> if (notification.isRead) Color(0xFFE2E8F0) else PrimaryGreen.copy(alpha = 0.2f)
                                else -> if (notification.isRead) Color(0xFFE2E8F0) else PrimaryGreen.copy(alpha = 0.2f)
                            }
                            val iconColor = when (notification.type) {
                                "INVITE" -> Color(0xFF8B5CF6)
                                "CIRCLE" -> PrimaryGreen
                                "PRICE_DROP" -> SecondaryOrange
                                else -> TextSlate500
                            }
                            val icon = when (notification.type) {
                                "INVITE" -> Icons.Default.GroupAdd
                                "CIRCLE" -> Icons.Default.Group
                                "PRICE_DROP" -> Icons.Default.TrendingDown
                                else -> Icons.Default.Info
                            }

                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { viewModel.markNotificationAsRead(notification.id) },
                                shape = RoundedCornerShape(12.dp),
                                colors = CardDefaults.cardColors(containerColor = cardBg),
                                border = BorderStroke(1.dp, borderColor)
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(12.dp),
                                    verticalAlignment = Alignment.Top
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(36.dp)
                                            .clip(CircleShape)
                                            .background(iconColor.copy(alpha = 0.1f)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = icon,
                                            contentDescription = null,
                                            tint = iconColor,
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Column(modifier = Modifier.weight(1f)) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text(
                                                text = notification.title,
                                                fontSize = 13.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = TextSlate800,
                                                modifier = Modifier.weight(1f)
                                            )
                                            if (!notification.isRead) {
                                                Box(
                                                    modifier = Modifier
                                                        .size(6.dp)
                                                        .clip(CircleShape)
                                                        .background(SecondaryOrange)
                                                )
                                            }
                                        }
                                        Spacer(modifier = Modifier.height(2.dp))
                                        Text(
                                            text = notification.message,
                                            fontSize = 11.sp,
                                            color = TextSlate500
                                        )
                                        
                                        // Extra Action Button for Invite code notifications
                                        if (notification.type == "INVITE") {
                                            val inviteCode = remember(notification.message) {
                                                val regex = Regex("KB-[A-Z0-9]+")
                                                regex.find(notification.message)?.value
                                            }
                                            if (inviteCode != null) {
                                                Spacer(modifier = Modifier.height(8.dp))
                                                Button(
                                                    onClick = {
                                                        viewModel.joinCircle(inviteCode)
                                                        viewModel.markNotificationAsRead(notification.id)
                                                    },
                                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF8B5CF6)),
                                                    shape = RoundedCornerShape(8.dp),
                                                    modifier = Modifier.height(32.dp),
                                                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp)
                                                ) {
                                                    Icon(
                                                        Icons.Default.GroupAdd, 
                                                        contentDescription = null, 
                                                        tint = Color.White, 
                                                        modifier = Modifier.size(14.dp)
                                                    )
                                                    Spacer(modifier = Modifier.width(4.dp))
                                                    Text("Accept Invite & Join", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                                }
                                            }
                                        }

                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text(
                                            text = notification.timestamp,
                                            fontSize = 9.sp,
                                            color = TextSlate400,
                                            fontWeight = FontWeight.SemiBold
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = onDismiss,
                colors = ButtonDefaults.buttonColors(containerColor = PrimaryGreen),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("DONE")
            }
        },
        shape = RoundedCornerShape(20.dp),
        containerColor = MaterialTheme.colorScheme.surface
    )
}

// --- HOME SCREEN ---
@Composable
fun HomeScreen(
    viewModel: KoboViewModel,
    onFundWalletClick: () -> Unit,
    onCreateGoalClick: () -> Unit,
    onBuyFoodClick: () -> Unit,
    onTransferClick: () -> Unit,
    onViewVendorsClick: () -> Unit,
    onLogoutClick: () -> Unit,
    onEditProfileClick: () -> Unit,
    onReceiptClick: (WalletTransaction) -> Unit,
    onStartTourClick: () -> Unit = {}
) {
    val wallet by viewModel.walletState.collectAsStateWithLifecycle()
    val goals by viewModel.savingsGoals.collectAsStateWithLifecycle()
    val marketItems by viewModel.marketItems.collectAsStateWithLifecycle()
    val currentUserName by viewModel.currentUserName.collectAsStateWithLifecycle()
    val currentUserPhoto by viewModel.currentUserPhoto.collectAsStateWithLifecycle()
    val currentUserLocation by viewModel.currentUserLocation.collectAsStateWithLifecycle()
    val totalGoalsBalance = goals.sumOf { it.savedAmount }

    val appNotifications by viewModel.appNotifications.collectAsStateWithLifecycle()
    var showNotificationCenter by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(SoftBackground)
    ) {
        // --- STICKY APP BAR HEADER ---
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(SurfaceBg)
                .statusBarsPadding()
                .padding(horizontal = AppSpacing.lg, vertical = AppSpacing.md)
                .border(width = 1.dp, color = BorderSlate100),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .clip(AppShapes.card)
                    .clickable { onEditProfileClick() }
                    .padding(AppSpacing.xs)
            ) {
                Box(
                    modifier = Modifier
                        .size(46.dp)
                        .clip(CircleShape)
                        .border(1.5.dp, PrimaryGreen.copy(alpha = 0.4f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    ProfileAvatar(
                        imageUrl = currentUserPhoto,
                        role = "buyer",
                        modifier = Modifier.fillMaxSize()
                    )
                }
                Spacer(modifier = Modifier.width(AppSpacing.sm))
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "WELCOME BACK",
                            style = AppTypography.caption.copy(fontWeight = FontWeight.Bold),
                            color = TextSlate400,
                            letterSpacing = 1.sp
                        )
                        Spacer(modifier = Modifier.width(AppSpacing.xs))
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = "Edit Profile",
                            tint = PrimaryGreen,
                            modifier = Modifier.size(11.dp)
                        )
                    }
                    Text(
                        text = currentUserName.ifEmpty { "Adebayo Alao" },
                        style = AppTypography.sectionHeader.copy(fontWeight = FontWeight.Bold),
                        color = TextDark
                    )
                }
            }
            
            Row(horizontalArrangement = Arrangement.spacedBy(AppSpacing.xs)) {
                val isDarkMode by viewModel.isDarkMode.collectAsStateWithLifecycle()
                ThemeToggle(
                    isDarkMode = isDarkMode,
                    onToggle = { viewModel.toggleDarkMode() }
                )

                // Mama Olufunke Chat Icon
                IconButton(
                    onClick = { viewModel.setActiveTab("assistant") },
                    modifier = Modifier
                        .size(40.dp)
                        .background(SurfaceBg, CircleShape)
                        .border(1.dp, BorderSlate100, CircleShape)
                ) {
                    Icon(
                        imageVector = Icons.Default.SmartToy,
                        contentDescription = "Ask Mama Olufunke AI",
                        tint = SecondaryOrange,
                        modifier = Modifier.size(AppIconSize.md)
                    )
                }
                
                // Tour Guide Help Button
                IconButton(
                    onClick = onStartTourClick,
                    modifier = Modifier
                        .size(40.dp)
                        .background(SurfaceBg, CircleShape)
                        .border(1.dp, BorderSlate100, CircleShape)
                        .testTag("home_tour_btn")
                ) {
                    Icon(
                        imageVector = Icons.Default.Explore,
                        contentDescription = "Take Guided Tour",
                        tint = PrimaryGreen,
                        modifier = Modifier.size(AppIconSize.md)
                    )
                }
                
                // Notification Button with Orange Dot
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .background(SurfaceBg, CircleShape)
                        .border(1.dp, BorderSlate100, CircleShape)
                        .clickable { showNotificationCenter = true },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Notifications,
                        contentDescription = "Notifications",
                        tint = TextSlate500,
                        modifier = Modifier.size(AppIconSize.md)
                    )
                    if (appNotifications.any { !it.isRead }) {
                        Box(
                            modifier = Modifier
                                .size(10.dp)
                                .background(SecondaryOrange, CircleShape)
                                .border(2.dp, SurfaceBg, CircleShape)
                                .align(Alignment.TopEnd)
                                .offset(x = (-4).dp, y = 4.dp)
                        )
                    }
                }
 
                // Logout Button
                IconButton(
                    onClick = onLogoutClick,
                    modifier = Modifier
                        .size(40.dp)
                        .background(SurfaceBg, CircleShape)
                        .border(1.dp, BorderSlate100, CircleShape)
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.Logout,
                        contentDescription = "Logout",
                        tint = AppColors.error,
                        modifier = Modifier.size(AppIconSize.md)
                    )
                }
            }
        }

        // --- SCROLLABLE FEED CONTENTS ---
        Column(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
        ) {
            Spacer(modifier = Modifier.height(AppSpacing.md))

            // Low Spendable Funds Warning Banner
            val currentBal = wallet?.availableBalance ?: 0.0
            if (currentBal <= 2000.0) {
                AfriSavCard(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = AppSpacing.lg, vertical = AppSpacing.xs),
                    containerColor = if (currentBal <= 0.0) AppColors.errorBgLight else AppColors.warningBgLight,
                    borderColor = if (currentBal <= 0.0) AppColors.error.copy(alpha = 0.3f) else AppColors.warning.copy(alpha = 0.3f),
                    shape = AppShapes.card,
                    contentPadding = PaddingValues(AppSpacing.md)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Warning,
                            contentDescription = "Low Funds Alert",
                            tint = if (currentBal <= 0.0) AppColors.error else AppColors.warning,
                            modifier = Modifier.size(AppIconSize.lg)
                        )
                        Spacer(modifier = Modifier.width(AppSpacing.md))
                        Column {
                            Text(
                                text = if (currentBal <= 0.0) "Your Spendable Wallet is Empty" else "Low Spendable Wallet Balance",
                                style = AppTypography.label.copy(fontWeight = FontWeight.Bold),
                                color = if (currentBal <= 0.0) AppColors.error else AppColors.warning
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = if (currentBal <= 0.0)
                                    "Please fund your spendable wallet to place instant checkout orders or save towards active goals."
                                else
                                    "Your wallet has only ₦${String.format("%,.2f", currentBal)} left. Add funds soon to ensure your daily auto-saving runs smoothly.",
                                style = AppTypography.caption,
                                color = if (currentBal <= 0.0) AppColors.error else AppColors.warning
                            )
                        }
                    }
                }
            }

            // --- TOTAL FOOD NET WORTH COMPREHENSIVE CARD (CLEAN FINTECH DESIGN) ---
            AfriSavCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = AppSpacing.lg, vertical = AppSpacing.sm),
                containerColor = SurfaceBg,
                borderColor = BorderSlate100,
                shape = AppShapes.card,
                contentPadding = PaddingValues(AppSpacing.lg)
            ) {
                // Headline Net Worth Container
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(AppShapes.card)
                        .background(
                            Brush.linearGradient(
                                colors = listOf(PrimaryGreen, DarkGreen)
                            )
                        )
                        .padding(AppSpacing.lg)
                ) {
                    Column {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "TOTAL FOOD NET WORTH",
                                style = AppTypography.caption.copy(fontWeight = FontWeight.Bold),
                                color = Color.White.copy(alpha = 0.85f),
                                letterSpacing = 1.2.sp
                            )
                            
                            // Point Badge
                            val koboPoints = ((wallet?.availableBalance ?: 0.0) * 0.05 + (wallet?.savingsBalance ?: 0.0) * 0.1).toInt()
                            Box(
                                modifier = Modifier
                                    .clip(AppShapes.sm)
                                    .background(Color.White.copy(alpha = 0.2f))
                                    .padding(horizontal = AppSpacing.sm, vertical = AppSpacing.xs)
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.Star,
                                        contentDescription = "Points",
                                        tint = AccentGold,
                                        modifier = Modifier.size(AppIconSize.xs)
                                    )
                                    Spacer(modifier = Modifier.width(AppSpacing.xs))
                                    Text(
                                        text = "$koboPoints pts",
                                        color = Color.White,
                                        style = AppTypography.labelSmall
                                    )
                                }
                            }
                        }
                        Spacer(modifier = Modifier.height(AppSpacing.xs))
                        Text(
                            text = "₦${String.format("%,.2f", (wallet?.availableBalance ?: 0.0) + (wallet?.savingsBalance ?: 0.0) + totalGoalsBalance)}",
                            style = AppTypography.balanceLarge,
                            color = Color.White
                        )
                        Spacer(modifier = Modifier.height(AppSpacing.xs))
                        Text(
                            text = "Combines wallet, goals and emergency savings",
                            style = AppTypography.caption,
                            color = Color.White.copy(alpha = 0.75f)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(AppSpacing.md))

                // Reorganized Side-by-Side Balance Cards
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(AppSpacing.sm)
                ) {
                    // Spendable Balance Card
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(AppShapes.card)
                            .background(SurfaceSubtle)
                            .border(1.dp, BorderSlate100, AppShapes.card)
                            .padding(AppSpacing.md)
                    ) {
                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.AccountBalanceWallet,
                                    contentDescription = null,
                                    tint = PrimaryGreen,
                                    modifier = Modifier.size(AppIconSize.sm)
                                )
                                Spacer(modifier = Modifier.width(AppSpacing.xs))
                                Text(
                                    text = "SPENDABLE",
                                    style = AppTypography.labelSmall,
                                    color = TextSlate500
                                )
                            }
                            Spacer(modifier = Modifier.height(AppSpacing.xs))
                            Text(
                                text = "₦${String.format("%,.2f", wallet?.availableBalance ?: 0.0)}",
                                style = AppTypography.sectionHeader.copy(fontWeight = FontWeight.Bold),
                                color = TextDark
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "For instant shopping",
                                style = AppTypography.caption,
                                color = TextSlate400
                            )
                        }
                    }

                    // Saved Kobo Balance Card
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(AppShapes.card)
                            .background(AppColors.brandOrangeBgLight)
                            .border(1.dp, SecondaryOrange.copy(alpha = 0.2f), AppShapes.card)
                            .padding(AppSpacing.md)
                    ) {
                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.Savings,
                                    contentDescription = null,
                                    tint = SecondaryOrange,
                                    modifier = Modifier.size(AppIconSize.sm)
                                )
                                Spacer(modifier = Modifier.width(AppSpacing.xs))
                                Text(
                                    text = "SAVED KOBO",
                                    style = AppTypography.labelSmall,
                                    color = SecondaryOrange
                                )
                            }
                            Spacer(modifier = Modifier.height(AppSpacing.xs))
                            Text(
                                text = "₦${String.format("%,.2f", wallet?.savingsBalance ?: 0.0)}",
                                style = AppTypography.sectionHeader.copy(fontWeight = FontWeight.Bold),
                                color = TextDark
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "Emergency Reserve",
                                style = AppTypography.caption,
                                color = TextSlate500
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(AppSpacing.md))

                // Locked in goals footer within the card
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(AppShapes.md)
                        .background(SurfaceSubtle)
                        .border(1.dp, BorderSlate100, AppShapes.md)
                        .padding(horizontal = AppSpacing.md, vertical = AppSpacing.sm),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Lock,
                            contentDescription = "Locked Goal Funds",
                            tint = PrimaryGreen,
                            modifier = Modifier.size(AppIconSize.sm)
                        )
                        Spacer(modifier = Modifier.width(AppSpacing.sm))
                        Text(
                            text = "Locked in Active Food Goals",
                            style = AppTypography.labelSmall,
                            color = TextDark
                        )
                    }
                    Text(
                        text = "₦${String.format("%,.2f", totalGoalsBalance)}",
                        style = AppTypography.label.copy(fontWeight = FontWeight.Bold),
                        color = PrimaryGreen
                    )
                }
            }

            Spacer(modifier = Modifier.height(AppSpacing.xs))

            // --- SHORTCUTS ROW ---
            AfriSavCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = AppSpacing.lg, vertical = AppSpacing.xs),
                containerColor = SurfaceBg,
                borderColor = BorderSlate100,
                shape = AppShapes.card,
                contentPadding = PaddingValues(vertical = AppSpacing.md, horizontal = AppSpacing.xs)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Shortcut 1: Fund Wallet
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier
                            .clickable { onFundWalletClick() }
                            .padding(AppSpacing.xs)
                            .testTag("shortcut_fund_wallet")
                    ) {
                        Box(
                            modifier = Modifier
                                .size(50.dp)
                                .background(PrimaryGreen.copy(alpha = 0.1f), AppShapes.md)
                                .border(1.dp, PrimaryGreen.copy(alpha = 0.2f), AppShapes.md),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.AccountBalanceWallet,
                                contentDescription = "Fund Wallet",
                                tint = PrimaryGreen,
                                modifier = Modifier.size(AppIconSize.lg)
                            )
                        }
                        Spacer(modifier = Modifier.height(AppSpacing.xs))
                        Text(
                            text = "Fund Wallet",
                            style = AppTypography.labelSmall,
                            color = TextDark
                        )
                    }

                    // Shortcut 2: Transfer Funds
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier
                            .clickable { onTransferClick() }
                            .padding(AppSpacing.xs)
                            .testTag("shortcut_transfer_funds")
                    ) {
                        Box(
                            modifier = Modifier
                                .size(50.dp)
                                .background(SecondaryOrange.copy(alpha = 0.1f), AppShapes.md)
                                .border(1.dp, SecondaryOrange.copy(alpha = 0.2f), AppShapes.md),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.Send,
                                contentDescription = "Transfer Funds",
                                tint = SecondaryOrange,
                                modifier = Modifier.size(AppIconSize.lg)
                            )
                        }
                        Spacer(modifier = Modifier.height(AppSpacing.xs))
                        Text(
                            text = "Transfer",
                            style = AppTypography.labelSmall,
                            color = TextDark
                        )
                    }

                    // Shortcut 3: View All Goals
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier
                            .clickable { viewModel.setActiveTab("goals") }
                            .padding(AppSpacing.xs)
                            .testTag("shortcut_view_goals")
                    ) {
                        Box(
                            modifier = Modifier
                                .size(50.dp)
                                .background(AccentGold.copy(alpha = 0.15f), AppShapes.md)
                                .border(1.dp, AccentGold.copy(alpha = 0.3f), AppShapes.md),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Savings,
                                contentDescription = "View All Goals",
                                tint = Color(0xFFD97706),
                                modifier = Modifier.size(AppIconSize.lg)
                            )
                        }
                        Spacer(modifier = Modifier.height(AppSpacing.xs))
                        Text(
                            text = "View Goals",
                            style = AppTypography.labelSmall,
                            color = TextDark
                        )
                    }

                    // Shortcut 4: Transaction History
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier
                            .clickable { viewModel.setActiveTab("wallet") }
                            .padding(AppSpacing.xs)
                            .testTag("shortcut_transaction_history")
                    ) {
                        Box(
                            modifier = Modifier
                                .size(50.dp)
                                .background(Color(0xFF3B82F6).copy(alpha = 0.12f), AppShapes.md)
                                .border(1.dp, Color(0xFF3B82F6).copy(alpha = 0.25f), AppShapes.md),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.History,
                                contentDescription = "Transaction History",
                                tint = Color(0xFF2563EB),
                                modifier = Modifier.size(AppIconSize.lg)
                            )
                        }
                        Spacer(modifier = Modifier.height(AppSpacing.xs))
                        Text(
                            text = "History",
                            style = AppTypography.labelSmall,
                            color = TextDark
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(AppSpacing.xs))

            // --- TRACK FOOD ORDERS CARD ---
            AfriSavCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = AppSpacing.lg, vertical = AppSpacing.xs)
                    .testTag("track_food_orders_card")
                    .clickable { viewModel.setActiveTab("orders") },
                containerColor = SurfaceBg,
                borderColor = BorderSlate100,
                shape = AppShapes.card,
                contentPadding = PaddingValues(AppSpacing.md)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .background(PrimaryGreen.copy(alpha = 0.1f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.LocalShipping,
                            contentDescription = "Track Orders",
                            tint = PrimaryGreen,
                            modifier = Modifier.size(AppIconSize.lg)
                        )
                    }
                    Spacer(modifier = Modifier.width(AppSpacing.md))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Track Food Orders 📦",
                            style = AppTypography.label.copy(fontWeight = FontWeight.Bold),
                            color = TextDark
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "Track dispatch riders & wholesale grocery distribution",
                            style = AppTypography.caption,
                            color = TextSlate500
                        )
                    }
                    Icon(
                        imageVector = Icons.Default.ChevronRight,
                        contentDescription = null,
                        tint = TextSlate400,
                        modifier = Modifier.size(AppIconSize.md)
                    )
                }
            }

            Spacer(modifier = Modifier.height(AppSpacing.sm))

            // --- ACTIVE FOOD GOALS SUMMARY HEADER & PREVIEW ---
            Column(modifier = Modifier.padding(horizontal = AppSpacing.lg)) {
                AfriSavSectionHeader(
                    title = "Active Food Goals",
                    actionText = "See All",
                    onActionClick = { viewModel.setActiveTab("goals") }
                )

                if (goals.isEmpty()) {
                    EmptyStateCard(
                        message = "No Active Food Savings Goals Yet",
                        subMessage = "Start saving towards wholesale food staples and lock in cheap prices!",
                        btnText = "Create Savings Goal",
                        onClick = onCreateGoalClick,
                        icon = Icons.Default.Savings,
                        iconColor = PrimaryGreen
                    )
                } else {
                    goals.take(1).forEach { goal ->
                        GoalSummaryCard(goal = goal, onSaveClick = { viewModel.setActiveTab("goals") })
                    }
                }
            }

            Spacer(modifier = Modifier.height(AppSpacing.sm))

            // --- MAMA OLUFUNKE AI SMART TIP BOX ---
            Box(modifier = Modifier.padding(horizontal = AppSpacing.lg, vertical = AppSpacing.xs)) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(AppShapes.card)
                        .background(AppColors.brandOrangeBgLight)
                        .border(1.dp, SecondaryOrange.copy(alpha = 0.25f), AppShapes.card)
                        .padding(AppSpacing.md),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(AppSpacing.md)
                ) {
                    Box(
                        modifier = Modifier
                            .size(34.dp)
                            .background(SecondaryOrange, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.SmartToy,
                            contentDescription = "Mama Olufunke AI Advice",
                            tint = Color.White,
                            modifier = Modifier.size(AppIconSize.sm)
                        )
                    }
                    Text(
                        text = "Mama Olufunke: Rice prices dropped by 4% at Mushin Market. Lock in this discount with your active Rice Goal today!",
                        style = AppTypography.caption.copy(fontWeight = FontWeight.Medium),
                        color = Color(0xFF8B4D00),
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            Spacer(modifier = Modifier.height(AppSpacing.sm))

            // --- SAVINGS ANALYTICS TREND CARD ---
            AfriSavCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = AppSpacing.lg, vertical = AppSpacing.xs),
                containerColor = SurfaceBg,
                borderColor = BorderSlate100,
                shape = AppShapes.card,
                contentPadding = PaddingValues(AppSpacing.lg)
            ) {
                Text(
                    text = "My Savings Trend",
                    style = AppTypography.cardTitle,
                    color = TextDark
                )
                Text(
                    text = "Your weekly savings deposits and group goal contributions",
                    style = AppTypography.caption,
                    color = TextSlate500
                )

                Spacer(modifier = Modifier.height(AppSpacing.lg))

                // Custom Drawings Canvas Chart with clean Material styles
                Canvas(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(110.dp)
                ) {
                    val width = size.width
                    val height = size.height

                    // Draw background reference grid
                    drawLine(
                        color = Color.LightGray.copy(alpha = 0.25f),
                        start = androidx.compose.ui.geometry.Offset(0f, height * 0.5f),
                        end = androidx.compose.ui.geometry.Offset(width, height * 0.5f),
                        strokeWidth = 2f
                    )

                    val points = listOf(
                        0.1f to 0.85f, 
                        0.25f to 0.70f, 
                        0.45f to 0.75f, 
                        0.65f to 0.45f, 
                        0.8f to 0.35f, 
                        1.0f to 0.15f
                    )
                    val path = Path().apply {
                        moveTo(0f, height * 0.9f)
                        points.forEach { (xPercent, yPercent) ->
                            lineTo(width * xPercent, height * yPercent)
                        }
                    }

                    // Draw beautiful savings gradient path fill
                    val fillPath = Path().apply {
                        addPath(path)
                        lineTo(width, height)
                        lineTo(0f, height)
                        close()
                    }
                    drawPath(
                        path = fillPath,
                        brush = Brush.verticalGradient(
                            colors = listOf(PrimaryGreen.copy(alpha = 0.25f), Color.Transparent)
                        )
                    )

                    // Draw spline line
                    drawPath(
                        path = path,
                        color = PrimaryGreen,
                        style = Stroke(width = 8f, cap = StrokeCap.Round)
                    )

                    // Draw dots at junctions
                    points.forEach { (xPercent, yPercent) ->
                        drawCircle(
                            color = SecondaryOrange,
                            radius = 7f,
                            center = androidx.compose.ui.geometry.Offset(width * xPercent, height * yPercent)
                        )
                        drawCircle(
                            color = Color.White,
                            radius = 3f,
                            center = androidx.compose.ui.geometry.Offset(width * xPercent, height * yPercent)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(AppSpacing.sm))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Mon", style = AppTypography.caption, color = TextSlate400)
                    Text("Tue", style = AppTypography.caption, color = TextSlate400)
                    Text("Wed", style = AppTypography.caption, color = TextSlate400)
                    Text("Thu", style = AppTypography.caption, color = TextSlate400)
                    Text("Fri", style = AppTypography.caption, color = TextSlate400)
                    Text("Sat", style = AppTypography.caption, color = TextSlate400)
                }
            }

            Spacer(modifier = Modifier.height(AppSpacing.sm))

            // --- NEARBY FOOD SELLERS SECTION ---
            Column(modifier = Modifier.padding(horizontal = AppSpacing.lg, vertical = AppSpacing.xs)) {
                AfriSavSectionHeader(
                    title = "Nearby Food Sellers",
                    actionText = "View Marketplace",
                    onActionClick = onViewVendorsClick
                )

                val sortedNearbySellers = remember(marketItems, currentUserLocation) {
                    marketItems.sortedWith(
                        compareBy<MarketItem> { item ->
                            val userLocLower = currentUserLocation.lowercase()
                            val itemStateLower = item.state.lowercase()
                            if (userLocLower.contains(itemStateLower) || itemStateLower.contains(userLocLower)) 0 else 1
                        }.thenBy { it.distance }
                    )
                }

                sortedNearbySellers.take(3).forEachIndexed { index, item ->
                    VendorPreviewRow(
                        item = item, 
                        onClick = onViewVendorsClick,
                        isHighlight = (index == 0)
                    )
                    Spacer(modifier = Modifier.height(AppSpacing.sm))
                }
            }

            Spacer(modifier = Modifier.height(AppSpacing.sm))

            // --- RECENT TRANSACTIONS COMPACT PREVIEW SECTION ---
            Column(modifier = Modifier.padding(horizontal = AppSpacing.lg, vertical = AppSpacing.xs)) {
                AfriSavSectionHeader(
                    title = "Recent Transactions",
                    actionText = "See All",
                    onActionClick = { viewModel.setActiveTab("wallet") }
                )
                Text(
                    text = "Track your recent savings deposits, wallet loads, and purchases.",
                    style = AppTypography.caption,
                    color = TextSlate500,
                    modifier = Modifier.padding(bottom = AppSpacing.sm)
                )

                val txs by viewModel.transactions.collectAsStateWithLifecycle()

                if (txs.isEmpty()) {
                    EmptyStateCard(
                        message = "No Transaction History Yet",
                        subMessage = "Your savings deposits, wallet transfers, and checkout transactions will show up here.",
                        btnText = "Fund Wallet",
                        onClick = onFundWalletClick,
                        icon = Icons.Default.AccountBalanceWallet,
                        iconColor = Color(0xFF0F766E)
                    )
                } else {
                    AfriSavCard(
                        containerColor = SurfaceBg,
                        borderColor = BorderSlate100,
                        shape = AppShapes.card,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("dashboard_transaction_history"),
                        contentPadding = PaddingValues(AppSpacing.md)
                    ) {
                        Column(
                            verticalArrangement = Arrangement.spacedBy(AppSpacing.sm)
                        ) {
                            txs.take(3).forEach { tx ->
                                DashboardTransactionRow(tx = tx, onReceiptClick = onReceiptClick)
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(AppSpacing.xxl))
        }
    }

    if (showNotificationCenter) {
        NotificationCenterDialog(
            viewModel = viewModel,
            onDismiss = { showNotificationCenter = false }
        )
    }
}

@Composable
fun QuickActionItem(
    title: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    tint: Color,
    onClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .clickable(onClick = onClick)
            .padding(4.dp)
    ) {
        Box(
            modifier = Modifier
                .size(56.dp)
                .background(Color.White, RoundedCornerShape(16.dp))
                .border(1.dp, BorderSlate100, RoundedCornerShape(16.dp)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = title,
                tint = tint,
                modifier = Modifier.size(24.dp)
            )
        }
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = title,
            fontSize = 11.sp,
            fontWeight = FontWeight.SemiBold,
            color = TextSlate500
        )
    }
}

@Composable
fun FoodCategoryItem(
    name: String,
    emoji: String,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .width(88.dp)
            .clip(RoundedCornerShape(20.dp))
            .clickable(onClick = onClick)
            .testTag("category_quick_card_${name.lowercase()}"),
        colors = CardDefaults.cardColors(containerColor = SurfaceBg),
        border = BorderStroke(1.dp, BorderSlate100),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp, horizontal = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .background(PrimaryGreen.copy(alpha = 0.08f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text(text = emoji, fontSize = 20.sp)
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = name,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = TextSlate800,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                maxLines = 1
            )
        }
    }
}

@Composable
fun OverlappingMembersAvatarPile(
    members: List<String>,
    modifier: Modifier = Modifier,
    avatarSize: Dp = 26.dp,
    maxVisible: Int = 4
) {
    val cleanMembers = members.filter { it.isNotBlank() }
    if (cleanMembers.isEmpty()) return

    val displayList = cleanMembers.take(maxVisible)
    val remainingCount = cleanMembers.size - displayList.size

    val avatarColors = listOf(
        Color(0xFFE11D48), // Rose
        Color(0xFF2563EB), // Blue
        Color(0xFF059669), // Emerald Green
        Color(0xFFD97706), // Amber
        Color(0xFF7C3AED), // Purple
        Color(0xFFDB2777), // Pink
        Color(0xFF0891B2), // Cyan
        Color(0xFFEA580C)  // Orange
    )

    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(contentAlignment = Alignment.CenterStart) {
            displayList.forEachIndexed { index, name ->
                val initials = name.split(" ")
                    .filter { it.isNotBlank() }
                    .take(2)
                    .map { it.first().uppercaseChar() }
                    .joinToString("")
                    .ifBlank { name.take(1).uppercase() }

                val colorIndex = Math.abs(name.hashCode()) % avatarColors.size
                val bgColor = avatarColors[colorIndex]
                
                Box(
                    modifier = Modifier
                        .padding(start = (index * 16).dp)
                        .size(avatarSize)
                        .clip(CircleShape)
                        .background(bgColor)
                        .border(1.5.dp, Color.White, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = initials,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color.White
                    )
                }
            }
        }
        
        if (remainingCount > 0) {
            Spacer(modifier = Modifier.width(6.dp))
            Box(
                modifier = Modifier
                    .size(avatarSize)
                    .clip(CircleShape)
                    .background(Color(0xFFE2E8F0))
                    .border(1.5.dp, Color.White, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "+$remainingCount",
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF475569)
                )
            }
        }
    }
}

@Composable
fun BuyingCircleCard(
    circle: FoodCircle,
    currentUserName: String,
    onJoinClick: () -> Unit,
    onContributeClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val progress = (circle.currentAmount / circle.targetAmount).toFloat()
    
    val isCreator = circle.creatorName.equals(currentUserName, ignoreCase = true) || circle.creatorName == "You"
    val membersList = circle.members.split(",").map { it.trim() }
    val isMember = membersList.any { it.equals(currentUserName, ignoreCase = true) || it == "You" }
    val coAdminsList = circle.coAdmins.split(",").map { it.trim() }
    val isCoAdmin = coAdminsList.any { it.equals(currentUserName, ignoreCase = true) || it == "You" }
    val inCircle = isCreator || isMember || isCoAdmin

    val pendingList = circle.pendingRequests.split(",").map { it.trim() }
    val isPending = pendingList.any { it.equals(currentUserName, ignoreCase = true) || it == "You" }

    // Custom asset emoji/icon based on title
    val emoji = when {
        circle.title.contains("rice", ignoreCase = true) -> "🌾"
        circle.title.contains("oil", ignoreCase = true) -> "🥥"
        circle.title.contains("veggie", ignoreCase = true) -> "🍅"
        circle.title.contains("poultry", ignoreCase = true) -> "🍗"
        else -> "🛍️"
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("buying_circle_card_${circle.id}"),
        colors = CardDefaults.cardColors(containerColor = SurfaceBg),
        shape = RoundedCornerShape(20.dp),
        border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Header: Emoji + Title & Creator
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .background(PrimaryGreen.copy(alpha = 0.08f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text(text = emoji, fontSize = 22.sp)
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = circle.title,
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        color = TextSlate800
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        OverlappingMembersAvatarPile(
                            members = membersList,
                            avatarSize = 18.dp,
                            maxVisible = 3
                        )
                        Text(
                            text = "By ${circle.creatorName.ifBlank { "System" }} • ${circle.membersCount} active savers",
                            fontSize = 11.sp,
                            color = TextSlate500
                        )
                    }
                }
                
                // Active/Unlocked Badge
                if (circle.isWholesaleUnlocked) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color(0xFFDCFCE7))
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = "UNLOCKED 🎉",
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF15803D)
                        )
                    }
                } else {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color(0xFFFEF3C7))
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = "SAVING",
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFB45309)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))
            // Description
            Text(
                text = circle.description,
                fontSize = 12.sp,
                color = TextSlate500,
                lineHeight = 16.sp
            )

            Spacer(modifier = Modifier.height(14.dp))
            // Funding Info: Current / Target
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Pooled Amount",
                        fontSize = 10.sp,
                        color = TextSlate400,
                        fontWeight = FontWeight.Medium
                    )
                    Text(
                        text = "₦${String.format("%,.0f", circle.currentAmount)}",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = PrimaryGreen
                    )
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "Target Goal",
                        fontSize = 10.sp,
                        color = TextSlate400,
                        fontWeight = FontWeight.Medium
                    )
                    Text(
                        text = "₦${String.format("%,.0f", circle.targetAmount)}",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = SecondaryOrange
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))
            // Progress Bar
            LinearProgressIndicator(
                progress = if (progress > 1f) 1f else progress,
                color = PrimaryGreen,
                trackColor = Color(0xFFF1F5F9),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
                    .clip(RoundedCornerShape(4.dp))
            )

            Spacer(modifier = Modifier.height(14.dp))
            // Action Button: Join / Joined / Contribute / Requested
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Info Badge
                Row(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(10.dp))
                        .background(BgSlate50)
                        .border(1.dp, Color(0xFFE2E8F0), RoundedCornerShape(10.dp))
                        .padding(horizontal = 8.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.QrCode,
                        contentDescription = "Invite Code",
                        tint = TextSlate500,
                        modifier = Modifier.size(12.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "Code: ${circle.circleCode}",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextSlate500
                    )
                }

                // Primary Action Button
                if (inCircle) {
                    Button(
                        onClick = onContributeClick,
                        colors = ButtonDefaults.buttonColors(containerColor = PrimaryGreen),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .weight(1.2f)
                            .height(38.dp)
                            .testTag("buying_circle_contribute_btn_${circle.id}"),
                        contentPadding = PaddingValues(0.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Savings,
                            contentDescription = "Contribute",
                            tint = Color.White,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Contribute", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                } else if (isPending) {
                    Button(
                        onClick = {},
                        enabled = false,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFFE2E8F0),
                            disabledContainerColor = Color(0xFFE2E8F0)
                        ),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .weight(1.2f)
                            .height(38.dp)
                            .testTag("buying_circle_pending_btn_${circle.id}"),
                        contentPadding = PaddingValues(0.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.HourglassEmpty,
                            contentDescription = "Pending",
                            tint = TextSlate400,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Requested ⏳", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = TextSlate500)
                    }
                } else {
                    Button(
                        onClick = onJoinClick,
                        colors = ButtonDefaults.buttonColors(containerColor = SecondaryOrange),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .weight(1.2f)
                            .height(38.dp)
                            .testTag("buying_circle_join_btn_${circle.id}"),
                        contentPadding = PaddingValues(0.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.GroupAdd,
                            contentDescription = "Join Pool",
                            tint = Color.White,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Join Pool 👥", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
fun MamaChatWidget(
    viewModel: KoboViewModel,
    modifier: Modifier = Modifier
) {
    val msgs by viewModel.chatMessages.collectAsStateWithLifecycle()
    val isLoading by viewModel.isChatLoading.collectAsStateWithLifecycle()
    var isOpen by remember { mutableStateOf(false) }
    var textInput by remember { mutableStateOf("") }
    val lazyListState = rememberLazyListState()

    // Auto-scroll chat to latest message when it changes or when opening
    LaunchedEffect(key1 = msgs.size, key2 = isLoading, key3 = isOpen) {
        if (isOpen && msgs.isNotEmpty()) {
            lazyListState.animateScrollToItem(msgs.size - 1)
        }
    }

    Box(
        modifier = modifier
            .padding(16.dp)
            .testTag("mama_chat_widget_container")
    ) {
        if (isOpen) {
            // Expanded Chat Window overlay
            Card(
                modifier = Modifier
                    .padding(bottom = 60.dp) // Offset so it floats above the FAB button
                    .widthIn(max = 320.dp)
                    .fillMaxWidth(0.9f)
                    .height(420.dp)
                    .testTag("mama_chat_widget_expanded"),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = BgSlate50), // soft bg
                elevation = CardDefaults.cardElevation(defaultElevation = 12.dp),
                border = BorderStroke(1.dp, Color(0xFFE2E8F0))
            ) {
                Column(modifier = Modifier.fillMaxSize()) {
                    // Header Bar
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(PrimaryGreen)
                            .padding(horizontal = 14.dp, vertical = 10.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(32.dp)
                                        .background(Color.White.copy(alpha = 0.2f), CircleShape),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text("👵", fontSize = 18.sp)
                                }
                                Spacer(modifier = Modifier.width(10.dp))
                                Column {
                                    Text(
                                        text = "Mama Olufunke (AI)",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 13.sp,
                                        color = Color.White
                                    )
                                    Text(
                                        text = "Online • Wise Savings Helper",
                                        fontSize = 10.sp,
                                        color = Color.White.copy(alpha = 0.85f)
                                    )
                                }
                            }
                            IconButton(
                                onClick = { isOpen = false },
                                modifier = Modifier.size(28.dp).testTag("mama_chat_widget_close_button")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = "Close chat widget",
                                    tint = Color.White,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                    }

                    // Chat History Area
                    LazyColumn(
                        state = lazyListState,
                        modifier = Modifier
                            .weight(1f)
                            .padding(horizontal = 12.dp, vertical = 8.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        if (msgs.isEmpty()) {
                            item {
                                Box(
                                    modifier = Modifier
                                        .fillParentMaxSize()
                                        .padding(16.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Column(
                                        horizontalAlignment = Alignment.CenterHorizontally,
                                        verticalArrangement = Arrangement.Center
                                    ) {
                                        Text("👵", fontSize = 40.sp)
                                        Spacer(modifier = Modifier.height(10.dp))
                                        Text(
                                            text = "Welcome to Mama Basket Savings Advisor!",
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = TextSlate800,
                                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                                        )
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text(
                                            text = "Ask me about food prices or budgeting tips below.",
                                            fontSize = 11.sp,
                                            color = TextSlate500,
                                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                                        )
                                    }
                                }
                            }
                        } else {
                            items(msgs) { msg ->
                                ChatBubble(msg = msg)
                            }
                        }
                        if (isLoading) {
                            item {
                                MamaLoadingBubble()
                            }
                        }
                    }

                    // Quick-Access Suggestion Chips Row
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState())
                            .padding(horizontal = 12.dp, vertical = 4.dp),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        val tipsAndPrices = listOf(
                            "Savings tips? 💡" to "Give me some quick food savings tips!",
                            "Rice price? 🌾" to "What is the average price of Rice?",
                            "Beans price? 🫘" to "What is the price of Beans?",
                            "Market deals? 🍅" to "Are there any cheap tomatoes or food deals?",
                            "What's a Circle? 🤝" to "What is a Food Circle group buy?"
                        )
                        tipsAndPrices.forEach { (label, prompt) ->
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(SurfaceBg)
                                    .border(1.dp, Color(0xFFE2E8F0), RoundedCornerShape(12.dp))
                                    .clickable { viewModel.sendMessageToMama(prompt) }
                                    .padding(horizontal = 10.dp, vertical = 5.dp)
                                    .testTag("mama_quick_chip_${label.replace("?", "").replace(" ", "_").lowercase()}")
                            ) {
                                Text(
                                    text = label,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = PrimaryGreen
                                )
                            }
                        }
                    }

                    // Divider
                    HorizontalDivider(color = Color(0xFFE2E8F0), thickness = 1.dp)

                    // Input Row
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(SurfaceBg)
                            .padding(horizontal = 10.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedTextField(
                            value = textInput,
                            onValueChange = { textInput = it },
                            placeholder = { Text("Ask Mama...", fontSize = 12.sp) },
                            singleLine = true,
                            modifier = Modifier
                                .weight(1f)
                                .height(44.dp)
                                .testTag("mama_chat_widget_input"),
                            shape = RoundedCornerShape(16.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = PrimaryGreen,
                                unfocusedBorderColor = Color(0xFFE2E8F0)
                            )
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        IconButton(
                            onClick = {
                                if (textInput.trim().isNotEmpty()) {
                                    viewModel.sendMessageToMama(textInput)
                                    textInput = ""
                                }
                            },
                            modifier = Modifier
                                .size(36.dp)
                                .background(PrimaryGreen, CircleShape)
                                .testTag("mama_chat_widget_send_button")
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.Send,
                                contentDescription = "Send",
                                tint = Color.White,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }
            }
        }

        // Toggle FAB Button at the very bottom right
        Card(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .clickable { isOpen = !isOpen }
                .testTag("mama_chat_widget_toggle"),
            shape = RoundedCornerShape(28.dp),
            colors = CardDefaults.cardColors(containerColor = if (isOpen) Color.White else PrimaryGreen),
            elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
            border = if (isOpen) BorderStroke(1.dp, Color(0xFFE2E8F0)) else null
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Box {
                    Text("👵", fontSize = 20.sp)
                    // Tiny green dot for online status
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .background(Color(0xFF4ADE80), CircleShape)
                            .align(Alignment.BottomEnd)
                            .border(1.dp, Color.White, CircleShape)
                    )
                }
                if (!isOpen) {
                    Text(
                        text = "Ask Mama AI",
                        color = Color.White,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                } else {
                    Text(
                        text = "Close",
                        color = TextSlate800,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

@Composable
fun GoalSummaryCard(goal: SavingsGoal, onSaveClick: () -> Unit) {
    AfriSavCard(
        modifier = Modifier.fillMaxWidth(),
        shape = AppShapes.card,
        contentPadding = PaddingValues(AppSpacing.lg)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(AppShapes.md)
                        .background(AppColors.brandOrangeBgLight),
                    contentAlignment = Alignment.Center
                ) {
                    Text(getCategoryEmoji(goal.category), fontSize = 22.sp)
                }
                Spacer(modifier = Modifier.width(AppSpacing.md))
                Column {
                    Text(
                        text = goal.title,
                        style = AppTypography.cardTitle,
                        color = TextDark
                    )
                    Text(
                        text = "Target: ₦${String.format("%,.0f", goal.targetAmount)}",
                        style = AppTypography.caption,
                        color = TextSlate500
                    )
                }
            }
            if (goal.isLocked) {
                AfriSavBadge(
                    text = "Locked",
                    type = AfriSavBadgeType.ERROR,
                    icon = Icons.Default.Lock
                )
            }
        }

        Spacer(modifier = Modifier.height(AppSpacing.md))

        BasketSavingsProgressBar(
            savedAmount = goal.savedAmount,
            targetAmount = goal.targetAmount,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(AppSpacing.md))

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(AppShapes.md)
                .background(SurfaceSubtle)
                .border(1.dp, BorderSlate100, AppShapes.md)
                .padding(AppSpacing.md),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "SAVED SO FAR",
                    style = AppTypography.labelSmall,
                    color = TextSlate400
                )
                Text(
                    text = "₦${String.format("%,.0f", goal.savedAmount)}",
                    style = AppTypography.sectionHeader.copy(fontWeight = FontWeight.Bold),
                    color = TextDark
                )
            }
            AfriSavPrimaryButton(
                text = "Top Up",
                onClick = onSaveClick,
                fullWidth = false,
                modifier = Modifier.height(38.dp)
            )
        }
    }
}

@Composable
fun VendorPreviewRow(
    item: MarketItem, 
    onClick: () -> Unit,
    isHighlight: Boolean = false
) {
    AfriSavCard(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .testTag("vendor_row_${item.id}"),
        containerColor = if (isHighlight) AppColors.brandGreenBgLight else SurfaceBg,
        borderColor = if (isHighlight) PrimaryGreen.copy(alpha = 0.4f) else BorderSlate100,
        shape = AppShapes.card,
        contentPadding = PaddingValues(0.dp)
    ) {
        Column {
            if (isHighlight) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(PrimaryGreen)
                        .padding(horizontal = AppSpacing.md, vertical = AppSpacing.xs),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Explore,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(AppIconSize.xs)
                    )
                    Spacer(modifier = Modifier.width(AppSpacing.xs))
                    Text(
                        text = "CLOSEST TO YOUR LOCATION",
                        color = Color.White,
                        style = AppTypography.labelSmall,
                        maxLines = 1,
                        softWrap = false
                    )
                }
            }

            Row(
                modifier = Modifier.padding(AppSpacing.md),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape)
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
                Spacer(modifier = Modifier.width(AppSpacing.md))
                Column(modifier = Modifier.weight(1f)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = item.vendorName,
                            style = AppTypography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                            color = TextDark,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.weight(1f, fill = false)
                        )
                        if (isHighlight) {
                            Spacer(modifier = Modifier.width(AppSpacing.xs))
                            AfriSavBadge(
                                text = "Nearest",
                                type = AfriSavBadgeType.SUCCESS
                            )
                        }
                    }
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Star, contentDescription = null, tint = AccentGold, modifier = Modifier.size(14.dp))
                        Text(
                            text = " ${item.rating} • ${item.distance} km away",
                            style = AppTypography.caption,
                            color = TextSlate500
                        )
                    }
                }
                Spacer(modifier = Modifier.width(AppSpacing.sm))
                Column(horizontalAlignment = Alignment.End) {
                    PriceDisplay(
                        price = item.price,
                        originalPrice = item.originalPrice,
                        priceFontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = item.name.split(" ").take(2).joinToString(" "),
                        style = AppTypography.caption,
                        color = TextSlate500
                    )
                }
            }
        }
    }
}

// --- WALLET SCREEN ---
@Composable
fun WalletScreen(
    viewModel: KoboViewModel,
    onFundClick: () -> Unit,
    onTransferClick: () -> Unit,
    onReceiptClick: (WalletTransaction) -> Unit
) {
    val wallet by viewModel.walletState.collectAsStateWithLifecycle()
    val txs by viewModel.transactions.collectAsStateWithLifecycle()
    val escrowOrders by viewModel.escrowOrdersForBuyer.collectAsStateWithLifecycle()
    
    val activeEscrows = escrowOrders.filter { it.status == "PENDING" || it.status == "DISPUTED" }
    var selectedEscrowForRating by remember { mutableStateOf<EscrowOrder?>(null) }
    var selectedEscrowForDispute by remember { mutableStateOf<EscrowOrder?>(null) }
    var disputeReason by remember { mutableStateOf("") }
    var showSetKoboGoalDialog by remember { mutableStateOf(false) }
    var showKoboContributeDialog by remember { mutableStateOf(false) }
    var isBalanceVisible by remember { mutableStateOf(true) }
    var showAllTransactions by remember { mutableStateOf(false) }

    var isLoading by remember { mutableStateOf(true) }
    LaunchedEffect(Unit) {
        delay(800)
        isLoading = false
    }

    if (selectedEscrowForRating != null) {
        EscrowCompletionDialog(
            escrow = selectedEscrowForRating!!,
            onDismiss = { selectedEscrowForRating = null },
            onConfirm = { vRating, vReview, bRating, bReview, rRating, rReview ->
                viewModel.confirmEscrowReceipt(
                    orderId = selectedEscrowForRating!!.id,
                    vendorRating = vRating,
                    vendorReview = vReview,
                    buyerRating = bRating,
                    buyerReview = bReview,
                    riderRating = rRating,
                    riderReview = rReview
                )
                selectedEscrowForRating = null
            }
        )
    }

    if (selectedEscrowForDispute != null) {
        AlertDialog(
            onDismissRequest = { selectedEscrowForDispute = null },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Warning, contentDescription = null, tint = Color.Red, modifier = Modifier.size(24.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("File Escrow Contract Dispute", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                }
            },
            text = {
                Column {
                    Text(
                        "Filing a formal dispute immediately freezes the contract's escrow funds (₦${String.format("%,.0f", selectedEscrowForDispute!!.amount)}) in safe holding. Please state your reason below.",
                        fontSize = 12.sp,
                        color = Color(0xFF475569)
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    OutlinedTextField(
                        value = disputeReason,
                        onValueChange = { disputeReason = it },
                        placeholder = { Text("e.g. Goods not delivered, item count incomplete...") },
                        modifier = Modifier.fillMaxWidth(),
                        textStyle = TextStyle(fontSize = 13.sp)
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (disputeReason.isNotBlank()) {
                            viewModel.fileEscrowDispute(selectedEscrowForDispute!!.id, disputeReason)
                            selectedEscrowForDispute = null
                            disputeReason = ""
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Red)
                ) {
                    Text("File Dispute")
                }
            },
            dismissButton = {
                TextButton(onClick = { selectedEscrowForDispute = null }) {
                    Text("Cancel")
                }
            }
        )
    }

    if (showSetKoboGoalDialog) {
        SetKoboGoalDialog(
            currentGoal = wallet?.koboContributionGoal ?: 50000.0,
            currentFrequency = wallet?.koboGoalFrequency ?: "Daily",
            onDismiss = { showSetKoboGoalDialog = false },
            onConfirm = { goal, freq ->
                viewModel.updateKoboContributionGoal(goal, freq)
                showSetKoboGoalDialog = false
            }
        )
    }

    if (showKoboContributeDialog) {
        KoboContributeDialog(
            availableBalanceInNaira = wallet?.availableBalance ?: 0.0,
            onDismiss = { showKoboContributeDialog = false },
            onConfirm = { amount ->
                viewModel.contributeKobo(amount)
                showKoboContributeDialog = false
            }
        )
    }

    if (isLoading) {
        WalletSkeleton()
    } else {
        val context = LocalContext.current
        val clipboardManager = androidx.compose.ui.platform.LocalClipboardManager.current
        
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(SoftBackground)
                .verticalScroll(rememberScrollState())
                .statusBarsPadding()
                .padding(horizontal = AppSpacing.lg, vertical = AppSpacing.lg)
        ) {
            // Screen Title & Subtitle using standardized Page Header
            AfriSavPageHeader(
                title = "Digital Food Wallet",
                subtitle = "Manage your food capital, emergency savings, and automated funding"
            )

            Spacer(modifier = Modifier.height(AppSpacing.lg))

            // Unified Total Balance Summary Card at the Top
            val totalBalance = (wallet?.availableBalance ?: 0.0) + (wallet?.savingsBalance ?: 0.0)
            AfriSavCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("wallet_balance_card"),
                containerColor = SurfaceBg,
                borderColor = BorderSlate100,
                shape = AppShapes.card,
                contentPadding = PaddingValues(0.dp)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            Brush.linearGradient(
                                colors = listOf(PrimaryGreen, DarkGreen)
                            )
                        )
                        .padding(AppSpacing.lg)
                ) {
                    Column {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = "COMBINED CAPITAL SUMMARY",
                                    style = AppTypography.caption.copy(fontWeight = FontWeight.Bold),
                                    color = Color.White.copy(alpha = 0.75f),
                                    letterSpacing = 1.2.sp
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = "AfriSav Smart Wallet 🛡️",
                                    style = AppTypography.cardTitle,
                                    color = Color.White
                                )
                            }
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                IconButton(
                                    onClick = { isBalanceVisible = !isBalanceVisible },
                                    modifier = Modifier.size(36.dp)
                                ) {
                                    Icon(
                                        imageVector = if (isBalanceVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                        contentDescription = "Toggle Balance Visibility",
                                        tint = Color.White.copy(alpha = 0.85f),
                                        modifier = Modifier.size(AppIconSize.md)
                                    )
                                }
                                Spacer(modifier = Modifier.width(AppSpacing.xs))
                                Icon(
                                    imageVector = Icons.Default.AccountBalanceWallet,
                                    contentDescription = null,
                                    tint = AccentGold,
                                    modifier = Modifier.size(26.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(AppSpacing.md))

                        Text(
                            text = "Total Combined Balance (Spendable + Saved)",
                            style = AppTypography.bodySmall,
                            color = Color.White.copy(alpha = 0.85f)
                        )
                        Spacer(modifier = Modifier.height(AppSpacing.xs))
                        Text(
                            text = if (isBalanceVisible) {
                                "₦${String.format("%,.2f", totalBalance)}"
                            } else {
                                "₦ ••••.••"
                            },
                            style = AppTypography.balanceLarge,
                            color = Color.White,
                            letterSpacing = if (isBalanceVisible) (-0.5).sp else 1.5.sp
                        )

                        Spacer(modifier = Modifier.height(AppSpacing.md))
                        HorizontalDivider(color = Color.White.copy(alpha = 0.2f))
                        Spacer(modifier = Modifier.height(AppSpacing.sm))

                        // Account number with copy functionality (Clean, highly legible card overlay)
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(AppShapes.md)
                                .background(Color.White.copy(alpha = 0.12f))
                                .padding(horizontal = AppSpacing.md, vertical = AppSpacing.sm)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text(
                                        text = "DEDICATED ACCOUNT (OPAY PARTNER)",
                                        style = AppTypography.caption.copy(fontWeight = FontWeight.Bold),
                                        color = Color.White.copy(alpha = 0.8f)
                                    )
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(
                                            text = "1215543001",
                                            style = AppTypography.sectionHeader.copy(fontWeight = FontWeight.Bold),
                                            color = Color.White,
                                            letterSpacing = 0.5.sp
                                        )
                                        Spacer(modifier = Modifier.width(AppSpacing.sm))
                                        Icon(
                                            imageVector = Icons.Default.ContentCopy,
                                            contentDescription = "Copy Account Number",
                                            tint = Color.White,
                                            modifier = Modifier
                                                .size(AppIconSize.sm)
                                                .clickable {
                                                    clipboardManager.setText(androidx.compose.ui.text.AnnotatedString("1215543001"))
                                                    android.widget.Toast.makeText(context, "Account number copied! 📋", android.widget.Toast.LENGTH_SHORT).show()
                                                }
                                        )
                                    }
                                }
                                Box(
                                    modifier = Modifier
                                        .clip(AppShapes.sm)
                                        .background(Color.White.copy(alpha = 0.2f))
                                        .padding(horizontal = AppSpacing.sm, vertical = AppSpacing.xs)
                                ) {
                                    Text(
                                        text = "Tier 1 Limit",
                                        style = AppTypography.labelSmall,
                                        color = AccentGold
                                    )
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(AppSpacing.md))

            // Side-by-Side Cards for Spendable vs Savings Balance
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(AppSpacing.md)
            ) {
                // Spendable Card
                AfriSavCard(
                    modifier = Modifier.weight(1f),
                    containerColor = SurfaceBg,
                    borderColor = BorderSlate100,
                    shape = AppShapes.card,
                    contentPadding = PaddingValues(AppSpacing.md)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .background(PrimaryGreen.copy(alpha = 0.1f), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.AccountBalanceWallet,
                                contentDescription = null,
                                tint = PrimaryGreen,
                                modifier = Modifier.size(AppIconSize.sm)
                            )
                        }
                        Spacer(modifier = Modifier.width(AppSpacing.sm))
                        Text(
                            text = "Spendable 💳",
                            style = AppTypography.label.copy(fontWeight = FontWeight.Bold),
                            color = TextDark
                        )
                    }
                    Spacer(modifier = Modifier.height(AppSpacing.sm))
                    Text(
                        text = if (isBalanceVisible) {
                            "₦${String.format("%,.2f", wallet?.availableBalance ?: 0.0)}"
                        } else {
                            "₦ ••••"
                        },
                        style = AppTypography.cardTitle.copy(fontWeight = FontWeight.Bold),
                        color = PrimaryGreen
                    )
                    Spacer(modifier = Modifier.height(AppSpacing.xs))
                    Text(
                        text = "Instant shopping buffer",
                        style = AppTypography.caption,
                        color = TextSlate500
                    )
                }

                // Locked Savings Card
                AfriSavCard(
                    modifier = Modifier.weight(1f),
                    containerColor = SurfaceBg,
                    borderColor = BorderSlate100,
                    shape = AppShapes.card,
                    contentPadding = PaddingValues(AppSpacing.md)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .background(SecondaryOrange.copy(alpha = 0.12f), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Savings,
                                contentDescription = null,
                                tint = SecondaryOrange,
                                modifier = Modifier.size(AppIconSize.sm)
                            )
                        }
                        Spacer(modifier = Modifier.width(AppSpacing.sm))
                        Text(
                            text = "Savings 🔒",
                            style = AppTypography.label.copy(fontWeight = FontWeight.Bold),
                            color = TextDark
                        )
                    }
                    Spacer(modifier = Modifier.height(AppSpacing.sm))
                    Text(
                        text = if (isBalanceVisible) {
                            "₦${String.format("%,.2f", wallet?.savingsBalance ?: 0.0)}"
                        } else {
                            "₦ ••••"
                        },
                        style = AppTypography.cardTitle.copy(fontWeight = FontWeight.Bold),
                        color = SecondaryOrange
                    )
                    Spacer(modifier = Modifier.height(AppSpacing.xs))
                    Text(
                        text = "Secured target funds",
                        style = AppTypography.caption,
                        color = TextSlate500
                    )
                }
            }

            Spacer(modifier = Modifier.height(AppSpacing.md))

            // Quick Actions: Fund Wallet & Transfer (High Touch-Target Buttons)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(AppSpacing.md)
            ) {
                AfriSavPrimaryButton(
                    text = "Fund Wallet",
                    onClick = onFundClick,
                    leadingIcon = Icons.Default.Add,
                    modifier = Modifier
                        .weight(1f)
                        .testTag("wallet_fund_button")
                )
                AfriSavPrimaryButton(
                    text = "Transfer Out",
                    onClick = onTransferClick,
                    leadingIcon = Icons.Default.Send,
                    containerColor = SecondaryOrange,
                    modifier = Modifier
                        .weight(1f)
                        .testTag("wallet_transfer_button")
                )
            }

            Spacer(modifier = Modifier.height(AppSpacing.lg))

            // --- SMART WALLET KOBO CONTRIBUTION GOAL COMPONENT ---
            AfriSavCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("kobo_smart_wallet_card"),
                containerColor = SurfaceBg,
                borderColor = BorderSlate100,
                shape = AppShapes.card,
                contentPadding = PaddingValues(AppSpacing.lg)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(44.dp)
                                .background(PrimaryGreen.copy(alpha = 0.1f), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.TrendingUp,
                                contentDescription = null,
                                tint = PrimaryGreen,
                                modifier = Modifier.size(AppIconSize.lg)
                            )
                        }
                        Spacer(modifier = Modifier.width(AppSpacing.md))
                        Column {
                            Text(
                                text = "Smart Naira Saver 🎯",
                                style = AppTypography.cardTitle,
                                color = TextDark
                            )
                            Text(
                                text = "Automated piggybank & food goals",
                                style = AppTypography.caption,
                                color = TextSlate500
                            )
                        }
                    }
                    
                    // Edit Goal button
                    IconButton(
                        onClick = { showSetKoboGoalDialog = true },
                        modifier = Modifier
                            .background(BorderSlate100, CircleShape)
                            .size(36.dp)
                            .testTag("edit_kobo_goal_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = "Edit Naira Goal",
                            tint = TextDark,
                            modifier = Modifier.size(AppIconSize.sm)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(AppSpacing.md))

                // Savings Balance in Naira
                val savingsBalanceInNaira = wallet?.savingsBalance ?: 0.0
                Text(
                    text = "YOUR SAVINGS BALANCE",
                    style = AppTypography.caption.copy(fontWeight = FontWeight.Bold),
                    color = TextSlate400,
                    letterSpacing = 0.8.sp
                )
                Spacer(modifier = Modifier.height(AppSpacing.xs))
                Text(
                    text = "₦${String.format("%,.2f", savingsBalanceInNaira)}",
                    style = AppTypography.balanceLarge,
                    color = PrimaryGreen
                )

                Spacer(modifier = Modifier.height(AppSpacing.md))
                HorizontalDivider(color = BorderSlate100)
                Spacer(modifier = Modifier.height(AppSpacing.md))

                // Contribution Goal Details
                val goalAmount = wallet?.koboContributionGoal ?: 50000.0
                val currentContribution = wallet?.currentKoboContribution ?: 0.0
                val frequency = wallet?.koboGoalFrequency ?: "Daily"
                val progress = if (goalAmount > 0) (currentContribution / goalAmount).toFloat().coerceIn(0f, 1f) else 0f

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "$frequency Savings Target",
                        style = AppTypography.label.copy(fontWeight = FontWeight.Bold),
                        color = TextDark
                    )
                    Text(
                        text = "₦${String.format("%,.0f", currentContribution)} / ₦${String.format("%,.0f", goalAmount)}",
                        style = AppTypography.label.copy(fontWeight = FontWeight.Bold),
                        color = PrimaryGreen
                    )
                }

                Spacer(modifier = Modifier.height(AppSpacing.sm))

                // Customized Gradient Progress Bar
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(8.dp)
                        .clip(AppShapes.pill)
                        .background(BorderSlate100)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxHeight()
                            .fillMaxWidth(progress)
                            .clip(AppShapes.pill)
                            .background(
                                Brush.horizontalGradient(
                                    colors = listOf(PrimaryGreen, DarkGreen)
                                )
                            )
                    )
                }

                Spacer(modifier = Modifier.height(AppSpacing.sm))

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(AppSpacing.xs)
                ) {
                    Icon(
                        imageVector = if (progress >= 1f) Icons.Default.Stars else Icons.Default.OfflineBolt,
                        contentDescription = null,
                        tint = if (progress >= 1f) AccentGold else PrimaryGreen,
                        modifier = Modifier.size(AppIconSize.sm)
                    )
                    Text(
                        text = if (progress >= 1f) "Goal achieved! Pure star behavior! ⭐" else "${String.format("%.0f", progress * 100)}% of your $frequency goal completed",
                        style = AppTypography.caption.copy(fontWeight = FontWeight.Bold),
                        color = if (progress >= 1f) PrimaryGreen else TextSlate500
                    )
                }

                Spacer(modifier = Modifier.height(AppSpacing.md))

                // Action buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(AppSpacing.sm)
                ) {
                    AfriSavPrimaryButton(
                        text = "Save Naira",
                        onClick = { showKoboContributeDialog = true },
                        leadingIcon = Icons.Default.AddCircle,
                        modifier = Modifier
                            .weight(1f)
                            .testTag("kobo_contribute_button")
                    )

                    AfriSavOutlinedButton(
                        text = "Reset Goal",
                        onClick = { viewModel.resetKoboContribution() },
                        leadingIcon = Icons.Default.Refresh,
                        modifier = Modifier
                            .weight(1f)
                            .testTag("kobo_reset_button")
                    )
                }
            }

            Spacer(modifier = Modifier.height(AppSpacing.lg))

            // --- AUTOMATIC DEPOSITS CARD ---
            AfriSavCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("auto_monthly_deposit_card"),
                containerColor = SurfaceBg,
                borderColor = BorderSlate100,
                shape = AppShapes.card,
                contentPadding = PaddingValues(AppSpacing.lg)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.weight(1f)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(44.dp)
                                .background(Color(0xFFF3E8FF), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Autorenew,
                                contentDescription = null,
                                tint = Color(0xFF8B5CF6),
                                modifier = Modifier.size(AppIconSize.lg)
                            )
                        }
                        Spacer(modifier = Modifier.width(AppSpacing.md))
                        Column {
                            Text(
                                text = "Auto Savings 🔄",
                                style = AppTypography.cardTitle,
                                color = TextDark
                            )
                            Text(
                                text = "Automated food savings at selected frequency",
                                style = AppTypography.caption,
                                color = TextSlate500
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(AppSpacing.sm))

                    // Toggle Switch
                    val isAutoEnabled = wallet?.isAutoMonthlyDepositEnabled ?: false
                    val currentAutoAmount = wallet?.autoMonthlyDepositAmount ?: 5000.0
                    val currentFrequency = wallet?.autoDepositFrequency ?: "Monthly"
                    Switch(
                        checked = isAutoEnabled,
                        onCheckedChange = { enabled ->
                            viewModel.updateAutoDepositSettings(enabled, currentAutoAmount, currentFrequency)
                        },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Color.White,
                            checkedTrackColor = Color(0xFF8B5CF6)
                        ),
                        modifier = Modifier.testTag("auto_monthly_deposit_switch")
                    )
                }

                Spacer(modifier = Modifier.height(AppSpacing.md))
                HorizontalDivider(color = BorderSlate100)
                Spacer(modifier = Modifier.height(AppSpacing.md))

                val isAutoEnabled = wallet?.isAutoMonthlyDepositEnabled ?: false
                val currentAutoAmount = wallet?.autoMonthlyDepositAmount ?: 5000.0
                val currentFrequency = wallet?.autoDepositFrequency ?: "Monthly"

                if (isAutoEnabled) {
                    // Selectable Frequency Chips
                    Text(
                        text = "SAVINGS FREQUENCY",
                        style = AppTypography.caption.copy(fontWeight = FontWeight.Bold),
                        color = TextSlate400,
                        letterSpacing = 0.8.sp
                    )

                    Spacer(modifier = Modifier.height(AppSpacing.sm))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        listOf("Daily", "Weekly", "Bi-weekly", "Monthly").forEach { freq ->
                            val isSelected = currentFrequency == freq
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(AppShapes.sm)
                                    .background(
                                        color = if (isSelected) Color(0xFFF3E8FF) else BorderSlate100
                                    )
                                    .border(
                                        width = if (isSelected) 1.5.dp else 1.dp,
                                        color = if (isSelected) Color(0xFF8B5CF6) else Color.Transparent,
                                        shape = AppShapes.sm
                                    )
                                    .clickable {
                                        viewModel.updateAutoDepositSettings(true, currentAutoAmount, freq)
                                    }
                                    .padding(vertical = AppSpacing.sm),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = freq,
                                    style = AppTypography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                    color = if (isSelected) Color(0xFF8B5CF6) else TextSlate500
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(AppSpacing.md))

                    Text(
                        text = "${currentFrequency.uppercase()} TRANSFER AMOUNT",
                        style = AppTypography.caption.copy(fontWeight = FontWeight.Bold),
                        color = TextSlate400,
                        letterSpacing = 0.8.sp
                    )

                    Spacer(modifier = Modifier.height(AppSpacing.xs))

                    Text(
                        text = "₦${String.format("%,.0f", currentAutoAmount)}",
                        style = AppTypography.balanceLarge,
                        color = Color(0xFF8B5CF6)
                    )

                    Spacer(modifier = Modifier.height(AppSpacing.md))

                    // Quick select chips
                    Text(
                        text = "Quick Select Amount",
                        style = AppTypography.label.copy(fontWeight = FontWeight.Bold),
                        color = TextDark
                    )

                    Spacer(modifier = Modifier.height(AppSpacing.sm))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(AppSpacing.sm)
                    ) {
                        listOf(2000.0, 5000.0, 10000.0, 20000.0).forEach { amount ->
                            val isSelected = currentAutoAmount == amount
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .background(
                                        color = if (isSelected) Color(0xFFF3E8FF) else BorderSlate100,
                                        shape = AppShapes.sm
                                    )
                                    .border(
                                        width = if (isSelected) 1.5.dp else 1.dp,
                                        color = if (isSelected) Color(0xFF8B5CF6) else Color.Transparent,
                                        shape = AppShapes.sm
                                    )
                                    .clickable {
                                        viewModel.updateAutoDepositSettings(true, amount, currentFrequency)
                                    }
                                    .padding(vertical = AppSpacing.sm),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "₦${String.format("%,.0f", amount / 1000)}k",
                                    style = AppTypography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                    color = if (isSelected) Color(0xFF8B5CF6) else TextSlate500
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(AppSpacing.md))

                    // Custom deposit text-field input
                    var customAutoInputVal by remember { mutableStateOf("") }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(AppSpacing.sm)
                    ) {
                        AfriSavTextField(
                            value = customAutoInputVal,
                            onValueChange = { customAutoInputVal = it.filter { char -> char.isDigit() } },
                            label = "Custom Amount (₦)",
                            placeholder = "e.g. 15000",
                            modifier = Modifier
                                .weight(1.5f)
                                .testTag("custom_auto_deposit_input")
                        )

                        AfriSavPrimaryButton(
                            text = "Set Value",
                            onClick = {
                                val amount = customAutoInputVal.toDoubleOrNull()
                                if (amount != null && amount > 0) {
                                    viewModel.updateAutoDepositSettings(true, amount, currentFrequency)
                                    customAutoInputVal = ""
                                }
                            },
                            containerColor = Color(0xFF8B5CF6),
                            modifier = Modifier
                                .weight(1f)
                                .testTag("set_custom_auto_deposit_button")
                        )
                    }

                    Spacer(modifier = Modifier.height(AppSpacing.md))

                    // Simulation control inside the card
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color(0xFFF9F5FF), AppShapes.md)
                            .border(1.dp, Color(0xFFE9D5FF), AppShapes.md)
                            .padding(AppSpacing.md)
                    ) {
                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.Info,
                                    contentDescription = null,
                                    tint = Color(0xFF7C3AED),
                                    modifier = Modifier.size(AppIconSize.sm)
                                )
                                Spacer(modifier = Modifier.width(AppSpacing.sm))
                                Text(
                                    text = "Simulation Mode Available",
                                    style = AppTypography.label.copy(fontWeight = FontWeight.Bold),
                                    color = Color(0xFF5B21B6)
                                )
                            }
                            Spacer(modifier = Modifier.height(AppSpacing.xs))
                            Text(
                                text = "Click below to simulate an immediate ${currentFrequency.lowercase()} deposit check. This transfers your specified amount from Available Balance into Savings.",
                                style = AppTypography.caption,
                                color = Color(0xFF6B21A8),
                                lineHeight = 16.sp
                            )
                            Spacer(modifier = Modifier.height(AppSpacing.md))
                            AfriSavPrimaryButton(
                                text = "Simulate Auto-Deposit Now 🚀",
                                onClick = { viewModel.triggerSimulationAutoDeposit() },
                                containerColor = Color(0xFF7C3AED),
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }
                } else {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(AppColors.warningBgLight, AppShapes.md)
                            .border(1.dp, AppColors.warning.copy(alpha = 0.3f), AppShapes.md)
                            .padding(AppSpacing.md)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Info,
                                contentDescription = null,
                                tint = AppColors.warning,
                                modifier = Modifier.size(AppIconSize.md)
                            )
                            Spacer(modifier = Modifier.width(AppSpacing.sm))
                            Text(
                                text = "Auto Savings is currently disabled. Toggle to automatically lock away savings regularly towards your food secure target.",
                                style = AppTypography.caption,
                                color = Color(0xFF78350F),
                                lineHeight = 16.sp
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(AppSpacing.md))

                Text(
                    text = "💡 Pro Tip: Configuring auto-deposits helps you maintain a healthy buffer of food capital to buy bulk items and secure heavy group discounts effortlessly.",
                    style = AppTypography.caption,
                    color = TextSlate500,
                    lineHeight = 16.sp
                )
            }

            Spacer(modifier = Modifier.height(AppSpacing.lg))

            // Transaction History Section
            AfriSavSectionHeader(
                title = "Transaction History 💸",
                actionText = if (txs.size > 3) {
                    if (showAllTransactions) "Show Less" else "View All (${txs.size})"
                } else null,
                onActionClick = if (txs.size > 3) {
                    { showAllTransactions = !showAllTransactions }
                } else null
            )
            Spacer(modifier = Modifier.height(AppSpacing.sm))

            if (txs.isEmpty()) {
                EmptyStateCard(
                    message = "No Transaction History Found",
                    subMessage = "Your wallet deposits, bank transfers, and bulk buying savings will appear here.",
                    btnText = "Fund Wallet",
                    onClick = onFundClick,
                    icon = Icons.Default.AccountBalanceWallet,
                    iconColor = PrimaryGreen
                )
            } else {
                val previewTxs = if (showAllTransactions) txs else txs.take(3)
                previewTxs.forEach { tx ->
                    TransactionRow(tx = tx, onReceiptClick = onReceiptClick)
                    Spacer(modifier = Modifier.height(AppSpacing.sm))
                }
                
                if (txs.size > 3 && !showAllTransactions) {
                    AfriSavOutlinedButton(
                        text = "View Full Transaction History 🔽",
                        onClick = { showAllTransactions = true },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = AppSpacing.xs)
                    )
                }
            }
        }
    }
}

@Composable
fun TransactionRow(tx: WalletTransaction, onReceiptClick: (WalletTransaction) -> Unit) {
    val isDeposit = tx.type == "FUND" || tx.type == "GOAL_SAVE"
    val color = if (isDeposit) PrimaryGreen else SecondaryOrange
    val symbol = if (tx.type == "FUND") "+" else "-"

    AfriSavCard(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onReceiptClick(tx) }
            .testTag("transaction_row_${tx.id}"),
        containerColor = SurfaceBg,
        borderColor = BorderSlate100,
        shape = AppShapes.card,
        contentPadding = PaddingValues(AppSpacing.md)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .background(color.copy(alpha = 0.1f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = when (tx.type) {
                        "FUND" -> Icons.Default.AddCard
                        "TRANSFER" -> Icons.Default.Send
                        "GOAL_SAVE" -> Icons.Default.Savings
                        "BUY_FOOD" -> Icons.Default.Restaurant
                        else -> Icons.Default.SwapHoriz
                    },
                    contentDescription = null,
                    tint = color,
                    modifier = Modifier.size(AppIconSize.md)
                )
            }
            Spacer(modifier = Modifier.width(AppSpacing.md))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = tx.title,
                    style = AppTypography.label.copy(fontWeight = FontWeight.Bold),
                    color = TextDark
                )
                Text(
                    text = tx.description,
                    style = AppTypography.caption,
                    color = TextSlate500,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = "$symbol ₦${String.format("%,.2f", tx.amount)}",
                    style = AppTypography.sectionHeader.copy(fontWeight = FontWeight.Bold),
                    color = color
                )
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.ReceiptLong,
                        contentDescription = "View Receipt",
                        tint = Color(0xFF2563EB),
                        modifier = Modifier.size(AppIconSize.xs)
                    )
                    Spacer(modifier = Modifier.width(2.dp))
                    Text(
                        text = "Receipt",
                        style = AppTypography.caption.copy(fontWeight = FontWeight.Bold),
                        color = Color(0xFF2563EB)
                    )
                }
            }
        }
    }
}

@Composable
fun DashboardTransactionRow(tx: WalletTransaction, onReceiptClick: (WalletTransaction) -> Unit) {
    val isDeposit = tx.type == "FUND" || tx.type == "GOAL_SAVE"
    val color = if (isDeposit) PrimaryGreen else SecondaryOrange
    val symbol = if (tx.type == "FUND") "+" else "-"
    val timeString = remember(tx.timestamp) {
        try {
            val sdf = java.text.SimpleDateFormat("MMM dd • HH:mm", java.util.Locale.getDefault())
            sdf.format(java.util.Date(tx.timestamp))
        } catch (e: Exception) {
            ""
        }
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onReceiptClick(tx) }
            .testTag("dashboard_transaction_row_${tx.id}")
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(36.dp)
                .background(color.copy(alpha = 0.1f), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = when (tx.type) {
                    "FUND" -> Icons.Default.AddCard
                    "TRANSFER" -> Icons.Default.Send
                    "GOAL_SAVE" -> Icons.Default.Savings
                    "BUY_FOOD" -> Icons.Default.Restaurant
                    else -> Icons.Default.SwapHoriz
                },
                contentDescription = null,
                tint = color,
                modifier = Modifier.size(18.dp)
            )
        }
        Spacer(modifier = Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = tx.title,
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp,
                    color = TextSlate800,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f, fill = false)
                )
                if (timeString.isNotEmpty()) {
                    Text(
                        text = "• $timeString",
                        fontSize = 10.sp,
                        color = TextSlate400
                    )
                }
            }
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = tx.description,
                fontSize = 11.sp,
                color = TextSlate500,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
        Spacer(modifier = Modifier.width(8.dp))
        Column(horizontalAlignment = Alignment.End) {
            Text(
                text = "$symbol ₦${String.format("%,.2f", tx.amount)}",
                fontWeight = FontWeight.ExtraBold,
                fontSize = 13.sp,
                color = color
            )
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.ReceiptLong,
                    contentDescription = "View Receipt",
                    tint = Color(0xFF2563EB),
                    modifier = Modifier.size(10.dp)
                )
                Spacer(modifier = Modifier.width(2.dp))
                Text(
                    text = "Receipt",
                    fontSize = 9.sp,
                    color = Color(0xFF2563EB),
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
fun DigitalReceiptDialog(
    tx: WalletTransaction,
    onDismiss: () -> Unit,
    viewModel: KoboViewModel
) {
    val context = LocalContext.current
    val isDarkMode by viewModel.isDarkMode.collectAsStateWithLifecycle()
    val bg = if (isDarkMode) Color(0xFF1E293B) else Color.White
    val textPrimary = if (isDarkMode) Color(0xFFF8FAFC) else Color(0xFF0F172A)
    val textSecondary = if (isDarkMode) Color(0xFF94A3B8) else Color(0xFF64748B)
    val dividerColor = if (isDarkMode) Color(0xFF334155) else Color(0xFFE2E8F0)

    val timeString = remember(tx.timestamp) {
        try {
            val sdf = java.text.SimpleDateFormat("MMM dd, yyyy • HH:mm:ss", java.util.Locale.getDefault())
            sdf.format(java.util.Date(tx.timestamp))
        } catch (e: Exception) {
            ""
        }
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.92f)
                .wrapContentHeight()
                .padding(16.dp)
                .testTag("digital_receipt_dialog"),
            shape = RoundedCornerShape(24.dp),
            color = bg,
            tonalElevation = 4.dp
        ) {
            Column(
                modifier = Modifier
                    .padding(24.dp)
                    .fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Receipt Top Icon
                Box(
                    modifier = Modifier
                        .size(60.dp)
                        .background(PrimaryGreen.copy(alpha = 0.15f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.ReceiptLong,
                        contentDescription = "Receipt Icon",
                        tint = PrimaryGreen,
                        modifier = Modifier.size(32.dp)
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "Transaction Receipt",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = textPrimary
                )
                Text(
                    text = "AfriSav Food Security Platform",
                    fontSize = 12.sp,
                    color = textSecondary
                )

                Spacer(modifier = Modifier.height(20.dp))

                // Dashed separator
                Canvas(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(1.dp)
                ) {
                    val pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 10f), 0f)
                    drawLine(
                        color = dividerColor,
                        start = Offset(0f, 0f),
                        end = Offset(size.width, 0f),
                        pathEffect = pathEffect,
                        strokeWidth = 2f
                    )
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Receipt Fields List
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    ReceiptFieldRow(label = "Receipt Reference", value = "AFRISAV-TXN-${tx.id}", textPrimary = textPrimary, textSecondary = textSecondary)
                    ReceiptFieldRow(label = "Date & Time", value = timeString, textPrimary = textPrimary, textSecondary = textSecondary)
                    ReceiptFieldRow(label = "Category", value = when (tx.type) {
                        "FUND" -> "Wallet Top-up 📥"
                        "TRANSFER" -> "Outgoing Transfer 📤"
                        "GOAL_SAVE" -> "Circle Contribution 👥"
                        "BUY_FOOD" -> "Bulk Purchase 🛒"
                        else -> "Transaction 🔄"
                    }, textPrimary = textPrimary, textSecondary = textSecondary)
                    ReceiptFieldRow(label = "Payment Method", value = "AfriSav Secure Wallet", textPrimary = textPrimary, textSecondary = textSecondary)
                    ReceiptFieldRow(label = "Status", value = "SUCCESSFUL ✅", textPrimary = PrimaryGreen, textSecondary = textSecondary, isBoldValue = true)
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Dashed separator
                Canvas(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(1.dp)
                ) {
                    val pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 10f), 0f)
                    drawLine(
                        color = dividerColor,
                        start = Offset(0f, 0f),
                        end = Offset(size.width, 0f),
                        pathEffect = pathEffect,
                        strokeWidth = 2f
                    )
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Items/Description
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            if (isDarkMode) Color(0xFF334155).copy(alpha = 0.3f) else Color(0xFFF8FAFC),
                            RoundedCornerShape(12.dp)
                        )
                        .padding(16.dp)
                ) {
                    Text(
                        text = "TRANSACTION DETAILS",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = textSecondary,
                        letterSpacing = 1.sp
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = tx.title,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = textPrimary
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = tx.description,
                        fontSize = 11.sp,
                        color = textSecondary,
                        lineHeight = 16.sp
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Total Amount
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Total Paid",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Medium,
                        color = textSecondary
                    )
                    Text(
                        text = "₦${String.format("%,.2f", tx.amount)}",
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Black,
                        color = if (tx.type == "FUND") PrimaryGreen else SecondaryOrange
                    )
                }

                Spacer(modifier = Modifier.height(28.dp))

                // Actions: Download & Close
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp),
                        shape = RoundedCornerShape(14.dp),
                        border = BorderStroke(1.dp, textSecondary.copy(alpha = 0.5f))
                    ) {
                        Text(
                            text = "Close",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = textPrimary
                        )
                    }

                    Button(
                        onClick = {
                            viewModel.downloadTransactionReceipt(context, tx)
                        },
                        modifier = Modifier
                            .weight(1.5f)
                            .height(48.dp)
                            .testTag("download_receipt_button"),
                        colors = ButtonDefaults.buttonColors(containerColor = PrimaryGreen),
                        shape = RoundedCornerShape(14.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.FileDownload,
                            contentDescription = "Download Icon",
                            tint = Color.White,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Save Receipt",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun ReceiptFieldRow(
    label: String,
    value: String,
    textPrimary: Color,
    textSecondary: Color,
    isBoldValue: Boolean = false
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            fontSize = 12.sp,
            color = textSecondary
        )
        Text(
            text = value,
            fontSize = 12.sp,
            fontWeight = if (isBoldValue) FontWeight.Bold else FontWeight.Medium,
            color = textPrimary
        )
    }
}

data class TrendItemData(
    val title: String,
    val prices: List<Double>,
    val changeText: String,
    val recommendation: String,
    val unitLabel: String,
    val minY: Double,
    val maxY: Double
)

@Composable
fun HistoricalPriceTrendChart(
    viewModel: KoboViewModel
) {
    var isExpanded by remember { mutableStateOf(false) }
    var selectedTrendItem by remember { mutableStateOf("Rice") }

    val trendsData = remember {
        mapOf(
            "Rice" to TrendItemData(
                title = "50kg Premium Caprice Rice",
                prices = listOf(64000.0, 68000.0, 72000.0),
                changeText = "₦64,000 ➜ ₦72,000 (+12.5%) over last 3 months",
                recommendation = "Wholesale imports are experiencing port custom duty hikes. Join Alimosho Wholesale Rice Circle to purchase at ₦58,000!",
                unitLabel = "₦",
                minY = 50000.0,
                maxY = 80000.0
            ),
            "Oil" to TrendItemData(
                title = "25L Jerrycan Vegetable Oil",
                prices = listOf(24500.0, 25800.0, 27500.0),
                changeText = "₦24,500 ➜ ₦27,500 (+12.2%) over last 3 months",
                recommendation = "Factory pricing on refined vegetable oil is rising due to transport logistics. Join Alimosho Oil Circle to save up to 18%!",
                unitLabel = "₦",
                minY = 20000.0,
                maxY = 30000.0
            ),
            "Beans" to TrendItemData(
                title = "Oloyin Honey Beans (Paint Bucket)",
                prices = listOf(3400.0, 3900.0, 4500.0),
                changeText = "₦3,400 ➜ ₦4,500 (+32.4%) over last 3 months",
                recommendation = "Post-harvest logistical constraints have inflated store prices by 32%. Secure direct farm batches via bulk buying pools.",
                unitLabel = "₦",
                minY = 2000.0,
                maxY = 5000.0
            ),
            "Garri" to TrendItemData(
                title = "Sweet Sweet Sweet Garri (1 Bag)",
                prices = listOf(18000.0, 19500.0, 21000.0),
                changeText = "₦18,000 ➜ ₦21,000 (+16.7%) over last 3 months",
                recommendation = "Cassava processing and fuel costs raised bulk prices. Join split-bag pools before further retail inflation.",
                unitLabel = "₦",
                minY = 15000.0,
                maxY = 25000.0
            ),
            "Tomatoes" to TrendItemData(
                title = "Fresh Pepper / Tomatoes (Big Basket)",
                prices = listOf(15000.0, 22000.0, 25000.0),
                changeText = "₦15,000 ➜ ₦25,000 (+66.7%) over last 3 months",
                recommendation = "Northern farm harvest supply shocks caused a massive 66% price spike. Co-buy wholesale baskets directly to bypass retail markups.",
                unitLabel = "₦",
                minY = 10000.0,
                maxY = 30000.0
            )
        )
    }

    val currentData = trendsData[selectedTrendItem] ?: trendsData["Rice"]!!

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp)
            .testTag("historical_price_trends_card"),
        colors = CardDefaults.cardColors(containerColor = SurfaceBg),
        border = BorderStroke(1.dp, BorderSlate100),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Header Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Timeline,
                        contentDescription = null,
                        tint = PrimaryGreen,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Historical Price Trends 📈",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextSlate800
                    )
                }
                TextButton(onClick = { isExpanded = !isExpanded }) {
                    Text(
                        text = if (isExpanded) "Collapse ✕" else "Explore 📈",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = PrimaryGreen
                    )
                }
            }

            Text(
                text = "Visualize local retail vs. factory bulk food prices over the last 3 months.",
                fontSize = 11.sp,
                color = TextSlate500,
                modifier = Modifier.padding(top = 2.dp)
            )

            if (isExpanded) {
                Spacer(modifier = Modifier.height(14.dp))

                // Item filter chips
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    listOf(
                        "Rice" to "Rice 🌾",
                        "Oil" to "Oil 🛢️",
                        "Beans" to "Beans 🫘",
                        "Garri" to "Garri 🥣",
                        "Tomatoes" to "Tomatoes 🍅"
                    ).forEach { (key, display) ->
                        val isSel = selectedTrendItem == key
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (isSel) PrimaryGreen else Color(0xFFF1F5F9))
                                .clickable { selectedTrendItem = key }
                                .padding(horizontal = 8.dp, vertical = 6.dp)
                        ) {
                            Text(
                                text = display,
                                fontSize = 11.sp,
                                color = if (isSel) Color.White else TextSlate500,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Trend Summary Box
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0xFFF8FAFC), RoundedCornerShape(10.dp))
                        .padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = currentData.title,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextSlate800
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = currentData.changeText,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (currentData.prices.last() > currentData.prices.first()) SecondaryOrange else PrimaryGreen
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    // Short trend tag
                    Box(
                        modifier = Modifier
                            .background(
                                color = if (currentData.prices.last() > currentData.prices.first()) Color(0xFFFEF2F2) else Color(0xFFF0FDF4),
                                shape = RoundedCornerShape(6.dp)
                            )
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = if (currentData.prices.last() > currentData.prices.first()) "Upward Trend" else "Downward Trend",
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (currentData.prices.last() > currentData.prices.first()) Color.Red else PrimaryGreen
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // The Premium Interactive Chart (Canvas)
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(180.dp)
                        .background(SurfaceBg)
                ) {
                    val primaryColorVal = PrimaryGreen
                    
                    Canvas(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 4.dp)
                    ) {
                        val width = size.width
                        val height = size.height
                        
                        val paddingLeft = 70f
                        val paddingRight = 40f
                        val paddingTop = 30f
                        val paddingBottom = 40f
                        
                        val chartWidth = width - paddingLeft - paddingRight
                        val chartHeight = height - paddingTop - paddingBottom
                        
                        val minYVal = currentData.minY
                        val maxYVal = currentData.maxY
                        val valueRange = maxYVal - minYVal
                        
                        // Draw grid lines (horizontal)
                        val gridCount = 3
                        for (i in 0..gridCount) {
                            val yRatio = i.toFloat() / gridCount.toFloat()
                            val yLoc = paddingTop + yRatio * chartHeight
                            drawLine(
                                color = Color(0xFFE2E8F0),
                                start = Offset(paddingLeft, yLoc),
                                end = Offset(paddingLeft + chartWidth, yLoc),
                                strokeWidth = 1f
                            )
                            
                            // Y value labels
                            val gridVal = maxYVal - yRatio * valueRange
                            drawIntoCanvas { canvas ->
                                val textPaint = Paint().apply {
                                    color = android.graphics.Color.parseColor("#94A3B8")
                                    textSize = 22f
                                    typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
                                }
                                val displayValStr = if (gridVal >= 1000) {
                                    "₦${String.format("%.0fk", gridVal / 1000)}"
                                } else {
                                    "₦${gridVal.toInt()}"
                                }
                                canvas.nativeCanvas.drawText(
                                    displayValStr,
                                    10f,
                                    yLoc + 8f,
                                    textPaint
                                )
                            }
                        }
                        
                        // X axis month coordinates
                        val months = listOf("May 2026", "June 2026", "July (Current)")
                        val xPoints = listOf(
                            paddingLeft,
                            paddingLeft + chartWidth / 2f,
                            paddingLeft + chartWidth
                        )
                        
                        // Map prices to Y coordinates
                        val yPoints = currentData.prices.map { price ->
                            val ratio = (price - minYVal) / valueRange
                            // Invert since Y goes downwards in android coordinates
                            paddingTop + chartHeight - (ratio.toFloat() * chartHeight)
                        }
                        
                        // Draw Area under line (Gradient)
                        val areaPath = Path().apply {
                            moveTo(xPoints[0], paddingTop + chartHeight)
                            lineTo(xPoints[0], yPoints[0])
                            
                            val controlPoint1X = (xPoints[0] + xPoints[1]) / 2f
                            val controlPoint1Y = (yPoints[0] + yPoints[1]) / 2f
                            quadraticTo(controlPoint1X, controlPoint1Y, xPoints[1], yPoints[1])
                            
                            val controlPoint2X = (xPoints[1] + xPoints[2]) / 2f
                            val controlPoint2Y = (yPoints[1] + yPoints[2]) / 2f
                            quadraticTo(controlPoint2X, controlPoint2Y, xPoints[2], yPoints[2])
                            
                            lineTo(xPoints[2], paddingTop + chartHeight)
                            close()
                        }
                        
                        drawPath(
                            path = areaPath,
                            brush = Brush.verticalGradient(
                                colors = listOf(
                                    primaryColorVal.copy(alpha = 0.25f),
                                    primaryColorVal.copy(alpha = 0.01f)
                                ),
                                startY = yPoints.minOrNull()?.toFloat() ?: paddingTop,
                                endY = paddingTop + chartHeight
                            )
                        )
                        
                        // Draw primary trend line
                        val linePath = Path().apply {
                            moveTo(xPoints[0], yPoints[0])
                            val controlPoint1X = (xPoints[0] + xPoints[1]) / 2f
                            val controlPoint1Y = (yPoints[0] + yPoints[1]) / 2f
                            quadraticTo(controlPoint1X, controlPoint1Y, xPoints[1], yPoints[1])
                            
                            val controlPoint2X = (xPoints[1] + xPoints[2]) / 2f
                            val controlPoint2Y = (yPoints[1] + yPoints[2]) / 2f
                            quadraticTo(controlPoint2X, controlPoint2Y, xPoints[2], yPoints[2])
                        }
                        
                        drawPath(
                            path = linePath,
                            color = primaryColorVal,
                            style = Stroke(width = 6f, cap = StrokeCap.Round)
                        )
                        
                        // Draw point circles with glow and tooltip/price labels
                        xPoints.forEachIndexed { index, x ->
                            val y = yPoints[index]
                            val pVal = currentData.prices[index]
                            
                            // Outer shadow glow
                            drawCircle(
                                color = primaryColorVal.copy(alpha = 0.15f),
                                radius = 16f,
                                center = Offset(x, y)
                            )
                            // Inner core circle
                            drawCircle(
                                color = primaryColorVal,
                                radius = 7f,
                                center = Offset(x, y)
                            )
                            // White dot center
                            drawCircle(
                                color = Color.White,
                                radius = 3.5f,
                                center = Offset(x, y)
                            )
                            
                            // Month Labels under points
                            drawIntoCanvas { canvas ->
                                val monthPaint = Paint().apply {
                                    color = android.graphics.Color.parseColor("#64748B")
                                    textSize = 20f
                                    textAlign = Paint.Align.CENTER
                                    typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
                                }
                                canvas.nativeCanvas.drawText(
                                    months[index],
                                    x,
                                    paddingTop + chartHeight + 32f,
                                    monthPaint
                                )
                            }
                            
                            // Exact values above points
                            drawIntoCanvas { canvas ->
                                val valPaint = Paint().apply {
                                    color = android.graphics.Color.parseColor("#1E293B")
                                    textSize = 22f
                                    textAlign = Paint.Align.CENTER
                                    typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
                                }
                                canvas.nativeCanvas.drawText(
                                    "₦${String.format("%,.0f", pVal)}",
                                    x,
                                    y - 18f,
                                    valPaint
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Price Drop Alerts Notification Toggle Box
                val alerts by viewModel.priceAlerts.collectAsStateWithLifecycle()
                val existingAlert = alerts.find { it.category.equals(selectedTrendItem, ignoreCase = true) }
                val hasAlert = existingAlert != null

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp)
                        .testTag("price_drop_alerts_toggle_card"),
                    colors = CardDefaults.cardColors(
                        containerColor = if (hasAlert) Color(0xFFECFDF5) else Color(0xFFF8FAFC)
                    ),
                    border = BorderStroke(
                        width = 1.dp,
                        color = if (hasAlert) Color(0xFFA7F3D0) else Color(0xFFE2E8F0)
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(
                                modifier = Modifier.weight(1f),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = if (hasAlert) Icons.Default.NotificationsActive else Icons.Default.Notifications,
                                    contentDescription = "Notification Toggle",
                                    tint = if (hasAlert) PrimaryGreen else TextSlate400,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Column {
                                    Text(
                                        text = "Price Drop Alerts for $selectedTrendItem",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = TextSlate800
                                    )
                                    Text(
                                        text = if (hasAlert) "Alert is active below threshold 🔔" else "Get notified when prices drop below your threshold",
                                        fontSize = 10.sp,
                                        color = if (hasAlert) PrimaryGreen else TextSlate500
                                    )
                                }
                            }
                            Switch(
                                checked = hasAlert,
                                onCheckedChange = { isChecked ->
                                    if (isChecked) {
                                        // Auto-create alert at 10% below current price
                                        val latestPrice = currentData.prices.last()
                                        val defaultTarget = latestPrice * 0.9
                                        viewModel.addPriceAlert(selectedTrendItem, defaultTarget, latestPrice)
                                    } else {
                                        existingAlert?.let { viewModel.removePriceAlert(it.id) }
                                    }
                                },
                                colors = SwitchDefaults.colors(
                                    checkedThumbColor = Color.White,
                                    checkedTrackColor = PrimaryGreen,
                                    uncheckedThumbColor = Color.White,
                                    uncheckedTrackColor = Color(0xFFCBD5E1)
                                ),
                                modifier = Modifier.testTag("price_alert_switch_$selectedTrendItem")
                            )
                        }

                        if (hasAlert && existingAlert != null) {
                            Spacer(modifier = Modifier.height(10.dp))
                            Divider(color = Color(0xFFD1FAE5), thickness = 1.dp)
                            Spacer(modifier = Modifier.height(10.dp))

                            Text(
                                text = "Customize Target Price Threshold:",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = TextSlate800
                            )
                            Spacer(modifier = Modifier.height(6.dp))

                            var targetInputText by remember(existingAlert.id) {
                                mutableStateOf(existingAlert.targetPrice.toInt().toString())
                            }

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                OutlinedTextField(
                                    value = targetInputText,
                                    onValueChange = { newVal ->
                                        if (newVal.all { it.isDigit() }) {
                                            targetInputText = newVal
                                            val numericValue = newVal.toDoubleOrNull() ?: 0.0
                                            if (numericValue > 0) {
                                                // Update existing alert
                                                viewModel.removePriceAlert(existingAlert.id)
                                                viewModel.addPriceAlert(
                                                    category = selectedTrendItem,
                                                    targetPrice = numericValue,
                                                    currentPrice = currentData.prices.last()
                                                )
                                            }
                                        }
                                    },
                                    prefix = { Text("₦", fontSize = 11.sp, color = TextSlate800) },
                                    singleLine = true,
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(46.dp),
                                    shape = RoundedCornerShape(8.dp),
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedContainerColor = Color.White,
                                        unfocusedContainerColor = Color.White,
                                        focusedBorderColor = PrimaryGreen,
                                        unfocusedBorderColor = Color(0xFFCBD5E1)
                                    ),
                                    textStyle = androidx.compose.ui.text.TextStyle(fontSize = 12.sp)
                                )

                                // Percentage Quick Buttons
                                listOf(5, 10, 15, 20).forEach { pct ->
                                    val pctVal = currentData.prices.last() * (100 - pct) / 100.0
                                    val isCurrentPct = Math.abs(existingAlert.targetPrice - pctVal) < (currentData.prices.last() * 0.02)
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(6.dp))
                                            .background(if (isCurrentPct) PrimaryGreen else Color.White)
                                            .border(1.dp, if (isCurrentPct) PrimaryGreen else Color(0xFFCBD5E1), RoundedCornerShape(6.dp))
                                            .clickable {
                                                targetInputText = pctVal.toInt().toString()
                                                viewModel.removePriceAlert(existingAlert.id)
                                                viewModel.addPriceAlert(
                                                    category = selectedTrendItem,
                                                    targetPrice = pctVal,
                                                    currentPrice = currentData.prices.last()
                                                )
                                            }
                                            .padding(horizontal = 6.dp, vertical = 6.dp)
                                    ) {
                                        Text(
                                            text = "-$pct%",
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = if (isCurrentPct) Color.White else TextSlate500
                                        )
                                    }
                                }
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Current Price: ₦${String.format("%,.0f", currentData.prices.last())}. You will be alerted when the market price drops below ₦${String.format("%,.0f", existingAlert.targetPrice)}.",
                                fontSize = 10.sp,
                                color = TextSlate500
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Strategic Advice panel
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0xFFFFFBEB), RoundedCornerShape(10.dp))
                        .border(1.dp, Color(0xFFFEF3C7), RoundedCornerShape(10.dp))
                        .padding(10.dp)
                ) {
                    Row(verticalAlignment = Alignment.Top) {
                        Text("💡", fontSize = 16.sp)
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text(
                                text = "Mama Olufunke's Strategic Advice",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF92400E)
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = currentData.recommendation,
                                fontSize = 10.sp,
                                color = Color(0xFFB45309),
                                lineHeight = 14.sp
                            )
                        }
                    }
                }
            }
        }
    }
}

// --- ORDERS SCREEN ---
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OrdersScreen(
    viewModel: KoboViewModel,
    onBackClick: () -> Unit,
    onContactRider: ((EscrowOrder, String) -> Unit)? = null
) {
    val escrowOrders by viewModel.escrowOrdersForBuyer.collectAsStateWithLifecycle()
    val allReviews by viewModel.allReviews.collectAsStateWithLifecycle()
    val currentUserName by viewModel.currentUserName.collectAsStateWithLifecycle()
    var selectedTab by remember { mutableStateOf("All") } // "All", "Pending", "Completed", "Disputed"
    
    var selectedEscrowForRating by remember { mutableStateOf<EscrowOrder?>(null) }
    var selectedEscrowForDispute by remember { mutableStateOf<EscrowOrder?>(null) }
    var disputeReason by remember { mutableStateOf("") }
    var reviewTargetOrder by remember { mutableStateOf<EscrowOrder?>(null) }
    var reviewTargetType by remember { mutableStateOf("") } // "VENDOR" or "RIDER"

    if (selectedEscrowForRating != null) {
        EscrowCompletionDialog(
            escrow = selectedEscrowForRating!!,
            onDismiss = { selectedEscrowForRating = null },
            onConfirm = { vRating, vReview, bRating, bReview, rRating, rReview ->
                viewModel.confirmEscrowReceipt(
                    orderId = selectedEscrowForRating!!.id,
                    vendorRating = vRating,
                    vendorReview = vReview,
                    buyerRating = bRating,
                    buyerReview = bReview,
                    riderRating = rRating,
                    riderReview = rReview
                )
                selectedEscrowForRating = null
            }
        )
    }

    if (reviewTargetOrder != null) {
        BuyerLeaveReviewDialog(
            order = reviewTargetOrder!!,
            targetType = reviewTargetType,
            onDismiss = { reviewTargetOrder = null },
            onSubmit = { rating, comment ->
                if (reviewTargetType == "VENDOR") {
                    viewModel.submitReviewRating(
                        targetName = reviewTargetOrder!!.vendorName,
                        isTargetSeller = true,
                        rating = rating,
                        reviewText = comment,
                        orderId = reviewTargetOrder!!.id
                    )
                } else if (reviewTargetType == "RIDER" && !reviewTargetOrder!!.riderName.isNullOrEmpty()) {
                    viewModel.submitReviewRating(
                        targetName = reviewTargetOrder!!.riderName!!,
                        isTargetSeller = false,
                        rating = rating,
                        reviewText = comment,
                        orderId = reviewTargetOrder!!.id
                    )
                }
                reviewTargetOrder = null
            }
        )
    }

    if (selectedEscrowForDispute != null) {
        AlertDialog(
            onDismissRequest = { selectedEscrowForDispute = null },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Warning, contentDescription = null, tint = Color.Red, modifier = Modifier.size(24.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("File Escrow Contract Dispute", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                }
            },
            text = {
                Column {
                    Text(
                        "Filing a formal dispute immediately freezes the contract's escrow funds (₦${String.format("%,.0f", selectedEscrowForDispute!!.amount)}) in safe holding. Please state your reason below.",
                        fontSize = 12.sp,
                        color = Color(0xFF475569)
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    OutlinedTextField(
                        value = disputeReason,
                        onValueChange = { disputeReason = it },
                        placeholder = { Text("e.g. Goods not delivered, item count incomplete...") },
                        modifier = Modifier.fillMaxWidth(),
                        textStyle = TextStyle(fontSize = 13.sp)
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (disputeReason.isNotBlank()) {
                            viewModel.fileEscrowDispute(selectedEscrowForDispute!!.id, disputeReason)
                            selectedEscrowForDispute = null
                            disputeReason = ""
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Red)
                ) {
                    Text("File Dispute")
                }
            },
            dismissButton = {
                TextButton(onClick = { selectedEscrowForDispute = null }) {
                    Text("Cancel")
                }
            }
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(SoftBackground)
    ) {
        // Sticky Header Row
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(SurfaceBg)
                .statusBarsPadding()
                .padding(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = onBackClick,
                modifier = Modifier
                    .background(Color(0xFFF1F5F9), CircleShape)
                    .size(36.dp)
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back",
                    tint = TextSlate800,
                    modifier = Modifier.size(20.dp)
                )
            }
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                text = "My Food Orders 📦",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = TextSlate800
            )
        }

        Column(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
        ) {
            // Hero decorative image banner
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                shape = RoundedCornerShape(16.dp)
            ) {
                Image(
                    painter = painterResource(id = R.drawable.img_orders_banner_1783336525985),
                    contentDescription = "Food Delivery tracking",
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(110.dp),
                    contentScale = ContentScale.Crop
                )
            }

            // Tabs / Filters
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                val tabs = listOf("All", "Pending", "Completed", "Disputed")
                tabs.forEach { tab ->
                    val isSelected = selectedTab == tab
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(20.dp))
                            .background(if (isSelected) PrimaryGreen else Color.White)
                            .border(1.dp, if (isSelected) PrimaryGreen else Color(0xFFE2E8F0), RoundedCornerShape(20.dp))
                            .clickable { selectedTab = tab }
                            .padding(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = tab,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isSelected) Color.White else TextSlate500
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Filtered order list
            val filteredOrders = when (selectedTab) {
                "Pending" -> escrowOrders.filter { it.status == "PENDING" || it.status == "AWAITING_SELLER_ACCEPTANCE" }
                "Completed" -> escrowOrders.filter { it.status == "COMPLETED" }
                "Disputed" -> escrowOrders.filter { it.status == "DISPUTED" }
                else -> escrowOrders
            }

            if (filteredOrders.isEmpty()) {
                EmptyStateCard(
                    message = "No $selectedTab Orders Found",
                    subMessage = "You haven't placed or received any orders in the '$selectedTab' category yet.",
                    btnText = "Go to Market",
                    onClick = { viewModel.setActiveTab("market") },
                    icon = Icons.Default.ReceiptLong,
                    iconColor = Color(0xFFEA580C)
                )
            } else {
                filteredOrders.forEach { esc ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(start = 16.dp, end = 16.dp, bottom = 12.dp),
                        colors = CardDefaults.cardColors(containerColor = SurfaceBg),
                        shape = RoundedCornerShape(16.dp),
                        border = BorderStroke(1.dp, Color(0xFFE2E8F0))
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            // Top Row: Item Name & Status
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = esc.itemName,
                                        fontSize = 15.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = TextSlate800
                                    )
                                    Text(
                                        text = "Vendor: ${esc.vendorName}",
                                        fontSize = 12.sp,
                                        color = TextSlate500
                                    )
                                }
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(
                                            when (esc.status) {
                                                "AWAITING_SELLER_ACCEPTANCE" -> Color(0xFFFEF3C7) // Amber
                                                "DECLINED" -> Color(0xFFFEE2E2) // Light Red
                                                "COMPLETED" -> Color(0xFFECFDF5)
                                                "DISPUTED" -> Color(0xFFFEF2F2)
                                                else -> Color(0xFFEFF6FF)
                                            }
                                        )
                                        .padding(horizontal = 8.dp, vertical = 4.dp)
                                ) {
                                    Text(
                                        text = when (esc.status) {
                                            "AWAITING_SELLER_ACCEPTANCE" -> "Awaiting Acceptance"
                                            "PENDING" -> "Accepted"
                                            "DECLINED" -> "Declined"
                                            "COMPLETED" -> "Completed"
                                            "DISPUTED" -> "Disputed"
                                            else -> esc.status
                                        },
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = when (esc.status) {
                                            "AWAITING_SELLER_ACCEPTANCE" -> Color(0xFFD97706) // Dark Amber
                                            "DECLINED" -> Color(0xFFDC2626) // Red
                                            "COMPLETED" -> Color(0xFF059669)
                                            "DISPUTED" -> Color(0xFFDC2626)
                                            else -> Color(0xFF2563EB)
                                        }
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(10.dp))
                            Divider(color = Color(0xFFF1F5F9))
                            Spacer(modifier = Modifier.height(10.dp))

                            if (esc.status == "AWAITING_SELLER_ACCEPTANCE") {
                                Card(
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = CardDefaults.cardColors(containerColor = Color(0xFFFEF3C7).copy(alpha = 0.5f)),
                                    border = BorderStroke(1.dp, Color(0xFFFDE68A)),
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Column(modifier = Modifier.padding(12.dp)) {
                                        Text(
                                            text = "⏳ Awaiting Seller Acceptance",
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color(0xFFB45309)
                                        )
                                        Text(
                                            text = "The seller has been alerted to review and accept your order. No money has been deducted from your wallet yet. Payment will only be securely escrowed once accepted.",
                                            fontSize = 10.sp,
                                            color = Color(0xFF92400E),
                                            modifier = Modifier.padding(top = 4.dp)
                                        )
                                    }
                                }
                            } else if (esc.status == "DECLINED") {
                                Card(
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = CardDefaults.cardColors(containerColor = Color(0xFFFEF2F2)),
                                    border = BorderStroke(1.dp, Color(0xFFFCA5A5)),
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Column(modifier = Modifier.padding(12.dp)) {
                                        Text(
                                            text = "❌ Order Request Declined",
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color(0xFF991B1B)
                                        )
                                        Text(
                                            text = "The seller has declined this order request. No funds were deducted from your wallet.",
                                            fontSize = 10.sp,
                                            color = Color(0xFF7F1D1D),
                                            modifier = Modifier.padding(top = 4.dp)
                                        )
                                    }
                                }
                            } else {
                                // Logistics Progress and Status
                                Text(
                                    text = "LOGISTICS & RIDER TRACKING",
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = TextSlate400,
                                    letterSpacing = 0.5.sp
                                )
                                Spacer(modifier = Modifier.height(6.dp))

                                // Step progress indicators
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    val steps = listOf(
                                        "Placed" to true,
                                        "Rider Assigned" to (!esc.riderName.isNullOrEmpty()),
                                        "Picked Up" to esc.isPickedUp,
                                        "Delivered" to (esc.status == "COMPLETED")
                                    )

                                    steps.forEachIndexed { index, (label, active) ->
                                        Column(
                                            horizontalAlignment = Alignment.CenterHorizontally,
                                            modifier = Modifier.weight(1f)
                                        ) {
                                            Box(
                                                modifier = Modifier
                                                    .size(24.dp)
                                                    .clip(CircleShape)
                                                    .background(if (active) PrimaryGreen else Color(0xFFE2E8F0)),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Text(
                                                    text = "${index + 1}",
                                                    fontSize = 10.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = if (active) Color.White else TextSlate500
                                                )
                                            }
                                            Spacer(modifier = Modifier.height(4.dp))
                                            Text(
                                                text = label,
                                                fontSize = 9.sp,
                                                fontWeight = if (active) FontWeight.Bold else FontWeight.Normal,
                                                color = if (active) PrimaryGreen else TextSlate400,
                                                textAlign = TextAlign.Center
                                            )
                                        }
                                    }
                                }

                                Spacer(modifier = Modifier.height(12.dp))

                                // Dispatch Rider Details Card
                                Card(
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = CardDefaults.cardColors(containerColor = BgSlate50),
                                    border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Column(modifier = Modifier.padding(10.dp)) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.DirectionsBike,
                                                contentDescription = null,
                                                tint = if (!esc.riderName.isNullOrEmpty()) PrimaryGreen else TextSlate400,
                                                modifier = Modifier.size(24.dp)
                                            )
                                            Spacer(modifier = Modifier.width(10.dp))
                                            Column {
                                                Text(
                                                    text = if (!esc.riderName.isNullOrEmpty()) "Dispatch Rider Accepted!" else "Assigning Dispatch Rider...",
                                                    fontSize = 12.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = TextSlate800
                                                )
                                                Text(
                                                    text = if (!esc.riderName.isNullOrEmpty()) {
                                                        if (esc.isPickedUp) {
                                                            "Rider ${esc.riderName} has picked up your order and is currently in transit!"
                                                        } else {
                                                            "Rider ${esc.riderName} accepted the order. Heading to vendor stall to pick up!"
                                                        }
                                                    } else {
                                                        "Connecting with AfriSav partner dispatch riders nearby."
                                                    },
                                                    fontSize = 10.sp,
                                                    color = TextSlate500
                                                )
                                            }
                                        }

                                        if (!esc.riderName.isNullOrEmpty()) {
                                            Spacer(modifier = Modifier.height(10.dp))
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                                            ) {
                                                OutlinedButton(
                                                    onClick = {
                                                        onContactRider?.invoke(esc, "CALL")
                                                    },
                                                    modifier = Modifier.weight(1f).height(32.dp).testTag("buyer_call_rider_btn"),
                                                    contentPadding = PaddingValues(0.dp),
                                                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFF16A34A)),
                                                    border = BorderStroke(1.dp, Color(0xFF16A34A))
                                                ) {
                                                    Icon(Icons.Default.Phone, contentDescription = null, modifier = Modifier.size(12.dp))
                                                    Spacer(modifier = Modifier.width(4.dp))
                                                    Text("Call Rider", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                                }
                                                OutlinedButton(
                                                    onClick = {
                                                        onContactRider?.invoke(esc, "CHAT")
                                                    },
                                                    modifier = Modifier.weight(1f).height(32.dp).testTag("buyer_chat_rider_btn"),
                                                    contentPadding = PaddingValues(0.dp),
                                                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFF1D4ED8)),
                                                    border = BorderStroke(1.dp, Color(0xFF1D4ED8))
                                                ) {
                                                    Icon(Icons.Default.Chat, contentDescription = null, modifier = Modifier.size(12.dp))
                                                    Spacer(modifier = Modifier.width(4.dp))
                                                    Text("Chat Rider", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                                }
                                            }
                                        }
                                    }
                                }
                            }

                            // Show Order Delivery Tracker if rider accepted and cargo picked up
                            if (esc.status == "PENDING" && !esc.riderName.isNullOrEmpty()) {
                                Spacer(modifier = Modifier.height(12.dp))
                                OrderDeliveryTracker(order = esc, viewModel = viewModel)
                            }

                            Spacer(modifier = Modifier.height(12.dp))

                            // AfriSav Rider Bid system Negotiation Section for Buyer
                            if (esc.status == "PENDING" && esc.riderName.isNullOrEmpty()) {
                                Card(
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = CardDefaults.cardColors(containerColor = Color(0xFFEFF6FF)),
                                    border = BorderStroke(1.dp, Color(0xFFDBEAFE)),
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Column(modifier = Modifier.padding(10.dp)) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Column {
                                                Text("AFRISAV RIDER BID SYSTEM", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1D4ED8))
                                                Row(verticalAlignment = Alignment.CenterVertically) {
                                                    Text("Your Fare Offer: ", fontSize = 11.sp, color = Color(0xFF1E40AF))
                                                    Text("₦${String.format("%,.0f", esc.deliveryFee)}", fontSize = 12.sp, fontWeight = FontWeight.Black, color = Color(0xFF1D4ED8))
                                                    Text(" (Base: ₦${String.format("%,.0f", esc.baseDeliveryFee)})", fontSize = 9.sp, color = TextSlate500)
                                                }
                                            }
                                            
                                            // Quick increase offer button
                                            Button(
                                                onClick = {
                                                    viewModel.negotiateBuyerDeliveryFee(esc.id, esc.deliveryFee + 150.0)
                                                },
                                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1D4ED8)),
                                                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                                                modifier = Modifier.height(32.dp),
                                                shape = RoundedCornerShape(6.dp)
                                            ) {
                                                Text("+₦150", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color.White)
                                            }
                                        }

                                        // Or custom offer adjustment
                                        var newOfferText by remember(esc.id) { mutableStateOf("") }
                                        Row(
                                            modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                                        ) {
                                            OutlinedTextField(
                                                value = newOfferText,
                                                onValueChange = { newOfferText = it.filter { char -> char.isDigit() } },
                                                placeholder = { Text("Update offer", fontSize = 11.sp) },
                                                singleLine = true,
                                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                                modifier = Modifier.weight(1f).height(44.dp),
                                                colors = defaultTextFieldColors(),
                                                shape = RoundedCornerShape(6.dp)
                                            )
                                            
                                            Button(
                                                onClick = {
                                                    val newOffer = newOfferText.toDoubleOrNull() ?: 0.0
                                                    if (newOffer > 0.0) {
                                                        if (newOffer < esc.baseDeliveryFee) {
                                                            viewModel.triggerUiEvent("Your offer cannot be below the base delivery fee of ₦${String.format("%,.0f", esc.baseDeliveryFee)}.")
                                                        } else {
                                                            viewModel.negotiateBuyerDeliveryFee(esc.id, newOffer)
                                                            newOfferText = ""
                                                        }
                                                    }
                                                },
                                                modifier = Modifier.height(44.dp),
                                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1E40AF)),
                                                shape = RoundedCornerShape(6.dp),
                                                contentPadding = PaddingValues(horizontal = 12.dp)
                                            ) {
                                                Text("Update", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                            }
                                        }

                                        // If a rider has counters proposed
                                        if (esc.riderProposedFee > 0.0 && !esc.riderProposalName.isNullOrEmpty()) {
                                            Spacer(modifier = Modifier.height(10.dp))
                                            Divider(color = Color(0xFFDBEAFE))
                                            Spacer(modifier = Modifier.height(8.dp))
                                            
                                            Column(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .background(Color(0xFFFFF7ED), RoundedCornerShape(6.dp))
                                                    .border(1.dp, Color(0xFFFFEDD5), RoundedCornerShape(6.dp))
                                                    .padding(8.dp)
                                            ) {
                                                Text("RIDER COUNTER OFFER RECEIVED 🚴", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = Color(0xFFC2410C))
                                                Text("Rider: ${esc.riderProposalName}", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = TextSlate800)
                                                Text("Proposed Delivery Fee: ₦${String.format("%,.0f", esc.riderProposedFee)} (Your offer: ₦${String.format("%,.0f", esc.deliveryFee)})", fontSize = 11.sp, color = TextSlate500)
                                                
                                                Spacer(modifier = Modifier.height(8.dp))
                                                Row(
                                                    modifier = Modifier.fillMaxWidth(),
                                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                                ) {
                                                    Button(
                                                        onClick = {
                                                            viewModel.acceptRiderCounterOffer(esc.id)
                                                        },
                                                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF16A34A)),
                                                        modifier = Modifier.weight(1f).height(36.dp),
                                                        shape = RoundedCornerShape(6.dp)
                                                    ) {
                                                        Text("Accept Offer", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.White)
                                                    }
                                                    
                                                    OutlinedButton(
                                                        onClick = {
                                                            viewModel.rejectRiderCounterOffer(esc.id)
                                                        },
                                                        colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFFDC2626)),
                                                        border = BorderStroke(1.dp, Color(0xFFFCA5A5)),
                                                        modifier = Modifier.weight(1f).height(36.dp),
                                                        shape = RoundedCornerShape(6.dp)
                                                    ) {
                                                        Text("Decline", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                                Spacer(modifier = Modifier.height(12.dp))
                            }

                            // Secure Delivery PIN Section
                            if (esc.status == "PENDING") {
                                Card(
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = CardDefaults.cardColors(containerColor = Color(0xFFF0FDF4)),
                                    border = BorderStroke(1.dp, Color(0xFFBBF7D0)),
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Column(modifier = Modifier.padding(10.dp)) {
                                        Text("Secure Delivery PIN", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color(0xFF15803D))
                                        Text(esc.deliveryPin.ifEmpty { "Pending" }, fontSize = 16.sp, fontWeight = FontWeight.Black, color = Color(0xFF15803D))
                                        Text("Give this PIN to the dispatch rider only after you safely receive your order", fontSize = 9.sp, color = Color(0xFF15803D).copy(alpha = 0.8f))
                                    }
                                }
                            }

                            // Actions: Dispute or Release Escrow (only if pending)
                            if (esc.status == "PENDING") {
                                Spacer(modifier = Modifier.height(12.dp))
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    OutlinedButton(
                                        onClick = { selectedEscrowForDispute = esc },
                                        border = BorderStroke(1.dp, Color.Red),
                                        shape = RoundedCornerShape(8.dp),
                                        modifier = Modifier
                                            .weight(1f)
                                            .height(38.dp)
                                    ) {
                                        Text("Dispute ⚠️", fontSize = 11.sp, color = Color.Red, fontWeight = FontWeight.Bold)
                                    }

                                    Button(
                                        onClick = { selectedEscrowForRating = esc },
                                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2563EB)),
                                        shape = RoundedCornerShape(8.dp),
                                        modifier = Modifier
                                            .weight(1.5f)
                                            .height(38.dp)
                                    ) {
                                        Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(14.dp), tint = Color.White)
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("Confirm Receipt", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.White)
                                    }
                                }
                            } else if (esc.status == "DISPUTED") {
                                Spacer(modifier = Modifier.height(12.dp))
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    OutlinedButton(
                                        onClick = { viewModel.resolveEscrowDispute(esc.id, refundToBuyer = true) },
                                        border = BorderStroke(1.dp, Color.Red),
                                        shape = RoundedCornerShape(8.dp),
                                        modifier = Modifier
                                            .weight(1f)
                                            .height(38.dp)
                                    ) {
                                        Text("Refund Me 🛡️", fontSize = 11.sp, color = Color.Red, fontWeight = FontWeight.Bold)
                                    }

                                    Button(
                                        onClick = { viewModel.resolveEscrowDispute(esc.id, refundToBuyer = false) },
                                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0A8F3D)),
                                        shape = RoundedCornerShape(8.dp),
                                        modifier = Modifier
                                            .weight(1f)
                                            .height(38.dp)
                                    ) {
                                        Text("Release Funds 🤝", fontSize = 11.sp, color = Color.White, fontWeight = FontWeight.Bold)
                                    }
                                }
                            } else if (esc.status == "COMPLETED") {
                                Spacer(modifier = Modifier.height(14.dp))
                                Divider(color = Color(0xFFE2E8F0))
                                Spacer(modifier = Modifier.height(10.dp))
                                Text("ORDER REVIEWS & RATINGS ⭐", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = TextSlate500, letterSpacing = 0.5.sp)
                                Spacer(modifier = Modifier.height(8.dp))

                                val existingVendorReview = allReviews.find {
                                    it.isTargetSeller &&
                                    (it.orderId == esc.id || (it.orderId == 0 && it.reviewerName.equals(currentUserName, ignoreCase = true) && it.targetName.equals(esc.vendorName, ignoreCase = true))) &&
                                    it.targetName.equals(esc.vendorName, ignoreCase = true)
                                }

                                val existingRiderReview = if (!esc.riderName.isNullOrEmpty()) {
                                    allReviews.find {
                                        !it.isTargetSeller &&
                                        (it.orderId == esc.id || (it.orderId == 0 && it.reviewerName.equals(currentUserName, ignoreCase = true) && it.targetName.equals(esc.riderName, ignoreCase = true))) &&
                                        it.targetName.equals(esc.riderName, ignoreCase = true)
                                    }
                                } else null

                                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                    // Vendor Review Section
                                    Card(
                                        modifier = Modifier.fillMaxWidth(),
                                        colors = CardDefaults.cardColors(containerColor = if (existingVendorReview != null) Color(0xFFF8FAFC) else Color(0xFFFEF3C7).copy(alpha = 0.4f)),
                                        border = BorderStroke(1.dp, if (existingVendorReview != null) Color(0xFFE2E8F0) else Color(0xFFFDE68A)),
                                        shape = RoundedCornerShape(8.dp)
                                    ) {
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(10.dp),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Column(modifier = Modifier.weight(1f)) {
                                                Text("Vendor: ${esc.vendorName}", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = TextSlate800)
                                                if (existingVendorReview != null) {
                                                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(top = 2.dp)) {
                                                        for (i in 1..5) {
                                                            Icon(
                                                                imageVector = Icons.Default.Star,
                                                                contentDescription = null,
                                                                tint = if (i <= existingVendorReview.rating) Color(0xFFFF8C00) else Color(0xFFCBD5E1),
                                                                modifier = Modifier.size(14.dp)
                                                            )
                                                        }
                                                        Spacer(modifier = Modifier.width(6.dp))
                                                        Text("Reviewed", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFF059669))
                                                    }
                                                    if (existingVendorReview.reviewText.isNotBlank()) {
                                                        Text("\"${existingVendorReview.reviewText}\"", fontSize = 11.sp, fontStyle = androidx.compose.ui.text.font.FontStyle.Italic, color = TextSlate500, modifier = Modifier.padding(top = 2.dp))
                                                    }
                                                } else {
                                                    Text("Share feedback on food quality & packaging", fontSize = 10.sp, color = Color(0xFFB45309))
                                                }
                                            }
                                            if (existingVendorReview == null) {
                                                Button(
                                                    onClick = { reviewTargetOrder = esc; reviewTargetType = "VENDOR" },
                                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD97706)),
                                                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                                                    modifier = Modifier.height(32.dp).testTag("buyer_review_vendor_btn_${esc.id}"),
                                                    shape = RoundedCornerShape(6.dp)
                                                ) {
                                                    Icon(Icons.Default.RateReview, contentDescription = null, modifier = Modifier.size(12.dp), tint = Color.White)
                                                    Spacer(modifier = Modifier.width(4.dp))
                                                    Text("Review", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.White)
                                                }
                                            } else {
                                                OutlinedButton(
                                                    onClick = { /* disabled / already reviewed */ },
                                                    enabled = false,
                                                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                                                    modifier = Modifier.height(30.dp),
                                                    shape = RoundedCornerShape(6.dp)
                                                ) {
                                                    Text("Done ✓", fontSize = 10.sp)
                                                }
                                            }
                                        }
                                    }

                                    // Rider Review Section (if rider was assigned)
                                    if (!esc.riderName.isNullOrEmpty()) {
                                        Card(
                                            modifier = Modifier.fillMaxWidth(),
                                            colors = CardDefaults.cardColors(containerColor = if (existingRiderReview != null) Color(0xFFF8FAFC) else Color(0xFFEFF6FF)),
                                            border = BorderStroke(1.dp, if (existingRiderReview != null) Color(0xFFE2E8F0) else Color(0xFFBFDBFE)),
                                            shape = RoundedCornerShape(8.dp)
                                        ) {
                                            Row(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .padding(10.dp),
                                                horizontalArrangement = Arrangement.SpaceBetween,
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Column(modifier = Modifier.weight(1f)) {
                                                    Text("Rider: ${esc.riderName}", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = TextSlate800)
                                                    if (existingRiderReview != null) {
                                                        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(top = 2.dp)) {
                                                            for (i in 1..5) {
                                                                Icon(
                                                                    imageVector = Icons.Default.Star,
                                                                    contentDescription = null,
                                                                    tint = if (i <= existingRiderReview.rating) Color(0xFFFF8C00) else Color(0xFFCBD5E1),
                                                                    modifier = Modifier.size(14.dp)
                                                                )
                                                            }
                                                            Spacer(modifier = Modifier.width(6.dp))
                                                            Text("Reviewed", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFF059669))
                                                        }
                                                        if (existingRiderReview.reviewText.isNotBlank()) {
                                                            Text("\"${existingRiderReview.reviewText}\"", fontSize = 11.sp, fontStyle = androidx.compose.ui.text.font.FontStyle.Italic, color = TextSlate500, modifier = Modifier.padding(top = 2.dp))
                                                        }
                                                    } else {
                                                        Text("Rate rider delivery service & promptness", fontSize = 10.sp, color = Color(0xFF1D4ED8))
                                                    }
                                                }
                                                if (existingRiderReview == null) {
                                                    Button(
                                                        onClick = { reviewTargetOrder = esc; reviewTargetType = "RIDER" },
                                                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2563EB)),
                                                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                                                        modifier = Modifier.height(32.dp).testTag("buyer_review_rider_btn_${esc.id}"),
                                                        shape = RoundedCornerShape(6.dp)
                                                    ) {
                                                        Icon(Icons.Default.RateReview, contentDescription = null, modifier = Modifier.size(12.dp), tint = Color.White)
                                                        Spacer(modifier = Modifier.width(4.dp))
                                                        Text("Review", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.White)
                                                    }
                                                } else {
                                                    OutlinedButton(
                                                        onClick = { /* disabled / already reviewed */ },
                                                        enabled = false,
                                                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                                                        modifier = Modifier.height(30.dp),
                                                        shape = RoundedCornerShape(6.dp)
                                                    ) {
                                                        Text("Done ✓", fontSize = 10.sp)
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
            }
        }
    }
}

// --- SAVINGS GOALS SCREEN ---
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GoalsScreen(
    viewModel: KoboViewModel,
    onCreateGoalClick: () -> Unit
) {
    val context = androidx.compose.ui.platform.LocalContext.current
    var pendingGrantedCallback by remember { mutableStateOf<(() -> Unit)?>(null) }
    
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        onResult = { isGranted ->
            if (isGranted) {
                pendingGrantedCallback?.invoke()
            } else {
                viewModel.triggerUiEvent("Notification permission is required to receive daily savings reminders.")
            }
            pendingGrantedCallback = null
        }
    )
    
    val requestPermissionHelper: (() -> Unit) -> Unit = { onGranted ->
        if (android.os.Build.VERSION.SDK_INT >= 33) {
            val hasPermission = androidx.core.content.ContextCompat.checkSelfPermission(
                context,
                "android.permission.POST_NOTIFICATIONS"
            ) == android.content.pm.PackageManager.PERMISSION_GRANTED
            if (hasPermission) {
                onGranted()
            } else {
                pendingGrantedCallback = onGranted
                permissionLauncher.launch("android.permission.POST_NOTIFICATIONS")
            }
        } else {
            onGranted()
        }
    }

    val goals by viewModel.savingsGoals.collectAsStateWithLifecycle()
    var selectedGoalIdForContribution by remember { mutableStateOf<Int?>(null) }
    var contributeAmountText by remember { mutableStateOf("") }
    var showSaveGoalConfirmDialog by remember { mutableStateOf(false) }

    var selectedGoalForWithdrawalOptions by remember { mutableStateOf<SavingsGoal?>(null) }
    var showWithdrawWalletConfirmDialog by remember { mutableStateOf<SavingsGoal?>(null) }
    var showWithdrawalBankDialogForGoal by remember { mutableStateOf<SavingsGoal?>(null) }
    var showWithdrawBankConfirmDialog by remember { mutableStateOf(false) }
    var goalWithdrawBankName by remember { mutableStateOf("OPay") }
    var bankDropdownExpanded by remember { mutableStateOf(false) }
    var goalWithdrawAccountNumber by remember { mutableStateOf("") }
    var goalWithdrawBankError by remember { mutableStateOf("") }

    var activeSubTab by remember { mutableStateOf("goals") } // "goals" or "calculator"
    var currentStep by remember { mutableStateOf(1) } // step 1 to 4 for Budget Calculator
    var selectedStateForCalc by remember { mutableStateOf("Lagos") }
    val marketItems by viewModel.marketItems.collectAsStateWithLifecycle()
    val basketPreviewState by viewModel.basketPreviewState.collectAsStateWithLifecycle()
    
    // selected items and their quantities: item.id -> quantity
    var basketQuantities by remember { mutableStateOf(mapOf<Int, Int>()) }
    
    // timeline in weeks
    var savingTimelineWeeks by remember { mutableStateOf(4) } // 2, 4, 8, 12
    var savingFrequency by remember { mutableStateOf("Weekly") } // "Weekly" or "Monthly"

    var showPrefilledGoalDialog by remember { mutableStateOf(false) }
    var prefilledTitle by remember { mutableStateOf("") }
    var prefilledTargetAmount by remember { mutableStateOf(0.0) }
    var prefilledCategory by remember { mutableStateOf("Rice") }
    var prefilledAutoSaveAmount by remember { mutableStateOf(500.0) }

    fun applyTemplate(templateType: String) {
        val stateItems = marketItems.filter { it.state.equals(selectedStateForCalc, ignoreCase = true) }
        val finalItems = if (stateItems.isNotEmpty()) stateItems else marketItems
        
        val riceItem = finalItems.find { it.category == "Rice" }
        val beansItem = finalItems.find { it.category == "Beans" }
        val garriItem = finalItems.find { it.category == "Garri" }
        val tomatoesItem = finalItems.find { it.category == "Tomatoes" }
        val yamItem = finalItems.find { it.category == "Yam" }
        val fishItem = finalItems.find { it.category == "Fish" }
        
        val newBasket = mutableMapOf<Int, Int>()
        when (templateType) {
            "single" -> {
                garriItem?.let { newBasket[it.id] = 1 }
                beansItem?.let { newBasket[it.id] = 1 }
                fishItem?.let { newBasket[it.id] = 1 }
            }
            "family" -> {
                riceItem?.let { newBasket[it.id] = 1 }
                garriItem?.let { newBasket[it.id] = 2 }
                beansItem?.let { newBasket[it.id] = 2 }
                tomatoesItem?.let { newBasket[it.id] = 1 }
                yamItem?.let { newBasket[it.id] = 1 }
            }
            "feast" -> {
                riceItem?.let { newBasket[it.id] = 2 }
                yamItem?.let { newBasket[it.id] = 3 }
                tomatoesItem?.let { newBasket[it.id] = 2 }
                fishItem?.let { newBasket[it.id] = 2 }
            }
        }
        basketQuantities = newBasket
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF5F8F6))
            .statusBarsPadding()
    ) {
        // Modern Tab row switch
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 12.dp)
                .background(Color(0xFFE2E8F0), RoundedCornerShape(12.dp))
                .padding(4.dp)
        ) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(10.dp))
                    .clickable { activeSubTab = "goals" }
                    .background(if (activeSubTab == "goals") Color.White else Color.Transparent)
                    .padding(vertical = 10.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "My Goals",
                    fontWeight = FontWeight.Bold,
                    color = if (activeSubTab == "goals") Color(0xFF0A8F3D) else Color(0xFF64748B),
                    fontSize = 14.sp
                )
            }
            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(10.dp))
                    .clickable { activeSubTab = "calculator" }
                    .background(if (activeSubTab == "calculator") Color.White else Color.Transparent)
                    .padding(vertical = 10.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Budget Calculator",
                    fontWeight = FontWeight.Bold,
                    color = if (activeSubTab == "calculator") Color(0xFF0A8F3D) else Color(0xFF64748B),
                    fontSize = 14.sp
                )
            }
        }

        if (activeSubTab == "goals") {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text("Food Savings Goals", fontSize = 22.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1E293B))
                        Text("Save daily or weekly towards wholesale food baskets", fontSize = 13.sp, color = Color(0xFF64748B))
                    }
                    IconButton(
                        onClick = onCreateGoalClick,
                        modifier = Modifier
                            .background(Color(0xFF0A8F3D), CircleShape)
                            .size(44.dp)
                            .testTag("create_goal_fab")
                    ) {
                        Icon(Icons.Default.Add, contentDescription = "Add Goal", tint = Color.White)
                    }
                }
            }

            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                item {
                    SavingsReminderCard(
                        viewModel = viewModel,
                        onRequestPermission = requestPermissionHelper
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                }

                if (goals.isEmpty()) {
                    item {
                        EmptyStateCard(
                            message = "Start Your First Food Goal",
                            subMessage = "Create goals for bulk groceries. Every daily deposit helps you beat food inflation and unlocks huge harvest discounts!",
                            btnText = "Create Savings Goal",
                            onClick = onCreateGoalClick,
                            icon = Icons.Default.Savings,
                            iconColor = PrimaryGreen
                        )
                    }
                } else {
                    items(goals) { goal ->
                        GoalCard(
                            goal = goal,
                            onContributeClick = { selectedGoalIdForContribution = goal.id },
                            onBuyNowClick = { viewModel.buyFoodWithCompletedGoal(goal.id) },
                            onPauseToggle = { viewModel.pauseResumeGoal(goal.id) },
                            onWithdrawClick = { selectedGoalForWithdrawalOptions = goal },
                            onDeleteClick = { viewModel.deleteGoal(goal.id) },
                            onLockToggle = { viewModel.toggleGoalLock(goal.id) }
                        )
                    }
                }
                item {
                    Spacer(modifier = Modifier.height(16.dp))
                }
            }
        } else {
            // BUDGET CALCULATOR SUB-SCREEN (Redesigned Step-by-Step wizard for enhanced clarity & accessibility)
            Column(
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp)
            ) {
                // Introduction Card
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFF0FDF4)),
                    border = BorderStroke(1.dp, Color(0xFFDCFCE7)),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Info,
                            contentDescription = null,
                            tint = Color(0xFF16A34A),
                            modifier = Modifier.size(28.dp)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = "Smart Cost Estimator",
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp,
                                color = Color(0xFF14532D)
                            )
                            Text(
                                text = "Plan your food expenses and build a personalized savings blueprint based on real local market prices.",
                                fontSize = 12.sp,
                                color = Color(0xFF166534)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Beautiful, modern step progress indicator for intuitive wizard flow
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 4.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    for (step in 1..4) {
                        val isActive = step == currentStep
                        val isCompleted = step < currentStep
                        Column(
                            modifier = Modifier.weight(1f),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(6.dp)
                                    .clip(RoundedCornerShape(3.dp))
                                    .background(
                                        when {
                                            isActive -> Color(0xFF0A8F3D)
                                            isCompleted -> Color(0xFF0A8F3D).copy(alpha = 0.5f)
                                            else -> Color(0xFFE2E8F0)
                                        }
                                    )
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = when (step) {
                                    1 -> "1. State"
                                    2 -> "2. Food"
                                    3 -> "3. Plan"
                                    else -> "4. Review"
                                },
                                fontSize = 11.sp,
                                fontWeight = if (isActive) FontWeight.Bold else FontWeight.Medium,
                                color = if (isActive) Color(0xFF0A8F3D) else Color(0xFF64748B)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Render current step in the guided workflow
                when (currentStep) {
                    1 -> {
                        // STEP 1: DELIVERY LOCATION
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = Color.White),
                            border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
                            shape = RoundedCornerShape(16.dp)
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.LocationOn, contentDescription = null, tint = Color(0xFF0A8F3D), modifier = Modifier.size(24.dp))
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("Where is your food delivered?", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = Color(0xFF1E293B))
                                }
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = "Prices vary slightly across regions. Selecting your location guarantees accurate current market rates.",
                                    fontSize = 13.sp,
                                    color = Color(0xFF64748B)
                                )
                                Spacer(modifier = Modifier.height(16.dp))

                                val marketStates = listOf("Lagos", "Ondo", "Oyo", "Kano", "Abuja")
                                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                                    marketStates.forEach { stateOption ->
                                        val isSelected = selectedStateForCalc == stateOption
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .clip(RoundedCornerShape(12.dp))
                                                .background(if (isSelected) Color(0xFFE8F5E9) else Color(0xFFF8FAFC))
                                                .border(
                                                    width = if (isSelected) 2.dp else 1.dp,
                                                    color = if (isSelected) Color(0xFF0A8F3D) else Color(0xFFE2E8F0),
                                                    shape = RoundedCornerShape(12.dp)
                                                )
                                                .clickable {
                                                    selectedStateForCalc = stateOption
                                                    // Reset basket quantities on state change to avoid mismatching items across states
                                                    basketQuantities = emptyMap()
                                                }
                                                .padding(14.dp),
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.SpaceBetween
                                        ) {
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Box(
                                                    modifier = Modifier
                                                        .size(36.dp)
                                                        .background(if (isSelected) Color(0xFF0A8F3D).copy(alpha = 0.15f) else Color(0xFFF1F5F9), CircleShape),
                                                    contentAlignment = Alignment.Center
                                                ) {
                                                    Icon(
                                                        imageVector = Icons.Default.LocationOn,
                                                        contentDescription = null,
                                                        tint = if (isSelected) Color(0xFF0A8F3D) else Color(0xFF64748B),
                                                        modifier = Modifier.size(18.dp)
                                                    )
                                                }
                                                Spacer(modifier = Modifier.width(12.dp))
                                                Column {
                                                    Text(
                                                        text = stateOption,
                                                        fontWeight = FontWeight.Bold,
                                                        fontSize = 15.sp,
                                                        color = Color(0xFF1E293B)
                                                    )
                                                    Text(
                                                        text = "Load active merchant prices in $stateOption",
                                                        fontSize = 11.sp,
                                                        color = Color(0xFF64748B)
                                                    )
                                                }
                                            }
                                            RadioButton(
                                                selected = isSelected,
                                                onClick = {
                                                    selectedStateForCalc = stateOption
                                                    basketQuantities = emptyMap()
                                                },
                                                colors = RadioButtonDefaults.colors(selectedColor = Color(0xFF0A8F3D))
                                            )
                                        }
                                    }
                                }
                                
                                Spacer(modifier = Modifier.height(24.dp))
                                Button(
                                    onClick = { currentStep = 2 },
                                    modifier = Modifier.fillMaxWidth().height(48.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0A8F3D)),
                                    shape = RoundedCornerShape(10.dp)
                                ) {
                                    Text("Next: Select Food Items ➡️", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                }
                            }
                        }
                    }
                    2 -> {
                        // STEP 2: CHOOSE FOOD ITEMS & TEMPLATES
                        Column {
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF7ED)),
                                border = BorderStroke(1.dp, Color(0xFFFFEDD5)),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Column(modifier = Modifier.padding(14.dp)) {
                                    Text(
                                        text = "💡 Quick Start: Choose a Basket Template",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 13.sp,
                                        color = Color(0xFFC2410C)
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = "Pre-fill standard basket quantities for your needs in one click, or select custom foods below.",
                                        fontSize = 11.sp,
                                        color = Color(0xFF7C2D12)
                                    )
                                    Spacer(modifier = Modifier.height(12.dp))
                                    Row(
                                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        listOf(
                                            "single" to "Single Basket 🎓",
                                            "family" to "Family Basket 👨‍👩‍👧",
                                            "feast" to "Feast Basket 👑"
                                        ).forEach { (key, label) ->
                                            Box(
                                                modifier = Modifier
                                                    .weight(1f)
                                                    .clip(RoundedCornerShape(8.dp))
                                                    .background(Color.White)
                                                    .border(1.dp, Color(0xFFFFD1A9), RoundedCornerShape(8.dp))
                                                    .clickable { 
                                                        applyTemplate(key)
                                                        // Informative toast of prefilled basket
                                                        android.widget.Toast.makeText(context, "Applied $label template!", android.widget.Toast.LENGTH_SHORT).show()
                                                    }
                                                    .padding(vertical = 10.dp),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Text(
                                                    text = label,
                                                    fontSize = 11.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = Color(0xFFC2410C)
                                                )
                                            }
                                        }
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(16.dp))

                            Text(
                                text = "What groceries do you need?",
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp,
                                color = Color(0xFF1E293B)
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Tap individual quantities below. Live local rates are configured for $selectedStateForCalc.",
                                fontSize = 12.sp,
                                color = Color(0xFF64748B)
                            )
                            Spacer(modifier = Modifier.height(12.dp))

                            val filteredItems = marketItems.filter { it.state.equals(selectedStateForCalc, ignoreCase = true) }
                            val itemsToDisplay = if (filteredItems.isNotEmpty()) filteredItems else marketItems

                            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                                itemsToDisplay.forEach { item ->
                                    val qty = basketQuantities[item.id] ?: 0
                                    Card(
                                        modifier = Modifier.fillMaxWidth(),
                                        colors = CardDefaults.cardColors(containerColor = Color.White),
                                        border = BorderStroke(1.dp, if (qty > 0) Color(0xFF0A8F3D).copy(alpha = 0.5f) else Color(0xFFE2E8F0)),
                                        shape = RoundedCornerShape(12.dp)
                                    ) {
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(14.dp),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Column(modifier = Modifier.weight(1f)) {
                                                Row(verticalAlignment = Alignment.CenterVertically) {
                                                    Box(
                                                        modifier = Modifier
                                                            .size(32.dp)
                                                            .background(Color(0xFFF1F5F9), CircleShape),
                                                        contentAlignment = Alignment.Center
                                                    ) {
                                                        Text(getCategoryEmoji(item.category), fontSize = 16.sp)
                                                    }
                                                    Spacer(modifier = Modifier.width(8.dp))
                                                    Text(
                                                        text = item.name,
                                                        fontWeight = FontWeight.Bold,
                                                        fontSize = 14.sp,
                                                        color = Color(0xFF1E293B)
                                                    )
                                                }
                                                Spacer(modifier = Modifier.height(4.dp))
                                                Text(
                                                    text = "₦${String.format("%,.2f", item.price)} • ${item.category}",
                                                    fontSize = 12.sp,
                                                    color = Color(0xFF64748B)
                                                )
                                            }

                                            Row(
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                                            ) {
                                                Box(
                                                    modifier = Modifier
                                                        .size(40.dp)
                                                        .clip(RoundedCornerShape(8.dp))
                                                        .background(Color(0xFFF1F5F9))
                                                        .clickable {
                                                            if (qty > 0) {
                                                                basketQuantities = basketQuantities.toMutableMap().apply {
                                                                    if (qty == 1) remove(item.id) else put(item.id, qty - 1)
                                                                }
                                                            }
                                                        },
                                                    contentAlignment = Alignment.Center
                                                ) {
                                                    Icon(
                                                        imageVector = Icons.Default.Remove,
                                                        contentDescription = "Reduce Quantity",
                                                        modifier = Modifier.size(18.dp),
                                                        tint = Color(0xFF475569)
                                                    )
                                                }

                                                Text(
                                                    text = qty.toString(),
                                                    fontWeight = FontWeight.Black,
                                                    fontSize = 16.sp,
                                                    color = Color(0xFF1E293B)
                                                )

                                                Box(
                                                    modifier = Modifier
                                                        .size(40.dp)
                                                        .clip(RoundedCornerShape(8.dp))
                                                        .background(Color(0xFFE8F5E9))
                                                        .clickable {
                                                            basketQuantities = basketQuantities.toMutableMap().apply {
                                                                put(item.id, qty + 1)
                                                            }
                                                        },
                                                    contentAlignment = Alignment.Center
                                                ) {
                                                    Icon(
                                                        imageVector = Icons.Default.Add,
                                                        contentDescription = "Increase Quantity",
                                                        modifier = Modifier.size(18.dp),
                                                        tint = Color(0xFF0A8F3D)
                                                    )
                                                }
                                            }
                                        }
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(16.dp))

                            val currentTotal = itemsToDisplay.sumOf { (basketQuantities[it.id] ?: 0) * it.price }
                            val totalItemsSelected = basketQuantities.values.sum()

                            if (totalItemsSelected > 0) {
                                Card(
                                    colors = CardDefaults.cardColors(containerColor = Color(0xFFE8F5E9)),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Row(
                                        modifier = Modifier.padding(14.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text(
                                            text = "Selected total: ₦${String.format("%,.0f", currentTotal)} ($totalItemsSelected food items)",
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 13.sp,
                                            color = Color(0xFF1B5E20)
                                        )
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(24.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                OutlinedButton(
                                    onClick = { currentStep = 1 },
                                    modifier = Modifier.weight(1f).height(48.dp),
                                    shape = RoundedCornerShape(10.dp),
                                    border = BorderStroke(1.dp, Color(0xFF64748B))
                                ) {
                                    Text("⬅️ Back", fontWeight = FontWeight.Bold, color = Color(0xFF475569))
                                }
                                Button(
                                    onClick = { currentStep = 3 },
                                    enabled = totalItemsSelected > 0,
                                    modifier = Modifier.weight(1.5f).height(48.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0A8F3D)),
                                    shape = RoundedCornerShape(10.dp)
                                ) {
                                    Text("Next: Savings Plan ➡️", fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                    3 -> {
                        // STEP 3: SAVINGS PLAN TIMELINE & FREQUENCY
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = Color.White),
                            border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
                            shape = RoundedCornerShape(16.dp)
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.DateRange, contentDescription = null, tint = Color(0xFF0A8F3D), modifier = Modifier.size(24.dp))
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("Choose your savings duration", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = Color(0xFF1E293B))
                                }
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = "Select how long you wish to save. Spreading deposits over a longer duration lowers each individual transfer amount.",
                                    fontSize = 13.sp,
                                    color = Color(0xFF64748B)
                                )
                                Spacer(modifier = Modifier.height(16.dp))

                                Text("I want to save for:", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1E293B))
                                Spacer(modifier = Modifier.height(8.dp))

                                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                    listOf(
                                        2 to "2 Weeks" to "Best for immediate short-term food storage needs.",
                                        4 to "4 Weeks (1 Month)" to "Standard family monthly grocery plan.",
                                        8 to "8 Weeks (2 Months)" to "Build reserves comfortably over time.",
                                        12 to "12 Weeks (3 Months)" to "Minimal daily burden, perfect for long-term planning."
                                    ).forEach { (pair, desc) ->
                                        val weeks = pair.first
                                        val label = pair.second
                                        val isSelected = savingTimelineWeeks == weeks
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .clip(RoundedCornerShape(10.dp))
                                                .background(if (isSelected) Color(0xFFE8F5E9) else Color(0xFFF8FAFC))
                                                .border(1.dp, if (isSelected) Color(0xFF0A8F3D) else Color(0xFFE2E8F0), RoundedCornerShape(10.dp))
                                                .clickable { savingTimelineWeeks = weeks }
                                                .padding(12.dp),
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.SpaceBetween
                                        ) {
                                            Column(modifier = Modifier.weight(1f)) {
                                                Text(label, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = Color(0xFF1E293B))
                                                Text(desc, fontSize = 11.sp, color = Color(0xFF64748B))
                                            }
                                            RadioButton(
                                                selected = isSelected,
                                                onClick = { savingTimelineWeeks = weeks },
                                                colors = RadioButtonDefaults.colors(selectedColor = Color(0xFF0A8F3D))
                                            )
                                        }
                                    }
                                }

                                Spacer(modifier = Modifier.height(20.dp))
                                Text("Savings Frequency:", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1E293B))
                                Spacer(modifier = Modifier.height(8.dp))

                                Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
                                    listOf(
                                        "Weekly" to "Once a week",
                                        "Monthly" to "Once a month"
                                    ).forEach { (freq, desc) ->
                                        val isSelected = savingFrequency == freq
                                        Row(
                                            modifier = Modifier
                                                .weight(1f)
                                                .clip(RoundedCornerShape(10.dp))
                                                .background(if (isSelected) Color(0xFFE8F5E9) else Color(0xFFF8FAFC))
                                                .border(1.dp, if (isSelected) Color(0xFF0A8F3D) else Color(0xFFE2E8F0), RoundedCornerShape(10.dp))
                                                .clickable { savingFrequency = freq }
                                                .padding(12.dp),
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.SpaceBetween
                                        ) {
                                            Column(modifier = Modifier.weight(1f)) {
                                                Text(freq, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = Color(0xFF1E293B))
                                                Text(desc, fontSize = 11.sp, color = Color(0xFF64748B))
                                            }
                                            RadioButton(
                                                selected = isSelected,
                                                onClick = { savingFrequency = freq },
                                                colors = RadioButtonDefaults.colors(selectedColor = Color(0xFF0A8F3D))
                                            )
                                        }
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(24.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            OutlinedButton(
                                onClick = { currentStep = 2 },
                                modifier = Modifier.weight(1f).height(48.dp),
                                shape = RoundedCornerShape(10.dp),
                                border = BorderStroke(1.dp, Color(0xFF64748B))
                            ) {
                                Text("⬅️ Back", fontWeight = FontWeight.Bold, color = Color(0xFF475569))
                            }
                            Button(
                                onClick = { currentStep = 4 },
                                modifier = Modifier.weight(1.5f).height(48.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0A8F3D)),
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Text("View Summary ➡️", fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                    else -> {
                        // STEP 4: SUMMARY & AI REVIEW & CONVERT TO savings goal
                        val filteredItemsSummary = marketItems.filter { it.state.equals(selectedStateForCalc, ignoreCase = true) }
                        val itemsToDisplaySummary = if (filteredItemsSummary.isNotEmpty()) filteredItemsSummary else marketItems
                        val totalCostSummary = itemsToDisplaySummary.sumOf { item ->
                            (basketQuantities[item.id] ?: 0) * item.price
                        }
                        val basketItemsWithQtySummary = itemsToDisplaySummary.mapNotNull { item ->
                            val qty = basketQuantities[item.id] ?: 0
                            if (qty > 0) Pair(item, qty) else null
                        }
                        val totalItemsCountSummary = basketQuantities.values.sum()
                        val previewImageRes = if (totalItemsCountSummary <= 3) {
                            R.drawable.img_custom_basket_single
                        } else if (totalItemsCountSummary <= 8) {
                            R.drawable.img_custom_basket_family
                        } else {
                            R.drawable.img_custom_basket_feast
                        }

                        val periods = if (savingFrequency == "Weekly") {
                            savingTimelineWeeks
                        } else {
                            Math.max(1, savingTimelineWeeks / 4)
                        }
                        val savingPerPeriod = if (periods > 0) totalCostSummary / periods else totalCostSummary

                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = Color.White),
                            border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
                            shape = RoundedCornerShape(16.dp)
                        ) {
                            Column {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(160.dp)
                                ) {
                                    Image(
                                        painter = painterResource(id = previewImageRes),
                                        contentDescription = "Custom Food Basket Preview",
                                        modifier = Modifier.fillMaxSize(),
                                        contentScale = ContentScale.Crop
                                    )
                                    Box(
                                        modifier = Modifier
                                            .fillMaxSize()
                                            .background(
                                                Brush.verticalGradient(
                                                    colors = listOf(Color.Black.copy(alpha = 0.1f), Color.Black.copy(alpha = 0.7f))
                                                )
                                            )
                                    )
                                    Column(
                                        modifier = Modifier
                                            .align(Alignment.BottomStart)
                                            .padding(16.dp)
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .background(Color(0xFF0A8F3D), RoundedCornerShape(4.dp))
                                                .padding(horizontal = 6.dp, vertical = 2.dp)
                                        ) {
                                            Text(
                                                text = "VISUAL BUNDLE PREVIEW",
                                                fontSize = 9.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = Color.White
                                            )
                                        }
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text(
                                            text = if (totalItemsCountSummary <= 3) "Single Starter Basket" else if (totalItemsCountSummary <= 8) "Standard Family Food Basket" else "Ultimate Abundance Feast Basket",
                                            fontWeight = FontWeight.ExtraBold,
                                            fontSize = 16.sp,
                                            color = Color.White
                                        )
                                        Text(
                                            text = "$totalItemsCountSummary food item(s) selected in $selectedStateForCalc",
                                            fontSize = 12.sp,
                                            color = Color.White.copy(alpha = 0.9f)
                                        )
                                    }
                                }

                                Column(modifier = Modifier.padding(16.dp)) {
                                    // Recommended Periodic Savings display
                                    Card(
                                        modifier = Modifier.fillMaxWidth(),
                                        colors = CardDefaults.cardColors(containerColor = Color(0xFF0A8F3D)),
                                        shape = RoundedCornerShape(12.dp)
                                    ) {
                                        Column(
                                            modifier = Modifier.padding(16.dp),
                                            horizontalAlignment = Alignment.CenterHorizontally
                                        ) {
                                            Text(
                                                text = "ESTIMATED TOTAL BUDGET",
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = Color.White.copy(alpha = 0.8f)
                                            )
                                            Spacer(modifier = Modifier.height(4.dp))
                                            Text(
                                                text = "₦${String.format("%,.2f", totalCostSummary)}",
                                                fontSize = 24.sp,
                                                fontWeight = FontWeight.Black,
                                                color = Color.White
                                            )
                                            
                                            Spacer(modifier = Modifier.height(12.dp))
                                            Divider(color = Color.White.copy(alpha = 0.2f))
                                            Spacer(modifier = Modifier.height(12.dp))

                                            Text(
                                                text = "RECOMMENDED SAVINGS PLAN",
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = Color.White.copy(alpha = 0.8f)
                                            )
                                            Spacer(modifier = Modifier.height(4.dp))
                                            Row(
                                                verticalAlignment = Alignment.Bottom,
                                                horizontalArrangement = Arrangement.Center
                                            ) {
                                                Text(
                                                    text = "₦${String.format("%,.0f", savingPerPeriod)}",
                                                    fontSize = 28.sp,
                                                    fontWeight = FontWeight.Black,
                                                    color = Color(0xFFFFD700) // Beautiful Gold Accent
                                                )
                                                Text(
                                                    text = if (savingFrequency == "Weekly") " / week" else " / month",
                                                    fontSize = 14.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = Color.White.copy(alpha = 0.9f),
                                                    modifier = Modifier.padding(bottom = 4.dp)
                                                )
                                            }
                                            Text(
                                                text = "for a duration of $savingTimelineWeeks Weeks",
                                                fontSize = 12.sp,
                                                color = Color.White.copy(alpha = 0.9f)
                                            )
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(16.dp))

                                    // MAMA AI BUNDLE REVIEW
                                    when (val previewState = basketPreviewState) {
                                        is BasketPreviewState.Idle -> {
                                            Button(
                                                onClick = {
                                                    viewModel.generateBasketPreview(basketItemsWithQtySummary)
                                                },
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .testTag("ai_preview_generate_button"),
                                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1E293B)),
                                                shape = RoundedCornerShape(10.dp)
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Default.Star,
                                                    contentDescription = null,
                                                    modifier = Modifier.size(16.dp),
                                                    tint = Color(0xFFFFD700)
                                                )
                                                Spacer(modifier = Modifier.width(8.dp))
                                                Text(
                                                    text = "Ask Mama AI to Review Your Basket",
                                                    fontWeight = FontWeight.Bold,
                                                    fontSize = 13.sp
                                                )
                                            }
                                        }
                                        is BasketPreviewState.Loading -> {
                                            Column(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .padding(vertical = 12.dp),
                                                horizontalAlignment = Alignment.CenterHorizontally
                                            ) {
                                                CircularProgressIndicator(
                                                    color = Color(0xFF0A8F3D),
                                                    modifier = Modifier.size(28.dp),
                                                    strokeWidth = 3.dp
                                                )
                                                Spacer(modifier = Modifier.height(10.dp))
                                                Text(
                                                    text = "Mama Olufunke is arranging your kitchen basket...",
                                                    fontSize = 12.sp,
                                                    color = Color(0xFF0A8F3D),
                                                    fontWeight = FontWeight.Medium
                                                )
                                            }
                                        }
                                        is BasketPreviewState.Success -> {
                                            Card(
                                                colors = CardDefaults.cardColors(containerColor = Color(0xFFF8FAFC)),
                                                border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
                                                shape = RoundedCornerShape(12.dp),
                                                modifier = Modifier.fillMaxWidth()
                                            ) {
                                                Column(modifier = Modifier.padding(12.dp)) {
                                                    Row(
                                                        verticalAlignment = Alignment.CenterVertically,
                                                        horizontalArrangement = Arrangement.SpaceBetween,
                                                        modifier = Modifier.fillMaxWidth()
                                                    ) {
                                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                                            Icon(
                                                                imageVector = Icons.Default.Star,
                                                                contentDescription = null,
                                                                tint = Color(0xFFFFB300),
                                                                modifier = Modifier.size(16.dp)
                                                            )
                                                            Spacer(modifier = Modifier.width(6.dp))
                                                            Text(
                                                                text = "MAMA OLUFUNKE'S REVIEW",
                                                                fontSize = 11.sp,
                                                                fontWeight = FontWeight.Black,
                                                                color = Color(0xFF1E293B)
                                                            )
                                                        }
                                                        
                                                        TextButton(
                                                            onClick = { viewModel.resetBasketPreview() },
                                                            contentPadding = PaddingValues(0.dp),
                                                            modifier = Modifier.height(24.dp)
                                                        ) {
                                                            Text("Clear Review", fontSize = 11.sp, color = Color(0xFFEF4444), fontWeight = FontWeight.Bold)
                                                        }
                                                    }
                                                    Spacer(modifier = Modifier.height(8.dp))
                                                    Text(
                                                        text = previewState.description,
                                                        fontSize = 12.sp,
                                                        color = Color(0xFF334155),
                                                        lineHeight = 18.sp
                                                    )
                                                }
                                            }
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(20.dp))

                                    // Convert to live goal Button
                                    Button(
                                        onClick = {
                                            if (totalCostSummary > 0) {
                                                prefilledTitle = "$selectedStateForCalc ${savingFrequency} Basket"
                                                prefilledTargetAmount = totalCostSummary
                                                val firstItem = itemsToDisplaySummary.find { (basketQuantities[it.id] ?: 0) > 0 }
                                                prefilledCategory = firstItem?.category ?: "Rice"
                                                prefilledAutoSaveAmount = if (savingFrequency == "Weekly") {
                                                    (savingPerPeriod / 7.0).coerceAtLeast(100.0)
                                                } else {
                                                    (savingPerPeriod / 30.0).coerceAtLeast(100.0)
                                                }
                                                showPrefilledGoalDialog = true
                                            }
                                        },
                                        modifier = Modifier.fillMaxWidth().height(52.dp),
                                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0A8F3D)),
                                        shape = RoundedCornerShape(10.dp)
                                    ) {
                                        Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(18.dp))
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text("Turn this into a Savings Goal 🎯", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(24.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            OutlinedButton(
                                onClick = { currentStep = 3 },
                                modifier = Modifier.weight(1f).height(48.dp),
                                shape = RoundedCornerShape(10.dp),
                                border = BorderStroke(1.dp, Color(0xFF64748B))
                            ) {
                                Text("⬅️ Back", fontWeight = FontWeight.Bold, color = Color(0xFF475569))
                            }
                            OutlinedButton(
                                onClick = {
                                    basketQuantities = emptyMap()
                                    currentStep = 1
                                    viewModel.resetBasketPreview()
                                },
                                modifier = Modifier.weight(1f).height(48.dp),
                                shape = RoundedCornerShape(10.dp),
                                colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFFEF4444)),
                                border = BorderStroke(1.dp, Color(0xFFEF4444))
                            ) {
                                Text("Reset 🔄", fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }
    }

    if (showPrefilledGoalDialog) {
        PreFilledGoalDialog(
            initialTitle = prefilledTitle,
            initialTargetAmount = prefilledTargetAmount,
            initialCategory = prefilledCategory,
            initialAutoSaveAmount = prefilledAutoSaveAmount,
            onDismiss = { showPrefilledGoalDialog = false },
            onConfirm = { title, target, category, autoSaveAmt, autoSaveEnabled ->
                viewModel.createGoal(title, target, category, autoSaveAmt, autoSaveEnabled)
                showPrefilledGoalDialog = false
                activeSubTab = "goals" // switch back to My Goals so they see the newly created goal! Incredibly satisfying!
            }
        )
    }

    selectedGoalIdForContribution?.let { goalId ->
        val goal = goals.find { it.id == goalId } ?: return@let
        AlertDialog(
            onDismissRequest = { selectedGoalIdForContribution = null },
            title = { Text("Save Towards: ${goal.title}") },
            text = {
                Column {
                    Text("How much would you like to save right now from your available balance?", fontSize = 14.sp)
                    Spacer(modifier = Modifier.height(16.dp))
                    OutlinedTextField(
                        value = contributeAmountText,
                        onValueChange = { contributeAmountText = it },
                        label = { Text("Savings Amount (₦)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val amt = contributeAmountText.toDoubleOrNull() ?: 0.0
                        if (amt > 0) {
                            showSaveGoalConfirmDialog = true
                        } else {
                            selectedGoalIdForContribution = null
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0A8F3D))
                ) {
                    Text("Confirm Save")
                }
            },
            dismissButton = {
                TextButton(onClick = { selectedGoalIdForContribution = null }) {
                    Text("Cancel")
                }
            }
        )
    }

    if (showSaveGoalConfirmDialog && selectedGoalIdForContribution != null) {
        val goalId = selectedGoalIdForContribution!!
        val goal = goals.find { it.id == goalId }
        if (goal != null) {
            val amt = contributeAmountText.toDoubleOrNull() ?: 0.0
            AlertDialog(
                onDismissRequest = { showSaveGoalConfirmDialog = false },
                title = { Text("Confirm Transfer") },
                text = {
                    Text("Are you sure you want to deposit/transfer ₦${String.format("%,.2f", amt)} into your savings goal \"${goal.title}\"?")
                },
                confirmButton = {
                    Button(
                        onClick = {
                            viewModel.saveToGoal(goalId, amt)
                            contributeAmountText = ""
                            selectedGoalIdForContribution = null
                            showSaveGoalConfirmDialog = false
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0A8F3D)),
                        modifier = Modifier.testTag("confirm_transfer_button")
                    ) {
                        Text("Confirm Transfer")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showSaveGoalConfirmDialog = false }) {
                        Text("Cancel")
                    }
                }
            )
        }
    }

    selectedGoalForWithdrawalOptions?.let { goal ->
        if (goal.isLocked) {
            AlertDialog(
                onDismissRequest = { selectedGoalForWithdrawalOptions = null },
                icon = { Icon(Icons.Default.Lock, contentDescription = null, tint = Color(0xFFFF8C00), modifier = Modifier.size(36.dp)) },
                title = { Text("Goal is Locked", fontWeight = FontWeight.Bold, color = Color(0xFF1E293B)) },
                text = {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Text(
                            text = "This savings goal is currently locked to secure your food funds and help you stay on budget.",
                            fontSize = 13.sp,
                            color = Color(0xFF64748B)
                        )
                        Text(
                            text = "To withdraw these funds back to your Available to Spend wallet or Bank, you must unlock this goal first.",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium,
                            color = Color(0xFF1E293B)
                        )
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            viewModel.unlockGoal(goal.id)
                            selectedGoalForWithdrawalOptions = null
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0A8F3D))
                    ) {
                        Text("Unlock Goal", color = Color.White)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { selectedGoalForWithdrawalOptions = null }) {
                        Text("Cancel", color = Color(0xFF64748B))
                    }
                }
            )
        } else {
            val isCompleted = goal.savedAmount >= goal.targetAmount
            AlertDialog(
                onDismissRequest = { selectedGoalForWithdrawalOptions = null },
            icon = { Icon(Icons.Default.AccountBalanceWallet, contentDescription = null, tint = Color(0xFF0A8F3D)) },
            title = { Text("Withdraw savings for ${goal.title}", fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        text = "You have ₦${String.format("%,.0f", goal.savedAmount)} saved in this goal. Where would you like to withdraw these funds?",
                        fontSize = 13.sp,
                        color = Color(0xFF64748B)
                    )
                    
                    if (!isCompleted) {
                        Card(
                            colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF7ED)),
                            border = BorderStroke(1.dp, Color(0xFFFFEDD5)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = "⚠️ This goal is incomplete. Emergency early withdrawal will incur a 5% penalty fee (₦${String.format("%,.2f", goal.savedAmount * 0.05)}) on the refunded amount.",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFFC2410C),
                                modifier = Modifier.padding(8.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                showWithdrawWalletConfirmDialog = goal
                                selectedGoalForWithdrawalOptions = null
                            },
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFF0FDF4)),
                        border = BorderStroke(1.dp, Color(0xFFDCFCE7))
                    ) {
                        Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.AccountBalanceWallet, contentDescription = null, tint = Color(0xFF16A34A), modifier = Modifier.size(24.dp))
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text("Transfer to AfriSav Wallet", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = Color(0xFF14532D))
                                Text("Move funds to available balance", fontSize = 11.sp, color = Color(0xFF166534))
                            }
                        }
                    }

                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                showWithdrawalBankDialogForGoal = goal
                                selectedGoalForWithdrawalOptions = null
                            },
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFEFF6FF)),
                        border = BorderStroke(1.dp, Color(0xFFDBEAFE))
                    ) {
                        Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.AccountBalance, contentDescription = null, tint = Color(0xFF2563EB), modifier = Modifier.size(24.dp))
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text("Direct Cashout to Bank", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = Color(0xFF1E3A8A))
                                Text("Settle directly to commercial bank", fontSize = 11.sp, color = Color(0xFF1E40AF))
                            }
                        }
                    }
                }
            },
            confirmButton = {},
            dismissButton = {
                TextButton(onClick = { selectedGoalForWithdrawalOptions = null }) {
                    Text("Cancel", color = Color(0xFF64748B))
                }
            }
        )
        }
    }

    showWithdrawalBankDialogForGoal?.let { goal ->
        AlertDialog(
            onDismissRequest = { 
                showWithdrawalBankDialogForGoal = null
                goalWithdrawAccountNumber = ""
                goalWithdrawBankError = ""
            },
            icon = { Icon(Icons.Default.AccountBalance, contentDescription = null, tint = Color(0xFF2563EB)) },
            title = { Text("Bank Cashout Details", fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        text = "Withdraw ₦${String.format("%,.0f", goal.savedAmount)} from \"${goal.title}\" directly to your bank account.",
                        fontSize = 13.sp,
                        color = Color(0xFF64748B)
                    )

                    Text("Select Destination Bank", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFF64748B))
                    val withdrawBanks = listOf(
                        "Access Bank",
                        "Zenith Bank",
                        "Guaranty Trust Bank (GTBank)",
                        "United Bank for Africa (UBA)",
                        "First Bank of Nigeria",
                        "Fidelity Bank",
                        "Union Bank of Nigeria",
                        "Stanbic IBTC Bank",
                        "Sterling Bank",
                        "Wema Bank",
                        "Ecobank Nigeria",
                        "Keystone Bank",
                        "Polaris Bank",
                        "Moniepoint MFB",
                        "OPay",
                        "PalmPay",
                        "Kuda Bank",
                        "VFD Microfinance Bank"
                    )
                    ExposedDropdownMenuBox(
                        expanded = bankDropdownExpanded,
                        onExpandedChange = { bankDropdownExpanded = !bankDropdownExpanded }
                    ) {
                        OutlinedTextField(
                            value = goalWithdrawBankName,
                            onValueChange = {},
                            readOnly = true,
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = bankDropdownExpanded) },
                            modifier = Modifier
                                .menuAnchor()
                                .fillMaxWidth(),
                            shape = RoundedCornerShape(8.dp),
                            textStyle = TextStyle(fontSize = 13.sp, fontWeight = FontWeight.Medium),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedContainerColor = Color.White,
                                unfocusedContainerColor = Color.White,
                                focusedBorderColor = Color(0xFF2563EB),
                                unfocusedBorderColor = Color(0xFFCBD5E1)
                            )
                        )
                        ExposedDropdownMenu(
                            expanded = bankDropdownExpanded,
                            onDismissRequest = { bankDropdownExpanded = false }
                        ) {
                            withdrawBanks.forEach { bank ->
                                DropdownMenuItem(
                                    text = { Text(bank, fontSize = 13.sp) },
                                    onClick = {
                                        goalWithdrawBankName = bank
                                        bankDropdownExpanded = false
                                    }
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    OutlinedTextField(
                        value = goalWithdrawAccountNumber,
                        onValueChange = {
                            if (it.length <= 10 && it.all { char -> char.isDigit() }) {
                                goalWithdrawAccountNumber = it
                                goalWithdrawBankError = ""
                            }
                        },
                        label = { Text("10-Digit Account Number") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    if (goalWithdrawBankError.isNotEmpty()) {
                        Text(goalWithdrawBankError, color = Color.Red, fontSize = 11.sp)
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (goalWithdrawAccountNumber.length != 10) {
                            goalWithdrawBankError = "Account number must be exactly 10 digits"
                        } else {
                            showWithdrawBankConfirmDialog = true
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2563EB)),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text("Request Bank Cashout", fontWeight = FontWeight.Bold, color = Color.White)
                }
            },
            dismissButton = {
                TextButton(onClick = { 
                    showWithdrawalBankDialogForGoal = null
                    goalWithdrawAccountNumber = ""
                    goalWithdrawBankError = ""
                }) {
                    Text("Cancel", color = Color(0xFF64748B))
                }
            }
        )
    }

    showWithdrawWalletConfirmDialog?.let { goal ->
        AlertDialog(
            onDismissRequest = { showWithdrawWalletConfirmDialog = null },
            title = { Text("Confirm Transfer") },
            text = {
                Text("Are you sure you want to withdraw/transfer ₦${String.format("%,.2f", goal.savedAmount)} from your savings goal \"${goal.title}\" back to your available spendable wallet balance?")
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.withdrawGoalFunds(goal.id)
                        showWithdrawWalletConfirmDialog = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0A8F3D)),
                    modifier = Modifier.testTag("confirm_transfer_button")
                ) {
                    Text("Confirm Transfer")
                }
            },
            dismissButton = {
                TextButton(onClick = { showWithdrawWalletConfirmDialog = null }) {
                    Text("Cancel")
                }
            }
        )
    }

    if (showWithdrawBankConfirmDialog && showWithdrawalBankDialogForGoal != null) {
        val goal = showWithdrawalBankDialogForGoal!!
        AlertDialog(
            onDismissRequest = { showWithdrawBankConfirmDialog = false },
            title = { Text("Confirm Transfer") },
            text = {
                Text("Are you sure you want to withdraw/transfer ₦${String.format("%,.2f", goal.savedAmount)} from \"${goal.title}\" directly to your $goalWithdrawBankName account ($goalWithdrawAccountNumber)?")
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.withdrawGoalFundsToBank(goal.id, goalWithdrawBankName, goalWithdrawAccountNumber)
                        showWithdrawBankConfirmDialog = false
                        showWithdrawalBankDialogForGoal = null
                        goalWithdrawAccountNumber = ""
                        goalWithdrawBankError = ""
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0A8F3D)),
                    modifier = Modifier.testTag("confirm_transfer_button")
                ) {
                    Text("Confirm Transfer")
                }
            },
            dismissButton = {
                TextButton(onClick = { showWithdrawBankConfirmDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
fun BasketSavingsProgressBar(
    savedAmount: Double,
    targetAmount: Double,
    modifier: Modifier = Modifier
) {
    val progress = if (targetAmount > 0) (savedAmount / targetAmount).toFloat().coerceIn(0f, 1f) else 0f
    
    // Smooth progress animation
    val animatedProgress by animateFloatAsState(
        targetValue = progress,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioLowBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "progress_animation"
    )

    Column(modifier = modifier) {
        // Milestone / Status indicator header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            val percentage = (progress * 100).toInt()
            val statusText = when {
                percentage >= 100 -> "🎉 Basket Goal Achieved!"
                percentage >= 75 -> "🔥 Almost there! Just a bit more"
                percentage >= 50 -> "💪 Halfway to your food basket!"
                percentage >= 25 -> "🌱 Growing nicely! Keep going"
                else -> "🚀 Journey started!"
            }
            Text(
                text = statusText,
                fontSize = 11.sp,
                fontWeight = FontWeight.SemiBold,
                color = if (percentage >= 100) Color(0xFF0A8F3D) else Color(0xFF475569)
            )
            Text(
                text = "$percentage%",
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF0A8F3D)
            )
        }

        Spacer(modifier = Modifier.height(6.dp))

        // Premium Gradient Progress Bar with Rounded Ends
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(12.dp)
                .clip(RoundedCornerShape(6.dp))
                .background(Color(0xFFE2E8F0)) // Track background
        ) {
            // Active Progress with LinearGradient
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .fillMaxWidth(animatedProgress)
                    .clip(RoundedCornerShape(6.dp))
                    .background(
                        Brush.horizontalGradient(
                            colors = listOf(
                                Color(0xFF4ADE80), // Vibrant light green
                                Color(0xFF0A8F3D)  // Premium dark green
                            )
                        )
                    )
            )
        }

        Spacer(modifier = Modifier.height(6.dp))

        // Savings stats
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            val remaining = targetAmount - savedAmount
            Text(
                text = "Saved: ₦${String.format("%,.0f", savedAmount)}",
                fontSize = 10.sp,
                color = Color(0xFF64748B),
                fontWeight = FontWeight.Medium
            )
            if (remaining > 0) {
                Text(
                    text = "₦${String.format("%,.0f", remaining)} left",
                    fontSize = 10.sp,
                    color = Color(0xFFFF8C00),
                    fontWeight = FontWeight.Bold
                )
            } else {
                Text(
                    text = "Ready to buy!",
                    fontSize = 10.sp,
                    color = Color(0xFF0A8F3D),
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
fun GoalCard(
    goal: SavingsGoal,
    onContributeClick: () -> Unit,
    onBuyNowClick: () -> Unit,
    onPauseToggle: () -> Unit,
    onWithdrawClick: () -> Unit,
    onDeleteClick: () -> Unit,
    onLockToggle: () -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    val isComplete = goal.savedAmount >= goal.targetAmount

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = SurfaceBg),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .background(Color(0xFF0A8F3D).copy(alpha = 0.1f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(getCategoryEmoji(goal.category), fontSize = 22.sp)
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(text = goal.title, fontWeight = FontWeight.Bold, fontSize = 15.sp, color = Color(0xFF1E293B))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(text = goal.category, fontSize = 11.sp, color = Color(0xFF64748B))
                            if (goal.isAutoSaveEnabled) {
                                Spacer(modifier = Modifier.width(6.dp))
                                Card(colors = CardDefaults.cardColors(containerColor = Color(0xFFE2F0D9))) {
                                    Text(
                                        "₦${goal.autoSaveAmount.toInt()}/day Auto",
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFF0A8F3D),
                                        modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                                    )
                                }
                            }
                            
                            Spacer(modifier = Modifier.width(6.dp))
                            Card(
                                colors = CardDefaults.cardColors(
                                    containerColor = if (goal.isLocked) Color(0xFFFEF2F2) else Color(0xFFF0FDF4)
                                ),
                                border = BorderStroke(1.dp, if (goal.isLocked) Color(0xFFFEE2E2) else Color(0xFFDCFCE7)),
                                shape = RoundedCornerShape(4.dp)
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = if (goal.isLocked) Icons.Default.Lock else Icons.Default.LockOpen,
                                        contentDescription = null,
                                        tint = if (goal.isLocked) Color(0xFFEF4444) else Color(0xFF15803D),
                                        modifier = Modifier.size(9.dp)
                                    )
                                    Spacer(modifier = Modifier.width(2.dp))
                                    Text(
                                        text = if (goal.isLocked) "Locked" else "Unlocked",
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (goal.isLocked) Color(0xFFEF4444) else Color(0xFF15803D)
                                    )
                                }
                            }
                        }
                    }
                }
                IconButton(onClick = { expanded = !expanded }) {
                    Icon(
                        imageVector = if (expanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                        contentDescription = "Options"
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            BasketSavingsProgressBar(
                savedAmount = goal.savedAmount,
                targetAmount = goal.targetAmount,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Target: ₦${String.format("%,.0f", goal.targetAmount)}",
                    fontSize = 11.sp,
                    color = Color(0xFF64748B)
                )
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (goal.isLocked) {
                        OutlinedButton(
                            onClick = onLockToggle,
                            border = BorderStroke(1.dp, Color(0xFF3B82F6)),
                            shape = RoundedCornerShape(8.dp),
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.LockOpen,
                                contentDescription = null,
                                tint = Color(0xFF3B82F6),
                                modifier = Modifier.size(12.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Unlock", fontSize = 12.sp, color = Color(0xFF3B82F6))
                        }
                    } else {
                        OutlinedButton(
                            onClick = onWithdrawClick,
                            border = BorderStroke(1.dp, Color(0xFF0A8F3D)),
                            shape = RoundedCornerShape(8.dp),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFF0A8F3D)),
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.AccountBalanceWallet,
                                contentDescription = null,
                                tint = Color(0xFF0A8F3D),
                                modifier = Modifier.size(12.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Withdraw", fontSize = 12.sp, color = Color(0xFF0A8F3D))
                        }
                    }
                    if (isComplete) {
                        Button(
                            onClick = onBuyNowClick,
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF15803D)),
                            shape = RoundedCornerShape(8.dp),
                            contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp)
                        ) {
                            Icon(Icons.Default.ShoppingCart, contentDescription = null, modifier = Modifier.size(13.dp), tint = Color.White)
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Buy Now", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.White)
                        }
                    } else {
                        Button(
                            onClick = onContributeClick,
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0A8F3D)),
                            shape = RoundedCornerShape(8.dp),
                            contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp)
                        ) {
                            Text("Add Funds", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            if (expanded) {
                Divider(modifier = Modifier.padding(vertical = 12.dp), color = Color(0xFFF1F5F9))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    TextButton(onClick = onPauseToggle) {
                        Icon(
                            imageVector = if (goal.isPaused) Icons.Default.PlayArrow else Icons.Default.Pause,
                            contentDescription = null,
                            tint = Color(0xFFFF8C00)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(if (goal.isPaused) "Resume" else "Pause", color = Color(0xFFFF8C00))
                    }
                    TextButton(onClick = onLockToggle) {
                        Icon(
                            imageVector = if (goal.isLocked) Icons.Default.LockOpen else Icons.Default.Lock,
                            contentDescription = null,
                            tint = Color(0xFF3B82F6)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(if (goal.isLocked) "Unlock" else "Lock", color = Color(0xFF3B82F6))
                    }
                    TextButton(onClick = onWithdrawClick) {
                        Icon(Icons.Default.Reply, contentDescription = null, tint = Color(0xFF065A26))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Withdraw", color = Color(0xFF065A26))
                    }
                    TextButton(onClick = onDeleteClick) {
                        Icon(Icons.Default.Delete, contentDescription = null, tint = MaterialTheme.colorScheme.error)
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Delete", color = MaterialTheme.colorScheme.error)
                    }
                }
            }
        }
    }
}

// --- MARKETPLACE SCREEN ---
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MarketScreen(
    viewModel: KoboViewModel,
    onBuyNowClick: (MarketItem) -> Unit,
    onSaveTowardsClick: (MarketItem) -> Unit,
    onAddToCartClick: ((MarketItem) -> Unit)? = null
) {
    val rawItems by viewModel.filteredMarketItems.collectAsStateWithLifecycle()
    val allMarketItems by viewModel.marketItems.collectAsStateWithLifecycle()
    val wishlist by viewModel.wishlistState.collectAsStateWithLifecycle()
    val cartItems by viewModel.cartState.collectAsStateWithLifecycle()
    val trackedIds by viewModel.trackedItemIds.collectAsStateWithLifecycle()
    val initialPrices by viewModel.trackedItemInitialPrices.collectAsStateWithLifecycle()
    var showOnlyWishlist by remember { mutableStateOf(false) }
    var filterOnlyBulk by remember { mutableStateOf(false) }

    val items = rawItems.filter { item ->
        val passesWishlist = !showOnlyWishlist || wishlist.contains(item.id)
        val isBulk = item.name.contains("50kg", ignoreCase = true) ||
                     item.name.contains("10kg", ignoreCase = true) ||
                     item.name.contains("Basket", ignoreCase = true) ||
                     item.name.contains("Tuber", ignoreCase = true) ||
                     item.name.contains("Crate", ignoreCase = true) ||
                     item.name.contains("Carton", ignoreCase = true) ||
                     item.name.contains("Bag", ignoreCase = true) ||
                     item.name.contains("Wholesale", ignoreCase = true)
        val passesBulk = !filterOnlyBulk || isBulk
        passesWishlist && passesBulk
    }

    val categories = listOf("All", "Rice", "Beans", "Garri", "Tomatoes", "Yam", "Fish", "Bundle")
    val selectedCat by viewModel.selectedMarketCategory.collectAsStateWithLifecycle()
    val search by viewModel.searchQuery.collectAsStateWithLifecycle()
    val sortBy by viewModel.sortBy.collectAsStateWithLifecycle()
    val selectedState by viewModel.selectedMarketState.collectAsStateWithLifecycle()
    val currentUserLocation by viewModel.currentUserLocation.collectAsStateWithLifecycle()

    var isLoading by remember { mutableStateOf(true) }
    var showCartDialog by remember { mutableStateOf(false) }
    
    // Collapsible filter panel state
    var filterPanelExpanded by remember { mutableStateOf(false) }
    var tempCategory by remember(filterPanelExpanded) { mutableStateOf(selectedCat) }
    var tempState by remember(filterPanelExpanded) { mutableStateOf(selectedState) }
    var tempSortBy by remember(filterPanelExpanded) { mutableStateOf(sortBy) }
    var tempFilterOnlyBulk by remember(filterPanelExpanded) { mutableStateOf(filterOnlyBulk) }

    // Detail view modal state
    var detailItemHistory by remember { mutableStateOf<List<MarketItem>>(emptyList()) }
    
    LaunchedEffect(Unit) {
        delay(800)
        isLoading = false
    }

    val marketStates = listOf("All", "Lagos", "Ondo", "Oyo", "Kano", "Abuja")

    var showPriceAlertsSection by remember { mutableStateOf(false) }
    val alerts by viewModel.priceAlerts.collectAsStateWithLifecycle()
    var newAlertCategory by remember { mutableStateOf("Rice") }
    var newAlertTargetPrice by remember { mutableStateOf("") }

    if (showCartDialog) {
        CartDialog(
            viewModel = viewModel,
            onDismiss = { showCartDialog = false }
        )
    }

    // Render Detail View Dialog if selected
    if (detailItemHistory.isNotEmpty()) {
        val currentItem = detailItemHistory.last()
        FoodItemDetailDialog(
            item = currentItem,
            allMarketItems = allMarketItems,
            isWishlisted = wishlist.contains(currentItem.id),
            onWishlistToggle = { viewModel.toggleWishlist(currentItem) },
            onBuyNowClick = {
                onBuyNowClick(currentItem)
                detailItemHistory = emptyList()
            },
            onSaveTowardsClick = {
                onSaveTowardsClick(currentItem)
                detailItemHistory = emptyList()
            },
            onItemClick = { nextItem ->
                detailItemHistory = detailItemHistory + nextItem
            },
            onDismiss = {
                detailItemHistory = detailItemHistory.dropLast(1)
            }
        )
    }

    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        val availableWidth = maxWidth
        val cols = when {
            availableWidth >= 1200.dp -> 4
            availableWidth >= 800.dp -> 3
            availableWidth >= 600.dp -> 3
            else -> 2
        }

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .background(SoftBackground)
                .statusBarsPadding(),
            contentPadding = PaddingValues(bottom = 24.dp)
        ) {
            item {
                // Search & Headers Block
                Column(modifier = Modifier.padding(start = 16.dp, end = 16.dp, top = 18.dp, bottom = 16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Food Marketplace 🥦", fontSize = 22.sp, fontWeight = FontWeight.Bold, color = TextSlate800)
                            Text(
                                text = if (showOnlyWishlist) "Your personal favorites and wishlist items" else "Real-time prices from trusted local wholesale merchants",
                                fontSize = 13.sp,
                                color = TextSlate500
                            )
                        }
                        
                        val cartCount = cartItems.sumOf { it.quantity }
                        
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Elegant Wishlist Button in Header
                            Box {
                                IconButton(
                                    onClick = { showOnlyWishlist = !showOnlyWishlist },
                                    modifier = Modifier
                                        .background(Color.White, CircleShape)
                                        .border(1.dp, BorderSlate100, CircleShape)
                                ) {
                                    Icon(
                                        imageVector = if (showOnlyWishlist) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                                        contentDescription = "Show Wishlist Only",
                                        tint = if (showOnlyWishlist) Color.Red else Color(0xFF64748B)
                                    )
                                }
                                if (wishlist.isNotEmpty()) {
                                    Box(
                                        modifier = Modifier
                                            .align(Alignment.TopEnd)
                                            .offset(x = 4.dp, y = (-4).dp)
                                            .background(Color.Red, CircleShape)
                                            .padding(horizontal = 6.dp, vertical = 2.dp)
                                    ) {
                                        Text(
                                            text = wishlist.size.toString(),
                                            color = Color.White,
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }
                            }

                            // Elegant Cart Button in Header
                            Box(
                                modifier = Modifier.clickable { showCartDialog = true }
                            ) {
                                IconButton(
                                    onClick = { showCartDialog = true },
                                    modifier = Modifier
                                        .background(Color.White, CircleShape)
                                        .border(1.dp, BorderSlate100, CircleShape)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.ShoppingCart,
                                        contentDescription = "Shopping Cart",
                                        tint = PrimaryGreen
                                    )
                                }
                                if (cartCount > 0) {
                                    Box(
                                        modifier = Modifier
                                            .align(Alignment.TopEnd)
                                            .offset(x = 4.dp, y = (-4).dp)
                                            .background(Color.Red, CircleShape)
                                            .padding(horizontal = 6.dp, vertical = 2.dp)
                                    ) {
                                        Text(
                                            text = cartCount.toString(),
                                            color = Color.White,
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Search Bar with custom high contrast colors
                    OutlinedTextField(
                        value = search,
                        onValueChange = { viewModel.setSearchQuery(it) },
                        placeholder = { Text("Search 50kg rice, basket tomatoes...") },
                        leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = TextSlate500) },
                        trailingIcon = {
                            if (search.isNotEmpty()) {
                                IconButton(onClick = { viewModel.setSearchQuery("") }) {
                                    Icon(Icons.Default.Close, contentDescription = "Clear", tint = TextSlate500)
                                }
                            }
                        },
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("market_search_input"),
                        shape = RoundedCornerShape(12.dp),
                        colors = defaultTextFieldColors()
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    // --- CONSOLIDATED FILTER BUTTON ---
                    val isFilterActive = selectedCat != "All" || selectedState != "All" || sortBy != "Cheapest" || filterOnlyBulk
                    
                    Button(
                        onClick = { filterPanelExpanded = !filterPanelExpanded },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (isFilterActive) SecondaryOrange else Color.White
                        ),
                        border = BorderStroke(1.dp, if (isFilterActive) Color.Transparent else BorderSlate100),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                            .testTag("filter_sort_toggle_btn")
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                imageVector = if (isFilterActive) Icons.Default.Favorite else Icons.Default.FavoriteBorder, // using favorite border as generic decoration or similar
                                contentDescription = "Filter and Sort",
                                tint = if (isFilterActive) Color.White else TextSlate800,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = if (isFilterActive) "Filters Active (Tap to Edit) ⚙️" else "Filter & Sort Options ⚙️",
                                fontWeight = FontWeight.Bold,
                                color = if (isFilterActive) Color.White else TextSlate800,
                                fontSize = 13.sp
                            )
                            if (isFilterActive) {
                                Spacer(modifier = Modifier.width(6.dp))
                                Box(
                                    modifier = Modifier
                                        .size(8.dp)
                                        .background(Color.White, CircleShape)
                                )
                            }
                        }
                    }

                    // Collapsible Filter Panel Card
                    AnimatedVisibility(visible = filterPanelExpanded) {
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 8.dp),
                            colors = CardDefaults.cardColors(containerColor = Color.White),
                            shape = RoundedCornerShape(16.dp),
                            border = BorderStroke(1.dp, BorderSlate100),
                            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text("Consolidated Filters ⚙️", fontWeight = FontWeight.Bold, fontSize = 15.sp, color = TextSlate800)
                                    IconButton(onClick = { filterPanelExpanded = false }) {
                                        Icon(Icons.Default.Close, contentDescription = "Close", tint = TextSlate500, modifier = Modifier.size(18.dp))
                                    }
                                }
                                
                                Spacer(modifier = Modifier.height(10.dp))
                                
                                // Section 1: Categories
                                Text("Category Type", fontWeight = FontWeight.Bold, fontSize = 11.sp, color = TextSlate500)
                                Spacer(modifier = Modifier.height(6.dp))
                                Row(
                                    modifier = Modifier.horizontalScroll(rememberScrollState()),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    categories.forEach { cat ->
                                        val isSelected = tempCategory == cat
                                        Box(
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(20.dp))
                                                .background(if (isSelected) PrimaryGreen else Color(0xFFF1F5F9))
                                                .clickable { tempCategory = cat }
                                                .padding(horizontal = 12.dp, vertical = 6.dp)
                                        ) {
                                            Text(
                                                text = cat,
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = if (isSelected) Color.White else TextSlate500
                                            )
                                        }
                                    }
                                }
                                
                                Spacer(modifier = Modifier.height(12.dp))
                                
                                // Section 2: Location/State
                                Text("Market State/Location", fontWeight = FontWeight.Bold, fontSize = 11.sp, color = TextSlate500)
                                Spacer(modifier = Modifier.height(6.dp))
                                Row(
                                    modifier = Modifier.horizontalScroll(rememberScrollState()),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    marketStates.forEach { state ->
                                        val isSelected = tempState == state
                                        Box(
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(20.dp))
                                                .background(if (isSelected) SecondaryOrange else Color(0xFFF1F5F9))
                                                .clickable { tempState = state }
                                                .padding(horizontal = 12.dp, vertical = 6.dp)
                                        ) {
                                            Text(
                                                text = state,
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = if (isSelected) Color.White else TextSlate500
                                            )
                                        }
                                    }
                                }
                                
                                Spacer(modifier = Modifier.height(12.dp))
                                
                                // Section 3: Sort Options
                                Text("Sort Priority", fontWeight = FontWeight.Bold, fontSize = 11.sp, color = TextSlate500)
                                Spacer(modifier = Modifier.height(6.dp))
                                Row(
                                    modifier = Modifier.horizontalScroll(rememberScrollState()),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    listOf(
                                        "Cheapest" to "Cheapest",
                                        "Nearest" to "Nearest",
                                        "Fastest" to "Fastest Delivery"
                                    ).forEach { (label, sortValue) ->
                                        val isSelected = tempSortBy == sortValue
                                        Box(
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(20.dp))
                                                .background(if (isSelected) AccentGold else Color(0xFFF1F5F9))
                                                .clickable { tempSortBy = sortValue }
                                                .padding(horizontal = 12.dp, vertical = 6.dp)
                                        ) {
                                            Text(
                                                text = label,
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = if (isSelected) Color(0xFF1E293B) else TextSlate500
                                            )
                                        }
                                    }
                                }
                                
                                Spacer(modifier = Modifier.height(12.dp))
                                
                                // Section 4: Bulk Selection
                                Text("Product Purchase Type", fontWeight = FontWeight.Bold, fontSize = 11.sp, color = TextSlate500)
                                Spacer(modifier = Modifier.height(6.dp))
                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    listOf(
                                        "All Items" to false,
                                        "Bulk/Wholesale 📦" to true
                                    ).forEach { (label, bulkValue) ->
                                        val isSelected = tempFilterOnlyBulk == bulkValue
                                        Box(
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(20.dp))
                                                .background(if (isSelected) PrimaryGreen else Color(0xFFF1F5F9))
                                                .clickable { tempFilterOnlyBulk = bulkValue }
                                                .padding(horizontal = 12.dp, vertical = 6.dp)
                                        ) {
                                            Text(
                                                text = label,
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = if (isSelected) Color.White else TextSlate500
                                            )
                                        }
                                    }
                                }
                                
                                Spacer(modifier = Modifier.height(16.dp))
                                
                                // Apply & Reset Action Buttons
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    OutlinedButton(
                                        onClick = {
                                            tempCategory = "All"
                                            tempState = "All"
                                            tempSortBy = "Cheapest"
                                            tempFilterOnlyBulk = false
                                            viewModel.setMarketCategory("All")
                                            viewModel.setMarketState("All")
                                            viewModel.setSortBy("Cheapest")
                                            filterOnlyBulk = false
                                            filterPanelExpanded = false
                                        },
                                        modifier = Modifier.weight(1f),
                                        shape = RoundedCornerShape(10.dp),
                                        border = BorderStroke(1.dp, Color(0xFFCBD5E1))
                                    ) {
                                        Text("Reset/Clear All", color = TextSlate800, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                    }
                                    
                                    Button(
                                        onClick = {
                                            viewModel.setMarketCategory(tempCategory)
                                            viewModel.setMarketState(tempState)
                                            viewModel.setSortBy(tempSortBy)
                                            filterOnlyBulk = tempFilterOnlyBulk
                                            filterPanelExpanded = false
                                        },
                                        modifier = Modifier.weight(1f),
                                        colors = ButtonDefaults.buttonColors(containerColor = PrimaryGreen),
                                        shape = RoundedCornerShape(10.dp)
                                    ) {
                                        Text("Apply Filters", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // --- HORIZONTAL QUICK ACCESS CATEGORY ROW ---
                    CategoryFilterBar(
                        selectedCategory = selectedCat,
                        onCategorySelected = { viewModel.setMarketCategory(it) },
                        items = rawItems,
                        modifier = Modifier.padding(vertical = 4.dp)
                    )
                }
            }

            item {
                // Historical Price Trends Section
                HistoricalPriceTrendChart(viewModel = viewModel)
            }

            // --- FEATURE 3: FOOD PRICE ALERTS ---
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 6.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFFFFBEB)),
                    border = BorderStroke(1.dp, Color(0xFFFDE68A)),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.NotificationsActive,
                                    contentDescription = "Price Alerts",
                                    tint = Color(0xFFD97706),
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Smart Price Drop Alerts (${alerts.size})",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF78350F)
                                )
                            }
                            TextButton(onClick = { showPriceAlertsSection = !showPriceAlertsSection }) {
                                Text(
                                    text = if (showPriceAlertsSection) "Collapse ✕" else "Manage ⚙️",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFFD97706)
                                )
                            }
                        }
                        
                        if (showPriceAlertsSection) {
                            Spacer(modifier = Modifier.height(10.dp))
                            
                            if (alerts.isEmpty()) {
                                Text("No active price alerts. Add one below!", fontSize = 11.sp, color = Color(0xFF92400E))
                            } else {
                                alerts.forEach { alert ->
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .background(Color.White.copy(alpha = 0.6f), RoundedCornerShape(6.dp))
                                            .padding(horizontal = 10.dp, vertical = 6.dp)
                                            .padding(bottom = 4.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Column {
                                            Text("${alert.category} target price drops", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1E293B))
                                            Text("Alert below: ₦${String.format("%,.0f", alert.targetPrice)} (Current: ₦${String.format("%,.0f", alert.originalPrice)})", fontSize = 10.sp, color = Color(0xFF64748B))
                                        }
                                        IconButton(
                                            onClick = { viewModel.removePriceAlert(alert.id) },
                                            modifier = Modifier.size(24.dp)
                                        ) {
                                            Icon(Icons.Default.Delete, contentDescription = "Delete Alert", tint = Color.Red, modifier = Modifier.size(16.dp))
                                        }
                                    }
                                }
                            }
                            
                            Spacer(modifier = Modifier.height(12.dp))
                            
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(modifier = Modifier.weight(1f)) {
                                    Button(
                                        onClick = {
                                            val cats = listOf("Rice", "Beans", "Garri", "Tomatoes", "Yam", "Fish")
                                            val nextIdx = (cats.indexOf(newAlertCategory) + 1) % cats.size
                                            newAlertCategory = cats[nextIdx]
                                        },
                                        colors = ButtonDefaults.buttonColors(containerColor = SurfaceBg),
                                        border = BorderStroke(1.dp, Color(0xFFFDE68A)),
                                        shape = RoundedCornerShape(8.dp),
                                        modifier = Modifier.fillMaxWidth(),
                                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp)
                                    ) {
                                        Text(newAlertCategory, fontSize = 11.sp, color = Color(0xFF78350F), fontWeight = FontWeight.Bold)
                                    }
                                }
                                
                                OutlinedTextField(
                                    value = newAlertTargetPrice,
                                    onValueChange = { newAlertTargetPrice = it },
                                    placeholder = { Text("Target ₦", fontSize = 11.sp) },
                                    singleLine = true,
                                    modifier = Modifier
                                        .weight(1.2f)
                                        .height(46.dp),
                                    shape = RoundedCornerShape(8.dp),
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedContainerColor = Color.White,
                                        unfocusedContainerColor = Color.White,
                                        focusedBorderColor = Color(0xFFD97706),
                                        unfocusedBorderColor = Color(0xFFFDE68A)
                                    )
                                )
                                
                                Button(
                                    onClick = {
                                        val target = newAlertTargetPrice.toDoubleOrNull() ?: 0.0
                                        if (target > 0) {
                                            val currentPrice = when (newAlertCategory) {
                                                "Rice" -> 70800.0
                                                "Tomatoes" -> 6500.0
                                                "Beans" -> 35000.0
                                                "Garri" -> 15000.0
                                                "Yam" -> 5500.0
                                                "Fish" -> 12000.0
                                                else -> 10000.0
                                            }
                                            viewModel.addPriceAlert(newAlertCategory, target, currentPrice)
                                            newAlertTargetPrice = ""
                                        }
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD97706)),
                                    shape = RoundedCornerShape(8.dp),
                                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp)
                                ) {
                                    Text("Set Alert", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                            
                            Spacer(modifier = Modifier.height(10.dp))
                            
                            Button(
                                onClick = { viewModel.simulatePriceDrop() },
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1E293B)),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Icon(Icons.Default.TrendingDown, contentDescription = null, tint = Color.Green, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Simulate Wholesale Price Drop 📈", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.Green)
                            }
                        }
                    }
                }
            }

            // Product Listings Grid grouped by state or direct items list
            if (isLoading) {
                items(3) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 6.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        repeat(cols) {
                            Box(modifier = Modifier.weight(1f)) { MarketItemSkeletonCard() }
                        }
                    }
                }
            } else if (items.isEmpty()) {
                // Friendly Empty State when there are no matching items (e.g. from search)
                item {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 48.dp, bottom = 48.dp)
                            .padding(horizontal = 24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text("🔍", fontSize = 54.sp)
                        Spacer(modifier = Modifier.height(14.dp))
                        Text(
                            text = if (search.isNotEmpty()) "No items found for \'$search\'" else "No food items found matching filters",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextSlate800,
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Try clearing search keywords or selecting \"All Items\" / \"All States\" in the Filter panel above.",
                            fontSize = 13.sp,
                            color = TextSlate500,
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(
                            onClick = {
                                viewModel.setSearchQuery("")
                                viewModel.setMarketCategory("All")
                                viewModel.setMarketState("All")
                                viewModel.setSortBy("Cheapest")
                                filterOnlyBulk = false
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = PrimaryGreen),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Text("Reset Search & Filters", color = Color.White)
                        }
                    }
                }
            } else {
                // If a search query is active, display items regardless of location grouping
                if (search.isNotEmpty() || selectedState != "All") {
                    val chunkedItems = items.chunked(cols)
                    items(chunkedItems) { rowItems ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 6.dp),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            rowItems.forEach { item ->
                                Box(modifier = Modifier.weight(1f)) {
                                    val isWishlisted = wishlist.contains(item.id)
                                    val isTracked = trackedIds.contains(item.id)
                                    val initialPrice = initialPrices[item.id]
                                    val quantity = cartItems.find { it.item.id == item.id }?.quantity ?: 0
                                    ProductCard(
                                        item = item,
                                        isWishlisted = isWishlisted,
                                        quantity = quantity,
                                        isTracked = isTracked,
                                        initialPrice = initialPrice,
                                        onWishlistClick = { viewModel.toggleWishlist(item) },
                                        onTrackPriceClick = { viewModel.toggleTrackPrice(item) },
                                        onAddToCart = { if (onAddToCartClick != null && (item.allowPortions || item.isBundle)) onAddToCartClick(item) else viewModel.addToCart(item) },
                                        onRemoveFromCart = { viewModel.removeFromCart(item) },
                                        onSaveClick = { onSaveTowardsClick(item) },
                                        onItemClick = { detailItemHistory = listOf(item) }
                                    )
                                }
                            }
                            if (rowItems.size < cols) {
                                repeat(cols - rowItems.size) {
                                    Spacer(modifier = Modifier.weight(1f))
                                }
                            }
                        }
                    }
                } else {
                    // Standard State Grouping (No active search)
                    val userState = when {
                        currentUserLocation.contains("Lagos", ignoreCase = true) -> "Lagos"
                        currentUserLocation.contains("Ondo", ignoreCase = true) -> "Ondo"
                        currentUserLocation.contains("Oyo", ignoreCase = true) -> "Oyo"
                        currentUserLocation.contains("Kano", ignoreCase = true) -> "Kano"
                        currentUserLocation.contains("Abuja", ignoreCase = true) -> "Abuja"
                        else -> ""
                    }
                    val grouped = items.groupBy { it.state }
                    val sortedGroups = grouped.entries.sortedBy { entry ->
                        if (entry.key.equals(userState, ignoreCase = true)) 0 else 1
                    }
                    sortedGroups.forEach { (state, stateItems) ->
                        item {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 16.dp)
                                    .padding(top = 12.dp, bottom = 4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(6.dp)
                                        .background(SecondaryOrange, CircleShape)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "📍 $state Market Sellers",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = TextSlate800
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                HorizontalDivider(
                                    color = BorderSlate100,
                                    thickness = 1.dp,
                                    modifier = Modifier.weight(1f)
                                )
                            }
                        }
                        val chunkedStateItems = stateItems.chunked(cols)
                        items(chunkedStateItems) { rowItems ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 16.dp, vertical = 6.dp),
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                rowItems.forEach { item ->
                                    Box(modifier = Modifier.weight(1f)) {
                                        val isWishlisted = wishlist.contains(item.id)
                                        val isTracked = trackedIds.contains(item.id)
                                        val initialPrice = initialPrices[item.id]
                                        val quantity = cartItems.find { it.item.id == item.id }?.quantity ?: 0
                                        ProductCard(
                                            item = item,
                                            isWishlisted = isWishlisted,
                                            quantity = quantity,
                                            isTracked = isTracked,
                                            initialPrice = initialPrice,
                                            onWishlistClick = { viewModel.toggleWishlist(item) },
                                            onTrackPriceClick = { viewModel.toggleTrackPrice(item) },
                                            onAddToCart = { if (onAddToCartClick != null && (item.allowPortions || item.isBundle)) onAddToCartClick(item) else viewModel.addToCart(item) },
                                            onRemoveFromCart = { viewModel.removeFromCart(item) },
                                            onSaveClick = { onSaveTowardsClick(item) },
                                            onItemClick = { detailItemHistory = listOf(item) }
                                        )
                                    }
                                }
                                if (rowItems.size < cols) {
                                    repeat(cols - rowItems.size) {
                                        Spacer(modifier = Modifier.weight(1f))
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

fun getBulkUnitPriceText(item: MarketItem): String {
    val name = item.name.lowercase()
    return when {
        name.contains("50kg") -> "₦${String.format("%,.0f", item.price / 50)}/kg"
        name.contains("10kg") -> "₦${String.format("%,.0f", item.price / 10)}/kg"
        name.contains("3 large") -> "₦${String.format("%,.0f", item.price / 3)}/tuber"
        name.contains("1kg") -> "₦${String.format("%,.0f", item.price)}/kg"
        name.contains("basket") -> "Wholesale Basket"
        name.contains("paint bucket") -> "Wholesale Bucket"
        else -> "Wholesale Rate"
    }
}

// --- NEW FEATURE: FOOD ITEM DETAIL VIEW MODAL DIALOG ---
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FoodItemDetailDialog(
    item: MarketItem,
    allMarketItems: List<MarketItem>,
    isWishlisted: Boolean,
    onWishlistToggle: () -> Unit,
    onBuyNowClick: () -> Unit,
    onSaveTowardsClick: () -> Unit,
    onItemClick: (MarketItem) -> Unit,
    onDismiss: () -> Unit
) {
    androidx.compose.ui.window.Dialog(
        onDismissRequest = onDismiss,
        properties = androidx.compose.ui.window.DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .fillMaxHeight(0.85f)
                .clip(RoundedCornerShape(24.dp)),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 12.dp)
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                // Header Thumbnail with overlays
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(180.dp)
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(SecondaryOrange.copy(alpha = 0.2f), PrimaryGreen.copy(alpha = 0.1f))
                            )
                        )
                ) {
                    FoodItemImage(
                        imageUrl = item.imageUrl,
                        category = item.category,
                        contentDescription = item.name,
                        modifier = Modifier.fillMaxSize()
                    )

                    // Close Button
                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(12.dp)
                            .background(Color.White.copy(alpha = 0.8f), CircleShape)
                    ) {
                        Icon(Icons.Default.Close, contentDescription = "Close", tint = TextSlate800)
                    }

                    // Bookmark / Heart toggle overlay
                    IconButton(
                        onClick = onWishlistToggle,
                        modifier = Modifier
                            .align(Alignment.TopStart)
                            .padding(12.dp)
                            .background(Color.White.copy(alpha = 0.8f), CircleShape)
                    ) {
                        Icon(
                            imageVector = if (isWishlisted) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                            contentDescription = "Save Item",
                            tint = if (isWishlisted) Color.Red else TextSlate500
                        )
                    }
                }

                // Details content scrollable
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .verticalScroll(rememberScrollState())
                        .padding(16.dp)
                ) {
                    // Category & Distance Tags
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .background(PrimaryGreen.copy(alpha = 0.1f), RoundedCornerShape(6.dp))
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text(item.category, color = PrimaryGreen, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                        }
                        Box(
                            modifier = Modifier
                                .background(SecondaryOrange.copy(alpha = 0.1f), RoundedCornerShape(6.dp))
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text("${item.distance} km away", color = SecondaryOrange, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                        }
                        Box(
                            modifier = Modifier
                                .background(Color(0xFFF1F5F9), RoundedCornerShape(6.dp))
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text(item.state, color = TextSlate500, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Item Name
                    Text(
                        text = item.name,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextSlate800
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    // Vendor/Seller Row with emoji for bulletproof compilations
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text("🏪", fontSize = 14.sp)
                        Text(
                            text = "Seller: ${item.vendorName}",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = TextSlate800
                        )
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Price & Rating Info
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text("Wholesale Unit Price", fontSize = 11.sp, color = TextSlate500, fontWeight = FontWeight.Bold)
                            PriceDisplay(
                                price = item.price,
                                originalPrice = item.originalPrice,
                                priceFontSize = 22.sp,
                                originalPriceFontSize = 14.sp,
                                fontWeight = FontWeight.Black
                            )
                        }
                        Column(horizontalAlignment = Alignment.End) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Star, contentDescription = "Rating", tint = AccentGold, modifier = Modifier.size(16.dp))
                                Text(" ${item.rating}", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = TextSlate800)
                            }
                            Text("(${item.ratingCount} reviews)", fontSize = 10.sp, color = TextSlate500)
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))
                    HorizontalDivider(color = BorderSlate100, thickness = 1.dp)
                    Spacer(modifier = Modifier.height(12.dp))

                    // Stock and Delivery Time details
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Availability", fontSize = 10.sp, color = TextSlate500, fontWeight = FontWeight.Bold)
                            Text("${item.stock} Units Stocked", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = TextSlate800)
                        }
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Est. Delivery Speed", fontSize = 10.sp, color = TextSlate500, fontWeight = FontWeight.Bold)
                            Text("${item.deliveryTimeMinutes} Minutes", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = TextSlate800)
                        }
                    }

                    if (item.isBundle && item.bundleItems.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(12.dp))
                        Text("Bundle Items", fontSize = 10.sp, color = TextSlate500, fontWeight = FontWeight.Bold)
                        Text(item.bundleItems, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = PrimaryGreen)
                    }

                    Spacer(modifier = Modifier.height(12.dp))
                    Text("About this Listing", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = TextSlate800)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Premium, carefully selected and packed ${item.name} supplied by ${item.vendorName} located in the ${item.state} market cluster. AfriSav verified wholesale merchants guarantee accurate weighting and fast dispatch with minimal logistics friction.",
                        fontSize = 12.sp,
                        color = TextSlate500,
                        lineHeight = 16.sp
                    )

                    // ADDITIVE REQUIREMENT: "More from this Vendor" Section
                    val otherVendorItems = allMarketItems.filter { it.vendorName == item.vendorName && it.id != item.id }
                    if (otherVendorItems.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(16.dp))
                        HorizontalDivider(color = BorderSlate100, thickness = 1.dp)
                        Spacer(modifier = Modifier.height(12.dp))
                        
                        Text(
                            text = "Other Items from ${item.vendorName} 🏪",
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
                            color = TextSlate800
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                            contentPadding = PaddingValues(bottom = 6.dp)
                        ) {
                            items(otherVendorItems) { otherItem ->
                                Card(
                                    modifier = Modifier
                                        .width(130.dp)
                                        .clickable { onItemClick(otherItem) },
                                    colors = CardDefaults.cardColors(containerColor = Color(0xFFF8FAFC)),
                                    shape = RoundedCornerShape(12.dp),
                                    border = BorderStroke(1.dp, BorderSlate100)
                                ) {
                                    Column {
                                        Box(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .height(70.dp)
                                                .background(Color(0xFFE2E8F0)),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            FoodItemImage(
                                                imageUrl = otherItem.imageUrl,
                                                category = otherItem.category,
                                                contentDescription = otherItem.name,
                                                modifier = Modifier.fillMaxSize()
                                            )
                                        }
                                        Column(modifier = Modifier.padding(6.dp)) {
                                            Text(
                                                text = otherItem.name,
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = TextSlate800,
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis
                                            )
                                            Spacer(modifier = Modifier.height(2.dp))
                                            Text(
                                                text = "₦${String.format("%,.0f", otherItem.price)}",
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.Black,
                                                color = PrimaryGreen
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                // Sticky Bottom Action Controls
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFF8FAFC)),
                    elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Save Towards Goal Action
                        OutlinedButton(
                            onClick = onSaveTowardsClick,
                            modifier = Modifier
                                .weight(1f)
                                .height(44.dp),
                            shape = RoundedCornerShape(10.dp),
                            border = BorderStroke(1.dp, SecondaryOrange),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = SecondaryOrange)
                        ) {
                            Text("Save Towards", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }

                        // Buy Now / Order Action
                        Button(
                            onClick = onBuyNowClick,
                            modifier = Modifier
                                .weight(1.2f)
                                .height(44.dp),
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = PrimaryGreen)
                        ) {
                            Text("Buy Now ⚡", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.White)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ProductCard(
    item: MarketItem,
    isWishlisted: Boolean,
    quantity: Int,
    isTracked: Boolean,
    initialPrice: Double?,
    onWishlistClick: () -> Unit,
    onTrackPriceClick: () -> Unit,
    onAddToCart: () -> Unit,
    onRemoveFromCart: () -> Unit,
    onSaveClick: () -> Unit,
    onItemClick: () -> Unit
) {
    val isBulkItem = item.name.contains("50kg", ignoreCase = true) ||
                     item.name.contains("10kg", ignoreCase = true) ||
                     item.name.contains("Basket", ignoreCase = true) ||
                     item.name.contains("Tuber", ignoreCase = true) ||
                     item.name.contains("Crate", ignoreCase = true) ||
                     item.name.contains("Carton", ignoreCase = true) ||
                     item.name.contains("Bag", ignoreCase = true) ||
                     item.name.contains("Wholesale", ignoreCase = true)

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onItemClick() }
            .testTag("product_card_${item.id}"),
        colors = CardDefaults.cardColors(containerColor = SurfaceBg),
        shape = RoundedCornerShape(14.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column {
            // Food visual thumbnail placeholder
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(110.dp)
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(SecondaryOrange.copy(alpha = 0.2f), PrimaryGreen.copy(alpha = 0.1f))
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                FoodItemImage(
                    imageUrl = item.imageUrl,
                    category = item.category,
                    contentDescription = item.name,
                    modifier = Modifier.fillMaxSize()
                )
                // Overlay Distance Card on the image
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(6.dp),
                    contentAlignment = Alignment.BottomStart
                ) {
                    Card(colors = CardDefaults.cardColors(containerColor = SecondaryOrange)) {
                        Text(
                            text = "${item.distance} km",
                            fontSize = 9.sp,
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }

                // Overlay WHOLESALE badge on bulk items
                if (isBulkItem) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(6.dp),
                        contentAlignment = Alignment.BottomEnd
                    ) {
                        Card(
                            colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B)),
                            shape = RoundedCornerShape(4.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(2.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Inventory,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(9.dp)
                                )
                                Text(
                                    text = "BULK",
                                    fontSize = 8.sp,
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }

                // Elegant Top-Left Favorite/Wishlist overlay button
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(6.dp),
                    contentAlignment = Alignment.TopStart
                ) {
                    IconButton(
                        onClick = onWishlistClick,
                        modifier = Modifier
                            .size(28.dp)
                            .background(Color.White.copy(alpha = 0.9f), CircleShape)
                            .border(1.dp, Color(0xFFE2E8F0), CircleShape)
                    ) {
                        Icon(
                            imageVector = if (isWishlisted) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                            contentDescription = "Toggle Wishlist",
                            tint = if (isWishlisted) Color.Red else Color(0xFF64748B),
                            modifier = Modifier.size(14.dp)
                        )
                    }
                }
            }

            Column(modifier = Modifier.padding(10.dp)) {
                Text(
                    text = item.name,
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp,
                    color = TextSlate800,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "by ${item.vendorName}",
                        fontSize = 10.sp,
                        color = TextSlate500,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f)
                    )
                    Box(
                        modifier = Modifier
                            .background(SecondaryOrange.copy(alpha = 0.08f), RoundedCornerShape(4.dp))
                            .padding(horizontal = 4.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = item.state,
                            fontSize = 8.sp,
                            color = SecondaryOrange,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                Spacer(modifier = Modifier.height(6.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    PriceDisplay(
                        price = item.price,
                        originalPrice = item.originalPrice,
                        priceFontSize = 14.sp,
                        fontWeight = FontWeight.Black
                    )
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Star, contentDescription = null, tint = AccentGold, modifier = Modifier.size(12.dp))
                        Text(" ${item.rating}", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = TextSlate800)
                    }
                }

                // Highlight Wholesale Specs and Unit Price Savings breakdown
                if (isBulkItem) {
                    Spacer(modifier = Modifier.height(6.dp))
                    val unitPrice = getBulkUnitPriceText(item)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color(0xFFF0FDF4), RoundedCornerShape(6.dp))
                            .padding(horizontal = 6.dp, vertical = 4.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = unitPrice,
                            fontSize = 10.sp,
                            color = Color(0xFF15803D),
                            fontWeight = FontWeight.Bold
                        )
                        Box(
                            modifier = Modifier
                                .background(Color(0xFFDCFCE7), RoundedCornerShape(4.dp))
                                .padding(horizontal = 4.dp, vertical = 1.dp)
                        ) {
                            Text(
                                text = "Save ~15%",
                                fontSize = 8.sp,
                                color = Color(0xFF15803D),
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(if (isTracked) SecondaryOrange.copy(alpha = 0.1f) else Color(0xFFF1F5F9))
                        .clickable { onTrackPriceClick() }
                        .padding(horizontal = 8.dp, vertical = 6.dp)
                        .testTag("track_price_row_${item.id}"),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = if (isTracked) Icons.Default.NotificationsActive else Icons.Default.Notifications,
                        contentDescription = "Track Price Icon",
                        tint = if (isTracked) SecondaryOrange else TextSlate500,
                        modifier = Modifier.size(13.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = if (isTracked) "Tracking (Initial: ₦${String.format("%,.0f", initialPrice ?: item.price)})" else "Track Price",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isTracked) SecondaryOrange else TextSlate500
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Beautiful interactive bottom action layout featuring Save For and Add to Basket / Quantity Selector
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Save Towards Goal Button (AfriSav's signature food savings target helper)
                    OutlinedButton(
                        onClick = onSaveClick,
                        modifier = Modifier
                            .weight(1f)
                            .height(36.dp),
                        contentPadding = PaddingValues(0.dp),
                        shape = RoundedCornerShape(10.dp),
                        border = BorderStroke(1.dp, SecondaryOrange)
                    ) {
                        Text("Save For", fontSize = 11.sp, color = SecondaryOrange, fontWeight = FontWeight.Bold)
                    }
                    
                    // Add to Basket / Quantity Selector
                    if (quantity == 0) {
                        Button(
                            onClick = onAddToCart,
                            modifier = Modifier
                                .weight(1.2f)
                                .height(36.dp)
                                .testTag("add_to_basket_btn_${item.id}"),
                            colors = ButtonDefaults.buttonColors(containerColor = PrimaryGreen),
                            shape = RoundedCornerShape(10.dp),
                            contentPadding = PaddingValues(0.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.ShoppingBasket,
                                    contentDescription = "Add to Basket",
                                    tint = Color.White,
                                    modifier = Modifier.size(14.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Basket +", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.White)
                            }
                        }
                    } else {
                        Row(
                            modifier = Modifier
                                .weight(1.2f)
                                .height(36.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(PrimaryGreen.copy(alpha = 0.08f))
                                .border(1.dp, PrimaryGreen, RoundedCornerShape(10.dp)),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            IconButton(
                                onClick = onRemoveFromCart,
                                modifier = Modifier.size(24.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Remove,
                                    contentDescription = "Remove One from Basket",
                                    tint = PrimaryGreen,
                                    modifier = Modifier.size(12.dp)
                                )
                            }
                            Text(
                                text = "$quantity",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = PrimaryGreen
                            )
                            IconButton(
                                onClick = onAddToCart,
                                modifier = Modifier.size(24.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Add,
                                    contentDescription = "Add One to Basket",
                                    tint = PrimaryGreen,
                                    modifier = Modifier.size(12.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

// --- COMMUNITY FOOD CIRCLES SCREEN ---
@Composable
fun CirclesScreen(
    viewModel: KoboViewModel,
    onCreateCircleClick: () -> Unit
) {
    val circles by viewModel.foodCircles.collectAsStateWithLifecycle()
    val currentUserName by viewModel.currentUserName.collectAsStateWithLifecycle()
    var selectedCircleIdForContribution by remember { mutableStateOf<Int?>(null) }
    var contributionText by remember { mutableStateOf("") }
    var showCircleConfirmDialog by remember { mutableStateOf(false) }
    var joinCodeText by remember { mutableStateOf("") }
    var activeSubTab by remember { mutableStateOf("dashboard") }

    val visibleCircles = remember(circles, currentUserName) {
        circles.filter { circle ->
            val isCreator = circle.creatorName.equals(currentUserName, ignoreCase = true) || circle.creatorName == "You"
            val membersList = circle.members.split(",").map { it.trim() }
            val isMember = membersList.any { it.equals(currentUserName, ignoreCase = true) || it == "You" }
            val coAdminsList = circle.coAdmins.split(",").map { it.trim() }
            val isCoAdmin = coAdminsList.any { it.equals(currentUserName, ignoreCase = true) || it == "You" }
            
            val inCircle = isCreator || isMember || isCoAdmin
            
            inCircle
        }
    }

    val pendingJoinRequests = remember(circles, currentUserName) {
        circles.filter { circle ->
            val isCreator = circle.creatorName.equals(currentUserName, ignoreCase = true) || circle.creatorName == "You"
            val coAdminsList = circle.coAdmins.split(",").map { it.trim() }
            val isCoAdmin = coAdminsList.any { it.equals(currentUserName, ignoreCase = true) || it == "You" }
            (isCreator || isCoAdmin) && circle.pendingRequests.isNotBlank()
        }.flatMap { circle ->
            circle.pendingRequests.split(",")
                .map { it.trim() }
                .filter { it.isNotEmpty() }
                .map { requester -> Pair(circle, requester) }
        }
    }

    val headerGradientColors = if (isDark) listOf(Color(0xFF0D2D18), Color(0xFF121212)) else listOf(Color(0xFFE2F0D9), Color(0xFFF5F8F6))

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(SoftBackground)
    ) {
        // Main Header with a beautiful description of invite-only groups
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.verticalGradient(
                        colors = headerGradientColors
                    )
                )
                .statusBarsPadding()
                .padding(start = 20.dp, end = 20.dp, top = 18.dp, bottom = 20.dp)
        ) {
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            "Invite-Only Food Circles", 
                            fontSize = 21.sp, 
                            fontWeight = FontWeight.Bold, 
                            color = Color(0xFF1E293B)
                        )
                        Text(
                            "FPL-Style collaborative buying with friends & family", 
                            fontSize = 12.sp, 
                            color = Color(0xFF475569)
                        )
                    }
                    Button(
                        onClick = onCreateCircleClick,
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0A8F3D)),
                        shape = RoundedCornerShape(12.dp),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp)
                    ) {
                        Icon(Icons.Default.GroupAdd, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Create Circle", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                Card(
                    colors = CardDefaults.cardColors(containerColor = SurfaceBg),
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Text(
                            "Join an Existing Circle 👥", 
                            fontWeight = FontWeight.Bold, 
                            fontSize = 13.sp, 
                            color = Color(0xFF1E293B)
                        )
                        Text(
                            "Enter the invite code shared by your friend, neighbor, or family member to join their group save.", 
                            fontSize = 11.sp, 
                            color = Color(0xFF64748B)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            OutlinedTextField(
                                value = joinCodeText,
                                onValueChange = { joinCodeText = it.uppercase() },
                                placeholder = { Text("e.g. KB-M12RICE", fontSize = 12.sp) },
                                singleLine = true,
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier
                                    .weight(1f)
                                    .height(52.dp),
                                colors = TextFieldDefaults.colors(
                                    focusedContainerColor = Color.White,
                                    unfocusedContainerColor = Color.White
                                )
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Button(
                                onClick = {
                                    if (joinCodeText.isNotBlank()) {
                                        viewModel.joinCircle(joinCodeText)
                                        joinCodeText = ""
                                    }
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0A8F3D)),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.height(48.dp)
                            ) {
                                Text("Join Group", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                            }
                        }
                    }
                }
            }
        }

        // Beautiful Sub-Tab Selector
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp)
                .background(BorderSlate100, RoundedCornerShape(12.dp))
                .padding(4.dp)
        ) {
            val tabs = listOf("dashboard" to "Circles Dashboard", "pools" to "Contribution Pools")
            tabs.forEach { (tabId, tabName) ->
                val isSelected = activeSubTab == tabId
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(8.dp))
                        .background(if (isSelected) MaterialTheme.colorScheme.surface else Color.Transparent)
                        .clickable { activeSubTab = tabId }
                        .padding(vertical = 10.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = tabName,
                            fontSize = 12.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                            color = if (isSelected) PrimaryGreen else TextSlate500
                        )
                        if (tabId == "dashboard" && pendingJoinRequests.isNotEmpty()) {
                            Spacer(modifier = Modifier.width(6.dp))
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFFE11D48))
                            )
                        }
                    }
                }
            }
        }

        if (activeSubTab == "dashboard") {
            // --- DASHBOARD VIEW ---
            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 16.dp),
                contentPadding = PaddingValues(bottom = 16.dp)
            ) {
                // Simulation Controls Card
                item {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 12.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFEFF6FF)),
                        border = BorderStroke(1.dp, Color(0xFFBFDBFE)),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.NotificationsActive,
                                    contentDescription = null,
                                    tint = Color(0xFF1D4ED8),
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "In-App Notification & Invite Simulator",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF1E3A8A)
                                )
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Test the automated alerts for circle contributions and secure invitations instantly.",
                                fontSize = 11.sp,
                                color = Color(0xFF1E3050)
                            )
                            Spacer(modifier = Modifier.height(10.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Button(
                                    onClick = { viewModel.triggerSimulationContributionDue() },
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2563EB)),
                                    shape = RoundedCornerShape(8.dp),
                                    modifier = Modifier.weight(1f).height(36.dp),
                                    contentPadding = PaddingValues(horizontal = 4.dp, vertical = 2.dp)
                                ) {
                                    Text("Simulate Due Alert 💰", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                }
                                Button(
                                    onClick = { viewModel.triggerSimulationInviteReceived() },
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF7C3AED)),
                                    shape = RoundedCornerShape(8.dp),
                                    modifier = Modifier.weight(1f).height(36.dp),
                                    contentPadding = PaddingValues(horizontal = 4.dp, vertical = 2.dp)
                                ) {
                                    Text("Simulate Invite ✉️", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }

                // Quick Statistics Cards
                item {
                    Text(
                        "Performance & Saving Statistics",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF475569),
                        modifier = Modifier.padding(vertical = 12.dp)
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        // Total Circles Card
                        Card(
                            colors = CardDefaults.cardColors(containerColor = SurfaceBg),
                            border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.weight(1.0f)
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Icon(Icons.Default.Groups, contentDescription = null, tint = Color(0xFF0A8F3D), modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.height(6.dp))
                                Text("Total Circles", fontSize = 10.sp, color = Color(0xFF64748B))
                                Text("${visibleCircles.size}", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1E293B))
                            }
                        }

                        // Connected Savers Card
                        Card(
                            colors = CardDefaults.cardColors(containerColor = SurfaceBg),
                            border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.weight(1.1f)
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Icon(Icons.Default.Person, contentDescription = null, tint = Color(0xFF0A8F3D), modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.height(6.dp))
                                Text("Group Savers", fontSize = 10.sp, color = Color(0xFF64748B))
                                Text("${visibleCircles.sumOf { it.membersCount }} Members", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1E293B))
                            }
                        }

                        // Combined Savings Pool Card
                        Card(
                            colors = CardDefaults.cardColors(containerColor = SurfaceBg),
                            border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.weight(1.3f)
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Icon(Icons.Default.Savings, contentDescription = null, tint = Color(0xFF0A8F3D), modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.height(6.dp))
                                Text("Pooled Funds", fontSize = 10.sp, color = Color(0xFF64748B))
                                Text("₦${String.format("%,.0f", visibleCircles.sumOf { it.currentAmount })}", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color(0xFF0A8F3D))
                            }
                        }
                    }
                }

                if (pendingJoinRequests.isNotEmpty()) {
                    item {
                        Text(
                            "Pending Join Requests (${pendingJoinRequests.size})",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFE11D48),
                            modifier = Modifier.padding(top = 20.dp, bottom = 12.dp)
                        )
                    }
                    items(pendingJoinRequests) { (circle, requester) ->
                        Card(
                            colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF1F2)),
                            border = BorderStroke(1.dp, Color(0xFFFECDD3)),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(36.dp)
                                            .clip(CircleShape)
                                            .background(Color(0xFFFFE4E6)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Person,
                                            contentDescription = null,
                                            tint = Color(0xFFE11D48),
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Column {
                                        Text(
                                            text = requester,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 13.sp,
                                            color = Color(0xFF1E293B)
                                        )
                                        Text(
                                            text = "Wants to join \"${circle.title}\"",
                                            fontSize = 10.sp,
                                            color = Color(0xFF64748B)
                                        )
                                    }
                                }

                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    // Reject Button
                                    Button(
                                        onClick = { viewModel.rejectJoinRequest(circle.id, requester) },
                                        colors = ButtonDefaults.buttonColors(containerColor = SurfaceBg),
                                        border = BorderStroke(1.dp, Color(0xFFFDA4AF)),
                                        shape = RoundedCornerShape(8.dp),
                                        contentPadding = PaddingValues(horizontal = 10.dp),
                                        modifier = Modifier.height(30.dp)
                                    ) {
                                        Text("Reject", color = Color(0xFFE11D48), fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                    }

                                    // Approve Button
                                    Button(
                                        onClick = { viewModel.approveJoinRequest(circle.id, requester) },
                                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF10B981)),
                                        shape = RoundedCornerShape(8.dp),
                                        contentPadding = PaddingValues(horizontal = 10.dp),
                                        modifier = Modifier.height(30.dp)
                                    ) {
                                        Text("Approve", color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        }
                    }
                }

                item {
                    Text(
                        "Circles Directory & Privacy Status",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF475569),
                        modifier = Modifier.padding(top = 20.dp, bottom = 12.dp)
                    )
                }

                if (visibleCircles.isEmpty()) {
                    item {
                        EmptyStateCard(
                            message = "No Buying Circles Registered",
                            subMessage = "Be the pioneer! Create a community buying circle and invite friends, family, or neighbors to pool food expenses.",
                            btnText = "Create Buying Circle",
                            onClick = onCreateCircleClick,
                            icon = Icons.Default.Groups,
                            iconColor = Color(0xFF2563EB)
                        )
                    }
                } else {
                    items(visibleCircles) { circle ->
                        val isCreator = circle.creatorName.equals(currentUserName, ignoreCase = true) || circle.creatorName == "You"
                        val clipboardManager = LocalClipboardManager.current
                        var isCopied by remember { mutableStateOf(false) }

                        LaunchedEffect(isCopied) {
                            if (isCopied) {
                                delay(2000)
                                isCopied = false
                            }
                        }

                        Card(
                            colors = CardDefaults.cardColors(containerColor = SurfaceBg),
                            shape = RoundedCornerShape(12.dp),
                            border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp)
                        ) {
                            Column(modifier = Modifier.padding(14.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = circle.title,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 14.sp,
                                            color = Color(0xFF1E293B)
                                        )
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            modifier = Modifier.padding(top = 4.dp)
                                        ) {
                                            // Your Role Badge
                                            Box(
                                                modifier = Modifier
                                                    .clip(RoundedCornerShape(4.dp))
                                                    .background(if (isCreator) Color(0xFFFEF3C7) else Color(0xFFE2F0D9))
                                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                                            ) {
                                                Text(
                                                    text = if (isCreator) "👑 Creator/Owner" else "👥 Joined Member",
                                                    fontSize = 9.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = if (isCreator) Color(0xFFB45309) else Color(0xFF15803D)
                                                )
                                            }

                                            Spacer(modifier = Modifier.width(8.dp))

                                            // Privacy Badge
                                            Row(
                                                verticalAlignment = Alignment.CenterVertically,
                                                modifier = Modifier
                                                    .clip(RoundedCornerShape(4.dp))
                                                    .background(if (circle.isPrivate) Color(0xFFFFF1F2) else Color(0xFFE2F0D9))
                                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                                            ) {
                                                Icon(
                                                    imageVector = if (circle.isPrivate) Icons.Default.Lock else Icons.Default.Public,
                                                    contentDescription = null,
                                                    tint = if (circle.isPrivate) Color(0xFFE11D48) else Color(0xFF15803D),
                                                    modifier = Modifier.size(9.dp)
                                                )
                                                Spacer(modifier = Modifier.width(3.dp))
                                                Text(
                                                    text = if (circle.isPrivate) "Private" else "Public",
                                                    fontSize = 9.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = if (circle.isPrivate) Color(0xFFE11D48) else Color(0xFF15803D)
                                                )
                                            }
                                        }
                                    }

                                    // Member Count Badge
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(Color(0xFFF1F5F9))
                                            .padding(horizontal = 8.dp, vertical = 4.dp)
                                    ) {
                                        Icon(Icons.Default.Group, contentDescription = null, tint = Color(0xFF475569), modifier = Modifier.size(12.dp))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("${circle.membersCount} Savers", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFF475569))
                                    }
                                }

                                Divider(modifier = Modifier.padding(vertical = 10.dp), color = Color(0xFFF1F5F9))

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    // Invite code section
                                    if (!circle.isPrivate || isCreator) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(6.dp))
                                            .background(Color(0xFFF1F5F9))
                                            .clickable {
                                                clipboardManager.setText(AnnotatedString(circle.circleCode))
                                                isCopied = true
                                            }
                                            .padding(horizontal = 8.dp, vertical = 4.dp)
                                    ) {
                                        Text("Code: ", fontSize = 10.sp, color = Color(0xFF64748B))
                                        Text(circle.circleCode, fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color(0xFF0A8F3D))
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Icon(
                                            imageVector = if (isCopied) Icons.Default.Check else Icons.Default.ContentCopy,
                                            contentDescription = "Copy",
                                            tint = if (isCopied) Color(0xFF15803D) else Color(0xFF64748B),
                                            modifier = Modifier.size(11.dp)
                                        )
                                    }
                                    } else {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(6.dp))
                                                .background(Color(0xFFFFF1F2))
                                                .padding(horizontal = 8.dp, vertical = 4.dp)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Lock,
                                                contentDescription = null,
                                                tint = Color(0xFFE11D48),
                                                modifier = Modifier.size(10.dp)
                                            )
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text(
                                                text = "Private (Code Hidden)",
                                                fontSize = 10.sp,
                                                fontWeight = FontWeight.Medium,
                                                color = Color(0xFFE11D48)
                                            )
                                        }
                                    }

                                    // Navigate / Contribute button
                                    Button(
                                        onClick = { activeSubTab = "pools" },
                                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0A8F3D)),
                                        shape = RoundedCornerShape(8.dp),
                                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                                        modifier = Modifier.height(32.dp)
                                    ) {
                                        Text("Manage Pool ➔", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                    }
                                }

                                var showMembers by remember { mutableStateOf(false) }
                                
                                Spacer(modifier = Modifier.height(10.dp))
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable { showMembers = !showMembers }
                                        .padding(vertical = 4.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.Center
                                ) {
                                    Text(
                                        text = if (showMembers) "Hide Members & Admin Roles ▲" else "View Members & Admin Roles ▼",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = Color(0xFF475569)
                                    )
                                }

                                if (showMembers) {
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text(
                                        "Circle Members",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFF64748B),
                                        modifier = Modifier.padding(bottom = 6.dp)
                                    )
                                    
                                    val membersList = circle.members.split(",").map { it.trim() }.filter { it.isNotEmpty() }
                                    val coAdminsList = circle.coAdmins.split(",").map { it.trim() }.filter { it.isNotEmpty() }
                                    
                                    membersList.forEach { member ->
                                        val isMemberCreator = member.equals(circle.creatorName, ignoreCase = true) || (member == "You" && circle.creatorName.equals(currentUserName, ignoreCase = true))
                                        val isMemberCoAdmin = coAdminsList.any { it.equals(member, ignoreCase = true) }
                                        
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(vertical = 4.dp)
                                                .background(Color(0xFFF8FAFC), RoundedCornerShape(6.dp))
                                                .padding(horizontal = 8.dp, vertical = 6.dp),
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.SpaceBetween
                                        ) {
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                val initials = member.split(" ")
                                                    .filter { it.isNotBlank() }
                                                    .take(2)
                                                    .map { it.first().uppercaseChar() }
                                                    .joinToString("")
                                                    .ifBlank { member.take(1).uppercase() }
                                                val avatarColors = listOf(
                                                    Color(0xFFE11D48), // Rose
                                                    Color(0xFF2563EB), // Blue
                                                    Color(0xFF059669), // Emerald Green
                                                    Color(0xFFD97706), // Amber
                                                    Color(0xFF7C3AED), // Purple
                                                    Color(0xFFDB2777), // Pink
                                                    Color(0xFF0891B2), // Cyan
                                                    Color(0xFFEA580C)  // Orange
                                                )
                                                val colorIndex = Math.abs(member.hashCode()) % avatarColors.size
                                                val bgColor = avatarColors[colorIndex]
                                                Box(
                                                    modifier = Modifier
                                                        .size(24.dp)
                                                        .clip(CircleShape)
                                                        .background(bgColor),
                                                    contentAlignment = Alignment.Center
                                                ) {
                                                    Text(
                                                        text = initials,
                                                        fontSize = 9.sp,
                                                        fontWeight = FontWeight.Bold,
                                                        color = Color.White
                                                    )
                                                }
                                                Spacer(modifier = Modifier.width(6.dp))
                                                Text(
                                                    text = member,
                                                    fontSize = 12.sp,
                                                    fontWeight = FontWeight.Medium,
                                                    color = Color(0xFF334155)
                                                )
                                                Spacer(modifier = Modifier.width(6.dp))
                                                
                                                // Role label
                                                if (isMemberCreator) {
                                                    Box(
                                                        modifier = Modifier
                                                            .clip(RoundedCornerShape(4.dp))
                                                            .background(Color(0xFFFEF3C7))
                                                            .padding(horizontal = 4.dp, vertical = 1.dp)
                                                    ) {
                                                        Text("Owner", fontSize = 8.sp, fontWeight = FontWeight.Bold, color = Color(0xFFB45309))
                                                    }
                                                } else if (isMemberCoAdmin) {
                                                    Box(
                                                        modifier = Modifier
                                                            .clip(RoundedCornerShape(4.dp))
                                                            .background(Color(0xFFEFF6FF))
                                                            .padding(horizontal = 4.dp, vertical = 1.dp)
                                                    ) {
                                                        Text("🛡️ Co-Admin", fontSize = 8.sp, fontWeight = FontWeight.Bold, color = Color(0xFF2563EB))
                                                    }
                                                }
                                            }
                                            
                                            // Action if current user is Creator/Owner and is not looking at themselves
                                            if (isCreator && !isMemberCreator) {
                                                TextButton(
                                                    onClick = { viewModel.toggleCoAdminRole(circle.id, member) },
                                                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                                                    modifier = Modifier.height(24.dp)
                                                ) {
                                                    Text(
                                                        text = if (isMemberCoAdmin) "Revoke Co-Admin" else "Make Co-Admin",
                                                        fontSize = 10.sp,
                                                        fontWeight = FontWeight.Bold,
                                                        color = if (isMemberCoAdmin) Color(0xFFEF4444) else Color(0xFF0A8F3D)
                                                    )
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
        } else {
            // --- DETAILED POOLS VIEW ---
            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 16.dp),
                contentPadding = PaddingValues(bottom = 16.dp)
            ) {
                item {
                    Text(
                        "Your Active Food Circles", 
                        fontSize = 14.sp, 
                        fontWeight = FontWeight.Bold, 
                        color = Color(0xFF475569),
                        modifier = Modifier.padding(vertical = 12.dp)
                    )
                }
                if (visibleCircles.isEmpty()) {
                    item {
                        EmptyStateCard(
                            message = "No Active Buying Circles Yet",
                            subMessage = "You haven't joined or created any active buying circles. Joint-purchases of bulk foodstuffs can save you up to 25%!",
                            btnText = "Create Buying Circle",
                            onClick = onCreateCircleClick,
                            icon = Icons.Default.Groups,
                            iconColor = Color(0xFF2563EB)
                        )
                    }
                } else {
                    items(visibleCircles) { circle ->
                        FoodCircleCard(
                            circle = circle,
                            currentUserName = currentUserName,
                            onContributeClick = { selectedCircleIdForContribution = circle.id },
                            onSimulateClick = { viewModel.simulateCircleContributions(circle.id) }
                        )
                        Spacer(modifier = Modifier.height(14.dp))
                    }
                }
            }
        }
    }

    selectedCircleIdForContribution?.let { circleId ->
        val circle = visibleCircles.find { it.id == circleId } ?: return@let
        AlertDialog(
            onDismissRequest = { selectedCircleIdForContribution = null },
            title = { Text("Contribute to Circle") },
            text = {
                Column {
                    Text("Enter amount to contribute from your available balance to the bulk savings target:", fontSize = 14.sp)
                    Spacer(modifier = Modifier.height(16.dp))
                    OutlinedTextField(
                        value = contributionText,
                        onValueChange = { contributionText = it },
                        label = { Text("Contribution (₦)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val amt = contributionText.toDoubleOrNull() ?: 0.0
                        if (amt > 0) {
                            showCircleConfirmDialog = true
                        } else {
                            selectedCircleIdForContribution = null
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0A8F3D))
                ) {
                    Text("Contribute")
                }
            },
            dismissButton = {
                TextButton(onClick = { selectedCircleIdForContribution = null }) {
                    Text("Cancel")
                }
            }
        )
    }

    if (showCircleConfirmDialog && selectedCircleIdForContribution != null) {
        val circleId = selectedCircleIdForContribution!!
        val circle = visibleCircles.find { it.id == circleId }
        if (circle != null) {
            val amt = contributionText.toDoubleOrNull() ?: 0.0
            AlertDialog(
                onDismissRequest = { showCircleConfirmDialog = false },
                title = { Text("Confirm Transfer") },
                text = {
                    Text("Are you sure you want to deposit/transfer ₦${String.format("%,.2f", amt)} into the community bulk savings circle \"${circle.title}\"?")
                },
                confirmButton = {
                    Button(
                        onClick = {
                            viewModel.contributeToCircle(circleId, amt)
                            contributionText = ""
                            selectedCircleIdForContribution = null
                            showCircleConfirmDialog = false
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0A8F3D)),
                        modifier = Modifier.testTag("confirm_transfer_button")
                    ) {
                        Text("Confirm Transfer")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showCircleConfirmDialog = false }) {
                        Text("Cancel")
                    }
                }
            )
        }
    }
}

@Composable
fun FoodCircleCard(
    circle: FoodCircle,
    currentUserName: String,
    onContributeClick: () -> Unit,
    onSimulateClick: () -> Unit
) {
    val progress = (circle.currentAmount / circle.targetAmount).toFloat()
    val clipboardManager = LocalClipboardManager.current
    var isCopied by remember { mutableStateOf(false) }
    
    LaunchedEffect(isCopied) {
        if (isCopied) {
            delay(2000)
            isCopied = false
        }
    }
    
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = SurfaceBg),
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(1.dp, Color(0xFFE2E8F0))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Row(modifier = Modifier.weight(1f), verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(46.dp)
                            .background(Color(0xFF0A8F3D).copy(alpha = 0.12f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.Groups, contentDescription = null, tint = Color(0xFF0A8F3D), modifier = Modifier.size(24.dp))
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(text = circle.title, fontWeight = FontWeight.Bold, fontSize = 15.sp, color = Color(0xFF1E293B))
                        Text(text = "Created by ${circle.creatorName.ifBlank { "You" }}", fontSize = 11.sp, color = Color(0xFF64748B))
                    }
                }

                if (circle.isWholesaleUnlocked) {
                    Card(colors = CardDefaults.cardColors(containerColor = Color(0xFFE2F0D9))) {
                        Text(
                            "UNLOCKED 🎉",
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF0A8F3D),
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                } else {
                    Card(colors = CardDefaults.cardColors(containerColor = Color(0xFFFEF3C7))) {
                        Text(
                            "POOLING",
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFD97706),
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))
            Text(text = circle.description, fontSize = 12.sp, color = Color(0xFF475569), lineHeight = 18.sp)
            Spacer(modifier = Modifier.height(10.dp))

            // PROMINENT INVITE CODE BADGE
            val isCreator = circle.creatorName.equals(currentUserName, ignoreCase = true) || circle.creatorName == "You"
            val membersList = circle.members.split(",").map { it.trim() }
            val isMember = membersList.any { it.equals(currentUserName, ignoreCase = true) || it == "You" }
            val inCircle = isCreator || isMember
            if (inCircle) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color(0xFFF1F5F9))
                        .clickable {
                            clipboardManager.setText(AnnotatedString(circle.circleCode))
                            isCopied = true
                        }
                        .padding(horizontal = 10.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Default.Share, 
                            contentDescription = null, 
                            tint = Color(0xFF475569), 
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Group Invite Code:", 
                            fontSize = 11.sp, 
                            fontWeight = FontWeight.Medium, 
                            color = Color(0xFF475569)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = circle.circleCode, 
                            fontSize = 12.sp, 
                            fontWeight = FontWeight.Bold, 
                            color = Color(0xFF0A8F3D)
                        )
                    }
                    
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .background(if (isCopied) Color(0xFFDCFCE7) else Color.Transparent)
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = if (isCopied) "Copied!" else "Copy",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isCopied) Color(0xFF15803D) else Color(0xFF0A8F3D)
                        )
                        Icon(
                            imageVector = if (isCopied) Icons.Default.Check else Icons.Default.ContentCopy, 
                            contentDescription = "Copy code", 
                            tint = if (isCopied) Color(0xFF15803D) else Color(0xFF0A8F3D), 
                            modifier = Modifier.size(12.dp)
                        )
                    }
                }
            } else {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color(0xFFFFF1F2))
                        .padding(horizontal = 10.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Default.Lock, 
                            contentDescription = null, 
                            tint = Color(0xFFE11D48), 
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Private Food Circle", 
                            fontSize = 11.sp, 
                            fontWeight = FontWeight.Bold, 
                            color = Color(0xFFE11D48)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "(Invite-only code hidden)", 
                            fontSize = 10.sp, 
                            color = Color(0xFFFDA4AF)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // MEMBERS LIST (Redesigned)
            val cleanMembersList = membersList.filter { it.isNotEmpty() }
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFFF8FAFC), RoundedCornerShape(12.dp))
                    .padding(horizontal = 12.dp, vertical = 10.dp)
                    .border(1.dp, Color(0xFFE2E8F0), RoundedCornerShape(12.dp))
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Group, 
                            contentDescription = null, 
                            tint = Color(0xFF0A8F3D), 
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Group Savers (${circle.membersCount})", 
                            fontSize = 12.sp, 
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF334155)
                        )
                    }
                    
                    OverlappingMembersAvatarPile(
                        members = cleanMembersList,
                        avatarSize = 24.dp,
                        maxVisible = 4
                    )
                }
                
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = if (circle.members.isNotBlank()) circle.members else "You",
                    fontSize = 11.sp,
                    color = Color(0xFF64748B),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(text = "Pooled: ₦${String.format("%,.0f", circle.currentAmount)}", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFF0A8F3D))
                Text(text = "Target: ₦${String.format("%,.0f", circle.targetAmount)}", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFFFF8C00))
            }

            Spacer(modifier = Modifier.height(6.dp))

            LinearProgressIndicator(
                progress = if (progress > 1f) 1f else progress,
                color = Color(0xFF0A8F3D),
                trackColor = Color(0xFFF1F5F9),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
                    .clip(RoundedCornerShape(4.dp))
            )

            Spacer(modifier = Modifier.height(14.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedButton(
                    onClick = onSimulateClick,
                    border = BorderStroke(1.dp, Color(0xFF0A8F3D)),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.weight(1.1f)
                ) {
                    Icon(Icons.Default.Group, contentDescription = null, tint = Color(0xFF0A8F3D), modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Friend Joint Save 👥", fontSize = 11.sp, color = Color(0xFF0A8F3D))
                }

                Button(
                    onClick = onContributeClick,
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0A8F3D)),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.weight(0.9f)
                ) {
                    Icon(Icons.Default.Savings, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Contribute", fontSize = 11.sp)
                }
            }
        }
    }
}

// --- MAMA BASKET ASSISTANT CHAT SCREEN ---
@Composable
fun AssistantScreen(
    viewModel: KoboViewModel
) {
    val msgs by viewModel.chatMessages.collectAsStateWithLifecycle()
    val isLoading by viewModel.isChatLoading.collectAsStateWithLifecycle()
    var textInput by remember { mutableStateOf("") }
    val lazyListState = rememberLazyListState()
    var selectedRecipeForPlanner by remember { mutableStateOf<Map<String, Any>?>(null) }
    var showVoiceDialog by remember { mutableStateOf(false) }
    val context = LocalContext.current

    val recordAudioPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        onResult = { isGranted ->
            if (isGranted) {
                showVoiceDialog = true
            } else {
                viewModel.triggerUiEvent("Microphone permission is required for voice commands.")
            }
        }
    )

    // Auto-scroll chat to latest message
    LaunchedEffect(key1 = msgs.size, key2 = isLoading) {
        if (msgs.isNotEmpty()) {
            lazyListState.animateScrollToItem(msgs.size - 1)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(SoftBackground)
    ) {
        // Mama Header Bar
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFF0A8F3D))
                .statusBarsPadding()
                .padding(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .background(Color.White.copy(alpha = 0.2f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("🥑", fontSize = 24.sp)
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text("Mama Olufunke (AI Advisor)", fontWeight = FontWeight.Bold, fontSize = 15.sp, color = Color.White)
                        Text("Online • Wise Food Savings Helper", fontSize = 11.sp, color = Color.White.copy(alpha = 0.8f))
                    }
                }
                IconButton(onClick = { viewModel.clearChat() }) {
                    Icon(Icons.Default.DeleteSweep, contentDescription = "Clear Chat", tint = Color.White)
                }
            }
        }

        // Chat bubbles
        LazyColumn(
            state = lazyListState,
            modifier = Modifier
                .weight(1f)
                .padding(horizontal = 16.dp)
        ) {
            items(msgs) { msg ->
                ChatBubble(msg = msg)
                Spacer(modifier = Modifier.height(8.dp))
            }
            if (isLoading) {
                item {
                    MamaLoadingBubble()
                }
            }
        }

        // --- FEATURE 2: MAMA BASKET RECIPE INGREDIENT PLANNER ---
        if (selectedRecipeForPlanner != null) {
            val recipe = selectedRecipeForPlanner!!
            val recipeName = recipe["name"] as String
            val recipeEmoji = recipe["emoji"] as String
            val recipeIngredients = recipe["ingredients"] as List<String>
            val recipeDesc = recipe["desc"] as String
            
            AlertDialog(
                onDismissRequest = { selectedRecipeForPlanner = null },
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(recipeEmoji, fontSize = 24.sp)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(recipeName, fontWeight = FontWeight.Bold)
                    }
                },
                text = {
                    Column {
                        Text(recipeDesc, fontSize = 13.sp, color = Color(0xFF64748B))
                        Spacer(modifier = Modifier.height(12.dp))
                        Text("Mama's Smart Ingredient Matching:", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = Color(0xFF1E293B))
                        Spacer(modifier = Modifier.height(6.dp))
                        
                        recipeIngredients.forEach { ing ->
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(vertical = 4.dp)
                            ) {
                                Icon(Icons.Default.CheckCircle, contentDescription = null, tint = Color(0xFF0A8F3D), modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(text = ing, fontSize = 13.sp, color = Color(0xFF334155))
                            }
                        }
                        Spacer(modifier = Modifier.height(12.dp))
                        Text("💡 One-click adds all matching items in our local markets directly into your secure Food Basket.", fontSize = 11.sp, color = Color(0xFF0A8F3D))
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            viewModel.addRecipeIngredientsToCart(recipeName, recipeIngredients)
                            selectedRecipeForPlanner = null
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0A8F3D))
                    ) {
                        Icon(Icons.Default.ShoppingCart, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Add Recipe to Basket")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { selectedRecipeForPlanner = null }) {
                        Text("Cancel")
                    }
                }
            )
        }

        Text(
            text = "🥘 Mama's Smart Recipe Planner",
            fontSize = 13.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF1E293B),
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
        )
        val recipes = listOf(
            mapOf("name" to "Party Jollof Rice", "emoji" to "🍛", "ingredients" to listOf("Rice", "Tomatoes", "Fish"), "desc" to "Rich, spicy classic Nigerian rice feast"),
            mapOf("name" to "Egusi & Yam Stew", "emoji" to "🍲", "ingredients" to listOf("Yam", "Beans", "Fish"), "desc" to "Hearty melon seed soup with yam slices"),
            mapOf("name" to "Fish Pepper Soup", "emoji" to "🥣", "ingredients" to listOf("Fish", "Tomatoes"), "desc" to "Hot, aromatic herbal local soup"),
            mapOf("name" to "Yam Porridge Special", "emoji" to "🍠", "ingredients" to listOf("Yam", "Tomatoes", "Fish"), "desc" to "Tender slow-cooked yam pottage")
        )
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 4.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            items(recipes) { rec ->
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color(0xFFE8F5E9))
                        .border(1.dp, Color(0xFFC8E6C9), RoundedCornerShape(12.dp))
                        .clickable { selectedRecipeForPlanner = rec }
                        .padding(horizontal = 12.dp, vertical = 8.dp)
                ) {
                    Text(rec["emoji"] as String, fontSize = 16.sp)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(rec["name"] as String, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFF0A8F3D))
                }
            }
        }
        Spacer(modifier = Modifier.height(4.dp))

        // Quick Suggestion Chips
        val suggestions = listOf(
            "How do I save for 50kg rice?",
            "Cheapest tomatoes basket?",
            "What is a Food Circle?",
            "Auto-save plans"
        )
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 6.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            items(suggestions) { sug ->
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(SurfaceBg)
                        .border(1.dp, Color(0xFFE2E8F0), RoundedCornerShape(12.dp))
                        .clickable { viewModel.sendMessageToMama(sug) }
                        .padding(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Text(sug, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFF0A8F3D))
                }
            }
        }

        // Input Field Bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(SurfaceBg)
                .padding(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedTextField(
                value = textInput,
                onValueChange = { textInput = it },
                placeholder = { Text("Ask Mama anything about food budgeting...", fontSize = 13.sp) },
                singleLine = true,
                modifier = Modifier
                    .weight(1f)
                    .testTag("assistant_text_input"),
                shape = RoundedCornerShape(20.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Color(0xFF0A8F3D)
                ),
                trailingIcon = {
                    IconButton(
                        onClick = {
                            val hasPermission = ContextCompat.checkSelfPermission(
                                context,
                                android.Manifest.permission.RECORD_AUDIO
                            ) == PackageManager.PERMISSION_GRANTED

                            if (hasPermission) {
                                showVoiceDialog = true
                            } else {
                                recordAudioPermissionLauncher.launch(android.Manifest.permission.RECORD_AUDIO)
                            }
                        },
                        modifier = Modifier.testTag("assistant_mic_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Mic,
                            contentDescription = "Voice command microphone",
                            tint = Color(0xFF0A8F3D)
                        )
                    }
                }
            )
            Spacer(modifier = Modifier.width(8.dp))
            IconButton(
                onClick = {
                    if (textInput.trim().isNotEmpty()) {
                        viewModel.sendMessageToMama(textInput)
                        textInput = ""
                    }
                },
                modifier = Modifier
                    .background(Color(0xFF0A8F3D), CircleShape)
                    .size(46.dp)
                    .testTag("assistant_send_button")
            ) {
                Icon(Icons.AutoMirrored.Filled.Send, contentDescription = "Send", tint = Color.White, modifier = Modifier.size(20.dp))
            }
        }

        if (showVoiceDialog) {
            VoiceCommandDialog(
                onDismiss = { showVoiceDialog = false },
                onSpeechResult = { resultText ->
                    showVoiceDialog = false
                    if (resultText.trim().isNotEmpty()) {
                        viewModel.sendMessageToMama(resultText)
                    }
                }
            )
        }
    }
}

@Composable
fun ChatBubble(msg: ChatMessage) {
    val alignRight = msg.isUser
    val bubbleColor = if (alignRight) Color(0xFF0A8F3D) else Color.White
    val textColor = if (alignRight) Color.White else Color(0xFF1E293B)
    val alignment = if (alignRight) Alignment.End else Alignment.Start

    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = alignment
    ) {
        Row(
            verticalAlignment = Alignment.Bottom,
            horizontalArrangement = if (alignRight) Arrangement.End else Arrangement.Start
        ) {
            if (!alignRight) {
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .background(Color(0xFFFF8C00).copy(alpha = 0.15f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text("👵", fontSize = 16.sp)
                }
                Spacer(modifier = Modifier.width(6.dp))
            }

            Card(
                colors = CardDefaults.cardColors(containerColor = bubbleColor),
                shape = RoundedCornerShape(
                    topStart = 16.dp,
                    topEnd = 16.dp,
                    bottomStart = if (alignRight) 16.dp else 4.dp,
                    bottomEnd = if (alignRight) 4.dp else 16.dp
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                modifier = Modifier.widthIn(max = 280.dp)
            ) {
                Text(
                    text = msg.text,
                    color = textColor,
                    fontSize = 13.sp,
                    lineHeight = 18.sp,
                    modifier = Modifier.padding(12.dp)
                )
            }
        }
    }
}

@Composable
fun MamaLoadingBubble() {
    Row(
        verticalAlignment = Alignment.Bottom,
        horizontalArrangement = Arrangement.Start
    ) {
        Box(
            modifier = Modifier
                .size(32.dp)
                .background(Color(0xFFFF8C00).copy(alpha = 0.15f), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Text("👵", fontSize = 16.sp)
        }
        Spacer(modifier = Modifier.width(6.dp))
        Card(
            colors = CardDefaults.cardColors(containerColor = SurfaceBg),
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier.width(80.dp)
        ) {
            Text(
                "Thinking...",
                fontSize = 12.sp,
                color = Color.LightGray,
                modifier = Modifier.padding(12.dp),
                fontStyle = androidx.compose.ui.text.font.FontStyle.Italic
            )
        }
    }
}

// --- HELPER DIALOGS ---

@Composable
fun CardVisualizer(
    cardNumber: String,
    expiryDate: String,
    cvv: String,
    cardHolder: String
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(165.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(
                Brush.linearGradient(
                    colors = listOf(
                        Color(0xFF0F172A), // Slate 900
                        Color(0xFF1E293B), // Slate 800
                        Color(0xFF0A8F3D)  // Kobo Green
                    )
                )
            )
            .padding(16.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "AfriSav Smart Card",
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp
                )
                Box(
                    modifier = Modifier
                        .size(32.dp, 24.dp)
                        .clip(RoundedCornerShape(4.dp))
                        .background(Color(0xFFF59E0B))
                )
            }

            val formattedCardNumber = remember(cardNumber) {
                val digitsOnly = cardNumber.replace(" ", "")
                val chunks = digitsOnly.chunked(4)
                if (chunks.isEmpty()) "•••• •••• •••• ••••"
                else {
                    val formatted = chunks.joinToString(" ")
                    val remainingDigits = 16 - digitsOnly.length
                    val dots = "•".repeat(remainingDigits).chunked(4).joinToString(" ")
                    if (dots.isNotEmpty()) "$formatted $dots" else formatted
                }
            }

            Text(
                text = formattedCardNumber,
                color = Color.White,
                fontWeight = FontWeight.SemiBold,
                fontSize = 18.sp,
                letterSpacing = 2.sp,
                modifier = Modifier.padding(vertical = 8.dp)
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "CARD HOLDER",
                        color = Color.White.copy(alpha = 0.6f),
                        fontSize = 8.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = if (cardHolder.isBlank()) "YOUR NAME" else cardHolder.uppercase(),
                        color = Color.White,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            text = "EXPIRES",
                            color = Color.White.copy(alpha = 0.6f),
                            fontSize = 8.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = if (expiryDate.isBlank()) "MM/YY" else expiryDate,
                            color = Color.White,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            text = "CVV",
                            color = Color.White.copy(alpha = 0.6f),
                            fontSize = 8.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = if (cvv.isBlank()) "•••" else "•".repeat(cvv.length),
                            color = Color.White,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun FundWalletDialog(
    currentUserName: String,
    onDismiss: () -> Unit,
    onConfirm: (Double) -> Unit
) {
    var amountText by remember { mutableStateOf("") }
    var selectedMethod by remember { mutableStateOf("paystack") } // "paystack", "flutterwave", "direct"
    var currentStep by remember { mutableStateOf(1) } // 1: Select Amt & Gateway, 2: Checkout Inputs, 3: Processing loader, 4: OTP, 5: Result
    
    // Card inputs
    var cardNumber by remember { mutableStateOf("") }
    var expiryDate by remember { mutableStateOf("") }
    var cvv by remember { mutableStateOf("") }
    var cardHolder by remember { mutableStateOf("") }
    
    // USSD / Bank Transfer inputs
    val banks = listOf("GTBank", "Zenith Bank", "Access Bank", "UBA", "Sterling Bank")
    var selectedBank by remember { mutableStateOf("GTBank") }
    var showBankDropdown by remember { mutableStateOf(false) }
    
    // USSD simulation states
    var showUSSDDialOverlay by remember { mutableStateOf(false) }
    var ussdPin by remember { mutableStateOf("") }
    var ussdSuccessMsg by remember { mutableStateOf("") }
    
    // OTP verification
    var otpText by remember { mutableStateOf("") }
    var showOtpError by remember { mutableStateOf(false) }
    
    // Test helper to simulate transaction failure
    var simulateDecline by remember { mutableStateOf(false) }
    
    // Progress loader states
    val coroutineScope = rememberCoroutineScope()
    var processingProgress by remember { mutableStateOf(0f) }
    var processingMessage by remember { mutableStateOf("") }
    
    val clipboardManager = LocalClipboardManager.current
    
    val amountDouble = amountText.toDoubleOrNull() ?: 0.0
    val formattedAmount = String.format("₦%,.2f", amountDouble)

    // Dynamic USSD code calculation
    val ussdCode = when (selectedBank) {
        "GTBank" -> "*737*1*2*${if (amountDouble > 0) amountDouble.toInt() else 1000}#"
        "Zenith Bank" -> "*966*3*${if (amountDouble > 0) amountDouble.toInt() else 1000}#"
        "Access Bank" -> "*901*1*1*${if (amountDouble > 0) amountDouble.toInt() else 1000}#"
        "UBA" -> "*919*3*${if (amountDouble > 0) amountDouble.toInt() else 1000}#"
        else -> "*822*2*${if (amountDouble > 0) amountDouble.toInt() else 1000}#"
    }

    Dialog(
        onDismissRequest = {
            if (currentStep != 3) { // Prevent dismiss during active gateway processing
                onDismiss()
            }
        }
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .wrapContentHeight()
                .testTag("top_up_wallet_modal"),
            shape = RoundedCornerShape(24.dp),
            color = Color.White,
            tonalElevation = 8.dp
        ) {
            Column(
                modifier = Modifier
                    .padding(20.dp)
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Step 1: Amount selection & Gateway Choice
                if (currentStep == 1) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Top-up Wallet 💳",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF1E293B)
                        )
                        IconButton(onClick = onDismiss) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Close Top-up modal",
                                tint = Color(0xFF64748B)
                            )
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    Text(
                        text = "Add funds securely using any of our simulated payment gateways.",
                        fontSize = 13.sp,
                        color = Color(0xFF64748B),
                        textAlign = TextAlign.Start,
                        modifier = Modifier.fillMaxWidth()
                    )
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    // Presets Grid
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        listOf(1000.0, 5000.0, 10000.0, 20000.0).forEach { preset ->
                            val isSelected = amountText == preset.toInt().toString()
                            OutlinedButton(
                                onClick = { amountText = preset.toInt().toString() },
                                modifier = Modifier.weight(1f),
                                border = BorderStroke(
                                    width = 1.dp,
                                    color = if (isSelected) Color(0xFF0A8F3D) else Color(0xFFE2E8F0)
                                ),
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.outlinedButtonColors(
                                    containerColor = if (isSelected) Color(0xFFE8F5E9) else Color.Transparent,
                                    contentColor = if (isSelected) Color(0xFF0A8F3D) else Color(0xFF64748B)
                                ),
                                contentPadding = PaddingValues(vertical = 8.dp)
                            ) {
                                Text(
                                    text = "₦${String.format("%,.0f", preset)}",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.sp
                                )
                            }
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    OutlinedTextField(
                        value = amountText,
                        onValueChange = { amountText = it.filter { char -> char.isDigit() } },
                        label = { Text("Custom Amount") },
                        placeholder = { Text("Enter custom top-up amount") },
                        leadingIcon = {
                            Text(
                                "₦",
                                color = Color(0xFF0A8F3D),
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp,
                                modifier = Modifier.padding(start = 12.dp, end = 4.dp)
                            )
                        },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color(0xFF0A8F3D),
                            focusedLabelColor = Color(0xFF0A8F3D)
                        ),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("top_up_amount_input")
                    )
                    
                    Spacer(modifier = Modifier.height(20.dp))
                    
                    Text(
                        text = "Choose Simulated Payment Gateway",
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = Color(0xFF1E293B),
                        modifier = Modifier.fillMaxWidth(),
                        textAlign = TextAlign.Start
                    )
                    
                    Spacer(modifier = Modifier.height(10.dp))
                    
                    // Gateway choices list
                    Column(
                        verticalArrangement = Arrangement.spacedBy(10.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        // PAYSTACK
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { selectedMethod = "paystack" }
                                .border(
                                    width = if (selectedMethod == "paystack") 2.dp else 1.dp,
                                    color = if (selectedMethod == "paystack") Color(0xFF09A5DB) else Color(0xFFE2E8F0),
                                    shape = RoundedCornerShape(16.dp)
                                ),
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = if (selectedMethod == "paystack") Color(0xFFF0FAFD) else Color.White
                            )
                        ) {
                            Row(
                                modifier = Modifier.padding(14.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(40.dp)
                                        .background(
                                            color = if (selectedMethod == "paystack") Color(0xFFD0F4FF) else Color(0xFFF1F5F9),
                                            shape = CircleShape
                                        ),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.CreditCard,
                                        contentDescription = "Paystack Gateway Icon",
                                        tint = if (selectedMethod == "paystack") Color(0xFF09A5DB) else Color(0xFF64748B)
                                    )
                                }
                                Spacer(modifier = Modifier.width(12.dp))
                                Column {
                                    Text(
                                        text = "Paystack Checkout (Simulated)",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 13.sp,
                                        color = Color(0xFF1E293B)
                                    )
                                    Text(
                                        text = "Simulate modern card payments & 2FA OTP processing",
                                        fontSize = 11.sp,
                                        color = Color(0xFF64748B)
                                    )
                                }
                            }
                        }
                        
                        // FLUTTERWAVE
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { selectedMethod = "flutterwave" }
                                .border(
                                    width = if (selectedMethod == "flutterwave") 2.dp else 1.dp,
                                    color = if (selectedMethod == "flutterwave") Color(0xFFF5A623) else Color(0xFFE2E8F0),
                                    shape = RoundedCornerShape(16.dp)
                                ),
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = if (selectedMethod == "flutterwave") Color(0xFFFFFBEA) else Color.White
                            )
                        ) {
                            Row(
                                modifier = Modifier.padding(14.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(40.dp)
                                        .background(
                                            color = if (selectedMethod == "flutterwave") Color(0xFFFEF3C7) else Color(0xFFF1F5F9),
                                            shape = CircleShape
                                        ),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Smartphone,
                                        contentDescription = "Flutterwave Gateway Icon",
                                        tint = if (selectedMethod == "flutterwave") Color(0xFFD97706) else Color(0xFF64748B)
                                    )
                                }
                                Spacer(modifier = Modifier.width(12.dp))
                                Column {
                                    Text(
                                        text = "Flutterwave USSD Code (Simulated)",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 13.sp,
                                        color = Color(0xFF1E293B)
                                    )
                                    Text(
                                        text = "Dial a quick USSD code and authorize via bank PIN simulation",
                                        fontSize = 11.sp,
                                        color = Color(0xFF64748B)
                                    )
                                }
                            }
                        }
                        
                        // DIRECT INSTANT BANK
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { selectedMethod = "direct" }
                                .border(
                                    width = if (selectedMethod == "direct") 2.dp else 1.dp,
                                    color = if (selectedMethod == "direct") Color(0xFF0A8F3D) else Color(0xFFE2E8F0),
                                    shape = RoundedCornerShape(16.dp)
                                ),
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = if (selectedMethod == "direct") Color(0xFFF0FDF4) else Color.White
                            )
                        ) {
                            Row(
                                modifier = Modifier.padding(14.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(40.dp)
                                        .background(
                                            color = if (selectedMethod == "direct") Color(0xFFDCFCE7) else Color(0xFFF1F5F9),
                                            shape = CircleShape
                                        ),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.AccountBalance,
                                        contentDescription = "Direct Transfer Gateway Icon",
                                        tint = if (selectedMethod == "direct") Color(0xFF16A34A) else Color(0xFF64748B)
                                    )
                                }
                                Spacer(modifier = Modifier.width(12.dp))
                                Column {
                                    Text(
                                        text = "Direct Smart Bank Transfer (Simulated)",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 13.sp,
                                        color = Color(0xFF1E293B)
                                    )
                                    Text(
                                        text = "Transfer to virtual bank with auto-clearing verification feeds",
                                        fontSize = 11.sp,
                                        color = Color(0xFF64748B)
                                    )
                                }
                            }
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(24.dp))
                    
                    Button(
                        onClick = {
                            if (amountDouble > 0) {
                                currentStep = 2
                            }
                        },
                        enabled = amountDouble > 0.0,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp)
                            .testTag("top_up_continue_button"),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0A8F3D)),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("Proceed to Checkout", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                    }
                }
                
                // Step 2: Checkout Inputs
                else if (currentStep == 2) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            IconButton(onClick = { currentStep = 1 }) {
                                Icon(
                                    imageVector = Icons.Default.ArrowBack,
                                    contentDescription = "Back to amount select",
                                    tint = Color(0xFF1E293B)
                                )
                            }
                            Text(
                                text = "Secure Checkout",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF1E293B)
                            )
                        }
                        
                        Text(
                            text = formattedAmount,
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 16.sp,
                            color = Color(0xFF0A8F3D)
                        )
                    }
                    
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    // --- PAYSTACK CARD CHECKOUT ---
                    if (selectedMethod == "paystack") {
                        CardVisualizer(
                            cardNumber = cardNumber,
                            expiryDate = expiryDate,
                            cvv = cvv,
                            cardHolder = cardHolder
                        )
                        
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        OutlinedTextField(
                            value = cardHolder,
                            onValueChange = { cardHolder = it },
                            label = { Text("Cardholder Name") },
                            placeholder = { Text("CHINEDU OKOYE") },
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = Color(0xFF0A8F3D),
                                focusedLabelColor = Color(0xFF0A8F3D)
                            ),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("top_up_cardholder_input")
                        )
                        
                        Spacer(modifier = Modifier.height(10.dp))
                        
                        OutlinedTextField(
                            value = cardNumber,
                            onValueChange = { input ->
                                val clean = input.filter { it.isDigit() }
                                if (clean.length <= 16) cardNumber = clean
                            },
                            label = { Text("Card Number") },
                            placeholder = { Text("4111 2222 3333 4444") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = Color(0xFF0A8F3D),
                                focusedLabelColor = Color(0xFF0A8F3D)
                            ),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("top_up_cardnumber_input")
                        )
                        
                        Spacer(modifier = Modifier.height(10.dp))
                        
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            OutlinedTextField(
                                value = expiryDate,
                                onValueChange = { input ->
                                    val clean = input.filter { it.isDigit() || it == '/' }
                                    if (clean.length <= 5) {
                                        if (clean.length == 2 && !expiryDate.contains('/') && !clean.contains('/')) {
                                            expiryDate = "$clean/"
                                        } else {
                                            expiryDate = clean
                                        }
                                    }
                                },
                                label = { Text("Expiry (MM/YY)") },
                                placeholder = { Text("12/28") },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                singleLine = true,
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = Color(0xFF0A8F3D),
                                    focusedLabelColor = Color(0xFF0A8F3D)
                                ),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("top_up_expiry_input")
                            )
                            
                            OutlinedTextField(
                                value = cvv,
                                onValueChange = { input ->
                                    val clean = input.filter { it.isDigit() }
                                    if (clean.length <= 3) cvv = clean
                                },
                                label = { Text("CVV") },
                                placeholder = { Text("123") },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                singleLine = true,
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = Color(0xFF0A8F3D),
                                    focusedLabelColor = Color(0xFF0A8F3D)
                                ),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("top_up_cvv_input")
                            )
                        }
                    }
                    
                    // --- FLUTTERWAVE USSD CHECKOUT ---
                    else if (selectedMethod == "flutterwave") {
                        Text(
                            text = "Choose your Bank to get the direct USSD Top-up code.",
                            fontSize = 12.sp,
                            color = Color(0xFF64748B),
                            modifier = Modifier.fillMaxWidth(),
                            textAlign = TextAlign.Start
                        )
                        
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        // Bank dropdown selector
                        Box(modifier = Modifier.fillMaxWidth()) {
                            OutlinedButton(
                                onClick = { showBankDropdown = true },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(54.dp),
                                shape = RoundedCornerShape(12.dp),
                                border = BorderStroke(1.dp, Color(0xFFCBD5E1)),
                                colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFF1E293B))
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(selectedBank, fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                                    Icon(
                                        imageVector = Icons.Default.ArrowDropDown,
                                        contentDescription = "Select Bank dropdown"
                                    )
                                }
                            }
                            
                            DropdownMenu(
                                expanded = showBankDropdown,
                                onDismissRequest = { showBankDropdown = false },
                                modifier = Modifier.fillMaxWidth(0.8f)
                            ) {
                                banks.forEach { bank ->
                                    DropdownMenuItem(
                                        text = { Text(bank) },
                                        onClick = {
                                            selectedBank = bank
                                            showBankDropdown = false
                                        }
                                    )
                                }
                            }
                        }
                        
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        Card(
                            colors = CardDefaults.cardColors(containerColor = BgSlate50),
                            border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
                            shape = RoundedCornerShape(16.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(
                                modifier = Modifier.padding(16.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    text = "DIAL THIS USSD CODE ON YOUR REGISTERED SIM",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF64748B),
                                    textAlign = TextAlign.Center
                                )
                                
                                Spacer(modifier = Modifier.height(10.dp))
                                
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.Center,
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text(
                                        text = ussdCode,
                                        fontSize = 20.sp,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = Color(0xFFD97706),
                                        letterSpacing = 1.sp
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    IconButton(
                                        onClick = {
                                            clipboardManager?.setText(AnnotatedString(ussdCode))
                                        },
                                        modifier = Modifier.size(36.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.ContentCopy,
                                            contentDescription = "Copy USSD Code",
                                            tint = Color(0xFFD97706),
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }
                                }
                                
                                Spacer(modifier = Modifier.height(12.dp))
                                
                                // Interactive "Dial Code" Simulation Button!
                                Button(
                                    onClick = {
                                        showUSSDDialOverlay = true
                                        ussdPin = ""
                                        ussdSuccessMsg = ""
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFEF3C7), contentColor = Color(0xFFD97706)),
                                    shape = RoundedCornerShape(8.dp),
                                    contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Phone,
                                        contentDescription = "Phone icon",
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Simulate Calling USSD", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                    
                    // --- DIRECT BANK TRANSFER CHECKOUT ---
                    else if (selectedMethod == "direct") {
                        Text(
                            text = "Transfer exactly $formattedAmount to the virtual bank account below. Our automated gateway verifies this transfer instantly.",
                            fontSize = 12.sp,
                            color = Color(0xFF64748B),
                            modifier = Modifier.padding(bottom = 12.dp),
                            textAlign = TextAlign.Start
                        )
                        
                        Card(
                            colors = CardDefaults.cardColors(containerColor = BgSlate50),
                            border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
                            shape = RoundedCornerShape(16.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Text(
                                    text = "RECEIVING BANK",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF64748B)
                                )
                                Text(
                                    text = "Opay Bank / AfriSav Virtual",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF1E293B)
                                )
                                
                                Spacer(modifier = Modifier.height(12.dp))
                                
                                Text(
                                    text = "ACCOUNT NUMBER",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF64748B)
                                )
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text(
                                        text = "9038271638",
                                        fontSize = 18.sp,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = Color(0xFF0A8F3D),
                                        letterSpacing = 1.sp
                                    )
                                    IconButton(
                                        onClick = {
                                            clipboardManager?.setText(AnnotatedString("9038271638"))
                                        },
                                        modifier = Modifier.size(28.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.ContentCopy,
                                            contentDescription = "Copy account number",
                                            tint = Color(0xFF0A8F3D),
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }
                                }
                                
                                Spacer(modifier = Modifier.height(12.dp))
                                
                                Text(
                                    text = "ACCOUNT NAME",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF64748B)
                                )
                                Text(
                                    text = "AfriSav - $currentUserName",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF1E293B)
                                )
                            }
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    // Simulated decline toggle (to verify failure pathways)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color(0xFFFEF2F2))
                            .clickable { simulateDecline = !simulateDecline }
                            .padding(10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Checkbox(
                            checked = simulateDecline,
                            onCheckedChange = { simulateDecline = it },
                            colors = CheckboxDefaults.colors(checkedColor = Color(0xFFEF4444))
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Column {
                            Text(
                                "Simulate Payment Decline",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF991B1B)
                            )
                            Text(
                                "Check to simulate a declined transaction flow",
                                fontSize = 10.sp,
                                color = Color(0xFFEF4444)
                            )
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(20.dp))
                    
                    val isInputsValid = when (selectedMethod) {
                        "paystack" -> cardHolder.isNotBlank() && cardNumber.length >= 15 && expiryDate.length >= 4 && cvv.length >= 3
                        else -> true
                    }
                    
                    Button(
                        onClick = {
                            if (isInputsValid) {
                                currentStep = 3
                                coroutineScope.launch {
                                    processingProgress = 0.1f
                                    processingMessage = when (selectedMethod) {
                                        "paystack" -> "Initiating secure card gateway connection..."
                                        "flutterwave" -> "Checking USSD channel status..."
                                        else -> "Checking central bank auto-clearing feed..."
                                    }
                                    delay(700)
                                    processingProgress = 0.3f
                                    processingMessage = when (selectedMethod) {
                                        "paystack" -> "Validating card parameters & processing secure layer..."
                                        "flutterwave" -> "Verifying phone line network signals..."
                                        else -> "Searching for matching deposits from your bank..."
                                    }
                                    delay(1000)
                                    processingProgress = 0.55f
                                    processingMessage = when (selectedMethod) {
                                        "paystack" -> "Contacting issuing bank secure 3D-secure portal..."
                                        "flutterwave" -> "Awaiting bank PIN authentication response..."
                                        else -> "Validating bank transfer reference number..."
                                    }
                                    delay(1200)
                                    processingProgress = 0.8f
                                    processingMessage = when (selectedMethod) {
                                        "paystack" -> "Awaiting One-Time Password verification..."
                                        "flutterwave" -> "Reconciling USSD network transaction ledger..."
                                        else -> "Updating Kobo ledger and wallet balance in real-time..."
                                    }
                                    delay(800)
                                    processingProgress = 1.0f
                                    
                                    if (simulateDecline) {
                                        currentStep = 5 // Failure
                                    } else {
                                        if (selectedMethod == "paystack") {
                                            currentStep = 4 // Go to OTP
                                        } else {
                                            currentStep = 5 // Go directly to Success
                                        }
                                    }
                                }
                            }
                        },
                        enabled = isInputsValid,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp)
                            .testTag("top_up_pay_button"),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = when (selectedMethod) {
                                "paystack" -> Color(0xFF09A5DB)
                                "flutterwave" -> Color(0xFFF5A623)
                                else -> Color(0xFF0A8F3D)
                            }
                        ),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(
                            text = when (selectedMethod) {
                                "paystack" -> "Pay $formattedAmount securely via Card"
                                "flutterwave" -> "Verify USSD Dial Code authorization"
                                else -> "I have completed the Bank Transfer (Verify)"
                            },
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                    }
                }
                
                // Step 3: Processing Animation Screen
                else if (currentStep == 3) {
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier.size(100.dp)
                    ) {
                        CircularProgressIndicator(
                            progress = { processingProgress },
                            modifier = Modifier.size(80.dp),
                            color = when (selectedMethod) {
                                "paystack" -> Color(0xFF09A5DB)
                                "flutterwave" -> Color(0xFFF5A623)
                                else -> Color(0xFF0A8F3D)
                            },
                            strokeWidth = 6.dp,
                        )
                    }
                    
                    Spacer(modifier = Modifier.height(24.dp))
                    
                    Text(
                        text = "Securing simulated payment...",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF1E293B)
                    )
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    Text(
                        text = processingMessage,
                        fontSize = 13.sp,
                        color = Color(0xFF64748B),
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(horizontal = 16.dp)
                    )
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    LinearProgressIndicator(
                        progress = { processingProgress },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(6.dp)
                            .clip(RoundedCornerShape(3.dp)),
                        color = Color(0xFF0A8F3D),
                        trackColor = Color(0xFFE2E8F0)
                    )
                    
                    Spacer(modifier = Modifier.height(16.dp))
                }
                
                // Step 4: OTP Screen
                else if (currentStep == 4) {
                    Icon(
                        imageVector = Icons.Default.Security,
                        contentDescription = "OTP secure icon",
                        tint = Color(0xFF09A5DB),
                        modifier = Modifier.size(64.dp)
                    )
                    
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    Text(
                        text = "Enter Authorization OTP",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF1E293B)
                    )
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    Text(
                        text = "We have sent a simulated 5-digit verification OTP to your phone. Enter 12345 (or any digits) to complete payment.",
                        fontSize = 12.sp,
                        color = Color(0xFF64748B),
                        textAlign = TextAlign.Center
                    )
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    OutlinedTextField(
                        value = otpText,
                        onValueChange = { input ->
                            val clean = input.filter { it.isDigit() }
                            if (clean.length <= 5) {
                                otpText = clean
                                showOtpError = false
                            }
                        },
                        label = { Text("5-Digit One-Time PIN") },
                        placeholder = { Text("12345") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color(0xFF09A5DB),
                            focusedLabelColor = Color(0xFF09A5DB)
                        ),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("top_up_otp_input")
                    )
                    
                    if (showOtpError) {
                        Text(
                            text = "Please enter exactly 5 digits to verify.",
                            color = Color.Red,
                            fontSize = 11.sp,
                            modifier = Modifier
                                .padding(top = 4.dp)
                                .fillMaxWidth(),
                            textAlign = TextAlign.Start
                        )
                    }
                    
                    Spacer(modifier = Modifier.height(24.dp))
                    
                    Button(
                        onClick = {
                            if (otpText.length == 5) {
                                currentStep = 5 // success screen!
                            } else {
                                showOtpError = true
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp)
                            .testTag("top_up_verify_otp_button"),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF09A5DB)),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("Verify & Finalize Top-up", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    }
                }
                
                // Step 5: Success or Failure Screen
                else if (currentStep == 5) {
                    if (simulateDecline) {
                        // Failure State
                        Icon(
                            imageVector = Icons.Default.Warning,
                            contentDescription = "Decline warning icon",
                            tint = Color(0xFFEF4444),
                            modifier = Modifier.size(64.dp)
                        )
                        
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        Text(
                            text = "Payment Declined ❌",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFEF4444)
                        )
                        
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        Text(
                            text = "Simulated bank payment decline: Transaction rejected due to simulation test trigger. Please uncheck 'Simulate Payment Decline' and try again.",
                            fontSize = 13.sp,
                            color = Color(0xFF64748B),
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(horizontal = 8.dp)
                        )
                        
                        Spacer(modifier = Modifier.height(24.dp))
                        
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            OutlinedButton(
                                onClick = onDismiss,
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Text("Cancel", color = Color(0xFF64748B))
                            }
                            
                            Button(
                                onClick = {
                                    simulateDecline = false
                                    currentStep = 1
                                },
                                modifier = Modifier.weight(1f),
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0A8F3D)),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Text("Try Again", fontWeight = FontWeight.Bold)
                            }
                        }
                    } else {
                        // Success State
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = "Success checkmark icon",
                            tint = Color(0xFF16A34A),
                            modifier = Modifier.size(64.dp)
                        )
                        
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        Text(
                            text = "Top-up Approved! 🎉",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF16A34A)
                        )
                        
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        Text(
                            text = "Successfully added $formattedAmount to your AfriSav Smart Wallet spendable balance via secure simulated payment gateway.",
                            fontSize = 13.sp,
                            color = Color(0xFF64748B),
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(horizontal = 8.dp)
                        )
                        
                        Spacer(modifier = Modifier.height(24.dp))
                        
                        Button(
                            onClick = {
                                onConfirm(amountDouble)
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(50.dp)
                                .testTag("top_up_done_button"),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF16A34A)),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text("Done", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
    
    // Sub-dialog overlay for USSD Sim Dial
    if (showUSSDDialOverlay) {
        Dialog(onDismissRequest = { showUSSDDialOverlay = false }) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                shape = RoundedCornerShape(16.dp),
                color = Color(0xFF0F172A),
                tonalElevation = 8.dp
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Simulated USSD Session",
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp
                        )
                        IconButton(onClick = { showUSSDDialOverlay = false }) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Close USSD overlay",
                                tint = Color.LightGray
                            )
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    if (ussdSuccessMsg.isNotEmpty()) {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = "USSD Dial success",
                            tint = Color.Green,
                            modifier = Modifier.size(48.dp)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = ussdSuccessMsg,
                            color = Color.White,
                            fontSize = 13.sp,
                            textAlign = TextAlign.Center
                        )
                    } else {
                        Text(
                            text = "Your bank requires your 4-digit card/transfer PIN to authorize transfer of $formattedAmount to AfriSav.",
                            color = Color.LightGray,
                            fontSize = 12.sp,
                            textAlign = TextAlign.Center
                        )
                        
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        OutlinedTextField(
                            value = ussdPin,
                            onValueChange = { input ->
                                val clean = input.filter { it.isDigit() }
                                if (clean.length <= 4) ussdPin = clean
                            },
                            label = { Text("4-Digit Bank PIN", color = Color.White) },
                            placeholder = { Text("••••", color = Color.Gray) },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            visualTransformation = PasswordVisualTransformation(),
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White,
                                focusedBorderColor = Color.Green,
                                unfocusedBorderColor = Color.LightGray,
                                focusedLabelColor = Color.Green,
                                unfocusedLabelColor = Color.LightGray
                            ),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        )
                        
                        Spacer(modifier = Modifier.height(20.dp))
                        
                        Button(
                            onClick = {
                                if (ussdPin.length == 4) {
                                    ussdSuccessMsg = "USSD code processed successfully! Transfer authorized. Please click 'Authorize' in the main checkout window to finalize and sync."
                                }
                            },
                            enabled = ussdPin.length == 4,
                            colors = ButtonDefaults.buttonColors(containerColor = Color.Green, contentColor = Color.Black),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Submit Bank PIN", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun TransferFundsDialog(onDismiss: () -> Unit, onConfirm: (String, Double) -> Unit) {
    var transferType by remember { mutableStateOf("wallet") } // "wallet" or "bank"
    var showConfirmDialog by remember { mutableStateOf(false) }
    
    // Wallet inputs
    var recipient by remember { mutableStateOf("") }
    
    // Bank inputs
    val banks = listOf(
        "GTBank", "Zenith Bank", "Access Bank", "UBA", 
        "Sterling Bank", "First Bank", "Wema Bank", "Kuda Bank"
    )
    var selectedBank by remember { mutableStateOf("GTBank") }
    var showBankDropdown by remember { mutableStateOf(false) }
    var accountNumber by remember { mutableStateOf("") }
    var remark by remember { mutableStateOf("") }
    
    // Common input
    var amountText by remember { mutableStateOf("") }
    
    // Resolve name from account number digits
    val resolvedName = remember(accountNumber) {
        if (accountNumber.length == 10) {
            val lastDigit = accountNumber.lastOrNull()?.digitToIntOrNull() ?: 0
            when (lastDigit) {
                1, 2 -> "Oluwaseun Chidi"
                3, 4 -> "Fatima Bello"
                5, 6 -> "Chinedu Okoye"
                7, 8 -> "Aminu Dankwambo"
                else -> "Ezenwa Nwachukwu"
            }
        } else {
            ""
        }
    }
    
    Dialog(onDismissRequest = onDismiss) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .wrapContentHeight()
                .testTag("transfer_funds_modal"),
            shape = RoundedCornerShape(24.dp),
            color = Color.White,
            tonalElevation = 8.dp
        ) {
            Column(
                modifier = Modifier
                    .padding(20.dp)
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Header with Close Button
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Transfer Money 💸",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF1E293B)
                    )
                    IconButton(onClick = onDismiss) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close Transfer Modal",
                            tint = Color(0xFF64748B)
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(12.dp))
                
                Text(
                    text = "Transfer instantly and securely to another wallet or bank account.",
                    fontSize = 13.sp,
                    color = Color(0xFF64748B),
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.Start
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Tabs
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color(0xFFF1F5F9))
                        .padding(4.dp)
                ) {
                    // Wallet Tab
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(8.dp))
                            .background(if (transferType == "wallet") Color.White else Color.Transparent)
                            .clickable { transferType = "wallet" }
                            .padding(vertical = 10.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Person,
                                contentDescription = "To Wallet",
                                tint = if (transferType == "wallet") Color(0xFF0A8F3D) else Color(0xFF64748B),
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "To Wallet",
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp,
                                color = if (transferType == "wallet") Color(0xFF0A8F3D) else Color(0xFF64748B)
                            )
                        }
                    }
                    
                    // Bank Tab
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(8.dp))
                            .background(if (transferType == "bank") Color.White else Color.Transparent)
                            .clickable { transferType = "bank" }
                            .padding(vertical = 10.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.AccountBalance,
                                contentDescription = "To Bank",
                                tint = if (transferType == "bank") Color(0xFF0A8F3D) else Color(0xFF64748B),
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "To Bank",
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp,
                                color = if (transferType == "bank") Color(0xFF0A8F3D) else Color(0xFF64748B)
                            )
                        }
                    }
                }
                
                Spacer(modifier = Modifier.height(20.dp))
                
                // Form Fields
                if (transferType == "wallet") {
                    OutlinedTextField(
                        value = recipient,
                        onValueChange = { recipient = it },
                        label = { Text("Recipient (Phone or @Tag)") },
                        placeholder = { Text("e.g. 08012345678 or @chinedu") },
                        singleLine = true,
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.Person,
                                contentDescription = "Tag",
                                tint = Color(0xFF0A8F3D)
                            )
                        },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color(0xFF0A8F3D),
                            focusedLabelColor = Color(0xFF0A8F3D)
                        ),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("transfer_wallet_recipient_input")
                    )
                } else {
                    // Select Bank Dropdown
                    Box(modifier = Modifier.fillMaxWidth()) {
                        OutlinedButton(
                            onClick = { showBankDropdown = true },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(54.dp),
                            shape = RoundedCornerShape(12.dp),
                            border = BorderStroke(1.dp, Color(0xFFCBD5E1)),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFF1E293B))
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.AccountBalance,
                                        contentDescription = "Bank",
                                        tint = Color(0xFF0A8F3D),
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(selectedBank, fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                                }
                                Icon(
                                    imageVector = Icons.Default.ArrowDropDown,
                                    contentDescription = "Select Bank Dropdown",
                                    tint = Color(0xFF64748B)
                                )
                            }
                        }
                        
                        DropdownMenu(
                            expanded = showBankDropdown,
                            onDismissRequest = { showBankDropdown = false },
                            modifier = Modifier.fillMaxWidth(0.8f)
                        ) {
                            banks.forEach { bank ->
                                DropdownMenuItem(
                                    text = { Text(bank) },
                                    onClick = {
                                        selectedBank = bank
                                        showBankDropdown = false
                                    }
                                )
                            }
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    // Account Number input
                    OutlinedTextField(
                        value = accountNumber,
                        onValueChange = { input ->
                            val clean = input.filter { it.isDigit() }
                            if (clean.length <= 10) accountNumber = clean
                        },
                        label = { Text("Account Number") },
                        placeholder = { Text("10-digit NUBAN account") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.CreditCard,
                                contentDescription = "Account Number",
                                tint = Color(0xFF0A8F3D)
                            )
                        },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color(0xFF0A8F3D),
                            focusedLabelColor = Color(0xFF0A8F3D)
                        ),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("transfer_bank_account_input")
                    )
                    
                    Spacer(modifier = Modifier.height(6.dp))
                    
                    // Account name resolution display
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        if (accountNumber.length == 10) {
                            Icon(
                                imageVector = Icons.Default.CheckCircle,
                                contentDescription = "Resolved",
                                tint = Color(0xFF0A8F3D),
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "Account resolved: $resolvedName",
                                color = Color(0xFF0A8F3D),
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                        } else {
                            Text(
                                text = "Enter 10 digits to auto-resolve account name",
                                color = Color(0xFF64748B),
                                fontSize = 11.sp
                            )
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    // Remark input
                    OutlinedTextField(
                        value = remark,
                        onValueChange = { remark = it },
                        label = { Text("Remark / Description (Optional)") },
                        placeholder = { Text("e.g. Rice purchase") },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color(0xFF0A8F3D),
                            focusedLabelColor = Color(0xFF0A8F3D)
                        ),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Common Amount field
                OutlinedTextField(
                    value = amountText,
                    onValueChange = { amountText = it },
                    label = { Text("Amount (₦)") },
                    placeholder = { Text("e.g. 5000") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    leadingIcon = {
                        Text(
                            "₦",
                            color = Color(0xFF0A8F3D),
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp,
                            modifier = Modifier.padding(start = 12.dp, end = 4.dp)
                        )
                    },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color(0xFF0A8F3D),
                        focusedLabelColor = Color(0xFF0A8F3D)
                    ),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("transfer_amount_input")
                )
                
                Spacer(modifier = Modifier.height(24.dp))
                
                // Confirm/Cancel Buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp),
                        shape = RoundedCornerShape(12.dp),
                        border = BorderStroke(1.dp, Color(0xFFCBD5E1)),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFF64748B))
                    ) {
                        Text("Cancel", fontWeight = FontWeight.Bold)
                    }
                    
                    val isFormValid = remember(transferType, recipient, accountNumber, amountText) {
                        val amt = amountText.toDoubleOrNull() ?: 0.0
                        if (transferType == "wallet") {
                            recipient.isNotBlank() && amt > 0.0
                        } else {
                            accountNumber.length == 10 && amt > 0.0
                        }
                    }
                    
                    Button(
                        onClick = {
                            showConfirmDialog = true
                        },
                        enabled = isFormValid,
                        modifier = Modifier
                            .weight(1.5f)
                            .height(48.dp)
                            .testTag("transfer_submit_button"),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF0A8F3D),
                            disabledContainerColor = Color(0xFFCBD5E1)
                        ),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("Send Money", fontWeight = FontWeight.Bold, color = Color.White)
                    }
                }
            }
        }
    }

    if (showConfirmDialog) {
        AlertDialog(
            onDismissRequest = { showConfirmDialog = false },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = null,
                        tint = Color(0xFF0A8F3D),
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Confirm Transfer", fontWeight = FontWeight.Bold, fontSize = 18.sp)
                }
            },
            text = {
                Column {
                    Text(
                        text = "Please verify the transfer details below before executing this transaction:",
                        fontSize = 13.sp,
                        color = Color(0xFF64748B)
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Card(
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFF1F5F9)),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("Amount:", fontSize = 12.sp, color = Color(0xFF64748B))
                                Text(
                                    "₦${String.format("%,.2f", amountText.toDoubleOrNull() ?: 0.0)}",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF1E293B)
                                )
                            }
                            Spacer(modifier = Modifier.height(6.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("Recipient:", fontSize = 12.sp, color = Color(0xFF64748B))
                                Text(
                                    if (transferType == "wallet") recipient else "$selectedBank ($accountNumber)",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF1E293B)
                                )
                            }
                            if (transferType == "bank" && resolvedName.isNotEmpty()) {
                                Spacer(modifier = Modifier.height(6.dp))
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text("Name:", fontSize = 12.sp, color = Color(0xFF64748B))
                                    Text(
                                        resolvedName,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFF1E293B)
                                    )
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        showConfirmDialog = false
                        val amt = amountText.toDoubleOrNull() ?: 0.0
                        if (transferType == "wallet") {
                            onConfirm(recipient, amt)
                        } else {
                            val fullRecipient = "$selectedBank ($accountNumber) - $resolvedName"
                            onConfirm(fullRecipient, amt)
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0A8F3D)),
                    modifier = Modifier.testTag("confirm_transfer_button")
                ) {
                    Text("Confirm Transfer")
                }
            },
            dismissButton = {
                TextButton(onClick = { showConfirmDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateGoalDialog(onDismiss: () -> Unit, onConfirm: (String, Double, String, Double, Boolean) -> Unit) {
    var title by remember { mutableStateOf("") }
    var targetText by remember { mutableStateOf("") }
    var category by remember { mutableStateOf("Rice") }
    var autoSaveAmtText by remember { mutableStateOf("500") }
    var autoSaveEnabled by remember { mutableStateOf(true) }

    val categories = listOf("Rice", "Beans", "Garri", "Tomatoes", "Yam", "Fish")
    var catExpanded by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Create Savings Goal") },
        text = {
            Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                Text("Set up small automated daily or weekly deposits to easily fund wholesale baskets.", fontSize = 13.sp)
                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Goal Title (e.g. 50kg Rice)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(12.dp))

                ExposedDropdownMenuBox(
                    expanded = catExpanded,
                    onExpandedChange = { catExpanded = !catExpanded }
                ) {
                    OutlinedTextField(
                        value = category,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Food Category") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = catExpanded) },
                        modifier = Modifier
                            .menuAnchor()
                            .fillMaxWidth()
                    )
                    ExposedDropdownMenu(
                        expanded = catExpanded,
                        onDismissRequest = { catExpanded = false }
                    ) {
                        categories.forEach { cat ->
                            DropdownMenuItem(
                                text = { Text(cat) },
                                onClick = {
                                    category = cat
                                    catExpanded = false
                                }
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = targetText,
                    onValueChange = { targetText = it },
                    label = { Text("Target Amount (₦)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(12.dp))

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(checked = autoSaveEnabled, onCheckedChange = { autoSaveEnabled = it })
                    Text("Enable Auto-Save Plan", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                }

                if (autoSaveEnabled) {
                    OutlinedTextField(
                        value = autoSaveAmtText,
                        onValueChange = { autoSaveAmtText = it },
                        label = { Text("Daily Automated Savings (₦)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val target = targetText.toDoubleOrNull() ?: 0.0
                    val autoAmt = autoSaveAmtText.toDoubleOrNull() ?: 500.0
                    if (title.isNotEmpty() && target > 0) {
                        onConfirm(title, target, category, autoAmt, autoSaveEnabled)
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0A8F3D))
            ) {
                Text("Start Saving")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}

@Composable
fun CreateCircleDialog(onDismiss: () -> Unit, onConfirm: (String, String, Double, Double, Boolean) -> Unit) {
    var title by remember { mutableStateOf("") }
    var desc by remember { mutableStateOf("") }
    var targetText by remember { mutableStateOf("") }
    var initContribText by remember { mutableStateOf("") }
    var isPrivate by remember { mutableStateOf(true) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Create Food Circle") },
        text = {
            Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                Text("Form a collaborative bulk buying group for family or neighborhood sharing.", fontSize = 13.sp)
                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Circle Name (e.g. Gbagada Rice group)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = desc,
                    onValueChange = { desc = it },
                    label = { Text("Description (e.g. Save 10% on rice)") },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = targetText,
                    onValueChange = { targetText = it },
                    label = { Text("Bulk Goal Target Price (₦)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = initContribText,
                    onValueChange = { initContribText = it },
                    label = { Text("Your First Contribution (₦)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFFEF2F4)),
                    border = BorderStroke(1.dp, Color(0xFFFCE7F3)),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Default.Lock, 
                            contentDescription = null, 
                            tint = Color(0xFFE11D48),
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            "This circle is Private & Secure", 
                            fontSize = 11.sp, 
                            fontWeight = FontWeight.Bold, 
                            color = Color(0xFF9F1239)
                        )
                    }
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Only members can see this circle on their dashboard or participate. It is completely hidden from non-members.",
                    fontSize = 10.sp,
                    color = Color(0xFF64748B)
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val target = targetText.toDoubleOrNull() ?: 0.0
                    val initContrib = initContribText.toDoubleOrNull() ?: 0.0
                    if (title.isNotEmpty() && target > 0) {
                        onConfirm(title, desc, target, initContrib, true)
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0A8F3D))
            ) {
                Text("Create Circle")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}

@Composable
fun SaveTowardsProductDialog(
    item: MarketItem,
    onDismiss: () -> Unit,
    onConfirm: (Double) -> Unit
) {
    var initSaveText by remember { mutableStateOf("") }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Save Towards This Food") },
        text = {
            Column {
                Text("You are setting up a dedicated Food Savings Goal for:", fontSize = 13.sp)
                Spacer(modifier = Modifier.height(8.dp))
                Card(colors = CardDefaults.cardColors(containerColor = Color(0xFFF1F5F9))) {
                    Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(SurfaceBg),
                            contentAlignment = Alignment.Center
                        ) {
                            FoodItemImage(
                                imageUrl = item.imageUrl,
                                category = item.category,
                                contentDescription = item.name,
                                modifier = Modifier.fillMaxSize()
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(item.name, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                            Text("₦${String.format("%,.0f", item.price)}", color = Color(0xFF0A8F3D), fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        }
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
                Text("Would you like to make an initial save payment right now from your wallet?", fontSize = 13.sp)
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = initSaveText,
                    onValueChange = { initSaveText = it },
                    label = { Text("Initial Deposit Amount (₦) - Optional") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val amt = initSaveText.toDoubleOrNull() ?: 0.0
                    onConfirm(amt)
                },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0A8F3D))
            ) {
                Text("Create Savings Plan")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}

@Composable
fun CheckoutDialog(
    item: MarketItem,
    viewModel: KoboViewModel,
    onDismiss: () -> Unit,
    onConfirm: (String) -> Unit,
    portionName: String = "Full Unit",
    portionFraction: Double = 1.0
) {
    var selectedMethod by remember { mutableStateOf("Wallet") }
    val paymentMethods = listOf("Wallet", "Debit Card", "Bank Transfer", "Split Payment")
    val wallet by viewModel.walletState.collectAsStateWithLifecycle()

    val reviews by viewModel.allReviews.collectAsStateWithLifecycle()
    val vendorReviews = reviews.filter { it.targetName.equals(item.vendorName, ignoreCase = true) && it.isTargetSeller }
    val avgRating = if (vendorReviews.isNotEmpty()) vendorReviews.map { it.rating }.average() else item.rating
    val totalReviews = if (vendorReviews.isNotEmpty()) vendorReviews.size else item.ratingCount

    var userRating by remember { mutableStateOf(5) }
    var reviewComment by remember { mutableStateOf("") }
    var reviewSubmittedByMe by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Checkout Food Basket", fontWeight = FontWeight.Bold)
                val cartItems by viewModel.cartState.collectAsStateWithLifecycle()
                val cartCount = cartItems.sumOf { it.quantity }
                if (cartCount > 0) {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = PrimaryGreen.copy(alpha = 0.12f)),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(
                            text = "$cartCount in Cart",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = PrimaryGreen,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .verticalScroll(rememberScrollState())
                    .fillMaxWidth()
            ) {
                val cartItems by viewModel.cartState.collectAsStateWithLifecycle()
                val cartCount = cartItems.sumOf { it.quantity }
                if (cartCount > 0) {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = PrimaryGreen.copy(alpha = 0.08f)),
                        border = BorderStroke(1.dp, PrimaryGreen.copy(alpha = 0.15f)),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 12.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(10.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.ShoppingCart,
                                contentDescription = "Cart",
                                tint = PrimaryGreen,
                                modifier = Modifier.size(16.dp)
                            )
                            Text(
                                text = "Note: You currently have $cartCount item(s) in your secure Food Basket.",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Medium,
                                color = PrimaryGreen
                            )
                        }
                    }
                }

                Text("Secure checkout via AfriSav fintech integrations.", fontSize = 12.sp, color = TextSlate500)
                Spacer(modifier = Modifier.height(12.dp))

                val basePrice = item.price * portionFraction
                val salesFee = basePrice * 0.025
                val serviceCharge = 50.0
                val totalCost = basePrice + salesFee + serviceCharge

                Card(
                    colors = CardDefaults.cardColors(containerColor = BgSlate50),
                    border = BorderStroke(1.dp, BorderSlate100)
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            val displayName = if (portionName == "Full Unit" || portionName == "Full Bundle" || portionName.isEmpty()) item.name else "$portionName of ${item.name}"
                            Text(displayName, fontWeight = FontWeight.Bold, fontSize = 13.sp, modifier = Modifier.weight(1f))
                            Text("₦${String.format("%,.2f", basePrice)}", fontWeight = FontWeight.Black, color = Color(0xFF334155), fontSize = 13.sp)
                        }
                        Text("Vendor: ${item.vendorName}", fontSize = 11.sp, color = Color.Gray)
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Sales Fee (2.5%):", fontSize = 11.sp, color = Color.Gray)
                            Text("₦${String.format("%,.2f", salesFee)}", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFFEA580C))
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Service Charge:", fontSize = 11.sp, color = Color.Gray)
                            Text("₦${String.format("%,.2f", serviceCharge)}", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFFEA580C))
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Delivery Fee:", fontSize = 11.sp, color = Color.Gray)
                            Text("₦950.00", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFF334155))
                        }
                        Divider(color = BorderSlate100, modifier = Modifier.padding(vertical = 8.dp))
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Total Escrow Balance:", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1E293B))
                            Text("₦${String.format("%,.2f", totalCost)}", fontWeight = FontWeight.Black, color = Color(0xFF0A8F3D), fontSize = 14.sp)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))
                Text("Select Secure Payment Method", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = Color(0xFF1E293B))
                Spacer(modifier = Modifier.height(8.dp))

                paymentMethods.forEach { method ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { selectedMethod = method }
                            .padding(vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(selected = selectedMethod == method, onClick = { selectedMethod = method })
                        Column {
                            Text(method, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                            if (method == "Wallet") {
                                Text(
                                    "Bal: ₦${String.format("%,.2f", wallet?.availableBalance ?: 0.0)}",
                                    fontSize = 10.sp,
                                    color = Color(0xFF0A8F3D)
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))
                Divider(color = BorderSlate100)
                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Vendor Reviews & Ratings", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = Color(0xFF1E293B))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Star, contentDescription = null, tint = SecondaryOrange, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "${String.format("%.1f", avgRating)} ($totalReviews)",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextSlate800
                        )
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))

                if (vendorReviews.isEmpty()) {
                    Text(
                        "No reviews posted yet for ${item.vendorName}.",
                        fontSize = 11.sp,
                        color = Color.Gray,
                        modifier = Modifier.padding(vertical = 4.dp)
                    )
                } else {
                    vendorReviews.forEach { rev ->
                        Card(
                            colors = CardDefaults.cardColors(containerColor = SurfaceBg),
                            border = BorderStroke(1.dp, Color(0xFFF1F5F9)),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp)
                        ) {
                            Column(modifier = Modifier.padding(8.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(rev.reviewerName, fontWeight = FontWeight.Bold, fontSize = 11.sp, color = TextSlate800)
                                    Row {
                                        for (i in 1..rev.rating) {
                                            Icon(Icons.Default.Star, contentDescription = null, tint = SecondaryOrange, modifier = Modifier.size(10.dp))
                                        }
                                    }
                                }
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(rev.reviewText, fontSize = 11.sp, color = TextSlate500)
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = { onConfirm(selectedMethod) },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0A8F3D))
            ) {
                Text("Place Order")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}

@Composable
fun PortionSelectionDialog(
    item: MarketItem,
    onDismiss: () -> Unit,
    onConfirmPortion: (String, Double, Boolean) -> Unit // portionName, portionFraction, triggerBuyNow
) {
    // Portions or bundles configuration
    val isBundle = item.isBundle
    val allowPortions = item.allowPortions
    val portionType = item.portionType ?: "Bag"

    var selectedPortionName by remember { mutableStateOf("Full Unit") }
    var selectedPortionFraction by remember { mutableStateOf(1.0) }

    // Prepare portion list
    val portions = when {
        isBundle -> listOf(Pair("Full Bundle", 1.0))
        allowPortions && portionType.equals("Bag", ignoreCase = true) -> listOf(
            Pair("Full Bag", 1.0),
            Pair("Half Bag (1/2)", 0.5),
            Pair("Quarter Bag (1/4)", 0.25)
        )
        allowPortions && portionType.equals("Crate", ignoreCase = true) -> listOf(
            Pair("Full Crate", 1.0),
            Pair("Half Crate (1/2)", 0.5)
        )
        allowPortions && portionType.equals("Tuber", ignoreCase = true) -> listOf(
            Pair("Full Bunch (5 Tubers)", 1.0),
            Pair("4 Tubers", 0.8),
            Pair("3 Tubers", 0.6),
            Pair("2 Tubers", 0.4),
            Pair("1 Tuber", 0.2)
        )
        allowPortions -> listOf(
            Pair("Full Unit", 1.0),
            Pair("Half Unit (1/2)", 0.5),
            Pair("Quarter Unit (1/4)", 0.25)
        )
        else -> listOf(Pair("Full Unit", 1.0))
    }

    // Initialize selection
    LaunchedEffect(item) {
        selectedPortionName = portions.first().first
        selectedPortionFraction = portions.first().second
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = if (isBundle) "Basket Food Bundle 🧺" else "Purchase Options 🛒",
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    color = TextSlate800
                )
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Product Summary
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(BgSlate50, RoundedCornerShape(12.dp))
                        .border(1.dp, BorderSlate100, RoundedCornerShape(12.dp))
                        .padding(10.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(50.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(PrimaryGreen.copy(alpha = 0.1f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(getCategoryEmoji(item.category), fontSize = 24.sp)
                    }
                    Column(modifier = Modifier.weight(1f)) {
                        Text(item.name, fontWeight = FontWeight.Bold, fontSize = 13.sp, color = TextSlate800)
                        Text("Vendor: ${item.vendorName} • ${item.state}", fontSize = 11.sp, color = TextSlate500)
                    }
                }

                if (isBundle) {
                    // Show Bundle Contents
                    Text("Included in this bundle:", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = TextSlate800)
                    val contents = if (item.bundleItems.isNullOrEmpty()) {
                        listOf("1x 25kg Caprice Rice", "1x 10kg Cassava Garri", "1x Crate of Fresh Eggs", "3x Large Yam Tubers")
                    } else {
                        item.bundleItems.split(",").map { it.trim() }
                    }
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color(0xFFF0FDF4), RoundedCornerShape(10.dp))
                            .padding(10.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        contents.forEach { content ->
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.CheckCircle, contentDescription = null, tint = PrimaryGreen, modifier = Modifier.size(14.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(content, fontSize = 12.sp, color = Color(0xFF15803D), fontWeight = FontWeight.Medium)
                            }
                        }
                    }
                } else if (allowPortions) {
                    // Show Portion Selections
                    Text("Select Quantity / Portion Size:", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = TextSlate800)
                    portions.forEach { (pName, pFraction) ->
                        val pPrice = item.price * pFraction
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(10.dp))
                                .background(if (selectedPortionName == pName) PrimaryGreen.copy(alpha = 0.08f) else Color.White)
                                .border(
                                    width = 1.dp,
                                    color = if (selectedPortionName == pName) PrimaryGreen else BorderSlate100,
                                    shape = RoundedCornerShape(10.dp)
                                )
                                .clickable {
                                    selectedPortionName = pName
                                    selectedPortionFraction = pFraction
                                }
                                .padding(12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                RadioButton(
                                    selected = selectedPortionName == pName,
                                    onClick = {
                                        selectedPortionName = pName
                                        selectedPortionFraction = pFraction
                                    },
                                    colors = RadioButtonDefaults.colors(selectedColor = PrimaryGreen)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = pName,
                                    fontWeight = if (selectedPortionName == pName) FontWeight.Bold else FontWeight.Normal,
                                    fontSize = 13.sp,
                                    color = TextSlate800
                                )
                            }
                            Text(
                                text = "₦${String.format("%,.0f", pPrice)}",
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp,
                                color = if (selectedPortionName == pName) PrimaryGreen else TextSlate800
                            )
                        }
                    }
                } else {
                    // Regular full unit item
                    Text(
                        text = "This item is sold as a full package/unit. Enjoy wholesale rates directly from the source.",
                        fontSize = 12.sp,
                        color = TextSlate500
                    )
                }

                // Pricing Summary Box
                Spacer(modifier = Modifier.height(4.dp))
                val calculatedPrice = item.price * selectedPortionFraction
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0xFFFFF7ED), RoundedCornerShape(10.dp))
                        .border(1.dp, Color(0xFFFFEDD5), RoundedCornerShape(10.dp))
                        .padding(12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Calculated Subtotal:", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = SecondaryOrange)
                    Text("₦${String.format("%,.2f", calculatedPrice)}", fontWeight = FontWeight.Black, fontSize = 16.sp, color = SecondaryOrange)
                }
            }
        },
        confirmButton = {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(
                    onClick = { onConfirmPortion(selectedPortionName, selectedPortionFraction, false) },
                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryGreen),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text("Add to Basket", fontWeight = FontWeight.Bold, color = Color.White, fontSize = 12.sp)
                }
                Button(
                    onClick = { onConfirmPortion(selectedPortionName, selectedPortionFraction, true) },
                    colors = ButtonDefaults.buttonColors(containerColor = SecondaryOrange),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text("Buy Now", fontWeight = FontWeight.Bold, color = Color.White, fontSize = 12.sp)
                }
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = TextSlate500, fontWeight = FontWeight.Bold)
            }
        }
    )
}

@Composable
fun CartDialog(
    viewModel: KoboViewModel,
    onDismiss: () -> Unit
) {
    var selectedMethod by remember { mutableStateOf("Wallet") }
    var customDeliveryFeeOffer by remember { mutableStateOf(950.0) }
    val paymentMethods = listOf("Wallet", "Debit Card", "Bank Transfer", "Split Payment")
    val wallet by viewModel.walletState.collectAsStateWithLifecycle()
    val cartItems by viewModel.cartState.collectAsStateWithLifecycle()

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { 
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Your Food Basket", fontWeight = FontWeight.Bold)
                if (cartItems.isNotEmpty()) {
                    TextButton(onClick = { viewModel.clearCart() }) {
                        Text("Clear All", color = Color.Red, fontSize = 12.sp)
                    }
                }
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .verticalScroll(rememberScrollState())
                    .fillMaxWidth()
            ) {
                if (cartItems.isEmpty()) {
                    EmptyStateCard(
                        message = "Your Food Basket is Empty",
                        subMessage = "Add foodstuffs from the Soko marketplace to start a secure joint-checkout.",
                        icon = Icons.Default.ShoppingCart,
                        iconColor = Color(0xFF0A8F3D)
                    )
                } else {
                    Text(
                        text = "Secure joint-checkout with Escrow Protection.",
                        fontSize = 12.sp,
                        color = TextSlate500
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    // Items List
                    cartItems.forEach { cartItem ->
                        Card(
                            colors = CardDefaults.cardColors(containerColor = SurfaceBg),
                            border = BorderStroke(1.dp, BorderSlate100),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    val displayName = if (cartItem.selectedPortionName == "Full Unit" || cartItem.selectedPortionName == "Full Bundle" || cartItem.selectedPortionName.isEmpty()) cartItem.item.name else "${cartItem.selectedPortionName} of ${cartItem.item.name}"
                                    val unitPrice = cartItem.item.price * cartItem.selectedPortionFraction
                                    Text(
                                        text = displayName,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 13.sp,
                                        color = TextSlate800
                                    )
                                    Text(
                                        text = "by ${cartItem.item.vendorName}",
                                        fontSize = 10.sp,
                                        color = TextSlate500
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = "₦${String.format("%,.0f", unitPrice)} each",
                                        fontWeight = FontWeight.SemiBold,
                                        fontSize = 11.sp,
                                        color = PrimaryGreen
                                    )
                                }

                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    IconButton(
                                        onClick = { viewModel.removeFromCart(cartItem.item) },
                                        modifier = Modifier
                                            .size(28.dp)
                                            .background(Color(0xFFF1F5F9), CircleShape)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Remove,
                                            contentDescription = "Decrease",
                                            modifier = Modifier.size(14.dp),
                                            tint = TextSlate800
                                        )
                                    }

                                    Text(
                                        text = cartItem.quantity.toString(),
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 14.sp,
                                        modifier = Modifier.padding(horizontal = 6.dp)
                                    )

                                    IconButton(
                                        onClick = { viewModel.addToCart(cartItem.item) },
                                        modifier = Modifier
                                            .size(28.dp)
                                            .background(Color(0xFFF1F5F9), CircleShape)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Add,
                                            contentDescription = "Increase",
                                            modifier = Modifier.size(14.dp),
                                            tint = TextSlate800
                                        )
                                    }
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // AfriSav Rider Bid system Custom Starting Delivery Fee Offer Card
                    Card(
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF7ED)),
                        border = BorderStroke(1.dp, Color(0xFFFFEDD5)),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text(
                                "AfriSav Rider Bid system - Customize Rider Payout Offer 🚴",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFFC2410C)
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                "Set a starting delivery fee to offer nearby dispatch riders. It cannot be lower than the base price of ₦950.",
                                fontSize = 10.sp,
                                color = Color(0xFF9A3412)
                            )
                            Spacer(modifier = Modifier.height(10.dp))
                            
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                // Decrement button
                                Box(
                                    modifier = Modifier
                                        .size(36.dp)
                                        .background(Color.White, CircleShape)
                                        .border(1.dp, Color(0xFFFED7AA), CircleShape)
                                        .clickable {
                                            if (customDeliveryFeeOffer > 950.0) {
                                                customDeliveryFeeOffer -= 50.0
                                            }
                                        },
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text("-", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color(0xFFC2410C))
                                }
                                
                                // Display and edit custom delivery offer
                                OutlinedTextField(
                                    value = customDeliveryFeeOffer.toInt().toString(),
                                    onValueChange = { inputVal ->
                                        val entered = inputVal.filter { it.isDigit() }.toDoubleOrNull() ?: 0.0
                                        customDeliveryFeeOffer = entered
                                    },
                                    singleLine = true,
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                    textStyle = TextStyle(fontSize = 14.sp, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center),
                                    modifier = Modifier.weight(1f).height(46.dp),
                                    colors = defaultTextFieldColors(),
                                    shape = RoundedCornerShape(8.dp)
                                )
                                
                                // Increment button
                                Box(
                                    modifier = Modifier
                                        .size(36.dp)
                                        .background(Color.White, CircleShape)
                                        .border(1.dp, Color(0xFFFED7AA), CircleShape)
                                        .clickable {
                                            customDeliveryFeeOffer += 50.0
                                        },
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text("+", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color(0xFFC2410C))
                                }
                            }
                            
                            if (customDeliveryFeeOffer < 950.0) {
                                customDeliveryFeeOffer = 950.0
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Calculations
                    val rawSubtotal = cartItems.sumOf { it.item.price * it.selectedPortionFraction * it.quantity }
                    val salesFee = rawSubtotal * 0.025
                    val serviceCharge = 50.0
                    val deliveryFee = customDeliveryFeeOffer
                    val totalCost = rawSubtotal + salesFee + serviceCharge + deliveryFee

                    Card(
                        colors = CardDefaults.cardColors(containerColor = BgSlate50),
                        border = BorderStroke(1.dp, BorderSlate100)
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("Subtotal:", fontSize = 11.sp, color = Color.Gray)
                                Text("₦${String.format("%,.2f", rawSubtotal)}", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = TextSlate800)
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("Sales Fee (2.5%):", fontSize = 11.sp, color = Color.Gray)
                                Text("₦${String.format("%,.2f", salesFee)}", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFFEA580C))
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("Service Charge:", fontSize = 11.sp, color = Color.Gray)
                                Text("₦${String.format("%,.2f", serviceCharge)}", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFFEA580C))
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("Delivery Fee:", fontSize = 11.sp, color = Color.Gray)
                                Text("₦${String.format("%,.2f", deliveryFee)}", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = TextSlate800)
                            }
                            Divider(color = BorderSlate100, modifier = Modifier.padding(vertical = 8.dp))
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("Total Escrow Balance:", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1E293B))
                                Text("₦${String.format("%,.2f", totalCost)}", fontWeight = FontWeight.Black, color = Color(0xFF0A8F3D), fontSize = 14.sp)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))
                    Text("Select Secure Payment Method", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = Color(0xFF1E293B))
                    Spacer(modifier = Modifier.height(8.dp))

                    paymentMethods.forEach { method ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { selectedMethod = method }
                                .padding(vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(selected = selectedMethod == method, onClick = { selectedMethod = method })
                            Column {
                                Text(method, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                if (method == "Wallet") {
                                    Text(
                                        "Bal: ₦${String.format("%,.2f", wallet?.availableBalance ?: 0.0)}",
                                        fontSize = 10.sp,
                                        color = Color(0xFF0A8F3D)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            if (cartItems.isNotEmpty()) {
                Button(
                    onClick = {
                        viewModel.checkoutCart(selectedMethod, customDeliveryFeeOffer)
                        onDismiss()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0A8F3D))
                ) {
                    Text("Place Cart Order")
                }
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Close") }
        }
    )
}

// --- CORE BOTTOM NAVBAR ---
@Composable
fun KoboBottomNavBar(
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
                selected = activeTab == "home",
                onClick = { onTabSelected("home") },
                icon = { Icon(Icons.Default.Home, contentDescription = "Home") },
                label = { Text("Home", fontSize = 11.sp, fontWeight = FontWeight.Medium) },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = PrimaryGreen,
                    selectedTextColor = PrimaryGreen,
                    unselectedIconColor = TextSlate400,
                    unselectedTextColor = TextSlate400,
                    indicatorColor = PrimaryGreen.copy(alpha = 0.12f)
                )
            )
            NavigationBarItem(
                selected = activeTab == "wallet",
                onClick = { onTabSelected("wallet") },
                icon = { Icon(Icons.Default.AccountBalanceWallet, contentDescription = "Wallet") },
                label = { Text("Wallet", fontSize = 11.sp, fontWeight = FontWeight.Medium) },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = PrimaryGreen,
                    selectedTextColor = PrimaryGreen,
                    unselectedIconColor = TextSlate400,
                    unselectedTextColor = TextSlate400,
                    indicatorColor = PrimaryGreen.copy(alpha = 0.12f)
                )
            )
            NavigationBarItem(
                selected = activeTab == "goals",
                onClick = { onTabSelected("goals") },
                icon = { Icon(Icons.Default.Savings, contentDescription = "Goals") },
                label = { Text("Goals", fontSize = 11.sp, fontWeight = FontWeight.Medium) },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = PrimaryGreen,
                    selectedTextColor = PrimaryGreen,
                    unselectedIconColor = TextSlate400,
                    unselectedTextColor = TextSlate400,
                    indicatorColor = PrimaryGreen.copy(alpha = 0.12f)
                )
            )
            NavigationBarItem(
                selected = activeTab == "market",
                onClick = { onTabSelected("market") },
                icon = { Icon(Icons.Default.Storefront, contentDescription = "Market") },
                label = { Text("Market", fontSize = 11.sp, fontWeight = FontWeight.Medium) },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = PrimaryGreen,
                    selectedTextColor = PrimaryGreen,
                    unselectedIconColor = TextSlate400,
                    unselectedTextColor = TextSlate400,
                    indicatorColor = PrimaryGreen.copy(alpha = 0.12f)
                )
            )
            NavigationBarItem(
                selected = activeTab == "circles",
                onClick = { onTabSelected("circles") },
                icon = { Icon(Icons.Default.Groups, contentDescription = "Circles") },
                label = { Text("Circles", fontSize = 11.sp, fontWeight = FontWeight.Medium) },
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

// --- PLACEHOLDER HELPERS ---
@Composable
fun EmptyStateCard(
    message: String,
    btnText: String = "",
    onClick: (() -> Unit)? = null,
    icon: androidx.compose.ui.graphics.vector.ImageVector? = null,
    emoji: String = "🥑",
    subMessage: String = "",
    iconColor: Color = Color(0xFF0A8F3D)
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = SurfaceBg),
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 32.dp, horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Elegant background ring pattern with main icon/emoji
            Box(
                modifier = Modifier
                    .size(72.dp)
                    .clip(CircleShape)
                    .background(iconColor.copy(alpha = 0.08f)),
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier
                        .size(54.dp)
                        .clip(CircleShape)
                        .background(iconColor.copy(alpha = 0.12f)),
                    contentAlignment = Alignment.Center
                ) {
                    if (icon != null) {
                        Icon(
                            imageVector = icon,
                            contentDescription = null,
                            tint = iconColor,
                            modifier = Modifier.size(26.dp)
                        )
                    } else {
                        Text(
                            text = emoji,
                            fontSize = 26.sp
                        )
                    }
                }
            }
            
            // Intelligent context-aware illustrative stock imagery
            val illustrationType = when {
                message.lowercase().contains("goal") || message.lowercase().contains("savings") -> "goals"
                message.lowercase().contains("history") || message.lowercase().contains("orders") || message.lowercase().contains("settlement") -> "history"
                message.lowercase().contains("cart") || message.lowercase().contains("buying") -> "cart"
                message.lowercase().contains("review") || message.lowercase().contains("feedback") -> "reviews"
                else -> null
            }
            
            illustrationType?.let { t ->
                Spacer(modifier = Modifier.height(14.dp))
                Box(
                    modifier = Modifier
                        .size(width = 160.dp, height = 100.dp)
                        .clip(RoundedCornerShape(12.dp))
                ) {
                    EmptyStateIllustration(
                        type = t,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Text(
                text = message,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF1E293B),
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = 12.dp)
            )
            
            if (subMessage.isNotEmpty()) {
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = subMessage,
                    fontSize = 12.sp,
                    color = Color(0xFF64748B),
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(horizontal = 12.dp)
                )
            }
            
            if (btnText.isNotEmpty() && onClick != null) {
                Spacer(modifier = Modifier.height(18.dp))
                Button(
                    onClick = onClick,
                    colors = ButtonDefaults.buttonColors(containerColor = iconColor),
                    shape = RoundedCornerShape(12.dp),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
                ) {
                    Text(btnText, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.White)
                }
            }
        }
    }
}

fun getCategoryEmoji(category: String): String {
    return when (category.lowercase()) {
        "rice" -> "🌾"
        "beans" -> "🫘"
        "garri" -> "🥣"
        "tomatoes" -> "🍅"
        "pepper" -> "🌶️"
        "yam" -> "🥔"
        "fish" -> "🐟"
        "vegetables" -> "🥦"
        "bundle" -> "🧺"
        else -> "🍛"
    }
}

// Redesigned SellerDashboardScreen has been modularized and moved to SellerDashboardRedesign.kt

// A simple utility data class to aggregate trade logs
data class HistoryItem(
    val isSale: Boolean,
    val title: String,
    val subtitle: String,
    val amount: Double
)

// --- DISPATCH RIDER DASHBOARD ---
data class RiderDeliveryItem(
    val id: Int,
    val item: String,
    val vendor: String,
    val buyer: String,
    val pickupAddress: String,
    val deliveryAddress: String,
    val fee: Double,
    val status: String, // "Available", "Accepted", "In Transit", "Delivered"
    val baseFee: Double = 950.0,
    val riderProposedFee: Double = 0.0,
    val riderProposalName: String? = null,
    val isReal: Boolean = false
)

@Composable
fun RiderDashboardScreen(
    viewModel: KoboViewModel,
    onLogoutClick: () -> Unit
) {
    val riderName by viewModel.currentUserName.collectAsStateWithLifecycle()
    val riderPhone by viewModel.currentUserPhone.collectAsStateWithLifecycle()
    val riderEmail by viewModel.currentUserEmail.collectAsStateWithLifecycle()
    val riderLocation by viewModel.currentUserLocation.collectAsStateWithLifecycle()
    val riderBio by viewModel.currentUserBio.collectAsStateWithLifecycle()
    val riderPhoto by viewModel.currentUserPhoto.collectAsStateWithLifecycle()

    val vehicleType by viewModel.riderVehicleType.collectAsStateWithLifecycle()
    val licensePlate by viewModel.riderPlate.collectAsStateWithLifecycle()
    val securityPin by viewModel.riderPin.collectAsStateWithLifecycle()

    var isOnline by remember { mutableStateOf(true) }
    var todayEarnings by remember { mutableStateOf(16800.0) }
    var completedTrips by remember { mutableStateOf(8) }

    var pinPromptOrder by remember { mutableStateOf<RiderDeliveryItem?>(null) }
    var enteredPin by remember { mutableStateOf("") }
    var pinErrorText by remember { mutableStateOf("") }

    var activeRiderTab by remember { mutableStateOf("dashboard") }

    var deliveries by remember {
        mutableStateOf(
            listOf(
                RiderDeliveryItem(
                    id = 1,
                    item = "Bag of Honey Beans (Oloyin)",
                    vendor = "Salami Stores (Mushin Market)",
                    buyer = "Adebayo Alao (Gbagada)",
                    pickupAddress = "Stall 14, Mushin Food Market, Lagos",
                    deliveryAddress = "Block 4A, Gbagada Phase 2, Lagos",
                    fee = 2500.0,
                    status = "Available"
                ),
                RiderDeliveryItem(
                    id = 2,
                    item = "Basket of Fresh Tomatoes & Habanero Pepper",
                    vendor = "Ibrahim Agro (Mile 12 Market)",
                    buyer = "Chioma Nze (Ikeja)",
                    pickupAddress = "Main Depot, Mile 12 Agricultural Market, Lagos",
                    deliveryAddress = "15 Allen Avenue, Ikeja, Lagos",
                    fee = 1800.0,
                    status = "Available"
                ),
                RiderDeliveryItem(
                    id = 3,
                    item = "Tubers of Abuja Yam (5x) & Palm Oil (5L)",
                    vendor = "Mama Ejima Foods (Oyingbo Market)",
                    buyer = "Olumide Bakare (Surulere)",
                    pickupAddress = "Yam Section, Oyingbo Modern Market, Lagos",
                    deliveryAddress = "42 Adeniran Ogunsanya St, Surulere, Lagos",
                    fee = 3200.0,
                    status = "Available"
                )
            )
        )
    }

    val allEscrowOrders by viewModel.allEscrowOrders.collectAsStateWithLifecycle()
    val riderType by viewModel.riderType.collectAsStateWithLifecycle()
    val dispatchCompany by viewModel.dispatchCompanyName.collectAsStateWithLifecycle()
    val riderBalance by viewModel.riderBalance.collectAsStateWithLifecycle()
    val withdrawals by viewModel.riderWithdrawals.collectAsStateWithLifecycle()
    val allReviews by viewModel.allReviews.collectAsStateWithLifecycle()

    // Map real escrow orders to RiderDeliveryItems if pending and (not assigned OR assigned to me)
    val realDeliveries = allEscrowOrders
        .filter { it.status == "PENDING" && (it.riderName.isNullOrEmpty() || it.riderName.equals(riderName, ignoreCase = true)) }
        .map { esc ->
            RiderDeliveryItem(
                id = esc.id,
                item = esc.itemName,
                vendor = "${esc.vendorName} (Market)",
                buyer = esc.buyerName,
                pickupAddress = "Vendor Stall, Lagos Market",
                deliveryAddress = "Buyer Residence Address, Lagos",
                fee = esc.deliveryFee,
                status = if (esc.isPickedUp) "In Transit"
                         else if (!esc.riderName.isNullOrEmpty()) "Accepted"
                         else "Available",
                baseFee = esc.baseDeliveryFee,
                riderProposedFee = esc.riderProposedFee,
                riderProposalName = esc.riderProposalName,
                isReal = true
            )
        }

    // Active deliveries in progress (Accepted or In Transit)
    val activeDeliveriesInProgress = (realDeliveries + deliveries.filter { d -> d.status != "Delivered" && realDeliveries.none { r -> r.id == d.id } })
        .filter { it.status == "Accepted" || it.status == "In Transit" }

    Scaffold(
        contentWindowInsets = androidx.compose.foundation.layout.WindowInsets(0, 0, 0, 0),
        bottomBar = {
            RiderBottomNavBar(
                activeTab = activeRiderTab,
                onTabSelected = { activeRiderTab = it }
            )
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(SoftBackground)
        ) {
            when (activeRiderTab) {
                "dashboard" -> {
                    RiderDashboardContent(
                        viewModel = viewModel,
                        riderName = riderName,
                        riderPhone = riderPhone,
                        isOnline = isOnline,
                        onOnlineToggle = { isOnline = it },
                        todayEarnings = todayEarnings,
                        completedTrips = completedTrips,
                        deliveries = deliveries,
                        onDeliveriesChange = { deliveries = it },
                        realDeliveries = realDeliveries,
                        riderType = riderType,
                        dispatchCompany = dispatchCompany,
                        riderBalance = riderBalance,
                        withdrawals = withdrawals,
                        activeDeliveriesInProgress = activeDeliveriesInProgress,
                        onActionClick = { item -> pinPromptOrder = item },
                        allReviews = allReviews,
                        onLogoutClick = onLogoutClick,
                        onNavigateToTab = { activeRiderTab = it }
                    )
                }
                "history" -> {
                    RiderHistoryContent(
                        riderName = riderName,
                        allEscrowOrders = allEscrowOrders
                    )
                }
                "reviews" -> {
                    RiderReviewsContent(
                        riderName = riderName,
                        allReviews = allReviews
                    )
                }
                "profile" -> {
                    RiderProfileContent(
                        viewModel = viewModel,
                        riderName = riderName,
                        riderPhone = riderPhone,
                        riderEmail = riderEmail,
                        riderLocation = riderLocation,
                        riderBio = riderBio,
                        vehicleType = vehicleType,
                        licensePlate = licensePlate,
                        securityPin = securityPin,
                        completedTrips = completedTrips,
                        allReviews = allReviews,
                        onLogoutClick = onLogoutClick
                    )
                }
            }
        }
    }

    if (pinPromptOrder != null) {
        val order = pinPromptOrder!!
        val isPickup = order.status == "Accepted"
        val dialogTitle = if (isPickup) "Verify Cargo Pickup" else "Confirm Delivery & Inspection"
        val helpText = if (isPickup) {
            "Please ask the seller for their secure 4-digit Pickup PIN to verify cargo transfer."
        } else {
            "Upon inspection and complete buyer satisfaction, ask the buyer for their secure 4-digit Delivery PIN to release escrow funds."
        }

        AlertDialog(
            onDismissRequest = {
                pinPromptOrder = null
                enteredPin = ""
                pinErrorText = ""
            },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = if (isPickup) Icons.Default.LockOpen else Icons.Default.CheckCircle,
                        contentDescription = null,
                        tint = if (isPickup) Color(0xFFEA580C) else Color(0xFF16A34A)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(dialogTitle, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                }
            },
            text = {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Text(helpText, fontSize = 12.sp, color = TextSlate500)
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    OutlinedTextField(
                        value = enteredPin,
                        onValueChange = { if (it.length <= 4) enteredPin = it },
                        label = { Text("4-Digit Verification PIN") },
                        placeholder = { Text("e.g. 1234") },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth().testTag("verification_pin_input"),
                        colors = defaultTextFieldColors(),
                        shape = RoundedCornerShape(10.dp)
                    )

                    if (pinErrorText.isNotEmpty()) {
                        Text(
                            text = pinErrorText,
                            color = MaterialTheme.colorScheme.error,
                            fontSize = 11.sp,
                            modifier = Modifier.padding(top = 8.dp)
                        )
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val isSimulated = order.id in listOf(1, 2, 3)
                        if (isSimulated) {
                            if (enteredPin == "1234" || enteredPin == securityPin) {
                                deliveries = deliveries.map { d ->
                                    if (d.id == order.id) {
                                        val nextStatus = if (d.status == "Accepted") "In Transit" else "Delivered"
                                        if (nextStatus == "Delivered") {
                                            todayEarnings += d.fee
                                            completedTrips += 1
                                        }
                                        d.copy(status = nextStatus)
                                    } else d
                                }
                                pinPromptOrder = null
                                enteredPin = ""
                                pinErrorText = ""
                            } else {
                                pinErrorText = "Incorrect PIN. (Tip: Use 1234 or your security PIN)."
                            }
                        } else {
                            if (isPickup) {
                                viewModel.verifyRiderPickup(order.id, enteredPin) { success, msg ->
                                    if (success) {
                                        pinPromptOrder = null
                                        enteredPin = ""
                                        pinErrorText = ""
                                    } else {
                                        pinErrorText = msg
                                    }
                                }
                            } else {
                                viewModel.verifyRiderDelivery(order.id, enteredPin) { success, msg ->
                                    if (success) {
                                        todayEarnings += order.fee
                                        completedTrips += 1
                                        pinPromptOrder = null
                                        enteredPin = ""
                                        pinErrorText = ""
                                    } else {
                                        pinErrorText = msg
                                    }
                                }
                            }
                        }
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isPickup) Color(0xFFEA580C) else Color(0xFF16A34A)
                    ),
                    modifier = Modifier.testTag("submit_pin_verification_btn")
                ) {
                    Text("Verify & Proceed")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        pinPromptOrder = null
                        enteredPin = ""
                        pinErrorText = ""
                    }
                ) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
fun OrderDeliveryTracker(
    order: EscrowOrder,
    viewModel: KoboViewModel,
    modifier: Modifier = Modifier
) {
    val isDarkMode by viewModel.isDarkMode.collectAsStateWithLifecycle()
    val textPrimary = if (isDarkMode) Color(0xFFF8FAFC) else Color(0xFF0F172A)
    val textSecondary = if (isDarkMode) Color(0xFF94A3B8) else Color(0xFF64748B)
    val cardBg = if (isDarkMode) Color(0xFF1E293B) else Color.White
    val borderColor = if (isDarkMode) Color(0xFF334155) else Color(0xFFE2E8F0)
    
    val steps = listOf(
        "CONFIRMED" to "Order Confirmed",
        "PREPARING" to "Vendor Packing",
        "READY_FOR_PICKUP" to "Ready for Pickup",
        "IN_TRANSIT" to "In Transit",
        "ARRIVED" to "Arrived Doorstep"
    )
    
    val currentStatus = order.deliveryStatus
    val activeStepIndex = steps.indexOfFirst { it.first == currentStatus }.coerceAtLeast(0)
    
    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("order_delivery_tracker_${order.id}"),
        colors = CardDefaults.cardColors(containerColor = cardBg),
        border = BorderStroke(1.dp, borderColor),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Header: ETA & Status Title
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Real-Time Delivery Journey 🚚",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = textPrimary
                    )
                    Text(
                        text = "Bulk Order Tracker #${order.id}",
                        fontSize = 11.sp,
                        color = textSecondary
                    )
                }
                
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color(0xFFECFDF5))
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = if (order.currentEtaMinutes > 0) "${order.currentEtaMinutes} mins ETA" else "Arrived! 📍",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF059669)
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Horizontal stepper
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                steps.forEachIndexed { index, step ->
                    val isCompleted = index < activeStepIndex
                    val isActive = index == activeStepIndex
                    val color = when {
                        isActive -> Color(0xFF8B5CF6) // Active step purple
                        isCompleted -> Color(0xFF059669) // Completed step green
                        else -> Color(0xFF94A3B8) // Pending step grey
                    }
                    
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.weight(1f)
                    ) {
                        // Dot with icon or number
                        Box(
                            modifier = Modifier
                                .size(28.dp)
                                .clip(CircleShape)
                                .background(color.copy(alpha = 0.15f))
                                .border(
                                    width = if (isActive) 2.dp else 1.dp,
                                    color = color,
                                    shape = CircleShape
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            if (isCompleted) {
                                Icon(
                                    imageVector = Icons.Default.Check,
                                    contentDescription = null,
                                    tint = color,
                                    modifier = Modifier.size(14.dp)
                                )
                            } else {
                                Text(
                                    text = (index + 1).toString(),
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = color
                                )
                            }
                        }
                        
                        Spacer(modifier = Modifier.height(4.dp))
                        
                        Text(
                            text = step.second,
                            fontSize = 8.sp,
                            fontWeight = if (isActive) FontWeight.Bold else FontWeight.Medium,
                            color = color,
                            textAlign = TextAlign.Center,
                            maxLines = 1
                        )
                    }
                    
                    if (index < steps.size - 1) {
                        val lineColor = if (index < activeStepIndex) Color(0xFF059669) else Color(0xFFCBD5E1)
                        Box(
                            modifier = Modifier
                                .height(2.dp)
                                .weight(0.5f)
                                .background(lineColor)
                        )
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Map integration if In Transit or Arrived
            if (currentStatus == "IN_TRANSIT" || currentStatus == "ARRIVED") {
                LiveDeliveryMap(modifier = Modifier.height(110.dp))
                Spacer(modifier = Modifier.height(12.dp))
            }
            
            // Dispatch Rider Profile Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = if (isDarkMode) Color(0xFF1E293B) else Color(0xFFF8FAFC)),
                border = BorderStroke(1.dp, borderColor),
                shape = RoundedCornerShape(12.dp)
            ) {
                Row(
                    modifier = Modifier.padding(10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Rider avatar / icon
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(Color(0xFFF5F3FF)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("🚴", fontSize = 18.sp)
                    }
                    
                    Spacer(modifier = Modifier.width(10.dp))
                    
                    Column(modifier = Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = order.riderName?.ifEmpty { "Adekunle (AfriSav Rider)" } ?: "Adekunle (AfriSav Rider)",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = textPrimary
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Icon(
                                imageVector = Icons.Default.Star,
                                contentDescription = null,
                                tint = SecondaryOrange,
                                modifier = Modifier.size(12.dp)
                            )
                            Spacer(modifier = Modifier.width(2.dp))
                            val riderRating = viewModel.getRiderRating(order.riderName)
                            Text(
                                text = String.format("%.1f", riderRating),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = textPrimary
                            )
                        }
                        Text(
                            text = "Tricycle (Keke) • ${order.riderPlate}",
                            fontSize = 10.sp,
                            color = textSecondary
                        )
                    }
                    
                    // Contact Buttons
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        IconButton(
                            onClick = { /* Simulated Call */ },
                            modifier = Modifier.size(32.dp).background(Color(0xFFDEF7EC), CircleShape)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Phone,
                                contentDescription = "Call Rider",
                                tint = Color(0xFF03543F),
                                modifier = Modifier.size(14.dp)
                            )
                        }
                        IconButton(
                            onClick = { /* Simulated Chat */ },
                            modifier = Modifier.size(32.dp).background(Color(0xFFE1EFFE), CircleShape)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Send,
                                contentDescription = "Chat Rider",
                                tint = Color(0xFF1E40AF),
                                modifier = Modifier.size(14.dp)
                            )
                        }
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            // Interactive Simulation Controls
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFFFFFBEB), RoundedCornerShape(12.dp))
                    .border(1.dp, Color(0xFFFDE68A), RoundedCornerShape(12.dp))
                    .padding(10.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("⚙️", fontSize = 14.sp)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Interactive Tracker Simulator",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF92400E)
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Test the live routing & stepper journey from marketplace vendor pickup to doorstep in real-time.",
                    fontSize = 10.sp,
                    color = Color(0xFFB45309),
                    lineHeight = 14.sp
                )
                Spacer(modifier = Modifier.height(10.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = { viewModel.simulateDeliveryStep(order.id) },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD97706)),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.weight(1f).height(32.dp)
                    ) {
                        Text("Next Step 🚴", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color.White)
                    }
                    
                    Button(
                        onClick = { viewModel.startAutoDeliverySimulation(order.id) },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1E3A8A)),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.weight(1f).height(32.dp)
                    ) {
                        Text("Auto Play 🚀", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color.White)
                    }
                }
            }
        }
    }
}

@Composable
fun LiveDeliveryMap(modifier: Modifier = Modifier) {
    val infiniteTransition = rememberInfiniteTransition(label = "LiveMap")
    val progress by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 4000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "RiderProgress"
    )

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(100.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(Color(0xFFF1F5F9))
            .border(1.dp, Color(0xFFE2E8F0), RoundedCornerShape(8.dp))
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val w = size.width
            val h = size.height

            // Draw clean path road
            val path = Path().apply {
                moveTo(w * 0.1f, h * 0.5f)
                quadraticTo(w * 0.5f, h * 0.2f, w * 0.9f, h * 0.5f)
            }
            
            // Draw background route line
            drawPath(
                path = path,
                color = Color(0xFFCBD5E1),
                style = Stroke(
                    width = 6f, 
                    pathEffect = PathEffect.dashPathEffect(floatArrayOf(15f, 10f), 0f)
                )
            )

            // Draw seller point (Lagos Wholesale Room)
            drawCircle(
                color = Color(0xFF0A8F3D),
                radius = 12f,
                center = Offset(w * 0.1f, h * 0.5f)
            )

            // Draw buyer point (Home Delivery Spot)
            drawCircle(
                color = Color(0xFF2563EB),
                radius = 12f,
                center = Offset(w * 0.9f, h * 0.5f)
            )

            // Animate Rider courier icon along the path
            val t = progress
            val riderX = (1 - t) * (1 - t) * (w * 0.1f) + 2 * (1 - t) * t * (w * 0.5f) + t * t * (w * 0.9f)
            val riderY = (1 - t) * (1 - t) * (h * 0.5f) + 2 * (1 - t) * t * (h * 0.2f) + t * t * (h * 0.5f)

            // Draw animated courier delivery bubble
            drawCircle(
                color = Color(0xFFFF8C00),
                radius = 12f,
                center = Offset(riderX, riderY)
            )
            
            // Courier icon accent line
            drawCircle(
                color = Color.White,
                radius = 5f,
                center = Offset(riderX, riderY)
            )
        }
        
        Row(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .background(Color.White.copy(alpha = 0.85f))
                .padding(vertical = 4.dp, horizontal = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("📍 Seller Hub", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = Color(0xFF0A8F3D))
            Text("⚡ Dispatch Rider Moving Live", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = Color(0xFFFF8C00))
            Text("🏠 Your Home", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = Color(0xFF2563EB))
        }
    }
}

@Composable
fun EscrowCompletionDialog(
    escrow: EscrowOrder,
    onDismiss: () -> Unit,
    onConfirm: (vendorRating: Int, vendorReview: String, buyerRating: Int, buyerReview: String, riderRating: Int, riderReview: String) -> Unit
) {
    var vendorRating by remember { mutableStateOf(5) }
    var vendorReviewText by remember { mutableStateOf("") }
    
    var buyerRating by remember { mutableStateOf(5) }
    var buyerReviewText by remember { mutableStateOf("Excellent buyer, fast transaction and prompt pickup!") }

    var riderRating by remember { mutableStateOf(if (!escrow.riderName.isNullOrEmpty()) 5 else 0) }
    var riderReviewText by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.CheckCircle, contentDescription = null, tint = PrimaryGreen, modifier = Modifier.size(24.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("Complete Escrow & Rate", fontSize = 18.sp, fontWeight = FontWeight.Bold)
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
            ) {
                Text(
                    text = "Confirming receipt releases ₦${String.format("%,.0f", escrow.amount)} securely to ${escrow.vendorName}. Under the escrow contract, please complete the mutual ratings below.",
                    fontSize = 12.sp,
                    color = Color(0xFF475569)
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                Divider(color = Color(0xFFF1F5F9))
                Spacer(modifier = Modifier.height(16.dp))
                
                // --- RATE SELLER/VENDOR ---
                Text("1. Rate Vendor (${escrow.vendorName})", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = Color(0xFF1E293B))
                Spacer(modifier = Modifier.height(6.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    for (i in 1..5) {
                        Icon(
                            imageVector = Icons.Default.Star,
                            contentDescription = "Star $i",
                            tint = if (i <= vendorRating) Color(0xFFFF8C00) else Color.LightGray,
                            modifier = Modifier
                                .size(28.dp)
                                .clickable { vendorRating = i }
                        )
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = vendorReviewText,
                    onValueChange = { vendorReviewText = it },
                    label = { Text("Vendor Review Comment", fontSize = 12.sp) },
                    placeholder = { Text("How was the food item and packaging?") },
                    modifier = Modifier.fillMaxWidth(),
                    colors = defaultTextFieldColors(),
                    shape = RoundedCornerShape(8.dp)
                )

                Spacer(modifier = Modifier.height(20.dp))
                
                // --- RATE BUYER ---
                Text("2. Vendor's Rating of You (Buyer)", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = Color(0xFF1E293B))
                Text("Peer ratings help maintain high-trust profiles on AfriSav.", fontSize = 11.sp, color = Color(0xFF64748B))
                Spacer(modifier = Modifier.height(6.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    for (i in 1..5) {
                        Icon(
                            imageVector = Icons.Default.Star,
                            contentDescription = "Star $i",
                            tint = if (i <= buyerRating) SecondaryOrange else Color.LightGray,
                            modifier = Modifier
                                .size(28.dp)
                                .clickable { buyerRating = i }
                        )
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = buyerReviewText,
                    onValueChange = { buyerReviewText = it },
                    label = { Text("Vendor's Comment on Buyer", fontSize = 12.sp) },
                    placeholder = { Text("Feedback about the buyer interaction") },
                    modifier = Modifier.fillMaxWidth(),
                    colors = defaultTextFieldColors(),
                    shape = RoundedCornerShape(8.dp)
                )

                if (!escrow.riderName.isNullOrEmpty()) {
                    Spacer(modifier = Modifier.height(20.dp))
                    Text("3. Rate Dispatch Rider (${escrow.riderName})", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = Color(0xFF1E293B))
                    Text("How was the delivery and handling?", fontSize = 11.sp, color = Color(0xFF64748B))
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        for (i in 1..5) {
                            Icon(
                                imageVector = Icons.Default.Star,
                                contentDescription = "Star $i",
                                tint = if (i <= riderRating) Color(0xFFFF8C00) else Color.LightGray,
                                modifier = Modifier
                                    .size(28.dp)
                                    .clickable { riderRating = i }
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = riderReviewText,
                        onValueChange = { riderReviewText = it },
                        label = { Text("Rider Review Comment", fontSize = 12.sp) },
                        placeholder = { Text("Fast delivery and polite service!") },
                        modifier = Modifier.fillMaxWidth(),
                        colors = defaultTextFieldColors(),
                        shape = RoundedCornerShape(8.dp)
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    onConfirm(vendorRating, vendorReviewText, buyerRating, buyerReviewText, riderRating, riderReviewText)
                },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0A8F3D))
            ) {
                Text("Confirm & Release Funds")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@Composable
fun BuyerLeaveReviewDialog(
    order: EscrowOrder,
    targetType: String,
    onDismiss: () -> Unit,
    onSubmit: (rating: Int, comment: String) -> Unit
) {
    var rating by remember { mutableStateOf(5) }
    var comment by remember { mutableStateOf("") }
    val targetName = if (targetType == "VENDOR") order.vendorName else (order.riderName ?: "Rider")

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.RateReview, contentDescription = null, tint = PrimaryGreen, modifier = Modifier.size(24.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("Review $targetName", fontSize = 18.sp, fontWeight = FontWeight.Bold)
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
            ) {
                Text(
                    text = if (targetType == "VENDOR") "Rate your experience with vendor ${order.vendorName} for item: ${order.itemName}."
                           else "Rate delivery service provided by rider ${order.riderName}.",
                    fontSize = 12.sp,
                    color = Color(0xFF475569)
                )
                Spacer(modifier = Modifier.height(16.dp))
                Divider(color = Color(0xFFF1F5F9))
                Spacer(modifier = Modifier.height(16.dp))

                Text("Star Rating (1 - 5)", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = Color(0xFF1E293B))
                Spacer(modifier = Modifier.height(6.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    for (i in 1..5) {
                        Icon(
                            imageVector = Icons.Default.Star,
                            contentDescription = "Star $i",
                            tint = if (i <= rating) Color(0xFFFF8C00) else Color.LightGray,
                            modifier = Modifier
                                .size(32.dp)
                                .clickable { rating = i }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))
                OutlinedTextField(
                    value = comment,
                    onValueChange = { comment = it },
                    label = { Text("Comment / Feedback (Optional)", fontSize = 12.sp) },
                    placeholder = { Text(if (targetType == "VENDOR") "Great food quality and clean packaging!" else "Fast and polite delivery!") },
                    modifier = Modifier.fillMaxWidth().testTag("buyer_review_comment_input"),
                    colors = defaultTextFieldColors(),
                    shape = RoundedCornerShape(8.dp)
                )
            }
        },
        confirmButton = {
            Button(
                onClick = { onSubmit(rating, comment.trim()) },
                colors = ButtonDefaults.buttonColors(containerColor = PrimaryGreen),
                modifier = Modifier.testTag("buyer_review_submit_btn")
            ) {
                Text("Submit Review")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PreFilledGoalDialog(
    initialTitle: String,
    initialTargetAmount: Double,
    initialCategory: String,
    initialAutoSaveAmount: Double,
    onDismiss: () -> Unit,
    onConfirm: (String, Double, String, Double, Boolean) -> Unit
) {
    var title by remember { mutableStateOf(initialTitle) }
    var targetText by remember { mutableStateOf(initialTargetAmount.toInt().toString()) }
    var category by remember { mutableStateOf(initialCategory) }
    var autoSaveAmtText by remember { mutableStateOf(initialAutoSaveAmount.toInt().toString()) }
    var autoSaveEnabled by remember { mutableStateOf(true) }

    val categories = listOf("Rice", "Beans", "Garri", "Tomatoes", "Yam", "Fish")
    var catExpanded by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.CheckCircle, contentDescription = null, tint = Color(0xFF0A8F3D), modifier = Modifier.size(24.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("Convert Basket to Goal", fontSize = 18.sp, fontWeight = FontWeight.Bold)
            }
        },
        text = {
            Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                Text(
                    "We have prefilled your savings target based on your calculated basket budget. You can customize the name, auto-save plan, and start saving!",
                    fontSize = 12.sp,
                    color = Color(0xFF64748B)
                )
                Spacer(modifier = Modifier.height(16.dp))

                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Goal Title (e.g. Lagos Family Basket)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(12.dp))

                ExposedDropdownMenuBox(
                    expanded = catExpanded,
                    onExpandedChange = { catExpanded = !catExpanded }
                ) {
                    OutlinedTextField(
                        value = category,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Food Category") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = catExpanded) },
                        modifier = Modifier
                            .menuAnchor()
                            .fillMaxWidth()
                    )
                    ExposedDropdownMenu(
                        expanded = catExpanded,
                        onDismissRequest = { catExpanded = false }
                    ) {
                        categories.forEach { cat ->
                            DropdownMenuItem(
                                text = { Text(cat) },
                                onClick = {
                                    category = cat
                                    catExpanded = false
                                }
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = targetText,
                    onValueChange = { targetText = it },
                    label = { Text("Target Amount (₦)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(12.dp))

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(checked = autoSaveEnabled, onCheckedChange = { autoSaveEnabled = it })
                    Text("Enable Auto-Save Plan", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                }

                if (autoSaveEnabled) {
                    OutlinedTextField(
                        value = autoSaveAmtText,
                        onValueChange = { autoSaveAmtText = it },
                        label = { Text("Daily Automated Savings (₦)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val target = targetText.toDoubleOrNull() ?: 0.0
                    val autoAmt = autoSaveAmtText.toDoubleOrNull() ?: 500.0
                    if (title.isNotEmpty() && target > 0) {
                        onConfirm(title, target, category, autoAmt, autoSaveEnabled)
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0A8F3D))
            ) {
                Text("Create Savings Goal")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@Composable
fun SkeletonBox(
    modifier: Modifier,
    shape: androidx.compose.ui.graphics.Shape = RoundedCornerShape(8.dp)
) {
    val infiniteTransition = rememberInfiniteTransition(label = "skeleton_shimmer")
    val alpha by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = 0.9f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 800, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "alpha"
    )
    Box(
        modifier = modifier
            .background(Color.LightGray.copy(alpha = alpha), shape)
    )
}

@Composable
fun MarketItemSkeletonCard() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = SurfaceBg),
        shape = RoundedCornerShape(14.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            SkeletonBox(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(110.dp),
                shape = RoundedCornerShape(10.dp)
            )
            Spacer(modifier = Modifier.height(10.dp))
            SkeletonBox(
                modifier = Modifier
                    .fillMaxWidth(0.7f)
                    .height(16.dp)
            )
            Spacer(modifier = Modifier.height(6.dp))
            SkeletonBox(
                modifier = Modifier
                    .fillMaxWidth(0.4f)
                    .height(12.dp)
            )
            Spacer(modifier = Modifier.height(12.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                SkeletonBox(
                    modifier = Modifier
                        .width(60.dp)
                        .height(18.dp)
                )
                SkeletonBox(
                    modifier = Modifier
                        .width(70.dp)
                        .height(28.dp),
                    shape = RoundedCornerShape(8.dp)
                )
            }
        }
    }
}

@Composable
fun MarketSkeleton() {
    LazyVerticalGrid(
        columns = GridCells.Fixed(2),
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(6) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = SurfaceBg),
                shape = RoundedCornerShape(14.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    SkeletonBox(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(110.dp),
                        shape = RoundedCornerShape(10.dp)
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    SkeletonBox(
                        modifier = Modifier
                            .fillMaxWidth(0.7f)
                            .height(16.dp)
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    SkeletonBox(
                        modifier = Modifier
                            .fillMaxWidth(0.4f)
                            .height(12.dp)
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        SkeletonBox(
                            modifier = Modifier
                                .width(60.dp)
                                .height(18.dp)
                        )
                        SkeletonBox(
                            modifier = Modifier
                                .width(70.dp)
                                .height(28.dp),
                            shape = RoundedCornerShape(8.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun WalletSkeleton() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF5F8F6))
            .padding(16.dp)
            .verticalScroll(rememberScrollState())
    ) {
        SkeletonBox(modifier = Modifier.width(180.dp).height(24.dp))
        Spacer(modifier = Modifier.height(4.dp))
        SkeletonBox(modifier = Modifier.width(240.dp).height(14.dp))

        Spacer(modifier = Modifier.height(16.dp))

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF065A26).copy(alpha = 0.8f)),
            shape = RoundedCornerShape(20.dp)
        ) {
            Column(modifier = Modifier.padding(24.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        SkeletonBox(modifier = Modifier.width(150.dp).height(10.dp), shape = RoundedCornerShape(4.dp))
                        Spacer(modifier = Modifier.height(6.dp))
                        SkeletonBox(modifier = Modifier.width(100.dp).height(14.dp), shape = RoundedCornerShape(4.dp))
                    }
                    Box(modifier = Modifier.size(28.dp).background(Color.White.copy(alpha = 0.2f), CircleShape))
                }

                Spacer(modifier = Modifier.height(28.dp))

                SkeletonBox(modifier = Modifier.width(80.dp).height(12.dp))
                Spacer(modifier = Modifier.height(6.dp))
                SkeletonBox(modifier = Modifier.width(160.dp).height(30.dp))

                Spacer(modifier = Modifier.height(20.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    SkeletonBox(modifier = Modifier.width(180.dp).height(12.dp))
                    SkeletonBox(modifier = Modifier.width(60.dp).height(12.dp))
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            SkeletonBox(modifier = Modifier.weight(1f).height(50.dp), shape = RoundedCornerShape(12.dp))
            SkeletonBox(modifier = Modifier.weight(1f).height(50.dp), shape = RoundedCornerShape(12.dp))
        }

        Spacer(modifier = Modifier.height(30.dp))

        SkeletonBox(modifier = Modifier.width(150.dp).height(20.dp))
        Spacer(modifier = Modifier.height(12.dp))

        repeat(3) {
            Card(
                modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
                colors = CardDefaults.cardColors(containerColor = SurfaceBg),
                shape = RoundedCornerShape(12.dp)
            ) {
                Row(
                    modifier = Modifier.padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .background(Color.LightGray.copy(alpha = 0.2f), CircleShape)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        SkeletonBox(modifier = Modifier.width(120.dp).height(14.dp))
                        Spacer(modifier = Modifier.height(6.dp))
                        SkeletonBox(modifier = Modifier.width(80.dp).height(10.dp))
                    }
                    SkeletonBox(modifier = Modifier.width(60.dp).height(14.dp))
                }
            }
        }
    }
}

@Composable
fun EditProfileDialog(
    viewModel: KoboViewModel,
    onDismiss: () -> Unit,
    onLogoutClick: () -> Unit
) {
    val currentName by viewModel.currentUserName.collectAsStateWithLifecycle()
    val currentPhone by viewModel.currentUserPhone.collectAsStateWithLifecycle()
    val currentEmail by viewModel.currentUserEmail.collectAsStateWithLifecycle()
    val currentLocation by viewModel.currentUserLocation.collectAsStateWithLifecycle()
    val currentBio by viewModel.currentUserBio.collectAsStateWithLifecycle()
    val userRole by viewModel.userRole.collectAsStateWithLifecycle()
    val currentUserPhoto by viewModel.currentUserPhoto.collectAsStateWithLifecycle()

    val trackedIds by viewModel.trackedItemIds.collectAsStateWithLifecycle()
    val initialPrices by viewModel.trackedItemInitialPrices.collectAsStateWithLifecycle()
    val marketItems by viewModel.marketItems.collectAsStateWithLifecycle()

    var name by remember { mutableStateOf(currentName) }
    var phone by remember { mutableStateOf(currentPhone) }
    var email by remember { mutableStateOf(currentEmail) }
    var location by remember { mutableStateOf(currentLocation) }
    var bio by remember { mutableStateOf(currentBio) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.AccountCircle,
                    contentDescription = null,
                    tint = PrimaryGreen,
                    modifier = Modifier.size(28.dp)
                )
                Text(
                    text = "Edit Profile Info",
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    color = TextSlate800
                )
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "Update your public profile displayed to other members of the cooperative.",
                    fontSize = 11.sp,
                    color = TextSlate500
                )

                if (currentUserPhoto != null) {
                    Box(
                        modifier = Modifier
                            .size(72.dp)
                            .align(Alignment.CenterHorizontally)
                            .clip(CircleShape)
                            .border(2.dp, PrimaryGreen, CircleShape)
                    ) {
                        AsyncImage(
                            model = currentUserPhoto,
                            contentDescription = "Profile Photo",
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                    }
                }

                // Name field
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Full Name", fontSize = 12.sp) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = PrimaryGreen,
                        focusedLabelColor = PrimaryGreen
                    )
                )

                // Phone field
                OutlinedTextField(
                    value = phone,
                    onValueChange = { phone = it },
                    label = { Text("Phone Number", fontSize = 12.sp) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = PrimaryGreen,
                        focusedLabelColor = PrimaryGreen
                    )
                )

                // Email field
                OutlinedTextField(
                    value = email,
                    onValueChange = { email = it },
                    label = { Text("Email Address", fontSize = 12.sp) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = PrimaryGreen,
                        focusedLabelColor = PrimaryGreen
                    )
                )

                // Location field
                OutlinedTextField(
                    value = location,
                    onValueChange = { location = it },
                    label = { Text("Primary Cooperative Hub / Location", fontSize = 12.sp) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = PrimaryGreen,
                        focusedLabelColor = PrimaryGreen
                    )
                )

                // Bio field
                OutlinedTextField(
                    value = bio,
                    onValueChange = { bio = it },
                    label = { Text("Short Bio", fontSize = 12.sp) },
                    maxLines = 3,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = PrimaryGreen,
                        focusedLabelColor = PrimaryGreen
                    )
                )

                // Display Role as non-editable info badge
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = BgSlate50),
                    border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Active System Role:", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = TextSlate500)
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(4.dp))
                                .background(PrimaryGreen.copy(alpha = 0.15f))
                                .padding(horizontal = 8.dp, vertical = 2.dp)
                        ) {
                            Text(userRole.uppercase(), fontSize = 10.sp, fontWeight = FontWeight.Bold, color = PrimaryGreen)
                        }
                    }
                }

                // Tracked price items list inside user profile
                Divider(color = Color(0xFFE2E8F0), thickness = 1.dp)

                Text(
                    text = "Tracked Price Items (${trackedIds.size})",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextSlate800,
                    modifier = Modifier.padding(top = 4.dp)
                )

                if (trackedIds.isEmpty()) {
                    Text(
                        text = "You are not tracking any food items yet. Find products in the Marketplace and click 'Track Price'!",
                        fontSize = 11.sp,
                        color = TextSlate500,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                } else {
                    Column(
                        verticalArrangement = Arrangement.spacedBy(6.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        trackedIds.forEach { id ->
                            val item = marketItems.find { it.id == id }
                            if (item != null) {
                                val initialPrice = initialPrices[id] ?: item.price
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .background(Color(0xFFF8FAFC), RoundedCornerShape(8.dp))
                                        .border(1.dp, Color(0xFFE2E8F0), RoundedCornerShape(8.dp))
                                        .padding(horizontal = 10.dp, vertical = 8.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = item.name,
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = TextSlate800
                                        )
                                        Text(
                                            text = "Initial price: ₦${String.format("%,.0f", initialPrice)} | Current: ₦${String.format("%,.0f", item.price)}",
                                            fontSize = 10.sp,
                                            color = if (item.price < initialPrice) Color(0xFF16A34A) else TextSlate500,
                                            fontWeight = if (item.price < initialPrice) FontWeight.Bold else FontWeight.Normal
                                        )
                                    }
                                    IconButton(
                                        onClick = { viewModel.toggleTrackPrice(item) },
                                        modifier = Modifier.size(24.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Delete,
                                            contentDescription = "Stop Tracking",
                                            tint = Color.Red,
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (name.isNotBlank() && phone.isNotBlank()) {
                        viewModel.updateUserProfile(
                            name = name,
                            phone = phone,
                            email = email,
                            location = location,
                            bio = bio
                        )
                        onDismiss()
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = PrimaryGreen),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text("Save Profile", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                TextButton(
                    onClick = onLogoutClick,
                    colors = ButtonDefaults.textButtonColors(contentColor = Color(0xFFEF4444))
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.Logout,
                        contentDescription = "Sign Out",
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Sign Out", fontWeight = FontWeight.Bold)
                }
                
                TextButton(onClick = onDismiss) {
                    Text("Cancel", color = TextSlate500)
                }
            }
        }
    )
}

data class ConfettiParticle(
    val x: Float,
    val y: Float,
    val vx: Float,
    val vy: Float,
    val color: Color,
    val size: Float,
    val isCircle: Boolean,
    val rotationSpeed: Float
)

@Composable
fun SuccessModalDialog(
    state: SuccessModalState,
    onDismiss: () -> Unit
) {
    Dialog(
        onDismissRequest = onDismiss
    ) {
        val infiniteTransition = rememberInfiniteTransition(label = "ConfettiAnimation")
        val animTime by infiniteTransition.animateFloat(
            initialValue = 0f,
            targetValue = 1f,
            animationSpec = infiniteRepeatable(
                animation = tween(durationMillis = 2800, easing = LinearEasing),
                repeatMode = RepeatMode.Restart
            ),
            label = "ConfettiTime"
        )

        val colors = listOf(
            Color(0xFF22C55E), // Green
            Color(0xFF3B82F6), // Blue
            Color(0xFFFBBF24), // Gold/Yellow
            Color(0xFFEF4444), // Red
            Color(0xFFEC4899), // Pink
            Color(0xFFA855F7)  // Purple
        )

        val particles = remember {
            List(70) {
                val angle = (0..360).random() * Math.PI / 180.0
                val speed = (3..12).random().toFloat()
                ConfettiParticle(
                    x = 0.5f,
                    y = 0.35f,
                    vx = (kotlin.math.cos(angle) * speed).toFloat(),
                    vy = (kotlin.math.sin(angle) * speed).toFloat() - (1..4).random().toFloat(),
                    color = colors.random(),
                    size = (6..16).random().toFloat(),
                    isCircle = (0..1).random() == 0,
                    rotationSpeed = (-8..8).random().toFloat()
                )
            }
        }

        var startAnim by remember { mutableStateOf(false) }
        LaunchedEffect(Unit) {
            startAnim = true
        }
        val scale by animateFloatAsState(
            targetValue = if (startAnim) 1f else 0.3f,
            animationSpec = spring(
                dampingRatio = Spring.DampingRatioMediumBouncy,
                stiffness = Spring.StiffnessLow
            ),
            label = "ScaleAnimation"
        )

        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .wrapContentHeight(),
            shape = RoundedCornerShape(28.dp),
            colors = CardDefaults.cardColors(containerColor = SurfaceBg),
            elevation = CardDefaults.cardElevation(defaultElevation = 12.dp)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                contentAlignment = Alignment.Center
            ) {
                Canvas(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(220.dp)
                ) {
                    val w = size.width
                    val h = size.height
                    
                    particles.forEach { p ->
                        val t = animTime * 65f
                        val curX = (w * p.x) + (p.vx * t)
                        val curY = (h * p.y) + (p.vy * t) + (0.28f * t * t)
                        
                        val alpha = (1f - animTime).coerceIn(0f, 1f)
                        
                        if (curX in 0f..w && curY in 0f..h) {
                            drawContext.canvas.save()
                            drawContext.canvas.translate(curX, curY)
                            drawContext.canvas.rotate(p.rotationSpeed * t)
                            if (p.isCircle) {
                                drawCircle(
                                    color = p.color.copy(alpha = alpha),
                                    radius = p.size / 2f
                                )
                            } else {
                                drawRect(
                                    color = p.color.copy(alpha = alpha),
                                    size = androidx.compose.ui.geometry.Size(p.size, p.size / 2f)
                                )
                            }
                            drawContext.canvas.restore()
                        }
                    }
                }

                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Spacer(modifier = Modifier.height(16.dp))

                    Box(
                        modifier = Modifier
                            .size(80.dp)
                            .scale(scale)
                            .background(
                                brush = Brush.radialGradient(
                                    colors = listOf(Color(0xFFE8F5E9), Color(0xFFC8E6C9))
                                ),
                                shape = CircleShape
                            )
                            .border(3.dp, Color(0xFF2E7D32), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = "Success checkmark",
                            tint = Color(0xFF2E7D32),
                            modifier = Modifier.size(44.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    Text(
                        text = state.title,
                        fontWeight = FontWeight.Bold,
                        fontSize = 20.sp,
                        textAlign = TextAlign.Center,
                        color = Color(0xFF1B5E20),
                        modifier = Modifier.padding(horizontal = 8.dp)
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    if (state.amount != null) {
                        Card(
                            colors = CardDefaults.cardColors(containerColor = Color(0xFFF1F5F9)),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.padding(vertical = 4.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.CheckCircle,
                                    contentDescription = null,
                                    tint = Color(0xFF22C55E),
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "₦${String.format("%,.2f", state.amount)}",
                                    fontWeight = FontWeight.ExtraBold,
                                    fontSize = 18.sp,
                                    color = Color(0xFF334155)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(12.dp))
                    }

                    Text(
                        text = state.message,
                        fontSize = 14.sp,
                        textAlign = TextAlign.Center,
                        color = Color(0xFF475569),
                        lineHeight = 20.sp,
                        modifier = Modifier.padding(horizontal = 12.dp)
                    )

                    Spacer(modifier = Modifier.height(28.dp))

                    Button(
                        onClick = onDismiss,
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF22C55E)),
                        shape = RoundedCornerShape(100),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                            .testTag("dismiss_success_modal_button")
                    ) {
                        Text(
                            text = if (state.isGoalHit) "Awesome, thank you! 🎉" else "Dismiss",
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp,
                            color = Color.White
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))
                }
            }
        }
    }
}

@Composable
fun VoiceCommandDialog(
    onDismiss: () -> Unit,
    onSpeechResult: (String) -> Unit
) {
    val context = LocalContext.current
    var isListening by remember { mutableStateOf(false) }
    var currentSpeechText by remember { mutableStateOf("") }
    var rmsLevel by remember { mutableStateOf(0f) }
    var statusText by remember { mutableStateOf("Initializing mic...") }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    // Speech Helper setup
    val speechHelper = remember {
        SpeechRecognizerHelper(
            context = context,
            onStart = {
                isListening = true
                statusText = "Listening... Speak now 🥑"
                errorMessage = null
            },
            onRmsChanged = { rms ->
                rmsLevel = rms
            },
            onPartialResult = { text ->
                currentSpeechText = text
            },
            onResult = { text ->
                currentSpeechText = text
                isListening = false
                statusText = "Speech recognized!"
            },
            onError = { err ->
                isListening = false
                errorMessage = err
                statusText = "Error"
            },
            onEndOfSpeech = {
                isListening = false
                statusText = "Processing speech..."
            }
        )
    }

    // Start listening on launch
    DisposableEffect(Unit) {
        speechHelper.startListening()
        onDispose {
            speechHelper.stopListening()
        }
    }

    // Soundwave visualization pulsing logic
    val scaleFactor by animateFloatAsState(
        targetValue = if (isListening) {
            (1f + (rmsLevel.coerceAtLeast(0f) / 10f)).coerceAtMost(1.8f)
        } else {
            1f
        },
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioLowBouncy,
            stiffness = Spring.StiffnessMedium
        ),
        label = "SoundwaveScale"
    )

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .wrapContentHeight(),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = SurfaceBg),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Mama Basket Voice Command",
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        color = Color(0xFF1E293B)
                    )
                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.size(24.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close Voice Dialog",
                            tint = Color(0xFF64748B)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Pulsing Mic Sphere
                Box(
                    modifier = Modifier
                        .size(100.dp),
                    contentAlignment = Alignment.Center
                ) {
                    // Ripple effect outer circle
                    Box(
                        modifier = Modifier
                            .size(100.dp)
                            .scale(scaleFactor)
                            .background(
                                color = if (isListening) Color(0xFFE8F5E9) else Color(0xFFF1F5F9),
                                shape = CircleShape
                            )
                    )
                    
                    // Ripple effect middle circle
                    Box(
                        modifier = Modifier
                            .size(76.dp)
                            .scale(if (isListening) (scaleFactor + 1f) / 2f else 1f)
                            .background(
                                color = if (isListening) Color(0xFFC8E6C9) else Color(0xFFE2E8F0),
                                shape = CircleShape
                            )
                    )

                    // Inner Mic circle
                    Box(
                        modifier = Modifier
                            .size(56.dp)
                            .background(
                                color = if (isListening) Color(0xFF0A8F3D) else Color(0xFF64748B),
                                shape = CircleShape
                            )
                            .clickable {
                                if (isListening) {
                                    speechHelper.stopListening()
                                    isListening = false
                                    statusText = "Stopped listening."
                                } else {
                                    currentSpeechText = ""
                                    errorMessage = null
                                    speechHelper.startListening()
                                }
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = if (isListening) Icons.Default.Mic else Icons.Default.MicOff,
                            contentDescription = "Microphone Status",
                            tint = Color.White,
                            modifier = Modifier.size(28.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Listening Status Text
                Text(
                    text = statusText,
                    fontWeight = FontWeight.Medium,
                    fontSize = 14.sp,
                    color = if (isListening) Color(0xFF0A8F3D) else Color(0xFF64748B),
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Transcript Window
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 60.dp, max = 120.dp)
                        .background(Color(0xFFF8FAFC), RoundedCornerShape(12.dp))
                        .border(1.dp, Color(0xFFE2E8F0), RoundedCornerShape(12.dp))
                        .padding(12.dp),
                    contentAlignment = Alignment.Center
                ) {
                    if (currentSpeechText.isNotEmpty()) {
                        Text(
                            text = currentSpeechText,
                            fontSize = 14.sp,
                            color = Color(0xFF1E293B),
                            fontWeight = FontWeight.SemiBold,
                            textAlign = TextAlign.Center
                        )
                    } else if (errorMessage != null) {
                        Text(
                            text = errorMessage ?: "",
                            fontSize = 13.sp,
                            color = Color.Red,
                            textAlign = TextAlign.Center
                        )
                    } else {
                        Text(
                            text = "Start speaking...\nMama will write it down for you.",
                            fontSize = 13.sp,
                            color = Color(0xFF94A3B8),
                            textAlign = TextAlign.Center,
                            lineHeight = 18.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Visual hints of what you can say
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.Start
                ) {
                    Text(
                        text = "💡 Try saying:",
                        fontWeight = FontWeight.Bold,
                        fontSize = 11.sp,
                        color = Color(0xFF0A8F3D)
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "• \"How do I save for 50kg rice?\"",
                        fontSize = 11.sp,
                        color = Color(0xFF64748B)
                    )
                    Text(
                        text = "• \"What is a Food Circle?\"",
                        fontSize = 11.sp,
                        color = Color(0xFF64748B)
                    )
                    Text(
                        text = "• \"How do I fund my wallet?\"",
                        fontSize = 11.sp,
                        color = Color(0xFF64748B)
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Action Buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFF64748B)),
                        shape = RoundedCornerShape(100)
                    ) {
                        Text("Cancel", fontWeight = FontWeight.Bold)
                    }

                    Button(
                        onClick = {
                            if (currentSpeechText.isNotEmpty()) {
                                onSpeechResult(currentSpeechText)
                            } else {
                                onDismiss()
                            }
                        },
                        modifier = Modifier
                            .weight(1f)
                            .testTag("submit_voice_button"),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0A8F3D)),
                        shape = RoundedCornerShape(100),
                        enabled = currentSpeechText.isNotEmpty() || errorMessage != null
                    ) {
                        Text("Use Speech", fontWeight = FontWeight.Bold, color = Color.White)
                    }
                }
            }
        }
    }
}

@Composable
fun PriceFluctuationWarningDialog(
    onDismiss: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .wrapContentHeight(),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = SurfaceBg),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier
                        .size(64.dp)
                        .background(Color(0xFFFEF3C7), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.TrendingUp,
                        contentDescription = "Price fluctuation warning",
                        tint = Color(0xFFD97706),
                        modifier = Modifier.size(36.dp)
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "Important Price Advisory",
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    color = Color(0xFF1E293B),
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = "Dear Saver, please be advised that the prices of food items in our local markets can fluctuate due to seasonality, supply, and general market conditions.",
                    fontSize = 14.sp,
                    color = Color(0xFF475569),
                    textAlign = TextAlign.Center,
                    lineHeight = 20.sp
                )

                Spacer(modifier = Modifier.height(12.dp))

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0xFFFFFBEB), RoundedCornerShape(12.dp))
                        .border(1.dp, Color(0xFFFDE68A), RoundedCornerShape(12.dp))
                        .padding(12.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Info,
                            contentDescription = null,
                            tint = Color(0xFFD97706),
                            modifier = Modifier.size(20.dp)
                        )
                        Text(
                            text = "Prices may vary between the day you start your savings and the day you are ready to purchase.",
                            fontSize = 12.sp,
                            color = Color(0xFF92400E),
                            fontWeight = FontWeight.Medium,
                            lineHeight = 16.sp,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "AfriSav automatically compares and coordinates with local wholesalers to find you the absolute cheapest rate when you complete your goal! 😊",
                    fontSize = 13.sp,
                    color = Color(0xFF64748B),
                    textAlign = TextAlign.Center,
                    lineHeight = 18.sp
                )

                Spacer(modifier = Modifier.height(24.dp))

                Button(
                    onClick = onDismiss,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .testTag("price_warning_dismiss_button"),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0A8F3D)),
                    shape = RoundedCornerShape(100)
                ) {
                    Text(
                        text = "I Understand & Agree",
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = Color.White
                    )
                }
            }
        }
    }
}

@Composable
fun SetKoboGoalDialog(
    currentGoal: Double,
    currentFrequency: String,
    onDismiss: () -> Unit,
    onConfirm: (Double, String) -> Unit
) {
    var goalText by remember { mutableStateOf(String.format("%.0f", currentGoal)) }
    var frequency by remember { mutableStateOf(currentFrequency) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Set Naira Goal 🎯", fontWeight = FontWeight.Bold, fontSize = 18.sp) },
        text = {
            Column {
                Text(
                    "Decide how much Naira you want to save per cycle.",
                    fontSize = 12.sp,
                    color = Color(0xFF64748B)
                )
                Spacer(modifier = Modifier.height(16.dp))

                OutlinedTextField(
                    value = goalText,
                    onValueChange = { goalText = it },
                    label = { Text("Target Goal (in ₦)") },
                    placeholder = { Text("e.g. 50000") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth().testTag("kobo_goal_input_field"),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(16.dp))

                Text("Goal Frequency", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1E293B))
                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    listOf("Daily", "Weekly").forEach { freq ->
                        val isSelected = frequency == freq
                        Button(
                            onClick = { frequency = freq },
                            modifier = Modifier.weight(1f).testTag("freq_button_$freq"),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (isSelected) Color(0xFF0A8F3D) else Color(0xFFF1F5F9)
                            ),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text(
                                text = freq,
                                color = if (isSelected) Color.White else Color(0xFF475569),
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val goalVal = goalText.toDoubleOrNull() ?: 50000.0
                    onConfirm(goalVal, frequency)
                },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0A8F3D)),
                modifier = Modifier.testTag("kobo_goal_save_confirm")
            ) {
                Text("Save Target")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss, modifier = Modifier.testTag("kobo_goal_save_cancel")) {
                Text("Cancel")
            }
        }
    )
}

@Composable
fun KoboContributeDialog(
    availableBalanceInNaira: Double,
    onDismiss: () -> Unit,
    onConfirm: (Double) -> Unit
) {
    var amountText by remember { mutableStateOf("") }
    var showConfirmDialog by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Contribute Naira 💰", fontWeight = FontWeight.Bold, fontSize = 18.sp) },
        text = {
            Column {
                Text(
                    "Available in spendable wallet: ₦${String.format("%,.2f", availableBalanceInNaira)}",
                    fontSize = 12.sp,
                    color = Color(0xFF64748B)
                )
                Spacer(modifier = Modifier.height(16.dp))

                OutlinedTextField(
                    value = amountText,
                    onValueChange = { amountText = it },
                    label = { Text("Contribution Amount (in ₦)") },
                    placeholder = { Text("e.g. 500") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth().testTag("kobo_contribute_input_field"),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Predefined quick contributions
                Text("Quick Options", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFF64748B))
                Spacer(modifier = Modifier.height(6.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    listOf(500.0, 1000.0, 5000.0).forEach { quickAmt ->
                        OutlinedButton(
                            onClick = { amountText = String.format("%.0f", quickAmt) },
                            modifier = Modifier.weight(1f).testTag("quick_kobo_$quickAmt"),
                            shape = RoundedCornerShape(8.dp),
                            border = BorderStroke(1.dp, Color(0xFFCBD5E1)),
                            contentPadding = PaddingValues(horizontal = 4.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = "+₦${String.format("%,.0f", quickAmt)}",
                                fontSize = 10.sp,
                                color = Color(0xFF0A8F3D),
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val amtVal = amountText.toDoubleOrNull() ?: 0.0
                    if (amtVal > 0) {
                        showConfirmDialog = true
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0A8F3D)),
                modifier = Modifier.testTag("kobo_contribute_confirm")
            ) {
                Text("Contribute")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss, modifier = Modifier.testTag("kobo_contribute_cancel")) {
                Text("Cancel")
            }
        }
    )

    if (showConfirmDialog) {
        val amtVal = amountText.toDoubleOrNull() ?: 0.0
        AlertDialog(
            onDismissRequest = { showConfirmDialog = false },
            title = { Text("Confirm Transfer") },
            text = {
                Text("Are you sure you want to deposit/transfer ₦${String.format("%,.2f", amtVal)} into your savings balance?")
            },
            confirmButton = {
                Button(
                    onClick = {
                        onConfirm(amtVal)
                        showConfirmDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0A8F3D)),
                    modifier = Modifier.testTag("confirm_transfer_button")
                ) {
                    Text("Confirm Transfer")
                }
            },
            dismissButton = {
                TextButton(onClick = { showConfirmDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

// --- SIMULATED CALL OVERLAY FOR RIDER CONTACT ---
@Composable
fun SimulatedCallOverlay(
    riderName: String,
    onDismiss: () -> Unit
) {
    var callDurationSeconds by remember { mutableStateOf(0) }
    var isCallMuted by remember { mutableStateOf(false) }
    var isSpeakerOn by remember { mutableStateOf(false) }

    LaunchedEffect(key1 = true) {
        while (true) {
            delay(1000)
            callDurationSeconds++
        }
    }

    val minutes = callDurationSeconds / 60
    val seconds = callDurationSeconds % 60
    val timeString = String.format("%02d:%02d", minutes, seconds)

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFF0F172A)) // Deep slate phone dialer background
                .padding(24.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxHeight()
            ) {
                // Top section: Call Status and Contact
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.padding(top = 48.dp)
                ) {
                    Text(
                        text = "AFRISAV SECURE LINE",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF4ADE80),
                        letterSpacing = 1.5.sp
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    // Avatar
                    Box(
                        modifier = Modifier
                            .size(100.dp)
                            .background(Color(0xFF1E293B), CircleShape)
                            .border(2.dp, Color(0xFF4ADE80), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("🚴", fontSize = 48.sp)
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = riderName,
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Connected • $timeString",
                        fontSize = 14.sp,
                        color = Color(0xFF94A3B8)
                    )
                }

                // Middle section: Audio controls grid
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(24.dp)
                ) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(36.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Mute
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            IconButton(
                                onClick = { isCallMuted = !isCallMuted },
                                modifier = Modifier
                                    .size(60.dp)
                                    .background(
                                        if (isCallMuted) Color.White else Color(0xFF1E293B),
                                        CircleShape
                                    )
                            ) {
                                Icon(
                                    imageVector = if (isCallMuted) Icons.Default.MicOff else Icons.Default.Mic,
                                    contentDescription = "Mute",
                                    tint = if (isCallMuted) Color(0xFF0F172A) else Color.White,
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                            Spacer(modifier = Modifier.height(6.dp))
                            Text("Mute", fontSize = 11.sp, color = Color(0xFF94A3B8))
                        }

                        // Speaker
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            IconButton(
                                onClick = { isSpeakerOn = !isSpeakerOn },
                                modifier = Modifier
                                    .size(60.dp)
                                    .background(
                                        if (isSpeakerOn) Color.White else Color(0xFF1E293B),
                                        CircleShape
                                    )
                            ) {
                                Icon(
                                    imageVector = Icons.Default.VolumeUp,
                                    contentDescription = "Speaker",
                                    tint = if (isSpeakerOn) Color(0xFF0F172A) else Color.White,
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                            Spacer(modifier = Modifier.height(6.dp))
                            Text("Speaker", fontSize = 11.sp, color = Color(0xFF94A3B8))
                        }
                    }
                }

                // Bottom section: End Call Button
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.padding(bottom = 48.dp)
                ) {
                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier
                            .size(72.dp)
                            .background(Color(0xFFEF4444), CircleShape)
                    ) {
                        Icon(
                            imageVector = Icons.Default.CallEnd,
                            contentDescription = "End Call",
                            tint = Color.White,
                            modifier = Modifier.size(36.dp)
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "End Call",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFFEF4444)
                    )
                }
            }
        }
    }
}

// --- SIMULATED CHAT DIALOG FOR RIDER CONTACT ---
@Composable
fun SimulatedChatDialog(
    riderName: String,
    onDismiss: () -> Unit
) {
    var messages by remember {
        mutableStateOf(
            listOf(
                ChatMessage(text = "Hello! I am your AfriSav Dispatch Rider. I have accepted your delivery.", isUser = false),
                ChatMessage(text = "Please let me know if you have any special pickup/delivery instructions!", isUser = false)
            )
        )
    }
    var typedText by remember { mutableStateOf("") }
    val listState = rememberLazyListState()

    LaunchedEffect(messages.size) {
        if (messages.isNotEmpty()) {
            listState.animateScrollToItem(messages.size - 1)
        }
    }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.8f),
            colors = CardDefaults.cardColors(containerColor = SurfaceBg),
            shape = RoundedCornerShape(16.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                // Header
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(PrimaryGreen)
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .background(Color.White.copy(alpha = 0.2f), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("🚴", fontSize = 18.sp)
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text(riderName, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color.White)
                            Text("Online • AfriSav Logistics", fontSize = 10.sp, color = Color.White.copy(alpha = 0.8f))
                        }
                    }
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Close chat", tint = Color.White)
                    }
                }

                // Messages area
                LazyColumn(
                    state = listState,
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .padding(12.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(messages) { msg ->
                        RiderChatBubble(msg = msg)
                    }
                }

                // Quick options row
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 4.dp),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    listOf("Are you near?", "I am ready!", "Thank you!").forEach { phrase ->
                        Box(
                            modifier = Modifier
                                .border(1.dp, Color(0xFFE2E8F0), RoundedCornerShape(12.dp))
                                .clickable {
                                    messages = messages + ChatMessage(text = phrase, isUser = true)
                                    val replyText = when (phrase) {
                                        "Are you near?" -> "Yes, I am heading towards the vendor stall now. Be there in 5 mins."
                                        "I am ready!" -> "Great! Safe logistics is my priority. Moving now!"
                                        else -> "You are welcome! Have a wonderful day."
                                    }
                                    messages = messages + ChatMessage(text = replyText, isUser = false)
                                }
                                .padding(horizontal = 10.dp, vertical = 6.dp)
                        ) {
                            Text(phrase, fontSize = 11.sp, color = Color(0xFF475569))
                        }
                    }
                }

                // Input Bar
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = typedText,
                        onValueChange = { typedText = it },
                        placeholder = { Text("Type message...", fontSize = 12.sp) },
                        modifier = Modifier
                            .weight(1f)
                            .height(50.dp),
                        singleLine = true,
                        colors = defaultTextFieldColors(),
                        shape = RoundedCornerShape(24.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    IconButton(
                        onClick = {
                            if (typedText.isNotBlank()) {
                                val userMsg = typedText
                                messages = messages + ChatMessage(text = userMsg, isUser = true)
                                typedText = ""
                                
                                val responses = listOf(
                                    "Understood! I'm on it.",
                                    "Copy that, arriving shortly.",
                                    "Noted! Safe deliveries are my priority.",
                                    "Perfect, thank you for clarifying!"
                                )
                                messages = messages + ChatMessage(text = responses.random(), isUser = false)
                            }
                        },
                        modifier = Modifier
                            .size(44.dp)
                            .background(PrimaryGreen, CircleShape)
                    ) {
                        Icon(Icons.Default.Send, contentDescription = "Send message", tint = Color.White, modifier = Modifier.size(18.dp))
                    }
                }
            }
        }
    }
}

// --- CUSTOM CHAT BUBBLE FOR RIDER CHAT ---
@Composable
fun RiderChatBubble(msg: ChatMessage) {
    val alignRight = msg.isUser
    val bubbleColor = if (alignRight) Color(0xFF16A34A) else Color(0xFFF1F5F9)
    val textColor = if (alignRight) Color.White else Color(0xFF1E293B)
    val alignment = if (alignRight) Alignment.End else Alignment.Start

    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = alignment
    ) {
        Row(
            verticalAlignment = Alignment.Bottom,
            horizontalArrangement = if (alignRight) Arrangement.End else Arrangement.Start
        ) {
            if (!alignRight) {
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .background(Color(0xFF16A34A).copy(alpha = 0.15f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text("🚴", fontSize = 16.sp)
                }
                Spacer(modifier = Modifier.width(6.dp))
            }

            Card(
                colors = CardDefaults.cardColors(containerColor = bubbleColor),
                shape = RoundedCornerShape(
                    topStart = 12.dp,
                    topEnd = 12.dp,
                    bottomStart = if (alignRight) 12.dp else 2.dp,
                    bottomEnd = if (alignRight) 2.dp else 12.dp
                ),
                border = if (!alignRight) BorderStroke(1.dp, Color(0xFFE2E8F0)) else null,
                modifier = Modifier.widthIn(max = 240.dp)
            ) {
                Text(
                    text = msg.text,
                    color = textColor,
                    fontSize = 12.sp,
                    modifier = Modifier.padding(10.dp)
                )
            }
        }
    }
}

@Composable
fun ThemeToggle(
    isDarkMode: Boolean,
    onToggle: () -> Unit,
    modifier: Modifier = Modifier
) {
    IconButton(
        onClick = onToggle,
        modifier = modifier
            .size(40.dp)
            .background(
                color = if (isDarkMode) Color(0xFF334155) else Color(0xFFF1F5F9),
                shape = CircleShape
            )
            .testTag("theme_toggle_btn")
    ) {
        Icon(
            imageVector = if (isDarkMode) Icons.Default.LightMode else Icons.Default.DarkMode,
            contentDescription = if (isDarkMode) "Switch Theme Mode" else "Switch Theme Mode",
            tint = if (isDarkMode) Color(0xFFFDE047) else Color(0xFF475569),
            modifier = Modifier.size(20.dp)
        )
    }
}

@Composable
fun SavingsReminderCard(
    viewModel: KoboViewModel,
    onRequestPermission: (onPermissionGranted: () -> Unit) -> Unit,
    modifier: Modifier = Modifier
) {
    val reminderEnabled by viewModel.reminderEnabled.collectAsStateWithLifecycle()
    val reminderHour by viewModel.reminderHour.collectAsStateWithLifecycle()
    val reminderMinute by viewModel.reminderMinute.collectAsStateWithLifecycle()
    val isDarkMode by viewModel.isDarkMode.collectAsStateWithLifecycle()
    val context = LocalContext.current
    
    val cardBg = if (isDarkMode) Color(0xFF1E1E1E) else Color.White
    val textColor = if (isDarkMode) Color(0xFFF8FAFC) else Color(0xFF1E293B)
    val subtitleColor = if (isDarkMode) Color(0xFF94A3B8) else Color(0xFF64748B)
    val borderColor = if (isDarkMode) Color(0xFF334155) else Color(0xFFE2E8F0)
    
    var selectedHour by remember(reminderHour) { mutableStateOf(reminderHour) }
    var selectedMinute by remember(reminderMinute) { mutableStateOf(reminderMinute) }
    
    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("savings_reminder_card"),
        colors = CardDefaults.cardColors(containerColor = cardBg),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(1.dp, borderColor)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .background(PrimaryGreen.copy(alpha = 0.15f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("⏰", fontSize = 20.sp)
                    }
                    Column {
                        Text(
                            text = "Daily Savings Reminder",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = textColor
                        )
                        Text(
                            text = if (reminderEnabled) {
                                "Set for ${String.format("%02d:%02d %s", if (reminderHour % 12 == 0) 12 else reminderHour % 12, reminderMinute, if (reminderHour >= 12) "PM" else "AM")}"
                            } else {
                                "Never miss your savings goals"
                            },
                            fontSize = 12.sp,
                            color = subtitleColor
                        )
                    }
                }
                
                Switch(
                    checked = reminderEnabled,
                    onCheckedChange = { checked ->
                        if (checked) {
                            onRequestPermission {
                                viewModel.setReminderEnabled(true)
                            }
                        } else {
                            viewModel.setReminderEnabled(false)
                        }
                    },
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = Color.White,
                        checkedTrackColor = PrimaryGreen,
                        uncheckedThumbColor = subtitleColor,
                        uncheckedTrackColor = if (isDarkMode) Color(0xFF334155) else Color(0xFFE2E8F0)
                    ),
                    modifier = Modifier.testTag("savings_reminder_switch")
                )
            }
            
            if (reminderEnabled) {
                Spacer(modifier = Modifier.height(14.dp))
                Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(borderColor.copy(alpha = 0.5f)))
                Spacer(modifier = Modifier.height(14.dp))
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Reminder Time",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium,
                        color = textColor
                    )
                    
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        // Hour selector
                        Row(
                            modifier = Modifier
                                .background(if (isDarkMode) Color(0xFF2D3748) else Color(0xFFF1F5F9), RoundedCornerShape(8.dp))
                                .border(1.dp, borderColor, RoundedCornerShape(8.dp))
                                .padding(horizontal = 10.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Text(
                                text = String.format("%02d", selectedHour),
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = textColor
                            )
                            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                                Icon(
                                    imageVector = Icons.Default.KeyboardArrowUp,
                                    contentDescription = "Increase Hour",
                                    tint = PrimaryGreen,
                                    modifier = Modifier
                                        .size(16.dp)
                                        .clickable {
                                            selectedHour = (selectedHour + 1) % 24
                                            viewModel.setReminderTime(selectedHour, selectedMinute)
                                        }
                                )
                                Icon(
                                    imageVector = Icons.Default.KeyboardArrowDown,
                                    contentDescription = "Decrease Hour",
                                    tint = PrimaryGreen,
                                    modifier = Modifier
                                        .size(16.dp)
                                        .clickable {
                                            selectedHour = if (selectedHour == 0) 23 else selectedHour - 1
                                            viewModel.setReminderTime(selectedHour, selectedMinute)
                                        }
                                )
                            }
                        }
                        
                        Text(":", fontWeight = FontWeight.Bold, color = textColor)
                        
                        // Minute selector
                        Row(
                            modifier = Modifier
                                .background(if (isDarkMode) Color(0xFF2D3748) else Color(0xFFF1F5F9), RoundedCornerShape(8.dp))
                                .border(1.dp, borderColor, RoundedCornerShape(8.dp))
                                .padding(horizontal = 10.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Text(
                                text = String.format("%02d", selectedMinute),
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = textColor
                            )
                            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                                Icon(
                                    imageVector = Icons.Default.KeyboardArrowUp,
                                    contentDescription = "Increase Minute",
                                    tint = PrimaryGreen,
                                    modifier = Modifier
                                        .size(16.dp)
                                        .clickable {
                                            selectedMinute = (selectedMinute + 5) % 60
                                            viewModel.setReminderTime(selectedHour, selectedMinute)
                                        }
                                )
                                Icon(
                                    imageVector = Icons.Default.KeyboardArrowDown,
                                    contentDescription = "Decrease Minute",
                                    tint = PrimaryGreen,
                                    modifier = Modifier
                                        .size(16.dp)
                                        .clickable {
                                            selectedMinute = if (selectedMinute < 5) 55 else selectedMinute - 5
                                            viewModel.setReminderTime(selectedHour, selectedMinute)
                                        }
                                )
                            }
                        }
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            // Trigger Test Notification Button
            OutlinedButton(
                onClick = { 
                    onRequestPermission {
                        viewModel.triggerTestReminder()
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("trigger_test_reminder_button"),
                border = BorderStroke(1.dp, SecondaryOrange.copy(alpha = 0.5f)),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = SecondaryOrange),
                shape = RoundedCornerShape(10.dp)
            ) {
                Row(
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("🔔 ", fontSize = 14.sp)
                    Text(
                        text = "Test Notification Instantly",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

@Composable
fun DispatchCompanyDashboardScreen(
    viewModel: KoboViewModel,
    onLogoutClick: () -> Unit
) {
    val companyName by viewModel.currentUserName.collectAsStateWithLifecycle()
    val companyPhone by viewModel.currentUserPhone.collectAsStateWithLifecycle()
    val companyRiders by viewModel.companyRiders.collectAsStateWithLifecycle()

    var showAddRiderDialog by remember { mutableStateOf(false) }
    var showRemoveRiderDialog by remember { mutableStateOf<CompanyRiderInfo?>(null) }
    var showCompanyWithdrawDialog by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BgSlate50)
            .verticalScroll(rememberScrollState())
    ) {
        // Top Header Section
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFF0F172A))
                .statusBarsPadding()
                .padding(top = 20.dp, start = 16.dp, end = 16.dp, bottom = 24.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(50.dp)
                            .background(PrimaryGreen.copy(alpha = 0.2f), CircleShape)
                            .border(2.dp, PrimaryGreen, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = if (companyName.isNotEmpty()) companyName.take(1).uppercase() else "C",
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 20.sp
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = companyName,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Text(
                            text = "Logistics Dispatch Partner • $companyPhone",
                            fontSize = 12.sp,
                            color = Color.White.copy(alpha = 0.7f)
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

        Column(modifier = Modifier.padding(16.dp)) {
            // Stats card showing total active fleet and combined balance
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = SurfaceBg),
                shape = RoundedCornerShape(12.dp),
                border = BorderStroke(1.dp, Color(0xFFE2E8F0))
            ) {
                Row(
                    modifier = Modifier.padding(16.dp).fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("ACTIVE RIDERS", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color(0xFF64748B))
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(text = "${companyRiders.size} Active", fontSize = 18.sp, fontWeight = FontWeight.ExtraBold, color = Color(0xFF0F172A))
                    }
                    Divider(modifier = Modifier.height(35.dp).width(1.dp), color = Color(0xFFE2E8F0))
                    Column(modifier = Modifier.weight(1.5f)) {
                        Text("TOTAL BALANCE", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color(0xFF64748B))
                        Spacer(modifier = Modifier.height(4.dp))
                        val totalBalance = companyRiders.sumOf { it.currentBalance }
                        Text(
                            text = "₦${String.format("%,.2f", totalBalance)}",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = PrimaryGreen
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Action row: Add Rider, Withdraw
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Button(
                    onClick = { showAddRiderDialog = true },
                    modifier = Modifier.weight(1f).height(44.dp).testTag("add_rider_dashboard_button"),
                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryGreen),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Icon(imageVector = Icons.Default.Add, contentDescription = "Add Rider", tint = Color.White, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Add Rider", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                }

                OutlinedButton(
                    onClick = { showCompanyWithdrawDialog = true },
                    modifier = Modifier.weight(1f).height(44.dp).testTag("company_withdraw_dashboard_button"),
                    border = BorderStroke(1.dp, PrimaryGreen),
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = PrimaryGreen)
                ) {
                    Icon(imageVector = Icons.Default.AccountBalanceWallet, contentDescription = "Withdraw", modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Withdraw", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Registered Riders List Header
            Text(
                text = "Fleet Team & Drivers",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF1E293B)
            )
            Text(
                text = "Manage dispatch rider statuses and team members",
                fontSize = 12.sp,
                color = Color(0xFF64748B)
            )

            Spacer(modifier = Modifier.height(10.dp))

            if (companyRiders.isEmpty()) {
                EmptyStateCard(
                    message = "No Riders Registered Yet",
                    subMessage = "Add dispatch riders above or instruct your riders to select \"Company Rider\" and enter \"$companyName\" as their company name when registering.",
                    icon = Icons.Default.DirectionsBike,
                    iconColor = Color(0xFF1E3A8A)
                )
            } else {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    companyRiders.forEach { rider ->
                        Card(
                            modifier = Modifier.fillMaxWidth().testTag("company_rider_item_${rider.id}"),
                            colors = CardDefaults.cardColors(containerColor = SurfaceBg),
                            shape = RoundedCornerShape(12.dp),
                            border = BorderStroke(1.dp, Color(0xFFE2E8F0))
                        ) {
                            Column(modifier = Modifier.padding(14.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Box(
                                            modifier = Modifier
                                                .size(40.dp)
                                                .background(PrimaryGreen.copy(alpha = 0.1f), CircleShape),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text(text = "🚴", fontSize = 18.sp)
                                        }
                                        Spacer(modifier = Modifier.width(12.dp))
                                        Column {
                                            Text(
                                                text = rider.name,
                                                fontSize = 15.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = Color(0xFF1E293B)
                                            )
                                            Text(
                                                text = "📞 ${rider.phone} • Trips: ${rider.completedTrips}",
                                                fontSize = 12.sp,
                                                color = Color(0xFF64748B)
                                            )
                                        }
                                    }
                                }

                                Spacer(modifier = Modifier.height(12.dp))
                                Divider(color = Color(0xFFF1F5F9))
                                Spacer(modifier = Modifier.height(10.dp))

                                // Rider team status management & remove action row
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    // Team Status Badge (Read-only)
                                    Column {
                                        Text("TEAM STATUS", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = Color(0xFF94A3B8))
                                        Spacer(modifier = Modifier.height(4.dp))
                                        val status = rider.status
                                        Box(
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(6.dp))
                                                .background(
                                                    when (status) {
                                                        "Online" -> Color(0xFFDCFCE7)
                                                        "In Transit" -> Color(0xFFFEF3C7)
                                                        else -> Color(0xFFF1F5F9)
                                                    }
                                                )
                                                .border(
                                                    width = 1.dp,
                                                    color = when (status) {
                                                        "Online" -> Color(0xFF22C55E)
                                                        "In Transit" -> Color(0xFFF59E0B)
                                                        else -> Color(0xFF94A3B8)
                                                    },
                                                    shape = RoundedCornerShape(6.dp)
                                                )
                                                .padding(horizontal = 8.dp, vertical = 4.dp)
                                        ) {
                                            Text(
                                                text = status,
                                                fontSize = 10.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = when (status) {
                                                    "Online" -> Color(0xFF15803D)
                                                    "In Transit" -> Color(0xFFB45309)
                                                    else -> Color(0xFF475569)
                                                }
                                            )
                                        }
                                    }

                                    // Remove Button
                                    IconButton(
                                        onClick = { showRemoveRiderDialog = rider },
                                        modifier = Modifier
                                            .size(28.dp)
                                            .background(Color(0xFFFEF2F2), RoundedCornerShape(6.dp))
                                            .testTag("remove_rider_btn_${rider.id}")
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Delete,
                                            contentDescription = "Remove Rider",
                                            tint = Color(0xFFEF4444),
                                            modifier = Modifier.size(14.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // Add Rider Dialog
    if (showAddRiderDialog) {
        var nameInput by remember { mutableStateOf("") }
        var phoneInput by remember { mutableStateOf("") }
        var addErrorText by remember { mutableStateOf("") }

        AlertDialog(
            onDismissRequest = { showAddRiderDialog = false },
            title = { Text("Add Dispatch Rider 🚴", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color(0xFF0F172A)) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        text = "Register a new rider under your dispatch fleet. They will be able to receive orders and accumulate earnings.",
                        fontSize = 12.sp,
                        color = Color(0xFF64748B)
                    )

                    OutlinedTextField(
                        value = nameInput,
                        onValueChange = { nameInput = it },
                        label = { Text("Rider's Full Name") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth().testTag("add_rider_name_input"),
                        colors = defaultTextFieldColors()
                    )

                    OutlinedTextField(
                        value = phoneInput,
                        onValueChange = { phoneInput = it },
                        label = { Text("Phone Number") },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                        modifier = Modifier.fillMaxWidth().testTag("add_rider_phone_input"),
                        colors = defaultTextFieldColors()
                    )

                    if (addErrorText.isNotEmpty()) {
                        Text(addErrorText, color = Color.Red, fontSize = 11.sp)
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (nameInput.trim().isEmpty()) {
                            addErrorText = "Name cannot be empty."
                        } else if (phoneInput.trim().isEmpty()) {
                            addErrorText = "Phone number cannot be empty."
                        } else {
                            viewModel.addCompanyRider(nameInput.trim(), phoneInput.trim(), 0.0)
                            showAddRiderDialog = false
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryGreen),
                    modifier = Modifier.testTag("add_rider_confirm_button")
                ) {
                    Text("Add Rider")
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddRiderDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    // Remove Rider Dialog
    if (showRemoveRiderDialog != null) {
        val rider = showRemoveRiderDialog!!
        AlertDialog(
            onDismissRequest = { showRemoveRiderDialog = null },
            title = { Text("Remove Rider from Fleet? ⚠️", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color(0xFF0F172A)) },
            text = {
                Text(
                    text = "Are you sure you want to remove ${rider.name} (${rider.phone}) from your dispatch fleet? This rider's status will no longer be visible in your company roster.",
                    fontSize = 12.sp,
                    color = Color(0xFF64748B)
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.removeCompanyRider(rider.id)
                        showRemoveRiderDialog = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEF4444)),
                    modifier = Modifier.testTag("remove_rider_confirm_button")
                ) {
                    Text("Remove")
                }
            },
            dismissButton = {
                TextButton(onClick = { showRemoveRiderDialog = null }) {
                    Text("Cancel")
                }
            }
        )
    }

    // Company Withdraw Dialog
    if (showCompanyWithdrawDialog) {
        var withdrawAmount by remember { mutableStateOf("") }
        var bankName by remember { mutableStateOf("") }
        var accountNumber by remember { mutableStateOf("") }
        var withdrawError by remember { mutableStateOf("") }
        var showConfirmationDialog by remember { mutableStateOf(false) }

        val totalBalance = companyRiders.sumOf { it.currentBalance }

        if (!showConfirmationDialog) {
            AlertDialog(
                onDismissRequest = { showCompanyWithdrawDialog = false },
                title = { Text("Logistics Company Withdrawal 🏦", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color(0xFF0F172A)) },
                text = {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Text(
                            text = "Place a withdrawal request of earnings on behalf of your fleet company. Available Balance: ₦${String.format("%,.2f", totalBalance)}",
                            fontSize = 12.sp,
                            color = Color(0xFF64748B)
                        )

                        OutlinedTextField(
                            value = withdrawAmount,
                            onValueChange = { withdrawAmount = it.filter { c -> c.isDigit() } },
                            label = { Text("Amount (₦)") },
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.fillMaxWidth().testTag("company_withdraw_amount_input"),
                            colors = defaultTextFieldColors()
                        )

                        OutlinedTextField(
                            value = bankName,
                            onValueChange = { bankName = it },
                            label = { Text("Bank Name") },
                            placeholder = { Text("e.g. GTBank, Zenith, Access Bank") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth().testTag("company_withdraw_bank_input"),
                            colors = defaultTextFieldColors()
                        )

                        OutlinedTextField(
                            value = accountNumber,
                            onValueChange = { accountNumber = it.filter { c -> c.isDigit() } },
                            label = { Text("Account Number") },
                            placeholder = { Text("10-Digit Account No.") },
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.fillMaxWidth().testTag("company_withdraw_account_input"),
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
                            } else if (amount > totalBalance) {
                                withdrawError = "Insufficient total balance (Max: ₦${String.format("%,.0f", totalBalance)})"
                            } else if (bankName.trim().isEmpty()) {
                                withdrawError = "Please enter bank name."
                            } else if (accountNumber.length != 10) {
                                withdrawError = "Account number must be exactly 10 digits."
                            } else {
                                showConfirmationDialog = true
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = PrimaryGreen),
                        modifier = Modifier.testTag("company_withdraw_confirm_button")
                    ) {
                        Text("Withdraw")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showCompanyWithdrawDialog = false }) {
                        Text("Cancel")
                    }
                }
            )
        } else {
            // Confirmation Dialog
            AlertDialog(
                onDismissRequest = { showConfirmationDialog = false },
                title = { Text("Confirm Withdrawal Transfer 🔐", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color(0xFF0F172A)) },
                text = {
                    Column {
                        Text(
                            text = "Please verify the fleet transfer details below before executing this withdrawal:",
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
                                    Text("Company:", fontSize = 11.sp, color = Color(0xFF64748B))
                                    Text(companyName.ifEmpty { "Logistics Company" }, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                }
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
                            viewModel.withdrawCompanyBulk(amount, bankName, accountNumber)
                            showConfirmationDialog = false
                            showCompanyWithdrawDialog = false
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = PrimaryGreen),
                        modifier = Modifier.testTag("company_withdraw_execute_button")
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

// --- ONBOARDING TOUR TOOLTIPS ---
@Composable
fun TooltipPointer(isUpward: Boolean, color: Color) {
    Canvas(
        modifier = Modifier
            .size(24.dp, 12.dp)
    ) {
        val path = Path().apply {
            if (isUpward) {
                moveTo(size.width / 2, 0f)
                lineTo(0f, size.height)
                lineTo(size.width, size.height)
            } else {
                moveTo(0f, 0f)
                lineTo(size.width, 0f)
                lineTo(size.width / 2, size.height)
            }
            close()
        }
        drawPath(path = path, color = color)
    }
}

@Composable
fun OnboardingTourOverlay(
    currentStep: Int,
    userName: String,
    onNext: () -> Unit,
    onPrev: () -> Unit,
    onSkip: () -> Unit
) {
    val cardBg = MaterialTheme.colorScheme.surface
    val contentColor = MaterialTheme.colorScheme.onSurface
    
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.7f))
            .clickable(enabled = false) {} // block click throughs
            .padding(16.dp),
        contentAlignment = when (currentStep) {
            1 -> Alignment.BottomCenter // Points up to wallet top area
            2 -> Alignment.TopCenter    // Points down to circles list area
            3 -> Alignment.TopCenter    // Points down to circles create area
            else -> Alignment.Center    // Welcomes & Complete are centered
        }
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .fillMaxWidth(0.92f)
                .padding(vertical = if (currentStep == 1 || currentStep == 2 || currentStep == 3) 24.dp else 0.dp)
        ) {
            // Arrow on TOP for Step 1 (pointing up to balance/topup)
            if (currentStep == 1) {
                TooltipPointer(isUpward = true, color = cardBg)
            }

            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("tour_overlay_step_${currentStep}"),
                shape = RoundedCornerShape(20.dp),
                color = cardBg,
                tonalElevation = 6.dp,
                border = BorderStroke(2.dp, PrimaryGreen)
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Header badge + close
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .background(PrimaryGreen.copy(alpha = 0.12f), RoundedCornerShape(8.dp))
                                .padding(horizontal = 10.dp, vertical = 6.dp)
                        ) {
                            Text(
                                text = "Tour Guide • Step ${currentStep + 1} of 4",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = PrimaryGreen
                            )
                        }
                        IconButton(
                            onClick = onSkip,
                            modifier = Modifier.size(24.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Skip Tour",
                                tint = TextSlate400,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Step Icon Graphic
                    Box(
                        modifier = Modifier
                            .size(64.dp)
                            .background(
                                color = when (currentStep) {
                                    1 -> Color(0xFF3B82F6).copy(alpha = 0.12f)
                                    2 -> SecondaryOrange.copy(alpha = 0.12f)
                                    3 -> AccentGold.copy(alpha = 0.12f)
                                    else -> PrimaryGreen.copy(alpha = 0.12f)
                                },
                                shape = CircleShape
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = when (currentStep) {
                                0 -> Icons.Default.Explore
                                1 -> Icons.Default.AccountBalanceWallet
                                2 -> Icons.Default.Groups
                                else -> Icons.Default.CheckCircle
                            },
                            contentDescription = null,
                            tint = when (currentStep) {
                                1 -> Color(0xFF3B82F6)
                                2 -> SecondaryOrange
                                3 -> SecondaryOrange
                                else -> PrimaryGreen
                            },
                            modifier = Modifier.size(32.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Title
                    Text(
                        text = when (currentStep) {
                            0 -> "Welcome to AfriSav! 🌾"
                            1 -> "Instantly Fund Your Wallet 💳"
                            2 -> "Save & Bulk Buy in Circles 👥"
                            else -> "You are All Set! 🎉"
                        },
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = contentColor,
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    // Description
                    Text(
                        text = when (currentStep) {
                            0 -> "Hello $userName! AfriSav helps you beat high food prices and secure quality foodstuffs through joint savings and neighborhood bulk buying circles."
                            1 -> "Need to add money? Open this Wallet tab and use our secure top-up flow. You can pay securely via Card or Bank Transfer, and your balance is updated in real-time instantly!"
                            2 -> "Form Food Circles with family, friends, or neighbors. Combine savings together to buy food items in bulk directly from wholesale markets. Save up to 40% on groceries!"
                            else -> "Tap the '+' (Create Circle) button anytime on the Circles tab to launch your buying group. Start inviting neighbors, and start saving together!"
                        },
                        fontSize = 13.sp,
                        lineHeight = 19.sp,
                        color = contentColor.copy(alpha = 0.8f),
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(horizontal = 4.dp)
                    )

                    Spacer(modifier = Modifier.height(20.dp))

                    // Indicator Dots
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        modifier = Modifier.padding(bottom = 12.dp)
                    ) {
                        repeat(4) { idx ->
                            Box(
                                modifier = Modifier
                                    .size(if (idx == currentStep) 16.dp else 6.dp, 6.dp)
                                    .clip(CircleShape)
                                    .background(if (idx == currentStep) PrimaryGreen else Color(0xFFCBD5E1))
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Navigation Actions row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        if (currentStep > 0) {
                            OutlinedButton(
                                onClick = onPrev,
                                modifier = Modifier
                                    .weight(1.2f)
                                    .height(42.dp)
                                    .testTag("tour_back_btn"),
                                shape = RoundedCornerShape(10.dp),
                                border = BorderStroke(1.dp, Color(0xFFCBD5E1))
                            ) {
                                Text(
                                    text = "Back",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = contentColor.copy(alpha = 0.7f)
                                )
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                        } else {
                            TextButton(
                                onClick = onSkip,
                                modifier = Modifier
                                    .weight(1f)
                                    .height(42.dp)
                                    .testTag("tour_skip_btn")
                            ) {
                                Text(
                                    text = "Skip Tour",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = TextSlate400
                                )
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                        }

                        Button(
                            onClick = onNext,
                            modifier = Modifier
                                .weight(1.5f)
                                .height(42.dp)
                                .testTag("tour_next_btn"),
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = PrimaryGreen)
                        ) {
                            Text(
                                text = if (currentStep == 3) "Finish" else "Next Step",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }
                    }
                }
            }

            // Arrow on BOTTOM for Step 2 & 3 (pointing down to bottom area actions)
            if (currentStep == 2 || currentStep == 3) {
                TooltipPointer(isUpward = false, color = cardBg)
            }
        }
    }
}

// --- CATEGORY FILTER BAR FOR QUICK BROWSE ---
data class CategoryMetadata(
    val id: String,
    val displayName: String,
    val emoji: String,
    val color: Color
)

@Composable
fun CategoryFilterBar(
    selectedCategory: String,
    onCategorySelected: (String) -> Unit,
    items: List<MarketItem>,
    modifier: Modifier = Modifier
) {
    val categories = listOf(
        CategoryMetadata("All", "All Food", "🍲", Color(0xFF64748B)),
        CategoryMetadata("Grains", "Grains", "🌾", Color(0xFFEAB308)),
        CategoryMetadata("Proteins", "Proteins", "🍗", Color(0xFFEF4444)),
        CategoryMetadata("Vegetables", "Vegetables", "🍅", Color(0xFF22C55E)),
        CategoryMetadata("Tubers", "Tubers", "🥔", Color(0xFF8B5CF6))
    )

    LazyRow(
        modifier = modifier
            .fillMaxWidth()
            .testTag("category_filter_bar"),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
    ) {
        items(categories) { cat ->
            val isSelected = selectedCategory.equals(cat.id, ignoreCase = true)
            
            val count = when (cat.id) {
                "All" -> items.size
                "Grains" -> items.count { it.category.equals("Rice", true) || it.category.equals("Beans", true) || it.category.equals("Garri", true) || it.category.equals("Grains", true) }
                "Proteins" -> items.count { it.category.equals("Fish", true) || it.category.equals("Meat", true) || it.category.equals("Poultry", true) || it.category.equals("Proteins", true) }
                "Vegetables" -> items.count { it.category.equals("Tomatoes", true) || it.category.equals("Pepper", true) || it.category.equals("Onion", true) || it.category.equals("Vegetables", true) }
                "Tubers" -> items.count { it.category.equals("Yam", true) || it.category.equals("Plantain", true) || it.category.equals("Tubers", true) }
                else -> 0
            }

            Surface(
                modifier = Modifier
                    .testTag("category_chip_${cat.id.lowercase()}")
                    .clickable { onCategorySelected(cat.id) },
                shape = RoundedCornerShape(14.dp),
                color = if (isSelected) PrimaryGreen.copy(alpha = 0.12f) else Color.White,
                border = BorderStroke(
                    width = if (isSelected) 2.dp else 1.dp,
                    color = if (isSelected) PrimaryGreen else Color(0xFFE2E8F0)
                ),
                tonalElevation = if (isSelected) 2.dp else 0.dp
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(28.dp)
                            .background(
                                color = if (isSelected) PrimaryGreen.copy(alpha = 0.15f) else cat.color.copy(alpha = 0.08f),
                                shape = CircleShape
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(text = cat.emoji, fontSize = 14.sp)
                    }

                    Text(
                        text = cat.displayName,
                        fontSize = 13.sp,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.SemiBold,
                        color = if (isSelected) PrimaryGreen else Color(0xFF1E293B)
                    )

                    Box(
                        modifier = Modifier
                            .background(
                                color = if (isSelected) PrimaryGreen.copy(alpha = 0.2f) else Color(0xFFF1F5F9),
                                shape = RoundedCornerShape(8.dp)
                            )
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = count.toString(),
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isSelected) PrimaryGreen else Color(0xFF64748B)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun BuyerProfileScreen(
    viewModel: KoboViewModel,
    onBackClick: () -> Unit,
    onContactRider: (EscrowOrder, String) -> Unit,
    onLogoutClick: () -> Unit
) {
    var currentSection by remember { mutableStateOf("Main") }

    when (currentSection) {
        "Orders" -> {
            OrdersScreen(
                viewModel = viewModel,
                onBackClick = { currentSection = "Main" },
                onContactRider = onContactRider
            )
        }
        "Reviews" -> {
            BuyerReviewsScreen(
                viewModel = viewModel,
                onBackClick = { currentSection = "Main" }
            )
        }
        "DesignSystem" -> {
            DesignSystemShowcaseScreen(
                onBackClick = { currentSection = "Main" }
            )
        }
        else -> {
            BuyerProfileMainContent(
                viewModel = viewModel,
                onBackClick = onBackClick,
                onViewOrdersClick = { currentSection = "Orders" },
                onViewReviewsClick = { currentSection = "Reviews" },
                onViewDesignSystemClick = { currentSection = "DesignSystem" },
                onLogoutClick = onLogoutClick
            )
        }
    }
}

@Composable
fun BuyerReviewsScreen(
    viewModel: KoboViewModel,
    onBackClick: () -> Unit
) {
    val allReviews by viewModel.allReviews.collectAsStateWithLifecycle()
    val currentUserName by viewModel.currentUserName.collectAsStateWithLifecycle()
    
    val buyerReviews = remember(allReviews, currentUserName) {
        allReviews.filter { !it.isTargetSeller && it.targetName.equals(currentUserName, ignoreCase = true) }
    }
    
    val simulatedBuyerReviews = remember {
        listOf(
            ReviewRating(
                id = -1,
                reviewerName = "Mile 12 Bulk Hub",
                targetName = currentUserName,
                isTargetSeller = false,
                rating = 5,
                reviewText = "Excellent buyer! Swift pickup PIN verification and very friendly communication. Highly recommended member of the cooperative.",
                timestamp = System.currentTimeMillis() - 86400000L * 2
            ),
            ReviewRating(
                id = -2,
                reviewerName = "Mama Ngozi Vegetables",
                targetName = currentUserName,
                isTargetSeller = false,
                rating = 5,
                reviewText = "Reliable cooperative partner. Paid promptly through the escrow wallet and verified delivery instantly. A pleasure doing business with you!",
                timestamp = System.currentTimeMillis() - 86400000L * 5
            ),
            ReviewRating(
                id = -3,
                reviewerName = "Ketu Wholesalers",
                targetName = currentUserName,
                isTargetSeller = false,
                rating = 4,
                reviewText = "Smooth transaction. Everything went exactly as planned under the bulk purchase contract.",
                timestamp = System.currentTimeMillis() - 86400000L * 12
            )
        )
    }
    
    val combinedReviews = remember(buyerReviews) { buyerReviews + simulatedBuyerReviews }
    val averageRating = remember(combinedReviews) { if (combinedReviews.isEmpty()) 4.8 else combinedReviews.map { it.rating }.average() }
    
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
            .background(SoftBackground)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(SurfaceBg)
                .statusBarsPadding()
                .padding(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 14.dp)
                .border(width = 1.dp, color = BorderSlate100),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = onBackClick,
                modifier = Modifier.testTag("reviews_back_button")
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back",
                    tint = TextSlate800
                )
            }
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "Seller Reviews & Ratings",
                fontSize = 18.sp,
                fontWeight = FontWeight.ExtraBold,
                color = TextSlate800
            )
        }

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                Text(
                    text = "⭐ Your Cooperative Reputation",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextSlate800
                )
                Text(
                    text = "Ratings and feedback left for you by sellers after completed bulk transactions.",
                    fontSize = 12.sp,
                    color = TextSlate500
                )
                Spacer(modifier = Modifier.height(12.dp))
            }

            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = SurfaceBg),
                    border = BorderStroke(1.dp, BorderSlate100),
                    shape = RoundedCornerShape(16.dp)
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
                                text = String.format("%.1f", averageRating),
                                fontSize = 36.sp,
                                fontWeight = FontWeight.Black,
                                color = TextSlate800
                            )
                            Row(horizontalArrangement = Arrangement.spacedBy(2.dp)) {
                                (1..5).forEach { star ->
                                    Icon(
                                        imageVector = Icons.Default.Star,
                                        contentDescription = null,
                                        tint = if (star <= averageRating.toInt()) SecondaryOrange else Color(0xFFE2E8F0),
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = "${combinedReviews.size} Seller Reviews",
                                fontSize = 11.sp,
                                color = TextSlate500,
                                fontWeight = FontWeight.SemiBold
                            )
                        }

                        Box(
                            modifier = Modifier
                                .height(80.dp)
                                .width(1.dp)
                                .background(BorderSlate100)
                        )

                        Column(
                            modifier = Modifier
                                .weight(1.5f)
                                .padding(start = 16.dp),
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            ratingDistribution.forEachIndexed { index, pct ->
                                val starVal = 5 - index
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = "$starVal ★",
                                        fontSize = 10.sp,
                                        color = TextSlate500,
                                        modifier = Modifier.width(24.dp)
                                    )
                                    LinearProgressIndicator(
                                        progress = pct.toFloat(),
                                        modifier = Modifier
                                            .weight(1f)
                                            .height(6.dp)
                                            .clip(RoundedCornerShape(3.dp)),
                                        color = SecondaryOrange,
                                        trackColor = Color(0xFFF1F5F9)
                                    )
                                }
                            }
                        }
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Recent Feedback Feed",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextSlate800
                )
            }

            if (combinedReviews.isEmpty()) {
                item {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 12.dp),
                        colors = CardDefaults.cardColors(containerColor = SurfaceBg),
                        border = BorderStroke(1.dp, BorderSlate100),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(text = "✍️", fontSize = 28.sp)
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "No Seller Reviews Yet",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextSlate800
                            )
                            Text(
                                text = "Your ratings will appear here after sellers complete bulk deliveries.",
                                fontSize = 12.sp,
                                color = TextSlate500,
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center
                            )
                        }
                    }
                }
            } else {
                items(combinedReviews) { rev ->
                    val formattedDate = remember(rev.timestamp) {
                        try {
                            java.text.SimpleDateFormat("MMM dd, yyyy", java.util.Locale.getDefault()).format(java.util.Date(rev.timestamp))
                        } catch (e: Exception) {
                            "Recent"
                        }
                    }
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = SurfaceBg),
                        border = BorderStroke(1.dp, BorderSlate100),
                        shape = RoundedCornerShape(14.dp)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.Top
                            ) {
                                Column {
                                    Text(
                                        text = rev.reviewerName,
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = TextSlate800
                                    )
                                    Row(horizontalArrangement = Arrangement.spacedBy(1.dp)) {
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
                                    text = formattedDate,
                                    fontSize = 11.sp,
                                    color = TextSlate400,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                            if (rev.reviewText.isNotEmpty()) {
                                Spacer(modifier = Modifier.height(10.dp))
                                Text(
                                    text = rev.reviewText,
                                    fontSize = 13.sp,
                                    color = TextSlate500,
                                    lineHeight = 18.sp
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun BuyerProfileMainContent(
    viewModel: KoboViewModel,
    onBackClick: () -> Unit,
    onViewOrdersClick: () -> Unit,
    onViewReviewsClick: () -> Unit,
    onViewDesignSystemClick: () -> Unit,
    onLogoutClick: () -> Unit
) {
    val context = androidx.compose.ui.platform.LocalContext.current
    val currentName by viewModel.currentUserName.collectAsStateWithLifecycle()
    val currentPhone by viewModel.currentUserPhone.collectAsStateWithLifecycle()
    val currentEmail by viewModel.currentUserEmail.collectAsStateWithLifecycle()
    val currentLocation by viewModel.currentUserLocation.collectAsStateWithLifecycle()
    val currentBio by viewModel.currentUserBio.collectAsStateWithLifecycle()
    val currentUserPhoto by viewModel.currentUserPhoto.collectAsStateWithLifecycle()
    
    var name by remember(currentName) { mutableStateOf(currentName) }
    var phone by remember(currentPhone) { mutableStateOf(currentPhone) }
    var email by remember(currentEmail) { mutableStateOf(currentEmail) }
    var location by remember(currentLocation) { mutableStateOf(currentLocation) }
    var bio by remember(currentBio) { mutableStateOf(currentBio) }
    
    var newPin by remember { mutableStateOf("") }
    
    val sharedPrefs = remember { context.getSharedPreferences("buyer_notification_prefs", android.content.Context.MODE_PRIVATE) }
    var orderUpdatesEnabled by remember { mutableStateOf(sharedPrefs.getBoolean("order_updates", true)) }
    var savingsRemindersEnabled by remember { mutableStateOf(sharedPrefs.getBoolean("savings_reminders", true)) }
    var promotionalAlertsEnabled by remember { mutableStateOf(sharedPrefs.getBoolean("promotional_alerts", false)) }
    
    var showCameraDialog by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(SoftBackground)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(SurfaceBg)
                .statusBarsPadding()
                .padding(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 14.dp)
                .border(width = 1.dp, color = BorderSlate100),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = onBackClick,
                modifier = Modifier.testTag("profile_back_button")
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back",
                    tint = TextSlate800
                )
            }
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "Profile & Settings",
                fontSize = 18.sp,
                fontWeight = FontWeight.ExtraBold,
                color = TextSlate800
            )
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = SurfaceBg),
                border = BorderStroke(1.dp, BorderSlate100),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box(
                        modifier = Modifier
                            .size(96.dp)
                            .clip(CircleShape)
                            .border(2.dp, PrimaryGreen, CircleShape)
                            .clickable { showCameraDialog = true }
                    ) {
                        ProfileAvatar(
                            imageUrl = currentUserPhoto,
                            role = "buyer",
                            modifier = Modifier.fillMaxSize()
                        )
                        Box(
                            modifier = Modifier
                                .size(28.dp)
                                .background(PrimaryGreen, CircleShape)
                                .align(Alignment.BottomEnd)
                                .border(1.5.dp, Color.White, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.CameraAlt,
                                contentDescription = "Edit Photo",
                                tint = Color.White,
                                modifier = Modifier.size(14.dp)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = currentName,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = TextSlate800
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Box(
                        modifier = Modifier
                            .background(PrimaryGreen.copy(alpha = 0.1f), RoundedCornerShape(12.dp))
                            .padding(horizontal = 12.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = "Cooperative Buyer Hub",
                            fontSize = 11.sp,
                            color = PrimaryGreen,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            Text(
                text = "My Activity Tracking",
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = TextSlate800,
                modifier = Modifier.padding(horizontal = 4.dp)
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Card(
                    modifier = Modifier
                        .weight(1f)
                        .testTag("profile_view_orders_card")
                        .clickable { onViewOrdersClick() },
                    colors = CardDefaults.cardColors(containerColor = SurfaceBg),
                    border = BorderStroke(1.dp, BorderSlate100),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .background(PrimaryGreen.copy(alpha = 0.1f), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.LocalShipping,
                                contentDescription = null,
                                tint = PrimaryGreen,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "View Orders",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextSlate800
                        )
                        Text(
                            text = "Live delivery status",
                            fontSize = 11.sp,
                            color = TextSlate500
                        )
                    }
                }

                Card(
                    modifier = Modifier
                        .weight(1f)
                        .testTag("profile_view_reviews_card")
                        .clickable { onViewReviewsClick() },
                    colors = CardDefaults.cardColors(containerColor = SurfaceBg),
                    border = BorderStroke(1.dp, BorderSlate100),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .background(SecondaryOrange.copy(alpha = 0.1f), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Star,
                                contentDescription = null,
                                tint = SecondaryOrange,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "Seller Reviews",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextSlate800
                        )
                        Text(
                            text = "Your reputation score",
                            fontSize = 11.sp,
                            color = TextSlate500
                        )
                    }
                }
            }

            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = SurfaceBg),
                border = BorderStroke(1.dp, BorderSlate100),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Text(
                        text = "✏️ Edit Profile Details",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextSlate800
                    )

                    OutlinedTextField(
                        value = name,
                        onValueChange = { name = it },
                        label = { Text("Full Name") },
                        leadingIcon = { Icon(Icons.Default.Person, contentDescription = null, tint = PrimaryGreen) },
                        modifier = Modifier.fillMaxWidth().testTag("profile_name_input"),
                        shape = RoundedCornerShape(10.dp)
                    )

                    OutlinedTextField(
                        value = phone,
                        onValueChange = { phone = it },
                        label = { Text("Phone Number") },
                        leadingIcon = { Icon(Icons.Default.Phone, contentDescription = null, tint = PrimaryGreen) },
                        modifier = Modifier.fillMaxWidth().testTag("profile_phone_input"),
                        shape = RoundedCornerShape(10.dp),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone)
                    )

                    OutlinedTextField(
                        value = email,
                        onValueChange = { email = it },
                        label = { Text("Email Address") },
                        leadingIcon = { Icon(Icons.Default.Email, contentDescription = null, tint = PrimaryGreen) },
                        modifier = Modifier.fillMaxWidth().testTag("profile_email_input"),
                        shape = RoundedCornerShape(10.dp),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email)
                    )

                    OutlinedTextField(
                        value = location,
                        onValueChange = { location = it },
                        label = { Text("Cooperative Location") },
                        leadingIcon = { Icon(Icons.Default.LocationOn, contentDescription = null, tint = PrimaryGreen) },
                        modifier = Modifier.fillMaxWidth().testTag("profile_location_input"),
                        shape = RoundedCornerShape(10.dp)
                    )

                    OutlinedTextField(
                        value = bio,
                        onValueChange = { bio = it },
                        label = { Text("Bio / Slogan") },
                        leadingIcon = { Icon(Icons.Default.Info, contentDescription = null, tint = PrimaryGreen) },
                        modifier = Modifier.fillMaxWidth().height(90.dp).testTag("profile_bio_input"),
                        shape = RoundedCornerShape(10.dp),
                        maxLines = 3
                    )

                    Button(
                        onClick = {
                            if (name.isNotBlank() && phone.isNotBlank()) {
                                viewModel.updateUserProfile(name, phone, email, location, bio)
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = PrimaryGreen),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                            .testTag("save_profile_button"),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text("Save Profile Changes", fontWeight = FontWeight.Bold, color = Color.White)
                    }
                }
            }

            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = SurfaceBg),
                border = BorderStroke(1.dp, BorderSlate100),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Text(
                        text = "🔐 Security & Authentication",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextSlate800
                    )
                    Text(
                        text = "Update your 4-digit quick-entry authentication PIN used to open this app secure sessions.",
                        fontSize = 11.sp,
                        color = TextSlate500
                    )

                    OutlinedTextField(
                        value = newPin,
                        onValueChange = { input ->
                            if (input.length <= 4 && input.all { it.isDigit() }) {
                                newPin = input
                            }
                        },
                        label = { Text("New 4-Digit Security PIN") },
                        leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null, tint = PrimaryGreen) },
                        modifier = Modifier.fillMaxWidth().testTag("profile_pin_input"),
                        shape = RoundedCornerShape(10.dp),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword)
                    )

                    Button(
                        onClick = {
                            if (newPin.length == 4) {
                                viewModel.updateBuyerPin(newPin)
                                newPin = ""
                            }
                        },
                        enabled = newPin.length == 4,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = PrimaryGreen,
                            disabledContainerColor = PrimaryGreen.copy(alpha = 0.4f)
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                            .testTag("save_pin_button"),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text("Update Security PIN", fontWeight = FontWeight.Bold, color = Color.White)
                    }
                }
            }

            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = SurfaceBg),
                border = BorderStroke(1.dp, BorderSlate100),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "🔔 Notification Preferences",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextSlate800
                    )
                    Text(
                        text = "Configure your alert settings for order tracking, pricing changes, and cooperative cycles.",
                        fontSize = 11.sp,
                        color = TextSlate500
                    )

                    HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp), color = BorderSlate100)

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Order & Delivery Tracking",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextSlate800
                            )
                            Text(
                                text = "Live dispatch updates for food orders",
                                fontSize = 11.sp,
                                color = TextSlate500
                            )
                        }
                        Switch(
                            checked = orderUpdatesEnabled,
                            onCheckedChange = { isChecked ->
                                orderUpdatesEnabled = isChecked
                                sharedPrefs.edit().putBoolean("order_updates", isChecked).apply()
                            },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = Color.White,
                                checkedTrackColor = PrimaryGreen,
                                uncheckedThumbColor = Color.White,
                                uncheckedTrackColor = Color(0xFFCBD5E1)
                            ),
                            modifier = Modifier.testTag("switch_order_updates")
                        )
                    }

                    HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp), color = BorderSlate100)

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Cooperative Savings Goals",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextSlate800
                            )
                            Text(
                                text = "Reminders about cycle contributions",
                                fontSize = 11.sp,
                                color = TextSlate500
                            )
                        }
                        Switch(
                            checked = savingsRemindersEnabled,
                            onCheckedChange = { isChecked ->
                                savingsRemindersEnabled = isChecked
                                sharedPrefs.edit().putBoolean("savings_reminders", isChecked).apply()
                            },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = Color.White,
                                checkedTrackColor = PrimaryGreen,
                                uncheckedThumbColor = Color.White,
                                uncheckedTrackColor = Color(0xFFCBD5E1)
                            ),
                            modifier = Modifier.testTag("switch_savings_reminders")
                        )
                    }

                    HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp), color = BorderSlate100)

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Market Price Drops",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextSlate800
                            )
                            Text(
                                text = "Alerts on saved list price reductions",
                                fontSize = 11.sp,
                                color = TextSlate500
                            )
                        }
                        Switch(
                            checked = promotionalAlertsEnabled,
                            onCheckedChange = { isChecked ->
                                promotionalAlertsEnabled = isChecked
                                sharedPrefs.edit().putBoolean("promotional_alerts", isChecked).apply()
                            },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = Color.White,
                                checkedTrackColor = PrimaryGreen,
                                uncheckedThumbColor = Color.White,
                                uncheckedTrackColor = Color(0xFFCBD5E1)
                            ),
                            modifier = Modifier.testTag("switch_price_alerts")
                        )
                    }
                }
            }

            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = SurfaceBg),
                border = BorderStroke(1.dp, BorderSlate100),
                shape = RoundedCornerShape(16.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onViewDesignSystemClick() }
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(PrimaryGreen.copy(alpha = 0.12f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Palette,
                            contentDescription = null,
                            tint = PrimaryGreen,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "Design System Showcase",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextSlate800
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Box(
                                modifier = Modifier
                                    .clip(CircleShape)
                                    .background(SecondaryOrange.copy(alpha = 0.15f))
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    text = "Tokens & UI",
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = SecondaryOrange
                                )
                            }
                        }
                        Text(
                            text = "Preview buttons, cards, inputs, tokens in Light/Dark mode",
                            fontSize = 11.sp,
                            color = TextSlate500
                        )
                    }
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                        contentDescription = null,
                        tint = TextSlate400,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = SurfaceBg),
                border = BorderStroke(1.dp, Color(0xFFFEE2E2)),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Button(
                        onClick = onLogoutClick,
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEF4444)),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                            .testTag("logout_button"),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Icon(imageVector = Icons.Default.ExitToApp, contentDescription = null, tint = Color.White)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Log Out of Cooperative Hub", fontWeight = FontWeight.Bold, color = Color.White)
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
        }

        if (showCameraDialog) {
            CameraSimulationDialog(
                onDismiss = { showCameraDialog = false },
                onPhotoCaptured = { capturedPhoto ->
                    viewModel.updateProfilePhoto(capturedPhoto)
                    showCameraDialog = false
                }
            )
        }
    }
}







