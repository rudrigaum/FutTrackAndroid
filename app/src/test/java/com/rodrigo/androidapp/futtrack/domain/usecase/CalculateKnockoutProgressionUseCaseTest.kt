package com.rodrigo.androidapp.futtrack.domain.usecase

import com.rodrigo.androidapp.futtrack.domain.model.Match
import com.rodrigo.androidapp.futtrack.domain.model.MatchLeg
import com.rodrigo.androidapp.futtrack.domain.model.MatchStage
import com.rodrigo.androidapp.futtrack.domain.model.MatchStatus
import com.rodrigo.androidapp.futtrack.domain.model.Team
import com.rodrigo.androidapp.futtrack.domain.model.TeamStanding
import java.time.LocalDateTime
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class CalculateKnockoutProgressionUseCaseTest {

    private val useCase = CalculateKnockoutProgressionUseCase(
        calculateSemifinalStatusUseCase = CalculateSemifinalStatusUseCase(),
        resolveKnockoutTieUseCase = ResolveKnockoutTieUseCase()
    )

    @Test
    fun `semifinal remains pending until both legs finish`() {
        val result = useCase(
            regularStandings = standings(),
            matches = listOf(
                match(
                    stage = MatchStage.SEMIFINAL,
                    leg = MatchLeg.FIRST,
                    home = ITALY_ID,
                    away = GERMANY_ID,
                    homeScore = 1,
                    awayScore = 0
                )
            )
        )

        assertEquals(KnockoutTieResult.Pending, result.semifinalResult)
        assertNull(result.finalOpponentTeamId)
        assertEquals(KnockoutTieResult.Pending, result.finalResult)
    }

    @Test
    fun `aggregate tie advances the better regular season team`() {
        val result = useCase(
            regularStandings = standings(),
            matches = listOf(
                semifinalLeg(MatchLeg.FIRST, 2, 1),
                semifinalLeg(MatchLeg.SECOND, 2, 1)
            )
        )

        val decision = result.semifinalResult as KnockoutTieResult.Decided
        assertEquals(ITALY_ID, decision.winnerTeamId)
        assertEquals(KnockoutVictoryReason.BETTER_REGULAR_SEASON, decision.reason)
        assertEquals(ITALY_ID, result.finalOpponentTeamId)
        assertEquals(KnockoutTieResult.Pending, result.finalResult)
    }

    @Test
    fun `semifinal winner advances to face the first place team`() {
        val result = useCase(
            regularStandings = standings(),
            matches = listOf(
                semifinalLeg(MatchLeg.FIRST, 0, 2),
                semifinalLeg(MatchLeg.SECOND, 1, 0)
            )
        )

        val decision = result.semifinalResult as KnockoutTieResult.Decided
        assertEquals(GERMANY_ID, decision.winnerTeamId)
        assertEquals(KnockoutVictoryReason.AGGREGATE_SCORE, decision.reason)
        assertEquals(GERMANY_ID, result.finalOpponentTeamId)
    }

    @Test
    fun `direct final uses the top two when semifinal is not eligible`() {
        val result = useCase(
            regularStandings = standings(thirdPlacePoints = 40),
            matches = emptyList()
        )

        assertNull(result.semifinalResult)
        assertEquals(ITALY_ID, result.finalOpponentTeamId)
        assertEquals(KnockoutTieResult.Pending, result.finalResult)
    }

    @Test
    fun `aggregate tie in the direct final crowns the better campaign`() {
        val result = useCase(
            regularStandings = standings(thirdPlacePoints = 40),
            matches = listOf(
                finalLeg(MatchLeg.FIRST, BRAZIL_ID, ITALY_ID, 2, 1),
                finalLeg(MatchLeg.SECOND, ITALY_ID, BRAZIL_ID, 1, 0)
            )
        )

        val decision = result.finalResult as KnockoutTieResult.Decided
        assertEquals(BRAZIL_ID, decision.winnerTeamId)
        assertEquals(KnockoutVictoryReason.BETTER_REGULAR_SEASON, decision.reason)
    }

    @Test
    fun `final remains pending until both legs are finished`() {
        val result = useCase(
            regularStandings = standings(thirdPlacePoints = 40),
            matches = listOf(
                finalLeg(MatchLeg.FIRST, BRAZIL_ID, ITALY_ID, 1, 0),
                finalLeg(
                    MatchLeg.SECOND,
                    ITALY_ID,
                    BRAZIL_ID,
                    null,
                    null,
                    status = MatchStatus.SCHEDULED
                )
            )
        )

        assertEquals(KnockoutTieResult.Pending, result.finalResult)
    }

    @Test
    fun `final result is ignored when teams do not match the semifinal winner`() {
        val result = useCase(
            regularStandings = standings(),
            matches = listOf(
                semifinalLeg(MatchLeg.FIRST, 0, 2),
                semifinalLeg(MatchLeg.SECOND, 1, 0),
                finalLeg(MatchLeg.FIRST, BRAZIL_ID, ITALY_ID, 1, 0),
                finalLeg(MatchLeg.SECOND, ITALY_ID, BRAZIL_ID, 0, 1)
            )
        )

        assertEquals(GERMANY_ID, result.finalOpponentTeamId)
        assertEquals(KnockoutTieResult.Pending, result.finalResult)
    }

    private fun standings(thirdPlacePoints: Int = 75): List<TeamStanding> =
        listOf(
            TeamStanding(Team(BRAZIL_ID, "Brazil", "BR"), points = 120),
            TeamStanding(Team(ITALY_ID, "Italy", "IT"), points = 90),
            TeamStanding(Team(GERMANY_ID, "Germany", "DE"), points = thirdPlacePoints)
        )

    private fun semifinalLeg(
        leg: MatchLeg,
        homeScore: Int,
        awayScore: Int
    ): Match = when (leg) {
        MatchLeg.FIRST -> match(
            MatchStage.SEMIFINAL, leg, ITALY_ID, GERMANY_ID, homeScore, awayScore
        )
        MatchLeg.SECOND -> match(
            MatchStage.SEMIFINAL, leg, GERMANY_ID, ITALY_ID, homeScore, awayScore
        )
    }

    private fun finalLeg(
        leg: MatchLeg,
        home: String,
        away: String,
        homeScore: Int?,
        awayScore: Int?,
        status: MatchStatus = MatchStatus.FINISHED
    ): Match = match(
        MatchStage.FINAL, leg, home, away, homeScore, awayScore, status
    )

    private fun match(
        stage: MatchStage,
        leg: MatchLeg,
        home: String,
        away: String,
        homeScore: Int?,
        awayScore: Int?,
        status: MatchStatus = MatchStatus.FINISHED
    ): Match = Match(
        id = "${stage.name}-${leg.name}",
        homeTeamId = home,
        awayTeamId = away,
        homeScore = homeScore,
        awayScore = awayScore,
        date = LocalDateTime.of(2026, 11, 1, 9, 0)
            .plusDays(if (stage == MatchStage.FINAL) 14 else 0)
            .plusDays(if (leg == MatchLeg.SECOND) 7 else 0),
        status = status,
        stage = stage,
        leg = leg
    )

    private companion object {
        const val BRAZIL_ID = "team_brasil"
        const val ITALY_ID = "team_italia"
        const val GERMANY_ID = "team_alemanha"
    }
}
