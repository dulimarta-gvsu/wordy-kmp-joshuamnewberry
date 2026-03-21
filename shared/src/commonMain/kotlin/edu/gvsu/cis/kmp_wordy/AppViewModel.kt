package edu.gvsu.cis.kmp_wordy

import com.hoc081098.kmp.viewmodel.ViewModel
import com.hoc081098.kmp.viewmodel.wrapper.NonNullStateFlowWrapper
import com.hoc081098.kmp.viewmodel.wrapper.wrap
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update

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
        often always music those both mark book letter until mile river car feet care second group carry took
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

class AppViewModel: ViewModel() {
    private val _sourceLetters = MutableStateFlow(emptyList<Letter?>())
    val sourceLetters:NonNullStateFlowWrapper<List<Letter?>> = _sourceLetters.stateIn(
        scope = viewModelScope,
        started = SharingStarted.Eagerly,
        initialValue = emptyList<Letter?>())
        .wrap()
    private val _targetLetters = MutableStateFlow(emptyList<Letter?>())
    val targetLetters:NonNullStateFlowWrapper<List<Letter?>> = _targetLetters.stateIn(
        scope = viewModelScope,
        started = SharingStarted.Eagerly,
        initialValue = emptyList<Letter?>())
        .wrap()

    private val _totalScore = MutableStateFlow(0)
    val totalScore:NonNullStateFlowWrapper<Int> = _totalScore.stateIn(
        scope = viewModelScope,
        started = SharingStarted.Eagerly,
        initialValue = 0)
        .wrap()
    private val _currentScore = MutableStateFlow(0)
    val currentScore:NonNullStateFlowWrapper<Int> = _currentScore.stateIn(
        scope = viewModelScope,
        started = SharingStarted.Eagerly,
        initialValue = 0)
        .wrap()

    private val _numWords = MutableStateFlow(0)
    val numWords:NonNullStateFlowWrapper<Int> = _numWords.stateIn(
        scope = viewModelScope,
        started = SharingStarted.Eagerly,
        initialValue = 0)
        .wrap()

    private val _successfulWords = mutableListOf<String>()

    init {
        selectRandomLetters()
    }

    fun selectRandomLetters() {
        _sourceLetters.update {
            // 60% vowels, 40% consonants
            val vowels = (1..6).map {
                "AEIOU".random()
            }
            val consonants = (1..4).map {
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
            }.shuffled()
        }
        _targetLetters.update { emptyList() }
        _currentScore.update { 0 }
    }

    fun rearrangeLetters(group: Origin, arr: List<Letter>) {
        when (group) {
            Origin.Stock -> {
                _sourceLetters.update { arr }
            }
            Origin.CenterBox -> {
                _targetLetters.update { arr }
                calculateScore()
            }
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
        return word in validWords && word !in _successfulWords
    }

    fun validWordCreated() {
        val word = returnString().lowercase()
        _numWords.update { _numWords.value + 1 }
        _totalScore.update { _totalScore.value + _currentScore.value }
        _successfulWords.add(word)

        _targetLetters.update { emptyList() }
        _currentScore.update { 0 }
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

}