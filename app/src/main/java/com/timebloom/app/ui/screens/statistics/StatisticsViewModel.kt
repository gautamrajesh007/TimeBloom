package com.timebloom.app.ui.screens.statistics

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.timebloom.app.data.repository.PlantRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

data class StatisticsState(
    val totalPlants: Int = 0,
    val totalCheckIns: Int = 0,
    val longestStreak: Int = 0,
    val totalRainDrops: Int = 0,
    val plantsByStage: Map<String, Int> = emptyMap()
)

class StatisticsViewModel(
    private val repository: PlantRepository
) : ViewModel() {

    val statistics: StateFlow<StatisticsState> = repository.allActivePlants
        .map { plants ->
            StatisticsState(
                totalPlants = plants.size,
                totalCheckIns = plants.sumOf { it.totalCheckIns },
                longestStreak = plants.maxOfOrNull { it.longestStreakCount } ?: 0,
                totalRainDrops = plants.sumOf { it.rainDrops },
                plantsByStage = plants.groupBy { it.growthStage.displayName }
                    .mapValues { it.value.size }
            )
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), StatisticsState())
}