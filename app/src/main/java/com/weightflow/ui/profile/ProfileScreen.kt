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
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
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
        if (state.goalWeightDisplay != null) {
            item { Spacer(modifier = Modifier.height(10.dp)) }
            item { GoalShowcase(state) }
        }
        item { Spacer(modifier = Modifier.height(10.dp)) }
        item { BodyStatsGrid(state) }
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

// ── Goal showcase ─────────────────────────────────────────────────────────────

@Composable
private fun GoalShowcase(state: ProfileUiState.Loaded) {
    val accent = MaterialTheme.colorScheme.primary
    val phase = when {
        state.maintenanceMode     -> "Maintenance"
        state.goalProgressPercent != null && state.goalProgressPercent >= 1f -> "Complete!"
        else                      -> "Cutting Phase"
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 14.dp)
            .clip(RoundedCornerShape(26.dp))
            .background(WFTokens.Card)
            .border(1.dp, WFTokens.accentBorder(accent), RoundedCornerShape(26.dp))
            .padding(18.dp),
    ) {
        Column {
            // Top row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = "CURRENT GOAL",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.5.sp,
                    color = accent,
                )
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(WFTokens.Elevated)
                        .padding(horizontal = 10.dp, vertical = 3.dp),
                ) {
                    Text(
                        text = phase,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = WFTokens.Text2,
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Start / Current / Goal trio
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                GoalCell(label = "Start", value = state.startWeightDisplay ?: "—", color = WFTokens.Text2)
                Box(modifier = Modifier.width(1.dp).height(40.dp).background(WFTokens.Border))
                GoalCell(label = "Current", value = state.currentWeightDisplay ?: "—", color = accent)
                Box(modifier = Modifier.width(1.dp).height(40.dp).background(WFTokens.Border))
                GoalCell(label = "Goal", value = state.goalWeightDisplay ?: "—", color = WFTokens.Text2)
            }

            // Progress bar
            if (state.goalProgressPercent != null) {
                Spacer(modifier = Modifier.height(16.dp))
                if (state.goalSummaryLabel != null) {
                    Text(
                        text = state.goalSummaryLabel,
                        fontSize = 11.sp,
                        color = WFTokens.Text2,
                        modifier = Modifier.padding(bottom = 6.dp),
                    )
                }
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(5.dp)
                        .clip(RoundedCornerShape(3.dp))
                        .background(WFTokens.Elevated),
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(state.goalProgressPercent)
                            .fillMaxHeight()
                            .clip(RoundedCornerShape(3.dp))
                            .background(
                                Brush.linearGradient(listOf(accent, accent.copy(alpha = 0.5f))),
                            ),
                    )
                }
            }
        }
    }
}

@Composable
private fun GoalCell(label: String, value: String, color: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = label.uppercase(),
            fontSize = 9.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 0.8.sp,
            color = WFTokens.Text3,
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = value,
            fontFamily = MaterialTheme.typography.displayLarge.fontFamily,
            fontSize = 24.sp,
            color = color,
            lineHeight = 24.sp,
        )
    }
}

// ── Body stats grid ───────────────────────────────────────────────────────────

@Composable
private fun BodyStatsGrid(state: ProfileUiState.Loaded) {
    val accent = MaterialTheme.colorScheme.primary
    val cells = buildList {
        add(Triple("Height", state.heightCm?.let { "${it.toInt()} cm" } ?: "—", false))
        add(Triple("BMI", state.bmiDisplay ?: "—", state.bmiDisplay != null))
        add(Triple("Logged", "${state.totalEntriesCount} days", false))
        add(Triple("Streak", if (state.streakDays > 0) "${state.streakDays} days" else "—", false))
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 14.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        cells.chunked(2).forEach { pair ->
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                pair.forEach { (label, value, isAccent) ->
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(14.dp))
                            .background(WFTokens.Card)
                            .border(
                                1.dp,
                                if (isAccent) WFTokens.accentBorder(accent) else WFTokens.Border,
                                RoundedCornerShape(14.dp),
                            )
                            .padding(horizontal = 12.dp, vertical = 14.dp),
                    ) {
                        Column {
                            Text(
                                text = label.uppercase(),
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 1.sp,
                                color = WFTokens.Text3,
                            )
                            Spacer(modifier = Modifier.height(5.dp))
                            Text(
                                text = value,
                                fontFamily = MaterialTheme.typography.displayLarge.fontFamily,
                                fontSize = 28.sp,
                                color = if (isAccent) accent else MaterialTheme.colorScheme.onBackground,
                                lineHeight = 28.sp,
                            )
                        }
                    }
                }
            }
        }
    }
}

// ── Achievements ──────────────────────────────────────────────────────────────

@Composable
private fun AchievementsHeader(earned: Int, total: Int) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 18.dp, vertical = 16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = "ACHIEVEMENTS",
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 1.5.sp,
            color = WFTokens.Text3,
        )
        Text(
            text = "$earned / $total",
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            color = WFTokens.Text3,
        )
    }
}

@Composable
private fun BadgeRow(earnedBadges: Set<Badge>) {
    val accent = MaterialTheme.colorScheme.primary
    LazyRow(
        contentPadding = PaddingValues(horizontal = 14.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        items(Badge.entries) { badge ->
            val isEarned = badge in earnedBadges
            BadgeItem(badge = badge, isEarned = isEarned, accent = accent)
        }
    }
}

@Composable
private fun BadgeItem(badge: Badge, isEarned: Boolean, accent: Color) {
    Column(
        modifier = Modifier.width(60.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        Box(
            modifier = Modifier
                .size(46.dp)
                .clip(RoundedCornerShape(15.dp))
                .background(if (isEarned) WFTokens.accentDim(accent) else WFTokens.Card)
                .border(
                    1.dp,
                    if (isEarned) WFTokens.accentBorder(accent) else WFTokens.Border,
                    RoundedCornerShape(15.dp),
                ),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = badge.emoji(),
                fontSize = 20.sp,
                modifier = if (!isEarned) Modifier.then(
                    Modifier.background(Color.Transparent)
                ) else Modifier,
                color = if (isEarned) Color.Unspecified else Color.Unspecified.copy(alpha = 0.4f),
            )
        }
        Text(
            text = badge.shortName(),
            fontSize = 9.sp,
            fontWeight = FontWeight.Bold,
            color = WFTokens.Text3,
            textAlign = TextAlign.Center,
            letterSpacing = 0.3.sp,
            maxLines = 2,
            lineHeight = 11.sp,
        )
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

// ── Support section ───────────────────────────────────────────────────────────

@Composable
private fun SupportSection() {
    val uriHandler = LocalUriHandler.current
    val accent = MaterialTheme.colorScheme.primary

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 14.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Text(
            text = "SUPPORT DEVELOPMENT",
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 1.5.sp,
            color = WFTokens.Text3,
            modifier = Modifier.padding(horizontal = 4.dp, vertical = 4.dp),
        )

        val links = listOf(
            Triple("Ko-fi", "☕", "https://ko-fi.com/vaibhavaher"),
            Triple("Liberapay", "💛", "https://liberapay.com/vaibhavaher"),
            Triple("GitHub Sponsors", "⭐", "https://github.com/sponsors/VaibhavAher100"),
        )

        links.forEach { (label, emoji, url) ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .background(WFTokens.Card)
                    .border(1.dp, WFTokens.Border, RoundedCornerShape(14.dp))
                    .clickable { uriHandler.openUri(url) }
                    .padding(horizontal = 16.dp, vertical = 14.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    Text(text = emoji, fontSize = 18.sp)
                    Text(
                        text = label,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface,
                        fontWeight = FontWeight.Medium,
                    )
                }
                Text(
                    text = "→",
                    fontSize = 16.sp,
                    color = WFTokens.Text3,
                )
            }
        }
    }
}

private fun Badge.emoji(): String = when (this) {
    Badge.FIRST_WEIGH_IN      -> "🏆"
    Badge.GOAL_SET            -> "🎯"
    Badge.SEVEN_DAY_STREAK    -> "🔥"
    Badge.THIRTY_DAY_STREAK   -> "📅"
    Badge.HUNDRED_DAY_STREAK  -> "🌟"
    Badge.TEN_LOGS            -> "⚡"
    Badge.FIFTY_LOGS          -> "💪"
    Badge.THREE_SIXTY_FIVE_LOGS -> "🏅"
    Badge.HALFWAY_THERE       -> "🎯"
    Badge.GOAL_CRUSHER        -> "🏆"
    Badge.COMEBACK            -> "💪"
    Badge.STEADY_STATE        -> "⭐"
}

private fun Badge.shortName(): String = when (this) {
    Badge.FIRST_WEIGH_IN      -> "First Log"
    Badge.GOAL_SET            -> "Goal Set"
    Badge.SEVEN_DAY_STREAK    -> "7-Day"
    Badge.THIRTY_DAY_STREAK   -> "30-Day"
    Badge.HUNDRED_DAY_STREAK  -> "100 Days"
    Badge.TEN_LOGS            -> "10 Logs"
    Badge.FIFTY_LOGS          -> "50 Logs"
    Badge.THREE_SIXTY_FIVE_LOGS -> "365 Logs"
    Badge.HALFWAY_THERE       -> "Halfway"
    Badge.GOAL_CRUSHER        -> "Goal Hit"
    Badge.COMEBACK            -> "Comeback"
    Badge.STEADY_STATE        -> "Steady"
}
