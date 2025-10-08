package com.timebloom.app.ui.screens.garden

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.timebloom.app.data.local.entity.Mood
import com.timebloom.app.data.local.entity.Plant
import com.timebloom.app.data.repository.PlantRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class GardenViewModel(
    private val repository: PlantRepository
) : ViewModel() {

    val plants: StateFlow<List<Plant>> = repository.allActivePlants
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

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
}