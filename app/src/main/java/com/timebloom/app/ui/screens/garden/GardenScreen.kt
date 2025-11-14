package com.timebloom.app.ui.screens.garden

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import com.timebloom.app.data.local.entity.Plant
import com.timebloom.app.ui.components.CheckInDialog
import com.timebloom.app.ui.components.RevivalDialog
import com.timebloom.app.ui.components.SwipeablePlantCard
import com.timebloom.app.utils.PlantGrowthCalculator

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GardenScreen(
    viewModel: GardenViewModel,
    onPlantClick: (Long) -> Unit,
    onAddPlantClick: () -> Unit,
    onStatisticsClick: () -> Unit,
    onSettingsClick: () -> Unit
) {
    val context = LocalContext.current

    // Create a launcher to request the notification permission
    val notificationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        onResult = { isGranted ->
            if (!isGranted) {
                // You can show a toast or dialog explaining why the permission is needed
                Toast.makeText(context, "Notifications are disabled", Toast.LENGTH_SHORT).show()
            }
        }
    )

    // Check and request permission when the screen is first composed
    LaunchedEffect(Unit) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) { // Android 13
            when (ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS)) {
                PackageManager.PERMISSION_DENIED -> {
                    // Request the permission
                    notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                }
                else -> {
                    // Permission is already granted
                }
            }
        }
    }

    val plants by viewModel.plants.collectAsState()
    var showCheckInDialog by remember { mutableStateOf<Long?>(null) }
    var showRevivalDialog by remember { mutableStateOf<Plant?>(null) }


    val exportState by viewModel.exportState.collectAsState()
    LaunchedEffect(exportState) {
        when (exportState) {
            is ExportState.Success -> {
                Toast.makeText(context, "Operation successful!", Toast.LENGTH_SHORT).show()
                viewModel.resetExportState()
            }
            is ExportState.Error -> {
                Toast.makeText(context, (exportState as ExportState.Error).message, Toast.LENGTH_LONG).show()
                viewModel.resetExportState()
            }
            else -> {}
        }
    }

    val checkInState by viewModel.checkInState.collectAsState()
    LaunchedEffect(checkInState) {
        when (val state = checkInState) {
            is CheckInState.Success -> {
                Toast.makeText(context, "Plant checked in successfully!", Toast.LENGTH_SHORT).show()
                viewModel.resetCheckInState()
            }
            is CheckInState.Error -> {
                Toast.makeText(context, (checkInState as CheckInState.Error).message, Toast.LENGTH_LONG).show()
                viewModel.resetCheckInState()
            }
            is CheckInState.NeedsRevival -> {
                showRevivalDialog = state.plant
            }
            else -> {}
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("My Garden 🌱") },
                actions = {
                    IconButton(onClick = onStatisticsClick) {
                        Icon(Icons.Default.BarChart, "Statistics")
                    }
                    IconButton(onClick = onSettingsClick) {
                        Icon(Icons.Default.Settings, "Settings")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = onAddPlantClick) {
                Icon(Icons.Default.Add, "Add Plant")
            }
        }
    ) {
        padding ->
        if (plants.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "🌱",
                        style = MaterialTheme.typography.displayLarge
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "Your garden is empty",
                        style = MaterialTheme.typography.titleLarge
                    )
                    Text(
                        text = "Tap + to plant your first habit!",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        } else {
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                contentPadding = PaddingValues(16.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
            ) {
                items(plants) { plant ->
                    val isCheckInLoading = checkInState is CheckInState.Loading
                    SwipeablePlantCard(
                        plant = plant,
                        onClick = { onPlantClick(plant.id) },
                        onLongClick = { showCheckInDialog = plant.id },
                        onArchive = { viewModel.archivePlant(plant.id) },
                        onDelete = { viewModel.deletePlant(plant.id) }
                    )
                }

            }
        }
    }

    showRevivalDialog?.let { plant ->
        val revivalCost = PlantGrowthCalculator.calculateReviveCost(plant)
        RevivalDialog(
            plantName = plant.name,
            currentRainDrops = plant.rainDrops,
            revivalCost = revivalCost,
            onDismiss = {
                showRevivalDialog = null
                viewModel.resetCheckInState() // Reset state on dismiss
            },
            onRevive = {
                viewModel.revivePlant(plant.id)
                showRevivalDialog = null
                // No need to reset state, revivePlant will set it to Success/Error
            }
        )
    }

    showCheckInDialog?.let { plantId ->
        val plant = plants.find { it.id == plantId }
        if (plant != null) {
            CheckInDialog(
                plantName = plant.name,
                onDismiss = { showCheckInDialog = null },
                onConfirm = { note, mood ->
                    viewModel.checkInPlant(plantId, note, mood)
                    showCheckInDialog = null
                },
            )
        }
    }
}