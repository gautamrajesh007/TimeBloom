package com.timebloom.app.ui.screens.createedit

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.timebloom.app.data.local.entity.Difficulty
import com.timebloom.app.data.local.entity.Frequency
import com.timebloom.app.data.local.entity.Plant
import com.timebloom.app.data.repository.PlantRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed class SavePlantState {
    object Idle : SavePlantState()
    object Loading : SavePlantState()
    data class Success(val plantId: Long?) : SavePlantState()
//    object Success : SavePlantState()
    data class Error(val message: String) : SavePlantState()
}

class CreateEditPlantViewModel(
    private val repository: PlantRepository,
    private val plantId: Long?
) : ViewModel() {

    private val _plant = MutableStateFlow<Plant?>(null)
    val plant: StateFlow<Plant?> = _plant.asStateFlow()

    private val _saveState = MutableStateFlow<SavePlantState>(SavePlantState.Idle)
    val saveState: StateFlow<SavePlantState> = _saveState.asStateFlow()

    init {
        // Only load plant if plantId is valid (not null and not -1)
        if (plantId != null && plantId > 0) {
            viewModelScope.launch {
                try {
                    repository.getPlantById(plantId).collect { p ->
                        _plant.value = p
                    }
                } catch (e: Exception) {
                    _saveState.value = SavePlantState.Error("Failed to load plant: ${e.message}")
                }
            }
        }
    }

    fun savePlant(
        name: String,
        description: String,
        frequency: Frequency,
        difficulty: Difficulty,
        color: String
    ) {
        viewModelScope.launch {
            try {
                _saveState.value = SavePlantState.Loading

                val currentPlant = _plant.value // Capture current value once
                val plantToSave = if (plantId != null && currentPlant != null) {
                    currentPlant.copy(
                        name = name,
                        description = description,
                        frequency = frequency,
                        difficulty = difficulty,
                        color = color
                    )
                } else {
                    Plant(
                        name = name,
                        description = description,
                        frequency = frequency,
                        difficulty = difficulty,
                        color = color,
                        createdAt = System.currentTimeMillis()
                    )
                }

                val id = repository.insertPlant(plantToSave)
                _saveState.value = SavePlantState.Success(id)
            } catch (e: Exception) {
                _saveState.value = SavePlantState.Error(
                    "Failed to save plant. Please try again. (${e.localizedMessage ?: "Unknown error"})"
                )
                Log.e("CreateEditPlantViewModel", "Error saving plant: ${e.message}", e)
            }
        }
    }


}
