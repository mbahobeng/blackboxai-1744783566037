package com.autoclick.app.models

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.Relation

@Entity(tableName = "click_configurations")
data class ClickConfiguration(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    var name: String = "",
    var isActive: Boolean = false,
    var startTime: Long? = null,
    var endTime: Long? = null,
    var createdAt: Long = System.currentTimeMillis(),
    var repeatCount: Int = 0, // 0 means infinite
    var globalDelay: Long = 0 // Additional delay between sequences
) {
    @Relation(
        parentColumn = "id",
        entityColumn = "configurationId"
    )
    var clickPoints: List<ClickPoint> = emptyList()

    fun isValidTime(): Boolean {
        return when {
            startTime == null && endTime == null -> true
            startTime != null && endTime != null -> startTime < endTime
            else -> false
        }
    }

    fun isValidName(): Boolean {
        return name.isNotBlank()
    }

    fun isValidGlobalDelay(): Boolean {
        return globalDelay >= 0
    }

    fun isValidRepeatCount(): Boolean {
        return repeatCount >= 0
    }

    fun validate(): Boolean {
        return isValidTime() && 
               isValidName() && 
               isValidGlobalDelay() && 
               isValidRepeatCount()
    }
}
