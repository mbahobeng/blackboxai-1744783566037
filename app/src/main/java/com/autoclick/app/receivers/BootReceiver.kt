package com.autoclick.app.receivers

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.provider.Settings
import com.autoclick.app.services.AutoClickService
import com.autoclick.app.utils.PreferenceManager
import com.autoclick.app.utils.Utils
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BOOT_COMPLETED) {
            val preferenceManager = PreferenceManager.getInstance(context)
            
            // Check if service was running before device shutdown
            if (preferenceManager.isServiceRunning) {
                CoroutineScope(Dispatchers.Main).launch {
                    // Check required permissions
                    if (checkPermissions(context)) {
                        // Start the service
                        startAutoClickService(context)
                    } else {
                        // Reset service running state if permissions are not granted
                        preferenceManager.isServiceRunning = false
                    }
                }
            }
        }
    }

    private fun checkPermissions(context: Context): Boolean {
        // Check overlay permission
        if (!Settings.canDrawOverlays(context)) {
            return false
        }

        // Check accessibility service
        val serviceName = context.packageName + "/com.autoclick.app.services.AutoClickAccessibilityService"
        if (!Utils.isAccessibilityServiceEnabled(context, serviceName)) {
            return false
        }

        return true
    }

    private fun startAutoClickService(context: Context) {
        val serviceIntent = Intent(context, AutoClickService::class.java).apply {
            action = "START"
        }
        
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            context.startForegroundService(serviceIntent)
        } else {
            context.startService(serviceIntent)
        }
    }
}
