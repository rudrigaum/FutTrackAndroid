package com.rodrigo.androidapp.futtrack.presentation.topscorers

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.rodrigo.androidapp.futtrack.domain.model.Player
import com.rodrigo.androidapp.futtrack.presentation.auth.AuthViewModel
import com.rodrigo.androidapp.futtrack.presentation.components.FutTrackTopAppBar
import com.rodrigo.androidapp.futtrack.ui.utils.getTeamCrest

@Composable
fun TopScorersRoute(
    viewModel: TopScorersViewModel = hiltViewModel(),
    authViewModel: AuthViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val authUiState by authViewModel.uiState.collectAsStateWithLifecycle()

    TopScorersScreen(
        uiState = uiState,
        isAdminMode = authUiState.isAdminMode,
        onIncrement = { player ->
            viewModel.updatePlayerGoals(
                playerId = player.id,
                currentGoals = player.goals,
                isIncrement = true
            )
        },
        onDecrement = { player ->
            viewModel.updatePlayerGoals(
                playerId = player.id,
                currentGoals = player.goals,
                isIncrement = false
            )
        }
    )
}

@Composable
fun TopScorersScreen(
    uiState: TopScorersUiState,
    isAdminMode: Boolean,
    onIncrement: (Player) -> Unit,
    onDecrement: (Player) -> Unit,
    modifier: Modifier = Modifier
) {
    Scaffold(
        modifier = modifier,
        topBar = {
            FutTrackTopAppBar(title = "Artilharia")
        }
    ) { paddingValues ->
        TopScorersContent(
            uiState = uiState,
            isAdminMode = isAdminMode,
            onIncrement = onIncrement,
            onDecrement = onDecrement,
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        )
    }
}

@Composable
private fun TopScorersContent(
    uiState: TopScorersUiState,
    isAdminMode: Boolean,
    onIncrement: (Player) -> Unit,
    onDecrement: (Player) -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center
    ) {
        when {
            uiState.isLoading -> {
                CircularProgressIndicator()
            }

            uiState.topScorers.isEmpty() -> {
                Text(
                    text = "Nenhum jogador cadastrado.",
                    style = MaterialTheme.typography.bodyLarge
                )
            }

            else -> {
                TopScorersList(
                    players = uiState.topScorers,
                    isAdminMode = isAdminMode,
                    onIncrement = onIncrement,
                    onDecrement = onDecrement,
                    modifier = Modifier.fillMaxSize()
                )
            }
        }
    }
}

@Composable
private fun TopScorersList(
    players: List<Player>,
    isAdminMode: Boolean,
    onIncrement: (Player) -> Unit,
    onDecrement: (Player) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier,
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        itemsIndexed(
            items = players,
            key = { _, player -> player.id }
        ) { index, player ->
            TopScorerItem(
                rank = index + 1,
                player = player,
                isAdmin = isAdminMode,
                onIncrement = { onIncrement(player) },
                onDecrement = { onDecrement(player) }
            )
        }
    }
}

@Composable
fun TopScorerItem(
    rank: Int,
    player: Player,
    isAdmin: Boolean,
    onIncrement: () -> Unit,
    onDecrement: () -> Unit,
    modifier: Modifier = Modifier
) {
    val rankColor = getRankColor(rank)
    val rankIndicator = getRankIndicator(rank)

    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "$rank$rankIndicator",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.ExtraBold,
                modifier = Modifier.width(48.dp),
                color = rankColor
            )

            Image(
                painter = painterResource(id = getTeamCrest(player.teamId)),
                contentDescription = null,
                modifier = Modifier.size(32.dp)
            )

            Spacer(modifier = Modifier.width(12.dp))

            Text(
                text = player.name,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.weight(1f)
            )

            Text(
                text = player.goals.toString(),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Black,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(
                    start = 8.dp,
                    end = 24.dp
                )
            )

            if (isAdmin) {
                GoalControls(
                    onIncrement = onIncrement,
                    onDecrement = onDecrement
                )
            } else {
                Text(
                    text = "Gols",
                    style = MaterialTheme.typography.labelMedium,
                    modifier = Modifier.padding(
                        start = 4.dp,
                        end = 8.dp
                    )
                )
            }
        }
    }
}

@Composable
private fun GoalControls(
    onIncrement: () -> Unit,
    onDecrement: () -> Unit
) {
    Row(
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(
            onClick = onDecrement,
            modifier = Modifier
                .size(32.dp)
                .background(
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    shape = CircleShape
                )
        ) {
            Text(
                text = "-",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        Spacer(modifier = Modifier.width(16.dp))

        IconButton(
            onClick = onIncrement,
            modifier = Modifier
                .size(32.dp)
                .background(
                    color = MaterialTheme.colorScheme.primaryContainer,
                    shape = CircleShape
                )
        ) {
            Icon(
                imageVector = Icons.Default.Add,
                contentDescription = "Adicionar gol",
                modifier = Modifier.size(16.dp)
            )
        }
    }
}

@Composable
private fun getRankColor(rank: Int): Color {
    return when (rank) {
        1 -> Color(0xFFD4AF37)
        2 -> Color(0xFFB0B0B0)
        3 -> Color(0xFFCD7F32)
        else -> MaterialTheme.colorScheme.onSurfaceVariant
    }
}

private fun getRankIndicator(rank: Int): String {
    return when (rank) {
        1 -> " 🏆"
        2 -> " 🥈"
        3 -> " 🥉"
        else -> "º"
    }
}