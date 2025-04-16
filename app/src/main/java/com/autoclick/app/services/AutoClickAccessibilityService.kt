package com.autoclick.app.services

import android.accessibilityservice.AccessibilityService
import android.accessibilityservice.GestureDescription
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.graphics.Path
import android.graphics.PixelFormat
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.Gravity
import android.view.LayoutInflater
import android.view.WindowManager
import android.view.accessibility.AccessibilityEvent
import android.widget.FrameLayout
import com.autoclick.app.R
import com.google.android.material.floatingactionbutton.FloatingActionButton

class AutoClickAccessibilityService : AccessibilityService() {
    private var windowManager: WindowManager? = null
    private var overlayLayout: FrameLayout? = null
    private val clickReceiver = ClickReceiver()
    
    companion object {
        private const val TAG = "AutoClickService"
        private const val CLICK_DURATION = 100L // Duration of click in milliseconds
    }

    override fun onCreate() {
        super.onCreate()
        registerReceiver(clickReceiver, IntentFilter("PERFORM_CLICK"))
    }

    override fun onServiceConnected() {
        Log.d(TAG, "AutoClickAccessibilityService connected")
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        // We don't need to handle accessibility events
    }

    override fun onInterrupt() {
        Log.d(TAG, "AutoClickAccessibilityService interrupted")
    }

    override fun onDestroy() {
        super.onDestroy()
        try {
            unregisterReceiver(clickReceiver)
        } catch (e: Exception) {
            Log.e(TAG, "Error unregistering receiver", e)
        }
        removeOverlay()
    }

    private fun performClick(x: Float, y: Float) {
        if (!isServiceRunning()) {
            Log.e(TAG, "Service is not running")
            return
        }

        val path = Path()
        path.moveTo(x, y)

        val gestureBuilder = GestureDescription.Builder()
        val gestureStroke = GestureDescription.StrokeDescription(path, 0, CLICK_DURATION)
        gestureBuilder.addStroke(gestureStroke)

        dispatchGesture(gestureBuilder.build(), object : GestureResultCallback() {
            override fun onCompleted(gestureDescription: GestureDescription?) {
                Log.d(TAG, "Click completed at ($x, $y)")
            }

            override fun onCancelled(gestureDescription: GestureDescription?) {
                Log.e(TAG, "Click cancelled at ($x, $y)")
            }
        }, Handler(Looper.getMainLooper()))
    }

    private fun showOverlay(x: Float, y: Float) {
        if (overlayLayout != null) return

        windowManager = getSystemService(Context.WINDOW_SERVICE) as WindowManager
        
        val layoutParams = WindowManager.LayoutParams().apply {
            type = WindowManager.LayoutParams.TYPE_ACCESSIBILITY_OVERLAY
            format = PixelFormat.TRANSLUCENT
            flags = WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
            width = WindowManager.LayoutParams.WRAP_CONTENT
            height = WindowManager.LayoutParams.WRAP_CONTENT
            gravity = Gravity.TOP or Gravity.START
            this.x = x.toInt()
            this.y = y.toInt()
        }

        overlayLayout = FrameLayout(this).apply {
            val fab = FloatingActionButton(context).apply {
                setImageResource(android.R.drawable.ic_menu_click)
                alpha = 0.7f
            }
            addView(fab)
        }

        try {
            windowManager?.addView(overlayLayout, layoutParams)
            Handler(Looper.getMainLooper()).postDelayed({
                removeOverlay()
            }, 300) // Remove overlay after 300ms
        } catch (e: Exception) {
            Log.e(TAG, "Error showing overlay", e)
        }
    }

    private fun removeOverlay() {
        try {
            if (overlayLayout != null) {
                windowManager?.removeView(overlayLayout)
                overlayLayout = null
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error removing overlay", e)
        }
    }

    private fun isServiceRunning(): Boolean {
        return true // Add proper service status check if needed
    }

    inner class ClickReceiver : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            if (intent?.action == "PERFORM_CLICK") {
                val x = intent.getFloatExtra("x", 0f)
                val y = intent.getFloatExtra("y", 0f)
                showOverlay(x, y)
                performClick(x, y)
            }
        }
    }
}
