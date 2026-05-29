package com.weightflow.ui.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.weightflow.domain.Badge
import com.weightflow.ui.theme.WFTokens

@Composable
fun ProfileScreen(viewModel: ProfileViewModel, onSettingsClick: () -> Unit = {}) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var showDeleteDialog by rememberSaveable { mutableStateOf(false) }

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Delete all data?") },
            text = {
                Text(
                    "This permanently deletes all weight entries, your profile, and preferences from this device. This cannot be undone.",
                    style = MaterialTheme.typography.bodyMedium,
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.deleteAllData()
                    showDeleteDialog = false
                }) {
                    Text("Delete", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) {
                    Text("Cancel")
                }
            },
        )
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
    ) {
        when (uiState) {
            is ProfileUiState.Loading   -> Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center,
            ) { CircularProgressIndicator(color = MaterialTheme.colorScheme.primary) }

            is ProfileUiState.NoProfile -> Box(
                modifier = Modifier.fillMaxSize().padding(32.dp),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = "Complete onboarding to see your profile",
                    style = MaterialTheme.typography.bodyLarge,
                    color = WFTokens.Text2,
                    textAlign = TextAlign.Center,
                )
            }

            is ProfileUiState.Loaded -> ProfileContent(
                state = uiState as ProfileUiState.Loaded,
                onDeleteAllData = { showDeleteDialog = true },
                onSettingsClick = onSettingsClick,
            )
        }
    }
}

// ── Main content ──────────────────────────────────────────────────────────────

@Composable
private fun ProfileContent(
    state: ProfileUiState.Loaded,
    onDeleteAllData: () -> Unit,
    onSettingsClick: () -> Unit,
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(bottom = 32.dp),
    ) {
        item { PageHeader(onSettingsClick) }
        item { ProfileHero(state) }
        item { Spacer(modifier = Modifier.height(10.dp)) }
        item {
            val accent = MaterialTheme.colorScheme.primary
            JourneyCard(state = state, accent = accent)
        }
        item { Spacer(modifier = Modifier.height(10.dp)) }
        item { BodyStatsGrid(state) }
        if (state.bmiCategory != null) {
            item { Spacer(modifier = Modifier.height(8.dp)) }
            item { BmiContextCard(state) }
        }
        item {
            AchievementsHeader(
                earned = state.earnedBadges.size,
                total = Badge.entries.size,
            )
        }
        item { BadgeRow(earnedBadges = state.earnedBadges) }
        item { Spacer(modifier = Modifier.height(16.dp)) }
        item { SupportSection() }
        item { Spacer(modifier = Modifier.height(16.dp)) }
        item { DeleteDataSection(onDeleteAllData) }
        item { Spacer(modifier = Modifier.height(8.dp)) }
    }
}

// ── Page header ───────────────────────────────────────────────────────────────

@Composable
private fun PageHeader(onSettingsClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 18.dp, vertical = 16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = "Profile",
            fontSize = 22.sp,
            fontWeight = FontWeight.ExtraBold,
            color = MaterialTheme.colorScheme.onBackground,
        )
        IconButton(onClick = onSettingsClick) {
            Icon(
                imageVector = Icons.Default.Settings,
                contentDescription = "Settings",
                tint = WFTokens.Text2,
            )
        }
    }
}

// ── Profile hero ──────────────────────────────────────────────────────────────

@Composable
private fun ProfileHero(state: ProfileUiState.Loaded) {
    val accent = MaterialTheme.colorScheme.primary
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 18.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        // Avatar
        val initials = state.displayName
            .split(" ")
            .filter { it.isNotEmpty() }
            .take(2)
            .joinToString("") { it.first().uppercase() }
        Box(
            modifier = Modifier
                .size(64.dp)
                .clip(RoundedCornerShape(20.dp))
                .background(
                    Brush.linearGradient(
                        listOf(accent, accent.copy(alpha = 0.3f)),
                    ),
                ),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = initials,
                fontSize = 26.sp,
                fontWeight = FontWeight.ExtraBold,
                color = MaterialTheme.colorScheme.onPrimary,
            )
        }

        Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text(
                text = state.displayName,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground,
            )
            Text(
                text = "${state.totalEntriesCount} entries logged",
                fontSize = 12.sp,
                color = WFTokens.Text2,
            )
            if (state.streakDays > 0) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                ) {
                    Text(text = "🔥", fontSize = 14.sp)
                    Text(
                        text = "${state.streakDays}",
                        fontFamily = MaterialTheme.typography.displayLarge.fontFamily,
                        fontSize = 18.sp,
                        color = WFTokens.Success,
                    )
                    Text(
                        text = "day streak",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = WFTokens.Text3,
                    )
                }
            }
        }
    }
}

// ── Delete all data (GDPR Art. 17 / Play Store account deletion requirement) ──

@Composable
private fun DeleteDataSection(onDeleteAllData: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 14.dp),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(14.dp))
                .background(WFTokens.Card)
                .border(1.dp, MaterialTheme.colorScheme.error.copy(alpha = 0.3f), RoundedCornerShape(14.dp))
                .clickable(onClick = onDeleteAllData)
                .padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Text(
                text = "Delete all my data",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.error,
                fontWeight = FontWeight.Medium,
            )
            Text(text = "→", fontSize = 16.sp, color = MaterialTheme.colorScheme.error.copy(alpha = 0.6f))
        }
        Text(
            text = "Permanently removes all entries, profile, and preferences from this device.",
            fontSize = 10.sp,
            color = WFTokens.Text3,
            modifier = Modifier.padding(horizontal = 4.dp, vertical = 4.dp),
        )
    }
}
