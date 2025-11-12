// Updated app/src/main/java/com/timebloom/app/ui/screens/garden/GardenViewModel.kt
package com.timebloom.app.ui.screens.garden

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.timebloom.app.data.export.GardenExporter
import com.timebloom.app.data.local.entity.Mood
import com.timebloom.app.data.local.entity.Plant
import com.timebloom.app.data.repository.PlantRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class GardenViewModel(
    private val repository: PlantRepository,
    private val context: Context
) : ViewModel() {

    val plants: StateFlow<List<Plant>> = repository.allActivePlants
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _exportState = MutableStateFlow<ExportState>(ExportState.Idle)
    val exportState: StateFlow<ExportState> = _exportState.asStateFlow()

    fun checkInPlant(plantId: Long, note: String = "", mood: Mood = Mood.NEUTRAL) {
        viewModelScope.launch {
            repository.checkInPlant(plantId, note, mood)
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
}

sealed class ExportState {
    object Idle : ExportState()
    object Loading : ExportState()
    object Success : ExportState()
    data class Error(val message: String) : ExportState()
}
