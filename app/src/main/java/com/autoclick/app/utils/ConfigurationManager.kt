package com.autoclick.app.utils

import android.content.Context
import android.net.Uri
import com.autoclick.app.models.ClickConfiguration
import com.autoclick.app.models.ClickPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.io.*
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class ConfigurationManager(private val context: Context) {

    /**
     * Export configuration to JSON file
     */
    suspend fun exportConfiguration(
        configuration: ClickConfiguration,
        clickPoints: List<ClickPoint>,
        uri: Uri
    ) = withContext(Dispatchers.IO) {
        try {
            val jsonObject = JSONObject().apply {
                put("configuration", configurationToJson(configuration))
                put("clickPoints", clickPointsToJson(clickPoints))
                put("version", 1) // For future compatibility
                put("timestamp", System.currentTimeMillis())
            }

            val tempFile = createTempFile()
            try {
                // Write to temp file first
                tempFile.writeText(jsonObject.toString(2))
                // Copy to final destination
                copyFileToUri(tempFile, uri)
                Result.success(Unit)
            } finally {
                deleteFile(tempFile)
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Import configuration from JSON file
     */
    suspend fun importConfiguration(uri: Uri): Result<Pair<ClickConfiguration, List<ClickPoint>>> = 
        withContext(Dispatchers.IO) {
            val tempFile = createTempFile()
            try {
                copyUriToFile(uri, tempFile)
                val jsonString = tempFile.readText()
                val jsonObject = JSONObject(jsonString)
                val version = jsonObject.optInt("version", 1)

                // Version check for future compatibility
                if (version > 1) {
                    throw IllegalStateException("Unsupported configuration version")
                }

                val configuration = jsonToConfiguration(jsonObject.getJSONObject("configuration"))
                val clickPoints = jsonToClickPoints(jsonObject.getJSONArray("clickPoints"))

                // Clean up old temp files
                cleanupTempFiles()

                Result.success(Pair(configuration, clickPoints))
            } catch (e: Exception) {
                Result.failure(e)
            } finally {
                deleteFile(tempFile)
            }
        }

    private fun configurationToJson(configuration: ClickConfiguration): JSONObject {
        return JSONObject().apply {
            put("name", configuration.name)
            put("isActive", configuration.isActive)
            put("startTime", configuration.startTime)
            put("endTime", configuration.endTime)
            put("createdAt", configuration.createdAt)
        }
    }

    private fun clickPointsToJson(clickPoints: List<ClickPoint>): JSONArray {
        return JSONArray().apply {
            clickPoints.forEach { clickPoint ->
                put(JSONObject().apply {
                    put("name", clickPoint.name)
                    put("x", clickPoint.x)
                    put("y", clickPoint.y)
                    put("size", clickPoint.size)
                    put("delay", clickPoint.delay)
                    put("order", clickPoint.order)
                    put("startTime", clickPoint.startTime)
                    put("endTime", clickPoint.endTime)
                })
            }
        }
    }

    private fun jsonToConfiguration(json: JSONObject): ClickConfiguration {
        return ClickConfiguration(
            name = json.getString("name"),
            isActive = json.getBoolean("isActive"),
            startTime = json.optLong("startTime").takeIf { it != 0L },
            endTime = json.optLong("endTime").takeIf { it != 0L },
            createdAt = json.optLong("createdAt", System.currentTimeMillis())
        )
    }

    private fun jsonToClickPoints(jsonArray: JSONArray): List<ClickPoint> {
        return List(jsonArray.length()) { index ->
            val json = jsonArray.getJSONObject(index)
            ClickPoint(
                name = json.getString("name"),
                x = json.getDouble("x").toFloat(),
                y = json.getDouble("y").toFloat(),
                size = json.getDouble("size").toFloat(),
                delay = json.getLong("delay"),
                order = json.getInt("order"),
                startTime = json.optLong("startTime").takeIf { it != 0L },
                endTime = json.optLong("endTime").takeIf { it != 0L }
            )
        }
    }

    /**
     * Generate unique file name for export
     */
    fun generateExportFileName(configName: String): String {
        val timestamp = System.currentTimeMillis()
        val sanitizedName = configName.replace("[^a-zA-Z0-9]".toRegex(), "_")
        return "autoclick_${sanitizedName}_$timestamp.json"
    }

    /**
     * Validate imported configuration
     */
    fun validateConfiguration(config: ClickConfiguration, clickPoints: List<ClickPoint>): Boolean {
        return try {
            // Validate configuration
            if (!config.validate()) {
                return false
            }

            // Validate each click point
            clickPoints.forEach { point ->
                if (!point.validate()) {
                    return false
                }
            }

            // Check for duplicate order values
            val orders = clickPoints.map { it.order }.toSet()
            if (orders.size != clickPoints.size) {
                return false // Duplicate order values found
            }

            true
        } catch (e: Exception) {
            false
        }
    }

    fun generateExportFileName(configName: String): String {
        val sanitizedName = configName
            .replace("[^a-zA-Z0-9-_]".toRegex(), "_") // Replace invalid chars with underscore
            .take(50) // Limit name length
            .trim('_') // Remove leading/trailing underscores
        val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US)
            .format(Date())
        return "autoclick_${sanitizedName}_$timestamp$FILE_EXTENSION"
    }

    companion object {
        private const val MIME_TYPE = "application/json"
        const val FILE_EXTENSION = ".json"
        private const val CONFIG_DIR = "configurations"
    }

    init {
        // Create configurations directory if it doesn't exist
        val configDir = File(context.getExternalFilesDir(null), CONFIG_DIR)
        if (!configDir.exists()) {
            configDir.mkdirs()
        }
    }

    private fun getConfigDir(): File {
        return File(context.getExternalFilesDir(null), CONFIG_DIR)
    }

    private fun createTempFile(prefix: String = "config", suffix: String = FILE_EXTENSION): File {
        return File.createTempFile(prefix, suffix, getConfigDir())
    }

    private fun copyUriToFile(uri: Uri, file: File) {
        context.contentResolver.openInputStream(uri)?.use { input ->
            FileOutputStream(file).use { output ->
                input.copyTo(output)
            }
        }
    }

    private fun copyFileToUri(file: File, uri: Uri) {
        context.contentResolver.openOutputStream(uri)?.use { output ->
            FileInputStream(file).use { input ->
                input.copyTo(output)
            }
        }
    }

    private fun deleteFile(file: File) {
        if (file.exists()) {
            file.delete()
        }
    }

    private fun cleanupTempFiles() {
        getConfigDir().listFiles()?.forEach { file ->
            if (file.name.startsWith("config") && file.name.endsWith(FILE_EXTENSION)) {
                // Delete files older than 24 hours
                if (System.currentTimeMillis() - file.lastModified() > 24 * 60 * 60 * 1000) {
                    deleteFile(file)
                }
            }
        }
    }
}
