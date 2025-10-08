package com.timebloom.app.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey

@Entity(
    tableName = "check_ins",
    foreignKeys = [
        ForeignKey(
            entity = Plant::class,
            parentColumns = ["id"],
            childColumns = ["plantId"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class CheckIn(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val plantId: Long,
    val timestamp: Long = System.currentTimeMillis(),
    val note: String = "",
    val mood: Mood = Mood.NEUTRAL
)

enum class Mood(val emoji: String) {
    GREAT("😊"),
    GOOD("🙂"),
    NEUTRAL("😐"),
    BAD("😞")
}
