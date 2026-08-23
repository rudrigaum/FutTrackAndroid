package com.rodrigo.androidapp.futtrack.domain.usecase

import com.rodrigo.androidapp.futtrack.domain.model.Team
import com.rodrigo.androidapp.futtrack.domain.model.TeamStanding
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class CalculateSemifinalStatusUseCaseTest {

    private lateinit var useCase: CalculateSemifinalStatusUseCase

    @Before
    fun setUp() {
        useCase = CalculateSemifinalStatusUseCase()
    }

    @Test
    fun `given exactly thirty percent difference when calculating then semifinal is eligible`() {
        val secondPlace = standing(
            team = italy,
            points = 100
        )
        val thirdPlace = standing(
            team = germany,
            points = 70
        )

        val result = useCase(
            secondPlace = secondPlace,
            thirdPlace = thirdPlace
        )

        assertEquals(
            30.0,
            result.differencePercentage,
            DOUBLE_DELTA
        )
        assertEquals(
            30.0,
            result.limitPercentage,
            DOUBLE_DELTA
        )
        assertTrue(result.isEligible)
    }

    @Test
    fun `given difference above thirty percent when calculating then semifinal is not eligible`() {
        val secondPlace = standing(
            team = italy,
            points = 101
        )
        val thirdPlace = standing(
            team = germany,
            points = 70
        )

        val result = useCase(
            secondPlace = secondPlace,
            thirdPlace = thirdPlace
        )

        assertEquals(
            30.693069,
            result.differencePercentage,
            DOUBLE_DELTA
        )
        assertFalse(result.isEligible)
    }

    @Test
    fun `given equal points when calculating then difference is zero and semifinal is eligible`() {
        val secondPlace = standing(
            team = italy,
            points = 80
        )
        val thirdPlace = standing(
            team = germany,
            points = 80
        )

        val result = useCase(
            secondPlace = secondPlace,
            thirdPlace = thirdPlace
        )

        assertEquals(
            0.0,
            result.differencePercentage,
            DOUBLE_DELTA
        )
        assertTrue(result.isEligible)
    }

    @Test
    fun `given both teams with zero points when calculating then difference is zero`() {
        val secondPlace = standing(
            team = italy,
            points = 0
        )
        val thirdPlace = standing(
            team = germany,
            points = 0
        )

        val result = useCase(
            secondPlace = secondPlace,
            thirdPlace = thirdPlace
        )

        assertEquals(
            0.0,
            result.differencePercentage,
            DOUBLE_DELTA
        )
        assertTrue(result.isEligible)
    }

    @Test
    fun `given valid standings when calculating then result preserves second and third place teams`() {
        val secondPlace = standing(
            team = italy,
            points = 89
        )
        val thirdPlace = standing(
            team = germany,
            points = 84
        )

        val result = useCase(
            secondPlace = secondPlace,
            thirdPlace = thirdPlace
        )

        assertEquals(
            italy,
            result.secondPlaceTeam
        )
        assertEquals(
            germany,
            result.thirdPlaceTeam
        )
    }

    private fun standing(
        team: Team,
        points: Int
    ): TeamStanding {
        return TeamStanding(
            team = team,
            points = points
        )
    }

    private companion object {

        const val DOUBLE_DELTA = 0.000001

        val italy = Team(
            id = "team_italy",
            name = "Itália",
            isoCode = "ITA"
        )

        val germany = Team(
            id = "team_germany",
            name = "Alemanha",
            isoCode = "GER"
        )
    }
}