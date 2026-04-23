package com.weightflow.ui.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Button
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.weightflow.domain.Badge
import com.weightflow.domain.WeightUnit

@Composable
fun ProfileScreen(viewModel: ProfileViewModel, onSettingsClick: () -> Unit = {}) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    when (uiState) {
        is ProfileUiState.Loading -> Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center,
        ) {
            CircularProgressIndicator(
                modifier = Modifier.semantics { contentDescription = "Loading" },
                color = MaterialTheme.colorScheme.primary,
            )
        }

        is ProfileUiState.NoProfile -> Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = "Complete setup to see your profile",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }

        is ProfileUiState.Loaded -> {
            val state = uiState as ProfileUiState.Loaded
            ProfileContent(state = state, onUnitChanged = viewModel::onUnitChanged, onSettingsClick = onSettingsClick)
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun ProfileContent(
    state: ProfileUiState.Loaded,
    onUnitChanged: (WeightUnit) -> Unit,
    onSettingsClick: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Text(
            text = state.displayName,
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.onSurface,
        )

        if (state.goalWeightDisplay != null) {
            Text(
                text = "Goal: ${state.goalWeightDisplay}",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.secondary,
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "Weight unit",
            style = MaterialTheme.typography.titleSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            WeightUnit.entries.forEach { unit ->
                FilterChip(
                    selected = unit == state.weightUnit,
                    onClick = { onUnitChanged(unit) },
                    label = { Text(unit.name) },
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "Badges",
            style = MaterialTheme.typography.titleSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        BadgeGrid(earnedBadges = state.earnedBadges)

        Spacer(modifier = Modifier.height(8.dp))
        Button(onClick = onSettingsClick, modifier = Modifier.fillMaxWidth()) {
            Text("Settings")
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun BadgeGrid(earnedBadges: Set<Badge>) {
    FlowRow(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Badge.entries.forEach { badge ->
            val earned = badge in earnedBadges
            val badgeName = badge.name.replace("_", " ").lowercase()
                .replaceFirstChar { it.uppercase() }
            Box(
                modifier = Modifier
                    .size(72.dp)
                    .background(
                        color = if (earned) MaterialTheme.colorScheme.primaryContainer
                        else MaterialTheme.colorScheme.surfaceVariant,
                        shape = RoundedCornerShape(12.dp),
                    )
                    .semantics { contentDescription = if (earned) "$badgeName badge earned" else "$badgeName badge not yet earned" },
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = badgeName,
                    style = MaterialTheme.typography.labelSmall,
                    color = if (earned) MaterialTheme.colorScheme.onPrimaryContainer
                    else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.25f),
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(4.dp),
                )
            }
        }
    }
}
