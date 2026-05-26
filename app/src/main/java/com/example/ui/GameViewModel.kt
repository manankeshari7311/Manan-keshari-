package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.*
import com.example.utils.SoundManager
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class GameViewModel(
    application: Application,
    private val repository: GameRepository
) : AndroidViewModel(application) {

    private val _uiState = MutableStateFlow(GameUiState())
    val uiState: StateFlow<GameUiState> = _uiState.asStateFlow()

    private var timerJob: Job? = null
    private var freezeJob: Job? = null
    private val flippedCardIndexes = mutableListOf<Int>()
    private var isProcessingCards = false

    // Leaderboard flow loaded directly from DB
    val leaderboardFlow = repository.leaderboard

    init {
        startTimerCoroutine()
    }

    fun submitPlayerName(name: String) {
        val trimmed = name.trim()
        if (trimmed.isEmpty()) return
        viewModelScope.launch {
            val existing = repository.getProfile(trimmed)
            if (existing != null) {
                _uiState.update {
                    it.copy(
                        playerName = existing.name,
                        isLoggedIn = true,
                        totalScore = existing.totalScore,
                        smasherCount = existing.smasherCount,
                        mirrorCount = existing.mirrorCount,
                        freezeCount = existing.freezeCount,
                        shieldCount = existing.shieldCount,
                        springUnlocked = existing.springUnlocked,
                        summerUnlocked = existing.summerUnlocked,
                        autumnUnlocked = existing.autumnUnlocked,
                        winterUnlocked = existing.winterUnlocked
                    )
                }
            } else {
                val newProfile = PlayerProfile(name = trimmed)
                repository.insertProfile(newProfile)
                _uiState.update {
                    it.copy(
                        playerName = trimmed,
                        isLoggedIn = true,
                        totalScore = newProfile.totalScore,
                        smasherCount = newProfile.smasherCount,
                        mirrorCount = newProfile.mirrorCount,
                        freezeCount = newProfile.freezeCount,
                        shieldCount = newProfile.shieldCount,
                        springUnlocked = 1,
                        summerUnlocked = 1,
                        autumnUnlocked = 1,
                        winterUnlocked = 1
                    )
                }
            }
            restartGame()
        }
    }

    fun logout() {
        _uiState.update { GameUiState() }
    }

    fun chooseSeason(seasonKey: String) {
        _uiState.update {
            it.copy(
                currentSeason = seasonKey,
                currentLevel = 1
            )
        }
        restartGame()
    }

    fun selectLevel(level: Int) {
        val state = _uiState.value
        val isUnlocked = when (state.currentSeason) {
            "spring" -> level <= state.springUnlocked
            "summer" -> level <= state.summerUnlocked
            "autumn" -> level <= state.autumnUnlocked
            else -> level <= state.winterUnlocked
        }
        if (isUnlocked) {
            _uiState.update { it.copy(currentLevel = level) }
            restartGame()
        } else {
            SoundManager.playError()
        }
    }

    fun restartGame() {
        flippedCardIndexes.clear()
        isProcessingCards = false
        
        val state = _uiState.value
        val config = SEASONS_LIST.firstOrNull { it.key == state.currentSeason } ?: SEASONS_LIST[0]
        val pairsCount = getPairsCountForLevel(state.currentLevel)

        // Select items and duplicate them for matching
        val shuffledEmojis = config.emojis.shuffled()
        val chosen = shuffledEmojis.take(pairsCount)
        val doubleList = (chosen + chosen).shuffled()

        val newCards = doubleList.mapIndexed { index, emoji ->
            MemoryCard(id = index, emoji = emoji)
        }

        _uiState.update {
            it.copy(
                movesCount = 0,
                pairsFound = 0,
                timerSeconds = 0,
                isTimerActive = false,
                cards = newCards,
                chosenEmojis = chosen,
                matchedEmojis = emptySet(),
                showWinOverlay = false,
                showRetryOverlay = false,
                showShopOverlay = false,
                isSmasherActive = false,
                isFreezeActive = false,
                freezeRemainingSeconds = 0,
                isShieldActive = false
            )
        }
    }

    private fun startTimerCoroutine() {
        timerJob?.cancel()
        timerJob = viewModelScope.launch {
            while (true) {
                delay(1000)
                val state = _uiState.value
                if (state.isTimerActive && !state.isFreezeActive && !state.showWinOverlay && !state.showRetryOverlay) {
                    _uiState.update { it.copy(timerSeconds = it.timerSeconds + 1) }
                }
            }
        }
    }

    fun flipCard(index: Int) {
        val state = _uiState.value
        if (isProcessingCards) return
        if (index < 0 || index >= state.cards.size) return
        val card = state.cards[index]
        if (card.isFlipped || card.isMatched || card.isPeeked) return

        // ── Smasher Peek Mode ──
        if (state.isSmasherActive) {
            peekOneCard(index)
            return
        }

        SoundManager.playClick()

        // Turn on timer on first action
        if (!state.isTimerActive) {
            _uiState.update { it.copy(isTimerActive = true) }
        }

        val updatedCards = state.cards.toMutableList()
        updatedCards[index] = card.copy(isFlipped = true)
        
        _uiState.update { it.copy(cards = updatedCards) }
        flippedCardIndexes.add(index)

        if (flippedCardIndexes.size == 2) {
            isProcessingCards = true
            evaluateMatch()
        }
    }

    private fun peekOneCard(index: Int) {
        viewModelScope.launch {
            val state = _uiState.value
            if (state.smasherCount <= 0) {
                _uiState.update { it.copy(isSmasherActive = false) }
                return@launch
            }

            SoundManager.playPowerup()
            val updatedSmasherCount = state.smasherCount - 1
            
            // Perform Visual Peek
            val tempCards = state.cards.toMutableList()
            tempCards[index] = tempCards[index].copy(isPeeked = true)
            
            _uiState.update {
                it.copy(
                    smasherCount = updatedSmasherCount,
                    isSmasherActive = false,
                    cards = tempCards
                )
            }
            saveProfileState()

            delay(2000)

            _uiState.update { stateNow ->
                val reUpdated = stateNow.cards.toMutableList()
                if (index < reUpdated.size) {
                    reUpdated[index] = reUpdated[index].copy(isPeeked = false)
                }
                stateNow.copy(cards = reUpdated)
            }
        }
    }

    private fun evaluateMatch() {
        viewModelScope.launch {
            delay(400) // Small visual reveal delay before comparison
            val state = _uiState.value
            val idx1 = flippedCardIndexes[0]
            val idx2 = flippedCardIndexes[1]
            val card1 = state.cards[idx1]
            val card2 = state.cards[idx2]

            val updatedCards = state.cards.toMutableList()

            // Calculate live moves increment
            val newMovesCount = state.movesCount + 1

            if (card1.emoji == card2.emoji) {
                // MATCH!
                SoundManager.playMatch()
                updatedCards[idx1] = card1.copy(isFlipped = false, isMatched = true)
                updatedCards[idx2] = card2.copy(isFlipped = false, isMatched = true)

                val newMatchedEmojis = state.matchedEmojis.toMutableSet().apply {
                    add(card1.emoji)
                }
                val newPairsFound = state.pairsFound + 1
                val totalRequired = getPairsCountForLevel(state.currentLevel)

                _uiState.update {
                    it.copy(
                        movesCount = newMovesCount,
                        pairsFound = newPairsFound,
                        matchedEmojis = newMatchedEmojis,
                        cards = updatedCards
                    )
                }

                flippedCardIndexes.clear()
                isProcessingCards = false

                if (newPairsFound == totalRequired) {
                    triggerWinOrRetry()
                }
            } else {
                // MISMATCH
                if (state.isShieldActive) {
                    // Shield protecting player
                    SoundManager.playPowerup() // Shield absorption beep
                    updatedCards[idx1] = card1.copy(isFlipped = false)
                    updatedCards[idx2] = card2.copy(isFlipped = false)

                    _uiState.update {
                        it.copy(
                            isShieldActive = false,
                            cards = updatedCards
                        ) // No move added
                    }
                    flippedCardIndexes.clear()
                    isProcessingCards = false
                } else {
                    // Regular Wrong
                    SoundManager.playWrong()
                    updatedCards[idx1] = card1.copy(isWrong = true)
                    updatedCards[idx2] = card2.copy(isWrong = true)
                    _uiState.update {
                        it.copy(
                            movesCount = newMovesCount,
                            cards = updatedCards
                        )
                    }

                    delay(600) // Retain red wrong colors briefly

                    val restoredCards = _uiState.value.cards.toMutableList()
                    if (idx1 < restoredCards.size) restoredCards[idx1] = restoredCards[idx1].copy(isFlipped = false, isWrong = false)
                    if (idx2 < restoredCards.size) restoredCards[idx2] = restoredCards[idx2].copy(isFlipped = false, isWrong = false)

                    _uiState.update {
                        it.copy(cards = restoredCards)
                    }

                    flippedCardIndexes.clear()
                    isProcessingCards = false

                    // If max moves exceeded and round over, we can auto-resolve, but HTML lets them finish first!
                }
            }
        }
    }

    private fun triggerWinOrRetry() {
        val state = _uiState.value
        val config = SEASONS_LIST.firstOrNull { it.key == state.currentSeason } ?: SEASONS_LIST[0]
        val pairsCount = getPairsCountForLevel(state.currentLevel)

        val baseScore = (LEVEL_SCORE_BASE[state.currentLevel] ?: 100) * pairsCount
        val perfectMoves = pairsCount
        val moveBonus = if (state.movesCount > 0) {
            Math.max(0, Math.round((perfectMoves.toFloat() / state.movesCount) * baseScore * 0.5f))
        } else 0

        val (fastLimit, okLimit) = TIME_BONUS[state.currentLevel] ?: Pair(30, 60)
        val timeBonus = when {
            state.timerSeconds <= fastLimit -> Math.round(baseScore * 0.4f)
            state.timerSeconds <= okLimit -> Math.round(baseScore * 0.2f)
            else -> 0
        }

        val rawTotal = baseScore + moveBonus + timeBonus

        val (tierKey, tierMult) = when {
            state.movesCount <= 15 -> Pair("gold", 1.00f)
            state.movesCount <= 20 -> Pair("silver", 0.75f)
            else -> Pair("over", 0.00f)
        }

        val finalTotal = Math.round(rawTotal * tierMult)

        viewModelScope.launch {
            if (tierKey == "over") {
                // Show Too Many Moves (Retry Screen) - No score added, no level unlocked
                SoundManager.playWrong()
                _uiState.update {
                    it.copy(
                        showRetryOverlay = true,
                        lastScoreBase = baseScore,
                        lastScoreMoveBonus = moveBonus,
                        lastScoreTimeBonus = timeBonus,
                        lastScoreTotalEarned = 0,
                        lastScoreTier = "over"
                    )
                }
            } else {
                SoundManager.playWin()
                
                // Unlock logic
                val nextLevel = state.currentLevel + 1
                var unlockedSpringNow = state.springUnlocked
                var unlockedSummerNow = state.summerUnlocked
                var unlockedAutumnNow = state.autumnUnlocked
                var unlockedWinterNow = state.winterUnlocked

                if (nextLevel <= 3) {
                    when (state.currentSeason) {
                        "spring" -> if (nextLevel > state.springUnlocked) unlockedSpringNow = nextLevel
                        "summer" -> if (nextLevel > state.summerUnlocked) unlockedSummerNow = nextLevel
                        "autumn" -> if (nextLevel > state.autumnUnlocked) unlockedAutumnNow = nextLevel
                        "winter" -> if (nextLevel > state.winterUnlocked) unlockedWinterNow = nextLevel
                    }
                }

                val updatedScore = state.totalScore + finalTotal

                // Save Leaderboard
                repository.insertLeaderboard(LeaderboardEntry(name = state.playerName, score = updatedScore))

                _uiState.update {
                    it.copy(
                        totalScore = updatedScore,
                        lastScoreBase = baseScore,
                        lastScoreMoveBonus = moveBonus,
                        lastScoreTimeBonus = timeBonus,
                        lastScoreTotalEarned = finalTotal,
                        lastScoreTier = tierKey,
                        springUnlocked = unlockedSpringNow,
                        summerUnlocked = unlockedSummerNow,
                        autumnUnlocked = unlockedAutumnNow,
                        winterUnlocked = unlockedWinterNow,
                        showWinOverlay = true
                    )
                }
                saveProfileState()
            }
        }
    }

    fun useTool(toolId: String) {
        val state = _uiState.value
        when (toolId) {
            "smasher" -> {
                if (state.smasherCount > 0) {
                    _uiState.update { it.copy(isSmasherActive = !it.isSmasherActive) }
                } else {
                    SoundManager.playError()
                }
            }
            "mirror" -> {
                if (state.mirrorCount > 0) {
                    activateMirrorTool()
                } else {
                    SoundManager.playError()
                }
            }
            "freeze" -> {
                if (state.freezeCount > 0) {
                    activateFreezeTool()
                } else {
                    SoundManager.playError()
                }
            }
            "shield" -> {
                if (state.shieldCount > 0 && !state.isShieldActive) {
                    SoundManager.playPowerup()
                    _uiState.update {
                        it.copy(
                            shieldCount = it.shieldCount - 1,
                            isShieldActive = true
                        )
                    }
                    saveProfileState()
                } else {
                    SoundManager.playError()
                }
            }
        }
    }

    private fun activateMirrorTool() {
        val state = _uiState.value
        // Find first remaining unmatched pair
        val unmatched = state.cards.filter { !it.isMatched }
        val grouping = unmatched.groupBy { it.emoji }
        val pairEmoji = grouping.filterValues { it.size >= 2 }.keys.firstOrNull()

        if (pairEmoji != null) {
            SoundManager.playPowerup()
            val targetIndexes = state.cards.mapIndexed { i, c -> Pair(i, c) }
                .filter { it.second.emoji == pairEmoji && !it.second.isMatched }
                .map { it.first }

            if (targetIndexes.size >= 2) {
                viewModelScope.launch {
                    isProcessingCards = true
                    val updatedCards = state.cards.toMutableList()
                    val idx1 = targetIndexes[0]
                    val idx2 = targetIndexes[1]

                    // Visual Flip Open
                    updatedCards[idx1] = updatedCards[idx1].copy(isFlipped = true)
                    updatedCards[idx2] = updatedCards[idx2].copy(isFlipped = true)
                    _uiState.update { it.copy(cards = updatedCards) }

                    delay(800)

                    // Finalize match
                    val postCards = _uiState.value.cards.toMutableList()
                    postCards[idx1] = postCards[idx1].copy(isFlipped = false, isMatched = true)
                    postCards[idx2] = postCards[idx2].copy(isFlipped = false, isMatched = true)

                    val newMatched = _uiState.value.matchedEmojis.toMutableSet().apply {
                        add(pairEmoji)
                    }
                    val newPairsCount = _uiState.value.pairsFound + 1
                    val totalRequired = getPairsCountForLevel(_uiState.value.currentLevel)

                    _uiState.update {
                        it.copy(
                            mirrorCount = it.mirrorCount - 1,
                            pairsFound = newPairsCount,
                            matchedEmojis = newMatched,
                            cards = postCards
                        )
                    }
                    saveProfileState()
                    flippedCardIndexes.clear()
                    isProcessingCards = false

                    if (newPairsCount == totalRequired) {
                        triggerWinOrRetry()
                    }
                }
            }
        } else {
            SoundManager.playError()
        }
    }

    private fun activateFreezeTool() {
        val state = _uiState.value
        SoundManager.playPowerup()
        freezeJob?.cancel()
        _uiState.update {
            it.copy(
                freezeCount = it.freezeCount - 1,
                isFreezeActive = true,
                freezeRemainingSeconds = 5
            )
        }
        saveProfileState()

        freezeJob = viewModelScope.launch {
            while (_uiState.value.freezeRemainingSeconds > 0) {
                delay(1000)
                _uiState.update {
                    it.copy(freezeRemainingSeconds = it.freezeRemainingSeconds - 1)
                }
            }
            _uiState.update { it.copy(isFreezeActive = false) }
        }
    }

    fun buyTool(toolId: String, price: Int) {
        val state = _uiState.value
        val maxMap = mapOf("smasher" to 3, "mirror" to 2, "freeze" to 3, "shield" to 2)
        val currentOwned = when (toolId) {
            "smasher" -> state.smasherCount
            "mirror" -> state.mirrorCount
            "freeze" -> state.freezeCount
            else -> state.shieldCount
        }
        val maxAvailable = maxMap[toolId] ?: 3

        if (state.totalScore >= price && currentOwned < maxAvailable) {
            SoundManager.playPowerup() // Coin purchase sound
            _uiState.update {
                val subtracted = it.totalScore - price
                when (toolId) {
                    "smasher" -> it.copy(totalScore = subtracted, smasherCount = it.smasherCount + 1)
                    "mirror" -> it.copy(totalScore = subtracted, mirrorCount = it.mirrorCount + 1)
                    "freeze" -> it.copy(totalScore = subtracted, freezeCount = it.freezeCount + 1)
                    else -> it.copy(totalScore = subtracted, shieldCount = it.shieldCount + 1)
                }
            }
            saveProfileState()
        } else {
            SoundManager.playError()
        }
    }

    private fun saveProfileState() {
        viewModelScope.launch {
            val state = _uiState.value
            repository.insertProfile(
                PlayerProfile(
                    name = state.playerName,
                    totalScore = state.totalScore,
                    smasherCount = state.smasherCount,
                    mirrorCount = state.mirrorCount,
                    freezeCount = state.freezeCount,
                    shieldCount = state.shieldCount,
                    springUnlocked = state.springUnlocked,
                    summerUnlocked = state.summerUnlocked,
                    autumnUnlocked = state.autumnUnlocked,
                    winterUnlocked = state.winterUnlocked
                )
            )
        }
    }

    // Modal triggers
    fun toggleShopOverlay(show: Boolean) {
        _uiState.update { it.copy(showShopOverlay = show) }
    }

    fun toggleLeaderboardOverlay(show: Boolean) {
        _uiState.update { it.copy(showLeaderboardOverlay = show) }
    }

    private fun getPairsCountForLevel(level: Int): Int {
        return when (level) {
            1 -> 4
            2 -> 6
            else -> 8
        }
    }

    private companion object {
        val LEVEL_SCORE_BASE = mapOf(1 to 100, 2 to 200, 3 to 400)
        val TIME_BONUS = mapOf(
            1 to Pair(20, 40),
            2 to Pair(40, 70),
            3 to Pair(60, 100)
        )
    }
}

class GameViewModelFactory(
    private val application: Application,
    private val repository: GameRepository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(GameViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return GameViewModel(application, repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
