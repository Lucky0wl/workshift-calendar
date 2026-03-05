package com.example.workshiftcalendar

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.work.*
import com.example.workshiftcalendar.domain.model.ShiftKind
import com.example.workshiftcalendar.domain.model.ShiftDetails
import java.time.LocalDate
import java.util.concurrent.TimeUnit

/**
 * Планировщик уведомлений с использованием WorkManager
 */
object NotificationScheduler {

    private const val WORK_NAME = "workshift_daily_notification"
    private const val NOTIFICATION_CHANNEL_ID = "workshift_reminders"

    /**
     * Запланировать ежедневное уведомление
     */
    fun scheduleNotification(context: Context, hour: Int, minute: Int) {
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.NOT_REQUIRED)
            .build()

        val notificationWork = PeriodicWorkRequestBuilder<NotificationWorker>(1, TimeUnit.DAYS)
            .setInitialDelay(calculateInitialDelay(hour, minute), TimeUnit.MILLISECONDS)
            .setConstraints(constraints)
            .addTag(WORK_NAME)
            .setInputData(
                Data.Builder()
                    .putInt("hour", hour)
                    .putInt("minute", minute)
                    .build()
            )
            .build()

        WorkManager.getInstance(context).enqueueUniquePeriodicWork(
            WORK_NAME,
            ExistingPeriodicWorkPolicy.REPLACE,
            notificationWork
        )
    }

    /**
     * Отменить уведомление
     */
    fun cancelNotification(context: Context) {
        WorkManager.getInstance(context).cancelUniqueWork(WORK_NAME)
    }

    /**
     * Рассчитать начальную задержку до следующего уведомления
     */
    private fun calculateInitialDelay(hour: Int, minute: Int): Long {
        val now = System.currentTimeMillis()
        val calendar = java.util.Calendar.getInstance().apply {
            timeInMillis = now
            set(java.util.Calendar.HOUR_OF_DAY, hour)
            set(java.util.Calendar.MINUTE, minute)
            set(java.util.Calendar.SECOND, 0)
            set(java.util.Calendar.MILLISECOND, 0)
        }

        var targetTime = calendar.timeInMillis
        if (targetTime <= now) {
            targetTime += TimeUnit.DAYS.toMillis(1)
        }

        return targetTime - now
    }

    /**
     * Показать уведомление о смене
     */
    fun showShiftNotification(context: Context, shiftKind: ShiftKind, date: LocalDate) {
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as android.app.NotificationManager

        // Создаём канал для Android O+
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = android.app.NotificationChannel(
                NOTIFICATION_CHANNEL_ID,
                "Напоминания о сменах",
                android.app.NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "Ежедневные напоминания о рабочих сменах"
                enableLights(true)
                enableVibration(true)
            }
            notificationManager.createNotificationChannel(channel)
        }

        // Intent для открытия приложения
        val openAppIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent = PendingIntent.getActivity(
            context,
            0,
            openAppIntent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        // Строим уведомление
        val notification = androidx.core.app.NotificationCompat.Builder(context, NOTIFICATION_CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_menu_today)
            .setContentTitle("Рабочая смена завтра ${shiftKind.emoji}")
            .setContentText("Напоминание: Завтра у вас смена «${shiftKind.displayName}»")
            .setPriority(androidx.core.app.NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()

        notificationManager.notify(System.currentTimeMillis().toInt(), notification)
    }
}

/**
 * Worker для выполнения ежедневных уведомлений
 */
class NotificationWorker(
    private val appContext: Context,
    params: WorkerParameters
) : CoroutineWorker(appContext, params) {

    override suspend fun doWork(): Result {
        return try {
            val repository = createRepository()
            val tomorrow = LocalDate.now().plusDays(1)
            val tomorrowShift = repository.getShift(tomorrow)

            if (tomorrowShift != null && tomorrowShift.kind != ShiftKind.OFF) {
                NotificationScheduler.showShiftNotification(appContext, tomorrowShift.kind, tomorrow)
            }

            Result.success()
        } catch (e: Exception) {
            Result.retry()
        }
    }

    private fun createRepository(): com.example.workshiftcalendar.data.repository.WorkshiftRepository {
        val context = appContext
        val gson = com.google.gson.Gson()
        val localDataSource = com.example.workshiftcalendar.data.local.WorkshiftLocalDataSource(context, gson)
        return com.example.workshiftcalendar.data.repository.WorkshiftRepository(localDataSource)
    }
}
