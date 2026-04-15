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
import kotlinx.coroutines.IO


data class Letter(val text: Char = '$', val point: Int = 0, val letterMultiplier: Int = 1, val wordMultiplier: Int = 1)

enum class Origin {
    Stock, CenterBox
}

val validWords: List<String> = """the of to too and in is it you that he was for on are with as his they be at one
        have this from or had by hot but some what there we can out other were all your when up use word zap
        how said an each she which do their time if will way about many then them would write like so these fin
        her long make thing see him two has look more day could go come did my sound no most number who over
        know water than call first people may down side been now find any new work part take get place made fix
        live where after back little only round man year came show every good me give our under name very through
        just form much great think say help low line before turn cause same mean differ move right boy old too
        does tell sentence set three want air well also play small end put home read hand port large spell add
        even land here must big high such follow act why ask men change went light kind off need house picture
        try us again animal point mother world near build self earth father head stand own page should country
        found answer school grow study still learn plant cover food sun four thought let keep eye never last door
        between city tree cross since hard start might story saw far sea draw left late run don't while press
        close night real life few stop open seem together next white children begin got walk example ease paper
        often always music those both mark book letter until mile river car feet care second group carry took foul
        rain eat room friend began idea fish mountain north once base hear horse cut sure watch color face wood
        main enough plain girl usual young ready above ever red list though feel talk bird soon body dog family
        direct pose leave song measure state product black short numeral class wind question happen complete quac
        ship area half rock order fire south problem piece told knew pass farm top whole king size heard best
        hour better true false during hundred am remember step early hold west ground interest reach fast five sing
        listen six table travel less morning ten simple several vowel toward war lay against pattern slow center
        love person money serve appear road map science rule govern pull cold notice voice fall power town fine
        certain fly unit lead cry dark machine note wait plan figure star box noun field rest correct able pound
        done beauty drive stood contain front teach week final gave green oh quick develop sleep warm free minute
        strong special mind behind clear tail produce fact street inch lot nothing course stay wheel full force
        blue object decide surface deep moon island foot yet busy test record boat common gold possible plane fog
        age dry wonder laugh thousand ago ran check game shape yes hot miss brought heat snow bed bring sit vace
        perhaps fill east weight language among mug cat desk phone window wall floor jump happy sad tall clean dirty
        nose mouth ear leg arm shoe hat coat bus train cup plate bowl fork spoon knife bread cheese milk juice apple
        grape orange yellow purple brown pink gray bad job win lose fun pop mom dad kid toy sky cloud ice cool sweet
        sour taste smell touch soft loud quiet smart nice rich poor glad mad nag boo gay pal lap muk bug sue urn""".lowercase().split(" ")

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

class AppViewModel(val dao: AppDAO): ViewModel() {

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

    private val _numWords = MutableStateFlow(0)
    val numWords:StateFlow<Int> = _numWords.asStateFlow()

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

    init {
        selectRandomLetters()
        viewModelScope.launch(Dispatchers.IO) {
            // This keeps the list updated with the DB automatically
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
            val consonants = (1..(.4*_numberOfLetters.value).roundToInt()).map {
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
               word in validWords
    }

    fun validWordCreated() {
        addNew(
            word = returnString().lowercase(),
            points = _currentScore.value,
            numMoves = _numMoves,
            time = currentTime()
        )

        _totalScore.update { _totalScore.value + _currentScore.value }
        _numWords.value = numWords()
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

    fun confirmSettings(backgroundColor:List<Float>, minimumWordLength:Int, maximumWordLength:Int, numberOfLetters:Int) {
        _backgroundColor.update { backgroundColor}
        _minimumWordLength.update { minimumWordLength }
        _maximumWordLength.update { maximumWordLength }
        _numberOfLetters.update { numberOfLetters }
    }
}