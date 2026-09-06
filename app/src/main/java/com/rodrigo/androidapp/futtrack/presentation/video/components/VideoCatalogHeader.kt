package com.rodrigo.androidapp.futtrack.presentation.video.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.rodrigo.androidapp.futtrack.ui.theme.FutTrackTheme

@Composable
fun VideoCatalogHeader(
    videoCount: Int,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Text(
            text = "Últimos vídeos",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground
        )

        Text(
            text = "Melhores momentos, gols e lances do Baba Amigos do Lelé",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Text(
            text = videoCount.toVideoCountLabel(),
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.primary
        )
    }
}

private fun Int.toVideoCountLabel(): String {
    return when (this) {
        1 -> "1 vídeo"
        else -> "$this vídeos"
    }
}

@Preview(
    name = "Video Catalog Header",
    showBackground = true
)
@Composable
private fun VideoCatalogHeaderPreview() {
    FutTrackTheme {
        VideoCatalogHeader(
            videoCount = 20
        )
    }
}