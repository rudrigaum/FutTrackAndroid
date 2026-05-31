package com.rodrigo.androidapp.futtrack.data.repository

import com.rodrigo.androidapp.futtrack.data.local.dao.PlayerDao
import com.rodrigo.androidapp.futtrack.data.local.dao.TeamDao
import com.rodrigo.androidapp.futtrack.data.local.entity.PlayerEntity
import com.rodrigo.androidapp.futtrack.data.local.entity.TeamEntity
import com.rodrigo.androidapp.futtrack.data.local.entity.toDomain
import com.rodrigo.androidapp.futtrack.data.local.entity.toEntity
import com.rodrigo.androidapp.futtrack.domain.model.Team
import com.rodrigo.androidapp.futtrack.domain.repository.TeamRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import javax.inject.Inject

class TeamRepositoryImpl @Inject constructor(
    private val teamDao: TeamDao,
    private val playerDao: PlayerDao
) : TeamRepository {

    override fun getTeams(): Flow<List<Team>> {
        return teamDao.getAllTeams()
            .onEach { entities ->
                if (entities.isEmpty()) {
                    val alemanhaId = "team_alemanha"
                    val brasilId = "team_brasil"
                    val italiaId = "team_italia"

                    val initialTeams = listOf(
                        TeamEntity(id = alemanhaId, name = "Alemanha", isoCode = "ALE", crestUrl = null),
                        TeamEntity(id = brasilId, name = "Brasil", isoCode = "BRA", crestUrl = null),
                        TeamEntity(id = italiaId, name = "Itália", isoCode = "ITA", crestUrl = null)
                    )

                    // Insere os times oficiais
                    initialTeams.forEach { teamDao.insertTeam(it) }

                    // --- CONVOCAÇÃO ALEMANHA ---
                    val alemanhaPlayers = listOf(
                        PlayerEntity(id = "p1", name = "Gentil", teamId = alemanhaId, isGoalkeeper = true),
                        PlayerEntity(id = "p2", name = "Thiago", teamId = alemanhaId, isGoalkeeper = true),
                        PlayerEntity(id = "p3", name = "Matheus", teamId = alemanhaId, isGoalkeeper = false),
                        PlayerEntity(id = "p4", name = "Paulista", teamId = alemanhaId, isGoalkeeper = false),
                        PlayerEntity(id = "p5", name = "Ramon", teamId = alemanhaId, isGoalkeeper = false),
                        PlayerEntity(id = "p6", name = "Diego", teamId = alemanhaId, isGoalkeeper = false),
                        PlayerEntity(id = "p7", name = "Felipe", teamId = alemanhaId, isGoalkeeper = false),
                        PlayerEntity(id = "p8", name = "Martinez", teamId = alemanhaId, isGoalkeeper = false),
                        PlayerEntity(id = "p9", name = "Jessé", teamId = alemanhaId, isGoalkeeper = false),
                        PlayerEntity(id = "p10", name = "Alan", teamId = alemanhaId, isGoalkeeper = false),
                        PlayerEntity(id = "p11", name = "Ueliton", teamId = alemanhaId, isGoalkeeper = false),
                        PlayerEntity(id = "p12", name = "Jessé Jr.", teamId = alemanhaId, isGoalkeeper = false),
                        PlayerEntity(id = "p13", name = "Ybsen", teamId = alemanhaId, isGoalkeeper = false),
                        PlayerEntity(id = "p14", name = "Gean", teamId = alemanhaId, isGoalkeeper = false),
                        PlayerEntity(id = "p15", name = "Elielson", teamId = alemanhaId, isGoalkeeper = false),
                        PlayerEntity(id = "p16", name = "Rafael", teamId = alemanhaId, isGoalkeeper = false),
                        PlayerEntity(id = "p49", name = "Cauã", teamId = alemanhaId, isGoalkeeper = false)
                    )

                    // --- CONVOCAÇÃO BRASIL ---
                    val brasilPlayers = listOf(
                        PlayerEntity(id = "p17", name = "Léo", teamId = brasilId, isGoalkeeper = true),
                        PlayerEntity(id = "p18", name = "Politano", teamId = brasilId, isGoalkeeper = true),
                        PlayerEntity(id = "p19", name = "Galego", teamId = brasilId, isGoalkeeper = false),
                        PlayerEntity(id = "p20", name = "Humberto", teamId = brasilId, isGoalkeeper = false),
                        PlayerEntity(id = "p21", name = "Dida", teamId = brasilId, isGoalkeeper = false),
                        PlayerEntity(id = "p22", name = "Tarick", teamId = brasilId, isGoalkeeper = false),
                        PlayerEntity(id = "p23", name = "Gugão", teamId = brasilId, isGoalkeeper = false),
                        PlayerEntity(id = "p24", name = "Igor", teamId = brasilId, isGoalkeeper = false),
                        PlayerEntity(id = "p25", name = "Riquelme", teamId = brasilId, isGoalkeeper = false),
                        PlayerEntity(id = "p26", name = "Alisson", teamId = brasilId, isGoalkeeper = false),
                        PlayerEntity(id = "p27", name = "Levi", teamId = brasilId, isGoalkeeper = false),
                        PlayerEntity(id = "p28", name = "Lelezinho", teamId = brasilId, isGoalkeeper = false),
                        PlayerEntity(id = "p29", name = "Lelé", teamId = brasilId, isGoalkeeper = false),
                        PlayerEntity(id = "p30", name = "Gabriel", teamId = brasilId, isGoalkeeper = false),
                        PlayerEntity(id = "p31", name = "Hylton", teamId = brasilId, isGoalkeeper = false),
                        PlayerEntity(id = "p32", name = "Pepe", teamId = brasilId, isGoalkeeper = false),
                        PlayerEntity(id = "p50", name = "Pablo", teamId = brasilId, isGoalkeeper = false)
                    )

                    // --- CONVOCAÇÃO ITÁLIA ---
                    val italiaPlayers = listOf(
                        PlayerEntity(id = "p33", name = "Rudrigo", teamId = italiaId, isGoalkeeper = true),
                        PlayerEntity(id = "p34", name = "Valdeck", teamId = italiaId, isGoalkeeper = true),
                        PlayerEntity(id = "p35", name = "Paulo", teamId = italiaId, isGoalkeeper = false),
                        PlayerEntity(id = "p36", name = "Cidinho", teamId = italiaId, isGoalkeeper = false),
                        PlayerEntity(id = "p37", name = "Mario", teamId = italiaId, isGoalkeeper = false),
                        PlayerEntity(id = "p38", name = "Neto", teamId = italiaId, isGoalkeeper = false),
                        PlayerEntity(id = "p39", name = "Cauã", teamId = italiaId, isGoalkeeper = false),
                        PlayerEntity(id = "p40", name = "Thiago", teamId = italiaId, isGoalkeeper = false),
                        PlayerEntity(id = "p41", name = "Pithon", teamId = italiaId, isGoalkeeper = false),
                        PlayerEntity(id = "p42", name = "Nobre", teamId = italiaId, isGoalkeeper = false),
                        PlayerEntity(id = "p43", name = "Carlinhos", teamId = italiaId, isGoalkeeper = false),
                        PlayerEntity(id = "p44", name = "Tank", teamId = italiaId, isGoalkeeper = false),
                        PlayerEntity(id = "p45", name = "Antônio", teamId = italiaId, isGoalkeeper = false),
                        PlayerEntity(id = "p46", name = "Ari", teamId = italiaId, isGoalkeeper = false),
                        PlayerEntity(id = "p47", name = "Gel", teamId = italiaId, isGoalkeeper = false),
                        PlayerEntity(id = "p48", name = "Danilo", teamId = italiaId, isGoalkeeper = false),
                        PlayerEntity(id = "p51", name = "Mauricio", teamId = italiaId, isGoalkeeper = false),
                    )

                    val allPlayers = alemanhaPlayers + brasilPlayers + italiaPlayers
                    playerDao.insertPlayers(allPlayers)
                }
            }
            .map { entities ->
                entities.map { it.toDomain() }
            }
    }

    override suspend fun addTeam(team: Team) {
        teamDao.insertTeam(team.toEntity())
    }

    override suspend fun deleteTeam(teamId: String) {
        teamDao.deleteTeam(teamId)
    }
}