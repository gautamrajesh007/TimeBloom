package com.timebloom.app.ui.screens.plantdetail

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.timebloom.app.data.local.entity.CheckIn
import com.timebloom.app.data.local.entity.Mood
import com.timebloom.app.data.local.entity.Plant
import com.timebloom.app.data.repository.DuplicateCheckInException
import com.timebloom.app.data.repository.PlantRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

sealed class CheckInState {
    object Idle : CheckInState()
    object Loading : CheckInState()
    object Success : CheckInState()
    data class Error(val message: String) : CheckInState()
}

class PlantDetailViewModel(
    private val repository: PlantRepository,
    private val plantId: Long
) : ViewModel() {

    val plant: StateFlow<Plant?> = repository.getPlantById(plantId)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val checkIns: StateFlow<List<CheckIn>> = repository.getCheckInsForPlant(plantId)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _checkInState = MutableStateFlow<CheckInState>(CheckInState.Idle)
    val checkInState: StateFlow<CheckInState> = _checkInState.asStateFlow()

    fun checkInPlant(note: String = "", mood: Mood = Mood.NEUTRAL) {
        viewModelScope.launch {
            // Add try-catch block
            _checkInState.value = CheckInState.Loading
            try {
                repository.checkInPlant(plantId, note, mood)
                _checkInState.value = CheckInState.Success
            } catch (e: DuplicateCheckInException) {
                _checkInState.value = CheckInState.Error(e.message ?: "Already watered today")
                Log.w("PlantDetailViewModel", "Duplicate Check-in: ${e.message}")
            } catch (e: Exception) {
                _checkInState.value = CheckInState.Error(e.localizedMessage ?: "Check-in failed")
                Log.e("PlantDetailViewModel", "Check-in failed", e)
            }
        }
    }

    fun deletePlant() {
        viewModelScope.launch {
            plant.value?.let { repository.deletePlant(it) }
        }
    }

    fun resetCheckInState() {
        _checkInState.value = CheckInState.Idle
    }
}