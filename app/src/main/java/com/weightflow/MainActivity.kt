package com.weightflow

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.runtime.getValue
import com.weightflow.ui.shell.ShellScreen
import com.weightflow.ui.theme.WeightFlowTheme

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        val app = application as WeightFlowApp
        setContent {
            val palette by app.userPrefsDataStore.themePalette
                .collectAsStateWithLifecycle(initialValue = "lime")
            WeightFlowTheme(palette = palette) {
                ShellScreen()
            }
        }
    }
}
