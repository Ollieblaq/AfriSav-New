package com.example.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [
        WalletState::class,
        SavingsGoal::class,
        MarketItem::class,
        FoodCircle::class,
        WalletTransaction::class,
        ChatMessage::class,
        SellerEarningState::class,
        SellerSale::class,
        SellerWithdrawal::class,
        ReviewRating::class,
        EscrowOrder::class
    ],
    version = 20,
    exportSchema = false
)
abstract class AfriSavDatabase : RoomDatabase() {
    abstract fun koboDao(): KoboDao

    companion object {
        @Volatile
        private var INSTANCE: AfriSavDatabase? = null

        fun getDatabase(context: Context): AfriSavDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AfriSavDatabase::class.java,
                    "afrisav_database"
                )
                .fallbackToDestructiveMigration()
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
