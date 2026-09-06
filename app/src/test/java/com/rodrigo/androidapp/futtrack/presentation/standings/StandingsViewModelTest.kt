package com.rodrigo.androidapp.futtrack.presentation.standings

import com.rodrigo.androidapp.futtrack.domain.model.Match
import com.rodrigo.androidapp.futtrack.domain.model.StandingBaseline
import com.rodrigo.androidapp.futtrack.domain.model.Team
import com.rodrigo.androidapp.futtrack.domain.repository.MatchRepository
import com.rodrigo.androidapp.futtrack.domain.repository.StandingBaselineRepository
import com.rodrigo.androidapp.futtrack.domain.repository.TeamRepository
import com.rodrigo.androidapp.futtrack.domain.usecase.CalculateSemifinalStatusUseCase
import com.rodrigo.androidapp.futtrack.domain.usecase.CalculateStandingsUseCase
import com.rodrigo.androidapp.futtrack.testutil.MainDispatcherRule
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class StandingsViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    @Test
    fun `given three teams when standings are loaded then semifinal status is calculated`() =
        runTest {
            val viewModel = createViewModel(
                teams = listOf(
                    brazil,
                    italy,
                    germany
                ),
                baselines = listOf(
                    baseline(
                        team = brazil,
                        points = 110
                    ),
                    baseline(
                        team = italy,
                        points = 100
                    ),
                    baseline(
                        team = germany,
                        points = 70
                    )
                )
            )

            backgroundScope.launch(
                UnconfinedTestDispatcher(testScheduler)
            ) {
                viewModel.uiState.collect()
            }

            advanceUntilIdle()

            val state = viewModel.uiState.value
            val semifinalStatus = state.semifinalStatus

            assertFalse(state.isLoading)
            assertNotNull(semifinalStatus)

            requireNotNull(semifinalStatus)

            assertEquals(
                italy,
                semifinalStatus.secondPlaceTeam
            )
            assertEquals(
                germany,
                semifinalStatus.thirdPlaceTeam
            )
            assertEquals(
                30.0,
                semifinalStatus.differencePercentage,
                DOUBLE_DELTA
            )
            assertTrue(semifinalStatus.isEligible)
        }

    @Test
    fun `given difference above thirty percent when standings are loaded then semifinal is not eligible`() =
        runTest {
            val viewModel = createViewModel(
                teams = listOf(
                    brazil,
                    italy,
                    germany
                ),
                baselines = listOf(
                    baseline(
                        team = brazil,
                        points = 110
                    ),
                    baseline(
                        team = italy,
                        points = 101
                    ),
                    baseline(
                        team = germany,
                        points = 70
                    )
                )
            )

            backgroundScope.launch(
                UnconfinedTestDispatcher(testScheduler)
            ) {
                viewModel.uiState.collect()
            }

            advanceUntilIdle()

            val semifinalStatus =
                viewModel.uiState.value.semifinalStatus

            assertNotNull(semifinalStatus)

            requireNotNull(semifinalStatus)

            assertEquals(
                30.693069,
                semifinalStatus.differencePercentage,
                DOUBLE_DELTA
            )
            assertFalse(semifinalStatus.isEligible)
        }

    @Test
    fun `given fewer than three teams when standings are loaded then semifinal status is null`() =
        runTest {
            val viewModel = createViewModel(
                teams = listOf(
                    brazil,
                    italy
                ),
                baselines = listOf(
                    baseline(
                        team = brazil,
                        points = 110
                    ),
                    baseline(
                        team = italy,
                        points = 100
                    )
                )
            )

            backgroundScope.launch(
                UnconfinedTestDispatcher(testScheduler)
            ) {
                viewModel.uiState.collect()
            }

            advanceUntilIdle()

            val state = viewModel.uiState.value

            assertFalse(state.isLoading)
            assertEquals(
                2,
                state.standings.size
            )
            assertNull(state.semifinalStatus)
        }

    private fun createViewModel(
        teams: List<Team>,
        baselines: List<StandingBaseline>
    ): StandingsViewModel {
        return StandingsViewModel(
            teamRepository = FakeTeamRepository(
                teams = teams
            ),
            matchRepository = FakeMatchRepository(),
            standingBaselineRepository =
                FakeStandingBaselineRepository(
                    baselines = baselines
                ),
            calculateStandingsUseCase =
                CalculateStandingsUseCase(),
            calculateSemifinalStatusUseCase =
                CalculateSemifinalStatusUseCase()
        )
    }

    private fun baseline(
        team: Team,
        points: Int
    ): StandingBaseline {
        return StandingBaseline(
            teamId = team.id,
            points = points,
            matchesPlayed = 0,
            wins = 0,
            draws = 0,
            losses = 0,
            goalsFor = 0,
            goalsAgainst = 0
        )
    }

    private class FakeTeamRepository(
        private val teams: List<Team>
    ) : TeamRepository {

        override fun getTeams(): Flow<List<Team>> =
            flowOf(teams)

        override suspend fun addTeam(team: Team) = Unit

        override suspend fun deleteTeam(teamId: String) = Unit
    }

    private class FakeMatchRepository : MatchRepository {

        override fun getMatches(): Flow<List<Match>> =
            flowOf(emptyList())

        override suspend fun scheduleMatch(match: Match) = Unit

        override suspend fun deleteMatch(matchId: String) = Unit

        override suspend fun updateMatch(match: Match) = Unit
    }

    private class FakeStandingBaselineRepository(
        private val baselines: List<StandingBaseline>
    ) : StandingBaselineRepository {

        override fun getBaselines(): Flow<List<StandingBaseline>> =
            flowOf(baselines)
    }

    private companion object {

        const val DOUBLE_DELTA = 0.000001

        val brazil = Team(
            id = "team_brazil",
            name = "Brasil",
            isoCode = "BRA"
        )

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