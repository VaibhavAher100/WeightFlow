package com.weightflow.ui.trends

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.patrykandpatrick.vico.compose.chart.Chart
import com.patrykandpatrick.vico.compose.chart.line.lineChart
import com.patrykandpatrick.vico.core.chart.line.LineChart as VicoLineChart
import com.patrykandpatrick.vico.core.entry.ChartEntryModelProducer
import com.patrykandpatrick.vico.core.entry.FloatEntry
import com.weightflow.domain.WeightUnit
import com.weightflow.ui.theme.WFTokens
import kotlin.math.abs

@Composable
fun TrendsScreen(viewModel: TrendsViewModel) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val selectedRange by viewModel.selectedRange.collectAsStateWithLifecycle()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
    ) {
        FilterRow(selected = selectedRange, onSelect = viewModel::onRangeSelected)
        when (uiState) {
            is TrendsUiState.Loading -> LoadingView()
            is TrendsUiState.Empty   -> EmptyView()
            is TrendsUiState.HasData -> ChartView(uiState as TrendsUiState.HasData)
        }
    }
}

// ── Filter Row ────────────────────────────────────────────────────────────────

@Composable
private fun FilterRow(selected: TrendsTimeRange, onSelect: (TrendsTimeRange) -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState())
            .padding(horizontal = 14.dp, vertical = 10.dp),
        horizontalArrangement = Arrangement.spacedBy(7.dp),
    ) {
        TrendsTimeRange.entries.forEach { range ->
            RangePill(
                label = range.toLabel(),
                selected = range == selected,
                onClick = { onSelect(range) },
            )
        }
    }
}

@Composable
private fun RangePill(label: String, selected: Boolean, onClick: () -> Unit) {
    val bg          = if (selected) MaterialTheme.colorScheme.primary else Color.Transparent
    val fg          = if (selected) MaterialTheme.colorScheme.onPrimary else WFTokens.Text3
    val borderColor = if (selected) MaterialTheme.colorScheme.primary else WFTokens.Border
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(20.dp))
            .background(bg)
            .border(1.dp, borderColor, RoundedCornerShape(20.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 6.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(text = label, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = fg)
    }
}

private fun TrendsTimeRange.toLabel() = when (this) {
    TrendsTimeRange.DAYS_7   -> "7D"
    TrendsTimeRange.DAYS_30  -> "30D"
    TrendsTimeRange.DAYS_90  -> "3M"
    TrendsTimeRange.DAYS_180 -> "6M"
    TrendsTimeRange.DAYS_365 -> "1Y"
    TrendsTimeRange.ALL      -> "All"
}

// ── Loading / Empty ───────────────────────────────────────────────────────────

@Composable
private fun LoadingView() {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
    }
}

@Composable
private fun EmptyView() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = "Log at least two weights to see your trend",
            style = MaterialTheme.typography.bodyLarge,
            color = WFTokens.Text2,
            textAlign = TextAlign.Center,
        )
    }
}

// ── Chart View ────────────────────────────────────────────────────────────────

@Composable
private fun ChartView(state: TrendsUiState.HasData) {
    val accent = MaterialTheme.colorScheme.primary
    val modelProducer = remember { ChartEntryModelProducer() }
    var producerReady by remember { mutableStateOf(false) }

    LaunchedEffect(state.chartPoints) {
        val entries = state.chartPoints.mapIndexed { idx, pt ->
            FloatEntry(x = idx.toFloat(), y = pt.displayValue)
        }
        modelProducer.setEntriesSuspending(entries).await()
        producerReady = true
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()),
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp)
                .clip(RoundedCornerShape(20.dp))
                .background(WFTokens.Card)
                .border(1.dp, WFTokens.Border, RoundedCornerShape(20.dp))
                .padding(14.dp),
        ) {
            if (producerReady) {
                Chart(
                    chart = lineChart(
                        lines = listOf(
                            VicoLineChart.LineSpec(
                                lineColor = accent.toArgb(),
                                lineThicknessDp = 2f,
                            ),
                        ),
                    ),
                    chartModelProducer = modelProducer,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp),
                )
            } else {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    CircularProgressIndicator(
                        color = MaterialTheme.colorScheme.primary,
                        strokeWidth = 2.dp,
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))
        ChartStatsRow(state)
        state.statsSection?.let { stats ->
            Spacer(modifier = Modifier.height(16.dp))
            StatisticsSection(stats, state.weightUnit)
        }
        state.coachingSentence?.let { sentence ->
            Spacer(Modifier.height(10.dp))
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 14.dp)
                    .background(WFTokens.accentSoft(accent), RoundedCornerShape(16.dp))
                    .border(1.dp, WFTokens.accentBorder(accent), RoundedCornerShape(16.dp))
                    .padding(horizontal = 16.dp, vertical = 12.dp),
            ) {
                Text(
                    text = sentence,
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f),
                    fontStyle = FontStyle.Italic,
                    lineHeight = 16.sp,
                )
            }
        }
        Spacer(modifier = Modifier.height(16.dp))
    }
}

// ── Chart Stats Row ───────────────────────────────────────────────────────────

@Composable
private fun ChartStatsRow(state: TrendsUiState.HasData) {
    val unitLabel = when (state.weightUnit) {
        WeightUnit.KG  -> "kg"
        WeightUnit.LBS -> "lbs"
        WeightUnit.ST  -> "lbs"
    }
    val avg = state.chartPoints.map { it.displayValue }.average().toFloat()
    val change = if (state.chartPoints.size >= 2)
        state.chartPoints.last().displayValue - state.chartPoints.first().displayValue
    else null

    val changeDisplay = change?.let {
        if (it < 0f) "−${"%.1f".format(abs(it))}" else "+${"%.1f".format(it)}"
    } ?: "—"
    val changeColor = change?.let {
        if (it < 0f) WFTokens.Success else WFTokens.Danger
    } ?: WFTokens.Text2

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 14.dp),
        horizontalArrangement = Arrangement.spacedBy(7.dp),
    ) {
        ChartStat("%.1f".format(state.minDisplay), "MIN", unitLabel, modifier = Modifier.weight(1f))
        ChartStat("%.1f".format(state.maxDisplay), "MAX", unitLabel, modifier = Modifier.weight(1f))
        ChartStat("%.1f".format(avg),              "AVG", unitLabel, modifier = Modifier.weight(1f))
        ChartStat(changeDisplay, "CHANGE", if (change != null) unitLabel else "", changeColor, Modifier.weight(1f))
    }
}

@Composable
private fun ChartStat(
    value: String,
    label: String,
    unit: String,
    valueColor: Color = MaterialTheme.colorScheme.onBackground,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(14.dp))
            .background(WFTokens.Card)
            .border(1.dp, WFTokens.Border, RoundedCornerShape(14.dp))
            .padding(vertical = 11.dp, horizontal = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            text = value,
            style = MaterialTheme.typography.headlineSmall.copy(fontSize = 19.sp),
            color = valueColor,
            textAlign = TextAlign.Center,
        )
        if (unit.isNotEmpty()) {
            Text(
                text = unit,
                fontSize = 9.sp,
                color = WFTokens.Text3,
                fontWeight = FontWeight.Bold,
            )
        }
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = label,
            fontSize = 9.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 0.6.sp,
            color = WFTokens.Text3,
            textAlign = TextAlign.Center,
        )
    }
}

// ── Statistics Section ────────────────────────────────────────────────────────

@Composable
private fun StatisticsSection(stats: StatsSection, weightUnit: WeightUnit) {
    val unitLabel = when (weightUnit) {
        WeightUnit.KG  -> "kg"
        WeightUnit.LBS -> "lbs"
        WeightUnit.ST  -> "lbs"
    }
    val accent = MaterialTheme.colorScheme.primary

    Column(modifier = Modifier.padding(horizontal = 14.dp)) {
        StatsSectionHeader("All Time")
        Spacer(modifier = Modifier.height(8.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(7.dp),
        ) {
            StatRow("%.1f".format(stats.allTimeHighDisplay), "HIGH", unitLabel, modifier = Modifier.weight(1f))
            StatRow("%.1f".format(stats.allTimeLowDisplay),  "LOW",  unitLabel, modifier = Modifier.weight(1f))
            StatRow("%.1f".format(stats.allTimeAvgDisplay),  "AVG",  unitLabel, modifier = Modifier.weight(1f))
            StatRow("${stats.totalEntries}",                  "LOGS", "",        modifier = Modifier.weight(1f))
        }

        Spacer(modifier = Modifier.height(14.dp))
        StatsSectionHeader("Progress")
        Spacer(modifier = Modifier.height(8.dp))

        val change7DColor = stats.change7DDisplay?.let { if (it < 0f) WFTokens.Success else WFTokens.Danger }
            ?: WFTokens.Text3
        val change30DColor = stats.change30DDisplay?.let { if (it < 0f) WFTokens.Success else WFTokens.Danger }
            ?: WFTokens.Text3

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(7.dp),
        ) {
            StatRow(
                formatChange(stats.change7DDisplay), "7D CHANGE",
                if (stats.change7DDisplay != null) unitLabel else "", change7DColor, Modifier.weight(1f),
            )
            StatRow(
                formatChange(stats.change30DDisplay), "30D CHANGE",
                if (stats.change30DDisplay != null) unitLabel else "", change30DColor, Modifier.weight(1f),
            )
        }

        Spacer(modifier = Modifier.height(7.dp))

        val weekColor  = if (stats.avgChangePerWeekDisplay  < 0f) WFTokens.Success else WFTokens.Danger
        val monthColor = if (stats.avgChangePerMonthDisplay < 0f) WFTokens.Success else WFTokens.Danger

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(7.dp),
        ) {
            StatRow(
                formatChange(stats.avgChangePerWeekDisplay), "AVG / WEEK",
                unitLabel, weekColor, Modifier.weight(1f),
            )
            StatRow(
                formatChange(stats.avgChangePerMonthDisplay), "AVG / MONTH",
                unitLabel, monthColor, Modifier.weight(1f),
            )
        }

        stats.estimatedDaysToGoal?.let { days ->
            Spacer(modifier = Modifier.height(7.dp))
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .background(WFTokens.Card)
                    .border(1.dp, WFTokens.accentBorder(accent), RoundedCornerShape(14.dp))
                    .padding(horizontal = 16.dp, vertical = 12.dp),
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Column {
                        Text(
                            text = "ESTIMATED DAYS TO GOAL",
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 0.6.sp,
                            color = WFTokens.Text3,
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "At your current rate",
                            fontSize = 11.sp,
                            color = WFTokens.Text2,
                        )
                    }
                    Text(
                        text = "$days",
                        fontFamily = MaterialTheme.typography.displayLarge.fontFamily,
                        fontSize = 34.sp,
                        fontWeight = FontWeight.Bold,
                        color = accent,
                    )
                }
            }
        }
    }
}

@Composable
private fun StatsSectionHeader(title: String) {
    Text(
        text = title.uppercase(),
        fontSize = 10.sp,
        fontWeight = FontWeight.Bold,
        letterSpacing = 1.5.sp,
        color = WFTokens.Text3,
    )
}

@Composable
private fun StatRow(
    value: String,
    label: String,
    unit: String,
    valueColor: Color = MaterialTheme.colorScheme.onBackground,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(14.dp))
            .background(WFTokens.Card)
            .border(1.dp, WFTokens.Border, RoundedCornerShape(14.dp))
            .padding(vertical = 10.dp, horizontal = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            text = value,
            style = MaterialTheme.typography.headlineSmall.copy(fontSize = 17.sp),
            color = valueColor,
            textAlign = TextAlign.Center,
        )
        if (unit.isNotEmpty()) {
            Text(text = unit, fontSize = 9.sp, color = WFTokens.Text3, fontWeight = FontWeight.Bold)
        }
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = label,
            fontSize = 8.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 0.5.sp,
            color = WFTokens.Text3,
            textAlign = TextAlign.Center,
        )
    }
}

private fun formatChange(value: Float?): String {
    if (value == null) return "—"
    return if (value < 0f) "−${"%.1f".format(abs(value))}" else "+${"%.1f".format(value)}"
}
