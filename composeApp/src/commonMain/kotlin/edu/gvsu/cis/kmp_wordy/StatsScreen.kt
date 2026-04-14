package edu.gvsu.cis.kmp_wordy

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch

@Composable
fun StatsScreen(viewModel: AppViewModel, onBack: () -> Unit) {
    // Collect your history list from the ViewModel
    val history by viewModel.sessionList.collectAsState()
    viewModel.resetSort()
    Column(modifier = Modifier.fillMaxSize().padding(16.dp).padding(top = 25.dp)) {
        Row(horizontalArrangement = Arrangement.SpaceAround, modifier = Modifier.fillMaxWidth()) {
            Button(onClick = {onBack()}) { Text("Back to Game") }
            Button(onClick = {viewModel.sort()}) { Text("Sort") }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Sorting buttons would go here (Step 4 of your assignment)

        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f) // Takes up remaining space
                .border(1.dp, Color.Gray, RoundedCornerShape(8.dp))
        ) {
            if(history.isNotEmpty()){
                items(history) { session ->
                    StatsRow(viewModel, session)
                }
            }
            else {
                item {
                    StatsEmpty()
                }
            }
        }
    }
}

@Composable
fun StatsRow(viewModel: AppViewModel, session: GameSession) {
    val scope = rememberCoroutineScope()

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically // Keeps everything aligned in the middle row-wise
    ) {
        Column(
            modifier = Modifier.weight(1f),
            horizontalAlignment = Alignment.Start
        ) {
            Text(
                text = session.word.replaceFirstChar { it.uppercase() },
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp
            )
            Text(text = "${session.time}s | ${session.numMoves} moves", fontSize = 12.sp)
        }

        Column(
            modifier = Modifier.weight(1f),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "${session.points} pts",
                color = Color.Blue,
                fontWeight = FontWeight.Bold
            )
        }

        Column(
            modifier = Modifier.weight(1f),
            horizontalAlignment = Alignment.End
        ) {
            Button(
                onClick = {
                    scope.launch {
                        viewModel.dao.removeOne(session)
                    }
                },
                content = {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Delete Item",
                        tint = Color.White
                    )
                }
            )
        }
    }
}

@Composable
fun StatsEmpty() {
    Row (
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center,
        modifier = Modifier.fillMaxWidth().padding(top= 200.dp)
    ) {
        Text("Go Play First!", fontSize = 45.sp)
    }

}