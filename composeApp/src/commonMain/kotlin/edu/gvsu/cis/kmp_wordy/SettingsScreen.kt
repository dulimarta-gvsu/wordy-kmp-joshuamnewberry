package edu.gvsu.cis.kmp_wordy

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.math.max
import kotlin.math.min
import kotlin.math.roundToInt

@Composable
fun SettingsScreen(viewModel: AppViewModel, onBack:() -> Unit) {
    var redSlider by remember { mutableStateOf(viewModel.backgroundColor.value[0]) }
    var greenSlider by remember { mutableStateOf(viewModel.backgroundColor.value[1]) }
    var blueSlider by remember { mutableStateOf(viewModel.backgroundColor.value[2]) }
    var minimumWordLength by remember { mutableStateOf(viewModel.minimumWordLength.value) }
    var maximumWordLength by remember { mutableStateOf(viewModel.maximumWordLength.value)}
    var numberOfLetters by remember { mutableStateOf(viewModel.numberOfLetters.value) }
    var useFilteredList by remember { mutableStateOf(viewModel.useFilteredList.value) }
    Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 50.dp), horizontalAlignment = Alignment.CenterHorizontally) {
        Spacer(Modifier.padding(30.dp))
        Row(horizontalArrangement = Arrangement.SpaceAround, modifier = Modifier.fillMaxWidth()) {
            Button(onClick = {onBack()}) { Text("Go Back") }
            Button(onClick = {viewModel.confirmSettings(listOf(redSlider, greenSlider, blueSlider),
                minimumWordLength, maximumWordLength, numberOfLetters, useFilteredList)
                onBack()}) { Text("Confirm Changes") }
        }
        Spacer(Modifier.padding(7.5.dp))
        Text(text = "Select Background Color", fontSize = 25.sp, )
        Spacer(Modifier.padding(5.dp))
        Slider(
            value = redSlider,
            onValueChange = { redSlider = it },
            valueRange = 0f..255f,
            colors = SliderDefaults.colors(
                thumbColor = Color.Red,
                activeTrackColor = Color.Red,
                inactiveTrackColor = Color.Red.copy(alpha = 0.24f)
            )
        )
        Slider(
            value = greenSlider,
            onValueChange = { greenSlider = it },
            valueRange = 0f..255f,
            colors = SliderDefaults.colors(
                thumbColor = Color.Green,
                activeTrackColor = Color.Green,
                inactiveTrackColor = Color.Green.copy(alpha = 0.24f)
            )
        )
        Slider(
            value = blueSlider,
            onValueChange = { blueSlider = it },
            valueRange = 0f..255f,
            colors = SliderDefaults.colors(
                thumbColor = Color.Blue,
                activeTrackColor = Color.Blue,
                inactiveTrackColor = Color.Blue.copy(alpha = 0.24f)
            )
        )
        Spacer(Modifier.padding(7.5.dp))
        Box(modifier = Modifier
            .size(75.dp)
            .background(color = Color(redSlider / 255f, greenSlider / 255f, blueSlider / 255f))
            .border(2.dp, Color.Black)
        )
        Spacer(Modifier.padding(12.5.dp))
        Text(text = "Minimum Word Length: $minimumWordLength", fontSize = 20.sp)
        Slider(
            value = minimumWordLength.toFloat(),
            onValueChange = { minimumWordLength = it.roundToInt()
                maximumWordLength = max(minimumWordLength, maximumWordLength)
                numberOfLetters = max(minimumWordLength, numberOfLetters)},
            valueRange = 1f..20f,
            steps = 19,
            colors = SliderDefaults.colors(
                thumbColor = Color.Black,
                activeTrackColor = Color.Black,
                inactiveTrackColor = Color.Black.copy(alpha = 0.24f)
            )
        )
        Spacer(Modifier.padding(5.dp))
        Text(text = "Maximum Word Length: $maximumWordLength", fontSize = 20.sp)
        Slider(
            value = maximumWordLength.toFloat(),
            onValueChange = { maximumWordLength = it.roundToInt()
                numberOfLetters = max(maximumWordLength, numberOfLetters)
                minimumWordLength = min(maximumWordLength, minimumWordLength); },
            valueRange = 1f..20f,
            steps = 19,
            colors = SliderDefaults.colors(
                thumbColor = Color.Black,
                activeTrackColor = Color.Black,
                inactiveTrackColor = Color.Black.copy(alpha = 0.24f)
            )
        )
        Spacer(Modifier.padding(5.dp))
        Text(text = "Number of Letters: $numberOfLetters", fontSize = 20.sp)
        Slider(
            value = numberOfLetters.toFloat(),
            onValueChange = { numberOfLetters = it.roundToInt()
                minimumWordLength = min(numberOfLetters, minimumWordLength)
                maximumWordLength = min(numberOfLetters, maximumWordLength)},
            valueRange = 1f..20f,
            steps = 19,
            colors = SliderDefaults.colors(
                thumbColor = Color.Black,
                activeTrackColor = Color.Black,
                inactiveTrackColor = Color.Black.copy(alpha = 0.24f)
            )
        )
        Row(modifier = Modifier.padding(10.dp),
            verticalAlignment = Alignment.CenterVertically) {
            Checkbox(
                checked = useFilteredList,
                onCheckedChange = { useFilteredList = it }
            )
            Text(text = "Use Filtered List", fontSize = 20.sp)
        }
    }
}