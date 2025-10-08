package com.timebloom.app.ui.screens.createedit

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

class CreateEditPlantViewModel(
    private val repository: PlantRepository,
    private val plantId: Long?
) : ViewModel() {

    private val _plant = MutableStateFlow<Plant?>(null)
    val plant: StateFlow<Plant?> = _plant.asStateFlow()

    init {
        plantId?.let {
            viewModelScope.launch {
                repository.getPlantById(it).collect { p ->
                    _plant.value = p
                }
            }
        }
    }

    fun savePlant(
        name: String,
        description: String,
        difficulty: Difficulty,
        frequency: Frequency,
        color: String
    ) {
        viewModelScope.launch {
            val plantToSave = if (plantId != null && _plant.value != null) {
                _plant.value!!.copy(
                    name = name,
                    description = description,
                    difficulty = difficulty,
                    frequency = frequency,
                    color = color
                )
            } else {
                Plant(
                    name = name,
                    description = description,
                    difficulty = difficulty,
                    frequency = frequency,
                    color = color
                )
            }
            repository.insertPlant(plantToSave)
        }
    }
}