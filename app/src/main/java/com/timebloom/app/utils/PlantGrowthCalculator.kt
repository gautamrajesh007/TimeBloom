package com.timebloom.app.utils

import com.timebloom.app.data.local.entity.Frequency
import com.timebloom.app.data.local.entity.GrowthStage
import com.timebloom.app.data.local.entity.Plant
import java.util.concurrent.TimeUnit

object PlantGrowthCalculator {

    /**
     * Calculate the next growth stage based on total check-ins and difficulty
     */
    fun calculateGrowthStage(plant: Plant): GrowthStage {
        val growthPoints = plant.totalCheckIns * plant.difficulty.growthRate

        return when {
            growthPoints < 3 -> GrowthStage.SEED
            growthPoints < 7 -> GrowthStage.SPROUT
            growthPoints < 15 -> GrowthStage.PLANT
            growthPoints < 30 -> GrowthStage.FLOWER
            else -> GrowthStage.FRUIT
        }
    }

    /**
     * Check if plant should be withering based on missed check-ins
     */
    fun shouldBeWithering(plant: Plant): Boolean {
        val lastCheckIn = plant.lastCheckIn ?: return false
        val daysSinceLastCheckIn = TimeUnit.MILLISECONDS.toDays(
            System.currentTimeMillis() - lastCheckIn
        )

        val gracePeriod = when (plant.frequency) {
            Frequency.DAILY -> 2
            Frequency.TWICE_WEEKLY -> 4
            Frequency.THREE_TIMES_WEEKLY -> 3
            Frequency.WEEKLY -> 8
        }

        return daysSinceLastCheckIn > gracePeriod
    }

    /**
     * Calculate how many rain drops are needed to revive a withering plant
     */
    fun calculateReviveCost(plant: Plant): Int {
        val daysMissed = plant.lastCheckIn?.let {
            TimeUnit.MILLISECONDS.toDays(System.currentTimeMillis() - it).toInt()
        } ?: 0

        return when {
            daysMissed <= 3 -> 1
            daysMissed <= 7 -> 2
            daysMissed <= 14 -> 3
            else -> 5
        }
    }

    /**
     * Calculate next check-in due date based on frequency
     */
    fun calculateNextCheckInDue(plant: Plant): Long {
        val lastCheckIn = plant.lastCheckIn ?: System.currentTimeMillis()
        return lastCheckIn + TimeUnit.HOURS.toMillis(plant.frequency.hoursInterval.toLong())
    }

    /**
     * Calculate health percentage (0-100)
     */
    fun calculateHealthPercentage(plant: Plant): Float {
        if (plant.lastCheckIn == null) return 100f

        val daysSinceLastCheckIn = TimeUnit.MILLISECONDS.toDays(
            System.currentTimeMillis() - plant.lastCheckIn
        ).toFloat()

        val maxDaysForFullHealth = when (plant.frequency) {
            Frequency.DAILY -> 1f
            Frequency.TWICE_WEEKLY -> 3.5f
            Frequency.THREE_TIMES_WEEKLY -> 2.5f
            Frequency.WEEKLY -> 7f
        }

        val healthPercentage = ((maxDaysForFullHealth - daysSinceLastCheckIn) / maxDaysForFullHealth * 100)
            .coerceIn(0f, 100f)

        return healthPercentage
    }

    /**
     * Get rain drops earned per check-in based on streak
     */
    fun getRainDropsForCheckIn(currentStreak: Int): Int {
        return when {
            currentStreak < 7 -> 1
            currentStreak < 30 -> 2
            currentStreak < 90 -> 3
            else -> 5
        }
    }

    /**
     * Calculate total growth progress (0-1)
     */
    fun calculateGrowthProgress(plant: Plant): Float {
        val growthPoints = plant.totalCheckIns * plant.difficulty.growthRate
        val maxPoints = 30f // Points needed to reach FRUIT stage
        return (growthPoints / maxPoints).coerceIn(0f, 1f)
    }

    /**
     * Get motivational message based on plant state
     */
    fun getMotivationalMessage(plant: Plant): String {
        return when {
            shouldBeWithering(plant) -> "Your ${plant.name} needs attention! 🥺"
            plant.currentStreakCount >= 30 -> "Amazing dedication! Keep going! 🌟"
            plant.currentStreakCount >= 7 -> "One week streak! You're on fire! 🔥"
            plant.growthStage == GrowthStage.FRUIT -> "Your ${plant.name} is thriving! 🎉"
            plant.totalCheckIns == 0 -> "Start your journey with ${plant.name}! 🌱"
            else -> "Keep nurturing your ${plant.name}! 💪"
        }
    }

    /**
     * Calculate estimated days to next growth stage
     */
    fun daysToNextStage(plant: Plant): Int {
        val currentPoints = plant.totalCheckIns * plant.difficulty.growthRate
        val nextStagePoints = when (plant.growthStage) {
            GrowthStage.SEED -> 3f
            GrowthStage.SPROUT -> 7f
            GrowthStage.PLANT -> 15f
            GrowthStage.FLOWER -> 30f
            GrowthStage.FRUIT, GrowthStage.WITHERING -> return 0
        }

        val pointsNeeded = nextStagePoints - currentPoints
        val checkInsNeeded = (pointsNeeded / plant.difficulty.growthRate).toInt() + 1

        return when (plant.frequency) {
            Frequency.DAILY -> checkInsNeeded
            Frequency.TWICE_WEEKLY -> checkInsNeeded * 4
            Frequency.THREE_TIMES_WEEKLY -> checkInsNeeded * 2
            Frequency.WEEKLY -> checkInsNeeded * 7
        }
    }
}