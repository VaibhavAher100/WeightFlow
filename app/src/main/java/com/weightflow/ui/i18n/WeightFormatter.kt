package com.weightflow.ui.i18n

import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.stringResource
import com.weightflow.R
import com.weightflow.domain.WeightConverter
import com.weightflow.domain.WeightUnit
import java.util.Locale

/**
 * UI-layer weight display. Pure: takes Locale + resolved unit suffix strings so it is
 * unit-testable without Android Context. Reuses the pure numeric converters in domain.
 */
object WeightFormatter {

    fun format(
        kg: Double,
        unit: WeightUnit,
        locale: Locale,
        kgSuffix: String,
        lbsSuffix: String,
        stSuffix: String,
        lbSuffix: String,
    ): String = when (unit) {
        WeightUnit.KG -> "${oneDecimal(kg, locale)} $kgSuffix"
        WeightUnit.LBS -> "${oneDecimal(WeightConverter.kgToLbs(kg), locale)} $lbsSuffix"
        WeightUnit.ST -> {
            val r = WeightConverter.kgToStones(kg)
            "${r.stones}$stSuffix ${r.pounds}$lbSuffix"
        }
    }

    private fun oneDecimal(value: Double, locale: Locale): String =
        String.format(locale, "%.1f", value)
}

/**
 * Compose helper for screens that format a weight directly in the UI (not via a
 * ViewModel UiState). Resolves the active locale + unit suffixes from resources.
 */
@Composable
fun rememberWeightString(kg: Double, unit: WeightUnit): String {
    val locale = LocalConfiguration.current.locales[0]
    return WeightFormatter.format(
        kg = kg,
        unit = unit,
        locale = locale,
        kgSuffix = stringResource(R.string.unit_suffix_kg),
        lbsSuffix = stringResource(R.string.unit_suffix_lbs),
        stSuffix = stringResource(R.string.unit_suffix_st_stones),
        lbSuffix = stringResource(R.string.unit_suffix_st_pounds),
    )
}
