package com.weightflow.ui.trends

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.weightflow.data.UserPrefsDataStore
import com.weightflow.data.WeightRepository
import com.weightflow.domain.WeightConverter
import com.weightflow.domain.WeightUnit
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import java.time.LocalDate
import java.time.ZoneId

class TrendsViewModel(
    private val weightRepository: WeightRepository,
    private val userPrefsDataStore: UserPrefsDataStore,
) : ViewModel() {

    private val _selectedRange = MutableStateFlow(TrendsTimeRange.DAYS_30)
    val selectedRange: StateFlow<TrendsTimeRange> = _selectedRange.asStateFlow()

    val uiState: StateFlow<TrendsUiState> = combine(
        weightRepository.getEntriesOldestFirst(),
        userPrefsDataStore.weightUnit,
        _selectedRange,
    ) { entries, unit, range ->
        if (entries.isEmpty()) return@combine TrendsUiState.Empty

        val filtered = filterByRange(entries, range)
        if (filtered.isEmpty()) return@combine TrendsUiState.Empty

        val points = filtered.map { entry ->
            ChartPoint(
                timestamp = entry.timestamp,
                displayValue = toDisplayValue(entry.weightKg, unit),
            )
        }
        TrendsUiState.HasData(
            chartPoints = points,
            weightUnit = unit,
            minDisplay = points.minOf { it.displayValue },
            maxDisplay = points.maxOf { it.displayValue },
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = TrendsUiState.Loading,
    )

    fun onRangeSelected(range: TrendsTimeRange) {
        _selectedRange.value = range
    }

    private fun filterByRange(
        entries: List<com.weightflow.domain.WeightEntry>,
        range: TrendsTimeRange,
    ): List<com.weightflow.domain.WeightEntry> {
        val days = range.labelDays ?: return entries
        val cutoff = LocalDate.now()
            .minusDays(days.toLong())
            .atStartOfDay(ZoneId.systemDefault())
            .toInstant()
            .toEpochMilli()
        return entries.filter { it.timestamp >= cutoff }
    }

    private fun toDisplayValue(kg: Double, unit: WeightUnit): Float = when (unit) {
        WeightUnit.KG -> kg.toFloat()
        WeightUnit.LBS -> WeightConverter.kgToLbs(kg).toFloat()
        WeightUnit.ST -> WeightConverter.kgToLbs(kg).toFloat() // stones shown as lbs for chart
    }
}
