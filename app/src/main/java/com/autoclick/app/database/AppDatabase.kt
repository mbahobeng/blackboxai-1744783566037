package com.autoclick.app.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.autoclick.app.models.ClickPoint
import com.autoclick.app.models.ClickConfiguration
import com.autoclick.app.utils.Converters

@Database(
    entities = [ClickPoint::class, ClickConfiguration::class],
    version = 1,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun clickPointDao(): ClickPointDao
    abstract fun clickConfigurationDao(): ClickConfigurationDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "autoclick_database"
                )
                .fallbackToDestructiveMigration()
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}

// Type converters for Room
package com.autoclick.app.utils

import androidx.room.TypeConverter
import java.util.*

class Converters {
    @TypeConverter
    fun fromTimestamp(value: Long?): Date? {
        return value?.let { Date(it) }
    }

    @TypeConverter
    fun dateToTimestamp(date: Date?): Long? {
        return date?.time
    }

    @TypeConverter
    fun fromList(list: List<Long>): String {
        return list.joinToString(",")
    }

    @TypeConverter
    fun toList(data: String): List<Long> {
        if (data.isEmpty()) return emptyList()
        return data.split(",").map { it.toLong() }
    }
}
