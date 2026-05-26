package com.example.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface GameDao {
    @Query("SELECT * FROM leaderboard ORDER BY score DESC, timestamp DESC LIMIT 20")
    fun getLeaderboard(): Flow<List<LeaderboardEntry>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLeaderboard(entry: LeaderboardEntry)

    @Query("SELECT * FROM player_profile WHERE name = :name")
    suspend fun getProfile(name: String): PlayerProfile?

    @Query("SELECT * FROM player_profile")
    fun getAllProfilesFlow(): Flow<List<PlayerProfile>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProfile(profile: PlayerProfile)
}
