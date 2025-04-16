package com.autoclick.app.database

import androidx.room.*
import com.autoclick.app.models.ClickConfiguration
import kotlinx.coroutines.flow.Flow

@Dao
interface ClickConfigurationDao {
    @Transaction
    @Query("SELECT * FROM click_configurations ORDER BY createdAt DESC")
    fun getAllConfigurations(): Flow<List<ClickConfiguration>>

    @Transaction
    @Query("SELECT * FROM click_configurations WHERE id = :configId")
    suspend fun getConfigurationById(configId: Long): ClickConfiguration?

    @Transaction
    @Query("SELECT * FROM click_configurations WHERE isActive = 1")
    fun getActiveConfigurations(): Flow<List<ClickConfiguration>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(configuration: ClickConfiguration): Long

    @Update
    suspend fun update(configuration: ClickConfiguration)

    @Delete
    suspend fun delete(configuration: ClickConfiguration)

    @Query("DELETE FROM click_configurations")
    suspend fun deleteAll()

    @Query("UPDATE click_configurations SET isActive = :isActive WHERE id = :configId")
    suspend fun updateActiveStatus(configId: Long, isActive: Boolean)

    @Transaction
    suspend fun toggleConfiguration(configId: Long) {
        val config = getConfigurationById(configId)
        config?.let {
            updateActiveStatus(configId, !it.isActive)
        }
    }

    @Query("SELECT COUNT(*) FROM click_configurations")
    suspend fun getConfigurationCount(): Int

    @Transaction
    @Query("SELECT * FROM click_configurations WHERE name LIKE :searchQuery")
    fun searchConfigurations(searchQuery: String): Flow<List<ClickConfiguration>>

    @Transaction
    suspend fun duplicateConfiguration(configId: Long): Long? {
        val originalConfig = getConfigurationById(configId) ?: return null
        
        // Create new configuration with copied properties
        val newConfig = originalConfig.copy(
            id = 0,
            name = "${originalConfig.name} (Copy)",
            isActive = false,
            createdAt = System.currentTimeMillis()
        )
        
        return insert(newConfig)
    }
}
