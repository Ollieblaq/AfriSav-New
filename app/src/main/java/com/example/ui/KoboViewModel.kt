package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.BuildConfig
import com.example.data.*
import com.example.network.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlin.random.Random

data class CartItem(
    val item: MarketItem,
    val quantity: Int,
    val selectedPortionName: String = "Full Unit",
    val selectedPortionFraction: Double = 1.0
)

data class PriceAlert(
    val id: Int,
    val category: String,
    val targetPrice: Double,
    val originalPrice: Double,
    val isActive: Boolean = true
)

data class AppNotification(
    val id: Int,
    val title: String,
    val message: String,
    val timestamp: String,
    val isRead: Boolean = false,
    val type: String // "CIRCLE", "PRICE_DROP", "SYSTEM"
)

data class CompanyRiderInfo(
    val id: Int,
    val name: String,
    val phone: String,
    val currentBalance: Double,
    val completedTrips: Int,
    val status: String
)

data class RiderWithdrawal(
    val id: Int,
    val riderName: String,
    val amount: Double,
    val bankName: String,
    val accountNumber: String,
    val timestamp: Long = System.currentTimeMillis(),
    val requestedBy: String
)

class KoboViewModel(application: Application) : AndroidViewModel(application) {
    private val database = AfriSavDatabase.getDatabase(application)
    private val repository = KoboRepository(database.koboDao())

    // UI state flows from Room
    val walletState: StateFlow<WalletState?> = repository.walletState
        .map { state ->
            state?.copy(
                availableBalance = maxOf(0.0, state.availableBalance),
                savingsBalance = maxOf(0.0, state.savingsBalance),
                totalSaved = maxOf(0.0, state.totalSaved)
            )
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val savingsGoals: StateFlow<List<SavingsGoal>> = repository.savingsGoals
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val marketItems: StateFlow<List<MarketItem>> = repository.marketItems
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val foodCircles: StateFlow<List<FoodCircle>> = repository.foodCircles
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val transactions: StateFlow<List<WalletTransaction>> = repository.transactions
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val chatMessages: StateFlow<List<ChatMessage>> = repository.chatMessages
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())



    // Screen States and Filter States
    // Role management & User states
    private val _userRole = MutableStateFlow("Buyer") // "Buyer" or "Seller"
    val userRole: StateFlow<String> = _userRole.asStateFlow()

    private val _currentUserPhone = MutableStateFlow("08031234567")
    val currentUserPhone: StateFlow<String> = _currentUserPhone.asStateFlow()

    private val _currentUserName = MutableStateFlow("Adebayo Alao")
    val currentUserName: StateFlow<String> = _currentUserName.asStateFlow()

    private val _currentUserEmail = MutableStateFlow("adebayo.alao@kobomail.com")
    val currentUserEmail: StateFlow<String> = _currentUserEmail.asStateFlow()

    private val _currentUserLocation = MutableStateFlow("Lagos State, Alimosho LGA")
    val currentUserLocation: StateFlow<String> = _currentUserLocation.asStateFlow()

    private val _currentUserBio = MutableStateFlow("Passionate food enthusiast and community leader.")
    val currentUserBio: StateFlow<String> = _currentUserBio.asStateFlow()

    private val _currentUserPhoto = MutableStateFlow<String?>(null)
    val currentUserPhoto: StateFlow<String?> = _currentUserPhoto.asStateFlow()

    private val _sellerPin = MutableStateFlow("8443")
    val sellerPin: StateFlow<String> = _sellerPin.asStateFlow()

    fun updateSellerPin(newPin: String) {
        _sellerPin.value = newPin
    }

    private val _appNotifications = MutableStateFlow<List<AppNotification>>(listOf(
        AppNotification(
            id = 1,
            title = "Welcome to KoboBasket!",
            message = "Connect with bulk buying circles to lock in wholesale food prices, and set smart alerts for your saved foods.",
            timestamp = "Just now",
            isRead = false,
            type = "SYSTEM"
        ),
        AppNotification(
            id = 2,
            title = "Bulk Rice Saving Active",
            message = "Join the Mile 12 Bulk Rice Circle to save up to 40% on bulk food purchases with other families in your neighborhood.",
            timestamp = "3m ago",
            isRead = false,
            type = "CIRCLE"
        )
    ))
    val appNotifications: StateFlow<List<AppNotification>> = _appNotifications.asStateFlow()

    fun addNotification(title: String, message: String, type: String) {
        val current = _appNotifications.value.toMutableList()
        val nextId = (current.maxOfOrNull { it.id } ?: 0) + 1
        current.add(0, AppNotification(
            id = nextId,
            title = title,
            message = message,
            timestamp = "Just now",
            isRead = false,
            type = type
        ))
        _appNotifications.value = current
    }

    fun markNotificationAsRead(id: Int) {
        _appNotifications.value = _appNotifications.value.map {
            if (it.id == id) it.copy(isRead = true) else it
        }
    }

    fun clearAllNotifications() {
        _appNotifications.value = emptyList()
    }

    private val _riderType = MutableStateFlow("Individual") // "Individual" or "Company"
    val riderType: StateFlow<String> = _riderType.asStateFlow()

    private val _dispatchCompanyName = MutableStateFlow("")
    val dispatchCompanyName: StateFlow<String> = _dispatchCompanyName.asStateFlow()

    private val _riderBalance = MutableStateFlow(16800.0)
    val riderBalance: StateFlow<Double> = _riderBalance.asStateFlow()

    private val _riderWithdrawals = MutableStateFlow<List<RiderWithdrawal>>(
        listOf(
            RiderWithdrawal(1, "Emeka Okafor", 5000.0, "Kuda Bank", "9988776655", System.currentTimeMillis() - 86400000, "Rider"),
            RiderWithdrawal(2, "Babajide Sanwo", 12000.0, "GTBank", "0123456789", System.currentTimeMillis() - 172800000, "Company")
        )
    )
    val riderWithdrawals: StateFlow<List<RiderWithdrawal>> = _riderWithdrawals.asStateFlow()

    private val _companyRiders = MutableStateFlow<List<CompanyRiderInfo>>(
        listOf(
            CompanyRiderInfo(1, "Emeka Okafor", "08098765432", 12400.0, 15, "Online"),
            CompanyRiderInfo(2, "Babajide Sanwo", "08123456789", 25600.0, 32, "In Transit"),
            CompanyRiderInfo(3, "Chinedu Okeke", "07011223344", 8900.0, 9, "Offline")
        )
    )
    val companyRiders: StateFlow<List<CompanyRiderInfo>> = _companyRiders.asStateFlow()

    @OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
    val sellerEarningState: StateFlow<SellerEarningState?> = _currentUserName
        .flatMapLatest { name -> repository.getSellerEarningState(name) }
        .map { state ->
            state?.copy(
                availableBalance = maxOf(0.0, state.availableBalance),
                totalRevenue = maxOf(0.0, state.totalRevenue)
            )
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    @OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
    val sellerSales: StateFlow<List<SellerSale>> = _currentUserName
        .flatMapLatest { name -> repository.getSellerSales(name) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    @OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
    val sellerWithdrawals: StateFlow<List<SellerWithdrawal>> = _currentUserName
        .flatMapLatest { name -> repository.getSellerWithdrawals(name) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allReviews: StateFlow<List<ReviewRating>> = repository.getAllReviews()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allEscrowOrders: StateFlow<List<EscrowOrder>> = repository.getAllEscrowOrders()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    @OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
    val escrowOrdersForBuyer: StateFlow<List<EscrowOrder>> = _currentUserName
        .flatMapLatest { name -> repository.getEscrowOrdersForBuyer(name) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    @OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
    val escrowOrdersForSeller: StateFlow<List<EscrowOrder>> = _currentUserName
        .flatMapLatest { name -> repository.getEscrowOrdersForSeller(name) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _selectedMarketCategory = MutableStateFlow("All")
    val selectedMarketCategory: StateFlow<String> = _selectedMarketCategory.asStateFlow()

    private val _selectedMarketState = MutableStateFlow("All")
    val selectedMarketState: StateFlow<String> = _selectedMarketState.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _sortBy = MutableStateFlow("Cheapest") // Cheapest, Nearest, Highest Rated, Fastest Delivery
    val sortBy: StateFlow<String> = _sortBy.asStateFlow()

    private val _isChatLoading = MutableStateFlow(false)
    val isChatLoading: StateFlow<Boolean> = _isChatLoading.asStateFlow()

    private val prefs = application.getSharedPreferences("afrisav_prefs", android.content.Context.MODE_PRIVATE)
    private val _isDarkMode = MutableStateFlow(prefs.getBoolean("dark_mode", false))
    val isDarkMode: StateFlow<Boolean> = _isDarkMode.asStateFlow()

    private val _trackedItemIds = MutableStateFlow<List<Int>>(
        prefs.getString("tracked_item_ids", "")?.split(",")?.filter { it.isNotEmpty() }?.mapNotNull { it.toIntOrNull() } ?: emptyList()
    )
    val trackedItemIds: StateFlow<List<Int>> = _trackedItemIds.asStateFlow()

    private val _trackedItemInitialPrices = MutableStateFlow<Map<Int, Double>>(
        prefs.getString("tracked_item_prices", "")?.split(",")?.filter { it.isNotEmpty() }?.mapNotNull {
            val parts = it.split(":")
            if (parts.size == 2) {
                val id = parts[0].toIntOrNull()
                val price = parts[1].toDoubleOrNull()
                if (id != null && price != null) id to price else null
            } else null
        }?.toMap() ?: emptyMap()
    )
    val trackedItemInitialPrices: StateFlow<Map<Int, Double>> = _trackedItemInitialPrices.asStateFlow()

    fun toggleTrackPrice(item: MarketItem) {
        val currentList = _trackedItemIds.value.toMutableList()
        val currentPrices = _trackedItemInitialPrices.value.toMutableMap()
        val id = item.id
        if (currentList.contains(id)) {
            currentList.remove(id)
            currentPrices.remove(id)
            viewModelScope.launch {
                _uiEvent.emit("Stopped tracking price for ${item.name}")
            }
        } else {
            currentList.add(id)
            currentPrices[id] = item.price
            viewModelScope.launch {
                _uiEvent.emit("Tracking price for ${item.name} at starting threshold ₦${String.format("%,.0f", item.price)}")
            }
        }
        _trackedItemIds.value = currentList
        _trackedItemInitialPrices.value = currentPrices
        prefs.edit().putString("tracked_item_ids", currentList.joinToString(",")).apply()
        prefs.edit().putString("tracked_item_prices", currentPrices.map { "${it.key}:${it.value}" }.joinToString(",")).apply()
    }

    fun toggleDarkMode() {
        val newValue = !_isDarkMode.value
        _isDarkMode.value = newValue
        prefs.edit().putBoolean("dark_mode", newValue).apply()
    }

    // Daily savings reminder flows
    private val _reminderEnabled = MutableStateFlow(prefs.getBoolean("reminder_enabled", false))
    val reminderEnabled: StateFlow<Boolean> = _reminderEnabled.asStateFlow()

    private val _reminderHour = MutableStateFlow(prefs.getInt("reminder_hour", 8))
    val reminderHour: StateFlow<Int> = _reminderHour.asStateFlow()

    private val _reminderMinute = MutableStateFlow(prefs.getInt("reminder_minute", 0))
    val reminderMinute: StateFlow<Int> = _reminderMinute.asStateFlow()

    fun setReminderEnabled(enabled: Boolean) {
        _reminderEnabled.value = enabled
        prefs.edit().putBoolean("reminder_enabled", enabled).apply()
        
        if (enabled) {
            com.example.receiver.SavingsReminderHelper.scheduleDailyReminder(
                getApplication(),
                _reminderHour.value,
                _reminderMinute.value
            )
            viewModelScope.launch {
                _uiEvent.emit("Daily savings reminder scheduled for ${String.format("%02d:%02d", _reminderHour.value, _reminderMinute.value)} ⏰")
            }
        } else {
            com.example.receiver.SavingsReminderHelper.cancelReminder(getApplication())
            viewModelScope.launch {
                _uiEvent.emit("Daily savings reminder turned off.")
            }
        }
    }

    fun setReminderTime(hour: Int, minute: Int) {
        _reminderHour.value = hour
        _reminderMinute.value = minute
        prefs.edit().putInt("reminder_hour", hour).putInt("reminder_minute", minute).apply()
        
        if (_reminderEnabled.value) {
            com.example.receiver.SavingsReminderHelper.scheduleDailyReminder(
                getApplication(),
                hour,
                minute
            )
            viewModelScope.launch {
                _uiEvent.emit("Daily savings reminder rescheduled for ${String.format("%02d:%02d", hour, minute)} ⏰")
            }
        }
    }

    fun triggerTestReminder() {
        com.example.receiver.SavingsReminderHelper.triggerTestNotification(getApplication())
    }

    private val _activeTab = MutableStateFlow("home") // home, wallet, goals, market, circles, assistant
    val activeTab: StateFlow<String> = _activeTab.asStateFlow()

    private val _cartState = MutableStateFlow<List<CartItem>>(emptyList())
    val cartState: StateFlow<List<CartItem>> = _cartState.asStateFlow()

    private val _wishlistState = MutableStateFlow<Set<Int>>(emptySet())
    val wishlistState: StateFlow<Set<Int>> = _wishlistState.asStateFlow()

    private val _priceAlerts = MutableStateFlow<List<PriceAlert>>(
        listOf(
            PriceAlert(1, "Rice", 45000.0, 52000.0),
            PriceAlert(2, "Tomatoes", 4000.0, 6500.0)
        )
    )
    val priceAlerts: StateFlow<List<PriceAlert>> = _priceAlerts.asStateFlow()

    private val _successModalState = MutableStateFlow<SuccessModalState?>(null)
    val successModalState: StateFlow<SuccessModalState?> = _successModalState.asStateFlow()

    fun showSuccessModal(type: SuccessType, title: String, message: String, amount: Double? = null, isGoalHit: Boolean = false) {
        _successModalState.value = SuccessModalState(
            isOpen = true,
            type = type,
            title = title,
            message = message,
            amount = amount,
            isGoalHit = isGoalHit
        )
    }

    fun dismissSuccessModal() {
        _successModalState.value = null
    }

    fun toggleWishlist(item: MarketItem) {
        val current = _wishlistState.value.toMutableSet()
        val isWishlisted = current.contains(item.id)
        if (isWishlisted) {
            current.remove(item.id)
            viewModelScope.launch {
                _uiEvent.emit("${item.name} removed from Wishlist.")
            }
        } else {
            current.add(item.id)
            viewModelScope.launch {
                _uiEvent.emit("${item.name} added to Wishlist!")
            }
        }
        _wishlistState.value = current
    }

    fun addToCart(item: MarketItem, portionName: String = "Full Unit", portionFraction: Double = 1.0) {
        val currentList = _cartState.value.toMutableList()
        val existingIndex = currentList.indexOfFirst { it.item.id == item.id && it.selectedPortionName == portionName }
        if (existingIndex != -1) {
            val existing = currentList[existingIndex]
            currentList[existingIndex] = existing.copy(quantity = existing.quantity + 1)
        } else {
            currentList.add(CartItem(item, 1, portionName, portionFraction))
        }
        _cartState.value = currentList
        val total = currentList.sumOf { (it.item.price * it.selectedPortionFraction) * it.quantity }
        viewModelScope.launch {
            val displayLabel = if (portionName == "Full Unit") item.name else "$portionName of ${item.name}"
            _uiEvent.emit("$displayLabel added to basket! Current total: ₦${String.format("%,.2f", total)}")
        }
    }

    fun removeFromCart(item: MarketItem, portionName: String = "Full Unit") {
        val currentList = _cartState.value.toMutableList()
        val existingIndex = currentList.indexOfFirst { it.item.id == item.id && it.selectedPortionName == portionName }
        if (existingIndex != -1) {
            val existing = currentList[existingIndex]
            if (existing.quantity > 1) {
                currentList[existingIndex] = existing.copy(quantity = existing.quantity - 1)
            } else {
                currentList.removeAt(existingIndex)
            }
        }
        _cartState.value = currentList
    }

    fun clearCart() {
        _cartState.value = emptyList()
    }

    fun checkoutCart(paymentMethod: String, customDeliveryFee: Double = 950.0) {
        val currentCart = _cartState.value
        if (currentCart.isEmpty()) return
        
        viewModelScope.launch {
            val currentWallet = walletState.value ?: WalletState()
            
            val rawSubtotal = currentCart.sumOf { (it.item.price * it.selectedPortionFraction) * it.quantity }
            val salesFee = rawSubtotal * 0.025
            val serviceCharge = 50.0 // flat service charge
            val deliveryFee = customDeliveryFee // delivery fee (can be customized/negotiated)
            val totalCost = rawSubtotal + salesFee + serviceCharge + deliveryFee
            
            if (paymentMethod == "Wallet") {
                if (currentWallet.availableBalance < totalCost) {
                    _uiEvent.emit("Insufficient wallet balance. Total cost: ₦${String.format("%,.2f", totalCost)} (incl. 2.5% fee + ₦50 service charge + ₦${String.format("%,.0f", deliveryFee)} delivery).")
                    return@launch
                }
            }
            
            // Group by vendor so we can create vendor-specific escrow orders
            val groupedByVendor = currentCart.groupBy { it.item.vendorName }
            
            groupedByVendor.forEach { (vendorName, vendorItems) ->
                val vendorSubtotal = vendorItems.sumOf { (it.item.price * it.selectedPortionFraction) * it.quantity }
                val vendorSalesFee = vendorSubtotal * 0.025
                val vendorServiceCharge = serviceCharge / groupedByVendor.size
                val vendorDeliveryFee = deliveryFee / groupedByVendor.size
                val vendorBaseDeliveryFee = 950.0 / groupedByVendor.size
                
                val itemNamesDesc = vendorItems.joinToString(", ") { 
                    val label = if (it.selectedPortionName == "Full Unit") it.item.name else "${it.selectedPortionName} of ${it.item.name}"
                    "$label (x${it.quantity})"
                }
                
                val generatedPickupPin = (1000..9999).random().toString()
                val generatedDeliveryPin = (1000..9999).random().toString()
                
                repository.insertEscrowOrder(EscrowOrder(
                    buyerName = _currentUserName.value,
                    vendorName = vendorName,
                    itemName = itemNamesDesc,
                    amount = vendorSubtotal,
                    status = "AWAITING_SELLER_ACCEPTANCE",
                    pickupPin = generatedPickupPin,
                    deliveryPin = generatedDeliveryPin,
                    isPickedUp = false,
                    salesFee = vendorSalesFee,
                    serviceCharge = vendorServiceCharge,
                    deliveryFee = vendorDeliveryFee,
                    baseDeliveryFee = vendorBaseDeliveryFee
                ))

                addNotification(
                    title = "New Order Request! 📦",
                    message = "Buyer ${_currentUserName.value} has ordered foodstuffs: $itemNamesDesc. Click Accept in your dashboard to confirm.",
                    type = "NEW_ORDER_REQUEST"
                )
            }
            
            clearCart()
            _uiEvent.emit("Cart order request sent! Total pending: ₦${String.format("%,.2f", totalCost)}. Awaiting seller acceptance.")
            showSuccessModal(
                type = SuccessType.ORDER,
                title = "Order Request Sent! 🛒",
                message = "Your food order of ₦${String.format("%,.2f", totalCost)} has been submitted! Once the seller accepts, funds will be securely held in escrow.",
                amount = totalCost
            )
        }
    }

    // Notification toast-like events
    private val _uiEvent = MutableSharedFlow<String>()
    val uiEvent: SharedFlow<String> = _uiEvent.asSharedFlow()

    private val _hasTriggeredAlert = MutableStateFlow(false)

    init {
        viewModelScope.launch {
            repository.initializeDatabaseIfEmpty()
        }

    }

    fun loginUser(phone: String, name: String, role: String, pin: String = "") {
        _userRole.value = role
        _currentUserPhone.value = phone
        _currentUserName.value = if (name.isNotEmpty()) name else "User $phone"
        _currentUserEmail.value = if (name.isNotEmpty()) "${name.lowercase().replace(" ", ".")}@afrisavmail.com" else "user.${phone}@afrisavmail.com"
        _currentUserLocation.value = "Lagos State, Alimosho LGA"
        _currentUserBio.value = "Active AfriSav member."
        _sortBy.value = "Nearest"
        
        if (role == "Rider") {
            _riderBalance.value = 16800.0
        } else if (role == "Dispatch Company") {
            _dispatchCompanyName.value = name.ifEmpty { "GIG Logistics" }
            _currentUserName.value = name.ifEmpty { "GIG Logistics" }
        }

        if (pin.isNotEmpty()) {
            try {
                SecureSessionManager.saveSession(
                    context = getApplication(),
                    phone = phone,
                    pin = pin,
                    role = role,
                    name = _currentUserName.value
                )
            } catch (t: Throwable) {
                t.printStackTrace()
            }
        }
        
        viewModelScope.launch {
            _uiEvent.emit("Successfully logged in as $role")
        }
    }

    fun registerUser(phone: String, name: String, role: String, pin: String = "", riderSubtype: String = "Individual", companyName: String = "", profilePhoto: String? = null) {
        _userRole.value = role
        _currentUserPhone.value = phone
        _currentUserLocation.value = "Lagos State, Alimosho LGA"
        _currentUserBio.value = "Active AfriSav member."
        _currentUserPhoto.value = profilePhoto
        _sortBy.value = "Nearest"
        
        if (role == "Rider") {
            _riderType.value = riderSubtype
            _currentUserName.value = name
            _currentUserEmail.value = "${name.lowercase().replace(" ", ".")}@afrisavmail.com"
            if (riderSubtype == "Company") {
                _dispatchCompanyName.value = companyName
                val currentList = _companyRiders.value.toMutableList()
                if (!currentList.any { it.name.equals(name, ignoreCase = true) }) {
                    currentList.add(
                        CompanyRiderInfo(
                            id = currentList.size + 1,
                            name = name,
                            phone = phone,
                            currentBalance = 16800.0,
                            completedTrips = 8,
                            status = "Online"
                        )
                    )
                    _companyRiders.value = currentList
                }
            } else {
                _dispatchCompanyName.value = ""
                _riderBalance.value = 16800.0
            }
        } else if (role == "Dispatch Company") {
            _currentUserName.value = companyName.ifEmpty { name }
            _dispatchCompanyName.value = companyName.ifEmpty { name }
            _currentUserEmail.value = "${companyName.lowercase().replace(" ", ".")}@afrisavmail.com"
        } else {
            _currentUserName.value = name
            _currentUserEmail.value = "${name.lowercase().replace(" ", ".")}@afrisavmail.com"
        }

        if (pin.isNotEmpty()) {
            try {
                SecureSessionManager.saveSession(
                    context = getApplication(),
                    phone = phone,
                    pin = pin,
                    role = role,
                    name = _currentUserName.value
                )
            } catch (t: Throwable) {
                t.printStackTrace()
            }
        }
        
        viewModelScope.launch {
            _uiEvent.emit("Successfully registered as ${if (role == "Rider") "$riderSubtype Rider" else role}")
        }
    }

    fun hasSavedSession(): Boolean {
        return try {
            SecureSessionManager.hasSession(getApplication())
        } catch (t: Throwable) {
            false
        }
    }

    fun getSavedRole(): String? {
        return try {
            SecureSessionManager.getSavedRole(getApplication())
        } catch (t: Throwable) {
            null
        }
    }

    fun getSavedName(): String? {
        return try {
            SecureSessionManager.getSavedName(getApplication())
        } catch (t: Throwable) {
            null
        }
    }

    fun getSavedPhone(): String? {
        return try {
            SecureSessionManager.getSavedPhone(getApplication())
        } catch (t: Throwable) {
            null
        }
    }

    fun getSavedPin(): String? {
        return try {
            SecureSessionManager.getSavedPin(getApplication())
        } catch (t: Throwable) {
            null
        }
    }

    fun loginWithSavedSession() {
        try {
            val phone = getSavedPhone() ?: return
            val name = getSavedName() ?: ""
            val role = getSavedRole() ?: "Buyer"
            val pin = getSavedPin() ?: ""
            loginUser(phone, name, role, pin)
        } catch (t: Throwable) {
            t.printStackTrace()
        }
    }

    fun canUnlockWithBiometrics(): Boolean {
        return hasSavedSession()
    }

    fun withdrawRiderFunds(amount: Double, bankName: String, accountNumber: String, riderName: String, requestedBy: String): Boolean {
        if (amount <= 0) return false
        if (requestedBy == "Rider") {
            if (_riderBalance.value < amount) return false
            _riderBalance.value -= amount
            val newWithdrawal = RiderWithdrawal(
                id = _riderWithdrawals.value.size + 1,
                riderName = riderName,
                amount = amount,
                bankName = bankName,
                accountNumber = accountNumber,
                requestedBy = "Rider"
            )
            _riderWithdrawals.value = listOf(newWithdrawal) + _riderWithdrawals.value
            viewModelScope.launch {
                _uiEvent.emit("Withdrawal of ₦${String.format("%,.2f", amount)} requested successfully!")
            }
            return true
        } else {
            var success = false
            val updatedRiders = _companyRiders.value.map { rider ->
                if (rider.name == riderName) {
                    if (rider.currentBalance >= amount) {
                        success = true
                        rider.copy(currentBalance = rider.currentBalance - amount)
                    } else {
                        rider
                    }
                } else {
                    rider
                }
            }
            if (!success) return false
            _companyRiders.value = updatedRiders
            val newWithdrawal = RiderWithdrawal(
                id = _riderWithdrawals.value.size + 1,
                riderName = riderName,
                amount = amount,
                bankName = bankName,
                accountNumber = accountNumber,
                requestedBy = "Company"
            )
            _riderWithdrawals.value = listOf(newWithdrawal) + _riderWithdrawals.value
            viewModelScope.launch {
                _uiEvent.emit("Company placed withdrawal of ₦${String.format("%,.2f", amount)} for rider $riderName!")
            }
            return true
        }
    }

    fun withdrawCompanyBulk(amount: Double, bankName: String, accountNumber: String): Boolean {
        if (amount <= 0) return false
        val totalBalance = _companyRiders.value.sumOf { it.currentBalance }
        if (totalBalance < amount) {
            viewModelScope.launch {
                _uiEvent.emit("Insufficient total balance!")
            }
            return false
        }
        
        var remainingDeduction = amount
        val updatedRiders = _companyRiders.value.map { rider ->
            if (remainingDeduction <= 0.0) {
                rider
            } else {
                val availableToDeduct = rider.currentBalance
                if (availableToDeduct >= remainingDeduction) {
                    val newBal = availableToDeduct - remainingDeduction
                    remainingDeduction = 0.0
                    rider.copy(currentBalance = newBal)
                } else {
                    remainingDeduction -= availableToDeduct
                    rider.copy(currentBalance = 0.0)
                }
            }
        }
        _companyRiders.value = updatedRiders
        
        val newWithdrawal = RiderWithdrawal(
            id = _riderWithdrawals.value.size + 1,
            riderName = _dispatchCompanyName.value.ifEmpty { _currentUserName.value.ifEmpty { "Logistics Company" } },
            amount = amount,
            bankName = bankName,
            accountNumber = accountNumber,
            requestedBy = "Company"
        )
        _riderWithdrawals.value = listOf(newWithdrawal) + _riderWithdrawals.value
        
        viewModelScope.launch {
            _uiEvent.emit("Successfully withdrew ₦${String.format("%,.2f", amount)} from total balance!")
        }
        return true
    }

    fun updateCompanyRiderStatusByName(riderName: String, newStatus: String, incrementTrips: Boolean = false) {
        val updatedList = _companyRiders.value.map { rider ->
            if (rider.name.equals(riderName, ignoreCase = true)) {
                rider.copy(
                    status = newStatus,
                    completedTrips = if (incrementTrips) rider.completedTrips + 1 else rider.completedTrips
                )
            } else {
                rider
            }
        }
        _companyRiders.value = updatedList
    }

    fun addCompanyRider(name: String, phone: String, initialBalance: Double = 0.0) {
        if (name.isBlank() || phone.isBlank()) return
        val currentList = _companyRiders.value.toMutableList()
        if (currentList.any { it.name.equals(name, ignoreCase = true) }) {
            viewModelScope.launch {
                _uiEvent.emit("Rider with name '$name' already exists!")
            }
            return
        }
        val nextId = (currentList.maxOfOrNull { it.id } ?: 0) + 1
        currentList.add(
            CompanyRiderInfo(
                id = nextId,
                name = name,
                phone = phone,
                currentBalance = initialBalance,
                completedTrips = 0,
                status = "Online"
            )
        )
        _companyRiders.value = currentList
        viewModelScope.launch {
            _uiEvent.emit("Rider '$name' successfully added to your dispatch fleet!")
        }
    }

    fun removeCompanyRider(riderId: Int) {
        val currentList = _companyRiders.value.toMutableList()
        val riderToRemove = currentList.find { it.id == riderId }
        if (riderToRemove != null) {
            currentList.remove(riderToRemove)
            _companyRiders.value = currentList
            viewModelScope.launch {
                _uiEvent.emit("Rider '${riderToRemove.name}' removed from fleet.")
            }
        }
    }

    fun updateCompanyRiderStatus(riderId: Int, newStatus: String) {
        val updatedList = _companyRiders.value.map {
            if (it.id == riderId) {
                it.copy(status = newStatus)
            } else {
                it
            }
        }
        _companyRiders.value = updatedList
        viewModelScope.launch {
            _uiEvent.emit("Rider status updated to $newStatus.")
        }
    }

    fun fundCompanyRiderBalance(riderId: Int, amount: Double): Boolean {
        if (amount <= 0) return false
        var riderName = ""
        val updatedList = _companyRiders.value.map {
            if (it.id == riderId) {
                riderName = it.name
                it.copy(currentBalance = it.currentBalance + amount)
            } else {
                it
            }
        }
        if (riderName.isEmpty()) return false
        _companyRiders.value = updatedList
        viewModelScope.launch {
            _uiEvent.emit("Funded ₦${String.format("%,.2f", amount)} to $riderName's wallet!")
        }
        return true
    }

    fun logoutUser() {
        try {
            SecureSessionManager.clearSession(getApplication())
        } catch (t: Throwable) {
            t.printStackTrace()
        }
        _userRole.value = "Buyer"
        _currentUserPhone.value = ""
        _currentUserName.value = "Guest"
        _currentUserEmail.value = ""
        _currentUserLocation.value = ""
        _currentUserBio.value = ""
        _currentUserPhoto.value = null
        _activeTab.value = "home"
    }

    fun updateUserProfile(name: String, phone: String, email: String, location: String, bio: String) {
        _currentUserName.value = name
        _currentUserPhone.value = phone
        _currentUserEmail.value = email
        _currentUserLocation.value = location
        _currentUserBio.value = bio
        viewModelScope.launch {
            _uiEvent.emit("Profile updated successfully")
        }
    }

    fun updateBuyerPin(newPin: String) {
        val phone = _currentUserPhone.value
        val name = _currentUserName.value
        val role = _userRole.value
        try {
            SecureSessionManager.saveSession(getApplication(), phone, newPin, role, name)
        } catch (t: Throwable) {
            t.printStackTrace()
        }
        viewModelScope.launch {
            _uiEvent.emit("Security PIN updated successfully")
        }
    }

    fun updateProfilePhoto(photoUrl: String?) {
        _currentUserPhoto.value = photoUrl
        viewModelScope.launch {
            _uiEvent.emit("Profile photo updated successfully")
        }
    }

    fun setActiveTab(tab: String) {
        _activeTab.value = tab
    }

    fun setMarketCategory(category: String) {
        _selectedMarketCategory.value = category
    }

    fun setMarketState(state: String) {
        _selectedMarketState.value = state
    }

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun setSortBy(sort: String) {
        _sortBy.value = sort
    }

    // Filtered Market Items
    val filteredMarketItems: StateFlow<List<MarketItem>> = combine(
        marketItems,
        _selectedMarketCategory,
        _selectedMarketState,
        _searchQuery,
        _sortBy
    ) { items, category, state, query, sort ->
        var list = items
        if (category != "All") {
            list = list.filter { item ->
                when (category.lowercase()) {
                    "grains" -> item.category.equals("Rice", ignoreCase = true) ||
                                item.category.equals("Beans", ignoreCase = true) ||
                                item.category.equals("Garri", ignoreCase = true) ||
                                item.category.equals("Grains", ignoreCase = true)
                    "proteins" -> item.category.equals("Fish", ignoreCase = true) ||
                                  item.category.equals("Meat", ignoreCase = true) ||
                                  item.category.equals("Poultry", ignoreCase = true) ||
                                  item.category.equals("Proteins", ignoreCase = true)
                    "vegetables" -> item.category.equals("Tomatoes", ignoreCase = true) ||
                                    item.category.equals("Pepper", ignoreCase = true) ||
                                    item.category.equals("Onion", ignoreCase = true) ||
                                    item.category.equals("Fruits", ignoreCase = true) ||
                                    item.category.equals("Vegetables", ignoreCase = true)
                    "tubers" -> item.category.equals("Yam", ignoreCase = true) ||
                                item.category.equals("Plantain", ignoreCase = true) ||
                                item.category.equals("Tubers", ignoreCase = true) ||
                                item.category.equals("Cassava", ignoreCase = true) ||
                                item.category.equals("Sweet Potato", ignoreCase = true)
                    else -> item.category.equals(category, ignoreCase = true)
                }
            }
        }
        if (state != "All") {
            list = list.filter { it.state.equals(state, ignoreCase = true) }
        }
        if (query.isNotEmpty()) {
            list = list.filter { it.name.contains(query, ignoreCase = true) || it.vendorName.contains(query, ignoreCase = true) }
        }
        when (sort) {
            "Cheapest" -> list.sortedBy { it.price }
            "Nearest" -> list.sortedBy { it.distance }
            "Highest Rated" -> list.sortedByDescending { it.rating }
            "Fastest Delivery" -> list.sortedBy { it.deliveryTimeMinutes }
            else -> list
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Seller Action to list foodstuffs
    fun addMarketItem(
        name: String, 
        price: Double, 
        category: String, 
        vendorName: String, 
        state: String, 
        imageUrl: String = "ic_food_custom",
        isBundle: Boolean = false,
        bundleItems: String = "",
        allowPortions: Boolean = true,
        portionType: String = "Bag"
    ) {
        viewModelScope.launch {
            val item = MarketItem(
                name = name,
                price = price,
                category = category,
                imageUrl = imageUrl,
                vendorName = vendorName,
                distance = Random.nextDouble(1.0, 10.0), // Simulate distance
                rating = 5.0,
                deliveryTimeMinutes = Random.nextInt(15, 45),
                state = state,
                isBundle = isBundle,
                bundleItems = bundleItems,
                allowPortions = allowPortions,
                portionType = portionType
            )
            repository.insertMarketItem(item)
            _uiEvent.emit("Successfully listed $name in $state!")
        }
    }

    // Smart Wallet Naira Contribution Actions
    fun updateKoboContributionGoal(goalInNaira: Double, frequency: String) {
        viewModelScope.launch {
            val current = walletState.value ?: WalletState()
            val updated = current.copy(
                koboContributionGoal = goalInNaira,
                koboGoalFrequency = frequency
            )
            repository.updateWallet(updated)
            _uiEvent.emit("Updated savings goal to ₦${String.format("%,.2f", goalInNaira)} ($frequency)!")
        }
    }

    fun updateLowBalanceSettings(isEnabled: Boolean, threshold: Double) {
        viewModelScope.launch {
            val current = walletState.value ?: WalletState()
            val updated = current.copy(
                isLowBalanceAlertEnabled = isEnabled,
                lowBalanceThreshold = threshold
            )
            repository.updateWallet(updated)
            _hasTriggeredAlert.value = false
            _uiEvent.emit("Updated low balance alert: ${if (isEnabled) "Enabled (Threshold: ₦${String.format("%,.0f", threshold)})" else "Disabled"}")
        }
    }

    fun updateAutoDepositSettings(isEnabled: Boolean, amount: Double, frequency: String) {
        viewModelScope.launch {
            val current = walletState.value ?: WalletState()
            val updated = current.copy(
                isAutoMonthlyDepositEnabled = isEnabled,
                autoMonthlyDepositAmount = amount,
                autoDepositFrequency = frequency
            )
            repository.updateWallet(updated)
            
            if (isEnabled) {
                addNotification(
                    title = "Auto-Deposit Scheduled ⚙️",
                    message = "Your automatic ${frequency.lowercase()} deposit of ₦${String.format("%,.0f", amount)} has been successfully scheduled into your food savings.",
                    type = "CIRCLE"
                )
            } else {
                addNotification(
                    title = "Auto-Deposit Disabled ⚠️",
                    message = "Automatic savings deposits into food savings have been turned off.",
                    type = "CIRCLE"
                )
            }
            _uiEvent.emit("Updated automatic savings deposit: ${if (isEnabled) "Enabled ($frequency, Amount: ₦${String.format("%,.0f", amount)})" else "Disabled"}")
        }
    }

    fun triggerSimulationAutoDeposit() {
        viewModelScope.launch {
            val current = walletState.value ?: WalletState()
            if (!current.isAutoMonthlyDepositEnabled) {
                _uiEvent.emit("Please enable automatic savings deposit in Wallet settings first! ⚙️")
                return@launch
            }
            
            val depositAmount = current.autoMonthlyDepositAmount
            val frequency = current.autoDepositFrequency
            if (current.availableBalance < depositAmount) {
                addNotification(
                    title = "Auto-Deposit Failed ❌",
                    message = "Automatic ${frequency.lowercase()} deposit of ₦${String.format("%,.0f", depositAmount)} failed due to insufficient available balance.",
                    type = "CIRCLE"
                )
                _uiEvent.emit("Auto-Deposit Simulation: Failed! Insufficient funds.")
                return@launch
            }
            
            val newAvailable = current.availableBalance - depositAmount
            val newSavings = current.savingsBalance + depositAmount
            val newTotalSaved = current.totalSaved + depositAmount
            
            val updated = current.copy(
                availableBalance = newAvailable,
                savingsBalance = newSavings,
                totalSaved = newTotalSaved
            )
            repository.updateWallet(updated)
            
            repository.insertTransaction(
                WalletTransaction(
                    type = "GOAL_SAVE",
                    amount = depositAmount,
                    title = "Auto Savings ($frequency)",
                    description = "Automatic ${frequency.lowercase()} food savings deposit"
                )
            )
            
            addNotification(
                title = "Auto-Deposit Executed 🎉",
                message = "Successfully transferred ₦${String.format("%,.0f", depositAmount)} from your AfriSav Wallet to your Food Savings Balance.",
                type = "CIRCLE"
            )
            _uiEvent.emit("Success: Simulated automatic ${frequency.lowercase()} deposit of ₦${String.format("%,.0f", depositAmount)}! 🚀")
        }
    }

    fun contributeKobo(amountInNaira: Double) {
        viewModelScope.launch {
            val current = walletState.value ?: WalletState()
            if (current.availableBalance < amountInNaira) {
                _uiEvent.emit("Insufficient balance to contribute ₦${String.format("%,.2f", amountInNaira)}")
                return@launch
            }
            
            val newAvailable = current.availableBalance - amountInNaira
            val newSavings = current.savingsBalance + amountInNaira
            val newTotalSaved = current.totalSaved + amountInNaira
            val newContribution = current.currentKoboContribution + amountInNaira
            
            val updated = current.copy(
                availableBalance = newAvailable,
                savingsBalance = newSavings,
                totalSaved = newTotalSaved,
                currentKoboContribution = newContribution
            )
            repository.updateWallet(updated)
            
            repository.insertTransaction(WalletTransaction(
                type = "GOAL_SAVE",
                amount = amountInNaira,
                title = "Smart Wallet Contribution",
                description = "Saved ₦${String.format("%,.2f", amountInNaira)} to Smart Wallet"
            ))
            
            _uiEvent.emit("Contributed ₦${String.format("%,.2f", amountInNaira)} successfully!")
            
            if (newContribution >= current.koboContributionGoal) {
                showSuccessModal(
                    type = SuccessType.SAVINGS_GOAL,
                    title = "Naira Contribution Goal Met! 🎉",
                    message = "Amazing, pikin! You have reached your ${current.koboGoalFrequency.lowercase()} target of ₦${String.format("%,.2f", current.koboContributionGoal)}! Keep up this beautiful savings habit! 😊",
                    amount = current.koboContributionGoal,
                    isGoalHit = true
                )
            } else {
                showSuccessModal(
                    type = SuccessType.TRANSACTION,
                    title = "Naira Contributed! 💰",
                    message = "You saved ₦${String.format("%,.2f", amountInNaira)}. Only ₦${String.format("%,.2f", current.koboContributionGoal - newContribution)} left to hit your ${current.koboGoalFrequency.lowercase()} goal!",
                    amount = amountInNaira
                )
            }
        }
    }

    fun resetKoboContribution() {
        viewModelScope.launch {
            val current = walletState.value ?: WalletState()
            val updated = current.copy(currentKoboContribution = 0.0)
            repository.updateWallet(updated)
            _uiEvent.emit("Reset your contribution progress for the new cycle.")
        }
    }

    // Wallet Actions
    fun fundWallet(amount: Double) {
        viewModelScope.launch {
            val current = walletState.value ?: WalletState()
            val updated = current.copy(
                availableBalance = current.availableBalance + amount,
                totalSaved = current.totalSaved + amount
            )
            repository.updateWallet(updated)
            repository.insertTransaction(WalletTransaction(
                type = "FUND",
                amount = amount,
                title = "Wallet Funded",
                description = "Funded wallet with ₦${String.format("%,.2f", amount)} via Card/Transfer"
            ))
            _uiEvent.emit("Successfully funded wallet with ₦${String.format("%,.2f", amount)}")
            showSuccessModal(
                type = SuccessType.TRANSACTION,
                title = "Wallet Funded Successfully! 🎉",
                message = "Your spendable wallet has been credited with ₦${String.format("%,.2f", amount)} via secure payment transfer.",
                amount = amount
            )
        }
    }

    fun transferFunds(amount: Double, recipient: String) {
        viewModelScope.launch {
            val current = walletState.value ?: WalletState()
            if (current.availableBalance < amount) {
                _uiEvent.emit("Insufficient balance to transfer ₦${String.format("%,.2f", amount)}")
                return@launch
            }
            val updated = current.copy(availableBalance = current.availableBalance - amount)
            repository.updateWallet(updated)
            repository.insertTransaction(WalletTransaction(
                type = "TRANSFER",
                amount = amount,
                title = "Transfer to $recipient",
                description = "Transferred ₦${String.format("%,.2f", amount)} to $recipient"
            ))
            _uiEvent.emit("Transferred ₦${String.format("%,.2f", amount)} to $recipient")
            showSuccessModal(
                type = SuccessType.TRANSACTION,
                title = "Transfer Successful! 🚀",
                message = "You have transferred ₦${String.format("%,.2f", amount)} to $recipient. The recipient will receive the funds immediately.",
                amount = amount
            )
        }
    }

    // Goal Actions
    fun createGoal(title: String, targetAmount: Double, category: String, autoSaveAmount: Double, autoSaveEnabled: Boolean) {
        viewModelScope.launch {
            val goal = SavingsGoal(
                title = title,
                targetAmount = targetAmount,
                savedAmount = 0.0,
                category = category,
                isAutoSaveEnabled = autoSaveEnabled,
                autoSaveAmount = autoSaveAmount,
                estimatedCompletionDate = "${(targetAmount / if (autoSaveAmount > 0) autoSaveAmount else 500.0).toInt()} Days Left"
            )
            repository.insertGoal(goal)
            _uiEvent.emit("Created Food Goal: $title")
        }
    }

    fun saveToGoal(goalId: Int, amount: Double) {
        viewModelScope.launch {
            val currentWallet = walletState.value ?: WalletState()
            if (currentWallet.availableBalance < amount) {
                _uiEvent.emit("Insufficient available balance. Please fund your wallet first.")
                return@launch
            }

            val goal = savingsGoals.value.find { it.id == goalId } ?: return@launch
            val newSavedAmount = goal.savedAmount + amount
            if (newSavedAmount > goal.targetAmount) {
                _uiEvent.emit("Save amount exceeds target. Target is ₦${String.format("%,.2f", goal.targetAmount)}")
                return@launch
            }

            // Update goal
            val updatedGoal = goal.copy(savedAmount = newSavedAmount)
            repository.updateGoal(updatedGoal)

            // Update wallet
            val updatedWallet = currentWallet.copy(
                availableBalance = currentWallet.availableBalance - amount,
                savingsBalance = currentWallet.savingsBalance + amount
            )
            repository.updateWallet(updatedWallet)

            // Add Transaction
            repository.insertTransaction(WalletTransaction(
                type = "GOAL_SAVE",
                amount = amount,
                title = "Goal Savings: ${goal.title}",
                description = "Saved ₦${String.format("%,.2f", amount)} towards ${goal.title}"
            ))

            _uiEvent.emit("Saved ₦${String.format("%,.2f", amount)} to ${goal.title}!")

            val isGoalCompleted = newSavedAmount >= goal.targetAmount
            if (isGoalCompleted) {
                showSuccessModal(
                    type = SuccessType.SAVINGS_GOAL,
                    title = "Savings Goal Achieved! 🎉",
                    message = "Congratulations! You have fully hit your savings goal for \"${goal.title}\" by saving ₦${String.format("%,.2f", goal.targetAmount)}! The food funds are locked and ready to buy wholesale items.",
                    amount = goal.targetAmount,
                    isGoalHit = true
                )
            } else {
                showSuccessModal(
                    type = SuccessType.TRANSACTION,
                    title = "Saved Successfully! 💰",
                    message = "You have added ₦${String.format("%,.2f", amount)} to your \"${goal.title}\" savings goal. Only ₦${String.format("%,.2f", goal.targetAmount - newSavedAmount)} left to hit your goal!",
                    amount = amount
                )
            }
        }
    }

    fun pauseResumeGoal(goalId: Int) {
        viewModelScope.launch {
            val goal = savingsGoals.value.find { it.id == goalId } ?: return@launch
            val updated = goal.copy(isPaused = !goal.isPaused)
            repository.updateGoal(updated)
            _uiEvent.emit(if (updated.isPaused) "Goal paused" else "Goal resumed")
        }
    }

    fun unlockGoal(goalId: Int) {
        viewModelScope.launch {
            val goal = savingsGoals.value.find { it.id == goalId } ?: return@launch
            val updated = goal.copy(isLocked = false)
            repository.updateGoal(updated)
            _uiEvent.emit("Goal unlocked successfully!")
        }
    }

    fun toggleGoalLock(goalId: Int) {
        viewModelScope.launch {
            val goal = savingsGoals.value.find { it.id == goalId } ?: return@launch
            val updated = goal.copy(isLocked = !goal.isLocked)
            repository.updateGoal(updated)
            _uiEvent.emit(if (updated.isLocked) "Goal locked" else "Goal unlocked successfully!")
        }
    }

    fun withdrawGoalFunds(goalId: Int, isEmergency: Boolean = false) {
        viewModelScope.launch {
            val goal = savingsGoals.value.find { it.id == goalId } ?: return@launch
            if (goal.isLocked) {
                _uiEvent.emit("Goal is locked! Please unlock it first to withdraw.")
                return@launch
            }
            if (goal.savedAmount <= 0) {
                _uiEvent.emit("No savings to withdraw from this goal.")
                return@launch
            }

            val isCompleted = goal.savedAmount >= goal.targetAmount
            if (!isCompleted && !isEmergency) {
                _uiEvent.emit("GOAL_INCOMPLETE_WITHDRAWAL_CONFIRM:$goalId")
                return@launch
            }

            val currentWallet = walletState.value ?: WalletState()
            
            val penaltyRate = if (!isCompleted && isEmergency) 0.05 else 0.0
            val penaltyFee = goal.savedAmount * penaltyRate
            val refundAmount = goal.savedAmount - penaltyFee

            // Update Goal
            val updatedGoal = goal.copy(savedAmount = 0.0, isLocked = true)
            repository.updateGoal(updatedGoal)

            // Update Wallet
            val updatedWallet = currentWallet.copy(
                availableBalance = currentWallet.availableBalance + refundAmount,
                savingsBalance = currentWallet.savingsBalance - goal.savedAmount
            )
            repository.updateWallet(updatedWallet)

            // Transaction
            val txTitle = if (penaltyFee > 0) "Emergency Goal Refund" else "Goal Funds Refunded"
            val txDesc = if (penaltyFee > 0) {
                "Withdrew ₦${String.format("%,.2f", refundAmount)} from ${goal.title} to wallet after a 2.5% emergency penalty (₦${String.format("%,.2f", penaltyFee)})"
            } else {
                "Withdrew ₦${String.format("%,.2f", refundAmount)} from ${goal.title} to wallet"
            }

            repository.insertTransaction(WalletTransaction(
                type = "WITHDRAW",
                amount = refundAmount,
                title = txTitle,
                description = txDesc
            ))

            if (penaltyFee > 0) {
                _uiEvent.emit("Emergency withdrawal processed! Refunded ₦${String.format("%,.2f", refundAmount)} to Wallet (₦${String.format("%,.2f", penaltyFee)} penalty deducted).")
            } else {
                _uiEvent.emit("Withdrew ₦${String.format("%,.2f", refundAmount)} back to Wallet!")
            }
        }
    }

    fun withdrawGoalFundsToBank(goalId: Int, bankName: String, accountNumber: String, isEmergency: Boolean = false) {
        viewModelScope.launch {
            val goal = savingsGoals.value.find { it.id == goalId } ?: return@launch
            if (goal.isLocked) {
                _uiEvent.emit("Goal is locked! Please unlock it first to withdraw.")
                return@launch
            }
            if (goal.savedAmount <= 0) {
                _uiEvent.emit("No savings to withdraw from this goal.")
                return@launch
            }

            val isCompleted = goal.savedAmount >= goal.targetAmount
            if (!isCompleted && !isEmergency) {
                _uiEvent.emit("GOAL_INCOMPLETE_BANK_WITHDRAWAL_CONFIRM:$goalId|$bankName|$accountNumber")
                return@launch
            }

            val currentWallet = walletState.value ?: WalletState()
            
            val penaltyRate = if (!isCompleted && isEmergency) 0.05 else 0.0
            val penaltyFee = goal.savedAmount * penaltyRate
            val refundAmount = goal.savedAmount - penaltyFee

            // Update Goal
            val updatedGoal = goal.copy(savedAmount = 0.0, isLocked = true)
            repository.updateGoal(updatedGoal)

            // Update Wallet: deduct savings balance but do NOT add to availableBalance (since it went to bank)
            val updatedWallet = currentWallet.copy(
                savingsBalance = currentWallet.savingsBalance - goal.savedAmount
            )
            repository.updateWallet(updatedWallet)

            // Transactions & Reports
            val txTitle = if (penaltyFee > 0) "Emergency Goal Bank Cashout" else "Goal Bank Cashout"
            val txDesc = if (penaltyFee > 0) {
                "Withdrew ₦${String.format("%,.2f", refundAmount)} from ${goal.title} directly to $bankName ($accountNumber) after 2.5% emergency penalty (₦${String.format("%,.2f", penaltyFee)})"
            } else {
                "Withdrew ₦${String.format("%,.2f", refundAmount)} from ${goal.title} directly to $bankName ($accountNumber)"
            }

            repository.insertTransaction(WalletTransaction(
                type = "WITHDRAW",
                amount = refundAmount,
                title = txTitle,
                description = txDesc
            ))

            repository.insertSellerWithdrawal(SellerWithdrawal(
                vendorName = _currentUserName.value,
                amount = refundAmount,
                bankName = bankName,
                accountNumber = accountNumber
            ))

            if (penaltyFee > 0) {
                _uiEvent.emit("Emergency bank withdrawal requested! ₦${String.format("%,.2f", refundAmount)} will settle to $bankName ($accountNumber) shortly (₦${String.format("%,.2f", penaltyFee)} penalty deducted).")
            } else {
                _uiEvent.emit("Withdrawal of ₦${String.format("%,.2f", refundAmount)} requested! Funds will settle to $bankName ($accountNumber) shortly.")
            }
        }
    }

    fun buyFoodWithCompletedGoal(goalId: Int) {
        viewModelScope.launch {
            val goal = savingsGoals.value.find { it.id == goalId } ?: return@launch
            if (goal.savedAmount < goal.targetAmount) {
                _uiEvent.emit("Savings goal is not complete yet.")
                return@launch
            }

            val currentWallet = walletState.value ?: WalletState()
            val price = goal.targetAmount
            val salesFee = price * 0.025
            val serviceCharge = 50.0
            val totalCost = price + salesFee + serviceCharge
            val extraFee = salesFee + serviceCharge

            if (currentWallet.availableBalance < extraFee) {
                _uiEvent.emit("Insufficient wallet balance for fees. You need ₦${String.format("%,.2f", extraFee)} in available balance to cover checkout fee and service charge.")
                return@launch
            }

            // Update Wallet: deduct price from savingsBalance, and deduct extraFee from availableBalance
            val updatedWallet = currentWallet.copy(
                availableBalance = currentWallet.availableBalance - extraFee,
                savingsBalance = currentWallet.savingsBalance - price
            )
            repository.updateWallet(updatedWallet)

            // Update Goal: reset savedAmount to 0.0
            val updatedGoal = goal.copy(savedAmount = 0.0)
            repository.updateGoal(updatedGoal)

            // Insert Transaction
            repository.insertTransaction(WalletTransaction(
                type = "BUY_FOOD",
                amount = totalCost,
                title = "Bought: ${goal.title}",
                description = "Purchased using completed savings goal. Food: ₦${String.format("%,.0f", price)} from savings. Fees: ₦${String.format("%,.2f", extraFee)} from available balance. Secure escrow active."
            ))

            // Generate pickup and delivery PINs
            val generatedPickupPin = (1000..9999).random().toString()
            val generatedDeliveryPin = (1000..9999).random().toString()

            val vendorName = when (goal.category.lowercase()) {
                "rice" -> "Salami Stores (Mushin)"
                "beans" -> "Ibrahim Agro (Mile 12)"
                "garri" -> "Mama Ejima Foods (Oyingbo)"
                else -> "Cooperative Partner Vendor"
            }

            // Create Escrow Order
            repository.insertEscrowOrder(EscrowOrder(
                buyerName = _currentUserName.value,
                vendorName = vendorName,
                itemName = goal.title,
                amount = price,
                status = "PENDING",
                pickupPin = generatedPickupPin,
                deliveryPin = generatedDeliveryPin,
                isPickedUp = false,
                salesFee = salesFee,
                serviceCharge = serviceCharge
            ))

            _uiEvent.emit("Successfully purchased ${goal.title}! ₦${String.format("%,.2f", price)} debited from food savings. Your Pickup & Delivery PINs are ready under Orders!")
            showSuccessModal(
                type = SuccessType.ORDER,
                title = "Wholesale Purchase Complete! 📦",
                message = "Successfully purchased \"${goal.title}\" using your completed savings goal. ₦${String.format("%,.2f", price)} was debited from your savings balance. Your Pickup & Delivery PINs are ready under the Orders tab.",
                amount = price
            )
        }
    }

    fun deleteGoal(goalId: Int) {
        viewModelScope.launch {
            val goal = savingsGoals.value.find { it.id == goalId } ?: return@launch
            // Return funds if any
            if (goal.savedAmount > 0) {
                withdrawGoalFunds(goalId)
            }
            repository.deleteGoal(goal)
            _uiEvent.emit("Deleted Food Goal: ${goal.title}")
        }
    }

    // Marketplace Actions
    fun buyProductNow(item: MarketItem, paymentMethod: String, portionName: String = "", portionPrice: Double? = null) {
        viewModelScope.launch {
            val currentWallet = walletState.value ?: WalletState()
            
            val finalPrice = portionPrice ?: item.price
            val finalName = if (portionName.isNotEmpty()) "$portionName of ${item.name}" else item.name
            
            val salesFee = finalPrice * 0.025
            val serviceCharge = 50.0
            val totalCost = finalPrice + salesFee + serviceCharge
            
            if (paymentMethod == "Wallet") {
                if (currentWallet.availableBalance < totalCost) {
                    _uiEvent.emit("Insufficient wallet balance. Total cost: ₦${String.format("%,.2f", totalCost)} (incl. 2.5% fee + ₦50 service charge).")
                    return@launch
                }
            }

            // Generate 4-digit random PINs for seller pickup and buyer delivery
            val generatedPickupPin = (1000..9999).random().toString()
            val generatedDeliveryPin = (1000..9999).random().toString()

            // Create Escrow Order in AWAITING_SELLER_ACCEPTANCE state
            repository.insertEscrowOrder(EscrowOrder(
                buyerName = _currentUserName.value,
                vendorName = item.vendorName,
                itemName = finalName,
                amount = finalPrice,
                status = "AWAITING_SELLER_ACCEPTANCE",
                pickupPin = generatedPickupPin,
                deliveryPin = generatedDeliveryPin,
                isPickedUp = false,
                salesFee = salesFee,
                serviceCharge = serviceCharge
            ))

            addNotification(
                title = "New Order Request! 📦",
                message = "Buyer ${_currentUserName.value} wants to buy \"$finalName\". Click Accept in your dashboard to confirm.",
                type = "NEW_ORDER_REQUEST"
            )
            
            _uiEvent.emit("Order request sent! Awaiting seller acceptance.")
            showSuccessModal(
                type = SuccessType.ORDER,
                title = "Order Request Sent! 📦",
                message = "Your order of \"$finalName\" from ${item.vendorName} for ₦${String.format("%,.2f", totalCost)} has been sent! Once the seller accepts, funds will be securely held in escrow.",
                amount = totalCost
            )
        }
    }

    fun acceptOrder(orderId: Int) {
        viewModelScope.launch {
            val orders = allEscrowOrders.value
            val order = orders.find { it.id == orderId }
            if (order == null) {
                _uiEvent.emit("Order not found.")
                return@launch
            }
            if (order.status != "AWAITING_SELLER_ACCEPTANCE") {
                _uiEvent.emit("Order is already accepted or processed.")
                return@launch
            }

            // Retrieve the buyer's wallet state
            val buyerWallet = withContext(Dispatchers.IO) {
                database.koboDao().getWalletState().first()
            } ?: WalletState()

            val totalCost = order.amount + order.salesFee + order.serviceCharge + order.deliveryFee

            if (buyerWallet.availableBalance < totalCost) {
                _uiEvent.emit("Buyer has insufficient wallet balance (Requires ₦${String.format("%,.2f", totalCost)}) to complete acceptance.")
                return@launch
            }

            // 1. Deduct from buyer's wallet
            val updatedWallet = buyerWallet.copy(availableBalance = buyerWallet.availableBalance - totalCost)
            repository.updateWallet(updatedWallet)

            // 2. Insert buyer wallet transaction
            repository.insertTransaction(WalletTransaction(
                type = "BUY_FOOD",
                amount = totalCost,
                title = "Escrow: Order from ${order.vendorName}",
                description = "Items: ${order.itemName} | Total: ₦${String.format("%,.0f", totalCost)} held in secure escrow."
            ))

            // 3. Update escrow order status to PENDING
            val acceptedOrder = order.copy(status = "PENDING")
            repository.updateEscrowOrder(acceptedOrder)

            // 4. Alert both buyer and seller
            addNotification(
                title = "Order Accepted! ✅",
                message = "Seller ${order.vendorName} has accepted your order \"${order.itemName}\". Your payment of ₦${String.format("%,.0f", totalCost)} is safely held in escrow.",
                type = "ORDER_ACCEPTED"
            )

            addNotification(
                title = "Order Accepted! 📦",
                message = "You have accepted the order \"${order.itemName}\" from ${order.buyerName}. Prepare pickup and share the Pickup PIN ${order.pickupPin} with the rider.",
                type = "ORDER_ACCEPTED"
            )

            _uiEvent.emit("Order accepted successfully! Buyer's funds escrowed.")
        }
    }

    fun declineOrder(orderId: Int) {
        viewModelScope.launch {
            val orders = allEscrowOrders.value
            val order = orders.find { it.id == orderId }
            if (order == null) {
                _uiEvent.emit("Order not found.")
                return@launch
            }
            if (order.status != "AWAITING_SELLER_ACCEPTANCE") {
                _uiEvent.emit("Order is already processed.")
                return@launch
            }

            // 1. Update order status to DECLINED
            val declinedOrder = order.copy(status = "DECLINED")
            repository.updateEscrowOrder(declinedOrder)

            // 2. Alert both buyer and seller
            addNotification(
                title = "Order Declined ❌",
                message = "Seller ${order.vendorName} has declined your order \"${order.itemName}\". No funds were deducted from your wallet.",
                type = "ORDER_DECLINED"
            )

            addNotification(
                title = "Order Request Declined",
                message = "You declined the order \"${order.itemName}\" from ${order.buyerName}.",
                type = "ORDER_DECLINED"
            )

            _uiEvent.emit("Order request declined successfully.")
        }
    }

    fun confirmEscrowReceipt(
        orderId: Int,
        vendorRating: Int,
        vendorReview: String,
        buyerRating: Int,
        buyerReview: String,
        riderRating: Int = 0,
        riderReview: String = ""
    ) {
        viewModelScope.launch {
            val allEscrow = withContext(Dispatchers.IO) { repository.getAllEscrowOrders().first() }
            val order = allEscrow.find { it.id == orderId } ?: return@launch
            
            if (order.status == "PENDING") {
                // 1. Update order status to COMPLETED
                val completedOrder = order.copy(
                    status = "COMPLETED",
                    deliveryStatus = "DELIVERED",
                    currentEtaMinutes = 0
                )
                repository.updateEscrowOrder(completedOrder)
                
                // 2. Release funds to the seller
                val currentEarnings = withContext(Dispatchers.IO) {
                    database.koboDao().getSellerEarningState(order.vendorName).first()
                } ?: SellerEarningState(vendorName = order.vendorName)
                
                val updatedEarnings = currentEarnings.copy(
                    availableBalance = currentEarnings.availableBalance + order.amount,
                    totalRevenue = currentEarnings.totalRevenue + order.amount,
                    totalSalesCount = currentEarnings.totalSalesCount + 1
                )
                repository.insertSellerEarningState(updatedEarnings)
                
                // 3. Record Seller Sale (Trade Volume)
                val cal = java.util.Calendar.getInstance()
                val monthNames = arrayOf(
                    "January", "February", "March", "April", "May", "June",
                    "July", "August", "September", "October", "November", "December"
                )
                val currentMonth = monthNames[cal.get(java.util.Calendar.MONTH)]
                
                repository.insertSellerSale(SellerSale(
                    vendorName = order.vendorName,
                    itemName = order.itemName,
                    category = "Market",
                    amount = order.amount,
                    month = currentMonth,
                    buyerName = order.buyerName
                ))
                
                // 4. Record Buyer Rating of the Vendor
                repository.insertReviewRating(ReviewRating(
                    reviewerName = order.buyerName,
                    targetName = order.vendorName,
                    isTargetSeller = true,
                    rating = vendorRating,
                    reviewText = vendorReview,
                    orderId = order.id
                ))
                
                // 5. Record Vendor Rating of the Buyer (Simulated peer-to-peer review)
                repository.insertReviewRating(ReviewRating(
                    reviewerName = order.vendorName,
                    targetName = order.buyerName,
                    isTargetSeller = false,
                    rating = buyerRating,
                    reviewText = buyerReview,
                    orderId = order.id
                ))

                // 5b. Record Buyer Rating of the Dispatch Rider (if rider assigned and rated)
                if (riderRating > 0 && !order.riderName.isNullOrEmpty()) {
                    repository.insertReviewRating(ReviewRating(
                        reviewerName = order.buyerName,
                        targetName = order.riderName,
                        isTargetSeller = false,
                        rating = riderRating,
                        reviewText = riderReview,
                        orderId = order.id
                    ))
                }

                // 6. Release delivery fee to rider
                if (!order.riderName.isNullOrEmpty()) {
                    val currentList = _companyRiders.value
                    if (currentList.any { it.name.equals(order.riderName, ignoreCase = true) }) {
                        val updatedList = currentList.map { r ->
                            if (r.name.equals(order.riderName, ignoreCase = true)) {
                                r.copy(currentBalance = r.currentBalance + order.deliveryFee, completedTrips = r.completedTrips + 1)
                            } else {
                                r
                            }
                        }
                        _companyRiders.value = updatedList
                    } else if (_riderType.value == "Individual" && _userRole.value == "Rider" && _currentUserName.value.equals(order.riderName, ignoreCase = true)) {
                        _riderBalance.value += order.deliveryFee
                    } else {
                        _riderBalance.value += order.deliveryFee
                    }
                }
                
                _uiEvent.emit("Success! Released ₦${String.format("%,.2f", order.amount)} to ${order.vendorName}. Ratings submitted!")
                showSuccessModal(
                    type = SuccessType.TRANSACTION,
                    title = "Escrow Released! 🤝",
                    message = "Success! You have released ₦${String.format("%,.2f", order.amount)} from secure escrow to ${order.vendorName}. Your ratings and feedback have been successfully submitted.",
                    amount = order.amount
                )
            }
        }
    }

    fun verifyRiderPickup(orderId: Int, pin: String, onResult: (Boolean, String) -> Unit) {
        viewModelScope.launch {
            val orders = allEscrowOrders.value
            val order = orders.find { it.id == orderId }
            if (order == null) {
                onResult(false, "Order not found.")
                return@launch
            }
            if (order.pickupPin == pin) {
                val updatedOrder = order.copy(
                    isPickedUp = true,
                    riderName = _currentUserName.value,
                    deliveryStatus = "IN_TRANSIT",
                    currentEtaMinutes = 15
                )
                repository.updateEscrowOrder(updatedOrder)
                // Automatically update company rider status to In Transit when trip starts
                updateCompanyRiderStatusByName(_currentUserName.value, "In Transit")
                _uiEvent.emit("Cargo pickup verified successfully for ${order.itemName}!")
                onResult(true, "Success")
            } else {
                onResult(false, "Incorrect Pickup PIN. Please request the correct PIN from the seller.")
            }
        }
    }

    fun simulateDeliveryStep(orderId: Int) {
        viewModelScope.launch {
            val orders = allEscrowOrders.value
            val order = orders.find { it.id == orderId } ?: return@launch
            val nextStatus = when (order.deliveryStatus) {
                "CONFIRMED" -> "PREPARING"
                "PREPARING" -> "READY_FOR_PICKUP"
                "READY_FOR_PICKUP" -> "IN_TRANSIT"
                "IN_TRANSIT" -> "ARRIVED"
                "ARRIVED" -> "DELIVERED"
                else -> "CONFIRMED"
            }
            val nextEta = when (nextStatus) {
                "CONFIRMED" -> 35
                "PREPARING" -> 30
                "READY_FOR_PICKUP" -> 20
                "IN_TRANSIT" -> 15
                "ARRIVED" -> 0
                "DELIVERED" -> 0
                else -> 25
            }
            val isPickedUpNow = when (nextStatus) {
                "IN_TRANSIT", "ARRIVED", "DELIVERED" -> true
                else -> order.isPickedUp
            }
            val updatedOrder = order.copy(
                deliveryStatus = nextStatus,
                currentEtaMinutes = nextEta,
                isPickedUp = isPickedUpNow,
                riderName = if (order.riderName.isNullOrEmpty() && (nextStatus == "READY_FOR_PICKUP" || nextStatus == "IN_TRANSIT" || nextStatus == "ARRIVED")) "Adekunle (AfriSav Dispatch)" else order.riderName
            )
            repository.updateEscrowOrder(updatedOrder)
            _uiEvent.emit("Delivery status updated to: $nextStatus! 🚴")
        }
    }

    fun startAutoDeliverySimulation(orderId: Int) {
        viewModelScope.launch {
            _uiEvent.emit("Starting real-time delivery tracking simulation... 🚀")
            val statuses = listOf("CONFIRMED", "PREPARING", "READY_FOR_PICKUP", "IN_TRANSIT", "ARRIVED")
            val etas = listOf(35, 30, 20, 10, 0)
            for (i in statuses.indices) {
                val orders = allEscrowOrders.value
                val order = orders.find { it.id == orderId } ?: break
                
                // If it was already completed/delivered, stop simulating
                if (order.status == "COMPLETED" || order.deliveryStatus == "DELIVERED") break
                
                val status = statuses[i]
                val eta = etas[i]
                val isPickedUpNow = i >= 3 // IN_TRANSIT and beyond
                val updatedOrder = order.copy(
                    deliveryStatus = status,
                    currentEtaMinutes = eta,
                    isPickedUp = isPickedUpNow,
                    riderName = if (order.riderName.isNullOrEmpty()) "Adekunle (AfriSav Dispatch)" else order.riderName
                )
                repository.updateEscrowOrder(updatedOrder)
                _uiEvent.emit("Tracker Alert: Order is now $status 📍")
                kotlinx.coroutines.delay(4000) // Delay 4 seconds per step
            }
        }
    }

    fun verifyRiderDelivery(orderId: Int, pin: String, onResult: (Boolean, String) -> Unit) {
        viewModelScope.launch {
            val orders = allEscrowOrders.value
            val order = orders.find { it.id == orderId }
            if (order == null) {
                onResult(false, "Order not found.")
                return@launch
            }
            if (!order.isPickedUp) {
                onResult(false, "Cargo must be picked up from the seller before marking as delivered.")
                return@launch
            }
            if (order.deliveryPin == pin) {
                // When verified, mark as COMPLETED and release funds to seller
                confirmEscrowReceipt(
                    orderId = orderId,
                    vendorRating = 5,
                    vendorReview = "Delivered safely via verified dispatch rider.",
                    buyerRating = 5,
                    buyerReview = "Verified buyer received cargo."
                )
                // Automatically update company rider status to Online and increment trips when trip ends
                updateCompanyRiderStatusByName(order.riderName ?: _currentUserName.value, "Online", incrementTrips = true)
                onResult(true, "Success")
            } else {
                onResult(false, "Incorrect Delivery PIN. Upon inspection and satisfaction, the buyer must generate/provide the correct Delivery PIN.")
            }
        }
    }

    fun acceptRiderJob(orderId: Int) {
        viewModelScope.launch {
            val orders = allEscrowOrders.value
            val order = orders.find { it.id == orderId }
            if (order != null) {
                val updated = order.copy(riderName = _currentUserName.value)
                repository.updateEscrowOrder(updated)
                _uiEvent.emit("Job accepted! Head to ${order.vendorName} to pick up cargo.")
            }
        }
    }

    fun proposeRiderFee(orderId: Int, proposedFee: Double) {
        viewModelScope.launch {
            val orders = allEscrowOrders.value
            val order = orders.find { it.id == orderId }
            if (order != null) {
                if (proposedFee < order.baseDeliveryFee) {
                    _uiEvent.emit("Proposed fee cannot be lower than the base delivery fee of ₦${String.format("%,.0f", order.baseDeliveryFee)}.")
                    return@launch
                }
                val updated = order.copy(
                    riderProposedFee = proposedFee,
                    riderProposalName = _currentUserName.value
                )
                repository.updateEscrowOrder(updated)
                _uiEvent.emit("Counter offer of ₦${String.format("%,.0f", proposedFee)} submitted to buyer!")
            }
        }
    }

    fun acceptRiderCounterOffer(orderId: Int) {
        viewModelScope.launch {
            val orders = allEscrowOrders.value
            val order = orders.find { it.id == orderId }
            if (order != null && order.riderProposedFee > 0.0 && !order.riderProposalName.isNullOrEmpty()) {
                val difference = order.riderProposedFee - order.deliveryFee
                val currentWallet = walletState.value ?: WalletState()
                if (currentWallet.availableBalance < difference) {
                    _uiEvent.emit("Insufficient wallet balance to pay the negotiated fare difference of ₦${String.format("%,.0f", difference)}.")
                    return@launch
                }
                
                // Deduct difference from buyer's wallet
                val updatedWallet = currentWallet.copy(availableBalance = currentWallet.availableBalance - difference)
                repository.updateWallet(updatedWallet)
                
                // Record transaction
                repository.insertTransaction(WalletTransaction(
                    type = "BUY_FOOD",
                    amount = difference,
                    title = "Fare Negotiation: ${order.riderProposalName}",
                    description = "Additional negotiated delivery fare for order ${order.itemName}."
                ))
                
                // Update escrow order
                val updated = order.copy(
                    deliveryFee = order.riderProposedFee,
                    riderName = order.riderProposalName,
                    riderProposedFee = 0.0,
                    riderProposalName = null
                )
                repository.updateEscrowOrder(updated)
                _uiEvent.emit("Offer accepted! Rider ${updated.riderName} is now assigned at ₦${String.format("%,.0f", updated.deliveryFee)}.")
            }
        }
    }

    fun rejectRiderCounterOffer(orderId: Int) {
        viewModelScope.launch {
            val orders = allEscrowOrders.value
            val order = orders.find { it.id == orderId }
            if (order != null) {
                val updated = order.copy(
                    riderProposedFee = 0.0,
                    riderProposalName = null
                )
                repository.updateEscrowOrder(updated)
                _uiEvent.emit("Rider's counter-offer rejected.")
            }
        }
    }

    fun negotiateBuyerDeliveryFee(orderId: Int, newFee: Double) {
        viewModelScope.launch {
            val orders = allEscrowOrders.value
            val order = orders.find { it.id == orderId }
            if (order != null) {
                if (newFee < order.baseDeliveryFee) {
                    _uiEvent.emit("Delivery offer cannot be lower than the base price of ₦${String.format("%,.0f", order.baseDeliveryFee)}.")
                    return@launch
                }
                val difference = newFee - order.deliveryFee
                if (difference > 0) {
                    val currentWallet = walletState.value ?: WalletState()
                    if (currentWallet.availableBalance < difference) {
                        _uiEvent.emit("Insufficient wallet balance to increase the delivery fee by ₦${String.format("%,.0f", difference)}.")
                        return@launch
                    }
                    // Deduct difference from buyer's wallet
                    val updatedWallet = currentWallet.copy(availableBalance = currentWallet.availableBalance - difference)
                    repository.updateWallet(updatedWallet)
                    
                    // Record transaction
                    repository.insertTransaction(WalletTransaction(
                        type = "BUY_FOOD",
                        amount = difference,
                        title = "Offer Increased: Order #${order.id}",
                        description = "Increased delivery fee offer to ₦${String.format("%,.0f", newFee)}."
                    ))
                } else if (difference < 0) {
                    // If they decrease it (but still >= base), refund the difference to the wallet!
                    val refund = -difference
                    val currentWallet = walletState.value ?: WalletState()
                    val updatedWallet = currentWallet.copy(availableBalance = currentWallet.availableBalance + refund)
                    repository.updateWallet(updatedWallet)
                    
                    // Record refund transaction
                    repository.insertTransaction(WalletTransaction(
                        type = "FUND",
                        amount = refund,
                        title = "Offer Decreased Refund",
                        description = "Refund for lowering delivery offer back to ₦${String.format("%,.0f", newFee)}."
                    ))
                }
                
                val updated = order.copy(
                    deliveryFee = newFee,
                    riderProposedFee = 0.0, // Clear stale rider proposals if the buyer updates their offer
                    riderProposalName = null
                )
                repository.updateEscrowOrder(updated)
                _uiEvent.emit("Delivery offer updated to ₦${String.format("%,.0f", newFee)}!")
            }
        }
    }

    fun sellerWithdraw(amount: Double, bankName: String, accountNumber: String) {
        viewModelScope.launch {
            val vendorName = _currentUserName.value
            val currentEarnings = withContext(Dispatchers.IO) {
                database.koboDao().getSellerEarningState(vendorName).first()
            } ?: SellerEarningState(vendorName = vendorName)

            if (currentEarnings.availableBalance < amount) {
                _uiEvent.emit("Insufficient seller balance for withdrawal.")
                return@launch
            }

            val updatedEarnings = currentEarnings.copy(
                availableBalance = currentEarnings.availableBalance - amount
            )
            repository.insertSellerEarningState(updatedEarnings)

            // Log Seller Withdrawal
            repository.insertSellerWithdrawal(SellerWithdrawal(
                vendorName = vendorName,
                amount = amount,
                bankName = bankName,
                accountNumber = accountNumber
            ))

            _uiEvent.emit("Withdrawal of ₦${String.format("%,.0f", amount)} requested! Funds will settle to $bankName ($accountNumber) shortly.")
        }
    }

    fun submitReviewRating(targetName: String, isTargetSeller: Boolean, rating: Int, reviewText: String, orderId: Int = 0) {
        viewModelScope.launch {
            val reviewerName = _currentUserName.value
            repository.insertReviewRating(ReviewRating(
                reviewerName = reviewerName,
                targetName = targetName,
                isTargetSeller = isTargetSeller,
                rating = rating,
                reviewText = reviewText,
                orderId = orderId
            ))
            _uiEvent.emit("Review submitted successfully!")
        }
    }

    fun saveTowardsProduct(item: MarketItem, initialSave: Double = 0.0) {
        viewModelScope.launch {
            val goalTitle = "Save for ${item.name}"
            // Check if already exists
            val exists = savingsGoals.value.any { it.title == goalTitle }
            if (exists) {
                _uiEvent.emit("Goal already exists for this product.")
                return@launch
            }

            createGoal(
                title = goalTitle,
                targetAmount = item.price,
                category = item.category,
                autoSaveAmount = item.price / 30.0, // Default 30 days
                autoSaveEnabled = false
            )

            if (initialSave > 0) {
                // Wait for DB insertion and then add funds
                val freshGoals = repository.savingsGoals.first()
                val newlyCreated = freshGoals.find { it.title == goalTitle }
                if (newlyCreated != null) {
                    saveToGoal(newlyCreated.id, initialSave)
                }
            }
        }
    }

    // Community Circles Actions
    fun contributeToCircle(circleId: Int, amount: Double) {
        viewModelScope.launch {
            val currentWallet = walletState.value ?: WalletState()
            if (currentWallet.availableBalance < amount) {
                _uiEvent.emit("Insufficient balance to contribute to Food Circle.")
                return@launch
            }

            val circle = foodCircles.value.find { it.id == circleId } ?: return@launch
            val newAmount = circle.currentAmount + amount
            val isUnlocked = newAmount >= circle.targetAmount

            val updatedCircle = circle.copy(
                currentAmount = newAmount,
                isWholesaleUnlocked = isUnlocked
            )
            repository.updateFoodCircle(updatedCircle)

            val updatedWallet = currentWallet.copy(
                availableBalance = currentWallet.availableBalance - amount,
                savingsBalance = currentWallet.savingsBalance + amount
            )
            repository.updateWallet(updatedWallet)

            repository.insertTransaction(WalletTransaction(
                type = "GOAL_SAVE",
                amount = amount,
                title = "Circle Contribution: ${circle.title}",
                description = "Contributed ₦${String.format("%,.2f", amount)} to bulk purchase circle"
            ))

            _uiEvent.emit("Contributed ₦${String.format("%,.2f", amount)} successfully! " + 
                if (isUnlocked) "Wholesale Prices UNLOCKED! 🎉" else "")

            if (isUnlocked) {
                addNotification(
                    title = "Circle Goal Met! 🎉",
                    message = "Bulk purchase circle \"${circle.title}\" has reached its goal of ₦${String.format("%,.0f", circle.targetAmount)}! Wholesale prices are now unlocked for all members.",
                    type = "CIRCLE"
                )
                showSuccessModal(
                    type = SuccessType.CIRCLE,
                    title = "Wholesale Prices Unlocked! 🎉",
                    message = "Incredible! Your contribution of ₦${String.format("%,.2f", amount)} has fully funded the bulk purchase circle \"${circle.title}\"! Wholesale prices are now officially unlocked for all members.",
                    amount = circle.targetAmount,
                    isGoalHit = true
                )
            } else {
                showSuccessModal(
                    type = SuccessType.TRANSACTION,
                    title = "Contribution Successful! 👥",
                    message = "You contributed ₦${String.format("%,.2f", amount)} to \"${circle.title}\". Only ₦${String.format("%,.2f", circle.targetAmount - newAmount)} left to unlock wholesale pricing!",
                    amount = amount
                )
            }
        }
    }

    fun createCircle(title: String, description: String, targetAmount: Double, initialContribution: Double, isPrivate: Boolean) {
        viewModelScope.launch {
            val generatedCode = "KB-" + (10000..99999).random().toString()
            val creator = _currentUserName.value.ifBlank { "You" }
            val circle = FoodCircle(
                title = title,
                description = description,
                targetAmount = targetAmount,
                currentAmount = 0.0,
                membersCount = 1,
                imageUrl = "ic_circle_custom",
                circleCode = generatedCode,
                creatorName = creator,
                members = creator,
                isPrivate = isPrivate
            )
            repository.insertFoodCircle(circle)
            
            val freshCircles = repository.foodCircles.first()
            val newlyCreated = freshCircles.find { it.circleCode == generatedCode }
            if (newlyCreated != null && initialContribution > 0) {
                contributeToCircle(newlyCreated.id, initialContribution)
            }
            
            _uiEvent.emit("Created Food Circle: $title with Invite Code: $generatedCode! 🚀")
        }
    }

    fun joinCircle(code: String) {
        viewModelScope.launch {
            val normalizedCode = code.trim().uppercase()
            if (normalizedCode.isBlank()) {
                _uiEvent.emit("Please enter a valid circle code.")
                return@launch
            }

            val circles = repository.foodCircles.first()
            val circleToJoin = circles.find { it.circleCode.uppercase() == normalizedCode }
            if (circleToJoin == null) {
                _uiEvent.emit("Food Circle with code \"$normalizedCode\" not found! Please check and try again.")
                return@launch
            }

            val userName = _currentUserName.value.ifBlank { "You" }
            val currentMembers = circleToJoin.members.split(",").map { it.trim() }
            if (currentMembers.any { it.equals(userName, ignoreCase = true) }) {
                _uiEvent.emit("You are already a member of \"${circleToJoin.title}\"!")
                return@launch
            }

            val currentPending = circleToJoin.pendingRequests.split(",").map { it.trim() }.filter { it.isNotEmpty() }
            if (currentPending.any { it.equals(userName, ignoreCase = true) }) {
                _uiEvent.emit("You have already requested to join \"${circleToJoin.title}\"!")
                return@launch
            }

            val updatedPending = if (circleToJoin.pendingRequests.isBlank()) userName else "${circleToJoin.pendingRequests}, $userName"
            val updatedCircle = circleToJoin.copy(
                pendingRequests = updatedPending
            )
            repository.updateFoodCircle(updatedCircle)

            addNotification(
                title = "Join Request Sent! ✉️",
                message = "Your request to join \"${circleToJoin.title}\" has been sent to the admin for approval.",
                type = "CIRCLE"
            )

            _uiEvent.emit("Join request sent for \"${circleToJoin.title}\"! Waiting for approval. ⏳")
        }
    }

    fun approveJoinRequest(circleId: Int, requesterName: String) {
        viewModelScope.launch {
            val circles = repository.foodCircles.first()
            val circle = circles.find { it.id == circleId } ?: return@launch
            
            // Remove from pending
            val pendingList = circle.pendingRequests.split(",").map { it.trim() }.filter { it.isNotEmpty() && !it.equals(requesterName, ignoreCase = true) }
            val updatedPending = pendingList.joinToString(", ")
            
            // Add to members
            val currentMembers = circle.members.split(",").map { it.trim() }.filter { it.isNotEmpty() }.toMutableList()
            if (!currentMembers.contains(requesterName)) {
                currentMembers.add(requesterName)
            }
            val updatedMembers = currentMembers.joinToString(", ")
            
            val updatedCircle = circle.copy(
                membersCount = currentMembers.size,
                members = updatedMembers,
                pendingRequests = updatedPending
            )
            repository.updateFoodCircle(updatedCircle)
            _uiEvent.emit("Approved $requesterName's request to join \"${circle.title}\"! 🎉")
        }
    }

    fun rejectJoinRequest(circleId: Int, requesterName: String) {
        viewModelScope.launch {
            val circles = repository.foodCircles.first()
            val circle = circles.find { it.id == circleId } ?: return@launch
            
            // Remove from pending
            val pendingList = circle.pendingRequests.split(",").map { it.trim() }.filter { it.isNotEmpty() && !it.equals(requesterName, ignoreCase = true) }
            val updatedPending = pendingList.joinToString(", ")
            
            val updatedCircle = circle.copy(
                pendingRequests = updatedPending
            )
            repository.updateFoodCircle(updatedCircle)
            _uiEvent.emit("Rejected $requesterName's request to join \"${circle.title}\".")
        }
    }

    fun toggleCoAdminRole(circleId: Int, memberName: String) {
        viewModelScope.launch {
            val circles = repository.foodCircles.first()
            val circle = circles.find { it.id == circleId } ?: return@launch
            
            val currentCoAdmins = circle.coAdmins.split(",")
                .map { it.trim() }
                .filter { it.isNotEmpty() }
                .toMutableList()
            
            val isAlreadyCoAdmin = currentCoAdmins.any { it.equals(memberName, ignoreCase = true) }
            if (isAlreadyCoAdmin) {
                currentCoAdmins.removeAll { it.equals(memberName, ignoreCase = true) }
                val updatedCircle = circle.copy(coAdmins = currentCoAdmins.joinToString(", "))
                repository.updateFoodCircle(updatedCircle)
                _uiEvent.emit("Removed Co-Admin privileges from $memberName for \"${circle.title}\".")
            } else {
                currentCoAdmins.add(memberName)
                val updatedCircle = circle.copy(coAdmins = currentCoAdmins.joinToString(", "))
                repository.updateFoodCircle(updatedCircle)
                _uiEvent.emit("Assigned Co-Admin role to $memberName for \"${circle.title}\"! 🛡️")
            }
        }
    }

    // Chat / AI Assistant Actions
    fun sendMessageToMama(text: String) {
        if (text.trim().isEmpty()) return
        
        viewModelScope.launch {
            // 1. Add user message
            val userMsg = ChatMessage(text = text, isUser = true)
            repository.insertChatMessage(userMsg)
            _isChatLoading.value = true

            // 2. Fetch all messages for chat history context
            val history = chatMessages.value + userMsg

            // 3. Get response from Gemini API or local pidgin model fallback
            val reply = getMamaResponse(history, text)

            // 4. Insert Mama Olufunke reply
            repository.insertChatMessage(ChatMessage(text = reply, isUser = false))
            _isChatLoading.value = false
        }
    }

    fun clearChat() {
        viewModelScope.launch {
            repository.clearChat()
            repository.insertChatMessage(ChatMessage(
                text = "My pikin! I have cleared our old chat. Anything you want to ask me about food prices, savings, or wholesale bulk buying, just ask me again!",
                isUser = false
            ))
        }
    }

    private suspend fun getMamaResponse(chatHistory: List<ChatMessage>, latestQuery: String): String = withContext(Dispatchers.IO) {
        val apiKey = BuildConfig.GEMINI_API_KEY
        val hasApiKey = apiKey.isNotEmpty() && apiKey != "MY_GEMINI_API_KEY"

        if (hasApiKey) {
            try {
                // Setup prompt and system instruction
                val systemInstructionText = """
                    You are Mama Olufunke, a warm, energetic, extremely wise and savings-savvy Nigerian market woman and food savings advisor.
                    You speak in a highly engaging, friendly tone, blending Standard English with warm, natural Nigerian Pidgin English and expressions (e.g. 'Ah!', 'My pikin!', 'Abeg', 'No worry at all!', 'Chop better life').
                    You help users budget for food, save toward food products, compare market prices, join 'Food Circles' to save on bulk shipping/wholesale prices, and navigate the AfriSav application.
                    
                    Keep your answers highly practical, filled with warmth and empathy for the current economy, and encouraging of financial prudence. Refer to actual Nigerian markets (like Mile 12, Alaba, Ibadan farms, Oyingbo) and popular food items (Rice, Beans, Garri, Tomatoes, Pepper, Yam, Plantains, Fish, Meat).
                    Make sure you suggest using AfriSav's key features like 'Save Towards This Product', 'Food Circles' (Bulk Buying), and the 'Smart Wallet'.
                    Keep response length concise (1-2 short paragraphs or clean bullet points).
                """.trimIndent()

                val systemInstruction = GeminiContent(parts = listOf(GeminiPart(text = systemInstructionText)))
                
                // Map history to Gemini content list (keep it inside safe limit)
                val conversationContents = chatHistory.takeLast(10).map { msg ->
                    GeminiContent(parts = listOf(GeminiPart(
                        text = (if (msg.isUser) "User: " else "Mama Olufunke: ") + msg.text
                    )))
                }

                val requestBody = GeminiRequest(
                    contents = conversationContents,
                    systemInstruction = systemInstruction,
                    generationConfig = GeminiGenerationConfig(temperature = 0.7f)
                )

                val response = GeminiRetrofitClient.service.generateContent(apiKey, requestBody)
                val replyText = response.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text
                if (!replyText.isNullOrEmpty()) {
                    return@withContext replyText
                }
            } catch (e: Exception) {
                // Fall back to rule-based engine on network/API failure
            }
        }

        // --- Rule-Based Fallback Engine (Nigerian Pidgin Theme) ---
        val query = latestQuery.lowercase()
        return@withContext when {
            query.contains("rice") -> {
                "Ah, Rice! Rice is gold now, my pikin! Today in our marketplace, Alaba Food Hub has 50kg Mama Gold Rice for ₦70,800. But if you join the 'Mile 12 Bulk Rice Circle', we can split it together and get wholesale price. Why not use the 'Save Towards This Product' button? Even ₦500 daily will make you reach there before you know it!"
            }
            query.contains("cheapest") || query.contains("price") || query.contains("compare") -> {
                "My pikin, price comparison is my middle name! Today, Fresh Tomatoes Basket is ₦4,500 from Fresh Basket Lagos. Yam is ₦5,500 for 3 large tubers from Mile 12. If you want cheapest rice, look at Alaba Food Hub (₦70,800). Always sort by 'Cheapest' in our marketplace to get the best deals!"
            }
            query.contains("save") || query.contains("goal") || query.contains("how do i") -> {
                "E easy well well! Just click on the 'Savings Goals' tab, then click the floating action button to create a food goal. You can name it '50kg Rice' or 'My Monthly Groceries'. Then set your target (like ₦75,000) and choose auto-save. You can save ₦100, ₦500 or ₦1,000 daily. Small small, basket go full!"
            }
            query.contains("wallet") || query.contains("fund") || query.contains("money") -> {
                "Aba, your Smart Wallet is your food helper! You can click 'Fund Wallet' in the Wallet tab to add money via Bank Transfer or Card. When you have money in your available balance, you can save it into your Food Goals or buy items directly. Safe and fast!"
            }
            query.contains("circle") || query.contains("group") || query.contains("bulk") -> {
                "Ah! 'Food Circles' is where the real savings is! Nigeria's power is in community! In Food Circles, you and other families save together to buy food in bulk directly from farmers or wholesale cold rooms. It unlocks wholesale price and splits delivery cost. Join 'Mile 12 Bulk Rice Circle' or create your own circle today!"
            }
            query.contains("hello") || query.contains("hi") || query.contains("olufunke") || query.contains("mama") || query.contains("welcome") -> {
                "Welcome, my pikin! How family? I hope they are doing well! I am Mama Olufunke, your personal market helper. Tell me, what food do you want to save for today, or you want to know about current prices in the market?"
            }
            else -> {
                "Ah, I hear you my pikin! Food security is very important. To feed family well, we must save little by little and buy smart. You can set a Food Goal, join a Food Circle, or browse our marketplace to compare prices. Anything you need, Mama Olufunke is here to help you!"
            }
        }
    }

    // --- FEATURE 1: GROUP SAVINGS SIMULATION & SOCIAL GROWTH ---
    fun simulateCircleContributions(circleId: Int) {
        viewModelScope.launch {
            val circle = foodCircles.value.find { it.id == circleId } ?: return@launch
            val names = listOf("Chinedu", "Amina", "Oluwaseun", "Fatima", "Emeka", "Nkechi", "Tunde", "Yetunde")
            val randomName = names.random()
            val remaining = circle.targetAmount - circle.currentAmount
            if (remaining <= 0.0) {
                _uiEvent.emit("Circle is already fully funded and unlocked!")
                return@launch
            }
            val contribAmount = (circle.targetAmount * 0.15).coerceAtLeast(1000.0).coerceAtMost(remaining)
            val finalAmount = if (contribAmount <= 0.0) 2500.0 else contribAmount

            val newAmount = circle.currentAmount + finalAmount
            val isUnlocked = newAmount >= circle.targetAmount

            val currentMembers = circle.members.split(",").map { it.trim() }.filter { it.isNotEmpty() }.toMutableList()
            var addedCount = 0
            if (!currentMembers.contains(randomName)) {
                currentMembers.add(randomName)
                addedCount = 1
            }
            val updatedMembers = currentMembers.joinToString(", ")

            val updatedCircle = circle.copy(
                currentAmount = newAmount,
                isWholesaleUnlocked = isUnlocked,
                membersCount = circle.membersCount + addedCount,
                members = updatedMembers
            )
            repository.updateFoodCircle(updatedCircle)

            if (isUnlocked) {
                addNotification(
                    title = "Circle Goal Met! 🎉",
                    message = "Bulk purchase circle \"${circle.title}\" has reached its goal of ₦${String.format("%,.0f", circle.targetAmount)}! Wholesale prices are now unlocked for all members.",
                    type = "CIRCLE"
                )
            }

            _uiEvent.emit("Simulated: $randomName contributed ₦${String.format("%,.0f", finalAmount)}! Circle growing! 🚀")
        }
    }

    fun triggerSimulationContributionDue() {
        viewModelScope.launch {
            addNotification(
                title = "Circle Contribution Due 💰",
                message = "Friendly reminder: Your community savings contribution of ₦5,000 for 'Gbagada Fresh Veggies Circle' is due in 24 hours.",
                type = "CIRCLE"
            )
            _uiEvent.emit("Alert: Circle contribution due notification sent! Check notifications. 🔔")
        }
    }

    fun triggerSimulationInviteReceived() {
        viewModelScope.launch {
            addNotification(
                title = "New Circle Invitation ✉️",
                message = "Chinedu Okafor has invited you to join the private 'Surulere Organic Grain Circle'. Tap Accept to join! Code: KB-SURLORGN",
                type = "INVITE"
            )
            _uiEvent.emit("Alert: Circle invitation received! Check notifications to accept. ✉️")
        }
    }

    // --- FEATURE 2: MAMA BASKET RECIPE INGREDIENT AUTO-PLANNER ---
    fun addRecipeIngredientsToCart(recipeName: String, ingredientKeywords: List<String>) {
        viewModelScope.launch {
            val currentItems = marketItems.value
            var addedCount = 0
            ingredientKeywords.forEach { kw ->
                val match = currentItems.find { it.name.contains(kw, ignoreCase = true) || it.category.contains(kw, ignoreCase = true) }
                if (match != null) {
                    addToCart(match)
                    addedCount++
                }
            }
            if (addedCount > 0) {
                _uiEvent.emit("Added $addedCount ingredients for '$recipeName' to your Food Basket! 🍳")
            } else {
                _uiEvent.emit("Could not find matching ingredients in local markets.")
            }
        }
    }

    // --- FEATURE 3: FOOD PRICE ALERTS ---
    fun addPriceAlert(category: String, targetPrice: Double, currentPrice: Double) {
        val current = _priceAlerts.value.toMutableList()
        val nextId = (current.maxOfOrNull { it.id } ?: 0) + 1
        current.add(PriceAlert(nextId, category, targetPrice, currentPrice))
        _priceAlerts.value = current
        viewModelScope.launch {
            _uiEvent.emit("Price Alert set for $category when it drops to ₦${String.format("%,.0f", targetPrice)}! 🔔")
        }
    }

    fun removePriceAlert(alertId: Int) {
        val current = _priceAlerts.value.filter { it.id != alertId }
        _priceAlerts.value = current
        viewModelScope.launch {
            _uiEvent.emit("Price Alert removed.")
        }
    }

    fun simulatePriceDrop() {
        viewModelScope.launch {
            val alerts = _priceAlerts.value
            val trackedIds = _trackedItemIds.value
            val initialPrices = _trackedItemInitialPrices.value

            if (alerts.isEmpty() && trackedIds.isEmpty()) {
                _uiEvent.emit("Set a category alert or track a product's price first!")
                return@launch
            }

            // Let's update marketplace prices to match the drop (simulated)
            val updatedMarket = marketItems.value.map { item ->
                if (trackedIds.contains(item.id)) {
                    val initialPrice = initialPrices[item.id] ?: item.price
                    // Drop price below initial threshold by 15% to trigger alert
                    item.copy(price = initialPrice * 0.85)
                } else {
                    val matchingAlert = alerts.find { it.category.equals(item.category, ignoreCase = true) }
                    if (matchingAlert != null) {
                        item.copy(price = matchingAlert.targetPrice)
                    } else {
                        item
                    }
                }
            }
            // Update repository
            updatedMarket.forEach { repository.updateMarketItem(it) }

            // Add notifications for price drops
            alerts.forEach { alert ->
                addNotification(
                    title = "Price Drop Alert! 📉",
                    message = "Great news! Saved food item category \"${alert.category}\" has dropped in price to your target of ₦${String.format("%,.0f", alert.targetPrice)}!",
                    type = "PRICE_DROP"
                )
            }

            // Add notifications for tracked items
            trackedIds.forEach { id ->
                val item = updatedMarket.find { it.id == id }
                val initialPrice = initialPrices[id]
                if (item != null && initialPrice != null && item.price < initialPrice) {
                    addNotification(
                        title = "Tracked Item Price Drop! 📉",
                        message = "Your tracked food item \"${item.name}\" has dropped below your threshold of ₦${String.format("%,.0f", initialPrice)}! It is now ₦${String.format("%,.0f", item.price)}.",
                        type = "PRICE_DROP"
                    )
                }
            }

            _uiEvent.emit("🔔 Alert! Local wholesale prices dropped! Your set alerts are now ACTIVE!")
        }
    }

    // --- FEATURE 4: DETAILED ESCROW DISPUTES ENGINE ---
    fun fileEscrowDispute(orderId: Int, reason: String) {
        viewModelScope.launch {
            val orders = allEscrowOrders.value
            val order = orders.find { it.id == orderId } ?: return@launch
            val updated = order.copy(status = "DISPUTED")
            repository.updateEscrowOrder(updated)
            _uiEvent.emit("Dispute filed for order of ${order.itemName}. Escrow funds of ₦${String.format("%,.2f", order.amount)} are securely put on HOLD! 🛡️")
        }
    }

    fun resolveEscrowDispute(orderId: Int, refundToBuyer: Boolean) {
        viewModelScope.launch {
            val orders = allEscrowOrders.value
            val order = orders.find { it.id == orderId } ?: return@launch
            if (refundToBuyer) {
                val currentWallet = walletState.value ?: WalletState()
                val updatedWallet = currentWallet.copy(
                    availableBalance = currentWallet.availableBalance + order.amount
                )
                repository.updateWallet(updatedWallet)
                
                val updated = order.copy(status = "REFUNDED")
                repository.updateEscrowOrder(updated)
                
                repository.insertTransaction(WalletTransaction(
                    type = "FUND",
                    amount = order.amount,
                    title = "Escrow Refund: ${order.itemName}",
                    description = "Dispute resolved in your favor. Funds refunded."
                ))
                _uiEvent.emit("Dispute resolved! Refunded ₦${String.format("%,.2f", order.amount)} to Buyer's wallet. 🛡️")
            } else {
                confirmEscrowReceipt(
                    orderId = orderId,
                    vendorRating = 3,
                    vendorReview = "Dispute resolved: Funds released to Seller.",
                    buyerRating = 3,
                    buyerReview = "Dispute resolved."
                )
                _uiEvent.emit("Dispute resolved! Escrow funds released to Seller: ${order.vendorName}. 🛡️")
            }
        }
    }

    fun triggerUiEvent(message: String) {
        viewModelScope.launch {
            _uiEvent.emit(message)
        }
    }

    private val _basketPreviewState = MutableStateFlow<BasketPreviewState>(BasketPreviewState.Idle)
    val basketPreviewState: StateFlow<BasketPreviewState> = _basketPreviewState.asStateFlow()

    fun generateBasketPreview(basketItemsWithQty: List<Pair<MarketItem, Int>>) {
        viewModelScope.launch {
            _basketPreviewState.value = BasketPreviewState.Loading
            
            val itemsDescription = basketItemsWithQty.joinToString(", ") { "${it.second}x ${it.first.name}" }
            
            val prompt = """
                The user has built a custom food basket bundle in their AfriSav app consisting of: $itemsDescription.
                
                Please generate a beautiful, sensory, culturally rich Nigerian description of how this custom food basket is arranged and presented. Paint a vivid picture of these exact items beautifully arranged in a traditional woven wicker basket, resting on a clean wooden table.
                
                Also suggest:
                1. A wholesome, delicious, healthy local West African meal or dish that can be prepared with these exact ingredients (be creative, e.g., Jollof, Yam Porridge, Garri with rich stew, etc.).
                2. A warm market woman's encouraging blessing/comment in friendly Nigerian pidgin (from Mama Olufunke) about their smart shopping choice.
                
                Keep the output concise, structured with emojis, and visually appealing. Use standard headings or bold text. Avoid markdown codeblocks. Keep the response to 3-4 bullet points or short paragraphs.
            """.trimIndent()
            
            val apiKey = BuildConfig.GEMINI_API_KEY
            val hasApiKey = apiKey.isNotEmpty() && apiKey != "MY_GEMINI_API_KEY"
            
            if (hasApiKey) {
                try {
                    val systemInstruction = GeminiContent(parts = listOf(GeminiPart(text = "You are a warm, traditional West African culinary artist and food helper.")))
                    val requestBody = GeminiRequest(
                        contents = listOf(GeminiContent(parts = listOf(GeminiPart(text = prompt)))),
                        systemInstruction = systemInstruction,
                        generationConfig = GeminiGenerationConfig(temperature = 0.8f)
                    )
                    
                    val response = GeminiRetrofitClient.service.generateContent(apiKey, requestBody)
                    val replyText = response.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text
                    if (!replyText.isNullOrEmpty()) {
                        _basketPreviewState.value = BasketPreviewState.Success(replyText)
                        return@launch
                    }
                } catch (e: Exception) {
                    // fall back to default
                }
            }
            
            // Fallback description if offline or API key missing
            kotlinx.coroutines.delay(1500) // simulate loading beautifully
            val fallback = """
                🧺 **Your Custom Basket Arrangement:**
                Your custom bundle of $itemsDescription is beautifully arranged in a hand-woven palm-frond basket. The fresh, rich ingredients sit neatly under warm golden daylight, representing a proud, healthy household.
                
                🍳 **Mama's Meal Suggestion:**
                Combine these staples to prepare a rich, heartwarming meal! It's perfect for cooking a classic local pot of stew, serving wholesome family portions, or storing as key kitchen reserves.
                
                🇳🇬 **Mama Olufunke's Blessing:**
                "Ah! My pikin, you choose well! This basket is packed with wisdom. Small-small, your kitchen go always dey full of joy! Keep saving, you are doing great!"
            """.trimIndent()
            _basketPreviewState.value = BasketPreviewState.Success(fallback)
        }
    }

    fun resetBasketPreview() {
        _basketPreviewState.value = BasketPreviewState.Idle
    }

    fun downloadTransactionReceipt(context: android.content.Context, tx: WalletTransaction) {
        viewModelScope.launch {
            val result = kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) {
                try {
                    val fileName = "AfriSav_Receipt_${tx.id}.txt"
                    val sdf = java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss", java.util.Locale.getDefault())
                    val dateStr = sdf.format(java.util.Date(tx.timestamp))
                    val receiptContent = """
                        ================================================
                                     AFRISAV DIGITAL RECEIPT
                        ================================================
                        Receipt No: AFRISAV-TXN-${tx.id}
                        Date & Time: $dateStr
                        Transaction Type: ${tx.type}
                        
                        Description:
                        ${tx.title}
                        ${tx.description}
                        
                        ------------------------------------------------
                        TOTAL AMOUNT PAID: ₦${String.format("%,.2f", tx.amount)}
                        Payment Status: SUCCESSFUL
                        Payment Channel: AfriSav Wallet
                        ------------------------------------------------
                        Thank you for using AfriSav for food-secure 
                        savings and bulk purchases.
                        ================================================
                    """.trimIndent()

                    val resolver = context.contentResolver
                    val contentValues = android.content.ContentValues().apply {
                        put(android.provider.MediaStore.MediaColumns.DISPLAY_NAME, fileName)
                        put(android.provider.MediaStore.MediaColumns.MIME_TYPE, "text/plain")
                        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.Q) {
                            put(android.provider.MediaStore.MediaColumns.RELATIVE_PATH, android.os.Environment.DIRECTORY_DOWNLOADS)
                        }
                    }

                    val uri = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.Q) {
                        resolver.insert(android.provider.MediaStore.Downloads.EXTERNAL_CONTENT_URI, contentValues)
                    } else {
                        null
                    }

                    if (uri != null) {
                        resolver.openOutputStream(uri)?.use { os ->
                            os.write(receiptContent.toByteArray())
                        }
                        "Downloaded receipt successfully to system Downloads!"
                    } else {
                        val downloadsDir = context.getExternalFilesDir(android.os.Environment.DIRECTORY_DOWNLOADS)
                        val file = java.io.File(downloadsDir, fileName)
                        file.writeText(receiptContent)
                        "Downloaded receipt successfully! Saved to: ${file.absolutePath}"
                    }
                } catch (e: Exception) {
                    try {
                        val fileName = "AfriSav_Receipt_${tx.id}.txt"
                        val file = java.io.File(context.filesDir, fileName)
                        file.writeText("AfriSav Receipt #${tx.id}\nAmount: ₦${tx.amount}\nStatus: SUCCESS")
                        "Receipt saved to app storage: ${file.name}"
                    } catch (ex: Exception) {
                        "Error downloading receipt: ${e.localizedMessage}"
                    }
                }
            }
            _uiEvent.emit(result)
        }
    }

    // Rider specific profile states
    private val _riderVehicleType = kotlinx.coroutines.flow.MutableStateFlow("Motorcycle")
    val riderVehicleType: kotlinx.coroutines.flow.StateFlow<String> = _riderVehicleType.asStateFlow()

    private val _riderPlate = kotlinx.coroutines.flow.MutableStateFlow("LA-583-XB")
    val riderPlate: kotlinx.coroutines.flow.StateFlow<String> = _riderPlate.asStateFlow()

    private val _riderPin = kotlinx.coroutines.flow.MutableStateFlow("1234")
    val riderPin: kotlinx.coroutines.flow.StateFlow<String> = _riderPin.asStateFlow()

    fun updateRiderVehicleDetails(vehicleType: String, plate: String) {
        _riderVehicleType.value = vehicleType
        // Plate number is verified and read-only. We ignore the passed parameter and retain the existing plate.
    }

    fun updateRiderPin(newPin: String) {
        _riderPin.value = newPin
    }

    fun getRiderRating(riderName: String?): Double {
        if (riderName.isNullOrEmpty()) return 4.9
        val reviews = allReviews.value.filter { it.targetName.equals(riderName, ignoreCase = true) }
        if (reviews.isEmpty()) return 4.9
        val avg = reviews.map { it.rating }.average()
        return if (avg.isNaN()) 4.9 else avg
    }

    fun getRiderCompletedTrips(riderName: String?): Int {
        if (riderName.isNullOrEmpty()) return 8
        val dbCompleted = allEscrowOrders.value.count { 
            it.riderName.equals(riderName, ignoreCase = true) && it.status == "COMPLETED" 
        }
        return 8 + dbCompleted
    }
}

sealed interface BasketPreviewState {
    object Idle : BasketPreviewState
    object Loading : BasketPreviewState
    data class Success(val description: String) : BasketPreviewState
}

class KoboViewModelFactory(private val application: Application) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(KoboViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return KoboViewModel(application) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}

data class SuccessModalState(
    val isOpen: Boolean = false,
    val type: SuccessType = SuccessType.TRANSACTION,
    val title: String = "",
    val message: String = "",
    val amount: Double? = null,
    val isGoalHit: Boolean = false
)

enum class SuccessType {
    TRANSACTION,
    SAVINGS_GOAL,
    ORDER,
    CIRCLE
}
