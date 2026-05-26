package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "leaderboard")
data class LeaderboardEntry(
    @PrimaryKey val name: String,
    val score: Int,
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "player_profile")
data class PlayerProfile(
    @PrimaryKey val name: String,
    val totalScore: Int = 0,
    val smasherCount: Int = 2, // Start with 2 free tools of each to showcase them!
    val mirrorCount: Int = 1,
    val freezeCount: Int = 2,
    val shieldCount: Int = 1,
    val springUnlocked: Int = 1,
    val summerUnlocked: Int = 1,
    val autumnUnlocked: Int = 1,
    val winterUnlocked: Int = 1
)
