package com.rodrigo.androidapp.futtrack.presentation.match.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.rodrigo.androidapp.futtrack.ui.theme.FutTrackTheme

@Composable
fun ScoreDialog(
    homeName: String,
    awayName: String,
    initialHomeScore: Int,
    initialAwayScore: Int,
    onDismiss: () -> Unit,
    onConfirm: (Int, Int) -> Unit
) {
    var homeScore by rememberSaveable(initialHomeScore) {
        mutableIntStateOf(initialHomeScore)
    }

    var awayScore by rememberSaveable(initialAwayScore) {
        mutableIntStateOf(initialAwayScore)
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = scoreDialogTitle(
                    initialHomeScore = initialHomeScore,
                    initialAwayScore = initialAwayScore
                )
            )
        },
        text = {
            ScoreContent(
                homeName = homeName,
                awayName = awayName,
                homeScore = homeScore,
                awayScore = awayScore,
                onHomeScoreChange = { score ->
                    homeScore = score
                },
                onAwayScoreChange = { score ->
                    awayScore = score
                }
            )
        },
        confirmButton = {
            TextButton(
                onClick = {
                    onConfirm(
                        homeScore,
                        awayScore
                    )
                }
            ) {
                Text("Salvar")
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss
            ) {
                Text("Cancelar")
            }
        }
    )
}

@Composable
private fun ScoreContent(
    homeName: String,
    awayName: String,
    homeScore: Int,
    awayScore: Int,
    onHomeScoreChange: (Int) -> Unit,
    onAwayScoreChange: (Int) -> Unit
) {
    androidx.compose.foundation.layout.Column(
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        ScoreInputRow(
            teamName = homeName,
            score = homeScore,
            onScoreChange = onHomeScoreChange
        )

        ScoreInputRow(
            teamName = awayName,
            score = awayScore,
            onScoreChange = onAwayScoreChange
        )
    }
}

@Composable
private fun ScoreInputRow(
    teamName: String,
    score: Int,
    onScoreChange: (Int) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = teamName,
            modifier = Modifier.weight(1f),
            style = MaterialTheme.typography.bodyLarge
        )

        ScoreControls(
            score = score,
            onScoreChange = onScoreChange
        )
    }
}

@Composable
private fun ScoreControls(
    score: Int,
    onScoreChange: (Int) -> Unit
) {
    Row(
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(
            onClick = {
                if (score > MIN_SCORE) {
                    onScoreChange(score - 1)
                }
            }
        ) {
            Text(
                text = "-",
                style = MaterialTheme.typography.titleLarge
            )
        }

        Text(
            text = score.toString(),
            style = MaterialTheme.typography.titleLarge,
            modifier = Modifier.padding(
                horizontal = 16.dp
            )
        )

        IconButton(
            onClick = {
                onScoreChange(score + 1)
            }
        ) {
            Icon(
                imageVector = Icons.Default.Add,
                contentDescription = "Adicionar gol"
            )
        }
    }
}

private fun scoreDialogTitle(
    initialHomeScore: Int,
    initialAwayScore: Int
): String {
    return if (
        initialHomeScore > MIN_SCORE ||
        initialAwayScore > MIN_SCORE
    ) {
        "Editar Placar"
    } else {
        "Lançar Placar Final"
    }
}

@Preview(
    name = "New Score",
    showBackground = true
)
@Composable
private fun NewScoreDialogPreview() {
    FutTrackTheme {
        ScoreDialog(
            homeName = "Brasil",
            awayName = "Itália",
            initialHomeScore = 0,
            initialAwayScore = 0,
            onDismiss = {},
            onConfirm = { _, _ -> }
        )
    }
}

@Preview(
    name = "Edit Score",
    showBackground = true
)
@Composable
private fun EditScoreDialogPreview() {
    FutTrackTheme {
        ScoreDialog(
            homeName = "Brasil",
            awayName = "Alemanha",
            initialHomeScore = 2,
            initialAwayScore = 1,
            onDismiss = {},
            onConfirm = { _, _ -> }
        )
    }
}

private const val MIN_SCORE = 0