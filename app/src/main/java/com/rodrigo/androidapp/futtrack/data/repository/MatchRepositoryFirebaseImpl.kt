package com.rodrigo.androidapp.futtrack.data.repository

import com.google.firebase.firestore.FirebaseFirestore
import com.rodrigo.androidapp.futtrack.domain.model.Match
import com.rodrigo.androidapp.futtrack.domain.model.MatchStatus
import com.rodrigo.androidapp.futtrack.domain.repository.MatchRepository
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import java.time.LocalDateTime
import java.util.UUID
import javax.inject.Inject

class MatchRepositoryFirebaseImpl @Inject constructor(
    private val firestore: FirebaseFirestore
) : MatchRepository {

    private val collection = firestore.collection("matches")

    override fun getMatches(): Flow<List<Match>> = callbackFlow {
        val listener = collection.addSnapshotListener { snapshot, error ->
            if (error != null) {
                close(error)
                return@addSnapshotListener
            }

            val matches = snapshot?.documents?.mapNotNull { doc ->
                try {
                    Match(
                        id = doc.id,
                        homeTeamId = doc.getString("homeTeamId") ?: "",
                        awayTeamId = doc.getString("awayTeamId") ?: "",
                        homeScore = doc.getLong("homeScore")?.toInt(),
                        awayScore = doc.getLong("awayScore")?.toInt(),
                        status = MatchStatus.valueOf(doc.getString("status") ?: MatchStatus.SCHEDULED.name),
                        date = LocalDateTime.parse(doc.getString("date"))
                    )
                } catch (e: Exception) {
                    null
                }
            } ?: emptyList()

            if (matches.isEmpty() && snapshot?.metadata?.hasPendingWrites() == false) {
                seedHistoricalMatches()
            }

            trySend(matches.sortedByDescending { it.date })
        }

        awaitClose { listener.remove() }
    }

    override suspend fun scheduleMatch(match: Match) {
        val data = hashMapOf(
            "homeTeamId" to match.homeTeamId,
            "awayTeamId" to match.awayTeamId,
            "homeScore" to match.homeScore,
            "awayScore" to match.awayScore,
            "status" to match.status.name,
            "date" to match.date.toString()
        )
        collection.document(match.id).set(data).await()
    }

    override suspend fun updateMatch(match: Match) {
        scheduleMatch(match)
    }

    override suspend fun deleteMatch(matchId: String) {
        collection.document(matchId).delete().await()
    }

    private fun seedHistoricalMatches() {
        val brasilId = "team_brasil"
        val italiaId = "team_italia"
        val alemanhaId = "team_alemanha"

        val historicalDate = LocalDateTime.now().minusDays(1)
        val generatedMatches = mutableListOf<Match>()

        repeat(8) { generatedMatches.add(createDummyMatch(brasilId, italiaId, 1, 0, historicalDate)) }
        repeat(6) { generatedMatches.add(createDummyMatch(brasilId, italiaId, 0, 1, historicalDate)) }
        repeat(6) { generatedMatches.add(createDummyMatch(brasilId, italiaId, 1, 1, historicalDate)) }

        repeat(9) { generatedMatches.add(createDummyMatch(brasilId, alemanhaId, 1, 0, historicalDate)) }
        generatedMatches.add(createDummyMatch(brasilId, alemanhaId, 14, 0, historicalDate)) // Ajuste de GP do Brasil
        repeat(7) { generatedMatches.add(createDummyMatch(brasilId, alemanhaId, 0, 1, historicalDate)) }
        generatedMatches.add(createDummyMatch(brasilId, alemanhaId, 0, 10, historicalDate)) // Ajuste de GP da Alemanha
        repeat(2) { generatedMatches.add(createDummyMatch(brasilId, alemanhaId, 1, 1, historicalDate)) }

        repeat(10) { generatedMatches.add(createDummyMatch(italiaId, alemanhaId, 1, 0, historicalDate)) }
        generatedMatches.add(createDummyMatch(italiaId, alemanhaId, 7, 0, historicalDate)) // Ajuste de GP da Itália
        repeat(6) { generatedMatches.add(createDummyMatch(italiaId, alemanhaId, 0, 1, historicalDate)) }
        generatedMatches.add(createDummyMatch(italiaId, alemanhaId, 0, 3, historicalDate)) // Ajuste de GP da Alemanha
        repeat(2) { generatedMatches.add(createDummyMatch(italiaId, alemanhaId, 1, 1, historicalDate)) }

        val batch = firestore.batch()
        generatedMatches.forEach { match ->
            val docRef = collection.document(match.id)
            val data = hashMapOf(
                "homeTeamId" to match.homeTeamId,
                "awayTeamId" to match.awayTeamId,
                "homeScore" to match.homeScore,
                "awayScore" to match.awayScore,
                "status" to match.status.name,
                "date" to match.date.toString()
            )
            batch.set(docRef, data)
        }
        batch.commit()
    }

    private fun createDummyMatch(home: String, away: String, hScore: Int, aScore: Int, date: LocalDateTime): Match {
        return Match(
            id = UUID.randomUUID().toString(),
            homeTeamId = home,
            awayTeamId = away,
            homeScore = hScore,
            awayScore = aScore,
            status = MatchStatus.FINISHED,
            date = date
        )
    }
}