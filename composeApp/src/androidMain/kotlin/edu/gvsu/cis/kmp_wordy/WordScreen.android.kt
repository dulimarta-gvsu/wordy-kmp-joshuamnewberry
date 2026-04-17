package edu.gvsu.cis.kmp_wordy

import android.content.ClipData
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.draganddrop.dragAndDropSource
import androidx.compose.foundation.draganddrop.dragAndDropTarget
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draganddrop.DragAndDropEvent
import androidx.compose.ui.draganddrop.DragAndDropTarget
import androidx.compose.ui.draganddrop.DragAndDropTransferData
import androidx.compose.ui.draganddrop.toAndroidDragEvent
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.layout.boundsInRoot
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
actual fun WordScreen(viewModel: AppViewModel) {
    GameScreen(Modifier, viewModel)
}

@OptIn(ExperimentalFoundationApi::class, ExperimentalLayoutApi::class)
@Composable
fun GameScreen(modifier: Modifier = Modifier, viewModel: AppViewModel) {
    val stockLetters by viewModel.sourceLetters.collectAsState()
    val arrangedLetters by viewModel.targetLetters.collectAsState()

    val currentScore by viewModel.currentScore.collectAsState()
    val totalScore by viewModel.totalScore.collectAsState()
    val numWords by viewModel.numWords.collectAsState()
    val currentTime by viewModel.currentTime.collectAsState()

    Column(
        verticalArrangement = Arrangement.Top,
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier
            .fillMaxWidth()
            .padding(top = 45.dp)
    ) {

        Text("Current Word Score: $currentScore", fontSize = 22.sp, fontWeight = FontWeight.Bold)
        Text("Total Score: $totalScore", fontSize = 18.sp)
        Text("Words Built: $numWords", fontSize = 18.sp)
        Text("Time Elapsed: $currentTime", fontSize = 22.sp)

        Spacer(modifier = Modifier.height(20.dp))

        Row(
            horizontalArrangement = Arrangement.SpaceEvenly,
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp)
        ) {
            Button(onClick = { viewModel.shuffleLetters() }) {
                Text("Reshuffle")
            }
            Button(
                onClick = {
                    if(viewModel.isValidWord()) {
                        viewModel.validWordCreated()
                    }
                },
                enabled = currentScore > 0 // Button enabled only when score is non-zero
            ) {
                Text("Record Word")
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        LetterGroup(letters = arrangedLetters, groupId = "Top", backgroundColor = viewModel.backgroundColor.collectAsState().value) {
            viewModel.rearrangeLetters(Origin.CenterBox, it.filterNotNull())
        }

        Spacer(modifier = Modifier.height(32.dp))

        LetterGroup(letters = stockLetters, groupId = "Bottom", backgroundColor = viewModel.backgroundColor.collectAsState().value) {
            viewModel.rearrangeLetters(Origin.Stock, it.filterNotNull())
        }
    }
}

@Composable
fun BigLetter(modifier: Modifier = Modifier, letter: Letter?, cellSize: Dp = 48.dp, backgroundColor:List<Float>) {
    val smallTextSize = (cellSize.value * 0.2125).sp
    val largeTextSize = (cellSize.value * 0.625).sp

    val edgePadding = 2.375.dp

    val backColor = Color(
        backgroundColor[0] / 255f,
        backgroundColor[1] / 255f,
        backgroundColor[2] / 255f)

    Box(
        modifier = modifier
            .size(cellSize)
            .border(width = 1.dp, color = Color.Black, shape = RoundedCornerShape(8.dp))
            .background(
                if (letter == null) Color.Transparent else backColor,
                shape = RoundedCornerShape(8.dp)
            )
    ) {
        // Multiplier text
        Text(
            text = when {
                (letter?.letterMultiplier ?: 1) != 1 -> "${letter!!.letterMultiplier}L"
                (letter?.wordMultiplier ?: 1) != 1 -> "${letter!!.wordMultiplier}W"
                else -> ""
            },
            fontSize = smallTextSize,
            lineHeight = smallTextSize,
            color = when {
                backColor.luminance() >= .35 -> Color.Black
                else -> Color.White
            },
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(start = edgePadding, top = edgePadding)
        )
        // Main letter text
        Text(
            text = (letter?.text ?: "").toString(),
            fontSize = largeTextSize,
            color = when {
                backColor.luminance() > .5 -> Color.Black
                else -> Color.White
            },
            modifier = Modifier.align(Alignment.Center)
        )
        // Point value text
        Text(
            text = (letter?.point ?: "").toString(),
            fontSize = smallTextSize,
            lineHeight = smallTextSize,
            color = when {
                backColor.luminance() > .5 -> Color.Black
                else -> Color.White
            },
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(end = edgePadding, bottom = edgePadding)
        )
    }
}

@OptIn(ExperimentalFoundationApi::class, ExperimentalLayoutApi::class)
@Composable
fun LetterGroup(
    modifier: Modifier = Modifier, groupId: String,
    letters: List<Letter?>,
    backgroundColor: List<Float>,
    onRearranged: (List<Letter?>) -> Unit,
) {
    val configuration = LocalConfiguration.current
    val letterSize = (configuration.screenWidthDp.dp - 24.dp) /
            letters.size.coerceAtLeast(1)
    var borderColor by remember { mutableStateOf(Color.LightGray) }
    var boxBound by remember { mutableStateOf(Rect.Zero) }
    var emptyCellIndex by remember { mutableStateOf<Int?>(null) }
    var startDragIndex by remember { mutableStateOf<Int?>(null) }
    var draggedLetter by remember { mutableStateOf<Letter?>(null) }
    val mutLetters = remember { mutableStateListOf<Letter?>() }
    LaunchedEffect(letters) {
        // Recreate the mutable list when the letter list changed
        mutLetters.clear()
        mutLetters.addAll(letters)
    }

    // Convert pointer offset to letter cell index
    fun offsetToIndex(xOffset: Float): Int {
        val N = mutLetters.size
        if (N > 0) {
            val cellWidth = boxBound.width / N
            val offsetFromLeft = xOffset - boxBound.left
            val idx = (offsetFromLeft / cellWidth).toInt()
            return idx.coerceAtMost(N - 1)
        }
        return 0
    }

    val ddTarget = remember {
        object : DragAndDropTarget {
            override fun onDrop(event: DragAndDropEvent): Boolean {
                val ev = event.toAndroidDragEvent()
                val dropData = ev.clipData.getItemAt(0).text.toString()

                val parts = dropData.split("/")
                if (parts.size >= 4) {
                    val text = parts[0].first()
                    val point = parts[1].toInt()
                    val lMult = parts[2].toInt()
                    val wMult = parts[3].toInt()

                    // Reconstruct the full object
                    val letterObject = Letter(
                        text = text,
                        point = point,
                        letterMultiplier = lMult,
                        wordMultiplier = wMult
                    )

                    if (emptyCellIndex != null) {
                        mutLetters[emptyCellIndex!!] = letterObject
                    }
                }

                emptyCellIndex = null
                return true
            }

            override fun onEntered(event: DragAndDropEvent) {
                super.onEntered(event)
                // use darker border
                borderColor = Color.DarkGray
            }

            override fun onExited(event: DragAndDropEvent) {
                super.onExited(event)
                if (emptyCellIndex != null && emptyCellIndex!! < mutLetters.size) {
                    mutLetters.removeAt(emptyCellIndex!!)
                }
                emptyCellIndex = null
                borderColor = Color.LightGray
            }

            override fun onMoved(event: DragAndDropEvent) {
                super.onMoved(event)
                val ev = event.toAndroidDragEvent()
                val pointerIndex = offsetToIndex(ev.x)

                // After pointer exit, emptyCellIndex was set to null
                if (emptyCellIndex == null) {
                    // No empty cell yet, we need to insert one
                    if (mutLetters.isEmpty())
                        mutLetters.add(null)
                    else
                        mutLetters.add(pointerIndex, null)
                } else if (pointerIndex != emptyCellIndex!!) {
                    mutLetters.removeAt(emptyCellIndex!!)
                    mutLetters.add(pointerIndex, null)
                }
                emptyCellIndex = pointerIndex
            }

            override fun onEnded(event: DragAndDropEvent) {
                super.onEnded(event)
                val ev = event.toAndroidDragEvent()
                if (ev.result) {
                    // The letter was dropped
                    onRearranged(mutLetters.toList())
                } else if (startDragIndex != null) {
                    // Dragging gesture did not drop the letter, put the letter back
                    mutLetters.add(startDragIndex!!, draggedLetter)
                }
                emptyCellIndex = null
                startDragIndex = null
                draggedLetter = null
                borderColor = Color.LightGray
            }
        }
    }
    Column {
        Box(
            contentAlignment = Alignment.Center,
            modifier = modifier
                .defaultMinSize(72.dp, minHeight = 72.dp)
                .border(width = 3.dp, color = borderColor, shape = RoundedCornerShape(12.dp))
                .padding(8.dp)
                .dragAndDropTarget(shouldStartDragAndDrop = { true }, target = ddTarget)
        ) {
            LazyRow(modifier = Modifier.onGloballyPositioned {
                boxBound = it.boundsInRoot()
            }) {
                // Can't use only position as key: reordering won't work correctly
                // Can't use only character as key: the list may contain duplicate letters
                itemsIndexed(
                    mutLetters,
                    key = { pos, item -> "$pos-" + (item?.text ?: "#") }) { pos, lx ->
                    BigLetter(
                        letter = lx, cellSize = letterSize.coerceAtMost(80.dp), backgroundColor = backgroundColor,
                        modifier = Modifier
                            .dragAndDropSource(transferData = {
                                startDragIndex = pos
                                draggedLetter = lx
                                mutLetters[pos] = null
                                emptyCellIndex = pos

                                // Pack all 4 properties: text, point, letterMultiplier, wordMultiplier
                                val payload = "${lx?.text ?: "$"}/${lx?.point}/${lx?.letterMultiplier}/${lx?.wordMultiplier}"

                                DragAndDropTransferData(
                                    clipData = ClipData.newPlainText("letter_data", payload)
                                )
                            }))
                }
            }
        }
    }
}