package com.rodrigo.androidapp.futtrack.presentation.team

import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.rodrigo.androidapp.futtrack.R
import com.rodrigo.androidapp.futtrack.domain.model.Team
import com.rodrigo.androidapp.futtrack.presentation.auth.AuthViewModel
import com.rodrigo.androidapp.futtrack.ui.utils.getTeamCrest
import com.rodrigo.androidapp.futtrack.presentation.auth.components.AdminLoginDialog
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.material3.Button
import androidx.compose.ui.platform.LocalContext

@Composable
fun TeamListRoute(
    viewModel: TeamViewModel = hiltViewModel(),
    authViewModel: AuthViewModel = hiltViewModel(),
    onNavigateToTeamPlayers: (String, String) -> Unit
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val authUiState by authViewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current

    TeamListScreen(
        uiState = uiState,
        isAdminMode = authUiState.isAdminMode,
        onTeamClick = { team ->
            onNavigateToTeamPlayers(team.id, team.name)
        },
        onAdminLoginConfirm = { email, password ->
            authViewModel.signInWithEmail(
                email = email,
                password = password,
                onSuccess = {
                    Toast.makeText(context, "👑 Modo Capitão ativado com sucesso!", Toast.LENGTH_SHORT).show()
                },
                onError = { mensagemDeErro ->
                    Toast.makeText(context, mensagemDeErro, Toast.LENGTH_LONG).show()
                }
            )
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TeamListScreen(
    uiState: TeamUiState,
    isAdminMode: Boolean,
    onTeamClick: (Team) -> Unit,
    onAdminLoginConfirm: (String, String) -> Unit
) {
    var showLoginDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Image(
                            painter = painterResource(id = R.drawable.logo_bal),
                            contentDescription = "Logo BAL",
                            modifier = Modifier.height(40.dp),
                            contentScale = ContentScale.Fit
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = "Baba Amigos do Lelé",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentAlignment = Alignment.Center
        ) {
            if (uiState.isLoading) {
                CircularProgressIndicator()
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(uiState.teams) { team ->
                        TeamItem(
                            team = team,
                            onClick = { onTeamClick(team) }
                        )
                    }

                    item {
                        Spacer(modifier = Modifier.height(24.dp))
                        if (isAdminMode) {
                            Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                                Text("👑 Modo Capitão Ativado", color = MaterialTheme.colorScheme.primary)
                            }
                        } else {
                            Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                                Button(onClick = { showLoginDialog = true }) {
                                    Text("Área do Administrador")
                                }
                            }
                        }
                        Spacer(modifier = Modifier.height(24.dp))
                    }
                  }
                if (showLoginDialog) {
                    AdminLoginDialog(
                        onDismiss = { showLoginDialog = false },
                        onLoginConfirm = { email, password ->
                            showLoginDialog = false
                            onAdminLoginConfirm(email, password)
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun TeamItem(team: Team, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Image(
                painter = painterResource(id = getTeamCrest(team.id)),
                contentDescription = null,
                modifier = Modifier
                    .size(48.dp)
                    .padding(end = 16.dp),
                contentScale = ContentScale.Fit
            )
            Text(
                text = team.name,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
        }
    }
}