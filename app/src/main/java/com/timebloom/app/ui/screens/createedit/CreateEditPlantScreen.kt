package com.timebloom.app.ui.screens.createedit

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
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
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.graphics.toColorInt
import androidx.lifecycle.viewmodel.compose.viewModel
import com.timebloom.app.data.local.AppDatabase
import com.timebloom.app.data.local.entity.Difficulty
import com.timebloom.app.data.local.entity.Frequency
import com.timebloom.app.data.repository.PlantRepository

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateEditPlantScreen(
    plantId: Long?,
    onNavigateBack: () -> Unit
) {
    val context = LocalContext.current
    val database = remember { AppDatabase.getDatabase(context) }
    val repository = remember {
        PlantRepository(
            database.plantDao(),
            database.checkInDao(),
            database.achievementDao()
        )
    }
    val viewModel: CreateEditPlantViewModel = viewModel(
        factory = object : androidx.lifecycle.ViewModelProvider.Factory {
            override fun <T : androidx.lifecycle.ViewModel> create(modelClass: Class<T>): T {
                @Suppress("UNCHECKED_CAST")
                return CreateEditPlantViewModel(repository, plantId) as T
            }
        }
    )

    val plant by viewModel.plant.collectAsState()
    var name by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var selectedDifficulty by remember { mutableStateOf(Difficulty.MEDIUM) }
    var selectedFrequency by remember { mutableStateOf(Frequency.DAILY) }
    var selectedColor by remember { mutableStateOf("#4CAF50") }

    LaunchedEffect(plant) {
        plant?.let {
            name = it.name
            description = it.description
            selectedDifficulty = it.difficulty
            selectedFrequency = it.frequency
            selectedColor = it.color
        }
    }

    val colorOptions = listOf(
        "#4CAF50", "#2196F3", "#FF9800", "#E91E63",
        "#9C27B0", "#FF5722", "#00BCD4", "#8BC34A"
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (plantId == null) "New Plant" else "Edit Plant") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("Plant Name") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            OutlinedTextField(
                value = description,
                onValueChange = { description = it },
                label = { Text("Description (optional)") },
                modifier = Modifier.fillMaxWidth(),
                maxLines = 3
            )

            Text(
                text = "Difficulty",
                style = MaterialTheme.typography.titleMedium
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Difficulty.entries.forEach { diff ->
                    FilterChip(
                        selected = selectedDifficulty == diff,
                        onClick = { selectedDifficulty = diff },
                        label = { Text(diff.name) },
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            Text(
                text = "Frequency",
                style = MaterialTheme.typography.titleMedium
            )
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Frequency.entries.forEach { freq ->
                    FilterChip(
                        selected = selectedFrequency == freq,
                        onClick = { selectedFrequency = freq },
                        label = {
                            Text(
                                when (freq) {
                                    Frequency.DAILY -> "Daily"
                                    Frequency.WEEKLY -> "Weekly"
                                    Frequency.TWICE_WEEKLY -> "Twice a week"
                                    Frequency.THREE_TIMES_WEEKLY -> "Three times a week"
                                }
                            )
                        },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }

            Text(
                text = "Color",
                style = MaterialTheme.typography.titleMedium
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                colorOptions.forEach { colorHex ->
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .clip(CircleShape)
                            .background(Color(colorHex.toColorInt()))
                            .border(
                                width = if (selectedColor == colorHex) 3.dp else 0.dp,
                                color = MaterialTheme.colorScheme.primary,
                                shape = CircleShape
                            )
                            .clickable { selectedColor = colorHex }
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = {
                    if (name.isNotBlank()) {
                        viewModel.savePlant(
                            name = name,
                            description = description,
                            difficulty = selectedDifficulty,
                            frequency = selectedFrequency,
                            color = selectedColor
                        )
                        onNavigateBack()
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = name.isNotBlank()
            ) {
                Text(if (plantId == null) "Create Plant" else "Save Changes")
            }
        }
    }
}