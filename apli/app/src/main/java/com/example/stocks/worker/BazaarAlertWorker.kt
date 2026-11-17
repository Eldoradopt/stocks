package com.example.stocks.worker

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.stocks.data.local.db.AppDatabase
import com.example.stocks.data.remote.hypixel.HypixelApiService
import com.example.stocks.ui.components.toSkyCoins
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

class BazaarAlertWorker(
    appContext: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(appContext, workerParams), KoinComponent {

    private val hypixelApi: HypixelApiService by inject()
    private val db: AppDatabase by inject()

    companion object {
        const val CHANNEL_ID = "bazaar_price_alerts"
        const val CHANNEL_NAME = "Bazaar Price Alerts"
    }

    override suspend fun doWork(): Result {
        return try {
            val alerts = db.priceAlertDao().getActiveAlerts()
            if (alerts.isEmpty()) return Result.success()

            val bazaar = hypixelApi.getBazaar()
            val products = bazaar.products

            alerts.forEach { alert ->
                val prod = products[alert.itemTag]?.quickStatus ?: return@forEach
                val currentPrice = prod.sellPrice

                // Check High target
                if (alert.targetHighPrice > 0 && currentPrice >= alert.targetHighPrice) {
                    sendNotification(
                        title = "🚀 Price Alert: ${alert.itemTag.replace('_', ' ')}",
                        message = "Reached ${currentPrice.toSkyCoins()} coins (Target: ${alert.targetHighPrice.toSkyCoins()})"
                    )
                }

                // Check Low target
                if (alert.targetLowPrice > 0 && currentPrice <= alert.targetLowPrice) {
                    sendNotification(
                        title = "⚠️ Price Dip: ${alert.itemTag.replace('_', ' ')}",
                        message = "Dropped to ${currentPrice.toSkyCoins()} coins (Target: ${alert.targetLowPrice.toSkyCoins()})"
                    )
                }
            }

            Result.success()
        } catch (e: Exception) {
            Result.retry()
        }
    }

    private fun sendNotification(title: String, message: String) {
        val manager = applicationContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                CHANNEL_NAME,
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Notifies when SkyBlock Bazaar items hit price targets"
            }
            manager.createNotificationChannel(channel)
        }

        val notification = NotificationCompat.Builder(applicationContext, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle(title)
            .setContentText(message)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .build()

        manager.notify(System.currentTimeMillis().toInt(), notification)
    }
}
