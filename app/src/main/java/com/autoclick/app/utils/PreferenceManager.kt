package com.autoclick.app.utils

import android.content.Context
import android.content.SharedPreferences
import androidx.core.content.edit
import com.autoclick.app.utils.Constants.PREF_FIRST_RUN
import com.autoclick.app.utils.Constants.PREF_LAST_CONFIG_ID
import com.autoclick.app.utils.Constants.PREF_THEME_MODE
import com.autoclick.app.utils.Constants.PREFS_NAME

class PreferenceManager private constructor(context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    companion object {
        @Volatile
        private var instance: PreferenceManager? = null

        fun getInstance(context: Context): PreferenceManager {
            return instance ?: synchronized(this) {
                instance ?: PreferenceManager(context.applicationContext).also {
                    instance = it
                }
            }
        }
    }

    /**
     * Theme Mode
     */
    var themeMode: Int
        get() = prefs.getInt(PREF_THEME_MODE, -1)
        set(value) = prefs.edit { putInt(PREF_THEME_MODE, value) }

    /**
     * Last Used Configuration ID
     */
    var lastConfigurationId: Long
        get() = prefs.getLong(PREF_LAST_CONFIG_ID, -1L)
        set(value) = prefs.edit { putLong(PREF_LAST_CONFIG_ID, value) }

    /**
     * First Run Flag
     */
    var isFirstRun: Boolean
        get() = prefs.getBoolean(PREF_FIRST_RUN, true)
        set(value) = prefs.edit { putBoolean(PREF_FIRST_RUN, value) }

    /**
     * Save Click Point Position
     */
    fun saveClickPointPosition(id: Long, x: Float, y: Float) {
        prefs.edit {
            putFloat("click_point_${id}_x", x)
            putFloat("click_point_${id}_y", y)
        }
    }

    /**
     * Get Click Point Position
     */
    fun getClickPointPosition(id: Long): Pair<Float, Float>? {
        val x = prefs.getFloat("click_point_${id}_x", -1f)
        val y = prefs.getFloat("click_point_${id}_y", -1f)
        return if (x != -1f && y != -1f) Pair(x, y) else null
    }

    /**
     * Save Configuration Settings
     */
    fun saveConfigurationSettings(id: Long, settings: Map<String, Any>) {
        prefs.edit {
            settings.forEach { (key, value) ->
                when (value) {
                    is String -> putString("config_${id}_${key}", value)
                    is Int -> putInt("config_${id}_${key}", value)
                    is Long -> putLong("config_${id}_${key}", value)
                    is Float -> putFloat("config_${id}_${key}", value)
                    is Boolean -> putBoolean("config_${id}_${key}", value)
                }
            }
        }
    }

    /**
     * Get Configuration Setting
     */
    inline fun <reified T> getConfigurationSetting(id: Long, key: String, defaultValue: T): T {
        return when (T::class) {
            String::class -> prefs.getString("config_${id}_${key}", defaultValue as String) as T
            Int::class -> prefs.getInt("config_${id}_${key}", defaultValue as Int) as T
            Long::class -> prefs.getLong("config_${id}_${key}", defaultValue as Long) as T
            Float::class -> prefs.getFloat("config_${id}_${key}", defaultValue as Float) as T
            Boolean::class -> prefs.getBoolean("config_${id}_${key}", defaultValue as Boolean) as T
            else -> defaultValue
        }
    }

    /**
     * Clear Configuration Settings
     */
    fun clearConfigurationSettings(id: Long) {
        prefs.edit {
            prefs.all.keys
                .filter { it.startsWith("config_${id}_") }
                .forEach { remove(it) }
        }
    }

    /**
     * Clear All Preferences
     */
    fun clearAll() {
        prefs.edit { clear() }
    }

    /**
     * Save Last Used Values
     */
    fun saveLastUsedValues(delay: Long, size: Float) {
        prefs.edit {
            putLong("last_used_delay", delay)
            putFloat("last_used_size", size)
        }
    }

    /**
     * Get Last Used Values
     */
    fun getLastUsedDelay(): Long = prefs.getLong("last_used_delay", Constants.DEFAULT_DELAY)
    fun getLastUsedSize(): Float = prefs.getFloat("last_used_size", Constants.DEFAULT_BUTTON_SIZE)

    /**
     * Save Service State
     */
    var isServiceRunning: Boolean
        get() = prefs.getBoolean("service_running", false)
        set(value) = prefs.edit { putBoolean("service_running", value) }

    /**
     * Save Last Active Configuration
     */
    var lastActiveConfigId: Long
        get() = prefs.getLong("last_active_config", -1L)
        set(value) = prefs.edit { putLong("last_active_config", value) }

    /**
     * Tutorial Shown Flags
     */
    fun setTutorialShown(key: String) {
        prefs.edit { putBoolean("tutorial_${key}_shown", true) }
    }

    fun isTutorialShown(key: String): Boolean {
        return prefs.getBoolean("tutorial_${key}_shown", false)
    }
}
