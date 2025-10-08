package com.timebloom.app.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.timebloom.app.data.local.entity.CheckIn
import kotlinx.coroutines.flow.Flow

@Dao
interface CheckInDao {
    @Query("SELECT * FROM check_ins WHERE plantId = :plantId ORDER BY timestamp DESC")
    fun getCheckInsForPlant(plantId: Long): Flow<List<CheckIn>>

    @Query("SELECT * FROM check_ins WHERE timestamp >= :startTime AND timestamp <= :endTime")
    fun getCheckInsBetween(startTime: Long, endTime: Long): Flow<List<CheckIn>>

    @Insert
    suspend fun insertCheckIn(checkIn: CheckIn): Long

    @Query("DELETE FROM check_ins WHERE plantId = :plantId")
    suspend fun deleteCheckInsForPlant(plantId: Long)
}