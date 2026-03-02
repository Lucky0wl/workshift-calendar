package com.example.workshiftcalendar

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.time.LocalDate

class NotificationReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        // Handle boot/update events to reschedule the daily alarm
        if (intent.action == Intent.ACTION_BOOT_COMPLETED || 
            intent.action == Intent.ACTION_MY_PACKAGE_REPLACED ||
            intent.action == Intent.ACTION_TIME_SET ||
            intent.action == Intent.ACTION_TIMEZONE_CHANGED) {
            
            // Re-schedule alarm if notifications are enabled
            CoroutineScope(Dispatchers.IO).launch {
                val repository = WorkshiftRepository(context)
                val isEnabled = repository.isNotificationsEnabled()
                if (isEnabled) {
                    val timeParts = repository.getNotificationTime().split(":")
                    if (timeParts.size == 2) {
                        scheduleDailyAlarm(context, timeParts[0].toInt(), timeParts[1].toInt())
                    }
                }
            }
            return
        }

        // Handle the actual alarm trigger
        CoroutineScope(Dispatchers.IO).launch {
            val repository = WorkshiftRepository(context)
            if (!repository.isNotificationsEnabled()) return@launch

            val tomorrow = LocalDate.now().plusDays(1)
            val tomorrowShift = repository.getShift(tomorrow)
            
            if (tomorrowShift != null && tomorrowShift.type != ShiftType.NONE) {
                showNotification(context, tomorrowShift.type.displayName, tomorrowShift.type.emoji)
            }
        }
    }

    private fun showNotification(context: Context, shiftName: String, emoji: String) {
        val channelId = "workshift_reminders"
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        // Create notification channel for Android O and above
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "Напоминания о сменах",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "Ежедневные напоминания о рабочих сменах"
            }
            notificationManager.createNotificationChannel(channel)
        }

        // Intent to open the app when tapped
        val openAppIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent = PendingIntent.getActivity(
            context,
            0,
            openAppIntent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        // Build the notification
        val builder = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(R.drawable.ic_launcher_foreground) // Use a proper small silhouette icon in a real app
            .setContentTitle("Рабочая смена завтра $emoji")
            .setContentText("Напоминание: Завтра у вас смена «$shiftName»")
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)

        // Show notification (ID 1 ensures it updates the same notification instead of spamming)
        notificationManager.notify(1, builder.build())
    }
}
