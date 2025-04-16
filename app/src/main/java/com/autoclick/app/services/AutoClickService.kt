package com.autoclick.app.services

import android.app.*
import android.content.Intent
import android.os.IBinder
import android.os.Build
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import androidx.core.app.NotificationCompat
import com.autoclick.app.database.AppDatabase
import com.autoclick.app.models.ClickConfiguration
import kotlinx.coroutines.*
import java.util.*
import android.util.Log

class AutoClickService : Service() {
    private val serviceScope = CoroutineScope(Dispatchers.Default + Job())
    private lateinit var database: AppDatabase
    private var isRunning = false
    
    companion object {
        private const val NOTIFICATION_ID = 1
        private const val CHANNEL_ID = "AutoClickService"
        private const val CHANNEL_NAME = "Auto Click Service"
        private const val TAG = "AutoClickService"
    }

    override fun onCreate() {
        super.onCreate()
        database = AppDatabase.getDatabase(this)
        createNotificationChannel()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            "START" -> startAutoClick()
            "STOP" -> stopAutoClick()
        }
        return START_NOT_STICKY
    }

    private fun startAutoClick() {
        if (isRunning) return
        isRunning = true
        
        val notification = createNotification("Auto Click Service is running")
        startForeground(NOTIFICATION_ID, notification)

        serviceScope.launch {
            try {
                processActiveConfigurations()
            } catch (e: Exception) {
                Log.e(TAG, "Error in auto click process", e)
                stopAutoClick()
            }
        }
    }

    private suspend fun processActiveConfigurations() {
        database.clickConfigurationDao().getActiveConfigurations().collect { configurations ->
            configurations.forEach { config ->
                if (isRunning && shouldExecuteConfiguration(config)) {
                    executeConfiguration(config)
                }
            }
        }
    }

    private fun shouldExecuteConfiguration(config: ClickConfiguration): Boolean {
        val currentTime = System.currentTimeMillis()
        return when {
            !config.isActive -> false
            config.startTime != null && currentTime < config.startTime -> false
            config.endTime != null && currentTime > config.endTime -> false
            else -> true
        }
    }

    private suspend fun executeConfiguration(config: ClickConfiguration) {
        val clickPoints = database.clickPointDao().getClickPointsForConfiguration(config.id).first()
        
        var repeatCount = 0
        while (isRunning && (config.repeatCount == 0 || repeatCount < config.repeatCount)) {
            clickPoints.forEach { clickPoint ->
                if (!isRunning) return
                
                // Perform click through accessibility service
                val intent = Intent(this, AutoClickAccessibilityService::class.java).apply {
                    action = "PERFORM_CLICK"
                    putExtra("x", clickPoint.x)
                    putExtra("y", clickPoint.y)
                }
                sendBroadcast(intent)

                // Wait for the specified delay
                delay(clickPoint.delay)
            }
            
            // Global delay between sequences
            if (config.globalDelay > 0) {
                delay(config.globalDelay)
            }
            
            if (config.repeatCount > 0) {
                repeatCount++
            }
        }
    }

    private fun stopAutoClick() {
        isRunning = false
        serviceScope.cancel()
        stopForeground(STOP_FOREGROUND_REMOVE)
        stopSelf()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                CHANNEL_NAME,
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Auto Click Service Channel"
            }
            
            val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    private fun createNotification(message: String): Notification {
        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("Auto Click Service")
            .setContentText(message)
            .setSmallIcon(android.R.drawable.ic_menu_click)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .build()
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onDestroy() {
        super.onDestroy()
        serviceScope.cancel()
    }
}
