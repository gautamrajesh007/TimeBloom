package com.timebloom.app.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.timebloom.app.data.local.entity.Plant
import kotlinx.coroutines.flow.Flow

@Dao
interface PlantDao {
    @Query("SELECT * FROM plants WHERE isArchived = 0 ORDER BY priority DESC, createdAt DESC")
    fun getAllActivePlants(): Flow<List<Plant>>

    @Query("SELECT * FROM plants WHERE id = :plantId")
    fun getPlantById(plantId: Long): Flow<Plant?>

    @Query("SELECT * FROM plants WHERE id = :plantId LIMIT 1")
    suspend fun getPlantByIdSync(plantId: Long): Plant?

    @Query("SELECT * FROM plants WHERE isArchived = 1")
    fun getArchivedPlants(): Flow<List<Plant>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPlant(plant: Plant): Long

    @Update
    suspend fun updatePlant(plant: Plant)

    @Delete
    suspend fun deletePlant(plant: Plant)

    @Query("UPDATE plants SET isArchived = 1 WHERE id = :plantId")
    suspend fun archivePlant(plantId: Long)

    @Query("SELECT COUNT(*) FROM plants WHERE isArchived = 0")
    fun getActivePlantCount(): Flow<Int>

    @Query("SELECT * FROM plants WHERE id = :plantId")
    suspend fun getPlantByIdOnce(plantId: Long): Plant?
}