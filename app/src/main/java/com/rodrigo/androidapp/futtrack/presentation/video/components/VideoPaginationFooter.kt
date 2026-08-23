package com.rodrigo.androidapp.futtrack.presentation.video.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.rodrigo.androidapp.futtrack.ui.theme.FutTrackTheme

@Composable
fun VideoPaginationFooter(
    isLoading: Boolean,
    errorMessage: String?,
    onRetryClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        contentAlignment = Alignment.Center
    ) {
        when {
            isLoading -> {
                CircularProgressIndicator()
            }

            errorMessage != null -> {
                VideoPaginationError(
                    message = errorMessage,
                    onRetryClick = onRetryClick
                )
            }
        }
    }
}

@Composable
private fun VideoPaginationError(
    message: String,
    onRetryClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            text = message,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        TextButton(
            onClick = onRetryClick
        ) {
            Text(text = "Tentar novamente")
        }
    }
}

@Preview(
    name = "Pagination - Loading",
    showBackground = true
)
@Composable
private fun VideoPaginationLoadingPreview() {
    FutTrackTheme {
        VideoPaginationFooter(
            isLoading = true,
            errorMessage = null,
            onRetryClick = {}
        )
    }
}

@Preview(
    name = "Pagination - Error",
    showBackground = true
)
@Composable
private fun VideoPaginationErrorPreview() {
    FutTrackTheme {
        VideoPaginationFooter(
            isLoading = false,
            errorMessage = "Não foi possível carregar mais vídeos.",
            onRetryClick = {}
        )
    }
}