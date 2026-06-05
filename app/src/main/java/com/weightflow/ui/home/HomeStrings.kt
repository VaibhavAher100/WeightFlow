package com.weightflow.ui.home

import java.util.Locale

/**
 * Resolved, locale-aware strings the [HomeUiStateMapper] needs to format weights and
 * relative dates. Built by the UI layer (where Context + stringResource are available)
 * and passed into the pure mapper so it stays Android-Context-free and unit-testable.
 */
data class HomeStrings(
    val locale: Locale,
    val kgSuffix: String,
    val lbsSuffix: String,
    val stSuffix: String,
    val lbSuffix: String,
    val today: String,
    val yesterday: String,
)
