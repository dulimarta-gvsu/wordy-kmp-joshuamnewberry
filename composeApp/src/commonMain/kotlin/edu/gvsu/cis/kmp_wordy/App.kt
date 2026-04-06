package edu.gvsu.cis.kmp_wordy

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController

@Composable
@Preview
fun App() {
    MaterialTheme {
        val navController = rememberNavController()
        val vm: AppViewModel = remember { AppViewModel() }

        NavHost(navController = navController, startDestination = "main") {
            composable("main") {
                // MainScreen is the Parent
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