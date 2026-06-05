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
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.patrykandpatrick.vico.compose.chart.Chart
import com.patrykandpatrick.vico.compose.chart.column.columnChart
import com.patrykandpatrick.vico.compose.chart.line.lineChart
import com.patrykandpatrick.vico.compose.component.shape.shader.fromBrush
import com.patrykandpatrick.vico.core.chart.line.LineChart as VicoLineChart
import com.patrykandpatrick.vico.core.component.shape.LineComponent
import com.patrykandpatrick.vico.core.component.shape.shader.DynamicShaders
import com.patrykandpatrick.vico.core.entry.ChartEntryModelProducer
import com.patrykandpatrick.vico.core.entry.FloatEntry
import com.weightflow.R
import com.weightflow.domain.WeightUnit
import com.weightflow.ui.i18n.WeightFormatter
import com.weightflow.ui.theme.WFTokens
import java.util.Locale
import kotlin.math.abs

@Composable
fun TrendsScreen(viewModel: TrendsViewModel) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val selectedRange by viewModel.selectedRange.collectAsStateWithLifecycle()
    val selectedChart by viewModel.selectedChart.collectAsStateWithLifecycle()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
    ) {
        FilterRow(selected = selectedRange, onSelect = viewModel::onRangeSelected)
        ChartTypeRow(selected = selectedChart, onSelect = viewModel::onChartTypeSelected)
        when (uiState) {
            is TrendsUiState.Loading -> LoadingView()
            is TrendsUiState.Empty   -> EmptyView()
            is TrendsUiState.HasData -> ChartView(uiState as TrendsUiState.HasData, selectedChart)
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

@Composable
private fun TrendsTimeRange.toLabel(): String = stringResource(
    when (this) {
        TrendsTimeRange.DAYS_7   -> R.string.trends_range_7d
        TrendsTimeRange.DAYS_30  -> R.string.trends_range_30d
        TrendsTimeRange.DAYS_90  -> R.string.trends_range_3m
        TrendsTimeRange.DAYS_180 -> R.string.trends_range_6m
        TrendsTimeRange.DAYS_365 -> R.string.trends_range_1y
        TrendsTimeRange.ALL      -> R.string.trends_range_all
    },
)

// ── Chart Type Row ────────────────────────────────────────────────────────────

@Composable
private fun ChartTypeRow(selected: ChartType, onSelect: (ChartType) -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 14.dp, vertical = 4.dp),
        horizontalArrangement = Arrangement.spacedBy(7.dp),
    ) {
        ChartType.entries.forEach { type ->
            RangePill(
                label = type.toLabel(),
                selected = type == selected,
                onClick = { onSelect(type) },
            )
        }
    }
}

@Composable
private fun ChartType.toLabel(): String = stringResource(
    when (this) {
        ChartType.LINE -> R.string.trends_chart_line
        ChartType.BAR  -> R.string.trends_chart_bar
        ChartType.AREA -> R.string.trends_chart_area
    },
)

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
            text = stringResource(R.string.trends_empty),
            style = MaterialTheme.typography.bodyLarge,
            color = WFTokens.Text2,
            textAlign = TextAlign.Center,
        )
    }
}

// ── Chart View ────────────────────────────────────────────────────────────────

@Composable
private fun ChartView(state: TrendsUiState.HasData, chartType: ChartType) {
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
                val chart = when (chartType) {
                    ChartType.LINE -> lineChart(
                        lines = listOf(
                            VicoLineChart.LineSpec(
                                lineColor = accent.toArgb(),
                                lineThicknessDp = 2f,
                            ),
                        ),
                    )
                    ChartType.BAR -> columnChart(
                        columns = listOf(
                            LineComponent(
                                color = accent.toArgb(),
                                thicknessDp = 16f,
                            ),
                        ),
                    )
                    ChartType.AREA -> lineChart(
                        lines = listOf(
                            VicoLineChart.LineSpec(
                                lineColor = accent.toArgb(),
                                lineThicknessDp = 2f,
                                lineBackgroundShader = DynamicShaders.fromBrush(
                                    androidx.compose.ui.graphics.Brush.verticalGradient(
                                        colors = listOf(
                                            accent.copy(alpha = 0.4f),
                                            accent.copy(alpha = 0f),
                                        ),
                                    ),
                                ),
                            ),
                        ),
                    )
                }
                Chart(
                    chart = chart,
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
        val coachingGoalKg = state.coachingGoalWeightKg
        val coachingEta = state.coachingEtaDays
        if (coachingGoalKg != null && coachingEta != null) {
            val locale = LocalConfiguration.current.locales[0]
            val goalDisplay = WeightFormatter.format(
                kg = coachingGoalKg,
                unit = state.weightUnit,
                locale = locale,
                kgSuffix = stringResource(R.string.unit_suffix_kg),
                lbsSuffix = stringResource(R.string.unit_suffix_lbs),
                stSuffix = stringResource(R.string.unit_suffix_st_stones),
                lbSuffix = stringResource(R.string.unit_suffix_st_pounds),
            )
            val sentence = if (coachingEta < 14) {
                stringResource(R.string.trends_coaching_almost_there, goalDisplay)
            } else {
                stringResource(R.string.trends_coaching_eta_weeks, goalDisplay, coachingEta / 7)
            }
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
    val locale = LocalConfiguration.current.locales[0]
    val unitLabel = unitSuffix(state.weightUnit)
    val avg = state.chartPoints.map { it.displayValue }.average().toFloat()
    val change = if (state.chartPoints.size >= 2)
        state.chartPoints.last().displayValue - state.chartPoints.first().displayValue
    else null

    val changeDisplay = change?.let {
        if (it < 0f) "−${oneDecimal(abs(it), locale)}" else "+${oneDecimal(it, locale)}"
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
        ChartStat(oneDecimal(state.minDisplay, locale), stringResource(R.string.trends_stat_min).uppercase(locale), unitLabel, modifier = Modifier.weight(1f))
        ChartStat(oneDecimal(state.maxDisplay, locale), stringResource(R.string.trends_stat_max).uppercase(locale), unitLabel, modifier = Modifier.weight(1f))
        ChartStat(oneDecimal(avg, locale),              stringResource(R.string.trends_stat_avg).uppercase(locale), unitLabel, modifier = Modifier.weight(1f))
        ChartStat(changeDisplay, stringResource(R.string.trends_stat_change).uppercase(locale), if (change != null) unitLabel else "", modifier = Modifier.weight(1f), valueColor = changeColor)
    }
}

@Composable
private fun unitSuffix(unit: WeightUnit): String = stringResource(
    when (unit) {
        WeightUnit.KG  -> R.string.unit_suffix_kg
        WeightUnit.LBS -> R.string.unit_suffix_lbs
        WeightUnit.ST  -> R.string.unit_suffix_lbs
    },
)

private fun oneDecimal(value: Float, locale: Locale): String =
    String.format(locale, "%.1f", value)

private fun formatChange(value: Float?, locale: Locale): String {
    if (value == null) return "—"
    return if (value < 0f) "−${oneDecimal(abs(value), locale)}" else "+${oneDecimal(value, locale)}"
}

@Composable
private fun ChartStat(
    value: String,
    label: String,
    unit: String,
    modifier: Modifier = Modifier,
    valueColor: Color = MaterialTheme.colorScheme.onBackground,
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
    val locale = LocalConfiguration.current.locales[0]
    val unitLabel = unitSuffix(weightUnit)
    val accent = MaterialTheme.colorScheme.primary

    Column(modifier = Modifier.padding(horizontal = 14.dp)) {
        StatsSectionHeader(stringResource(R.string.trends_section_all_time))
        Spacer(modifier = Modifier.height(8.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(7.dp),
        ) {
            StatRow(oneDecimal(stats.allTimeHighDisplay, locale), stringResource(R.string.trends_stat_high).uppercase(locale), unitLabel, modifier = Modifier.weight(1f))
            StatRow(oneDecimal(stats.allTimeLowDisplay, locale),  stringResource(R.string.trends_stat_low).uppercase(locale),  unitLabel, modifier = Modifier.weight(1f))
            StatRow(oneDecimal(stats.allTimeAvgDisplay, locale),  stringResource(R.string.trends_stat_avg).uppercase(locale),  unitLabel, modifier = Modifier.weight(1f))
            StatRow("${stats.totalEntries}",                       stringResource(R.string.trends_stat_logs).uppercase(locale), "",        modifier = Modifier.weight(1f))
        }

        Spacer(modifier = Modifier.height(14.dp))
        StatsSectionHeader(stringResource(R.string.trends_section_progress))
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
                formatChange(stats.change7DDisplay, locale), stringResource(R.string.trends_stat_7d_change).uppercase(locale),
                if (stats.change7DDisplay != null) unitLabel else "", modifier = Modifier.weight(1f), valueColor = change7DColor,
            )
            StatRow(
                formatChange(stats.change30DDisplay, locale), stringResource(R.string.trends_stat_30d_change).uppercase(locale),
                if (stats.change30DDisplay != null) unitLabel else "", modifier = Modifier.weight(1f), valueColor = change30DColor,
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
                formatChange(stats.avgChangePerWeekDisplay, locale), stringResource(R.string.trends_stat_avg_per_week).uppercase(locale),
                unitLabel, modifier = Modifier.weight(1f), valueColor = weekColor,
            )
            StatRow(
                formatChange(stats.avgChangePerMonthDisplay, locale), stringResource(R.string.trends_stat_avg_per_month).uppercase(locale),
                unitLabel, modifier = Modifier.weight(1f), valueColor = monthColor,
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
                            text = stringResource(R.string.trends_estimated_days_to_goal).uppercase(locale),
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 0.6.sp,
                            color = WFTokens.Text3,
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = stringResource(R.string.trends_estimated_subtitle),
                            fontSize = 11.sp,
                            color = WFTokens.Text2,
                        )
                    }
                    Text(
                        text = String.format(locale, "%d", days),
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
    val locale = LocalConfiguration.current.locales[0]
    Text(
        text = title.uppercase(locale),
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
    modifier: Modifier = Modifier,
    valueColor: Color = MaterialTheme.colorScheme.onBackground,
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
