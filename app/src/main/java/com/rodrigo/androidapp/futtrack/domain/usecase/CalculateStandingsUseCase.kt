package com.rodrigo.androidapp.futtrack.domain.usecase

import com.rodrigo.androidapp.futtrack.domain.model.Match
import com.rodrigo.androidapp.futtrack.domain.model.MatchStatus
import com.rodrigo.androidapp.futtrack.domain.model.Team
import com.rodrigo.androidapp.futtrack.domain.model.TeamStanding
import javax.inject.Inject

class CalculateStandingsUseCase @Inject constructor() {

    operator fun invoke(teams: List<Team>, matches: List<Match>): List<TeamStanding> {
        val standingsMap = teams.associate { it.id to TeamStanding(team = it) }.toMutableMap()
        val finishedMatches = matches.filter { it.status == MatchStatus.FINISHED }

        for (match in finishedMatches) {
            val homeScore = match.homeScore ?: continue
            val awayScore = match.awayScore ?: continue
            val homeStanding = standingsMap[match.homeTeamId] ?: continue
            val awayStanding = standingsMap[match.awayTeamId] ?: continue
            val homePoints = if (homeScore > awayScore) 3 else if (homeScore == awayScore) 1 else 0
            val awayPoints = if (awayScore > homeScore) 3 else if (homeScore == awayScore) 1 else 0

            standingsMap[match.homeTeamId] = homeStanding.copy(
                points = homeStanding.points + homePoints,
                matchesPlayed = homeStanding.matchesPlayed + 1,
                wins = homeStanding.wins + if (homeScore > awayScore) 1 else 0,
                draws = homeStanding.draws + if (homeScore == awayScore) 1 else 0,
                losses = homeStanding.losses + if (homeScore < awayScore) 1 else 0,
                goalsFor = homeStanding.goalsFor + homeScore,
                goalsAgainst = homeStanding.goalsAgainst + awayScore
            )

            standingsMap[match.awayTeamId] = awayStanding.copy(
                points = awayStanding.points + awayPoints,
                matchesPlayed = awayStanding.matchesPlayed + 1,
                wins = awayStanding.wins + if (awayScore > homeScore) 1 else 0,
                draws = awayStanding.draws + if (homeScore == awayScore) 1 else 0,
                losses = awayStanding.losses + if (awayScore < homeScore) 1 else 0,
                goalsFor = awayStanding.goalsFor + awayScore,
                goalsAgainst = awayStanding.goalsAgainst + homeScore
            )
        }

        return standingsMap.values.sortedWith(
            compareByDescending<TeamStanding> { it.points }
                .thenByDescending { it.goalDifference }
                .thenByDescending { it.goalsFor }
                .thenBy { it.team.name }
        )
    }
}