package com.timebloom.app.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "achievements")
data class Achievement(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val plantId: Long,
    val type: AchievementType,
    val title: String,
    val description: String,
    val unlockedAt: Long,
    val rainDropsAwarded: Int
)

enum class AchievementType {
    FIRST_CHECKIN,
    WEEK_STREAK,
    MONTH_STREAK,
    HUNDRED_DAYS,
    FULL_GARDEN,
    PERFECT_WEEK,
    REVIVAL_MASTER,
    EARLY_BIRD,
    NIGHT_OWL
}
