package com.timebloom.app.ui.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.timebloom.app.data.local.entity.Plant
import com.timebloom.app.data.repository.PlantRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class CreatePlantViewModel(
    private val plantRepository: PlantRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<PlantCreationUiState>(PlantCreationUiState.Idle)
    val uiState: StateFlow<PlantCreationUiState> = _uiState.asStateFlow()

    fun createNewPlant(plantToSave: Plant) {
        viewModelScope.launch {
            _uiState.value = PlantCreationUiState.Loading

            try {
                val newPlantId = plantRepository.insertPlant(plantToSave)
                _uiState.value = PlantCreationUiState.Success(newPlantId)
            } catch (e: Exception) {
                _uiState.value = PlantCreationUiState.Error(
                    "Failed to create plant. Please try again. (${e.localizedMessage ?: "Unknown error"})"
                )
                Log.e("CreatePlantViewModel", "Error inserting plant: ${e.message}", e)
            }
        }
    }
}