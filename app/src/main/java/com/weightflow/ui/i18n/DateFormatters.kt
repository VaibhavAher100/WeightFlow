package com.weightflow.ui.i18n

import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

/**
 * Central locale-aware date formatting. Callers pass the locale explicitly so the
 * functions stay pure/testable; UI passes Locale.getDefault() (which AppCompat updates).
 */
object DateFormatters {

    enum class RelativeDay { TODAY, YESTERDAY, OTHER }

    fun dayMonth(locale: Locale): DateTimeFormatter =
        DateTimeFormatter.ofPattern("d MMM", locale)

    fun weekday(locale: Locale): DateTimeFormatter =
        DateTimeFormatter.ofPattern("EEE", locale)

    fun fullDate(locale: Locale): DateTimeFormatter =
        DateTimeFormatter.ofPattern("d MMM yyyy", locale)

    fun monthYear(locale: Locale): DateTimeFormatter =
        DateTimeFormatter.ofPattern("MMMM yyyy", locale)

    fun weekdayDayMonth(locale: Locale): DateTimeFormatter =
        DateTimeFormatter.ofPattern("EEEE, d MMM", locale)

    fun relativeDay(date: LocalDate, today: LocalDate = LocalDate.now()): RelativeDay = when (date) {
        today -> RelativeDay.TODAY
        today.minusDays(1) -> RelativeDay.YESTERDAY
        else -> RelativeDay.OTHER
    }
}
