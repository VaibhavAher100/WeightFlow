package com.weightflow

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.weightflow.ui.onboarding.OnboardingScreen
import com.weightflow.ui.onboarding.OnboardingViewModel
import com.weightflow.ui.shell.ShellScreen
import com.weightflow.ui.theme.WeightFlowTheme

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        val app = application as WeightFlowApp
        val onboardingViewModel = OnboardingViewModel(
            userProfileRepository = app.userProfileRepository,
            userPrefsDataStore = app.userPrefsDataStore,
        )
        setContent {
            val palette by app.userPrefsDataStore.themePalette
                .collectAsStateWithLifecycle(initialValue = "lime")
            // initialValue = true keeps the shell visible while prefs load,
            // preventing a flash of the onboarding screen on existing users.
            val onboardingComplete by app.userPrefsDataStore.onboardingComplete
                .collectAsStateWithLifecycle(initialValue = true)
            WeightFlowTheme(palette = palette) {
                if (onboardingComplete) {
                    ShellScreen(app = app)
                } else {
                    OnboardingScreen(
                        viewModel = onboardingViewModel,
                        onFinished = { /* DataStore Flow drives recompose — no manual nav needed */ },
                    )
                }
            }
        }
    }
}
