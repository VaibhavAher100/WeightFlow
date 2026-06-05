package com.weightflow

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.weightflow.ui.onboarding.OnboardingScreen
import com.weightflow.ui.onboarding.OnboardingViewModel
import com.weightflow.ui.shell.ShellScreen
import com.weightflow.ui.theme.WeightFlowTheme

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        val app = application as WeightFlowApp
        val onboardingViewModel = OnboardingViewModel(
            userProfileRepository = app.userProfileRepository,
            userPrefsDataStore = app.userPrefsDataStore,
            weightRepository = app.weightRepository,
        )
        setContent {
            val palette by app.userPrefsDataStore.themePalette
                .collectAsStateWithLifecycle(initialValue = "lime")
            // null = DataStore not yet emitted — show blank background to prevent shell flash
            val onboardingState by app.userPrefsDataStore.onboardingState
                .collectAsStateWithLifecycle(initialValue = null)
            WeightFlowTheme(palette = palette) {
                when (onboardingState) {
                    null  -> Box(Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background))
                    true  -> ShellScreen(app = app)
                    false -> OnboardingScreen(viewModel = onboardingViewModel, onFinished = { })
                }
            }
        }
    }
}
