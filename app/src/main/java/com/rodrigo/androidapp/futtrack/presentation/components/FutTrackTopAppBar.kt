package com.rodrigo.androidapp.futtrack.presentation.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.rodrigo.androidapp.futtrack.R
import com.rodrigo.androidapp.futtrack.ui.theme.FutTrackTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FutTrackTopAppBar(
    title: String,
    modifier: Modifier = Modifier
) {
    TopAppBar(
        modifier = modifier,
        title = {
            FutTrackTopAppBarTitle(title = title)
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = MaterialTheme.colorScheme.background
        )
    )
}

@Composable
private fun FutTrackTopAppBarTitle(
    title: String,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Image(
            painter = painterResource(R.drawable.logo_bal),
            contentDescription = null,
            modifier = Modifier.height(40.dp),
            contentScale = ContentScale.Fit
        )

        Spacer(modifier = Modifier.width(12.dp))

        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun FutTrackTopAppBarPreview() {
    FutTrackTheme {
        FutTrackTopAppBar(title = "Vídeos do Baba")
    }
}