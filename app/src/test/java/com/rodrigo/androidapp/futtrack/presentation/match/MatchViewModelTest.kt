package com.rodrigo.androidapp.futtrack.presentation.match

import com.rodrigo.androidapp.futtrack.domain.model.Match
import com.rodrigo.androidapp.futtrack.domain.model.MatchSlot
import com.rodrigo.androidapp.futtrack.domain.model.MatchStatus
import com.rodrigo.androidapp.futtrack.domain.model.Team
import com.rodrigo.androidapp.futtrack.domain.repository.MatchRepository
import com.rodrigo.androidapp.futtrack.domain.repository.TeamRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestCoroutineScheduler
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.time.LocalDate

@OptIn(ExperimentalCoroutinesApi::class)
class MatchViewModelTest {

    private val testScheduler = TestCoroutineScheduler()
    private val testDispatcher = StandardTestDispatcher(testScheduler)

    private lateinit var matchRepository: FakeMatchRepository
    private lateinit var teamRepository: FakeTeamRepository

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)

        matchRepository = FakeMatchRepository()

        teamRepository = FakeTeamRepository(
            initialTeams = TEST_TEAMS
        )
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `should return all match slots when round has no matches`() =
        runTest(testDispatcher) {
            val viewModel = createViewModel()

            collectUiState(viewModel)
            advanceUntilIdle()

            val availableSlots =
                viewModel.getAvailableMatchSlots(TEST_DATE)

            assertEquals(
                MatchSlot.entries.toList(),
                availableSlots
            )
        }

    @Test
    fun `should remove occupied slot from available slots for same date`() =
        runTest(testDispatcher) {
            matchRepository.setMatches(
                listOf(
                    createMatch(
                        id = "match-3",
                        slot = MatchSlot.GAME_3
                    )
                )
            )

            val viewModel = createViewModel()

            collectUiState(viewModel)
            advanceUntilIdle()

            val availableSlots =
                viewModel.getAvailableMatchSlots(TEST_DATE)

            assertFalse(
                MatchSlot.GAME_3 in availableSlots
            )

            assertEquals(
                5,
                availableSlots.size
            )
        }

    @Test
    fun `should keep slot available when occupied on another date`() =
        runTest(testDispatcher) {
            matchRepository.setMatches(
                listOf(
                    createMatch(
                        id = "another-round-match",
                        slot = MatchSlot.GAME_2,
                        date = TEST_DATE.plusDays(7)
                    )
                )
            )

            val viewModel = createViewModel()

            collectUiState(viewModel)
            advanceUntilIdle()

            val availableSlots =
                viewModel.getAvailableMatchSlots(TEST_DATE)

            assertTrue(
                MatchSlot.GAME_2 in availableSlots
            )
        }

    @Test
    fun `should order match groups by latest date first`() =
        runTest(testDispatcher) {
            val oldestDate = TEST_DATE
            val middleDate = TEST_DATE.plusDays(7)
            val latestDate = TEST_DATE.plusDays(14)

            matchRepository.setMatches(
                listOf(
                    createMatch(
                        id = "middle-round",
                        slot = MatchSlot.GAME_1,
                        date = middleDate
                    ),
                    createMatch(
                        id = "oldest-round",
                        slot = MatchSlot.GAME_1,
                        date = oldestDate
                    ),
                    createMatch(
                        id = "latest-round",
                        slot = MatchSlot.GAME_1,
                        date = latestDate
                    )
                )
            )

            val viewModel = createViewModel()

            collectUiState(viewModel)
            advanceUntilIdle()

            val groupedDates =
                viewModel.uiState.value.groupedMatches.keys.toList()

            assertEquals(
                listOf(
                    latestDate,
                    middleDate,
                    oldestDate
                ),
                groupedDates
            )
        }

    @Test
    fun `should schedule match with number and start time from selected slot`() =
        runTest(testDispatcher) {
            val viewModel = createViewModel()

            collectUiState(viewModel)
            advanceUntilIdle()

            viewModel.scheduleNewMatch(
                homeTeamId = HOME_TEAM.id,
                awayTeamId = AWAY_TEAM.id,
                date = TEST_DATE,
                slot = MatchSlot.GAME_3
            )

            advanceUntilIdle()

            val scheduledMatch =
                matchRepository.scheduledMatches.single()

            assertEquals(
                MatchSlot.GAME_3.matchNumber,
                scheduledMatch.matchNumber
            )

            assertEquals(
                TEST_DATE,
                scheduledMatch.date.toLocalDate()
            )

            assertEquals(
                MatchSlot.GAME_3.startTime,
                scheduledMatch.date.toLocalTime()
            )

            assertEquals(
                HOME_TEAM.id,
                scheduledMatch.homeTeamId
            )

            assertEquals(
                AWAY_TEAM.id,
                scheduledMatch.awayTeamId
            )
        }

    @Test
    fun `should not schedule match when home and away teams are the same`() =
        runTest(testDispatcher) {
            val viewModel = createViewModel()

            collectUiState(viewModel)
            advanceUntilIdle()

            viewModel.scheduleNewMatch(
                homeTeamId = HOME_TEAM.id,
                awayTeamId = HOME_TEAM.id,
                date = TEST_DATE,
                slot = MatchSlot.GAME_1
            )

            advanceUntilIdle()

            assertTrue(
                matchRepository.scheduledMatches.isEmpty()
            )
        }

    @Test
    fun `should not schedule match when selected slot is already occupied`() =
        runTest(testDispatcher) {
            matchRepository.setMatches(
                listOf(
                    createMatch(
                        id = "occupied-match",
                        slot = MatchSlot.GAME_4
                    )
                )
            )

            val viewModel = createViewModel()

            collectUiState(viewModel)
            advanceUntilIdle()

            viewModel.scheduleNewMatch(
                homeTeamId = HOME_TEAM.id,
                awayTeamId = AWAY_TEAM.id,
                date = TEST_DATE,
                slot = MatchSlot.GAME_4
            )

            advanceUntilIdle()

            assertTrue(
                matchRepository.scheduledMatches.isEmpty()
            )
        }

    @Test
    fun `should make slot available again after match is deleted`() =
        runTest(testDispatcher) {
            val existingMatch = createMatch(
                id = "match-to-delete",
                slot = MatchSlot.GAME_5
            )

            matchRepository.setMatches(
                listOf(existingMatch)
            )

            val viewModel = createViewModel()

            collectUiState(viewModel)
            advanceUntilIdle()

            assertFalse(
                MatchSlot.GAME_5 in
                        viewModel.getAvailableMatchSlots(TEST_DATE)
            )

            viewModel.deleteMatch(existingMatch.id)

            advanceUntilIdle()

            assertTrue(
                MatchSlot.GAME_5 in
                        viewModel.getAvailableMatchSlots(TEST_DATE)
            )

            assertEquals(
                listOf(existingMatch.id),
                matchRepository.deletedMatchIds
            )
        }

    @Test
    fun `should finish match with informed score`() =
        runTest(testDispatcher) {
            val existingMatch = createMatch(
                id = "match-to-finish",
                slot = MatchSlot.GAME_6
            )

            matchRepository.setMatches(
                listOf(existingMatch)
            )

            val viewModel = createViewModel()

            collectUiState(viewModel)
            advanceUntilIdle()

            viewModel.finishMatch(
                matchId = existingMatch.id,
                homeScore = 3,
                awayScore = 1
            )

            advanceUntilIdle()

            val updatedMatch =
                matchRepository.updatedMatches.single()

            assertEquals(
                existingMatch.id,
                updatedMatch.id
            )

            assertEquals(
                3,
                updatedMatch.homeScore
            )

            assertEquals(
                1,
                updatedMatch.awayScore
            )

            assertEquals(
                MatchStatus.FINISHED,
                updatedMatch.status
            )
        }

    private fun createViewModel(): MatchViewModel {
        return MatchViewModel(
            teamRepository = teamRepository,
            matchRepository = matchRepository
        )
    }

    private fun kotlinx.coroutines.test.TestScope.collectUiState(
        viewModel: MatchViewModel
    ) {
        backgroundScope.launch(
            UnconfinedTestDispatcher(testScheduler)
        ) {
            viewModel.uiState.collect()
        }
    }

    private fun createMatch(
        id: String,
        slot: MatchSlot,
        date: LocalDate = TEST_DATE
    ): Match {
        return Match(
            id = id,
            matchNumber = slot.matchNumber,
            homeTeamId = HOME_TEAM.id,
            awayTeamId = AWAY_TEAM.id,
            date = date.atTime(slot.startTime)
        )
    }

    private class FakeMatchRepository(
        initialMatches: List<Match> = emptyList()
    ) : MatchRepository {

        private val matches =
            MutableStateFlow(initialMatches)

        val scheduledMatches = mutableListOf<Match>()
        val updatedMatches = mutableListOf<Match>()
        val deletedMatchIds = mutableListOf<String>()

        override fun getMatches(): Flow<List<Match>> {
            return matches.asStateFlow()
        }

        override suspend fun scheduleMatch(match: Match) {
            scheduledMatches += match

            matches.value =
                matches.value + match
        }

        override suspend fun deleteMatch(matchId: String) {
            deletedMatchIds += matchId

            matches.value =
                matches.value.filterNot { match ->
                    match.id == matchId
                }
        }

        override suspend fun updateMatch(match: Match) {
            updatedMatches += match

            matches.value =
                matches.value.map { currentMatch ->
                    if (currentMatch.id == match.id) {
                        match
                    } else {
                        currentMatch
                    }
                }
        }

        fun setMatches(newMatches: List<Match>) {
            matches.value = newMatches
        }
    }

    private class FakeTeamRepository(
        initialTeams: List<Team>
    ) : TeamRepository {

        private val teams =
            MutableStateFlow(initialTeams)

        override fun getTeams(): Flow<List<Team>> {
            return teams.asStateFlow()
        }

        override suspend fun addTeam(team: Team) {
            teams.value =
                teams.value + team
        }

        override suspend fun deleteTeam(teamId: String) {
            teams.value =
                teams.value.filterNot { team ->
                    team.id == teamId
                }
        }
    }

    private companion object {

        val TEST_DATE: LocalDate =
            LocalDate.of(2026, 8, 15)

        val HOME_TEAM = Team(
            id = "team_brasil",
            name = "Brasil",
            isoCode = "BR"
        )

        val AWAY_TEAM = Team(
            id = "team_italia",
            name = "Itália",
            isoCode = "IT"
        )

        val TEST_TEAMS = listOf(
            HOME_TEAM,
            AWAY_TEAM
        )
    }
}