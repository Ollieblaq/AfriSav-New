package com.example.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface KoboDao {
    // Wallet State
    @Query("SELECT * FROM wallet_state WHERE id = 1 LIMIT 1")
    fun getWalletState(): Flow<WalletState?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertWalletState(walletState: WalletState)

    // Savings Goals
    @Query("SELECT * FROM savings_goals ORDER BY id DESC")
    fun getSavingsGoals(): Flow<List<SavingsGoal>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSavingsGoal(goal: SavingsGoal)

    @Update
    suspend fun updateSavingsGoal(goal: SavingsGoal)

    @Delete
    suspend fun deleteSavingsGoal(goal: SavingsGoal)

    @Query("SELECT * FROM savings_goals WHERE id = :id LIMIT 1")
    suspend fun getSavingsGoalById(id: Int): SavingsGoal?

    // Market Items
    @Query("SELECT * FROM market_items ORDER BY id DESC")
    fun getMarketItems(): Flow<List<MarketItem>>

    @Query("SELECT * FROM market_items WHERE category = :category ORDER BY id DESC")
    fun getMarketItemsByCategory(category: String): Flow<List<MarketItem>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMarketItems(items: List<MarketItem>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMarketItem(item: MarketItem)

    @Update
    suspend fun updateMarketItem(item: MarketItem)

    // Food Circles (Community)
    @Query("SELECT * FROM food_circles ORDER BY id DESC")
    fun getFoodCircles(): Flow<List<FoodCircle>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFoodCircle(circle: FoodCircle)

    @Update
    suspend fun updateFoodCircle(circle: FoodCircle)

    // Wallet Transactions
    @Query("SELECT * FROM transactions ORDER BY timestamp DESC")
    fun getTransactions(): Flow<List<WalletTransaction>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTransaction(transaction: WalletTransaction)

    // Chat Messages
    @Query("SELECT * FROM chat_messages ORDER BY timestamp ASC")
    fun getChatMessages(): Flow<List<ChatMessage>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertChatMessage(message: ChatMessage)

    @Query("DELETE FROM chat_messages")
    suspend fun clearChatMessages()

    // Seller Earnings
    @Query("SELECT * FROM seller_earnings WHERE vendorName = :vendorName LIMIT 1")
    fun getSellerEarningState(vendorName: String): Flow<SellerEarningState?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSellerEarningState(earningState: SellerEarningState)

    // Seller Sales (Trade History / Volume)
    @Query("SELECT * FROM seller_sales WHERE vendorName = :vendorName ORDER BY timestamp DESC")
    fun getSellerSales(vendorName: String): Flow<List<SellerSale>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSellerSale(sale: SellerSale)

    // Seller Withdrawals
    @Query("SELECT * FROM seller_withdrawals WHERE vendorName = :vendorName ORDER BY timestamp DESC")
    fun getSellerWithdrawals(vendorName: String): Flow<List<SellerWithdrawal>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSellerWithdrawal(withdrawal: SellerWithdrawal)

    // Reviews & Ratings
    @Query("SELECT * FROM reviews_ratings ORDER BY timestamp DESC")
    fun getAllReviews(): Flow<List<ReviewRating>>

    @Query("SELECT * FROM reviews_ratings WHERE targetName = :targetName ORDER BY timestamp DESC")
    fun getReviewsForTarget(targetName: String): Flow<List<ReviewRating>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertReviewRating(reviewRating: ReviewRating)

    // Escrow Orders
    @Query("SELECT * FROM escrow_orders ORDER BY timestamp DESC")
    fun getAllEscrowOrders(): Flow<List<EscrowOrder>>

    @Query("SELECT * FROM escrow_orders WHERE buyerName = :buyerName ORDER BY timestamp DESC")
    fun getEscrowOrdersForBuyer(buyerName: String): Flow<List<EscrowOrder>>

    @Query("SELECT * FROM escrow_orders WHERE vendorName = :vendorName ORDER BY timestamp DESC")
    fun getEscrowOrdersForSeller(vendorName: String): Flow<List<EscrowOrder>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEscrowOrder(order: EscrowOrder)

    @Update
    suspend fun updateEscrowOrder(order: EscrowOrder)

    @Delete
    suspend fun deleteFoodCircle(circle: FoodCircle)

    @Delete
    suspend fun deleteMarketItem(item: MarketItem)

    @Query("DELETE FROM wallet_state")
    suspend fun clearWalletState()

    @Query("DELETE FROM savings_goals")
    suspend fun clearSavingsGoals()

    @Query("DELETE FROM market_items")
    suspend fun clearMarketItems()

    @Query("DELETE FROM food_circles")
    suspend fun clearFoodCircles()

    @Query("DELETE FROM transactions")
    suspend fun clearTransactions()

    @Query("DELETE FROM escrow_orders")
    suspend fun clearEscrowOrders()

}
