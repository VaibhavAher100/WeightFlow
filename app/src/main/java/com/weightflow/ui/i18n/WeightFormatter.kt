package com.weightflow.ui.i18n

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
