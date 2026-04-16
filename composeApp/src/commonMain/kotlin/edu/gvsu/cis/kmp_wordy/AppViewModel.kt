package edu.gvsu.cis.kmp_wordy

import androidx.room.Entity
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlin.math.roundToInt
import kotlin.time.TimeMark
import kotlin.time.TimeSource
import androidx.room.PrimaryKey
import kotlinx.coroutines.Dispatchers
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.get
import io.ktor.serialization.kotlinx.json.json
import kotlinx.coroutines.IO
import kotlin.collections.List
import kotlinx.serialization.Serializable
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logging
import kotlinx.serialization.json.Json


data class Letter(val text: Char = '$', val point: Int = 0, val letterMultiplier: Int = 1, val wordMultiplier: Int = 1)

enum class Origin {
    Stock, CenterBox
}

val letterPoint = mapOf('A' to 1, 'B' to 3, 'C' to 3, 'D' to 2,
    'E' to 1, 'F' to 4, 'G' to 2, 'H' to 4, 'I' to 1, 'J' to 8, 'K' to 5, 'L' to 1,
    'M' to 3, 'N' to 1, 'O' to 1, 'P' to 3, 'Q' to 10, 'R' to 1, 'S' to 1, 'T' to 1,
    'U' to 1, 'V' to 4, 'W' to 4, 'X' to 8, 'Y' to 4, 'Z' to 10)

@Entity
data class GameSession(
    val word: String,
    val points: Int,
    val numMoves: Int,
    val time: Long,
    @PrimaryKey(autoGenerate = true) val sessionID: Int = 0,
)

@Serializable
data class QuoteResponse(
    val quotes: List<Quote>
)

@Serializable
data class Quote(
    val quote: String
)

class AppViewModel(val dao: AppDAO): ViewModel() {
    val client = HttpClient() {
        install(ContentNegotiation) {
            json(Json() {
                ignoreUnknownKeys = true
            })
        }
        install(Logging) {
            level = LogLevel.ALL // Other options: BODY, INFO, HEADERS
        }
    }

    // Stats
    private val _sessionList = MutableStateFlow<List<GameSession>>(emptyList())
    val sessionList: StateFlow<List<GameSession>> = _sessionList.asStateFlow()

    // Letter Lists
    private val _sourceLetters = MutableStateFlow<List<Letter?>>(emptyList())
    val sourceLetters: StateFlow<List<Letter?>> = _sourceLetters.asStateFlow()

    private val _targetLetters = MutableStateFlow<List<Letter?>>(emptyList())
    val targetLetters: StateFlow<List<Letter?>> = _targetLetters.asStateFlow()

    // Current Round Numbers
    private val _totalScore = MutableStateFlow(0)
    val totalScore:StateFlow<Int> = _totalScore.asStateFlow()

    private val _currentScore = MutableStateFlow(0)
    val currentScore:StateFlow<Int> = _currentScore.asStateFlow()

    private var _numMoves: Int = 0

    // UI Settings
    private val _backgroundColor = MutableStateFlow(listOf(0f,255f,0f))
    val backgroundColor:StateFlow<List<Float>> = _backgroundColor.asStateFlow()

    private val _minimumWordLength = MutableStateFlow(2)
    val minimumWordLength:StateFlow<Int> = _minimumWordLength.asStateFlow()

    private val _maximumWordLength = MutableStateFlow(10)
    val maximumWordLength:StateFlow<Int> = _maximumWordLength.asStateFlow()

    private val _numberOfLetters = MutableStateFlow(10)
    val numberOfLetters:StateFlow<Int> = _numberOfLetters.asStateFlow()

    private var _startTime: TimeMark = TimeSource.Monotonic.markNow()

    private val _currentTime = MutableStateFlow(currentTime())
    val currentTime:StateFlow<Long> = _currentTime.asStateFlow()

    private var _sortState: Int = 0

    // Words Lists and settings choice

    private val _validWords = MutableStateFlow<List<String>>(emptyList())

    private val _useFilteredList = MutableStateFlow(true)
    val useFilteredList:StateFlow<Boolean> = _useFilteredList.asStateFlow()

    private val _easyWords = setOf("a", "an", "the", "and", "but", "or", "for", "nor", "on", "at",
        "to", "from", "by", "of", "in", "up", "is", "it", "be", "as", "he", "we", "me", "us", "am", "hi")

    init {
        fetchValidWordsFromAPI()
        selectRandomLetters()
        viewModelScope.launch(Dispatchers.IO) {
            dao.selectAll().collect {
                _sessionList.value = it
            }
        }
        viewModelScope.launch {
            while (true) {
                delay(499)
                _currentTime.value = currentTime()
            }
        }
    }

    fun fetchValidWordsFromAPI() {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val response = client.get("https://dummyjson.com/quotes?limit=10000")

                val quoteResponse = response.body<QuoteResponse>()

                val processedWordsSet = mutableSetOf<String>()

                quoteResponse.quotes.forEach { quoteObj ->
                    val tokens = quoteObj.quote
                        .lowercase()
                        .replace(Regex("[^a-z ]"), "")
                        .split("\\s+".toRegex())

                    tokens.forEach { word ->
                        if (word.isNotBlank() && word.length > 2 && !_easyWords.contains(word)) {
                            processedWordsSet.add(word)
                        }
                    }
                }

                _validWords.value = processedWordsSet.toList()
                println("Successfully loaded ${processedWordsSet.size} unique words!")

            } catch (e: Exception) {
                println("API Fetch Error: ${e.message}")
            }
        }
    }

    fun addNew(word: String, points: Int, numMoves: Int, time: Long) {
        viewModelScope.launch(Dispatchers.IO) {
            dao.insert(GameSession(word, points, numMoves, time))
        }
    }

    fun selectRandomLetters() {
        _sourceLetters.update {
            // 60% vowels, 40% consonants
            val vowels = (1..(.6*_numberOfLetters.value).roundToInt()).map {
                "AEIOU".random()
            }
            val consonants = (1..(_numberOfLetters.value-vowels.size)).map {
                "BCDFGHJKLMNPQRSTVWXYZ".random()
            }
            (vowels + consonants).map {
                val multiplierEnabled = listOf(1,0,0,0,0,0,0,0,0,0).random()
                val letterEnabled = listOf(1,0).random()
                Letter(
                    text = it, point = letterPoint[it]!!,
                    letterMultiplier = if (multiplierEnabled == 1 && letterEnabled == 1) listOf(2, 2, 3, 3, 4).random() else 1,
                    wordMultiplier = if (multiplierEnabled == 1 && letterEnabled == 0) listOf(2, 2, 3, 3, 4).random() else 1
                )
            }
        }
        _targetLetters.update { emptyList() }
        _currentScore.update { 0 }
        _numMoves = 0
        resetTime()
    }

    fun rearrangeLetters(group: Origin, arr: List<Letter>) {
        val sourceBackup = _sourceLetters.value.toList()
        val targetBackup = _targetLetters.value.toList()
        when (group) {
            Origin.Stock -> {
                _sourceLetters.update { arr }
            }
            Origin.CenterBox -> {
                _targetLetters.update { arr }
                calculateScore()
            }
        }
        if((sourceBackup != _sourceLetters.value || targetBackup != _targetLetters.value)
            && _sourceLetters.value.filterNotNull().size
            + _targetLetters.value.filterNotNull().size == _numberOfLetters.value) {
            _numMoves++
        }
    }

    fun returnString(): String {
        var res = ""
        for (char in _targetLetters.value)
            res += char!!.text
        return res
    }

    fun isValidWord(): Boolean {
        val word = returnString().lowercase()
        return word.length >= _minimumWordLength.value &&
               word.length <= _maximumWordLength.value &&
                ((_useFilteredList.value && word in _validWords.value) ||
                    (!_useFilteredList.value && (word in _validWords.value || word in _easyWords)))
    }

    fun validWordCreated() {
        addNew(
            word = returnString().lowercase(),
            points = _currentScore.value,
            numMoves = _numMoves,
            time = currentTime()
        )

        _totalScore.update { _totalScore.value + _currentScore.value }
        selectRandomLetters()
    }

    fun shuffleLetters() {
        _sourceLetters.update { it.shuffled() }
    }

    fun calculateScore() {
        if (isValidWord()) {
            var baseScore = 0
            var totalWordMultiplier = 1

            _targetLetters.value.filterNotNull().forEach { letter ->
                baseScore += (letter.point * letter.letterMultiplier)
                totalWordMultiplier *= letter.wordMultiplier
            }

            _currentScore.update { baseScore * totalWordMultiplier }
        } else {
            _currentScore.update { 0 }
        }
    }

    fun numWords(): Int {
        return sessionList.value.size
    }

    fun resetTime() {
        _startTime = TimeSource.Monotonic.markNow()
        _currentTime.value = 0
    }

    fun currentTime(): Long {
        return _startTime.elapsedNow().inWholeSeconds
    }

    fun sort() {
        when(_sortState) {
            0 -> sortAlphabetical()
            1 -> sortLength()
            2 -> sortPoints()
            3 -> sortTimeAndMoves()
        }
        _sortState = when(_sortState) {
            0 -> 1
            1 -> 2
            2 -> 3
            else -> 0
        }
    }

    fun resetSort() {
        _sortState = 0
    }

    fun sortAlphabetical() {
        viewModelScope.launch(Dispatchers.IO) {
            _sessionList.value = dao.selectAllSortedByAlphabetical()
        }
    }

    fun sortLength() {
        viewModelScope.launch(Dispatchers.IO) {
            _sessionList.value = dao.selectAllSortedByLength()
        }
    }

    fun sortPoints() {
        viewModelScope.launch(Dispatchers.IO) {
            _sessionList.value = dao.selectAllSortedByPoints()
        }
    }

    fun sortTimeAndMoves() {
        viewModelScope.launch(Dispatchers.IO) {
            _sessionList.value = dao.selectAllSortedByTimeAndMoves()
        }
    }

    fun confirmSettings(backgroundColor:List<Float>, minimumWordLength:Int, maximumWordLength:Int, numberOfLetters:Int, useFilteredList: Boolean) {
        _backgroundColor.value = backgroundColor
        _minimumWordLength.value = minimumWordLength
        _maximumWordLength.value = maximumWordLength
        _numberOfLetters.value = numberOfLetters
        _useFilteredList.value = useFilteredList
    }
}