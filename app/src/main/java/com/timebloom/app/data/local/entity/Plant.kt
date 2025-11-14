package com.timebloom.app.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "plants")
data class Plant(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val description: String = "",
    val difficulty: Difficulty = Difficulty.MEDIUM,
    val frequency: Frequency = Frequency.DAILY,
    val growthStage: GrowthStage = GrowthStage.SEED,
    val currentStreakCount: Int = 0,
    val longestStreakCount: Int = 0,
    val totalCheckIns: Int = 0,
    val createdAt: Long = System.currentTimeMillis(),
    val lastCheckIn: Long? = null,
    val nextCheckInDue: Long? = null,
    val rainDrops: Int = 0,
    val isArchived: Boolean = false,
    val priority: Int = 0,
    val color: String = "#4CAF50"
)

enum class Difficulty(val growthRate: Float) {
    EASY(1.5f),
    MEDIUM(1.0f),
    HARD(0.7f)
}

enum class Frequency(val hoursInterval: Int) {
    DAILY(24),
    WEEKLY(168),
    TWICE_WEEKLY(84),
    THREE_TIMES_WEEKLY(56)
}

enum class GrowthStage(val displayName: String, val emoji: String) {
    SEED("Seed", "🌰"),
    SPROUT("Sprout", "🌱"),
    PLANT("Plant", "🪴"),
    FLOWER("Flower", "🌸"),
    FRUIT("Fruit", "🍎"),
    WITHERING("Withering", "🥀"),
    DEAD("Dead", "💀")
}