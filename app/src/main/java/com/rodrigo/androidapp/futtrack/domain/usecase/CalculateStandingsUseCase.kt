package com.rodrigo.androidapp.futtrack.domain.usecase

import com.rodrigo.androidapp.futtrack.domain.model.Match
import com.rodrigo.androidapp.futtrack.domain.model.MatchStatus
import com.rodrigo.androidapp.futtrack.domain.model.StandingBaseline
import com.rodrigo.androidapp.futtrack.domain.model.Team
import com.rodrigo.androidapp.futtrack.domain.model.TeamStanding
import javax.inject.Inject

class CalculateStandingsUseCase @Inject constructor() {

    operator fun invoke(
        teams: List<Team>,
        matches: List<Match>,
        baselines: List<StandingBaseline> = emptyList()
    ): List<TeamStanding> {
        val standings = createInitialStandings(
            teams = teams,
            baselines = baselines
        ).toMutableMap()

        matches
            .asSequence()
            .filter { it.status == MatchStatus.FINISHED }
            .forEach { match ->
                applyMatch(
                    match = match,
                    standings = standings
                )
            }

        return standings.values.sortedWith(standingsComparator)
    }

    private fun createInitialStandings(
        teams: List<Team>,
        baselines: List<StandingBaseline>
    ): Map<String, TeamStanding> {
        val baselineByTeamId = baselines.associateBy { it.teamId }

        return teams.associate { team ->
            val baseline = baselineByTeamId[team.id]

            team.id to TeamStanding(
                team = team,
                points = baseline?.points ?: 0,
                matchesPlayed = baseline?.matchesPlayed ?: 0,
                wins = baseline?.wins ?: 0,
                draws = baseline?.draws ?: 0,
                losses = baseline?.losses ?: 0,
                goalsFor = baseline?.goalsFor ?: 0,
                goalsAgainst = baseline?.goalsAgainst ?: 0
            )
        }
    }

    private fun applyMatch(
        match: Match,
        standings: MutableMap<String, TeamStanding>
    ) {
        val homeScore = match.homeScore ?: return
        val awayScore = match.awayScore ?: return

        val homeStanding = standings[match.homeTeamId] ?: return
        val awayStanding = standings[match.awayTeamId] ?: return

        standings[match.homeTeamId] = updateStanding(
            standing = homeStanding,
            goalsFor = homeScore,
            goalsAgainst = awayScore
        )

        standings[match.awayTeamId] = updateStanding(
            standing = awayStanding,
            goalsFor = awayScore,
            goalsAgainst = homeScore
        )
    }

    private fun updateStanding(
        standing: TeamStanding,
        goalsFor: Int,
        goalsAgainst: Int
    ): TeamStanding {
        val isWin = goalsFor > goalsAgainst
        val isDraw = goalsFor == goalsAgainst
        val isLoss = goalsFor < goalsAgainst

        val earnedPoints = when {
            isWin -> WIN_POINTS
            isDraw -> DRAW_POINTS
            else -> LOSS_POINTS
        }

        return standing.copy(
            points = standing.points + earnedPoints,
            matchesPlayed = standing.matchesPlayed + 1,
            wins = standing.wins + if (isWin) 1 else 0,
            draws = standing.draws + if (isDraw) 1 else 0,
            losses = standing.losses + if (isLoss) 1 else 0,
            goalsFor = standing.goalsFor + goalsFor,
            goalsAgainst = standing.goalsAgainst + goalsAgainst
        )
    }

    private companion object {
        const val WIN_POINTS = 3
        const val DRAW_POINTS = 1
        const val LOSS_POINTS = 0

        val standingsComparator =
            compareByDescending<TeamStanding> { it.points }
                .thenByDescending { it.goalDifference }
                .thenByDescending { it.goalsFor }
                .thenBy { it.team.name }
    }
}