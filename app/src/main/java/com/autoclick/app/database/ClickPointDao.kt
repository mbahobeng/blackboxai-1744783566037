package com.autoclick.app.database

import androidx.room.*
import com.autoclick.app.models.ClickPoint
import kotlinx.coroutines.flow.Flow

@Dao
interface ClickPointDao {
    @Query("SELECT * FROM click_points WHERE configurationId = :configId ORDER BY `order` ASC")
    fun getClickPointsForConfiguration(configId: Long): Flow<List<ClickPoint>>

    @Query("SELECT * FROM click_points ORDER BY `order` ASC")
    fun getAllClickPoints(): Flow<List<ClickPoint>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(clickPoint: ClickPoint): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(clickPoints: List<ClickPoint>)

    @Update
    suspend fun update(clickPoint: ClickPoint)

    @Delete
    suspend fun delete(clickPoint: ClickPoint)

    @Query("DELETE FROM click_points WHERE configurationId = :configId")
    suspend fun deleteAllForConfiguration(configId: Long)

    @Query("DELETE FROM click_points")
    suspend fun deleteAll()

    @Query("SELECT MAX(`order`) FROM click_points WHERE configurationId = :configId")
    suspend fun getMaxOrderForConfiguration(configId: Long): Int?

    @Query("UPDATE click_points SET `order` = `order` + 1 WHERE configurationId = :configId AND `order` >= :fromOrder")
    suspend fun shiftOrdersUp(configId: Long, fromOrder: Int)

    @Query("UPDATE click_points SET `order` = `order` - 1 WHERE configurationId = :configId AND `order` > :deletedOrder")
    suspend fun shiftOrdersDown(configId: Long, deletedOrder: Int)

    @Transaction
    suspend fun reorderPoint(clickPoint: ClickPoint, newOrder: Int) {
        val oldOrder = clickPoint.order
        if (oldOrder == newOrder) return

        if (oldOrder < newOrder) {
            // Moving down: decrease order of points between old and new position
            shiftOrdersDown(clickPoint.configurationId!!, oldOrder)
        } else {
            // Moving up: increase order of points between new and old position
            shiftOrdersUp(clickPoint.configurationId!!, newOrder)
        }
        
        update(clickPoint.copy(order = newOrder))
    }
}
