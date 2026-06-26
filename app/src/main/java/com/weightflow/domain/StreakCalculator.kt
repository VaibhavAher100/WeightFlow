package com.weightflow.domain

import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId

/**
 * Consecutive-day logging streak, counted back from today (or yesterday if today
 * has no entry yet). Shared by Home and Profile — single source of truth.
 */
fun computeStreak(entries: List<WeightEntry>): Int {
    if (entries.isEmpty()) return 0
    val zone = ZoneId.systemDefault()
    val days = entries.map {
        Instant.ofEpochMilli(it.timestamp).atZone(zone).toLocalDate()
    }.toSet()
    var streak = 0
    var current = LocalDate.now()
    if (!days.contains(current)) current = current.minusDays(1)
    while (days.contains(current)) {
        streak++
        current = current.minusDays(1)
    }
    return streak
}
