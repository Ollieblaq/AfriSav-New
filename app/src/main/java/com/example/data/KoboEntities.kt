package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "wallet_state")
data class WalletState(
    @PrimaryKey val id: Int = 1,
    val availableBalance: Double = 15000.0,
    val savingsBalance: Double = 42000.0,
    val totalSaved: Double = 57000.0,
    val koboContributionGoal: Double = 50000.0,
    val koboGoalFrequency: String = "Daily",
    val currentKoboContribution: Double = 0.0,
    val isLowBalanceAlertEnabled: Boolean = true,
    val lowBalanceThreshold: Double = 5000.0,
    val isAutoMonthlyDepositEnabled: Boolean = false,
    val autoMonthlyDepositAmount: Double = 5000.0,
    val autoDepositFrequency: String = "Monthly"
)

@Entity(tableName = "savings_goals")
data class SavingsGoal(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val title: String,
    val targetAmount: Double,
    val savedAmount: Double,
    val category: String, // Rice, Beans, Garri, Tomatoes, Pepper, Yam, Plantain, Palm Oil, Vegetable Oil, Fish, Meat, Poultry, Fruits, Vegetables
    val isAutoSaveEnabled: Boolean = false,
    val autoSaveAmount: Double = 500.0,
    val isPaused: Boolean = false,
    val estimatedCompletionDate: String = "30 Days Left",
    val isLocked: Boolean = true
)

@Entity(tableName = "market_items")
data class MarketItem(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val price: Double,
    val category: String,
    val imageUrl: String,
    val vendorName: String,
    val distance: Double, // in km
    val rating: Double,
    val deliveryTimeMinutes: Int,
    val ratingCount: Int = 24,
    val state: String = "Lagos",
    val stock: Int = 12,
    val isBundle: Boolean = false,
    val bundleItems: String = "",
    val allowPortions: Boolean = true,
    val portionType: String = "Bag", // "Bag", "Crate", "Tuber", "Custom"
    val originalPrice: Double? = null
)

@Entity(tableName = "food_circles")
data class FoodCircle(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val title: String,
    val description: String,
    val targetAmount: Double,
    val currentAmount: Double,
    val membersCount: Int,
    val isWholesaleUnlocked: Boolean = false,
    val imageUrl: String,
    val circleCode: String = "",
    val creatorName: String = "",
    val members: String = "",
    val pendingRequests: String = "",
    val coAdmins: String = "",
    val isPrivate: Boolean = true
)

@Entity(tableName = "transactions")
data class WalletTransaction(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val type: String, // FUND, WITHDRAW, GOAL_SAVE, BUY_FOOD, TRANSFER
    val amount: Double,
    val title: String,
    val description: String,
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "chat_messages")
data class ChatMessage(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val text: String,
    val isUser: Boolean,
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "seller_earnings")
data class SellerEarningState(
    @PrimaryKey val vendorName: String,
    val availableBalance: Double = 0.0,
    val totalRevenue: Double = 0.0,
    val totalSalesCount: Int = 0
)

@Entity(tableName = "seller_sales")
data class SellerSale(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val vendorName: String,
    val itemName: String,
    val category: String,
    val amount: Double,
    val month: String, // e.g., "January", "February", "June"
    val timestamp: Long = System.currentTimeMillis(),
    val buyerName: String = "Adebayo Alao"
)

@Entity(tableName = "seller_withdrawals")
data class SellerWithdrawal(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val vendorName: String,
    val amount: Double,
    val bankName: String,
    val accountNumber: String,
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "reviews_ratings")
data class ReviewRating(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val reviewerName: String,
    val targetName: String, // Vendor name, product name, or buyer name
    val isTargetSeller: Boolean, // true if reviewing a seller/product, false if reviewing a buyer
    val rating: Int, // 1 to 5 stars
    val reviewText: String,
    val timestamp: Long = System.currentTimeMillis(),
    val orderId: Int = 0
)

@Entity(tableName = "escrow_orders")
data class EscrowOrder(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val buyerName: String,
    val vendorName: String,
    val itemName: String,
    val amount: Double,
    val status: String, // "PENDING", "COMPLETED", "DISPUTED"
    val pickupPin: String = "",
    val deliveryPin: String = "",
    val isPickedUp: Boolean = false,
    val riderName: String? = null,
    val salesFee: Double = 0.0,
    val serviceCharge: Double = 50.0,
    val deliveryFee: Double = 950.0,
    val baseDeliveryFee: Double = 950.0,
    val riderProposedFee: Double = 0.0,
    val riderProposalName: String? = null,
    val deliveryStatus: String = "CONFIRMED", // "CONFIRMED", "PREPARING", "READY_FOR_PICKUP", "IN_TRANSIT", "ARRIVED", "DELIVERED"
    val riderPhone: String = "08031234567",
    val riderPlate: String = "LA-583-XB",
    val currentEtaMinutes: Int = 25,
    val timestamp: Long = System.currentTimeMillis()
)




