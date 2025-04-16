package com.autoclick.app.models

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "click_points")
data class ClickPoint(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    var x: Float = 0f,
    var y: Float = 0f,
    var size: Float = 48f, // Default size in dp
    var delay: Long = 1000, // Default delay in milliseconds
    var order: Int = 0,
    var startTime: Long? = null,
    var endTime: Long? = null,
    var name: String = "",
    var configurationId: Long? = null // Reference to parent configuration
) {
    fun isValidTime(): Boolean {
        return when {
            startTime == null && endTime == null -> true
            startTime != null && endTime != null -> startTime < endTime
            else -> false
        }
    }

    fun isValidCoordinates(): Boolean {
        return x >= 0f && y >= 0f
    }

    fun isValidSize(): Boolean {
        return size in 24f..200f // Minimum 24dp, Maximum 200dp
    }

    fun isValidDelay(): Boolean {
        return delay >= 0
    }

    fun validate(): Boolean {
        return isValidTime() && 
               isValidCoordinates() && 
               isValidSize() && 
               isValidDelay() &&
               configurationId != null && // Must belong to a configuration
               order >= 0 // Order must be non-negative
    }
}
