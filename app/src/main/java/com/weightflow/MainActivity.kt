package com.weightflow

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.getValue
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.weightflow.ui.onboarding.OnboardingScreen
import com.weightflow.ui.onboarding.OnboardingViewModel
import com.weightflow.ui.shell.ShellScreen
import com.weightflow.ui.theme.WeightFlowTheme

class MainActivity : ComponentActivity() {

    private val requestNotificationPermission =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { /* graceful deny — reminder won't fire */ }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
                != PackageManager.PERMISSION_GRANTED
        ) {
            requestNotificationPermission.launch(Manifest.permission.POST_NOTIFICATIONS)
        }
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
