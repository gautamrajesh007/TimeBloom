package com.timebloom.app.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.timebloom.app.data.local.dao.AchievementDao
import com.timebloom.app.data.local.dao.CheckInDao
import com.timebloom.app.data.local.dao.PlantDao
import com.timebloom.app.data.local.entity.Achievement
import com.timebloom.app.data.local.entity.CheckIn
import com.timebloom.app.data.local.entity.Plant

@Database(
    entities = [Plant::class, CheckIn::class, Achievement::class],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun plantDao(): PlantDao
    abstract fun checkInDao(): CheckInDao
    abstract fun achievementDao(): AchievementDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "timebloom_database"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}