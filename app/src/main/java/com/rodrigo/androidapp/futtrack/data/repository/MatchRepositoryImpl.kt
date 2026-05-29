package com.rodrigo.androidapp.futtrack.data.repository

import com.rodrigo.androidapp.futtrack.domain.model.Match
import com.rodrigo.androidapp.futtrack.domain.repository.MatchRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class MatchRepositoryImpl : MatchRepository {
    private val matchesFlow = MutableStateFlow<List<Match>>(emptyList())

    override fun getMatches(): Flow<List<Match>> {
        return matchesFlow.asStateFlow()
    }

    override suspend fun scheduleMatch(match: Match) {
        matchesFlow.update { currentMatches ->
            currentMatches + match
        }
    }

    override suspend fun deleteMatch(matchId: String) {
        matchesFlow.update { currentMatches ->
            currentMatches.filterNot { it.id == matchId }
        }
    }
}