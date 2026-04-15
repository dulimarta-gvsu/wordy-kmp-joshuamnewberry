package edu.gvsu.cis.kmp_wordy

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController

@Composable
fun App(dao: AppDAO) {
    MaterialTheme {
        val navController = rememberNavController()

        val vm =  AppViewModel(dao = dao)

        NavHost(navController = navController, startDestination = "main") {
            composable("main") {
                MainScreen(
                    viewModel = vm,
                    onNavigateToSettings = { navController.navigate("settings") },
                    onNavigateToStats = { navController.navigate("stats") }
                )
            }
            composable("settings") {
                SettingsScreen(viewModel = vm, onBack = { navController.popBackStack(); vm.selectRandomLetters() })
            }
            composable("stats") {
                StatsScreen(viewModel = vm, onBack = { navController.popBackStack(); vm.selectRandomLetters() })
            }
        }
    }
}