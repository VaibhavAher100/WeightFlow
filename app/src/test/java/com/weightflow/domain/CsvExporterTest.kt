package com.weightflow.domain

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDate

/**
 * TDD: These tests were written BEFORE CsvExporter exists.
 * Run them — they will all fail. Then implement CsvExporter to make them pass.
 */
class CsvExporterTest {

    private fun entry(daysAgo: Int, weightKg: Double) = WeightEntry(
        id = daysAgo.toLong(),
        timestamp = LocalDate.now().minusDays(daysAgo.toLong()).toEpochDay() * 86_400_000L,
        weightKg = weightKg,
        note = ""
    )

    // ── Headers ──────────────────────────────────────────────────────────────

    @Test
    fun `exports kg header when unit is KG`() {
        val csv = CsvExporter.export(emptyList(), WeightUnit.KG)
        assertTrue(csv.startsWith("date,weight_kg"))
    }

    @Test
    fun `exports lbs header when unit is LBS`() {
        val csv = CsvExporter.export(emptyList(), WeightUnit.LBS)
        assertTrue(csv.startsWith("date,weight_lbs"))
    }

    @Test
    fun `exports st header when unit is ST`() {
        val csv = CsvExporter.export(emptyList(), WeightUnit.ST)
        assertTrue(csv.startsWith("date,weight_st"))
    }

    // ── Unit conversion ───────────────────────────────────────────────────────

    @Test
    fun `exports weight in kg without conversion`() {
        val entries = listOf(entry(0, weightKg = 80.0))
        val csv = CsvExporter.export(entries, WeightUnit.KG)
        assertTrue(csv.contains("80.0"))
    }

    @Test
    fun `exports weight converted to lbs`() {
        val entries = listOf(entry(0, weightKg = 80.0))
        val csv = CsvExporter.export(entries, WeightUnit.LBS)
        // 80kg = ~176.4 lbs
        assertTrue(csv.contains("176."))
    }

    // ── Ordering ─────────────────────────────────────────────────────────────

    @Test
    fun `exports entries in chronological order oldest first`() {
        val entries = listOf(
            entry(daysAgo = 0, weightKg = 79.0),
            entry(daysAgo = 2, weightKg = 81.0),
            entry(daysAgo = 1, weightKg = 80.0)
        )
        val lines = CsvExporter.export(entries, WeightUnit.KG)
            .lines()
            .filter { it.isNotBlank() }
            .drop(1) // skip header

        assertTrue(lines[0].contains("81.0")) // oldest (2 days ago) is first
        assertTrue(lines[1].contains("80.0"))
        assertTrue(lines[2].contains("79.0")) // most recent is last
    }

    // ── Date format ───────────────────────────────────────────────────────────

    @Test
    fun `date is formatted as ISO-8601 yyyy-MM-dd`() {
        val entries = listOf(entry(0, weightKg = 80.0))
        val csv = CsvExporter.export(entries, WeightUnit.KG)
        val today = LocalDate.now().toString() // yyyy-MM-dd
        assertTrue(csv.contains(today))
    }

    // ── Empty list ────────────────────────────────────────────────────────────

    @Test
    fun `empty entry list returns header row only`() {
        val csv = CsvExporter.export(emptyList(), WeightUnit.KG)
        val lines = csv.lines().filter { it.isNotBlank() }
        assertEquals(1, lines.size) // only header
    }

    // ── Multiple entries ──────────────────────────────────────────────────────

    @Test
    fun `exports correct number of data rows`() {
        val entries = (0 until 5).map { entry(it, 80.0 - it * 0.3) }
        val csv = CsvExporter.export(entries, WeightUnit.KG)
        val dataLines = csv.lines().filter { it.isNotBlank() }.drop(1)
        assertEquals(5, dataLines.size)
    }
}
