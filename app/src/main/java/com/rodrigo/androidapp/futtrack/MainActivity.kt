package com.rodrigo.androidapp.futtrack

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.rodrigo.androidapp.futtrack.presentation.navigation.FuttrakMainScreen
import com.rodrigo.androidapp.futtrack.ui.theme.FutTrackTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            FutTrackTheme {
                FuttrakMainScreen()
            }
        }
    }
}