package com.weightflow.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.weightflow.domain.HomeDataAggregator
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

class HomeViewModel(aggregator: HomeDataAggregator) : ViewModel() {

    val uiState: StateFlow<HomeUiState> = aggregator.homeData
        .map { HomeUiStateMapper.map(it) }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = HomeUiState.Loading,
        )
}
