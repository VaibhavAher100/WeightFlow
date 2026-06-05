package com.weightflow.ui.profile

import java.util.Locale

/**
 * Resolved, locale-aware strings the [ProfileViewModel] needs to pre-format weights,
 * BMI category labels, and the goal-summary label into its UiState. Built by the UI
 * layer (where Context + stringResource are available) and pushed into the ViewModel so
 * it stays Android-Context-free and unit-testable, and so formatting tracks AppCompat
 * locale changes (the ViewModel survives the locale-change recreation).
 */
data class ProfileStrings(
    val locale: Locale,
    val kgSuffix: String,
    val lbsSuffix: String,
    val stSuffix: String,
    val lbSuffix: String,
    val bmiUnderweight: String,
    val bmiNormal: String,
    val bmiOverweight: String,
    val bmiObese: String,
    /** Template: "−%1$s over %2$d days  %3$s" (lost weight, days, percent). */
    val goalSummaryTemplate: String,
)
