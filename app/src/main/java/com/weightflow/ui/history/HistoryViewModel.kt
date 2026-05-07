package com.weightflow.ui.history

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.weightflow.data.UserPrefsDataStore
import com.weightflow.data.WeightRepository
import com.weightflow.domain.WeightConverter
import com.weightflow.domain.WeightUnit
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter

class HistoryViewModel(
    private val weightRepository: WeightRepository,
    private val userPrefsDataStore: UserPrefsDataStore,
) : ViewModel() {

    private val dateFmt = DateTimeFormatter.ofPattern("d MMM yyyy")

    val uiState: StateFlow<HistoryUiState> = combine(
        weightRepository.getEntriesNewestFirst(),
        userPrefsDataStore.weightUnit,
    ) { entries, unit ->
        if (entries.isEmpty()) return@combine HistoryUiState.Empty

        val displayList = entries.mapIndexed { i, entry ->
            val olderEntry = entries.getOrNull(i + 1)
            val deltaKg = olderEntry?.let { entry.weightKg - it.weightKg }
            val deltaDisplay = deltaKg?.let { delta ->
                val absKg = kotlin.math.abs(delta)
                when (unit) {
                    WeightUnit.KG  -> "%.1f kg".format(absKg)
                    WeightUnit.LBS -> "%.1f lbs".format(WeightConverter.kgToLbs(absKg))
                    WeightUnit.ST  -> {
                        val r = WeightConverter.kgToStones(absKg)
                        "${r.stones}st ${r.pounds}lb"
                    }
                }
            }
            HistoryEntryDisplay(
                id = entry.id,
                weightKg = entry.weightKg,
                weightDisplay = WeightConverter.format(entry.weightKg, unit),
                dateDisplay = formatDate(entry.timestamp),
                timestamp = entry.timestamp,
                deltaDisplay = deltaDisplay,
                deltaIsDown = deltaKg?.let { it < 0 },
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

    private fun formatDate(timestamp: Long): String {
        val date = Instant.ofEpochMilli(timestamp)
            .atZone(ZoneId.systemDefault())
            .toLocalDate()
        return if (date == LocalDate.now()) "Today" else date.format(dateFmt)
    }
}
