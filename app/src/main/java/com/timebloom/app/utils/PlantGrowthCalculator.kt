package com.timebloom.app.utils

import com.timebloom.app.data.local.entity.Frequency
import com.timebloom.app.data.local.entity.GrowthStage
import com.timebloom.app.data.local.entity.Plant
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.temporal.ChronoUnit
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
     * Now timezone-aware
     */
    fun shouldBeWithering(plant: Plant): Boolean {
        val lastCheckIn = plant.lastCheckIn ?: return false

        val lastCheckInDate = Instant.ofEpochMilli(lastCheckIn)
            .atZone(ZoneId.systemDefault())
            .toLocalDate()

        val today = LocalDate.now()
        val daysSinceLastCheckIn = ChronoUnit.DAYS.between(lastCheckInDate, today)

        val gracePeriod = when (plant.frequency) {
            Frequency.DAILY -> 1L
            Frequency.TWICE_WEEKLY -> 4L
            Frequency.THREE_TIMES_WEEKLY -> 2L
            Frequency.WEEKLY -> 7L
        }

        return daysSinceLastCheckIn > gracePeriod
    }

    /**
     * Calculate how many rain drops are needed to revive a withering plant
     */
    fun calculateReviveCost(plant: Plant): Int {
        val lastCheckIn = plant.lastCheckIn ?: return 1

        val lastCheckInDate = Instant.ofEpochMilli(lastCheckIn)
            .atZone(ZoneId.systemDefault())
            .toLocalDate()

        val daysMissed = ChronoUnit.DAYS.between(lastCheckInDate, LocalDate.now()).toInt()

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
     * Calculate health percentage (0-100) - Timezone aware
     */
    fun calculateHealthPercentage(plant: Plant): Float {
        if (plant.lastCheckIn == null) return 100f

        val lastCheckInDate = Instant.ofEpochMilli(plant.lastCheckIn)
            .atZone(ZoneId.systemDefault())
            .toLocalDate()

        val daysSinceLastCheckIn = ChronoUnit.DAYS.between(lastCheckInDate, LocalDate.now()).toFloat()

        val maxDaysForFullHealth = when (plant.frequency) {
            Frequency.DAILY -> 1f
            Frequency.TWICE_WEEKLY -> 3.5f
            Frequency.THREE_TIMES_WEEKLY -> 2.5f
            Frequency.WEEKLY -> 7f
        }

        // Ensure we don't have negative health
        if (daysSinceLastCheckIn < 0) return 100f

        val healthPercentage = ((maxDaysForFullHealth - daysSinceLastCheckIn) / maxDaysForFullHealth * 100)
            .coerceIn(0f, 100f)

        return healthPercentage
    }

    /**
     * Get rain drops earned per check-in based on streak
     */
    fun getRainDropsForCheckIn(currentStreak: Int): Int {
        return when {
            currentStreak < 6 -> 0 // 0 drops from 0-5
            currentStreak == 6 -> 1 // 1 drop on the 7th day (streak becomes 7)
            currentStreak < 29 -> 2 // 2 drops from 7-29
            currentStreak < 89 -> 3 // 3 drops from 30-89
            else -> 5 // 5 drops for 90+
        }
    }

    /**
     * Calculate total growth progress (0-1)
     */
    fun calculateGrowthProgress(plant: Plant): Float {
        val growthPoints = plant.totalCheckIns * plant.difficulty.growthRate
        val maxPoints = 30f
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

    /**
     * Check if user has already checked in today (prevents duplicates)
     */
    fun hasCheckedInToday(plant: Plant): Boolean {
        val lastCheckIn = plant.lastCheckIn ?: return false

        val lastCheckInDate = Instant.ofEpochMilli(lastCheckIn)
            .atZone(ZoneId.systemDefault())
            .toLocalDate()

        val today = LocalDate.now()

        return lastCheckInDate.isEqual(today)
    }
}
