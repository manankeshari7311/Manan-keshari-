package com.example.ui

import androidx.compose.ui.graphics.Color

data class MemoryCard(
    val id: Int,
    val emoji: String,
    val isFlipped: Boolean = false,
    val isMatched: Boolean = false,
    val isPeeked: Boolean = false,
    val isWrong: Boolean = false
)

data class SeasonConfig(
    val key: String,
    val label: String,
    val title: String,
    val emoji: String,
    val desc: String,
    val emojis: List<String>,
    val emojiPts: Map<String, Int>,
    val bgColor: Color,
    val cardColor: Color,
    val accentColor: Color,
    val textColor: Color,
    val softColor: Color
)

data class GameUiState(
    val playerName: String = "",
    val isLoggedIn: Boolean = false,
    val currentSeason: String = "spring",
    val currentLevel: Int = 1,
    val totalScore: Int = 0,
    val movesCount: Int = 0,
    val pairsFound: Int = 0,
    val timerSeconds: Int = 0,
    val isTimerActive: Boolean = false,
    
    // Cards list
    val cards: List<MemoryCard> = emptyList(),
    val chosenEmojis: List<String> = emptyList(),
    val matchedEmojis: Set<String> = emptySet(),
    
    // Overlays
    val showWinOverlay: Boolean = false,
    val showRetryOverlay: Boolean = false,
    val showShopOverlay: Boolean = false,
    val showLeaderboardOverlay: Boolean = false,
    
    // Tools State
    val smasherCount: Int = 2,
    val mirrorCount: Int = 1,
    val freezeCount: Int = 2,
    val shieldCount: Int = 1,
    
    val isSmasherActive: Boolean = false,
    val isFreezeActive: Boolean = false,
    val freezeRemainingSeconds: Int = 0,
    val isShieldActive: Boolean = false,
    
    // Score stats for completed level
    val lastScoreBase: Int = 0,
    val lastScoreMoveBonus: Int = 0,
    val lastScoreTimeBonus: Int = 0,
    val lastScoreTotalEarned: Int = 0,
    val lastScoreTier: String = "", // "gold", "silver", "over"
    
    // Unlocked values
    val springUnlocked: Int = 1,
    val summerUnlocked: Int = 1,
    val autumnUnlocked: Int = 1,
    val winterUnlocked: Int = 1
)

val SEASONS_LIST = listOf(
    SeasonConfig(
        key = "spring",
        label = "🌸 Spring",
        title = "Spring",
        emoji = "🌸",
        desc = "Blooms, butterflies, and fresh beginnings — match the signs of spring.",
        emojis = listOf("🌸","🌼","🌱","🐝","🦋","🌧️","🐣","🌈"),
        emojiPts = mapOf("🌸" to 120, "🌼" to 110, "🌱" to 100, "🐝" to 130, "🦋" to 150, "🌧️" to 90, "🐣" to 140, "🌈" to 160),
        bgColor = Color(0xFFFDF6F0),
        cardColor = Color(0xFFF7E8D8),
        accentColor = Color(0xFFE8837A),
        textColor = Color(0xFF3A1F1A),
        softColor = Color(0xFFF3C5C2)
    ),
    SeasonConfig(
        key = "summer",
        label = "☀️ Summer",
        title = "Summer",
        emoji = "☀️",
        desc = "Sunshine, waves, and golden days — pair up the summer spirit.",
        emojis = listOf("☀️","🏖️","🍦","🌊","🍉","🌴","🦜","🕶️"),
        emojiPts = mapOf("☀️" to 120, "🏖️" to 130, "🍦" to 100, "🌊" to 110, "🍉" to 90, "🌴" to 140, "🦜" to 150, "🕶️" to 160),
        bgColor = Color(0xFFF0F8FF),
        cardColor = Color(0xFFDCEEFF),
        accentColor = Color(0xFF2A7DE1),
        textColor = Color(0xFF0D1F3C),
        softColor = Color(0xFFA8CFF5)
    ),
    SeasonConfig(
        key = "autumn",
        label = "🍂 Autumn",
        title = "Autumn",
        emoji = "🍂",
        desc = "Crisp air, falling leaves, harvest warmth — find every pair.",
        emojis = listOf("🍂","🍁","🎃","🌰","🦊","🕯️","🍎","🌾"),
        emojiPts = mapOf("🍂" to 110, "🍁" to 120, "🎃" to 140, "🌰" to 100, "🦊" to 150, "🕯️" to 130, "🍎" to 90, "🌾" to 160),
        bgColor = Color(0xFFFDF3E7),
        cardColor = Color(0xFFF5DDB5),
        accentColor = Color(0xFFC4641A),
        textColor = Color(0xFF2E1A07),
        softColor = Color(0xFFE8B97A)
    ),
    SeasonConfig(
        key = "winter",
        label = "❄️ Winter",
        title = "Winter",
        emoji = "❄️",
        desc = "Snowflakes, cozy fires, and frosty magic — match the winter wonders.",
        emojis = listOf("❄️","⛄","🧣","🎿","🎄","🦌","🌨️","🔥"),
        emojiPts = mapOf("❄️" to 120, "⛄" to 110, "🧣" to 100, "🎿" to 130, "🎄" to 160, "🦌" to 140, "🌨️" to 150, "🔥" to 90),
        bgColor = Color(0xFFF0F4F8),
        cardColor = Color(0xFFDCE8F3),
        accentColor = Color(0xFF4F7FBF),
        textColor = Color(0xFF0E1E30),
        softColor = Color(0xFFA0BCD8)
    )
)
