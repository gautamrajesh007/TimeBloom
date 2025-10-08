package com.timebloom.app.ui.screens.plantdetail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.timebloom.app.data.local.entity.CheckIn
import com.timebloom.app.data.local.entity.Mood
import com.timebloom.app.data.local.entity.Plant
import com.timebloom.app.data.repository.PlantRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class PlantDetailViewModel(
    private val repository: PlantRepository,
    private val plantId: Long
) : ViewModel() {

    val plant: StateFlow<Plant?> = repository.getPlantById(plantId)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val checkIns: StateFlow<List<CheckIn>> = repository.getCheckInsForPlant(plantId)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun checkInPlant(note: String = "", mood: Mood = Mood.NEUTRAL) {
        viewModelScope.launch {
            repository.checkInPlant(plantId, note, mood)
        }
    }

    fun deletePlant() {
        viewModelScope.launch {
            plant.value?.let { repository.deletePlant(it) }
        }
    }
}