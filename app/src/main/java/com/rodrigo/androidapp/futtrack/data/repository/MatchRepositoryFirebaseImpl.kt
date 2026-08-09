package com.rodrigo.androidapp.futtrack.data.repository

import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.rodrigo.androidapp.futtrack.domain.model.Match
import com.rodrigo.androidapp.futtrack.domain.model.MatchSlot
import com.rodrigo.androidapp.futtrack.domain.model.MatchStatus
import com.rodrigo.androidapp.futtrack.domain.repository.MatchRepository
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import java.time.LocalDateTime
import javax.inject.Inject

class MatchRepositoryFirebaseImpl @Inject constructor(
    private val firestore: FirebaseFirestore
) : MatchRepository {

    private val matchesCollection =
        firestore.collection(MATCHES_COLLECTION)

    override fun getMatches(): Flow<List<Match>> = callbackFlow {
        val listener = matchesCollection.addSnapshotListener { snapshot, error ->
            if (error != null) {
                close(error)
                return@addSnapshotListener
            }

            val matches = snapshot
                ?.documents
                ?.mapNotNull { document ->
                    document.toMatch()
                }
                .orEmpty()
                .sortedWith(matchComparator)

            trySend(matches)
        }

        awaitClose {
            listener.remove()
        }
    }

    override suspend fun scheduleMatch(match: Match) {
        val matchNumber = requireNotNull(match.matchNumber) {
            "Match number is required."
        }

        val slot = requireNotNull(
            MatchSlot.fromMatchNumber(matchNumber)
        ) {
            "Invalid match number: $matchNumber"
        }

        require(match.date.toLocalTime() == slot.startTime) {
            "Match time must match the selected slot."
        }

        val documentId = createMatchDocumentId(match)

        val matchDocument = matchesCollection.document(documentId)

        firestore.runTransaction { transaction ->
            val existingMatch = transaction.get(matchDocument)

            check(!existingMatch.exists()) {
                "Match slot $matchNumber is already occupied for ${
                    match.date.toLocalDate()
                }."
            }

            transaction.set(
                matchDocument,
                match.toFirestoreData()
            )
        }.await()
    }

    override suspend fun updateMatch(match: Match) {
        matchesCollection
            .document(match.id)
            .update(
                mapOf(
                    FIELD_HOME_SCORE to match.homeScore,
                    FIELD_AWAY_SCORE to match.awayScore,
                    FIELD_STATUS to match.status.name
                )
            )
            .await()
    }

    override suspend fun deleteMatch(matchId: String) {
        matchesCollection
            .document(matchId)
            .delete()
            .await()
    }

    private fun DocumentSnapshot.toMatch(): Match? {
        return runCatching {
            val homeTeamId = getString(FIELD_HOME_TEAM_ID)
                ?: return null

            val awayTeamId = getString(FIELD_AWAY_TEAM_ID)
                ?: return null

            val date = getString(FIELD_DATE)
                ?.let(LocalDateTime::parse)
                ?: return null

            val status = getString(FIELD_STATUS)
                ?.let(MatchStatus::valueOf)
                ?: MatchStatus.SCHEDULED

            Match(
                id = id,
                matchNumber = getLong(FIELD_MATCH_NUMBER)?.toInt(),
                homeTeamId = homeTeamId,
                awayTeamId = awayTeamId,
                homeScore = getLong(FIELD_HOME_SCORE)?.toInt(),
                awayScore = getLong(FIELD_AWAY_SCORE)?.toInt(),
                date = date,
                status = status
            )
        }.getOrNull()
    }

    private fun Match.toFirestoreData(): Map<String, Any?> {
        return mapOf(
            FIELD_MATCH_NUMBER to matchNumber,
            FIELD_HOME_TEAM_ID to homeTeamId,
            FIELD_AWAY_TEAM_ID to awayTeamId,
            FIELD_HOME_SCORE to homeScore,
            FIELD_AWAY_SCORE to awayScore,
            FIELD_STATUS to status.name,
            FIELD_DATE to date.toString()
        )
    }

    private fun createMatchDocumentId(match: Match): String {
        return buildString {
            append(match.date.toLocalDate())
            append("_game_")
            append(match.matchNumber)
        }
    }

    private companion object {
        const val MATCHES_COLLECTION = "matches"

        const val FIELD_MATCH_NUMBER = "matchNumber"
        const val FIELD_HOME_TEAM_ID = "homeTeamId"
        const val FIELD_AWAY_TEAM_ID = "awayTeamId"
        const val FIELD_HOME_SCORE = "homeScore"
        const val FIELD_AWAY_SCORE = "awayScore"
        const val FIELD_STATUS = "status"
        const val FIELD_DATE = "date"

        val matchComparator =
            compareBy<Match> { match ->
                match.date.toLocalDate()
            }.thenBy { match ->
                match.matchNumber ?: Int.MAX_VALUE
            }.thenBy { match ->
                match.date
            }
    }
}