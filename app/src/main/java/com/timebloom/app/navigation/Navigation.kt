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
import com.timebloom.app.data.local.AppDatabase
import com.timebloom.app.data.preferences.UserPreferences
import com.timebloom.app.data.repository.PlantRepository
import com.timebloom.app.ui.screens.createedit.CreateEditPlantScreen
import com.timebloom.app.ui.screens.garden.GardenScreen
import com.timebloom.app.ui.screens.garden.GardenViewModel
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
fun NavigationGraph() {
    val navController = rememberNavController()
    val context = LocalContext.current


    val database = remember { AppDatabase.getDatabase(context) }
    val repository = remember {
        PlantRepository(
            database.plantDao(),
            database.checkInDao(),
            database.achievementDao(),
            context = context.applicationContext
        )
    }

    val userPreferences = remember { UserPreferences(context.applicationContext) }
    val gardenViewModel: GardenViewModel = viewModel(
        factory = object : androidx.lifecycle.ViewModelProvider.Factory {
            override fun <T : androidx.lifecycle.ViewModel> create(modelClass: Class<T>): T {
                @Suppress("UNCHECKED_CAST")
                return GardenViewModel(
                    repository = repository,
                    userPreferences = userPreferences,
                    context = context
                ) as T
            }
        }
    )

    NavHost(navController = navController, startDestination = Screen.Garden.route) {
        composable(Screen.Garden.route) {
            GardenScreen(
                viewModel = gardenViewModel,
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
            arguments = listOf(navArgument("plantId") {
                type = NavType.LongType
                defaultValue = -1L
            })
        ) {
            backStackEntry ->
            val plantId = backStackEntry.arguments?.getLong("plantId") ?.takeIf { it != -1L }
            PlantDetailScreen(
                plantId = plantId ?: 0L,
                onNavigateBack = { navController.popBackStack() },
                onEditClick = {
                    plantId?.let { id ->
                        navController.navigate(Screen.CreateEditPlant.createRoute(id))
                    }
                }
            )
        }

        composable(
            route = Screen.CreateEditPlant.route,
            arguments = listOf(navArgument("plantId") {
                type = NavType.LongType
                defaultValue = -1L
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
                gardenViewModel = gardenViewModel
            )
        }
    }
}