package com.autoclick.app

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.appcompat.app.AppCompatDelegate
import com.autoclick.app.database.AppDatabase
import com.autoclick.app.utils.Constants
import com.autoclick.app.utils.PreferenceManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

class AutoClickApplication : Application() {
    // App-wide CoroutineScope
    private val applicationScope = CoroutineScope(SupervisorJob() + Dispatchers.Main)

    // Lazy initialization of database
    private val database by lazy {
        AppDatabase.getDatabase(this)
    }

    // Lazy initialization of PreferenceManager
    private val preferenceManager by lazy {
        PreferenceManager.getInstance(this)
    }

    override fun onCreate() {
        super.onCreate()
        initializeApp()
    }

    private fun initializeApp() {
        applicationScope.launch {
            // Initialize components
            createNotificationChannel()
            setupTheme()
            setupFirstRun()
        }
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                Constants.NOTIFICATION_CHANNEL_ID,
                Constants.NOTIFICATION_CHANNEL_NAME,
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Channel for Auto Click Service notifications"
                setShowBadge(false)
            }

            val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    private fun setupTheme() {
        val themeMode = preferenceManager.themeMode
        if (themeMode != -1) {
            AppCompatDelegate.setDefaultNightMode(themeMode)
        }
    }

    private fun setupFirstRun() {
        if (preferenceManager.isFirstRun) {
            applicationScope.launch(Dispatchers.IO) {
                // Initialize default configuration if needed
                val configCount = database.clickConfigurationDao().getConfigurationCount()
                if (configCount == 0) {
                    createDefaultConfiguration()
                }
                preferenceManager.isFirstRun = false
            }
        }
    }

    private suspend fun createDefaultConfiguration() {
        val configDao = database.clickConfigurationDao()
        val clickPointDao = database.clickPointDao()

        // Create a default configuration
        val configId = configDao.insert(
            ClickConfiguration(
                name = "Default Configuration",
                isActive = false,
                createdAt = System.currentTimeMillis()
            )
        )

        // Create a sample click point
        val centerX = resources.displayMetrics.widthPixels / 2f
        val centerY = resources.displayMetrics.heightPixels / 2f

        clickPointDao.insert(
            ClickPoint(
                name = "Sample Click Point",
                x = centerX,
                y = centerY,
                size = Constants.DEFAULT_BUTTON_SIZE,
                delay = Constants.DEFAULT_DELAY,
                order = 0,
                configurationId = configId
            )
        )

        // Save as last active configuration
        preferenceManager.lastConfigurationId = configId
    }

    companion object {
        @Volatile
        private var instance: AutoClickApplication? = null

        fun getInstance(): AutoClickApplication {
            return instance ?: synchronized(this) {
                instance ?: throw IllegalStateException("Application not initialized")
            }
        }
    }

    override fun attachBaseContext(base: Context) {
        super.attachBaseContext(base)
        instance = this
    }
}
