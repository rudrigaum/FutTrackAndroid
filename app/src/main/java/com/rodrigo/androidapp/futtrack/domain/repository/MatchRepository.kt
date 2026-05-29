package com.rodrigo.androidapp.futtrack.domain.repository

import com.rodrigo.androidapp.futtrack.domain.model.Match
import kotlinx.coroutines.flow.Flow

interface MatchRepository {
    fun getMatches(): Flow<List<Match>>
    suspend fun scheduleMatch(match: Match)
}