package com.weightflow.ui.history

import java.util.Locale

/**
 * Resolved, locale-aware strings the [HistoryViewModel] needs to format weights and
 * relative dates into its UiState. Built by the UI layer (where Context + stringResource
 * are available) and pushed into the ViewModel so it stays Android-Context-free and
 * unit-testable, and so formatting tracks AppCompat locale changes.
 */
data class HistoryStrings(
    val locale: Locale,
    val kgSuffix: String,
    val lbsSuffix: String,
    val stSuffix: String,
    val lbSuffix: String,
    val today: String,
)
