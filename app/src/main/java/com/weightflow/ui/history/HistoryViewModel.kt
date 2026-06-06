package com.weightflow.ui.history

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.weightflow.data.UserPrefsDataStore
import com.weightflow.data.WeightRepository
import com.weightflow.ui.i18n.DateFormatters
import com.weightflow.ui.i18n.WeightFormatter
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId

class HistoryViewModel(
    private val weightRepository: WeightRepository,
    private val userPrefsDataStore: UserPrefsDataStore,
) : ViewModel() {

    private val strings = MutableStateFlow<HistoryStrings?>(null)

    /** Called by the UI with locale-resolved strings; updates live on locale change. */
    fun setStrings(value: HistoryStrings) { strings.value = value }

    val uiState: StateFlow<HistoryUiState> = combine(
        weightRepository.getEntriesNewestFirst(),
        userPrefsDataStore.weightUnit,
        strings.filterNotNull(),
    ) { entries, unit, s ->
        if (entries.isEmpty()) return@combine HistoryUiState.Empty

        val displayList = entries.mapIndexed { i, entry ->
            val olderEntry = entries.getOrNull(i + 1)
            val deltaKg = olderEntry?.let { entry.weightKg - it.weightKg }
            val deltaDisplay = deltaKg?.let { delta ->
                WeightFormatter.format(
                    kg = kotlin.math.abs(delta),
                    unit = unit,
                    locale = s.locale,
                    kgSuffix = s.kgSuffix,
                    lbsSuffix = s.lbsSuffix,
                    stSuffix = s.stSuffix,
                    lbSuffix = s.lbSuffix,
                )
            }
            HistoryEntryDisplay(
                id = entry.id,
                weightKg = entry.weightKg,
                weightDisplay = WeightFormatter.format(
                    kg = entry.weightKg,
                    unit = unit,
                    locale = s.locale,
                    kgSuffix = s.kgSuffix,
                    lbsSuffix = s.lbsSuffix,
                    stSuffix = s.stSuffix,
                    lbSuffix = s.lbSuffix,
                ),
                dateDisplay = formatDate(entry.timestamp, s),
                timestamp = entry.timestamp,
                deltaDisplay = deltaDisplay,
                deltaIsDown = deltaKg?.let { it < 0 },
                deltaIsZero = deltaKg?.let { kotlin.math.abs(it) < DELTA_ZERO_THRESHOLD_KG },
            )
        }
        HistoryUiState.HasData(entries = displayList, weightUnit = unit)
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = HistoryUiState.Loading,
    )

    fun onDelete(entryId: Long) {
        viewModelScope.launch {
            weightRepository.removeEntry(entryId)
        }
    }

    fun onEditEntry(id: Long, newWeightKg: Double) {
        viewModelScope.launch {
            weightRepository.updateEntry(id, newWeightKg)
        }
    }

    private fun formatDate(timestamp: Long, s: HistoryStrings): String {
        val date = Instant.ofEpochMilli(timestamp)
            .atZone(ZoneId.systemDefault())
            .toLocalDate()
        return if (date == LocalDate.now()) {
            s.today
        } else {
            date.format(DateFormatters.fullDate(s.locale))
        }
    }

    private companion object {
        /** Deltas smaller than this (in kg) are treated as "no change" for the UI chip. */
        const val DELTA_ZERO_THRESHOLD_KG = 0.05
    }
}
