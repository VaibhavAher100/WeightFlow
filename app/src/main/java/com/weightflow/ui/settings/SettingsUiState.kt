package com.weightflow.ui.settings

import com.weightflow.domain.WeightUnit

data class SettingsUiState(
    val themePalette: String = "lime",
    val weightUnit: WeightUnit = WeightUnit.KG,
)
