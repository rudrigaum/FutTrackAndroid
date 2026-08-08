package com.rodrigo.androidapp.futtrack.data.repository

import android.util.Log
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.rodrigo.androidapp.futtrack.domain.model.StandingBaseline
import com.rodrigo.androidapp.futtrack.domain.repository.StandingBaselineRepository
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import javax.inject.Inject

class StandingBaselineRepositoryFirebaseImpl @Inject constructor(
    private val firestore: FirebaseFirestore
) : StandingBaselineRepository {

    private val collection = firestore.collection(COLLECTION_NAME)

    override fun getBaselines(): Flow<List<StandingBaseline>> = callbackFlow {

        val listener = collection.addSnapshotListener { snapshot, error ->
            if (error != null) {
                Log.e(
                    TAG,
                    "Failed to read standing baselines",
                    error
                )

                close(error)
                return@addSnapshotListener
            }

            snapshot?.documents?.forEach { document ->
            }

            val baselines = snapshot
                ?.documents
                ?.mapNotNull { document ->
                    document.toStandingBaseline()
                }
                .orEmpty()

            trySend(baselines)
        }

        awaitClose {
            listener.remove()
        }
    }

    private fun DocumentSnapshot.toStandingBaseline(): StandingBaseline? {
        return StandingBaseline(
            teamId = id,
            points = getInt(FIELD_POINTS) ?: return null,
            matchesPlayed = getInt(FIELD_MATCHES_PLAYED) ?: return null,
            wins = getInt(FIELD_WINS) ?: return null,
            draws = getInt(FIELD_DRAWS) ?: return null,
            losses = getInt(FIELD_LOSSES) ?: return null,
            goalsFor = getInt(FIELD_GOALS_FOR) ?: return null,
            goalsAgainst = getInt(FIELD_GOALS_AGAINST) ?: return null
        )
    }

    private fun DocumentSnapshot.getInt(field: String): Int? {
        val value = get(field)

        return when (value) {
            is Number -> value.toInt()
            is String -> value.toIntOrNull()
            else -> null
        }
    }
    private companion object {
        const val TAG = "StandingBaselineRepo"
        const val COLLECTION_NAME = "standingBaselines"
        const val FIELD_POINTS = "points"
        const val FIELD_MATCHES_PLAYED = "matchesPlayed"
        const val FIELD_WINS = "wins"
        const val FIELD_DRAWS = "draws"
        const val FIELD_LOSSES = "losses"
        const val FIELD_GOALS_FOR = "goalsFor"
        const val FIELD_GOALS_AGAINST = "goalsAgainst"
    }
}