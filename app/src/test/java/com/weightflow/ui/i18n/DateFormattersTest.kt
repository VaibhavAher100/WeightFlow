package com.weightflow.ui.i18n

import org.junit.Assert.assertEquals
import org.junit.Test
import java.time.LocalDate
import java.util.Locale

class DateFormattersTest {

    @Test
    fun `month short formats in German locale`() {
        val date = LocalDate.of(2026, 3, 9)
        val formatted = DateFormatters.dayMonth(Locale.GERMAN).format(date)
        assertEquals("9 März", formatted)
    }

    @Test
    fun `month short formats in English locale`() {
        val date = LocalDate.of(2026, 3, 9)
        val formatted = DateFormatters.dayMonth(Locale.ENGLISH).format(date)
        assertEquals("9 Mar", formatted)
    }

    @Test
    fun `relativeDay returns Today key for current date`() {
        assertEquals(DateFormatters.RelativeDay.TODAY, DateFormatters.relativeDay(LocalDate.now()))
    }

    @Test
    fun `relativeDay returns Yesterday key for previous date`() {
        assertEquals(
            DateFormatters.RelativeDay.YESTERDAY,
            DateFormatters.relativeDay(LocalDate.now().minusDays(1)),
        )
    }

    @Test
    fun `relativeDay returns Other for two days ago`() {
        assertEquals(
            DateFormatters.RelativeDay.OTHER,
            DateFormatters.relativeDay(LocalDate.now().minusDays(2)),
        )
    }
}
