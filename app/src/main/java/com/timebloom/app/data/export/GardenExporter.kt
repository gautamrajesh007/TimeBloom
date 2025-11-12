package com.timebloom.app.data.export

import android.content.Context
import android.net.Uri
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.timebloom.app.data.local.entity.CheckIn
import com.timebloom.app.data.local.entity.Plant
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.OutputStreamWriter

data class GardenBackup(
    val version: Int = 1,
    val exportDate: Long = System.currentTimeMillis(),
    val plants: List<Plant>,
    val checkIns: List<CheckIn>
)

class GardenExporter(private val context: Context) {
    private val gson: Gson = GsonBuilder()
        .setPrettyPrinting()
        .create()

    suspend fun exportToJson(
        plants: List<Plant>,
        checkIns: List<CheckIn>,
        uri: Uri
    ): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val backup = GardenBackup(
                plants = plants,
                checkIns = checkIns
            )

            context.contentResolver.openOutputStream(uri)?.use { outputStream ->
                OutputStreamWriter(outputStream).use { writer ->
                    gson.toJson(backup, writer)
                }
            }
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun importFromJson(uri: Uri): Result<GardenBackup> = withContext(Dispatchers.IO) {
        try {
            context.contentResolver.openInputStream(uri)?.use { inputStream ->
                val json = inputStream.bufferedReader().use { it.readText() }
                val backup = gson.fromJson(json, GardenBackup::class.java)
                Result.success(backup)
            } ?: Result.failure(Exception("Could not open file"))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
