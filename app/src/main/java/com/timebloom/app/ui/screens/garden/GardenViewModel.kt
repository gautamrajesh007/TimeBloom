package com.timebloom.app.ui.screens.garden

import android.content.Context
import android.net.Uri
import android.util.Log // Import Log for error logging
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.work.Data
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import com.timebloom.app.data.export.GardenExporter
import com.timebloom.app.data.local.entity.Mood
import com.timebloom.app.data.local.entity.Plant
import com.timebloom.app.data.preferences.UserPreferences
import com.timebloom.app.data.repository.DuplicateCheckInException // Import the specific exception
import com.timebloom.app.data.repository.InsufficientRainDropsException
import com.timebloom.app.data.repository.PlantIsDeadException
import com.timebloom.app.data.repository.PlantIsWitheringException
import com.timebloom.app.data.repository.PlantRepository
import com.timebloom.app.utils.PlantGrowthCalculator
import com.timebloom.app.utils.ReminderWorker
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.concurrent.TimeUnit

sealed class CheckInState {
    object Idle : CheckInState()
    object Loading : CheckInState()
    object Success : CheckInState()
    data class Error(val message: String) : CheckInState()
    data class NeedsRevival(val plant: Plant) : CheckInState()
    data class NeedsRestart(val plant: Plant?) : CheckInState()
}

sealed class ExportState {
    object Idle : ExportState()
    object Loading : ExportState()
    object Success : ExportState()
    data class Error(val message: String) : ExportState()
}


class GardenViewModel(
    private val repository: PlantRepository,
    private val userPreferences: UserPreferences,
    private val context: Context
) : ViewModel() {

    val plants: StateFlow<List<Plant>> = repository.allActivePlants
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _exportState = MutableStateFlow<ExportState>(ExportState.Idle)
    val exportState: StateFlow<ExportState> = _exportState.asStateFlow()

    private val _checkInState = MutableStateFlow<CheckInState>(CheckInState.Idle)
    val checkInState: StateFlow<CheckInState> = _checkInState.asStateFlow()

    val gardenTheme: StateFlow<String> = userPreferences.gardenTheme
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "zen")

    val themeMode: StateFlow<String> = userPreferences.themeMode
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "auto")


    fun checkInPlant(plantId: Long, note: String = "", mood: Mood = Mood.NEUTRAL) {
        viewModelScope.launch {
            // Set loading state
            _checkInState.value = CheckInState.Loading
            try {
                repository.checkInPlant(plantId, note, mood)
                // Set success state on successful check-in
                _checkInState.value = CheckInState.Success
            } catch (e: PlantIsWitheringException) {
                val witheringPlant = plants.value.find { it.id == plantId }
                if (witheringPlant != null) {
                    _checkInState.value = CheckInState.NeedsRevival(witheringPlant)
                } else {
                    _checkInState.value = CheckInState.Error("Unknown plant is withering")
                }
            } catch (e: PlantIsDeadException){
                val currentPlant = plants.value.find { it.id == plantId }
                if (currentPlant != null) {
                    _checkInState.value = CheckInState.NeedsRestart(currentPlant)
                } else {
                    _checkInState.value = CheckInState.Error("Plant data not found")
                }
            } catch (e: DuplicateCheckInException) {
                // Catch the duplicate check-in exception
                _checkInState.value = CheckInState.Error(e.message ?: "Already watered today")
                Log.w("GardenViewModel", "Duplicate Check-in attempt for plant $plantId: ${e.message}")
            } catch (e: Exception) {
                // Catch any other general exceptions during check-in
                _checkInState.value = CheckInState.Error(e.localizedMessage ?: "Check-in failed")
                Log.e("GardenViewModel", "Check-in failed for plant $plantId", e)
            }
        }
    }

    fun archivePlant(plantId: Long) {
        viewModelScope.launch {
            repository.archivePlant(plantId)
        }
    }

    fun restartPlant(plantId: Long) {
        viewModelScope.launch {
            repository.restartPlant(plantId)
            _checkInState.value = CheckInState.Idle
        }
    }

    fun deletePlant(plantId: Long) {
        viewModelScope.launch {
            plants.value.find { it.id == plantId }?.let {
                repository.deletePlant(it)
            }
        }
    }

    fun revivePlant(plantId: Long) {
        viewModelScope.launch {
            try {
                repository.revivePlant(plantId)
                _checkInState.value = CheckInState.Success
            } catch (e: InsufficientRainDropsException) {
                _checkInState.value = CheckInState.Error(e.message ?: "Not enough rain drops")
            } catch (e: Exception) {
                _checkInState.value = CheckInState.Error(e.message ?: "Revival failed")
            }
        }
    }

    fun exportGarden(uri: Uri) {
        viewModelScope.launch {
            _exportState.value = ExportState.Loading

            val exporter = GardenExporter(context)
            val allCheckIns = repository.getAllCheckIns()

            exporter.exportToJson(plants.value, allCheckIns, uri)
                .onSuccess { _exportState.value = ExportState.Success }
                .onFailure { _exportState.value = ExportState.Error(it.message ?: "Export failed") }
        }
    }

    fun importGarden(uri: Uri) {
        viewModelScope.launch {
            _exportState.value = ExportState.Loading

            val exporter = GardenExporter(context)
            exporter.importFromJson(uri)
                .onSuccess { backup ->
                    backup.plants.forEach { repository.insertPlant(it) }
                    backup.checkIns.forEach { repository.insertCheckIn(it) }
                    _exportState.value = ExportState.Success
                }
                .onFailure { _exportState.value = ExportState.Error(it.message ?: "Import failed") }
        }
    }

    fun resetExportState() {
        _exportState.value = ExportState.Idle
    }

    fun resetCheckInState() {
        _checkInState.value = CheckInState.Idle
    }
}