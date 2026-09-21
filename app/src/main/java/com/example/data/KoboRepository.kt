package com.example.data

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class KoboRepository(private val dao: KoboDao) {
    val walletState: Flow<WalletState?> = dao.getWalletState()
    val savingsGoals: Flow<List<SavingsGoal>> = dao.getSavingsGoals()
    val marketItems: Flow<List<MarketItem>> = dao.getMarketItems()
    val foodCircles: Flow<List<FoodCircle>> = dao.getFoodCircles()
    val transactions: Flow<List<WalletTransaction>> = dao.getTransactions()
    val chatMessages: Flow<List<ChatMessage>> = dao.getChatMessages()


    suspend fun getSavingsGoalById(id: Int): SavingsGoal? = dao.getSavingsGoalById(id)

    suspend fun initializeDatabaseIfEmpty() = withContext(Dispatchers.IO) {
        val wallet = dao.getWalletState().first()
        if (wallet == null) {
            dao.insertWalletState(WalletState())
            
            // Add transactions
            dao.insertTransaction(WalletTransaction(
                type = "FUND",
                amount = 25000.0,
                title = "Wallet Funded",
                description = "Funded wallet via Bank Transfer (Opay)"
            ))
            dao.insertTransaction(WalletTransaction(
                type = "GOAL_SAVE",
                amount = 5000.0,
                title = "Saved to Rice Goal",
                description = "Daily auto-save to 50kg Rice Goal"
            ))
            dao.insertTransaction(WalletTransaction(
                type = "BUY_FOOD",
                amount = 3800.0,
                title = "Yam and Tomatoes Bought",
                description = "Purchased from Mile 12 Grocery Store"
            ))

            // Add savings goals
            dao.insertSavingsGoal(SavingsGoal(
                title = "50kg Bag of Rice Goal",
                targetAmount = 75000.0,
                savedAmount = 32000.0,
                category = "Rice",
                isAutoSaveEnabled = true,
                autoSaveAmount = 500.0,
                estimatedCompletionDate = "30 Days Left"
            ))
            dao.insertSavingsGoal(SavingsGoal(
                title = "Oloyin Beans Supply",
                targetAmount = 30000.0,
                savedAmount = 15000.0,
                category = "Beans",
                isAutoSaveEnabled = true,
                autoSaveAmount = 200.0,
                estimatedCompletionDate = "25 Days Left"
            ))
            dao.insertSavingsGoal(SavingsGoal(
                title = "Monthly Family Basket",
                targetAmount = 120000.0,
                savedAmount = 65000.0,
                category = "Vegetables",
                isAutoSaveEnabled = false,
                autoSaveAmount = 1000.0,
                estimatedCompletionDate = "15 Days Left"
            ))

            // Add food circles
            dao.insertFoodCircle(FoodCircle(
                title = "Mile 12 Bulk Rice Circle",
                description = "Saving together to buy 10 bags of premium rice directly from wholesale imports. Save up to 20% on retail prices!",
                targetAmount = 700000.0,
                currentAmount = 450000.0,
                membersCount = 4,
                imageUrl = "ic_circle_rice",
                circleCode = "KB-M12RICE",
                creatorName = "Adebayo Alao",
                members = "Adebayo Alao, Amina Salami, Papa Joy, Ibrahim Agro",
                pendingRequests = "Chinedu Okafor, Fatima Musa, Emeka J.",
                coAdmins = "Amina Salami",
                isPrivate = true
            ))
            dao.insertFoodCircle(FoodCircle(
                title = "Gbagada Fresh Veggies Circle",
                description = "Splitting baskets of tomatoes, pepper, onions and yams. Bulk prices unlock at ₦100,000!",
                targetAmount = 120000.0,
                currentAmount = 85000.0,
                membersCount = 3,
                imageUrl = "ic_circle_veggies",
                circleCode = "KB-GBGVEG",
                creatorName = "Mama Ejima",
                members = "Mama Ejima, Sister Bose, Adebayo Alao",
                pendingRequests = "Kemi Ade, Ngozi Obi",
                coAdmins = "Sister Bose",
                isPrivate = true
            ))
            dao.insertFoodCircle(FoodCircle(
                title = "Lekki Poultry & Protein Circle",
                description = "Wholesale cold room buy of premium fish and farm chickens to split shipping and delivery costs.",
                targetAmount = 250000.0,
                currentAmount = 180000.0,
                membersCount = 3,
                imageUrl = "ic_circle_protein",
                circleCode = "KB-LEKKIPRO",
                creatorName = "Tunde Williams",
                members = "Tunde Williams, Uncle Dave, Adebayo Alao",
                pendingRequests = "Chioma N.",
                coAdmins = "",
                isPrivate = true
            ))
            dao.insertFoodCircle(FoodCircle(
                title = "Surulere Organic Grain Circle",
                description = "Premium hand-picked brown rice and organic honey directly from farmers in Surulere.",
                targetAmount = 150000.0,
                currentAmount = 20000.0,
                membersCount = 2,
                imageUrl = "ic_circle_custom",
                circleCode = "KB-SURLORGN",
                creatorName = "Chinedu Okafor",
                members = "Chinedu Okafor, Fatima Musa",
                pendingRequests = "Emeka J.",
                coAdmins = "Fatima Musa",
                isPrivate = true
            ))

            dao.insertFoodCircle(FoodCircle(
                title = "Alimosho Wholesale Oil Circle",
                description = "Pooling together to buy 25-liter jerrycans of premium vegetable oil directly from wholesale factories. Save up to 25% on retail prices!",
                targetAmount = 450000.0,
                currentAmount = 120000.0,
                membersCount = 2,
                imageUrl = "ic_circle_oil",
                circleCode = "KB-ALIMOIL",
                creatorName = "Mama Joy",
                members = "Mama Joy, Adebayo Alao",
                pendingRequests = "Fatima Musa",
                coAdmins = "",
                isPrivate = true
            ))

            // Add market items
            dao.insertMarketItems(listOf(
                MarketItem(
                    name = "50kg Mama Gold Rice (Imported)",
                    price = 70800.0,
                    category = "Rice",
                    imageUrl = "ic_food_rice_mama",
                    vendorName = "Alaba Food Hub",
                    distance = 1.2,
                    rating = 4.8,
                    deliveryTimeMinutes = 30,
                    state = "Lagos",
                    isBundle = false,
                    bundleItems = "",
                    allowPortions = true,
                    portionType = "Bag",
                    originalPrice = 75000.0
                ),
                MarketItem(
                    name = "50kg Premium Caprice Rice",
                    price = 72000.0,
                    category = "Rice",
                    imageUrl = "ic_food_rice_cap",
                    vendorName = "Iya Lola Stores",
                    distance = 2.5,
                    rating = 4.5,
                    deliveryTimeMinutes = 45,
                    state = "Ondo",
                    isBundle = false,
                    bundleItems = "",
                    allowPortions = true,
                    portionType = "Bag"
                ),
                MarketItem(
                    name = "50kg Stallion Long Grain Rice",
                    price = 74500.0,
                    category = "Rice",
                    imageUrl = "ic_food_rice_stallion",
                    vendorName = "Gbagada Wholesalers",
                    distance = 3.8,
                    rating = 4.7,
                    deliveryTimeMinutes = 25,
                    state = "Oyo",
                    isBundle = false,
                    bundleItems = "",
                    allowPortions = true,
                    portionType = "Bag"
                ),
                MarketItem(
                    name = "Paint Bucket Oloyin Honey Beans",
                    price = 6500.0,
                    category = "Beans",
                    imageUrl = "ic_food_beans",
                    vendorName = "Mile 12 Grocery",
                    distance = 1.5,
                    rating = 4.3,
                    deliveryTimeMinutes = 35,
                    state = "Kano",
                    isBundle = false,
                    bundleItems = "",
                    allowPortions = true,
                    portionType = "Bag"
                ),
                MarketItem(
                    name = "Premium Cassava Garri (White, 10kg)",
                    price = 3200.0,
                    category = "Garri",
                    imageUrl = "ic_food_garri",
                    vendorName = "Ibadan Farm Direct",
                    distance = 5.0,
                    rating = 4.6,
                    deliveryTimeMinutes = 50,
                    state = "Abuja",
                    isBundle = false,
                    bundleItems = "",
                    allowPortions = true,
                    portionType = "Bag"
                ),
                MarketItem(
                    name = "Fresh Tomatoes Basket (Large)",
                    price = 4500.0,
                    category = "Tomatoes",
                    imageUrl = "ic_food_tomatoes",
                    vendorName = "Fresh Basket Lagos",
                    distance = 0.8,
                    rating = 4.9,
                    deliveryTimeMinutes = 20,
                    state = "Lagos",
                    isBundle = false,
                    bundleItems = "",
                    allowPortions = true,
                    portionType = "Custom"
                ),
                MarketItem(
                    name = "Selected Yam Tubers (3 Large)",
                    price = 5500.0,
                    category = "Yam",
                    imageUrl = "ic_food_yam",
                    vendorName = "Mile 12 Grocery",
                    distance = 1.5,
                    rating = 4.4,
                    deliveryTimeMinutes = 35,
                    state = "Ondo",
                    isBundle = false,
                    bundleItems = "",
                    allowPortions = true,
                    portionType = "Tuber"
                ),
                MarketItem(
                    name = "Fresh Hake Fish (Kote) 1kg",
                    price = 3800.0,
                    category = "Fish",
                    imageUrl = "ic_food_fish",
                    vendorName = "Marina Sea Foods",
                    distance = 4.2,
                    rating = 4.7,
                    deliveryTimeMinutes = 40,
                    state = "Abuja",
                    isBundle = false,
                    bundleItems = "",
                    allowPortions = true,
                    portionType = "Custom"
                ),
                MarketItem(
                    name = "Soko Family Food Basket Bundle",
                    price = 85000.0,
                    category = "Bundle",
                    imageUrl = "ic_food_custom",
                    vendorName = "Soko Wholesale Depot",
                    distance = 2.1,
                    rating = 4.9,
                    deliveryTimeMinutes = 30,
                    state = "Lagos",
                    isBundle = true,
                    bundleItems = "25kg Caprice Rice, 10kg Cassava Garri, Paint Bucket Honey Beans, 1 Crate of Eggs, 3 Large Tubers of Yam",
                    allowPortions = false,
                    portionType = "Custom",
                    originalPrice = 92000.0
                ),
                MarketItem(
                    name = "Premium Poultry Crate (30 Eggs)",
                    price = 4200.0,
                    category = "Fish",
                    imageUrl = "ic_food_custom",
                    vendorName = "Alaba Food Hub",
                    distance = 1.3,
                    rating = 4.7,
                    deliveryTimeMinutes = 15,
                    state = "Lagos",
                    isBundle = false,
                    bundleItems = "",
                    allowPortions = true,
                    portionType = "Crate"
                )
            ))

            // Add welcome message from Mama Olufunke
            dao.insertChatMessage(ChatMessage(
                text = "Hello! I am Mama Olufunke, your personal food savings & price advisor. 🥑 I can help you set food goals, compare market prices, and suggest bulk buy Circles. Ask me anything, like: 'How do I save for 50kg of Rice?' or 'What are the cheapest tomatoes today?'",
                isUser = false
            ))

            // Seed Seller Earning State for "Salami Stores"
            dao.insertSellerEarningState(SellerEarningState(
                vendorName = "Salami Stores",
                availableBalance = 185000.0,
                totalRevenue = 450000.0,
                totalSalesCount = 12
            ))

            // Seed Seller Sales (trade volume by month)
            dao.insertSellerSale(SellerSale(
                vendorName = "Salami Stores",
                itemName = "50kg Mama Gold Rice (Imported)",
                category = "Rice",
                amount = 70800.0,
                month = "January",
                buyerName = "Adebayo Alao"
            ))
            dao.insertSellerSale(SellerSale(
                vendorName = "Salami Stores",
                itemName = "50kg Premium Caprice Rice",
                category = "Rice",
                amount = 72000.0,
                month = "January",
                buyerName = "Adebayo Alao"
            ))
            dao.insertSellerSale(SellerSale(
                vendorName = "Salami Stores",
                itemName = "Paint Bucket Oloyin Honey Beans",
                category = "Beans",
                amount = 6500.0,
                month = "January",
                buyerName = "Amina Musa"
            ))
            dao.insertSellerSale(SellerSale(
                vendorName = "Salami Stores",
                itemName = "Paint Bucket Oloyin Honey Beans",
                category = "Beans",
                amount = 6500.0,
                month = "January",
                buyerName = "Chidi Benson"
            ))
            dao.insertSellerSale(SellerSale(
                vendorName = "Salami Stores",
                itemName = "Paint Bucket Oloyin Honey Beans",
                category = "Beans",
                amount = 6500.0,
                month = "January",
                buyerName = "Fatima Yusuf"
            ))
            dao.insertSellerSale(SellerSale(
                vendorName = "Salami Stores",
                itemName = "Fresh Tomatoes Basket (Large)",
                category = "Tomatoes",
                amount = 4500.0,
                month = "February",
                buyerName = "Adebayo Alao"
            ))
            dao.insertSellerSale(SellerSale(
                vendorName = "Salami Stores",
                itemName = "Fresh Tomatoes Basket (Large)",
                category = "Tomatoes",
                amount = 4500.0,
                month = "February",
                buyerName = "Amina Musa"
            ))
            dao.insertSellerSale(SellerSale(
                vendorName = "Salami Stores",
                itemName = "Selected Yam Tubers (3 Large)",
                category = "Yam",
                amount = 5500.0,
                month = "June",
                buyerName = "Chidi Benson"
            ))
            dao.insertSellerSale(SellerSale(
                vendorName = "Salami Stores",
                itemName = "50kg Mama Gold Rice (Imported)",
                category = "Rice",
                amount = 70800.0,
                month = "June",
                buyerName = "Fatima Yusuf"
            ))

            // Seed Initial reviews for "Salami Stores" (or caprice rice)
            dao.insertReviewRating(ReviewRating(
                reviewerName = "Adebayo Alao",
                targetName = "Salami Stores",
                isTargetSeller = true,
                rating = 5,
                reviewText = "Excellent service! The caprice rice is pure grade, no stone at all. My family really enjoyed it."
            ))
            dao.insertReviewRating(ReviewRating(
                reviewerName = "Amina Musa",
                targetName = "Salami Stores",
                isTargetSeller = true,
                rating = 4,
                reviewText = "Good prices. The honey beans are clean. Highly recommended for bulk buyers!"
            ))
            dao.insertReviewRating(ReviewRating(
                reviewerName = "Salami Stores",
                targetName = "Adebayo Alao",
                isTargetSeller = false,
                rating = 5,
                reviewText = "A wonderful buyer! Trustworthy and made payment on time without any issues."
            ))
        }
    }

    // Methods to interact with data
    suspend fun insertGoal(goal: SavingsGoal) = dao.insertSavingsGoal(goal)
    suspend fun updateGoal(goal: SavingsGoal) = dao.updateSavingsGoal(goal)
    suspend fun deleteGoal(goal: SavingsGoal) = dao.deleteSavingsGoal(goal)

    suspend fun updateWallet(walletState: WalletState) {
        val clamped = walletState.copy(
            availableBalance = maxOf(0.0, walletState.availableBalance),
            savingsBalance = maxOf(0.0, walletState.savingsBalance),
            totalSaved = maxOf(0.0, walletState.totalSaved)
        )
        dao.insertWalletState(clamped)
    }
    suspend fun insertTransaction(transaction: WalletTransaction) = dao.insertTransaction(transaction)

    suspend fun insertChatMessage(message: ChatMessage) = dao.insertChatMessage(message)
    suspend fun clearChat() = dao.clearChatMessages()

    suspend fun insertFoodCircle(circle: FoodCircle) = dao.insertFoodCircle(circle)
    suspend fun updateFoodCircle(circle: FoodCircle) = dao.updateFoodCircle(circle)
    suspend fun insertMarketItem(item: MarketItem) = dao.insertMarketItem(item)
    suspend fun updateMarketItem(item: MarketItem) = dao.updateMarketItem(item)

    // Seller Earnings
    fun getSellerEarningState(vendorName: String): Flow<SellerEarningState?> = dao.getSellerEarningState(vendorName)
    suspend fun insertSellerEarningState(earningState: SellerEarningState) {
        val clamped = earningState.copy(
            availableBalance = maxOf(0.0, earningState.availableBalance),
            totalRevenue = maxOf(0.0, earningState.totalRevenue)
        )
        dao.insertSellerEarningState(clamped)
    }

    // Seller Sales
    fun getSellerSales(vendorName: String): Flow<List<SellerSale>> = dao.getSellerSales(vendorName)
    suspend fun insertSellerSale(sale: SellerSale) = dao.insertSellerSale(sale)

    // Seller Withdrawals
    fun getSellerWithdrawals(vendorName: String): Flow<List<SellerWithdrawal>> = dao.getSellerWithdrawals(vendorName)
    suspend fun insertSellerWithdrawal(withdrawal: SellerWithdrawal) = dao.insertSellerWithdrawal(withdrawal)

    // Reviews & Ratings
    fun getAllReviews(): Flow<List<ReviewRating>> = dao.getAllReviews()
    fun getReviewsForTarget(targetName: String): Flow<List<ReviewRating>> = dao.getReviewsForTarget(targetName)
    suspend fun insertReviewRating(reviewRating: ReviewRating) = dao.insertReviewRating(reviewRating)

    // Escrow Orders
    fun getAllEscrowOrders(): Flow<List<EscrowOrder>> = dao.getAllEscrowOrders()
    fun getEscrowOrdersForBuyer(buyerName: String): Flow<List<EscrowOrder>> = dao.getEscrowOrdersForBuyer(buyerName)
    fun getEscrowOrdersForSeller(vendorName: String): Flow<List<EscrowOrder>> = dao.getEscrowOrdersForSeller(vendorName)
    suspend fun insertEscrowOrder(order: EscrowOrder) = dao.insertEscrowOrder(order)
    suspend fun updateEscrowOrder(order: EscrowOrder) = dao.updateEscrowOrder(order)

    // Admin / Super Admin operations
    suspend fun deleteFoodCircle(circle: FoodCircle) = dao.deleteFoodCircle(circle)
    suspend fun deleteMarketItem(item: MarketItem) = dao.deleteMarketItem(item)
    suspend fun clearAllData() {
        dao.clearWalletState()
        dao.clearSavingsGoals()
        dao.clearMarketItems()
        dao.clearFoodCircles()
        dao.clearTransactions()
        dao.clearEscrowOrders()
    }
}
