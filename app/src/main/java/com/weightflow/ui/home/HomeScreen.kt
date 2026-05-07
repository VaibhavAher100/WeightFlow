package com.weightflow.ui.home

import androidx.compose.foundation.Canvas
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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
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
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
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
fun HomeScreen(viewModel: HomeViewModel, snackbarHostState: SnackbarHostState) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    LaunchedEffect(Unit) {
        viewModel.badgeEvents.collect { badges ->
            val names = badges.joinToString(", ") { it.name.replace('_', ' ').lowercase().replaceFirstChar { c -> c.uppercase() } }
            snackbarHostState.showSnackbar("Badge unlocked: $names")
            viewModel.onBadgeShown(badges)
        }
    }
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
    val accent = MaterialTheme.colorScheme.primary
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(bottom = 24.dp),
    ) {
        item { DashboardHeader() }
        item { HeroWeightCard(state) }
        if (state.sparklinePoints.size >= 2) {
            item { SparklineCard(state.sparklinePoints, accent) }
            item { Spacer(Modifier.height(8.dp)) }
        }
        item { StatsTrio(state) }
        if (state.goalProgress != null) {
            item { Spacer(Modifier.height(10.dp)) }
            item { GoalProgressBar(state.goalProgress, accent) }
        }
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
        item {
            Text(
                text = "WeightFlow is not a medical device. Consult a healthcare professional before making health decisions.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f),
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 16.dp),
            )
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
    val unitLabel = when (state.weightUnit) {
        WeightUnit.KG  -> "Kilograms"
        WeightUnit.LBS -> "Pounds"
        WeightUnit.ST  -> "Stone"
    }
    val numberText = state.latestWeightDisplay
        .replace(" kg", "").replace(" lbs", "").replace(" st", "")

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            text = "CURRENT WEIGHT",
            fontSize = 9.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 2.sp,
            color = WFTokens.Text3,
            modifier = Modifier.padding(top = 20.dp),
        )
        Spacer(Modifier.height(4.dp))

        Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxWidth()) {
            Canvas(
                Modifier
                    .fillMaxWidth()
                    .height(80.dp)
            ) {
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(accent.copy(alpha = 0.11f), Color.Transparent),
                        center = Offset(size.width / 2, size.height),
                        radius = size.width * 0.45f,
                    ),
                )
            }
            Text(
                text = numberText,
                fontSize = 64.sp,
                fontWeight = FontWeight.Black,
                letterSpacing = (-3).sp,
                color = MaterialTheme.colorScheme.onBackground,
                fontFamily = MaterialTheme.typography.displayLarge.fontFamily,
            )
        }

        Text(
            text = unitLabel.uppercase(),
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 3.sp,
            color = accent.copy(alpha = 0.55f),
        )

        if (state.deltaDisplay != null && state.deltaIsDown != null) {
            Spacer(Modifier.height(8.dp))
            val bg    = if (state.deltaIsDown) WFTokens.Success.copy(alpha = 0.1f) else WFTokens.Danger.copy(alpha = 0.1f)
            val color = if (state.deltaIsDown) WFTokens.Success else WFTokens.Danger
            val arrow = if (state.deltaIsDown) "▼" else "▲"
            Box(
                Modifier
                    .background(bg, RoundedCornerShape(999.dp))
                    .border(1.dp, color.copy(alpha = 0.2f), RoundedCornerShape(999.dp))
                    .padding(horizontal = 12.dp, vertical = 4.dp)
            ) {
                Text(
                    text = "$arrow ${state.deltaDisplay} this week",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = color,
                )
            }
        }
        Spacer(Modifier.height(16.dp))
    }
}

// ── Sparkline Card ────────────────────────────────────────────────────────────

@Composable
private fun SparklineCard(points: List<Float>, accent: Color) {
    if (points.size < 2) return
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .background(WFTokens.Card, RoundedCornerShape(16.dp))
            .border(1.dp, WFTokens.Border, RoundedCornerShape(16.dp))
            .padding(12.dp),
    ) {
        Column {
            Text(
                text = "30-DAY TREND",
                fontSize = 8.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.5.sp,
                color = WFTokens.Text3,
            )
            Spacer(Modifier.height(8.dp))
            Canvas(
                Modifier
                    .fillMaxWidth()
                    .height(48.dp)
            ) {
                val minVal = points.min()
                val maxVal = points.max()
                val range = (maxVal - minVal).coerceAtLeast(0.1f)
                val w = size.width
                val h = size.height
                val path = androidx.compose.ui.graphics.Path()
                points.forEachIndexed { i, v ->
                    val x = (i.toFloat() / (points.size - 1)) * w
                    val y = h - ((v - minVal) / range) * h
                    if (i == 0) path.moveTo(x, y) else path.lineTo(x, y)
                }
                drawPath(
                    path,
                    color = accent,
                    style = androidx.compose.ui.graphics.drawscope.Stroke(width = 1.5.dp.toPx()),
                )
                val lastX = w
                val lastY = h - ((points.last() - minVal) / range) * h
                drawCircle(
                    color = accent,
                    radius = 3.dp.toPx(),
                    center = Offset(lastX, lastY),
                )
            }
        }
    }
}

// ── Stat Card ─────────────────────────────────────────────────────────────────

@Composable
private fun StatCard(
    label: String,
    value: String,
    modifier: Modifier = Modifier,
    valueColor: Color = Color.Unspecified,
) {
    val textColor = if (valueColor == Color.Unspecified) MaterialTheme.colorScheme.onBackground else valueColor
    Column(
        modifier = modifier
            .background(WFTokens.Card, RoundedCornerShape(16.dp))
            .border(1.dp, WFTokens.Border, RoundedCornerShape(16.dp))
            .padding(vertical = 12.dp, horizontal = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            text = value,
            fontSize = 18.sp,
            fontWeight = FontWeight.Black,
            color = textColor,
            fontFamily = MaterialTheme.typography.displayLarge.fontFamily,
            letterSpacing = (-0.5).sp,
        )
        Spacer(Modifier.height(2.dp))
        Text(
            text = label,
            fontSize = 7.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 1.5.sp,
            color = WFTokens.Text3,
        )
    }
}

// ── Goal Progress Bar ─────────────────────────────────────────────────────────

@Composable
private fun GoalProgressBar(progress: Float, accent: Color) {
    Column(Modifier.padding(horizontal = 16.dp)) {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text(
                text = "GOAL PROGRESS",
                fontSize = 8.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.5.sp,
                color = WFTokens.Text3,
            )
            Text(
                text = "${(progress * 100).toInt()}%",
                fontSize = 8.sp,
                fontWeight = FontWeight.Bold,
                color = accent,
            )
        }
        Spacer(Modifier.height(5.dp))
        Box(
            Modifier
                .fillMaxWidth()
                .height(4.dp)
                .background(WFTokens.Elevated, RoundedCornerShape(999.dp))
        ) {
            Box(
                Modifier
                    .fillMaxWidth(progress.coerceIn(0f, 1f))
                    .fillMaxHeight()
                    .background(accent, RoundedCornerShape(999.dp))
            )
        }
    }
}

// ── Stats Trio ────────────────────────────────────────────────────────────────

@Composable
private fun StatsTrio(state: HomeUiState.HasData) {
    val accent = MaterialTheme.colorScheme.primary
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        StatCard(label = "START", value = state.startDisplay ?: "—", modifier = Modifier.weight(1f))
        StatCard(label = "LOST",  value = state.lostDisplay  ?: "—", modifier = Modifier.weight(1f), valueColor = accent)
        StatCard(label = "GOAL",  value = state.goalWeightDisplay ?: "—", modifier = Modifier.weight(1f))
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
