package com.timebloom.app.data.local.entity

data class Plant(
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
