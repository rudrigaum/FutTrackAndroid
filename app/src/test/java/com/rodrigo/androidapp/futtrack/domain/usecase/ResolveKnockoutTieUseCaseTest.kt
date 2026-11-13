
package com.rodrigo.androidapp.futtrack.domain.usecase

import com.rodrigo.androidapp.futtrack.domain.model.Match
import com.rodrigo.androidapp.futtrack.domain.model.MatchLeg
import com.rodrigo.androidapp.futtrack.domain.model.MatchStage
import com.rodrigo.androidapp.futtrack.domain.model.MatchStatus
import com.rodrigo.androidapp.futtrack.domain.model.Team
import com.rodrigo.androidapp.futtrack.domain.model.TeamStanding
import java.time.LocalDateTime
import org.junit.Assert.assertEquals
import org.junit.Assert.assertThrows
import org.junit.Assert.assertTrue
import org.junit.Test

class ResolveKnockoutTieUseCaseTest {

    private val useCase = ResolveKnockoutTieUseCase()

    @Test
    fun `higher aggregate score wins regardless of regular season ranking`() {
        val result = useCase(
            firstLeg = firstLeg(homeScore = 2, awayScore = 1),
            secondLeg = secondLeg(homeScore = 3, awayScore = 0),
            regularStandings = standings(
                firstTeamPoints = 20,
                secondTeamPoints = 10
            )
        )

        assertDecision(
            result = result,
            winnerTeamId = SECOND_TEAM_ID,
            firstTeamGoals = 2,
            secondTeamGoals = 4,
            reason = KnockoutVictoryReason.AGGREGATE_SCORE
        )
    }

    @Test
    fun `first team wins when it has the higher aggregate score`() {
        val result = useCase(
            firstLeg = firstLeg(homeScore = 2, awayScore = 0),
            secondLeg = secondLeg(homeScore = 1, awayScore = 0),
            regularStandings = standings(
                firstTeamPoints = 10,
                secondTeamPoints = 20
            )
        )

        assertDecision(
            result = result,
            winnerTeamId = FIRST_TEAM_ID,
            firstTeamGoals = 2,
            secondTeamGoals = 1,
            reason = KnockoutVictoryReason.AGGREGATE_SCORE
        )
    }

    @Test
    fun `aggregate tie favors team with more regular season points`() {
        val result = useCase(
            firstLeg = firstLeg(homeScore = 2, awayScore = 1),
            secondLeg = secondLeg(homeScore = 1, awayScore = 0),
            regularStandings = standings(
                firstTeamPoints = 20,
                secondTeamPoints = 15
            )
        )

        assertDecision(
            result = result,
            winnerTeamId = FIRST_TEAM_ID,
            firstTeamGoals = 2,
            secondTeamGoals = 2,
            reason = KnockoutVictoryReason.BETTER_REGULAR_SEASON
        )
    }

    @Test
    fun `aggregate tie can favor second team by regular season points`() {
        val result = useCase(
            firstLeg = firstLeg(homeScore = 2, awayScore = 1),
            secondLeg = secondLeg(homeScore = 1, awayScore = 0),
            regularStandings = standings(
                firstTeamPoints = 12,
                secondTeamPoints = 18
            )
        )

        assertDecision(
            result = result,
            winnerTeamId = SECOND_TEAM_ID,
            firstTeamGoals = 2,
            secondTeamGoals = 2,
            reason = KnockoutVictoryReason.BETTER_REGULAR_SEASON
        )
    }

    @Test
    fun `regular season goal difference breaks a points tie`() {
        val result = useCase(
            firstLeg = firstLeg(),
            secondLeg = secondLeg(),
            regularStandings = standings(
                firstTeamPoints = 20,
                secondTeamPoints = 20,
                firstTeamGoalsFor = 15,
                firstTeamGoalsAgainst = 10,
                secondTeamGoalsFor = 12,
                secondTeamGoalsAgainst = 5
            )
        )

        assertDecision(
            result = result,
            winnerTeamId = SECOND_TEAM_ID,
            firstTeamGoals = 2,
            secondTeamGoals = 2,
            reason = KnockoutVictoryReason.BETTER_REGULAR_SEASON
        )
    }

    @Test
    fun `regular season goals scored break equal points and goal difference`() {
        val result = useCase(
            firstLeg = firstLeg(),
            secondLeg = secondLeg(),
            regularStandings = standings(
                firstTeamPoints = 20,
                secondTeamPoints = 20,
                firstTeamGoalsFor = 12,
                firstTeamGoalsAgainst = 8,
                secondTeamGoalsFor = 14,
                secondTeamGoalsAgainst = 10
            )
        )

        assertDecision(
            result = result,
            winnerTeamId = SECOND_TEAM_ID,
            firstTeamGoals = 2,
            secondTeamGoals = 2,
            reason = KnockoutVictoryReason.BETTER_REGULAR_SEASON
        )
    }

    @Test
    fun `equal regular season campaigns require another tiebreaker`() {
        val result = useCase(
            firstLeg = firstLeg(),
            secondLeg = secondLeg(),
            regularStandings = standings(
                firstTeamPoints = 20,
                secondTeamPoints = 20
            )
        )

        assertEquals(
            KnockoutTieResult.RequiresTiebreaker,
            result
        )
    }

    @Test
    fun `missing or unfinished legs keep knockout tie pending`() {
        val regularStandings = standings()

        assertEquals(
            KnockoutTieResult.Pending,
            useCase(
                firstLeg = null,
                secondLeg = secondLeg(),
                regularStandings = regularStandings
            )
        )

        assertEquals(
            KnockoutTieResult.Pending,
            useCase(
                firstLeg = firstLeg(),
                secondLeg = null,
                regularStandings = regularStandings
            )
        )

        assertEquals(
            KnockoutTieResult.Pending,
            useCase(
                firstLeg = firstLeg(),
                secondLeg = secondLeg(
                    status = MatchStatus.SCHEDULED
                ),
                regularStandings = regularStandings
            )
        )
    }

    @Test
    fun `missing score keeps knockout tie pending`() {
        val result = useCase(
            firstLeg = firstLeg(awayScore = null),
            secondLeg = secondLeg(),
            regularStandings = standings()
        )

        assertEquals(KnockoutTieResult.Pending, result)
    }

    @Test
    fun `legs from different knockout stages are rejected`() {
        assertThrows(IllegalArgumentException::class.java) {
            useCase(
                firstLeg = firstLeg(),
                secondLeg = secondLeg(stage = MatchStage.FINAL),
                regularStandings = standings()
            )
        }
    }

    @Test
    fun `legs without reversed home and away teams are rejected`() {
        val invalidSecondLeg = secondLeg().copy(
            homeTeamId = FIRST_TEAM_ID,
            awayTeamId = SECOND_TEAM_ID
        )

        assertThrows(IllegalArgumentException::class.java) {
            useCase(
                firstLeg = firstLeg(),
                secondLeg = invalidSecondLeg,
                regularStandings = standings()
            )
        }
    }

    @Test
    fun `two legs cannot share the same match identifier`() {
        assertThrows(IllegalArgumentException::class.java) {
            useCase(
                firstLeg = firstLeg(),
                secondLeg = secondLeg().copy(id = "first"),
                regularStandings = standings()
            )
        }
    }

    @Test
    fun `negative scores are rejected`() {
        assertThrows(IllegalArgumentException::class.java) {
            useCase(
                firstLeg = firstLeg(homeScore = -1),
                secondLeg = secondLeg(),
                regularStandings = standings()
            )
        }
    }

    private fun firstLeg(
        homeScore: Int? = 2,
        awayScore: Int? = 1,
        status: MatchStatus = MatchStatus.FINISHED
    ): Match {
        return Match(
            id = "first",
            homeTeamId = FIRST_TEAM_ID,
            awayTeamId = SECOND_TEAM_ID,
            homeScore = homeScore,
            awayScore = awayScore,
            date = LocalDateTime.of(2026, 10, 24, 9, 0),
            status = status,
            stage = MatchStage.SEMIFINAL,
            leg = MatchLeg.FIRST
        )
    }

    private fun secondLeg(
        homeScore: Int? = 1,
        awayScore: Int? = 0,
        status: MatchStatus = MatchStatus.FINISHED,
        stage: MatchStage = MatchStage.SEMIFINAL
    ): Match {
        return Match(
            id = "second",
            homeTeamId = SECOND_TEAM_ID,
            awayTeamId = FIRST_TEAM_ID,
            homeScore = homeScore,
            awayScore = awayScore,
            date = LocalDateTime.of(2026, 10, 31, 9, 0),
            status = status,
            stage = stage,
            leg = MatchLeg.SECOND
        )
    }

    private fun standings(
        firstTeamPoints: Int = 20,
        secondTeamPoints: Int = 15,
        firstTeamGoalsFor: Int = 10,
        firstTeamGoalsAgainst: Int = 5,
        secondTeamGoalsFor: Int = 10,
        secondTeamGoalsAgainst: Int = 5
    ): List<TeamStanding> {
        return listOf(
            standing(
                teamId = FIRST_TEAM_ID,
                points = firstTeamPoints,
                goalsFor = firstTeamGoalsFor,
                goalsAgainst = firstTeamGoalsAgainst
            ),
            standing(
                teamId = SECOND_TEAM_ID,
                points = secondTeamPoints,
                goalsFor = secondTeamGoalsFor,
                goalsAgainst = secondTeamGoalsAgainst
            )
        )
    }

    private fun standing(
        teamId: String,
        points: Int,
        goalsFor: Int,
        goalsAgainst: Int
    ): TeamStanding {
        val team = Team(
            id = teamId,
            name = "Team $teamId",
            isoCode = "TST"
        )

        return TeamStanding(
            team = team,
            points = points,
            matchesPlayed = 10,
            wins = 6,
            draws = 2,
            losses = 2,
            goalsFor = goalsFor,
            goalsAgainst = goalsAgainst
        )
    }

    private fun assertDecision(
        result: KnockoutTieResult,
        winnerTeamId: String,
        firstTeamGoals: Int,
        secondTeamGoals: Int,
        reason: KnockoutVictoryReason
    ) {
        assertTrue(result is KnockoutTieResult.Decided)

        val decision = result as KnockoutTieResult.Decided

        assertEquals(winnerTeamId, decision.winnerTeamId)
        assertEquals(reason, decision.reason)
        assertEquals(firstTeamGoals, decision.firstTeamAggregateGoals)
        assertEquals(secondTeamGoals, decision.secondTeamAggregateGoals)

        val expectedLoser = if (winnerTeamId == FIRST_TEAM_ID) {
            SECOND_TEAM_ID
        } else {
            FIRST_TEAM_ID
        }

        assertEquals(expectedLoser, decision.loserTeamId)
    }

    private companion object {
        const val FIRST_TEAM_ID = "team-a"
        const val SECOND_TEAM_ID = "team-b"
    }
}
