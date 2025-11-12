package com.timebloom.app.ui.screens.garden

import android.content.Context
import android.net.Uri
import android.util.Log // Import Log for error logging
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.timebloom.app.data.export.GardenExporter
import com.timebloom.app.data.local.entity.Mood
import com.timebloom.app.data.local.entity.Plant
import com.timebloom.app.data.repository.DuplicateCheckInException // Import the specific exception
import com.timebloom.app.data.repository.PlantRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

// ... (ExportState and CheckInState sealed classes as defined above)
// Add CheckInState here if you put it in the same file
sealed class CheckInState {
    object Idle : CheckInState()
    object Loading : CheckInState()
    object Success : CheckInState()
    data class Error(val message: String) : CheckInState()
}
sealed class ExportState {
    object Idle : ExportState()
    object Loading : ExportState()
    object Success : ExportState()
    data class Error(val message: String) : ExportState()
}


class GardenViewModel(
    private val repository: PlantRepository,
    private val context: Context
) : ViewModel() {

    val plants: StateFlow<List<Plant>> = repository.allActivePlants
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _exportState = MutableStateFlow<ExportState>(ExportState.Idle)
    val exportState: StateFlow<ExportState> = _exportState.asStateFlow()

    // Add this new state flow for check-in operations
    private val _checkInState = MutableStateFlow<CheckInState>(CheckInState.Idle)
    val checkInState: StateFlow<CheckInState> = _checkInState.asStateFlow()


    fun checkInPlant(plantId: Long, note: String = "", mood: Mood = Mood.NEUTRAL) {
        viewModelScope.launch {
            // Set loading state
            _checkInState.value = CheckInState.Loading
            try {
                repository.checkInPlant(plantId, note, mood)
                // Set success state on successful check-in
                _checkInState.value = CheckInState.Success
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

    fun deletePlant(plantId: Long) {
        viewModelScope.launch {
            plants.value.find { it.id == plantId }?.let {
                repository.deletePlant(it)
            }
        }
    }

    fun revivePlant(plantId: Long) {
        viewModelScope.launch {
            // Add error handling for revivePlant similar to checkInPlant if needed
            // For example, if InsufficientRainDropsException can be thrown.
            repository.revivePlant(plantId)
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