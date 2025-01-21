package com.alekey.bingo

import android.content.pm.ActivityInfo
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import com.alekey.bingo.ui.BingoApp

class MainActivity : ComponentActivity() {
    private val settingsViewModel: SettingsViewModel by viewModels {
        ViewModelFactory(application)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
        setContent {
            BingoApp(settingsViewModel)
        }
    }
}