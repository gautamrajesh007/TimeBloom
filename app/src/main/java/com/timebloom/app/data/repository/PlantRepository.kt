package com.timebloom.app.data.repository

import android.util.Log
import com.timebloom.app.data.local.dao.AchievementDao
import com.timebloom.app.data.local.dao.CheckInDao
import com.timebloom.app.data.local.dao.PlantDao
import com.timebloom.app.data.local.entity.Achievement
import com.timebloom.app.data.local.entity.CheckIn
import com.timebloom.app.data.local.entity.Mood
import com.timebloom.app.data.local.entity.Plant
import com.timebloom.app.utils.PlantGrowthCalculator
import kotlinx.coroutines.flow.Flow

class PlantRepository(
    private val plantDao: PlantDao,
    private val checkInDao: CheckInDao,
    private val achievementDao: AchievementDao
) {
    val allActivePlants: Flow<List<Plant>> = plantDao.getAllActivePlants()

    fun getPlantById(id: Long): Flow<Plant?> = plantDao.getPlantById(id)

    fun getCheckInsForPlant(plantId: Long): Flow<List<CheckIn>> =
        checkInDao.getCheckInsForPlant(plantId)

    suspend fun insertPlant(plant: Plant): Long {
        return try {
            plantDao.insertPlant(plant)
        } catch (e: Exception) {
            Log.e("PlantRepository", "Failed to insert plant", e)
            throw DatabaseException("Could not save plant: ${e.localizedMessage}", e)
        }
    }

    suspend fun insertCheckIn(checkIn: CheckIn): Long = checkInDao.insertCheckIn(checkIn)

    suspend fun updatePlant(plant: Plant) = plantDao.updatePlant(plant)

    suspend fun deletePlant(plant: Plant) = plantDao.deletePlant(plant)

    suspend fun checkInPlant(plantId: Long, note: String = "", mood: Mood = Mood.NEUTRAL) {
        try {
            val plant = plantDao.getPlantByIdSync(plantId) ?: throw PlantNotFoundException()

            if (PlantGrowthCalculator.hasCheckedInToday(plant)) {
                throw DuplicateCheckInException()
            }

            // Create check-in
            val checkIn = CheckIn(plantId = plantId, note = note, mood = mood)
            checkInDao.insertCheckIn(checkIn) // This uses the internal DAO directly

            // Calculate rain drops based on streak
            val rainDropsEarned = PlantGrowthCalculator.getRainDropsForCheckIn(plant.currentStreakCount)

            // Update plant using PlantGrowthCalculator
            val updatedPlant = plant.copy(
                currentStreakCount = plant.currentStreakCount + 1,
                longestStreakCount = maxOf(plant.longestStreakCount, plant.currentStreakCount + 1),
                totalCheckIns = plant.totalCheckIns + 1,
                lastCheckIn = System.currentTimeMillis(),
                growthStage = PlantGrowthCalculator.calculateGrowthStage(
                    plant.copy(totalCheckIns = plant.totalCheckIns + 1)
                ),
                rainDrops = plant.rainDrops + rainDropsEarned,
                nextCheckInDue = PlantGrowthCalculator.calculateNextCheckInDue(plant)
            )
            plantDao.updatePlant(updatedPlant)
        } catch (e: Exception) {
            Log.e("PlantRepository", "Check-in failed", e)
            throw e
        }
    }

    suspend fun revivePlant(plantId: Long) {
        val plant = plantDao.getPlantByIdSync(plantId) ?: return
        val reviveCost = PlantGrowthCalculator.calculateReviveCost(plant)

        if (plant.rainDrops < reviveCost) {
            throw InsufficientRainDropsException("Need $reviveCost rain drops, have ${plant.rainDrops}")
        }

        plantDao.updatePlant(
            plant.copy(
                lastCheckIn = System.currentTimeMillis(),
                rainDrops = plant.rainDrops - reviveCost
            )
        )
    }

    suspend fun getAllCheckIns(): List<CheckIn> {
        return checkInDao.getAllCheckInsOnce()
    }

    suspend fun archivePlant(plantId: Long) = plantDao.archivePlant(plantId)

    val allAchievements: Flow<List<Achievement>> = achievementDao.getAllAchievements()
}

class DatabaseException(message: String, cause: Throwable? = null) : Exception(message, cause)
class PlantNotFoundException : Exception("Plant not found")
class DuplicateCheckInException : Exception("Already watered today")
class InsufficientRainDropsException(message: String) : Exception(message)