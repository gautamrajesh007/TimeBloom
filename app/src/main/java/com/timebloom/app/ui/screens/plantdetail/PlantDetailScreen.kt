package com.timebloom.app.ui.screens.plantdetail

import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.timebloom.app.data.local.AppDatabase
import com.timebloom.app.data.local.entity.GrowthStage
import com.timebloom.app.data.local.entity.Plant
import com.timebloom.app.data.repository.PlantRepository
import com.timebloom.app.ui.components.CheckInDialog
import com.timebloom.app.ui.components.PlantDeadDialog
import com.timebloom.app.ui.components.RevivalDialog
import com.timebloom.app.utils.PlantGrowthCalculator
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlantDetailScreen(
    plantId: Long,
    onNavigateBack: () -> Unit,
    onEditClick: () -> Unit
) {
    val context = LocalContext.current
    val database = remember { AppDatabase.getDatabase(context) }
    val repository = remember {
        PlantRepository(
            database.plantDao(),
            database.checkInDao(),
            database.achievementDao(),
            context.applicationContext
        )
    }
    val viewModel: PlantDetailViewModel = viewModel(
        factory = object : androidx.lifecycle.ViewModelProvider.Factory {
            override fun <T : androidx.lifecycle.ViewModel> create(modelClass: Class<T>): T {
                @Suppress("UNCHECKED_CAST")
                return PlantDetailViewModel(repository, plantId) as T
            }
        }
    )

    val plant by viewModel.plant.collectAsState()
    val checkIns by viewModel.checkIns.collectAsState()
    var showCheckInDialog by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf(false) }
    var showRevivalDialog by remember { mutableStateOf<Plant?>(null) }
    var showPlantDeadDialog by remember { mutableStateOf<Plant?>(null) }

    val checkInState by viewModel.checkInState.collectAsState()
    LaunchedEffect(checkInState) {
        when (val state = checkInState) {
            is CheckInState.Success -> {
                Toast.makeText(context, "Plant checked in successfully!", Toast.LENGTH_SHORT).show()
                viewModel.resetCheckInState()
            }
            is CheckInState.Error -> {
                Toast.makeText(context, state.message, Toast.LENGTH_LONG).show()
                viewModel.resetCheckInState()
            }
            is CheckInState.NeedsRevival -> {
                showRevivalDialog = state.plant
            }
            is CheckInState.NeedsRestart -> {
                showRevivalDialog = state.plant
                viewModel.resetCheckInState()
            }
            else -> {}
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(plant?.name ?: "Plant Detail") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back")
                    }
                },
                actions = {
                    IconButton(onClick = onEditClick) {
                        Icon(Icons.Default.Edit, "Edit")
                    }
                    IconButton(onClick = { showDeleteDialog = true }) {
                        Icon(Icons.Default.Delete, "Delete")
                    }
                }
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = {
                    plant?.let { p ->
                        val isDead = PlantGrowthCalculator.shouldBeDead(p)
                        val isWithering = PlantGrowthCalculator.shouldBeWithering(p)

                        when {
                            isDead -> showPlantDeadDialog = p
                            isWithering -> showRevivalDialog = p
                            else -> showCheckInDialog = true
                        }
                    }
                },
                icon = { Icon(Icons.Default.WaterDrop, "Water") },
                text = { Text("Water Plant") }
            )
        }
    ) {
            padding ->
        plant?.let { p ->
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Plant Visual
                item {
                    val isDead = PlantGrowthCalculator.shouldBeDead(p)
                    val isWithering = PlantGrowthCalculator.shouldBeWithering(p)
                    val displayStage = when {
                        isDead -> GrowthStage.DEAD
                        isWithering -> GrowthStage.WITHERING
                        else -> p.growthStage
                    }


                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.primaryContainer
                        )
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = displayStage.emoji,
                                fontSize = 80.sp
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = displayStage.displayName,
                                style = MaterialTheme.typography.titleLarge
                            )
                        }
                    }
                }

                // Statistics
                item {
                    Card(modifier = Modifier.fillMaxWidth()) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(
                                text = "Statistics",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(12.dp))

                            StatRow("Current Streak", "🔥 ${p.currentStreakCount} days")
                            StatRow("Longest Streak", "⭐ ${p.longestStreakCount} days")
                            StatRow("Total Check-ins", "✅ ${p.totalCheckIns}")
                            StatRow("Rain Drops", "💧 ${p.rainDrops}")
                            StatRow("Difficulty", p.difficulty.name)
                        }
                    }
                }

                // Check-in History
                item {
                    Text(
                        text = "Recent Activity",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }

                items(checkIns.take(10)) { checkIn ->
                    Card(modifier = Modifier.fillMaxWidth()) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
                                        .format(Date(checkIn.timestamp)),
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Medium
                                )
                                if (checkIn.note.isNotEmpty()) {
                                    Text(
                                        text = checkIn.note,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                            Text(
                                text = checkIn.mood.emoji,
                                fontSize = 24.sp
                            )
                        }
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
                    viewModel.resetCheckInState()
                },
                onRevive = {
                    viewModel.revivePlant()
                    showRevivalDialog = null
                }
            )
        }

        showPlantDeadDialog?.let { plant ->
            PlantDeadDialog(
                plantName = plant.name,
                onDismiss = { showPlantDeadDialog = null },
                onRestart = {
                    viewModel.restartPlant(plantId)
                    showPlantDeadDialog = null
                },
                onArchive = {
                    viewModel.archivePlant(plant.id)
                    showPlantDeadDialog = null
                }
            )
        }

        if (showCheckInDialog) {
            plant?.let { p ->
                CheckInDialog(
                    plantName = p.name,
                    onDismiss = { showCheckInDialog = false },
                    onConfirm = { note, mood ->
                        viewModel.checkInPlant(note, mood)
                        showCheckInDialog = false
                    }
                )
            }
        }

        if (showDeleteDialog) {
            AlertDialog(
                onDismissRequest = { showDeleteDialog = false },
                title = { Text("Delete Plant?") },
                text = { Text("This will permanently delete this plant and all its check-in history.") },
                confirmButton = {
                    Button(
                        onClick = {
                            viewModel.deletePlant()
                            onNavigateBack()
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.error
                        )
                    ) {
                        Text("Delete")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showDeleteDialog = false }) {
                        Text("Cancel")
                    }
                }
            )
        }
    }
}

@Composable
fun StatRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium
        )
    }
}