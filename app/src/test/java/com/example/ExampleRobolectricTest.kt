package com.example

import android.app.Application
import android.content.Context
import androidx.lifecycle.viewModelScope
import androidx.test.core.app.ApplicationProvider
import com.example.ui.KoboViewModel
import com.example.ui.SuccessType
import com.example.data.ChatMessage
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.withTimeoutOrNull
import kotlinx.coroutines.flow.first
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

  @Test
  fun `read string from context`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val appName = context.getString(R.string.app_name)
    assertEquals("AfriSav", appName)
  }

  @Test
  fun `test success modal flow`() {
    val application = ApplicationProvider.getApplicationContext<Application>()
    val viewModel = KoboViewModel(application)
    
    // Initial state should be null (closed)
    assertNull(viewModel.successModalState.value)

    // Open success modal
    viewModel.showSuccessModal(
      type = SuccessType.SAVINGS_GOAL,
      title = "Savings Goal Achieved! 🎉",
      message = "Congratulations! You saved ₦10,000.00!",
      amount = 10000.0,
      isGoalHit = true
    )

    val state = viewModel.successModalState.value
    assertNotNull(state)
    assertEquals(SuccessType.SAVINGS_GOAL, state?.type)
    assertEquals("Savings Goal Achieved! 🎉", state?.title)
    assertEquals("Congratulations! You saved ₦10,000.00!", state?.message)
    assertEquals(10000.0, state?.amount)
    assertEquals(true, state?.isGoalHit)

    // Dismiss modal
    viewModel.dismissSuccessModal()
    assertNull(viewModel.successModalState.value)
  }

  @Test
  fun `test speech helper instantiation and availability check`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    var errorReceived = ""
    val helper = com.example.ui.SpeechRecognizerHelper(
      context = context,
      onStart = {},
      onRmsChanged = {},
      onPartialResult = {},
      onResult = {},
      onError = { errorReceived = it },
      onEndOfSpeech = {}
    )
    
    // Starting to listen should report that speech recognition is not available in headless Robolectric JVM
    helper.startListening()
    assertEquals("Speech recognition is not available on this device.", errorReceived)
  }

  @Test
  fun `test custom food basket preview state flow`() {
    val application = ApplicationProvider.getApplicationContext<Application>()
    val viewModel = KoboViewModel(application)
    
    // Initial state should be Idle
    assertEquals(com.example.ui.BasketPreviewState.Idle, viewModel.basketPreviewState.value)
    
    // Trigger preview generation with an empty list
    viewModel.generateBasketPreview(emptyList())
    
    // Since it launches a coroutine, the intermediate state is Loading
    assertEquals(com.example.ui.BasketPreviewState.Loading, viewModel.basketPreviewState.value)
    
    // Reset state should bring it back to Idle
    viewModel.resetBasketPreview()
    assertEquals(com.example.ui.BasketPreviewState.Idle, viewModel.basketPreviewState.value)
  }

  @Test
  fun `test login user and logout user updates state flows`() {
    val application = ApplicationProvider.getApplicationContext<Application>()
    val viewModel = KoboViewModel(application)
    
    // Initial guest state
    assertEquals("Buyer", viewModel.userRole.value)
    assertEquals("Adebayo Alao", viewModel.currentUserName.value)
    
    // Perform login
    viewModel.loginUser("08012345678", "Chidi Okafor", "Seller")
    assertEquals("Seller", viewModel.userRole.value)
    assertEquals("Chidi Okafor", viewModel.currentUserName.value)
    
    // Perform logout
    viewModel.logoutUser()
    assertEquals("Buyer", viewModel.userRole.value)
    assertEquals("Guest", viewModel.currentUserName.value)
  }

  private fun awaitWalletState(viewModel: KoboViewModel, condition: (com.example.data.WalletState) -> Boolean): com.example.data.WalletState {
    var result: com.example.data.WalletState? = null
    val job = viewModel.viewModelScope.launch {
      viewModel.walletState.collect {
        if (it != null && condition(it)) {
          result = it
        }
      }
    }
    
    for (i in 1..20) {
      org.robolectric.shadows.ShadowLooper.idleMainLooper()
      if (result != null) break
      Thread.sleep(50)
    }
    
    job.cancel()
    return result ?: com.example.data.WalletState()
  }

  @Test
  fun `test smart wallet contribution goals`() {
    val application = ApplicationProvider.getApplicationContext<Application>()
    
    // Seed database synchronously before VM instantiation
    val db = com.example.data.AfriSavDatabase.getDatabase(application)
    val dao = db.koboDao()
    runBlocking {
      val existing = dao.getWalletState().first()
      if (existing == null) {
        dao.insertWalletState(com.example.data.WalletState(
          availableBalance = 15000.0,
          savingsBalance = 42000.0,
          totalSaved = 57000.0
        ))
      }
    }

    val viewModel = KoboViewModel(application)

    // Keep a persistent subscriber to walletState so WhileSubscribed stays active
    val keepAliveJob = viewModel.viewModelScope.launch {
      viewModel.walletState.collect { }
    }

    // 1. Initially wallet should exist after seeding
    val wallet = awaitWalletState(viewModel) { true }
    assertNotNull(wallet)
    
    val initialAvailable = wallet.availableBalance
    val initialSavings = wallet.savingsBalance
    val initialTotal = wallet.totalSaved

    // 2. Update goal
    viewModel.updateKoboContributionGoal(60000.0, "Weekly")
    val updatedWallet = awaitWalletState(viewModel) { it.koboContributionGoal == 60000.0 }
    assertEquals(60000.0, updatedWallet.koboContributionGoal, 0.01)
    assertEquals("Weekly", updatedWallet.koboGoalFrequency)

    // 3. Save Naira towards goal
    // Contribute 10,000 Naira
    viewModel.contributeKobo(10000.0)
    val contributedWallet = awaitWalletState(viewModel) { it.currentKoboContribution == 10000.0 }
    assertEquals(initialAvailable - 10000.0, contributedWallet.availableBalance, 0.01)
    assertEquals(initialSavings + 10000.0, contributedWallet.savingsBalance, 0.01)
    assertEquals(initialTotal + 10000.0, contributedWallet.totalSaved, 0.01)
    assertEquals(10000.0, contributedWallet.currentKoboContribution, 0.01)

    // 4. Reset cycle
    viewModel.resetKoboContribution()
    val resetWallet = awaitWalletState(viewModel) { it.currentKoboContribution == 0.0 }
    assertEquals(0.0, resetWallet.currentKoboContribution, 0.01)

    keepAliveJob.cancel()
  }

  private fun awaitChatMessages(viewModel: KoboViewModel, condition: (List<ChatMessage>) -> Boolean): List<ChatMessage> {
    var result: List<ChatMessage>? = null
    val job = viewModel.viewModelScope.launch {
      viewModel.chatMessages.collect {
        if (condition(it)) {
          result = it
        }
      }
    }
    
    for (i in 1..30) {
      org.robolectric.shadows.ShadowLooper.idleMainLooper()
      if (result != null) break
      Thread.sleep(50)
    }
    
    job.cancel()
    return result ?: viewModel.chatMessages.value
  }

  @Test
  fun `test mama ai assistant chat flow`() {
    val application = ApplicationProvider.getApplicationContext<Application>()
    val viewModel = KoboViewModel(application)
    
    // Clear chat initially should seed the default greeting
    viewModel.clearChat()
    
    // Await chat size 1 and the cleared message text
    val msgs = awaitChatMessages(viewModel) { it.size == 1 && !it.first().isUser }
    assertNotNull(msgs)
    assertEquals(1, msgs.size)
    assertEquals(false, msgs.first().isUser)
    assertEquals("My pikin! I have cleared our old chat. Anything you want to ask me about food prices, savings, or wholesale bulk buying, just ask me again!", msgs.first().text)

    // Test sending a query about Rice
    viewModel.sendMessageToMama("How much is 50kg rice?")
    
    // Await chat size 3 (Greeting + User query + Mama response)
    val updatedMsgs = awaitChatMessages(viewModel) { it.size == 3 }
    assertEquals(3, updatedMsgs.size)
    assertEquals("How much is 50kg rice?", updatedMsgs[1].text)
    assertEquals(true, updatedMsgs[1].isUser)
    
    // Verify fallback response for rice
    assertEquals(false, updatedMsgs[2].isUser)
    assert(updatedMsgs[2].text.contains("Ah, Rice! Rice is gold now, my pikin!"))
  }

  @Test
  fun `test community bulk buying circles seeding and joining`() {
    val application = ApplicationProvider.getApplicationContext<Application>()
    val db = com.example.data.AfriSavDatabase.getDatabase(application)
    val dao = db.koboDao()
    
    // Synchronously clear and seed the database first
    runBlocking(kotlinx.coroutines.Dispatchers.IO) {
      db.clearAllTables()
      dao.insertWalletState(com.example.data.WalletState(
        availableBalance = 15000.0,
        savingsBalance = 42000.0,
        totalSaved = 57000.0
      ))
      dao.insertFoodCircle(com.example.data.FoodCircle(
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
        isPrivate = false
      ))
    }
    
    val viewModel = KoboViewModel(application)
    
    // Activate the StateFlow by subscribing to it (needed for WhileSubscribed SharingStarted flows)
    val collectJob = viewModel.viewModelScope.launch {
      viewModel.foodCircles.collect {}
    }
    
    // Perform setup / looper idling so database loads complete
    org.robolectric.shadows.ShadowLooper.idleMainLooper()
    
    // Fetch the list of circles (wait for Room to query and emit)
    var circles: List<com.example.data.FoodCircle> = emptyList()
    for (i in 1..40) {
      org.robolectric.shadows.ShadowLooper.idleMainLooper()
      circles = viewModel.foodCircles.value
      if (circles.isNotEmpty()) {
        break
      }
      Thread.sleep(50)
    }
    
    // Assert that the seeded public Alimosho Wholesale Oil Circle exists
    val oilCircle = circles.find { it.circleCode == "KB-ALIMOIL" }
    assertNotNull(oilCircle)
    assertEquals("Alimosho Wholesale Oil Circle", oilCircle?.title)
    assertEquals(false, oilCircle?.isPrivate)
    
    // Test joining this community pool
    // Change user to a non-member so we can join (Adebayo Alao is already a member, so let's log in as a guest/different user)
    viewModel.loginUser("08099999999", "Chidimma Egwu", "Buyer")
    org.robolectric.shadows.ShadowLooper.idleMainLooper()
    
    // Join circle
    viewModel.joinCircle("KB-ALIMOIL")
    
    // Wait for the asynchronous database update and subsequent StateFlow collection to run
    var updatedOilCircle: com.example.data.FoodCircle? = null
    for (i in 1..40) {
      org.robolectric.shadows.ShadowLooper.idleMainLooper()
      val list = viewModel.foodCircles.value
      val circle = list.find { it.circleCode == "KB-ALIMOIL" }
      if (circle != null && circle.pendingRequests.contains("Chidimma Egwu")) {
        updatedOilCircle = circle
        break
      }
      Thread.sleep(50)
    }
    
    // Verify updated circles list
    assertNotNull("Oil circle update did not propagate in time", updatedOilCircle)
    assert(updatedOilCircle?.pendingRequests?.contains("Chidimma Egwu") == true)
    
    collectJob.cancel()
  }

  @Test
  fun `test transaction history dashboard integration`() {
    val application = ApplicationProvider.getApplicationContext<Application>()
    val db = com.example.data.AfriSavDatabase.getDatabase(application)
    val dao = db.koboDao()
    
    // Clear and seed transactions database
    runBlocking(kotlinx.coroutines.Dispatchers.IO) {
      db.clearAllTables()
      dao.insertWalletState(com.example.data.WalletState())
      
      dao.insertTransaction(com.example.data.WalletTransaction(
        type = "FUND",
        amount = 12500.0,
        title = "Seeded Deposit",
        description = "Initial deposit to wallet"
      ))
      dao.insertTransaction(com.example.data.WalletTransaction(
        type = "BUY_FOOD",
        amount = 4500.0,
        title = "Seeded Purchase",
        description = "Yam buy at market"
      ))
    }
    
    val viewModel = KoboViewModel(application)
    
    // Start active collector
    val collectJob = viewModel.viewModelScope.launch {
      viewModel.transactions.collect {}
    }
    
    org.robolectric.shadows.ShadowLooper.idleMainLooper()
    
    // Fetch and check active transactions flow
    var recentTxs: List<com.example.data.WalletTransaction> = emptyList()
    for (i in 1..40) {
      org.robolectric.shadows.ShadowLooper.idleMainLooper()
      recentTxs = viewModel.transactions.value
      if (recentTxs.isNotEmpty()) {
        break
      }
      Thread.sleep(50)
    }
    
    assert(recentTxs.isNotEmpty())
    val fundTx = recentTxs.find { it.type == "FUND" }
    val buyTx = recentTxs.find { it.type == "BUY_FOOD" }
    
    assertNotNull("FUND transaction should be seeded and queried", fundTx)
    assertNotNull("BUY_FOOD transaction should be seeded and queried", buyTx)
    assertEquals(12500.0, fundTx?.amount)
    assertEquals(4500.0, buyTx?.amount)
    
    collectJob.cancel()
  }

  @Test
  fun `test logistics partner rider management and finance`() {
    val application = ApplicationProvider.getApplicationContext<Application>()
    val viewModel = KoboViewModel(application)

    // Initial company riders
    val initialSize = viewModel.companyRiders.value.size
    assert(initialSize > 0)

    val initialTotalBalance = viewModel.companyRiders.value.sumOf { it.currentBalance }

    // Add a new rider
    viewModel.addCompanyRider("Ibrahim Musa", "09011223344", 5000.0)
    var updatedList = viewModel.companyRiders.value
    assertEquals(initialSize + 1, updatedList.size)
    val ibrahim = updatedList.find { it.name == "Ibrahim Musa" }
    assertNotNull(ibrahim)
    assertEquals("09011223344", ibrahim?.phone)
    assertEquals(5000.0, ibrahim?.currentBalance ?: 0.0, 0.01)
    assertEquals("Online", ibrahim?.status)

    val newTotalBalance = viewModel.companyRiders.value.sumOf { it.currentBalance }
    assertEquals(initialTotalBalance + 5000.0, newTotalBalance, 0.01)

    // Update rider status
    viewModel.updateCompanyRiderStatus(ibrahim!!.id, "In Transit")
    updatedList = viewModel.companyRiders.value
    val ibrahimUpdated = updatedList.find { it.id == ibrahim.id }
    assertEquals("In Transit", ibrahimUpdated?.status)

    // Bulk company withdrawal
    val withdrawSuccess = viewModel.withdrawCompanyBulk(3000.0, "Kuda Bank", "0123456789")
    assert(withdrawSuccess)
    
    val finalTotalBalance = viewModel.companyRiders.value.sumOf { it.currentBalance }
    assertEquals(newTotalBalance - 3000.0, finalTotalBalance, 0.01)

    // Remove rider
    viewModel.removeCompanyRider(ibrahim.id)
    updatedList = viewModel.companyRiders.value
    assertEquals(initialSize, updatedList.size)
    assertNull(updatedList.find { it.id == ibrahim.id })
  }
}
