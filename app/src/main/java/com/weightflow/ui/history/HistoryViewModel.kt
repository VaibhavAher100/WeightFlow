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

        val displayList = entries.map { entry ->
            HistoryEntryDisplay(
                id = entry.id,
                weightDisplay = WeightConverter.format(entry.weightKg, unit),
                dateDisplay = formatDate(entry.timestamp),
                timestamp = entry.timestamp,
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

    private fun formatDate(timestamp: Long): String {
        val date = Instant.ofEpochMilli(timestamp)
            .atZone(ZoneId.systemDefault())
            .toLocalDate()
        return if (date == LocalDate.now()) "Today" else date.format(dateFmt)
    }
}
