package com.autoclick.app.utils

object Constants {
    // Database
    const val DATABASE_NAME = "autoclick_database"
    const val DATABASE_VERSION = 1

    // Click Point Configuration
    const val MIN_BUTTON_SIZE = 24f
    const val MAX_BUTTON_SIZE = 200f
    const val DEFAULT_BUTTON_SIZE = 48f
    const val MIN_DELAY = 0L
    const val DEFAULT_DELAY = 1000L
    const val MAX_DELAY = 60000L // 1 minute

    // Service
    const val NOTIFICATION_CHANNEL_ID = "AutoClickService"
    const val NOTIFICATION_CHANNEL_NAME = "Auto Click Service"
    const val NOTIFICATION_ID = 1001
    const val SERVICE_ACTION_START = "START"
    const val SERVICE_ACTION_STOP = "STOP"
    const val SERVICE_ACTION_PERFORM_CLICK = "PERFORM_CLICK"

    // Intent Extra Keys
    const val EXTRA_CLICK_POINT_ID = "click_point_id"
    const val EXTRA_CONFIGURATION_ID = "configuration_id"
    const val EXTRA_CLICK_X = "click_x"
    const val EXTRA_CLICK_Y = "click_y"
    const val EXTRA_CLICK_SIZE = "click_size"
    const val EXTRA_CLICK_DELAY = "click_delay"

    // Shared Preferences
    const val PREFS_NAME = "autoclick_prefs"
    const val PREF_LAST_CONFIG_ID = "last_config_id"
    const val PREF_FIRST_RUN = "first_run"
    const val PREF_THEME_MODE = "theme_mode"

    // Permission Request Codes
    const val PERMISSION_OVERLAY = 1001
    const val PERMISSION_ACCESSIBILITY = 1002

    // Dialog Tags
    const val DIALOG_ADD_CLICK_POINT = "add_click_point"
    const val DIALOG_EDIT_CLICK_POINT = "edit_click_point"
    const val DIALOG_SAVE_CONFIG = "save_config"
    const val DIALOG_LOAD_CONFIG = "load_config"
    const val DIALOG_PERMISSIONS = "permissions"

    // Time Format
    const val TIME_FORMAT = "HH:mm"
    const val TIME_ZONE = "UTC"

    // Animation Durations
    const val ANIMATION_DURATION_SHORT = 150L
    const val ANIMATION_DURATION_MEDIUM = 300L
    const val ANIMATION_DURATION_LONG = 500L

    // Click Feedback
    const val CLICK_INDICATOR_DURATION = 300L
    const val CLICK_INDICATOR_ALPHA = 0.7f

    // Error Messages
    const val ERROR_INVALID_SIZE = "Invalid button size. Size must be between $MIN_BUTTON_SIZE and $MAX_BUTTON_SIZE dp"
    const val ERROR_INVALID_DELAY = "Invalid delay. Delay must be between $MIN_DELAY and $MAX_DELAY ms"
    const val ERROR_INVALID_TIME = "Invalid time settings. End time must be after start time"
    const val ERROR_INVALID_COORDINATES = "Invalid coordinates. Click point must be within screen bounds"
    const val ERROR_SERVICE_NOT_ENABLED = "Accessibility service is not enabled"
    const val ERROR_OVERLAY_PERMISSION = "Overlay permission is not granted"

    // Success Messages
    const val SUCCESS_CONFIG_SAVED = "Configuration saved successfully"
    const val SUCCESS_CONFIG_LOADED = "Configuration loaded successfully"
    const val SUCCESS_POINT_ADDED = "Click point added successfully"
    const val SUCCESS_POINT_UPDATED = "Click point updated successfully"
    const val SUCCESS_POINT_DELETED = "Click point deleted successfully"

    // Default Values
    const val DEFAULT_CONFIG_NAME = "New Configuration"
    const val DEFAULT_POINT_NAME = "New Click Point"
    const val INFINITE_REPEAT = 0

    // UI Constants
    const val MIN_TOUCH_TARGET_SIZE = 48 // dp
    const val LIST_ITEM_ANIMATION_DURATION = 300L
    const val DEBOUNCE_TIMEOUT = 600L
    const val RIPPLE_ALPHA = 0.2f
}
