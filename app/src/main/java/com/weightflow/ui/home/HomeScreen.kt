package com.weightflow.ui.home

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
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
import com.weightflow.domain.WeightUnit
import com.weightflow.ui.theme.WFTokens
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter

@Composable
fun HomeScreen(viewModel: HomeViewModel) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    HomeContent(uiState = uiState)
}

@Composable
private fun HomeContent(uiState: HomeUiState) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
    ) {
        when (uiState) {
            is HomeUiState.Loading -> LoadingView()
            is HomeUiState.Empty   -> EmptyView(uiState)
            is HomeUiState.HasData -> DataView(uiState)
        }
    }
}

// ── Loading ───────────────────────────────────────────────────────────────────

@Composable
private fun LoadingView() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center,
    ) {
        CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
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
            style = MaterialTheme.typography.displayLarge.copy(fontSize = 80.sp),
            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.3f),
        )
        Spacer(modifier = Modifier.height(12.dp))
        Text(
            text = "Log your first weight to start tracking",
            style = MaterialTheme.typography.bodyLarge,
            color = WFTokens.Text2,
            textAlign = TextAlign.Center,
        )
        if (state.goalWeightDisplay != null) {
            Spacer(modifier = Modifier.height(20.dp))
            Text(
                text = "Goal: ${state.goalWeightDisplay}",
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.primary,
            )
        }
    }
}

// ── Has Data ──────────────────────────────────────────────────────────────────

@Composable
private fun DataView(state: HomeUiState.HasData) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(bottom = 24.dp),
    ) {
        item { DashboardHeader() }
        item { Spacer(modifier = Modifier.height(14.dp)) }
        item { HeroWeightCard(state) }
        item { Spacer(modifier = Modifier.height(10.dp)) }
        item { StatsTrio(state) }
        if (state.recentEntries.isNotEmpty()) {
            item {
                SectionLabel(
                    text = "RECENT",
                    modifier = Modifier.padding(start = 18.dp, top = 20.dp, bottom = 8.dp),
                )
            }
            items(state.recentEntries, key = { it.id }) { entry ->
                RecentEntryRow(
                    entry = entry,
                    modifier = Modifier
                        .padding(horizontal = 14.dp)
                        .padding(bottom = 8.dp),
                )
            }
        }
    }
}

// ── Dashboard Header ──────────────────────────────────────────────────────────

@Composable
private fun DashboardHeader() {
    val dateStr = remember {
        LocalDate.now()
            .format(DateTimeFormatter.ofPattern("EEEE, d MMM"))
            .uppercase()
    }
    val greeting = remember {
        when {
            LocalTime.now().hour < 12 -> "Good morning"
            LocalTime.now().hour < 17 -> "Good afternoon"
            else                      -> "Good evening"
        }
    }
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 18.dp, end = 18.dp, top = 14.dp),
    ) {
        Text(
            text = dateStr,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 1.sp,
            color = WFTokens.Text3,
        )
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = greeting,
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.onBackground,
        )
    }
}

// ── Hero Card ─────────────────────────────────────────────────────────────────

@Composable
private fun HeroWeightCard(state: HomeUiState.HasData) {
    val accent = MaterialTheme.colorScheme.primary
    val unitSuffix = when (state.weightUnit) {
        WeightUnit.KG  -> "kg"
        WeightUnit.LBS -> "lbs"
        WeightUnit.ST  -> ""
    }
    val numberPart = if (unitSuffix.isNotEmpty())
        state.latestWeightDisplay.removeSuffix(" $unitSuffix")
    else
        state.latestWeightDisplay

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 14.dp)
            .clip(RoundedCornerShape(26.dp))
            .background(WFTokens.Card)
            .border(1.dp, WFTokens.Border, RoundedCornerShape(26.dp)),
    ) {
        // Ambient glow top-right
        Box(
            modifier = Modifier
                .size(200.dp)
                .align(Alignment.TopEnd)
                .offset(x = 60.dp, y = (-60).dp)
                .background(
                    brush = Brush.radialGradient(
                        listOf(accent.copy(alpha = 0.22f), Color.Transparent),
                    ),
                    shape = CircleShape,
                ),
        )

        Column(modifier = Modifier.padding(horizontal = 20.dp, vertical = 22.dp)) {
            Text(
                text = "CURRENT WEIGHT",
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.5.sp,
                color = WFTokens.Text3,
            )
            Spacer(modifier = Modifier.height(4.dp))

            Row(
                verticalAlignment = Alignment.Bottom,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text(
                    text = numberPart,
                    style = MaterialTheme.typography.displayLarge.copy(fontSize = 72.sp),
                    color = MaterialTheme.colorScheme.onBackground,
                )
                if (unitSuffix.isNotEmpty()) {
                    Text(
                        text = " $unitSuffix",
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Medium,
                        color = WFTokens.Text2,
                        modifier = Modifier.padding(bottom = 8.dp),
                    )
                }
                Spacer(modifier = Modifier.weight(1f))
                if (state.deltaDisplay != null && state.deltaIsDown != null) {
                    DeltaPill(
                        display = state.deltaDisplay,
                        isDown = state.deltaIsDown,
                        modifier = Modifier
                            .align(Alignment.Bottom)
                            .padding(bottom = 10.dp),
                    )
                }
            }

            if (state.goalProgress != null && state.goalWeightDisplay != null) {
                Spacer(modifier = Modifier.height(18.dp))
                GoalProgressBlock(
                    goalDisplay = state.goalWeightDisplay,
                    progress = state.goalProgress,
                    accent = accent,
                )
            }
        }
    }
}

// ── Delta Pill ────────────────────────────────────────────────────────────────

@Composable
private fun DeltaPill(display: String, isDown: Boolean, modifier: Modifier = Modifier) {
    val bg    = if (isDown) WFTokens.Success.copy(alpha = 0.12f) else WFTokens.Danger.copy(alpha = 0.12f)
    val color = if (isDown) WFTokens.Success else WFTokens.Danger
    val arrow = if (isDown) "▼" else "▲"
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(20.dp))
            .background(bg)
            .padding(horizontal = 10.dp, vertical = 4.dp),
    ) {
        Text(
            text = "$arrow $display",
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            color = color,
        )
    }
}

// ── Goal Progress ─────────────────────────────────────────────────────────────

@Composable
private fun GoalProgressBlock(goalDisplay: String, progress: Float, accent: Color) {
    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Text(
                text = "Goal",
                fontSize = 11.sp,
                fontWeight = FontWeight.Medium,
                color = WFTokens.Text2,
            )
            Text(
                text = goalDisplay,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = accent,
            )
        }
        Spacer(modifier = Modifier.height(7.dp))
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(5.dp)
                .clip(RoundedCornerShape(3.dp))
                .background(WFTokens.Elevated),
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(progress)
                    .fillMaxHeight()
                    .clip(RoundedCornerShape(3.dp))
                    .background(
                        Brush.linearGradient(listOf(accent, accent.copy(alpha = 0.5f))),
                    ),
            )
        }
    }
}

// ── Stats Trio ────────────────────────────────────────────────────────────────

@Composable
private fun StatsTrio(state: HomeUiState.HasData) {
    val goalPct = state.goalProgress?.let { "${(it * 100).toInt()}%" } ?: "—"
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 14.dp)
            .clip(RoundedCornerShape(20.dp))
            .background(WFTokens.Card)
            .border(1.dp, WFTokens.Border, RoundedCornerShape(20.dp))
            .padding(vertical = 16.dp),
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        StatCell(
            value = if (state.streakDays > 0) "${state.streakDays}" else "—",
            label = "DAY STREAK",
        )
        StatDivider()
        StatCell(
            value = state.avgDisplay ?: "—",
            label = "7D AVG",
        )
        StatDivider()
        StatCell(
            value = goalPct,
            label = "GOAL",
        )
    }
}

@Composable
private fun StatCell(value: String, label: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = value,
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.onBackground,
        )
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = label,
            fontSize = 9.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 0.5.sp,
            color = WFTokens.Text3,
        )
    }
}

@Composable
private fun StatDivider() {
    Box(
        modifier = Modifier
            .width(1.dp)
            .height(32.dp)
            .background(WFTokens.Border),
    )
}

// ── Section Label ─────────────────────────────────────────────────────────────

@Composable
private fun SectionLabel(text: String, modifier: Modifier = Modifier) {
    Text(
        text = text,
        fontSize = 11.sp,
        fontWeight = FontWeight.Bold,
        letterSpacing = 1.5.sp,
        color = WFTokens.Text3,
        modifier = modifier,
    )
}

// ── Recent Entry Row ──────────────────────────────────────────────────────────

@Composable
private fun RecentEntryRow(entry: RecentEntryDisplay, modifier: Modifier = Modifier) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(WFTokens.Card)
            .border(1.dp, WFTokens.Border, RoundedCornerShape(14.dp))
            .padding(horizontal = 16.dp, vertical = 14.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = entry.weightDisplay,
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.onSurface,
        )
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            if (entry.deltaDisplay != null && entry.deltaIsDown != null) {
                val color = if (entry.deltaIsDown) WFTokens.Success else WFTokens.Danger
                val arrow = if (entry.deltaIsDown) "▼" else "▲"
                Text(
                    text = "$arrow ${entry.deltaDisplay}",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = color,
                )
            }
            Text(
                text = entry.dateDisplay,
                style = MaterialTheme.typography.bodyMedium,
                color = WFTokens.Text2,
            )
        }
    }
}
