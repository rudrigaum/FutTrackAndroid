
package com.rodrigo.androidapp.futtrack.domain.usecase

import com.rodrigo.androidapp.futtrack.domain.model.Match
import com.rodrigo.androidapp.futtrack.domain.model.MatchLeg
import com.rodrigo.androidapp.futtrack.domain.model.MatchStage
import com.rodrigo.androidapp.futtrack.domain.repository.MatchRepository
import java.time.LocalDateTime
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ScheduleKnockoutMatchUseCaseTest {

    @Test
    fun `schedules valid first leg`() = runBlocking {
        val repository = FakeMatchRepository()
        val sut = createUseCase(repository)
        val candidate = createMatch()

        val result = sut(candidate)

        assertEquals(KnockoutSchedulingResult.Valid, result)
        assertEquals(listOf(candidate), repository.scheduledMatches)
    }

    @Test
    fun `schedules valid second leg`() = runBlocking {
        val firstLeg = createMatch()
        val repository = FakeMatchRepository(
            initialMatches = listOf(firstLeg)
        )
        val sut = createUseCase(repository)

        val secondLeg = createMatch(
            leg = MatchLeg.SECOND,
            homeTeamId = TEAM_B,
            awayTeamId = TEAM_A,
            date = SECOND_LEG_DATE,
            matchNumber = 2
        )

        val result = sut(secondLeg)

        assertEquals(KnockoutSchedulingResult.Valid, result)
        assertEquals(listOf(secondLeg), repository.scheduledMatches)
    }

    @Test
    fun `does not schedule duplicate first leg`() = runBlocking {
        val existing = createMatch()
        val repository = FakeMatchRepository(
            initialMatches = listOf(existing)
        )
        val sut = createUseCase(repository)

        val candidate = createMatch(
            date = SECOND_LEG_DATE,
            matchNumber = 2
        )

        val result = sut(candidate)

        assertEquals(
            KnockoutSchedulingResult.Invalid(
                KnockoutSchedulingError.DUPLICATE_LEG
            ),
            result
        )
        assertTrue(repository.scheduledMatches.isEmpty())
    }

    @Test
    fun `does not schedule match in occupied slot`() = runBlocking {
        val regularMatch = createMatch(
            stage = MatchStage.REGULAR,
            leg = null
        )

        val repository = FakeMatchRepository(
            initialMatches = listOf(regularMatch)
        )
        val sut = createUseCase(repository)
        val candidate = createMatch()

        val result = sut(candidate)

        assertEquals(
            KnockoutSchedulingResult.Invalid(
                KnockoutSchedulingError.OCCUPIED_SLOT
            ),
            result
        )
        assertTrue(repository.scheduledMatches.isEmpty())
    }

    @Test
    fun `does not schedule second leg without first leg`() = runBlocking {
        val repository = FakeMatchRepository()
        val sut = createUseCase(repository)

        val candidate = createMatch(
            leg = MatchLeg.SECOND,
            homeTeamId = TEAM_B,
            awayTeamId = TEAM_A,
            date = SECOND_LEG_DATE
        )

        val result = sut(candidate)

        assertEquals(
            KnockoutSchedulingResult.Invalid(
                KnockoutSchedulingError.MISSING_FIRST_LEG
            ),
            result
        )
        assertTrue(repository.scheduledMatches.isEmpty())
    }

    @Test
    fun `does not schedule second leg with different opponents`() = runBlocking {
        val firstLeg = createMatch()
        val repository = FakeMatchRepository(
            initialMatches = listOf(firstLeg)
        )
        val sut = createUseCase(repository)

        val candidate = createMatch(
            leg = MatchLeg.SECOND,
            homeTeamId = TEAM_C,
            awayTeamId = TEAM_A,
            date = SECOND_LEG_DATE,
            matchNumber = 2
        )

        val result = sut(candidate)

        assertEquals(
            KnockoutSchedulingResult.Invalid(
                KnockoutSchedulingError.INVALID_OPPONENTS
            ),
            result
        )
        assertTrue(repository.scheduledMatches.isEmpty())
    }

    @Test
    fun `does not schedule regular season match`() = runBlocking {
        val repository = FakeMatchRepository()
        val sut = createUseCase(repository)

        val candidate = createMatch(
            stage = MatchStage.REGULAR,
            leg = null
        )

        val result = sut(candidate)

        assertEquals(
            KnockoutSchedulingResult.Invalid(
                KnockoutSchedulingError.INVALID_STAGE
            ),
            result
        )
        assertTrue(repository.scheduledMatches.isEmpty())
    }

    @Test
    fun `propagates repository write failure`() = runBlocking {
        val repository = FakeMatchRepository(
            shouldFailScheduling = true
        )
        val sut = createUseCase(repository)

        val result = runCatching {
            sut(createMatch())
        }

        assertTrue(result.exceptionOrNull() is IllegalStateException)
        assertEquals(
            "Scheduling failed",
            result.exceptionOrNull()?.message
        )
        assertTrue(repository.scheduledMatches.isEmpty())
    }

    private fun createUseCase(
        repository: MatchRepository
    ): ScheduleKnockoutMatchUseCase {
        return ScheduleKnockoutMatchUseCase(
            matchRepository = repository,
            validateKnockoutSchedulingUseCase =
                ValidateKnockoutSchedulingUseCase()
        )
    }

    private fun createMatch(
        stage: MatchStage = MatchStage.SEMIFINAL,
        leg: MatchLeg? = MatchLeg.FIRST,
        homeTeamId: String = TEAM_A,
        awayTeamId: String = TEAM_B,
        date: LocalDateTime = FIRST_LEG_DATE,
        matchNumber: Int = 1
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

    private class FakeMatchRepository(
        initialMatches: List<Match> = emptyList(),
        private val shouldFailScheduling: Boolean = false
    ) : MatchRepository {

        private val matches = MutableStateFlow(initialMatches)

        val scheduledMatches = mutableListOf<Match>()

        override fun getMatches(): Flow<List<Match>> = matches

        override suspend fun scheduleMatch(match: Match) {
            if (shouldFailScheduling) {
                throw IllegalStateException("Scheduling failed")
            }

            scheduledMatches.add(match)
            matches.value = matches.value + match
        }

        override suspend fun updateMatch(match: Match) {
            error("Unexpected updateMatch call")
        }

        override suspend fun deleteMatch(matchId: String) {
            error("Unexpected deleteMatch call")
        }
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
