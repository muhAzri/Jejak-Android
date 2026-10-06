package com.muhazri.jejak.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.muhazri.jejak.app.navigation.JejakNavHost
import com.muhazri.jejak.core.designsystem.JejakTheme
import com.muhazri.jejak.features.onboarding.domain.usecases.GetOnboardingStatus
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    /** Read once, before the first frame: it decides whether the app opens on onboarding or Home. */
    @Inject
    lateinit var getOnboardingStatus: GetOnboardingStatus

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        val isOnboardingCompleted = getOnboardingStatus()
        setContent {
            JejakTheme {
                JejakNavHost(isOnboardingCompleted = isOnboardingCompleted)
            }
        }
    }
}
