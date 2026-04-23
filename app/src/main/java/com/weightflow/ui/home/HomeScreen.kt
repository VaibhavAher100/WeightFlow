package com.weightflow.ui.home

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.weightflow.domain.GoalState
import androidx.lifecycle.compose.collectAsStateWithLifecycle

@Composable
fun HomeScreen(viewModel: HomeViewModel, snackbarHostState: SnackbarHostState) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) {
        viewModel.badgeEvents.collect { badges ->
            val names = badges.joinToString { it.name.replace("_", " ").lowercase() }
            snackbarHostState.showSnackbar("Badge unlocked: $names")
            viewModel.onBadgeShown(badges)
        }
    }

    HomeContent(uiState = uiState)
}

@Composable
private fun HomeContent(uiState: HomeUiState) {
    when (uiState) {
        is HomeUiState.Loading -> LoadingView()
        is HomeUiState.Empty -> EmptyView(uiState)
        is HomeUiState.HasData -> DataView(uiState)
    }
}

// ── Loading ───────────────────────────────────────────────────────────────────

@Composable
private fun LoadingView() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center,
    ) {
        CircularProgressIndicator(
            modifier = Modifier.semantics { contentDescription = "Loading" },
            color = MaterialTheme.colorScheme.primary,
        )
    }
}

// ── Empty ─────────────────────────────────────────────────────────────────────

@Composable
private fun EmptyView(state: HomeUiState.Empty) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Text(
            text = "0.0",
            style = MaterialTheme.typography.displayLarge,
            color = MaterialTheme.colorScheme.primary,
            fontWeight = FontWeight.Bold,
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "Log your first weight to start tracking",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
        )
        if (state.goalWeightDisplay != null) {
            Spacer(modifier = Modifier.height(24.dp))
            Text(
                text = "Goal: ${state.goalWeightDisplay}",
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.secondary,
            )
        }
    }
}

// ── Has Data ──────────────────────────────────────────────────────────────────

@Composable
private fun DataView(state: HomeUiState.HasData) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        when (state.goalState) {
            is GoalState.Achieved -> item { GoalBanner("You crushed your goal!", MaterialTheme.colorScheme.tertiaryContainer, MaterialTheme.colorScheme.onTertiaryContainer) }
            is GoalState.Maintenance -> item { GoalBanner("Maintaining your goal — keep it up!", MaterialTheme.colorScheme.secondaryContainer, MaterialTheme.colorScheme.onSecondaryContainer) }
            else -> {}
        }
        item {
            HeroWeightCard(state)
        }
        if (state.recentEntries.isNotEmpty()) {
            item {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Recent",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.padding(start = 4.dp),
                )
            }
            items(
                items = state.recentEntries,
                key = { it.id },
            ) { entry ->
                RecentEntryRow(entry)
            }
        }
    }
}

@Composable
private fun HeroWeightCard(state: HomeUiState.HasData) {
    val cardDescription = buildString {
        append("Current weight: ${state.latestWeightDisplay}")
        if (state.goalWeightDisplay != null) append(", goal: ${state.goalWeightDisplay}")
    }
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                color = MaterialTheme.colorScheme.primaryContainer,
                shape = RoundedCornerShape(16.dp),
            )
            .padding(24.dp)
            .clearAndSetSemantics { contentDescription = cardDescription },
        contentAlignment = Alignment.Center,
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = state.latestWeightDisplay,
                style = MaterialTheme.typography.displayMedium,
                color = MaterialTheme.colorScheme.onPrimaryContainer,
                fontWeight = FontWeight.Bold,
            )
            if (state.goalWeightDisplay != null) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Goal: ${state.goalWeightDisplay}",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f),
                )
            }
        }
    }
}

@Composable
private fun GoalBanner(message: String, background: Color, contentColor: Color) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(color = background, shape = RoundedCornerShape(12.dp))
            .padding(horizontal = 16.dp, vertical = 12.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = message,
            style = MaterialTheme.typography.labelLarge,
            color = contentColor,
            textAlign = TextAlign.Center,
        )
    }
}

@Composable
private fun RecentEntryRow(entry: RecentEntryDisplay) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                color = MaterialTheme.colorScheme.surfaceVariant,
                shape = RoundedCornerShape(8.dp),
            )
            .padding(horizontal = 16.dp, vertical = 12.dp)
            .clearAndSetSemantics { contentDescription = "${entry.weightDisplay} on ${entry.dateDisplay}" },
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = entry.weightDisplay,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurface,
            fontWeight = FontWeight.Medium,
        )
        Text(
            text = entry.dateDisplay,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}
