package com.autoclick.app.utils

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.Settings
import android.view.View
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * View Extensions
 */
fun View.show() {
    visibility = View.VISIBLE
}

fun View.hide() {
    visibility = View.GONE
}

fun View.invisible() {
    visibility = View.INVISIBLE
}

fun View.showSnackbar(
    message: String,
    duration: Int = Snackbar.LENGTH_SHORT,
    action: String? = null,
    actionCallback: (() -> Unit)? = null
) {
    Snackbar.make(this, message, duration).apply {
        action?.let { actionText ->
            setAction(actionText) { actionCallback?.invoke() }
        }
    }.show()
}

fun View.setDebounceClickListener(
    debounceTime: Long = 600L,
    scope: CoroutineScope,
    onClick: () -> Unit
) {
    var lastClickTime: Long = 0
    var job: Job? = null
    
    setOnClickListener {
        val currentTime = System.currentTimeMillis()
        if (currentTime - lastClickTime >= debounceTime) {
            lastClickTime = currentTime
            job?.cancel()
            job = scope.launch {
                delay(debounceTime)
                onClick()
            }
        }
    }
}

/**
 * Context Extensions
 */
fun Context.showToast(message: String, duration: Int = Toast.LENGTH_SHORT) {
    Toast.makeText(this, message, duration).show()
}

fun Context.openAccessibilitySettings() {
    startActivity(Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS).apply {
        flags = Intent.FLAG_ACTIVITY_NEW_TASK
    })
}

fun Context.openOverlaySettings() {
    startActivity(Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION).apply {
        data = Uri.parse("package:$packageName")
        flags = Intent.FLAG_ACTIVITY_NEW_TASK
    })
}

fun Context.showPermissionDialog(
    title: String,
    message: String,
    positiveButton: String,
    negativeButton: String,
    onPositiveClick: () -> Unit,
    onNegativeClick: () -> Unit = {}
) {
    MaterialAlertDialogBuilder(this)
        .setTitle(title)
        .setMessage(message)
        .setPositiveButton(positiveButton) { _, _ -> onPositiveClick() }
        .setNegativeButton(negativeButton) { _, _ -> onNegativeClick() }
        .setCancelable(false)
        .show()
}

/**
 * Fragment Extensions
 */
fun Fragment.showToast(message: String, duration: Int = Toast.LENGTH_SHORT) {
    requireContext().showToast(message, duration)
}

fun Fragment.showSnackbar(
    message: String,
    duration: Int = Snackbar.LENGTH_SHORT,
    action: String? = null,
    actionCallback: (() -> Unit)? = null
) {
    view?.showSnackbar(message, duration, action, actionCallback)
}

/**
 * Long Extensions
 */
fun Long.toFormattedTime(): String = Utils.formatTime(this)

/**
 * String Extensions
 */
fun String.toTimeInMillis(): Long? = Utils.parseTime(this)

/**
 * Float Extensions
 */
fun Float.toDp(): Float = Utils.pxToDp(this)

fun Float.toPx(): Float = Utils.dpToPx(this)

/**
 * Boolean Extensions
 */
fun Boolean.toVisibility(): Int = if (this) View.VISIBLE else View.GONE

/**
 * Null Safety Extensions
 */
fun <T> T?.orDefault(default: T): T {
    return this ?: default
}

/**
 * Collection Extensions
 */
fun <T> List<T>.moveItem(fromPosition: Int, toPosition: Int): List<T> {
    if (fromPosition < 0 || fromPosition >= size || toPosition < 0 || toPosition >= size) {
        return this
    }
    
    return toMutableList().apply {
        val item = removeAt(fromPosition)
        add(toPosition, item)
    }
}
