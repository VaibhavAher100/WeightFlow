package com.weightflow.ui.i18n

import com.weightflow.domain.WeightUnit
import org.junit.Assert.assertEquals
import org.junit.Test
import java.util.Locale

class WeightFormatterTest {

    @Test
    fun `kg formats with one decimal and suffix in English`() {
        val out = WeightFormatter.format(
            kg = 72.55, unit = WeightUnit.KG, locale = Locale.ENGLISH,
            kgSuffix = "kg", lbsSuffix = "lbs", stSuffix = "st", lbSuffix = "lb",
        )
        assertEquals("72.6 kg", out)
    }

    @Test
    fun `kg uses comma decimal separator in German locale`() {
        val out = WeightFormatter.format(
            kg = 72.55, unit = WeightUnit.KG, locale = Locale.GERMAN,
            kgSuffix = "kg", lbsSuffix = "lbs", stSuffix = "st", lbSuffix = "lb",
        )
        assertEquals("72,6 kg", out)
    }

    @Test
    fun `lbs converts and appends suffix`() {
        val out = WeightFormatter.format(
            kg = 100.0, unit = WeightUnit.LBS, locale = Locale.ENGLISH,
            kgSuffix = "kg", lbsSuffix = "lbs", stSuffix = "st", lbSuffix = "lb",
        )
        assertEquals("220.5 lbs", out)
    }

    @Test
    fun `stones formats stones and pounds with suffixes`() {
        val out = WeightFormatter.format(
            kg = 100.0, unit = WeightUnit.ST, locale = Locale.ENGLISH,
            kgSuffix = "kg", lbsSuffix = "lbs", stSuffix = "st", lbSuffix = "lb",
        )
        assertEquals("15st 10lb", out)
    }
}
