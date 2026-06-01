package com.rodrigo.androidapp.futtrack.data.repository

import com.google.firebase.firestore.FirebaseFirestore
import com.rodrigo.androidapp.futtrack.domain.model.Player
import com.rodrigo.androidapp.futtrack.domain.repository.PlayerRepository
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import javax.inject.Inject

class PlayerRepositoryFirebaseImpl @Inject constructor(
    private val firestore: FirebaseFirestore
) : PlayerRepository {

    private val collection = firestore.collection("players")

    override fun getPlayersByTeam(teamId: String): Flow<List<Player>> = callbackFlow {
        val listener = collection.whereEqualTo("teamId", teamId)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }

                val players = snapshot?.documents?.mapNotNull { doc ->
                    Player(
                        id = doc.id,
                        name = doc.getString("name") ?: "",
                        teamId = doc.getString("teamId") ?: "",
                        isGoalkeeper = doc.getBoolean("isGoalkeeper") ?: false,
                        number = doc.getString("number") // Agora lê como String!
                    )
                } ?: emptyList()

                if (players.isEmpty() && snapshot?.metadata?.hasPendingWrites() == false) {
                    seedPlayersForTeam(teamId)
                }

                val sortedPlayers = players.sortedBy { it.number?.toIntOrNull() ?: 999 }
                trySend(sortedPlayers)
            }

        awaitClose { listener.remove() }
    }

    private fun seedPlayersForTeam(teamId: String) {
        val playersToSeed = when (teamId) {
            "team_alemanha" -> getAlemanhaPlayers()
            "team_brasil" -> getBrasilPlayers()
            "team_italia" -> getItaliaPlayers()
            else -> emptyList()
        }

        if (playersToSeed.isEmpty()) return

        val batch = firestore.batch()
        playersToSeed.forEach { player ->
            val docRef = collection.document(player.id)
            val data = hashMapOf(
                "name" to player.name,
                "teamId" to player.teamId,
                "isGoalkeeper" to player.isGoalkeeper,
                "number" to player.number
            )
            batch.set(docRef, data)
        }
        batch.commit()
    }

    // --- LISTAS COM NUMERAÇÃO EM STRING ---
    private fun getAlemanhaPlayers() = listOf(
        Player("p1", "Gentil", "team_alemanha", true, "1"),
        Player("p2", "Thiago", "team_alemanha", true, "99"),
        Player("p3", "Matheus", "team_alemanha", false, "6"),
        Player("p4", "Paulista", "team_alemanha", false, "8"),
        Player("p5", "Ramon", "team_alemanha", false, "5"),
        Player("p6", "Diego", "team_alemanha", false, "4"),
        Player("p7", "Felipe", "team_alemanha", false, "2"),
        Player("p8", "Martinez", "team_alemanha", false, "27"),
        Player("p9", "Jessé", "team_alemanha", false, "10"),
        Player("p10", "Alan", "team_alemanha", false, "14"),
        Player("p11", "Ueliton", "team_alemanha", false, "16"),
        Player("p12", "Jessé Jr.", "team_alemanha", false, "21"),
        Player("p13", "Ybsen", "team_alemanha", false, "9"),
        Player("p14", "Gean", "team_alemanha", false, "11"),
        Player("p15", "Elielson", "team_alemanha", false, "23"),
        Player("p16", "Rafael", "team_alemanha", false, "7"),
        Player("p49", "Cauã", "team_alemanha", false, "20")
    )

    private fun getBrasilPlayers() = listOf(
        Player("p17", "Léo", "team_brasil", true, "22"),
        Player("p18", "Politano", "team_brasil", true, "12"),
        Player("p19", "Galego", "team_brasil", false, "39"),
        Player("p20", "Humberto", "team_brasil", false, "16"),
        Player("p21", "Dida", "team_brasil", false, "28"),
        Player("p22", "Tarick", "team_brasil", false, "11"),
        Player("p23", "Gugão", "team_brasil", false, "9"),
        Player("p24", "Igor", "team_brasil", false, "10"),
        Player("p25", "Riquelme", "team_brasil", false, "14"),
        Player("p26", "Alisson", "team_brasil", false, "72"),
        Player("p27", "Levi", "team_brasil", false, "5"),
        Player("p28", "Lelezinho", "team_brasil", false, "87"),
        Player("p29", "Lelé", "team_brasil", false, "15"),
        Player("p30", "Gabriel", "team_brasil", false, "47"),
        Player("p31", "Hylton", "team_brasil", false, "8"),
        Player("p32", "Pepe", "team_brasil", false, "7"),
        Player("p50", "Pablo", "team_brasil", false, "01")
    )

    private fun getItaliaPlayers() = listOf(
        Player("p33", "Rudrigo", "team_italia", true, "13"),
        Player("p34", "Valdeck", "team_italia", true, "22"),
        Player("p35", "Paulo", "team_italia", false, "16"),
        Player("p36", "Cidinho", "team_italia", false, "6"),
        Player("p37", "Mario", "team_italia", false, "46"),
        Player("p38", "Neto", "team_italia", false, "3"),
        Player("p39", "Cauã", "team_italia", false, "2"),
        Player("p40", "Thiago", "team_italia", false, "10"),
        Player("p41", "Pithon", "team_italia", false, "8"),
        Player("p42", "Nobre", "team_italia", false, "17"),
        Player("p43", "Carlinhos", "team_italia", false, "7"),
        Player("p44", "Tank", "team_italia", false, "9"),
        Player("p45", "Antônio", "team_italia", false, "11"),
        Player("p46", "Ari", "team_italia", false, "99"),
        Player("p47", "Gel", "team_italia", false, "14"),
        Player("p48", "Danilo", "team_italia", false, "25"),
        Player("p51", "Mauricio", "team_italia", false, "20")
    )
}