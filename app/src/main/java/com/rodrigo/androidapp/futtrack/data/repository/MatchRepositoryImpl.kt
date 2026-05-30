package com.rodrigo.androidapp.futtrack.data.repository

import com.rodrigo.androidapp.futtrack.data.local.dao.MatchDao
import com.rodrigo.androidapp.futtrack.data.local.entity.toDomain
import com.rodrigo.androidapp.futtrack.data.local.entity.toEntity
import com.rodrigo.androidapp.futtrack.domain.model.Match
import com.rodrigo.androidapp.futtrack.domain.repository.MatchRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class MatchRepositoryImpl @Inject constructor(
    private val dao: MatchDao
) : MatchRepository {

    override fun getMatches(): Flow<List<Match>> {
        return dao.getAllMatches().map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override suspend fun scheduleMatch(match: Match) {
        dao.insertMatch(match.toEntity())
    }

    override suspend fun deleteMatch(matchId: String) {
        dao.deleteMatch(matchId)
    }

    override suspend fun updateMatch(match: Match) {
        dao.insertMatch(match.toEntity())
    }
}