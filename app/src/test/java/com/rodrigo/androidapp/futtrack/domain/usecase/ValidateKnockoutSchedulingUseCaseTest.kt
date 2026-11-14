
package com.rodrigo.androidapp.futtrack.domain.usecase

import com.rodrigo.androidapp.futtrack.domain.model.Match
import com.rodrigo.androidapp.futtrack.domain.model.MatchLeg
import com.rodrigo.androidapp.futtrack.domain.model.MatchStage
import java.time.LocalDateTime
import org.junit.Assert.assertEquals
import org.junit.Test

class ValidateKnockoutSchedulingUseCaseTest {

    private val sut = ValidateKnockoutSchedulingUseCase()

    @Test
    fun `accepts first leg when no matches exist`() {
        val result = sut(
            candidate = createMatch(),
            competitionMatches = emptyList()
        )

        assertEquals(KnockoutSchedulingResult.Valid, result)
    }

    @Test
    fun `accepts second leg with reversed home and away teams`() {
        val firstLeg = createMatch()
        val secondLeg = createMatch(
            leg = MatchLeg.SECOND,
            homeTeamId = TEAM_B,
            awayTeamId = TEAM_A,
            date = SECOND_LEG_DATE,
            matchNumber = 2
        )

        val result = sut(secondLeg, listOf(firstLeg))

        assertEquals(KnockoutSchedulingResult.Valid, result)
    }

    @Test
    fun `rejects regular season match`() {
        val candidate = createMatch(
            stage = MatchStage.REGULAR
        )

        assertInvalid(
            KnockoutSchedulingError.INVALID_STAGE,
            candidate
        )
    }

    @Test
    fun `rejects knockout match without leg`() {
        val candidate = createMatch(leg = null)

        assertInvalid(
            KnockoutSchedulingError.MISSING_LEG,
            candidate
        )
    }

    @Test
    fun `rejects match with identical teams`() {
        val candidate = createMatch(
            awayTeamId = TEAM_A
        )

        assertInvalid(
            KnockoutSchedulingError.SAME_TEAM,
            candidate
        )
    }

    @Test
    fun `rejects match without match number`() {
        val candidate = createMatch(
            matchNumber = null
        )

        assertInvalid(
            KnockoutSchedulingError.MISSING_MATCH_NUMBER,
            candidate
        )
    }

    @Test
    fun `rejects slot occupied by regular season match`() {
        val existing = createMatch(
            stage = MatchStage.REGULAR,
            leg = null
        )

        assertInvalid(
            error = KnockoutSchedulingError.OCCUPIED_SLOT,
            candidate = createMatch(),
            existingMatches = listOf(existing)
        )
    }

    @Test
    fun `accepts different slot on same date`() {
        val existing = createMatch(
            stage = MatchStage.REGULAR,
            leg = null,
            matchNumber = 2
        )

        val result = sut(
            candidate = createMatch(),
            competitionMatches = listOf(existing)
        )

        assertEquals(KnockoutSchedulingResult.Valid, result)
    }

    @Test
    fun `accepts same slot on different date`() {
        val existing = createMatch(
            stage = MatchStage.REGULAR,
            leg = null,
            date = FIRST_LEG_DATE.plusDays(1)
        )

        val result = sut(
            candidate = createMatch(),
            competitionMatches = listOf(existing)
        )

        assertEquals(KnockoutSchedulingResult.Valid, result)
    }

    @Test
    fun `rejects duplicate first leg`() {
        val existing = createMatch()

        val duplicate = createMatch(
            homeTeamId = TEAM_C,
            awayTeamId = TEAM_B,
            date = SECOND_LEG_DATE,
            matchNumber = 2
        )

        assertInvalid(
            error = KnockoutSchedulingError.DUPLICATE_LEG,
            candidate = duplicate,
            existingMatches = listOf(existing)
        )
    }

    @Test
    fun `rejects duplicate second leg`() {
        val firstLeg = createMatch()
        val existingSecondLeg = createMatch(
            leg = MatchLeg.SECOND,
            homeTeamId = TEAM_B,
            awayTeamId = TEAM_A,
            date = SECOND_LEG_DATE,
            matchNumber = 2
        )

        val duplicate = existingSecondLeg.copy(
            date = SECOND_LEG_DATE.plusDays(7),
            matchNumber = 3
        )

        assertInvalid(
            error = KnockoutSchedulingError.DUPLICATE_LEG,
            candidate = duplicate,
            existingMatches = listOf(firstLeg, existingSecondLeg)
        )
    }

    @Test
    fun `rejects second leg without first leg`() {
        val candidate = createMatch(
            leg = MatchLeg.SECOND,
            homeTeamId = TEAM_B,
            awayTeamId = TEAM_A,
            date = SECOND_LEG_DATE
        )

        assertInvalid(
            KnockoutSchedulingError.MISSING_FIRST_LEG,
            candidate
        )
    }

    @Test
    fun `rejects second leg with different opponents`() {
        val firstLeg = createMatch()
        val secondLeg = createMatch(
            leg = MatchLeg.SECOND,
            homeTeamId = TEAM_C,
            awayTeamId = TEAM_A,
            date = SECOND_LEG_DATE,
            matchNumber = 2
        )

        assertInvalid(
            error = KnockoutSchedulingError.INVALID_OPPONENTS,
            candidate = secondLeg,
            existingMatches = listOf(firstLeg)
        )
    }

    @Test
    fun `rejects second leg without reversing home and away`() {
        val firstLeg = createMatch()
        val secondLeg = createMatch(
            leg = MatchLeg.SECOND,
            date = SECOND_LEG_DATE,
            matchNumber = 2
        )

        assertInvalid(
            error = KnockoutSchedulingError.INVALID_HOME_AWAY_ORDER,
            candidate = secondLeg,
            existingMatches = listOf(firstLeg)
        )
    }

    @Test
    fun `rejects second leg before first leg`() {
        val firstLeg = createMatch()
        val secondLeg = createMatch(
            leg = MatchLeg.SECOND,
            homeTeamId = TEAM_B,
            awayTeamId = TEAM_A,
            date = FIRST_LEG_DATE.minusDays(1),
            matchNumber = 2
        )

        assertInvalid(
            error = KnockoutSchedulingError.INVALID_LEG_DATE,
            candidate = secondLeg,
            existingMatches = listOf(firstLeg)
        )
    }

    @Test
    fun `rejects second leg at same date and time as first leg`() {
        val firstLeg = createMatch()
        val secondLeg = createMatch(
            leg = MatchLeg.SECOND,
            homeTeamId = TEAM_B,
            awayTeamId = TEAM_A,
            matchNumber = 2
        )

        assertInvalid(
            error = KnockoutSchedulingError.INVALID_LEG_DATE,
            candidate = secondLeg,
            existingMatches = listOf(firstLeg)
        )
    }

    @Test
    fun `accepts final first leg when semifinal first leg exists`() {
        val semifinal = createMatch()
        val final = createMatch(
            stage = MatchStage.FINAL,
            date = SECOND_LEG_DATE,
            matchNumber = 2
        )

        val result = sut(
            candidate = final,
            competitionMatches = listOf(semifinal)
        )

        assertEquals(KnockoutSchedulingResult.Valid, result)
    }

    private fun assertInvalid(
        error: KnockoutSchedulingError,
        candidate: Match,
        existingMatches: List<Match> = emptyList()
    ) {
        val result = sut(
            candidate = candidate,
            competitionMatches = existingMatches
        )

        assertEquals(
            KnockoutSchedulingResult.Invalid(error),
            result
        )
    }

    private fun createMatch(
        stage: MatchStage = MatchStage.SEMIFINAL,
        leg: MatchLeg? = MatchLeg.FIRST,
        homeTeamId: String = TEAM_A,
        awayTeamId: String = TEAM_B,
        date: LocalDateTime = FIRST_LEG_DATE,
        matchNumber: Int? = 1
    ): Match {
        return Match(
            matchNumber = matchNumber,
            homeTeamId = homeTeamId,
            awayTeamId = awayTeamId,
            date = date,
            stage = stage,
            leg = leg
        )
    }

    private companion object {
        const val TEAM_A = "team_a"
        const val TEAM_B = "team_b"
        const val TEAM_C = "team_c"

        val FIRST_LEG_DATE = LocalDateTime.of(
            2026, 11, 10, 9, 0
        )

        val SECOND_LEG_DATE = FIRST_LEG_DATE.plusDays(7)
    }
}
