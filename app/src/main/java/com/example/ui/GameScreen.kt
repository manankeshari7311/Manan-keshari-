package com.example.ui

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.LeaderboardEntry
import com.example.utils.SoundManager

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun GameScreen(
    viewModel: GameViewModel,
    modifier: Modifier = Modifier
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val leaderboard by viewModel.leaderboardFlow.collectAsStateWithLifecycle(initialValue = emptyList())
    val config = SEASONS_LIST.firstOrNull { it.key == state.currentSeason } ?: SEASONS_LIST[0]

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(config.bgColor)
    ) {
        // Decorative background blobs
        BackgroundBlobs(config = config)

        AnimatedContent(
            targetState = state.isLoggedIn,
            transitionSpec = {
                fadeIn(animationSpec = tween(500)) with fadeOut(animationSpec = tween(400))
            },
            label = "ScreenTransition"
        ) { isLoggedIn ->
            if (!isLoggedIn) {
                NameEntryScreen(
                    onSubmit = { viewModel.submitPlayerName(it) },
                    config = config
                )
            } else {
                GameMainContent(
                    state = state,
                    leaderboard = leaderboard,
                    config = config,
                    viewModel = viewModel
                )
            }
        }
    }
}

@Composable
fun BackgroundBlobs(config: SeasonConfig) {
    Box(modifier = Modifier.fillMaxSize()) {
        Box(
            modifier = Modifier
                .offset(x = (-100).dp, y = (-100).dp)
                .size(350.dp)
                .clip(CircleShape)
                .background(config.softColor.copy(alpha = 0.25f))
        )
        Box(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .offset(x = 80.dp, y = 80.dp)
                .size(300.dp)
                .clip(CircleShape)
                .background(config.accentColor.copy(alpha = 0.15f))
        )
    }
}

@Composable
fun NameEntryScreen(
    onSubmit: (String) -> Unit,
    config: SeasonConfig
) {
    var nameInput by remember { mutableStateOf("") }
    val focusManager = LocalFocusManager.current

    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp)
            .windowInsetsPadding(WindowInsets.safeDrawing),
        contentAlignment = Alignment.Center
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .widthIn(max = 420.dp)
                .shadow(16.dp, RoundedCornerShape(28.dp), clip = false),
            shape = RoundedCornerShape(28.dp),
            colors = CardDefaults.cardColors(containerColor = config.cardColor)
        ) {
            Column(
                modifier = Modifier
                    .padding(32.dp)
                    .fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "🍂🌸❄️☀️",
                    fontSize = 52.sp,
                    modifier = Modifier.padding(bottom = 8.dp)
                )

                Text(
                    text = "Seasons Mind Game",
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold,
                    color = config.accentColor,
                    textAlign = TextAlign.Center,
                    fontFamily = FontFamily.Serif
                )

                Text(
                    text = "Memory & Matching Challenge",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium,
                    color = config.textColor.copy(alpha = 0.6f),
                    letterSpacing = 1.sp,
                    modifier = Modifier.padding(top = 4.dp, bottom = 24.dp)
                )

                OutlinedTextField(
                    value = nameInput,
                    onValueChange = { if (it.length <= 20) nameInput = it },
                    placeholder = { Text("Enter your name to begin…", color = config.textColor.copy(alpha = 0.4f)) },
                    singleLine = true,
                    textStyle = TextStyle(color = config.textColor),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = config.accentColor,
                        unfocusedBorderColor = config.textColor.copy(alpha = 0.15f),
                        cursorColor = config.accentColor
                    ),
                    keyboardOptions = KeyboardOptions(
                        imeAction = ImeAction.Done,
                        capitalization = KeyboardCapitalization.Words
                    ),
                    keyboardActions = KeyboardActions(onDone = {
                        focusManager.clearFocus()
                        if (nameInput.trim().isNotEmpty()) {
                            onSubmit(nameInput)
                        }
                    }),
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("player_name_input")
                )

                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = {
                        focusManager.clearFocus()
                        onSubmit(nameInput)
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(54.dp)
                        .testTag("start_playing_btn"),
                    colors = ButtonDefaults.buttonColors(containerColor = config.accentColor),
                    shape = RoundedCornerShape(14.dp),
                    enabled = nameInput.trim().isNotEmpty()
                ) {
                    Text(
                        text = "Start Playing ✦",
                        color = Color.White,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Text(
                    text = "Your high scores will be saved to the leaderboard",
                    fontSize = 11.sp,
                    color = config.textColor.copy(alpha = 0.4f),
                    modifier = Modifier.padding(top = 16.dp),
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

@Composable
fun GameMainContent(
    state: GameUiState,
    leaderboard: List<LeaderboardEntry>,
    config: SeasonConfig,
    viewModel: GameViewModel
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .windowInsetsPadding(WindowInsets.safeDrawing)
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        // Top HUD Row
        TopHUD(state = state, config = config, viewModel = viewModel)

        // LazyColumn to ensure everything scrolls smoothly and stays perfectly adaptive!
        LazyColumn(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(10.dp),
            contentPadding = PaddingValues(vertical = 4.dp)
        ) {
            // Header
            item {
                Column(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "${config.emoji} ${config.title}",
                        fontSize = 32.sp,
                        fontWeight = FontWeight.Black,
                        color = config.accentColor,
                        fontFamily = FontFamily.Serif
                    )
                    Text(
                        text = "MEMORY MIND GAME",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = config.textColor.copy(alpha = 0.5f),
                        letterSpacing = 1.2.sp
                    )
                }
            }

            // Season Selection Tabs
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    SEASONS_LIST.forEach { s ->
                        val isActive = state.currentSeason == s.key
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(40.dp))
                                .background(if (isActive) config.accentColor else config.cardColor)
                                .clickable {
                                    SoundManager.playClick()
                                    viewModel.chooseSeason(s.key)
                                }
                                .padding(vertical = 8.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = s.label,
                                color = if (isActive) Color.White else config.textColor,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                maxLines = 1
                            )
                        }
                    }
                }
            }

            // Level Selection Row
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "LEVEL",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = config.textColor.copy(alpha = 0.5f),
                        letterSpacing = 1.sp
                    )
                    listOf(
                        Pair(1, "1 · Easy"),
                        Pair(2, "2 · Medium"),
                        Pair(3, "3 · Hard")
                    ).forEach { (lvlId, lvlLabel) ->
                        val isActive = state.currentLevel == lvlId
                        val isLocked = when (state.currentSeason) {
                            "spring" -> lvlId > state.springUnlocked
                            "summer" -> lvlId > state.summerUnlocked
                            "autumn" -> lvlId > state.autumnUnlocked
                            else -> lvlId > state.winterUnlocked
                        }

                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(30.dp))
                                .background(
                                    if (isActive) config.textColor
                                    else if (isLocked) config.cardColor.copy(alpha = 0.4f)
                                    else Color.Transparent
                                )
                                .border(
                                    1.dp,
                                    if (isActive) config.textColor else config.textColor.copy(alpha = 0.15f),
                                    RoundedCornerShape(30.dp)
                                )
                                .clickable(enabled = !isLocked) {
                                    SoundManager.playClick()
                                    viewModel.selectLevel(lvlId)
                                }
                                .padding(vertical = 6.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = lvlLabel,
                                    color = if (isLocked) config.textColor.copy(alpha = 0.35f) else if (isActive) config.bgColor else config.textColor,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                if (isLocked) {
                                    Spacer(modifier = Modifier.width(3.dp))
                                    Icon(
                                        imageVector = Icons.Default.Lock,
                                        contentDescription = "Locked",
                                        tint = config.textColor.copy(alpha = 0.35f),
                                        modifier = Modifier.size(10.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Description of active season
            item {
                Text(
                    text = config.desc,
                    fontSize = 13.sp,
                    color = config.textColor.copy(alpha = 0.65f),
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 2.dp)
                )
            }

            // Level Progress Section
            item {
                val totalRequired = when (state.currentLevel) {
                    1 -> 4
                    2 -> 6
                    else -> 8
                }
                val fraction = if (totalRequired > 0) state.pairsFound.toFloat() / totalRequired else 0f
                val animatedProgress by animateFloatAsState(targetValue = fraction, animationSpec = tween(400))

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(config.cardColor.copy(alpha = 0.4f))
                        .padding(10.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "Pairs Collected",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = config.textColor.copy(alpha = 0.5f)
                        )
                        Text(
                            text = "${state.pairsFound} / $totalRequired",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = config.textColor
                        )
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(6.dp)
                            .background(config.bgColor, CircleShape)
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxHeight()
                                .fillMaxWidth(animatedProgress)
                                .background(config.accentColor, CircleShape)
                        )
                    }
                }
            }

            // Quick Stats Cards
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    StatCard(
                        value = state.movesCount.toString(),
                        label = "Moves",
                        modifier = Modifier.weight(1f),
                        config = config
                    )
                    val pairsTotal = when (state.currentLevel) {
                        1 -> 4
                        2 -> 6
                        else -> 8
                    }
                    StatCard(
                        value = "${state.pairsFound}/$pairsTotal",
                        label = "Pairs",
                        modifier = Modifier.weight(1f),
                        config = config
                    )
                    val timeLimits = when (state.currentLevel) {
                        1 -> Pair(20, 40)
                        2 -> Pair(40, 70)
                        else -> Pair(60, 100)
                    }
                    val fastLimit = timeLimits.first
                    val timerLabel = "${state.timerSeconds}s"
                    StatCard(
                        value = timerLabel,
                        label = "Time",
                        modifier = Modifier.weight(1f),
                        config = config,
                        accented = state.timerSeconds <= fastLimit && state.isTimerActive
                    )
                    StatCard(
                        value = state.totalScore.toString(),
                        label = "Points",
                        modifier = Modifier.weight(1.2f),
                        config = config,
                        highlighted = true
                    )
                }
            }

            // Freeze Alert Indicator
            item {
                AnimatedVisibility(
                    visible = state.isFreezeActive,
                    enter = scaleIn() + fadeIn(),
                    exit = scaleOut() + fadeOut()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(Brush.horizontalGradient(listOf(Color(0xFFA8D8F8), Color(0xFF5BB8F5))))
                            .padding(10.dp),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "🧊 Time Frozen! ",
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF0D3A5C),
                            fontSize = 13.sp
                        )
                        Text(
                            text = "${state.freezeRemainingSeconds}s remaining",
                            fontWeight = FontWeight.Black,
                            color = Color(0xFF0D3A5C),
                            fontSize = 13.sp
                        )
                    }
                }
            }

            // Interactive Tool Bar Items
            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(config.cardColor)
                        .padding(10.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "🔧 TOOLS",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = config.textColor.copy(alpha = 0.5f),
                            letterSpacing = 1.sp,
                            modifier = Modifier.weight(1f)
                        )
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(20.dp))
                                .background(config.accentColor.copy(alpha = 0.1f))
                                .clickable {
                                    SoundManager.playClick()
                                    viewModel.toggleShopOverlay(true)
                                }
                                .padding(horizontal = 10.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = "🛒 Tool Shop",
                                color = config.accentColor,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        ToolButton(
                            id = "smasher",
                            name = "Smasher",
                            emoji = "🔍",
                            owned = state.smasherCount,
                            isActive = state.isSmasherActive,
                            config = config,
                            onClick = { viewModel.useTool("smasher") },
                            modifier = Modifier.weight(1f)
                        )
                        ToolButton(
                            id = "mirror",
                            name = "Mirror",
                            emoji = "🪞",
                            owned = state.mirrorCount,
                            isActive = false,
                            config = config,
                            onClick = { viewModel.useTool("mirror") },
                            modifier = Modifier.weight(1f)
                        )
                        ToolButton(
                            id = "freeze",
                            name = "Freeze",
                            emoji = "🧊",
                            owned = state.freezeCount,
                            isActive = state.isFreezeActive,
                            config = config,
                            onClick = { viewModel.useTool("freeze") },
                            modifier = Modifier.weight(1f)
                        )
                        ToolButton(
                            id = "shield",
                            name = "Shield",
                            emoji = "🛡️",
                            owned = state.shieldCount,
                            isActive = state.isShieldActive,
                            config = config,
                            onClick = { viewModel.useTool("shield") },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }

            // Live Move Tier visual indicator
            item {
                val tier = getMoveTier(state.movesCount)
                val fillProgress = if (state.movesCount > 0) Math.min(1f, state.movesCount.toFloat() / 20f) else 0f
                val animatedFillProgress by animateFloatAsState(targetValue = fillProgress, animationSpec = tween(300))

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .background(config.cardColor)
                        .padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                "MOVES COMPLETED",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = config.textColor.copy(alpha = 0.5f)
                            )
                            Text(
                                "${state.movesCount} / 20 max",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Black,
                                color = config.textColor
                            )
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(8.dp)
                                .background(config.bgColor, CircleShape)
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxHeight()
                                    .fillMaxWidth(animatedFillProgress)
                                    .background(
                                        when (tier) {
                                            "gold" -> Color(0xFF4CAF82)
                                            "silver" -> Color(0xFFF0A030)
                                            else -> Color(0xFFE05252)
                                        },
                                        CircleShape
                                    )
                            )
                        }
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 4.dp),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("0", fontSize = 8.sp, color = config.textColor.copy(alpha = 0.4f))
                            Text("⭐ Full pts: ≤15", fontSize = 8.sp, color = config.textColor.copy(alpha = 0.4f))
                            Text("◐ 75% pts: ≤20", fontSize = 8.sp, color = config.textColor.copy(alpha = 0.4f))
                            Text("✕ Over: >20", fontSize = 8.sp, color = config.textColor.copy(alpha = 0.4f))
                        }
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(20.dp))
                            .background(
                                when (tier) {
                                    "gold" -> Color(0xFFD4EDDA)
                                    "silver" -> Color(0xFFFFF3CD)
                                    else -> Color(0xFFF8D7DA)
                                }
                            )
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = when (tier) {
                                "gold" -> "⭐ Max Score"
                                "silver" -> "🥈 Good (75%)"
                                else -> "❌ Retrying"
                            },
                            color = when (tier) {
                                "gold" -> Color(0xFF1A6935)
                                "silver" -> Color(0xFF7A5000)
                                else -> Color(0xFF842029)
                            },
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            // Legend collection panel
            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(18.dp))
                        .background(config.cardColor)
                        .padding(12.dp)
                ) {
                    Text(
                        text = "🃏 SEASON ITEMS COLLECTED",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = config.textColor.copy(alpha = 0.5f),
                        letterSpacing = 1.sp,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        config.emojis.forEach { emoji ->
                            val isMatched = state.matchedEmojis.contains(emoji)
                            Box(
                                modifier = Modifier
                                    .size(34.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(if (isMatched) config.softColor else config.bgColor)
                                    .border(
                                        1.dp,
                                        if (isMatched) config.accentColor else Color.Transparent,
                                        RoundedCornerShape(8.dp)
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    emoji,
                                    fontSize = 18.sp,
                                    color = if (isMatched) config.textColor else config.textColor.copy(alpha = 0.25f)
                                )
                            }
                        }
                    }
                }
            }

            // Memory Cards Grid
            item {
                val columnsCount = 4
                val spacedBy = 8.dp
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(
                            // Dynamically size matching container heights depending on columns
                            when (state.currentLevel) {
                                1 -> 180.dp // 8 cards (2 rows)
                                2 -> 250.dp // 12 cards (3 rows)
                                else -> 320.dp // 16 cards (4 rows)
                            }
                        )
                ) {
                    LazyVerticalGrid(
                        columns = GridCells.Fixed(columnsCount),
                        horizontalArrangement = Arrangement.spacedBy(spacedBy),
                        verticalArrangement = Arrangement.spacedBy(spacedBy),
                        userScrollEnabled = false,
                        modifier = Modifier.fillMaxSize()
                    ) {
                        items(state.cards) { card ->
                            MemoryCardItem(
                                card = card,
                                config = config,
                                onClick = { viewModel.flipCard(card.id) }
                            )
                        }
                    }
                }
            }

            // Footer credits/hints
            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 12.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Tap cards to flip • Match within 20 moves • Clear levels to advance",
                        color = config.textColor.copy(alpha = 0.4f),
                        fontSize = 11.sp,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "✦ Created By — Manan Keshari ✦",
                        fontFamily = FontFamily.Serif,
                        fontWeight = FontWeight.Bold,
                        color = config.accentColor.copy(alpha = 0.5f),
                        fontSize = 12.sp,
                        textAlign = TextAlign.Center
                    )
                }
            }
        }

        // Action Toolbar Control Rows
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 4.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Button(
                onClick = {
                    SoundManager.playClick()
                    viewModel.restartGame()
                },
                colors = ButtonDefaults.buttonColors(containerColor = config.cardColor),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.weight(1f)
            ) {
                Text("↻ Restart", color = config.textColor, fontWeight = FontWeight.Bold, fontSize = 13.sp)
            }
            Button(
                onClick = {
                    SoundManager.playClick()
                    viewModel.toggleLeaderboardOverlay(true)
                },
                colors = ButtonDefaults.buttonColors(containerColor = config.softColor),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.weight(1f)
            ) {
                Text("🏆 Board", color = config.textColor, fontWeight = FontWeight.Bold, fontSize = 13.sp)
            }
        }
    }

    // Overlays / Popups
    if (state.showShopOverlay) {
        ShopOverlay(viewModel = viewModel, state = state, config = config)
    }

    if (state.showLeaderboardOverlay) {
        LeaderboardOverlay(
            leaderboard = leaderboard,
            playerName = state.playerName,
            config = config,
            onClose = { viewModel.toggleLeaderboardOverlay(false) }
        )
    }

    if (state.showWinOverlay) {
        WinOverlay(
            state = state,
            config = config,
            onClose = { viewModel.restartGame() },
            viewModel = viewModel
        )
    }

    if (state.showRetryOverlay) {
        RetryOverlay(
            state = state,
            config = config,
            onRetry = { viewModel.restartGame() },
            onSkip = {
                viewModel.toggleShopOverlay(false)
                viewModel.restartGame()
                viewModel.chooseSeason(state.currentSeason)
            }
        )
    }
}

@Composable
fun TopHUD(
    state: GameUiState,
    config: SeasonConfig,
    viewModel: GameViewModel
) {
    var soundOn by remember { mutableStateOf(SoundManager.isEnabled) }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 8.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(config.cardColor)
            .border(1.dp, config.textColor.copy(alpha = 0.05f), RoundedCornerShape(16.dp))
            .padding(horizontal = 12.dp, vertical = 10.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(24.dp)
                    .clip(CircleShape)
                    .background(config.accentColor.copy(alpha = 0.2f))
                    .clickable {
                        SoundManager.playClick()
                        viewModel.logout()
                    },
                contentAlignment = Alignment.Center
            ) {
                Text("👤", fontSize = 12.sp)
            }
            Text(
                state.playerName,
                fontSize = 13.sp,
                color = config.textColor,
                fontWeight = FontWeight.Bold,
                maxLines = 1
            )
        }

        Text(
            text = "Lvl ${state.currentLevel}",
            fontSize = 11.sp,
            color = config.accentColor,
            fontWeight = FontWeight.ExtraBold
        )

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Sound controller
            Box(
                modifier = Modifier
                    .clip(CircleShape)
                    .background(config.bgColor)
                    .clickable {
                        soundOn = !soundOn
                        SoundManager.isEnabled = soundOn
                        SoundManager.playClick()
                    }
                    .padding(4.dp)
            ) {
                Text(
                    text = if (soundOn) "🔊" else "🔇",
                    fontSize = 12.sp
                )
            }

            // Coin points jar
            Row(
                modifier = Modifier
                    .clip(CircleShape)
                    .background(Brush.horizontalGradient(listOf(Color(0xFFF9C74F), Color(0xFFF4A01A))))
                    .padding(horizontal = 8.dp, vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text("💰", fontSize = 12.sp)
                Text(
                    state.totalScore.toString(),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF5A2D00)
                )
            }
        }
    }
}

@Composable
fun StatCard(
    value: String,
    label: String,
    modifier: Modifier = Modifier,
    config: SeasonConfig,
    highlighted: Boolean = false,
    accented: Boolean = false
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (highlighted) config.accentColor else if (accented) config.softColor else config.cardColor
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 10.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = value,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = if (highlighted) Color.White else config.textColor,
                fontFamily = FontFamily.Serif
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = label,
                fontSize = 10.sp,
                fontWeight = FontWeight.Medium,
                color = if (highlighted) Color.White.copy(alpha = 0.75f) else config.textColor.copy(alpha = 0.5f),
                letterSpacing = 0.5.sp
            )
        }
    }
}

@Composable
fun ToolButton(
    id: String,
    name: String,
    emoji: String,
    owned: Int,
    isActive: Boolean,
    config: SeasonConfig,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isOut = owned <= 0
    val dynamicBg = if (isActive) config.accentColor else if (isOut) config.bgColor.copy(alpha = 0.5f) else config.bgColor

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(20.dp))
            .background(dynamicBg)
            .border(
                1.5.dp,
                if (isActive) config.accentColor else config.textColor.copy(alpha = 0.1f),
                RoundedCornerShape(20.dp)
            )
            .clickable(enabled = !isOut || isActive) { onClick() }
            .padding(horizontal = 4.dp, vertical = 6.dp),
        contentAlignment = Alignment.Center
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(emoji, fontSize = 12.sp)
            Text(
                name,
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                color = if (isActive) Color.White else if (isOut) config.textColor.copy(alpha = 0.35f) else config.textColor
            )
            Box(
                modifier = Modifier
                    .size(16.dp)
                    .clip(CircleShape)
                    .background(if (isActive) Color.White.copy(alpha = 0.3f) else config.accentColor),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = owned.toString(),
                    fontSize = 9.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = if (isActive) config.accentColor else Color.White
                )
            }
        }
    }
}

@Composable
fun MemoryCardItem(
    card: MemoryCard,
    config: SeasonConfig,
    onClick: () -> Unit
) {
    val isRevealed = card.isFlipped || card.isMatched || card.isPeeked
    
    // Smooth 3D rotational flip animation
    val rotation by animateFloatAsState(
        targetValue = if (isRevealed) 180f else 0f,
        animationSpec = tween(durationMillis = 400, easing = FastOutSlowInEasing)
    )

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(1f)
            .graphicsLayer {
                rotationY = rotation
                cameraDistance = 12 * density
            }
            .clickable(enabled = !card.isMatched && !card.isFlipped) { onClick() },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isRevealed) Color.White else config.cardColor
        ),
        border = if (card.isWrong) {
            BorderStroke(2.dp, Color(0xFFE05252))
        } else if (card.isMatched) {
            BorderStroke(2.dp, config.accentColor)
        } else if (card.isPeeked) {
            BorderStroke(2.dp, config.softColor)
        } else {
            BorderStroke(1.dp, config.textColor.copy(alpha = 0.08f))
        }
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            if (rotation <= 90f) {
                // Card face down (Mystery sparkle)
                Text(
                    text = "✦",
                    fontSize = 24.sp,
                    color = config.textColor.copy(alpha = 0.3f)
                )
            } else {
                // Card face up (Actual emoji flipped correctly)
                Text(
                    text = card.emoji,
                    fontSize = 32.sp,
                    modifier = Modifier.graphicsLayer {
                        rotationY = 180f // Counter act rotation so it reads normal is not mirrored
                    }
                )
            }
        }
    }
}

@Composable
fun ShopOverlay(
    viewModel: GameViewModel,
    state: GameUiState,
    config: SeasonConfig
) {
    val shopItems = listOf(
        ShopToolItem(
            id = "smasher",
            name = "Smasher Probe",
            emoji = "🔍",
            desc = "Peek at one card's face for 2s. Counts as 0 moves. Strategic scouting helper.",
            price = 500,
            max = 3,
            owned = state.smasherCount
        ),
        ShopToolItem(
            id = "mirror",
            name = "Mirror Match",
            emoji = "🪞",
            desc = "Auto-match a remaining unmatched pair on the board with magical shimmer.",
            price = 1200,
            max = 2,
            owned = state.mirrorCount
        ),
        ShopToolItem(
            id = "freeze",
            name = "Time Freezer",
            emoji = "🧊",
            desc = "Pause the level timer countdown for 5 full seconds. Perfect for tactical planning.",
            price = 750,
            max = 3,
            owned = state.freezeCount
        ),
        ShopToolItem(
            id = "shield",
            name = "Shield Protection",
            emoji = "🛡️",
            desc = "Absorbsthe move increase for next mismatch so it does not scale your move metrics.",
            price = 950,
            max = 2,
            owned = state.shieldCount
        )
    )

    Dialog(
        onDismissRequest = { viewModel.toggleShopOverlay(false) },
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth(0.92f)
                .widthIn(max = 480.dp)
                .wrapContentHeight()
                .padding(16.dp),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = config.bgColor)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "🛒 Tool Shop",
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Bold,
                            color = config.accentColor,
                            fontFamily = FontFamily.Serif
                        )
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("Your Vault: ", fontSize = 11.sp, color = config.textColor.copy(alpha = 0.6f))
                            Text("💰 ${state.totalScore} pts", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFFB07800))
                        }
                    }
                    IconButton(
                        onClick = {
                            SoundManager.playClick()
                            viewModel.toggleShopOverlay(false)
                        },
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(config.cardColor)
                    ) {
                        Icon(imageVector = Icons.Default.Close, contentDescription = "Close", tint = config.textColor, modifier = Modifier.size(16.dp))
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.weight(1f, fill = false)
                ) {
                    items(shopItems) { tool ->
                        val isMax = tool.owned >= tool.max
                        val canAfford = state.totalScore >= tool.price
                        val btnEnabled = canAfford && !isMax

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(16.dp))
                                .background(config.cardColor)
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(46.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(config.bgColor),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(tool.emoji, fontSize = 22.sp)
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(tool.name, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = config.textColor)
                                Text(tool.desc, fontSize = 11.sp, color = config.textColor.copy(alpha = 0.55f), lineHeight = 14.sp)
                                Text("Owned: ${tool.owned} / ${tool.max}", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFF2E7D32))
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            Column(horizontalAlignment = Alignment.End) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text("💰", fontSize = 11.sp)
                                    Text(tool.price.toString(), fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFFB07800))
                                }
                                Spacer(modifier = Modifier.height(6.dp))
                                Button(
                                    onClick = { viewModel.buyTool(tool.id, tool.price) },
                                    enabled = btnEnabled,
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = config.accentColor,
                                        disabledContainerColor = config.textColor.copy(alpha = 0.08f)
                                    ),
                                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                                    shape = RoundedCornerShape(20.dp),
                                    modifier = Modifier.height(28.dp)
                                ) {
                                    Text(
                                        text = if (isMax) "Max" else "Buy",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 11.sp,
                                        color = if (isMax) config.textColor.copy(alpha = 0.35f) else Color.White
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

data class ShopToolItem(
    val id: String,
    val name: String,
    val emoji: String,
    val desc: String,
    val price: Int,
    val max: Int,
    val owned: Int
)

@Composable
fun LeaderboardOverlay(
    leaderboard: List<LeaderboardEntry>,
    playerName: String,
    config: SeasonConfig,
    onClose: () -> Unit
) {
    Dialog(
        onDismissRequest = { onClose() },
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth(0.92f)
                .widthIn(max = 420.dp)
                .fillMaxHeight(0.75f)
                .padding(16.dp),
            shape = RoundedCornerShape(28.dp),
            colors = CardDefaults.cardColors(containerColor = config.bgColor)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "🏆 Leaderboard",
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        color = config.accentColor,
                        fontFamily = FontFamily.Serif
                    )
                    IconButton(
                        onClick = {
                            SoundManager.playClick()
                            onClose()
                        },
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(config.cardColor)
                    ) {
                        Icon(imageVector = Icons.Default.Close, contentDescription = "Close", tint = config.textColor, modifier = Modifier.size(16.dp))
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))

                if (leaderboard.isEmpty()) {
                    Box(
                        modifier = Modifier.weight(1f).fillMaxWidth(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            "No entries yet.\nComplete levels to claim the crown! 👑",
                            fontSize = 14.sp,
                            color = config.textColor.copy(alpha = 0.4f),
                            textAlign = TextAlign.Center
                        )
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        itemsIndexed(leaderboard) { i, s ->
                            val isYou = s.name.lowercase() == playerName.lowercase()
                            val medal = when (i) {
                                0 -> "🥇"
                                1 -> "🥈"
                                2 -> "🥉"
                                else -> "#${i + 1}"
                            }

                            val rowColor = when (i) {
                                0 -> Color(0xFFFFF8E1)
                                1 -> Color(0xFFF5F5F5)
                                2 -> Color(0xFFFFF0E8)
                                else -> config.cardColor
                            }

                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(14.dp))
                                    .background(rowColor)
                                    .padding(vertical = 12.dp, horizontal = 14.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    medal,
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Black,
                                    modifier = Modifier.width(36.dp),
                                    textAlign = TextAlign.Center,
                                    color = config.accentColor
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    s.name,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = config.textColor,
                                    modifier = Modifier.weight(1f)
                                )
                                if (isYou) {
                                    Box(
                                        modifier = Modifier
                                            .padding(end = 8.dp)
                                            .clip(RoundedCornerShape(6.dp))
                                            .background(config.accentColor)
                                            .padding(horizontal = 6.dp, vertical = 2.dp)
                                    ) {
                                        Text("YOU", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = Color.White)
                                    }
                                }
                                Text(
                                    s.score.toString(),
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = config.accentColor
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun WinOverlay(
    state: GameUiState,
    config: SeasonConfig,
    onClose: () -> Unit,
    viewModel: GameViewModel
) {
    Dialog(
        onDismissRequest = { onClose() },
        properties = DialogProperties(dismissOnBackPress = false, dismissOnClickOutside = false)
    ) {
        val tier = state.lastScoreTier
        Card(
            modifier = Modifier
                .fillMaxWidth(0.92f)
                .widthIn(max = 400.dp)
                .wrapContentHeight()
                .padding(16.dp),
            shape = RoundedCornerShape(28.dp),
            colors = CardDefaults.cardColors(containerColor = config.bgColor),
            border = BorderStroke(2.dp, config.accentColor)
        ) {
            Column(
                modifier = Modifier
                    .padding(28.dp)
                    .fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = if (tier == "gold") "🎉" else "😊",
                    fontSize = 54.sp
                )

                Text(
                    text = if (state.currentLevel == 3) "Season Mastered!" else "Level ${state.currentLevel} Clear!",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = config.accentColor,
                    fontFamily = FontFamily.Serif,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(top = 8.dp)
                )

                Text(
                    text = "${config.label} • ${state.movesCount} moves • ${state.timerSeconds} seconds",
                    fontSize = 12.sp,
                    color = config.textColor.copy(alpha = 0.6f),
                    modifier = Modifier.padding(top = 4.dp, bottom = 12.dp),
                    textAlign = TextAlign.Center
                )

                // Match Tier Banner
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(if (tier == "gold") Color(0xFFD4EDDA) else Color(0xFFFFF3CD))
                        .padding(10.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = if (tier == "gold") "⭐ PERFECT! Under 15 moves — Full Points!"
                        else "🥈 GOOD JOB! 75% Points (16-20 moves)",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (tier == "gold") Color(0xFF1A6935) else Color(0xFF7A5000),
                        textAlign = TextAlign.Center
                    )
                }

                Text(
                    text = if (tier == "gold") "100% score modifier applied" else "75% score modifier applied",
                    fontSize = 11.sp,
                    color = config.textColor.copy(alpha = 0.4f),
                    modifier = Modifier.padding(top = 6.dp, bottom = 16.dp)
                )

                // Large point awarded view
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(40.dp))
                        .background(config.accentColor)
                        .padding(horizontal = 24.dp, vertical = 10.dp)
                ) {
                    Text(
                        text = "+${state.lastScoreTotalEarned} pts",
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Black,
                        color = Color.White,
                        fontFamily = FontFamily.Serif
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Breakdown math
                Text(
                    text = "Base ${state.lastScoreBase} + Move Bonus ${state.lastScoreMoveBonus} + Speed Bonus ${state.lastScoreTimeBonus}" +
                            if (tier == "silver") " [x0.75]" else "",
                    fontSize = 10.sp,
                    color = config.textColor.copy(alpha = 0.5f),
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(24.dp))

                Button(
                    onClick = {
                        SoundManager.playClick()
                        viewModel.restartGame()
                        viewModel.selectLevel(if (state.currentLevel < 3) state.currentLevel + 1 else 1)
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                        .testTag("win_advance_btn"),
                    colors = ButtonDefaults.buttonColors(containerColor = config.accentColor),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(
                        text = if (state.currentLevel < 3) "Next Level →" else "Explore Season →",
                        color = Color.White,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

@Composable
fun RetryOverlay(
    state: GameUiState,
    config: SeasonConfig,
    onRetry: () -> Unit,
    onSkip: () -> Unit
) {
    Dialog(
        onDismissRequest = { /* Force action */ },
        properties = DialogProperties(dismissOnBackPress = false, dismissOnClickOutside = false)
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth(0.92f)
                .widthIn(max = 380.dp)
                .wrapContentHeight()
                .padding(16.dp),
            shape = RoundedCornerShape(28.dp),
            colors = CardDefaults.cardColors(containerColor = config.bgColor),
            border = BorderStroke(2.dp, config.accentColor)
        ) {
            Column(
                modifier = Modifier
                    .padding(24.dp)
                    .fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text("😓", fontSize = 54.sp)
                Text(
                    "Too Many Moves!",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = config.accentColor,
                    fontFamily = FontFamily.Serif,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(top = 8.dp)
                )
                Text(
                    "You solved it but finished with ${state.movesCount} moves (limit is 20 for scoring). No coins awarded.",
                    fontSize = 13.sp,
                    lineHeight = 18.sp,
                    color = config.textColor.copy(alpha = 0.65f),
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(top = 6.dp, bottom = 12.dp)
                )

                Row(
                    modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    RetryStatBox(value = state.movesCount.toString(), label = "Moves", modifier = Modifier.weight(1f), config = config)
                    RetryStatBox(value = "${state.timerSeconds}s", label = "Elapsed", modifier = Modifier.weight(1f), config = config)
                    RetryStatBox(value = "+0", label = "Earned", modifier = Modifier.weight(1f), config = config)
                }

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color(0xFFFFF3CD))
                        .padding(10.dp)
                ) {
                    Text(
                        "💡 Tip: Map positions in your memory before quick clicks! Buying Smasher probes or Time Freezers in the shop helps heavily.",
                        fontSize = 11.sp,
                        color = Color(0xFF7A5000),
                        lineHeight = 15.sp
                    )
                }

                Spacer(modifier = Modifier.height(20.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedButton(
                        onClick = {
                            SoundManager.playClick()
                            onSkip()
                        },
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = config.textColor),
                        border = BorderStroke(1.dp, config.textColor.copy(alpha = 0.2f)),
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("Skip →", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    }
                    Button(
                        onClick = {
                            SoundManager.playClick()
                            onRetry()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = config.accentColor),
                        modifier = Modifier
                            .weight(1.3f)
                            .height(48.dp),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("↺ Try Again", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    }
                }
            }
        }
    }
}

@Composable
fun RetryStatBox(value: String, label: String, modifier: Modifier = Modifier, config: SeasonConfig) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(config.cardColor)
            .border(BorderStroke(1.dp, config.textColor.copy(alpha = 0.15f)), RoundedCornerShape(12.dp))
            .padding(vertical = 10.dp, horizontal = 4.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(value, fontSize = 16.sp, fontWeight = FontWeight.Bold, color = config.textColor, fontFamily = FontFamily.Serif)
            Text(label, fontSize = 9.sp, fontWeight = FontWeight.Medium, color = config.textColor.copy(alpha = 0.5f))
        }
    }
}

private fun getMoveTier(moves: Int): String {
    return when {
        moves <= 15 -> "gold"
        moves <= 20 -> "silver"
        else -> "over"
    }
}
