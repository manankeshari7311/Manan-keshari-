package com.example.data

import kotlinx.coroutines.flow.Flow

class GameRepository(private val gameDao: GameDao) {
    val leaderboard: Flow<List<LeaderboardEntry>> = gameDao.getLeaderboard()
    val allProfiles: Flow<List<PlayerProfile>> = gameDao.getAllProfilesFlow()

    suspend fun insertLeaderboard(entry: LeaderboardEntry) {
        gameDao.insertLeaderboard(entry)
    }

    suspend fun getProfile(name: String): PlayerProfile? {
        return gameDao.getProfile(name)
    }

    suspend fun insertProfile(profile: PlayerProfile) {
        gameDao.insertProfile(profile)
    }
}
