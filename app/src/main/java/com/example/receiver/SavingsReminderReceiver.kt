package com.example.receiver

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import com.example.MainActivity

class SavingsReminderReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val channelId = "savings_daily_reminders"

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "Daily Savings Reminders",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Daily reminder notifications to keep you on track with your food savings"
            }
            notificationManager.createNotificationChannel(channel)
        }

        val mainIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent = PendingIntent.getActivity(
            context,
            0,
            mainIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // Select a random warm motivational message from Mama Olufunke
        val tips = listOf(
            "Pikin, have you saved some Naira towards your wholesale bulk food goals today? Small-small, basket go full! 🌾",
            "Slow and steady we build our granary! Keep your food savings habit glowing today! 💰",
            "Naira is volatile, but food retains its absolute value! Put a little aside towards your food goals. 😊",
            "Mama Olufunke is checking on you: did you save for your next bag of rice or beans today? 🌾",
            "Protect your family from food inflation! Save a little cash towards your wholesale food circle today. 🤝"
        )
        val selectedMessage = tips.random()

        val notification = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle("Mama Olufunke's Savings Daily Reminder 🌾")
            .setContentText(selectedMessage)
            .setStyle(NotificationCompat.BigTextStyle().bigText(selectedMessage))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_REMINDER)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()

        notificationManager.notify(1001, notification)
    }
}
