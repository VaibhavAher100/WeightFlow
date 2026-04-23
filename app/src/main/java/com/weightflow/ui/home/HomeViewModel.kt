package com.weightflow.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.weightflow.domain.Badge
import com.weightflow.domain.BadgeObserver
import com.weightflow.domain.HomeDataAggregator
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class HomeViewModel(
    aggregator: HomeDataAggregator,
    private val badgeObserver: BadgeObserver,
) : ViewModel() {

    val uiState: StateFlow<HomeUiState> = aggregator.homeData
        .map { HomeUiStateMapper.map(it) }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = HomeUiState.Loading,
        )

    private val _badgeEvents = MutableSharedFlow<Set<Badge>>()
    val badgeEvents: SharedFlow<Set<Badge>> = _badgeEvents.asSharedFlow()

    init {
        viewModelScope.launch {
            badgeObserver.newlyUnlockedBadges.collect { badges ->
                if (badges.isNotEmpty()) _badgeEvents.emit(badges)
            }
        }
    }

    fun onBadgeShown(badges: Set<Badge>) {
        viewModelScope.launch { badgeObserver.markSeen(badges) }
    }
}
