package com.rodrigo.androidapp.futtrack.data.repository

import com.google.firebase.firestore.FirebaseFirestore
import com.rodrigo.androidapp.futtrack.domain.model.Team
import com.rodrigo.androidapp.futtrack.domain.repository.TeamRepository
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class TeamRepositoryFirebaseImpl @Inject constructor(
    private val firestore: FirebaseFirestore
) : TeamRepository {

    private val collection = firestore.collection("teams")

    override fun getTeams(): Flow<List<Team>> = callbackFlow {
        val listener = collection.addSnapshotListener { snapshot, error ->
            if (error != null) {
                close(error)
                return@addSnapshotListener
            }

            val teams = snapshot?.documents?.mapNotNull { doc ->
                Team(
                    id = doc.id,
                    name = doc.getString("name") ?: "",
                    isoCode = doc.getString("isoCode") ?: "",
                    crestUrl = doc.getString("crestUrl")
                )
            } ?: emptyList()

            if (teams.isEmpty() && snapshot?.metadata?.hasPendingWrites() == false) {
                seedInitialTeams()
            }

            trySend(teams)
        }

        awaitClose { listener.remove() }
    }

    override suspend fun addTeam(team: Team) {
        val teamMap = hashMapOf(
            "name" to team.name,
            "isoCode" to team.isoCode,
            "crestUrl" to team.crestUrl
        )
        collection.document(team.id).set(teamMap).await()
    }

    override suspend fun deleteTeam(teamId: String) {
        collection.document(teamId).delete().await()
    }
    private fun seedInitialTeams() {
        val alemanhaId = "team_alemanha"
        val brasilId = "team_brasil"
        val italiaId = "team_italia"

        val initialTeams = listOf(
            Team(id = alemanhaId, name = "Alemanha", isoCode = "DE", crestUrl = null),
            Team(id = brasilId, name = "Brasil", isoCode = "BR", crestUrl = null),
            Team(id = italiaId, name = "Itália", isoCode = "IT", crestUrl = null)
        )

        initialTeams.forEach { team ->
            val teamMap = hashMapOf(
                "name" to team.name,
                "isoCode" to team.isoCode,
                "crestUrl" to team.crestUrl
            )
            collection.document(team.id).set(teamMap)
        }
    }
}