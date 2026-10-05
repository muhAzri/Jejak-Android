package com.muhazri.jejak.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.muhazri.jejak.app.navigation.JejakNavHost
import com.muhazri.jejak.core.ui.theme.JejakTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            JejakTheme {
                JejakNavHost()
            }
        }
    }
}
