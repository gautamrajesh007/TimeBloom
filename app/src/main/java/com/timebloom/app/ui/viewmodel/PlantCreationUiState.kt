package com.timebloom.app.ui.viewmodel

sealed class PlantCreationUiState {
    object Idle : PlantCreationUiState()
    object Loading : PlantCreationUiState()
    data class Success(val plantId: Long) : PlantCreationUiState()
    data class Error(val message: String) : PlantCreationUiState()
}