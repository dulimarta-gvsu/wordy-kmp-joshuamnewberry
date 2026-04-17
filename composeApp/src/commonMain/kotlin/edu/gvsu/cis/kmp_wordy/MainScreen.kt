package edu.gvsu.cis.kmp_wordy

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun MainScreen(
    viewModel: AppViewModel,
    onNavigateToSettings: () -> Unit,
    onNavigateToStats: () -> Unit
) {
    Column(
        verticalArrangement = Arrangement.Top,
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .fillMaxSize()
            .padding(top = 100.dp)
    ) {
        Text("Welcome to Wordy!", fontSize = 40.sp)
        WordScreen(viewModel)
        Spacer(modifier = Modifier.height(100.dp))
        Button(onClick = { viewModel.selectRandomLetters() },
            modifier = Modifier.width(225.dp)) {
            Text("Restart", fontSize = 22.sp)
        }
        Spacer(modifier = Modifier.height(10.dp))
        Button(onClick = { onNavigateToSettings() },
            modifier = Modifier.width(225.dp)) {
            Text("Settings", fontSize = 22.sp)
        }
        Spacer(modifier = Modifier.height(10.dp))
        Button(onClick = { onNavigateToStats() },
            modifier = Modifier.width(225.dp)) {
            Text("Stats", fontSize = 22.sp)
        }
    }
}