package com.timebloom.app.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.timebloom.app.data.local.AppDatabase // Import your AppDatabase
import com.timebloom.app.data.repository.PlantRepository // Import your PlantRepository
import com.timebloom.app.ui.screens.createedit.CreateEditPlantScreen
import com.timebloom.app.ui.screens.garden.GardenScreen
import com.timebloom.app.ui.screens.garden.GardenViewModel // Import your GardenViewModel
import com.timebloom.app.ui.screens.plantdetail.PlantDetailScreen
import com.timebloom.app.ui.screens.settings.SettingsScreen
import com.timebloom.app.ui.screens.statistics.StatisticsScreen

sealed class Screen(val route: String) {
    object Garden : Screen("garden")
    object PlantDetail : Screen("plant_detail/{plantId}") {
        fun createRoute(plantId: Long) = "plant_detail/$plantId"
    }
    object CreateEditPlant : Screen("create_edit_plant?plantId={plantId}") {
        fun createRoute(plantId: Long? = null) =
            if (plantId != null) "create_edit_plant?plantId=$plantId"
            else "create_edit_plant"
    }
    object Statistics : Screen("statistics")
    object Settings : Screen("settings")
}

@Composable
fun NavigationGraph() { // Keeping the original function name
    val navController = rememberNavController()
    val context = LocalContext.current

    // --- Start of added/modified logic from the fix ---
    val database = remember { AppDatabase.getDatabase(context) }
    val repository = remember {
        PlantRepository(
            database.plantDao(),
            database.checkInDao(),
            database.achievementDao()
        )
    }

    // Create GardenViewModel at navigation level to share it
    val gardenViewModel: GardenViewModel = viewModel(
        factory = object : androidx.lifecycle.ViewModelProvider.Factory {
            override fun <T : androidx.lifecycle.ViewModel> create(modelClass: Class<T>): T {
                @Suppress("UNCHECKED_CAST")
                return GardenViewModel(repository = repository, context = context) as T
            }
        }
    )
    // --- End of added/modified logic from the fix ---

    NavHost(navController = navController, startDestination = Screen.Garden.route) {
        composable(Screen.Garden.route) {
            GardenScreen(
                viewModel = gardenViewModel, // Pass the shared instance
                onPlantClick = { plantId ->
                    navController.navigate(Screen.PlantDetail.createRoute(plantId))
                },
                onAddPlantClick = {
                    navController.navigate(Screen.CreateEditPlant.createRoute())
                },
                onStatisticsClick = {
                    navController.navigate(Screen.Statistics.route)
                },
                onSettingsClick = {
                    navController.navigate(Screen.Settings.route)
                }
            )
        }

        composable(
            route = Screen.PlantDetail.route,
            arguments = listOf(navArgument("plantId") { type = NavType.LongType })
        ) { backStackEntry ->
            val plantId = backStackEntry.arguments?.getLong("plantId") ?: return@composable
            PlantDetailScreen(
                plantId = plantId,
                onNavigateBack = { navController.popBackStack() },
                onEditClick = { navController.navigate(Screen.CreateEditPlant.createRoute(plantId)) }
            )
        }

        composable(
            route = Screen.CreateEditPlant.route,
            arguments = listOf(navArgument("plantId") {
                type = NavType.LongType
                nullable = true
                defaultValue = null
            })
        ) { backStackEntry ->
            val plantId = backStackEntry.arguments?.getLong("plantId")?.takeIf { it != -1L }
            CreateEditPlantScreen(
                plantId = plantId,
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(Screen.Statistics.route) {
            StatisticsScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(Screen.Settings.route) {
            SettingsScreen(
                onNavigateBack = { navController.popBackStack() },
                gardenViewModel = gardenViewModel // Pass the shared instance
            )
        }
    }
}