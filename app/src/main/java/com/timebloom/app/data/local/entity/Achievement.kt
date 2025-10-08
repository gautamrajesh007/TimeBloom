package com.timebloom.app.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "achievements")
data class Achievement(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val title: String,
    val description: String,
    val iconEmoji: String,
    val unlockedAt: Long? = null,
    val isUnlocked: Boolean = false
)
