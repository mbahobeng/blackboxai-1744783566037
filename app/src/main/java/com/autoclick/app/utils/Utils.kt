package com.autoclick.app.utils

import android.content.Context
import android.content.res.Resources
import android.os.Build
import android.provider.Settings
import android.util.TypedValue
import android.view.WindowManager
import java.text.SimpleDateFormat
import java.util.*

object Utils {
    /**
     * Convert dp to pixels
     */
    fun dpToPx(dp: Float): Float {
        return TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP,
            dp,
            Resources.getSystem().displayMetrics
        )
    }

    /**
     * Convert pixels to dp
     */
    fun pxToDp(px: Float): Float {
        return px / Resources.getSystem().displayMetrics.density
    }

    /**
     * Get screen dimensions
     */
    fun getScreenDimensions(context: Context): Pair<Int, Int> {
        val windowManager = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
        val metrics = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            val bounds = windowManager.currentWindowMetrics.bounds
            Pair(bounds.width(), bounds.height())
        } else {
            @Suppress("DEPRECATION")
            val display = windowManager.defaultDisplay
            val metrics = android.util.DisplayMetrics()
            @Suppress("DEPRECATION")
            display.getMetrics(metrics)
            Pair(metrics.widthPixels, metrics.heightPixels)
        }
        return metrics
    }

    /**
     * Check if accessibility service is enabled
     */
    fun isAccessibilityServiceEnabled(context: Context, serviceName: String): Boolean {
        val enabledServices = Settings.Secure.getString(
            context.contentResolver,
            Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES
        )
        return enabledServices?.contains(serviceName) == true
    }

    /**
     * Format time in milliseconds to HH:mm format
     */
    fun formatTime(timeInMillis: Long): String {
        return SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date(timeInMillis))
    }

    /**
     * Parse time string in HH:mm format to milliseconds
     */
    fun parseTime(timeString: String): Long? {
        return try {
            SimpleDateFormat("HH:mm", Locale.getDefault()).parse(timeString)?.time
        } catch (e: Exception) {
            null
        }
    }

    /**
     * Validate coordinates are within screen bounds
     */
    fun areCoordinatesValid(context: Context, x: Float, y: Float): Boolean {
        val (screenWidth, screenHeight) = getScreenDimensions(context)
        return x >= 0 && x <= screenWidth && y >= 0 && y <= screenHeight
    }

    /**
     * Get current time in milliseconds with only hours and minutes
     */
    fun getCurrentTimeInMillis(): Long {
        val calendar = Calendar.getInstance()
        calendar.apply {
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        return calendar.timeInMillis
    }

    /**
     * Check if the current time is within the given range
     */
    fun isTimeInRange(currentTime: Long, startTime: Long?, endTime: Long?): Boolean {
        if (startTime == null || endTime == null) return true
        return currentTime in startTime..endTime
    }

    /**
     * Calculate delay between two times in milliseconds
     */
    fun calculateDelay(time1: Long, time2: Long): Long {
        return kotlin.math.abs(time2 - time1)
    }

    /**
     * Validate size is within bounds
     */
    fun isValidSize(size: Float): Boolean {
        return size in 24f..200f
    }

    /**
     * Validate delay is non-negative
     */
    fun isValidDelay(delay: Long): Boolean {
        return delay >= 0
    }

    /**
     * Generate a unique name for a configuration
     */
    fun generateConfigName(existingNames: List<String>): String {
        var index = 1
        var name = "Configuration $index"
        while (existingNames.contains(name)) {
            index++
            name = "Configuration $index"
        }
        return name
    }
}
