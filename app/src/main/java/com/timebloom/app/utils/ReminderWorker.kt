package com.timebloom.app.utils

import android.content.Context
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters

class ReminderWorker(
    appContext: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(appContext, workerParams) {

    companion object {
        const val KEY_PLANT_NAME = "PLANT_NAME"
    }

    override suspend fun doWork(): Result {
        val plantName = inputData.getString(KEY_PLANT_NAME) ?: "Your plant"

        try {
            val notificationHelper = NotificationHelper(applicationContext)
            notificationHelper.showWaterReminderNotification(plantName)
            return Result.success()
        } catch (e: Exception) {
            Log.e("ReminderWorker", "Failed to show notification", e)
            return Result.failure()
        }
    }
}