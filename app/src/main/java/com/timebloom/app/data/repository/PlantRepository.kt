package com.timebloom.app.data.repository

import com.timebloom.app.data.local.dao.AchievementDao
import com.timebloom.app.data.local.dao.CheckInDao
import com.timebloom.app.data.local.dao.PlantDao
import com.timebloom.app.data.local.entity.Achievement
import com.timebloom.app.data.local.entity.CheckIn
import com.timebloom.app.data.local.entity.Mood
import com.timebloom.app.data.local.entity.Plant
import com.timebloom.app.utils.PlantGrowthCalculator
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first

class PlantRepository(
    private val plantDao: PlantDao,
    private val checkInDao: CheckInDao,
    private val achievementDao: AchievementDao
) {
    val allActivePlants: Flow<List<Plant>> = plantDao.getAllActivePlants()

    fun getPlantById(id: Long): Flow<Plant?> = plantDao.getPlantById(id)

    fun getCheckInsForPlant(plantId: Long): Flow<List<CheckIn>> =
        checkInDao.getCheckInsForPlant(plantId)

    suspend fun insertPlant(plant: Plant): Long = plantDao.insertPlant(plant)

    suspend fun updatePlant(plant: Plant) = plantDao.updatePlant(plant)

    suspend fun deletePlant(plant: Plant) = plantDao.deletePlant(plant)

    suspend fun checkInPlant(plantId: Long, note: String = "", mood: Mood = Mood.NEUTRAL) {
        val plant = plantDao.getPlantById(plantId).first() ?: return

        // Create check-in
        val checkIn = CheckIn(plantId = plantId, note = note, mood = mood)
        checkInDao.insertCheckIn(checkIn)

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
    }

    suspend fun revivePlant(plantId: Long) {
        val plant = plantDao.getPlantById(plantId).first() ?: return
        val reviveCost = PlantGrowthCalculator.calculateReviveCost(plant)

        if (plant.rainDrops >= reviveCost) {
            val updatedPlant = plant.copy(
                rainDrops = plant.rainDrops - reviveCost,
                lastCheckIn = System.currentTimeMillis(),
                currentStreakCount = 1,
                growthStage = PlantGrowthCalculator.calculateGrowthStage(plant)
            )
            plantDao.updatePlant(updatedPlant)
        }
    }

    suspend fun archivePlant(plantId: Long) = plantDao.archivePlant(plantId)

    val allAchievements: Flow<List<Achievement>> = achievementDao.getAllAchievements()
}