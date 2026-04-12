package com.weightflow.domain

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * TDD: These tests were written BEFORE the CSV parsers exist.
 * Run them — they will all fail. Then implement each parser to make them pass.
 *
 * All parsers output weights in kg regardless of input unit.
 * Duplicate: same calendar day + weightKg within 0.01 of an existing entry.
 */
class CsvParserTest {

    // ── WeightFitParser ──────────────────────────────────────────────────────

    @Test
    fun `WeightFitParser parses valid CSV with kg values`() {
        val csv = """
            Date,Weight
            2024-01-01,80.5
            2024-01-02,80.2
            2024-01-03,79.8
        """.trimIndent()

        val result = WeightFitParser.parse(csv, existingEntries = emptyList())

        assertTrue(result is ParseResult.Success)
        val entries = (result as ParseResult.Success).entries
        assertEquals(3, entries.size)
        assertEquals(80.5, entries[0].weightKg, 0.01)
        assertEquals(80.2, entries[1].weightKg, 0.01)
        assertEquals(79.8, entries[2].weightKg, 0.01)
    }

    @Test
    fun `WeightFitParser converts lbs input to kg`() {
        val csv = """
            Date,Weight (lbs)
            2024-01-01,176.4
        """.trimIndent()

        val result = WeightFitParser.parse(csv, existingEntries = emptyList())

        assertTrue(result is ParseResult.Success)
        val entries = (result as ParseResult.Success).entries
        assertEquals(80.0, entries[0].weightKg, 0.1)
    }

    @Test
    fun `WeightFitParser returns error for malformed CSV`() {
        val csv = "this is not a csv file at all !@#$"
        val result = WeightFitParser.parse(csv, existingEntries = emptyList())
        assertTrue(result is ParseResult.Error)
    }

    @Test
    fun `WeightFitParser returns empty success for empty file`() {
        val result = WeightFitParser.parse("", existingEntries = emptyList())
        assertTrue(result is ParseResult.Success)
        assertEquals(0, (result as ParseResult.Success).entries.size)
    }

    // ── HappyScaleParser ─────────────────────────────────────────────────────

    @Test
    fun `HappyScaleParser parses valid Happy Scale export`() {
        val csv = """
            "Date","Pounds"
            "2024-01-01","176.4"
            "2024-01-02","175.8"
        """.trimIndent()

        val result = HappyScaleParser.parse(csv, existingEntries = emptyList())

        assertTrue(result is ParseResult.Success)
        val entries = (result as ParseResult.Success).entries
        assertEquals(2, entries.size)
        assertEquals(80.0, entries[0].weightKg, 0.1)
    }

    @Test
    fun `HappyScaleParser returns error for unrecognised format`() {
        val csv = "unknown,headers,here\n1,2,3"
        val result = HappyScaleParser.parse(csv, existingEntries = emptyList())
        assertTrue(result is ParseResult.Error)
    }

    // ── AppleHealthParser ────────────────────────────────────────────────────

    @Test
    fun `AppleHealthParser parses Apple Health body mass export`() {
        val csv = """
            type,sourceName,sourceVersion,device,unit,creationDate,startDate,endDate,value
            HKQuantityTypeIdentifierBodyMass,iPhone,,,kg,2024-01-01 08:00:00 +0000,2024-01-01 08:00:00 +0000,2024-01-01 08:00:00 +0000,80.5
        """.trimIndent()

        val result = AppleHealthParser.parse(csv, existingEntries = emptyList())

        assertTrue(result is ParseResult.Success)
        val entries = (result as ParseResult.Success).entries
        assertEquals(1, entries.size)
        assertEquals(80.5, entries[0].weightKg, 0.01)
    }

    // ── GenericCsvParser ─────────────────────────────────────────────────────

    @Test
    fun `GenericCsvParser detects date and weight columns by header`() {
        val csv = """
            date,weight_kg
            2024-01-01,80.5
            2024-01-02,80.2
        """.trimIndent()

        val result = GenericCsvParser.parse(csv, existingEntries = emptyList())

        assertTrue(result is ParseResult.Success)
        assertEquals(2, (result as ParseResult.Success).entries.size)
    }

    @Test
    fun `GenericCsvParser handles weight_lbs header and converts to kg`() {
        val csv = """
            date,weight_lbs
            2024-01-01,176.4
        """.trimIndent()

        val result = GenericCsvParser.parse(csv, existingEntries = emptyList())

        assertTrue(result is ParseResult.Success)
        val entries = (result as ParseResult.Success).entries
        assertEquals(80.0, entries[0].weightKg, 0.1)
    }

    @Test
    fun `GenericCsvParser returns error when no recognisable headers found`() {
        val csv = "foo,bar\n1,2"
        val result = GenericCsvParser.parse(csv, existingEntries = emptyList())
        assertTrue(result is ParseResult.Error)
    }

    // ── Duplicate detection ──────────────────────────────────────────────────

    @Test
    fun `duplicate entries are skipped and counted`() {
        val existing = listOf(
            WeightEntry(id = 1, timestamp = dateToTimestamp("2024-01-01"),
                weightKg = 80.5, note = "")
        )
        val csv = """
            Date,Weight
            2024-01-01,80.5
            2024-01-02,80.2
        """.trimIndent()

        val result = WeightFitParser.parse(csv, existingEntries = existing)

        assertTrue(result is ParseResult.Success)
        val success = result as ParseResult.Success
        assertEquals(1, success.entries.size)
        assertEquals(1, success.duplicatesSkipped)
    }

    @Test
    fun `entry with same date but different weight is not a duplicate`() {
        val existing = listOf(
            WeightEntry(id = 1, timestamp = dateToTimestamp("2024-01-01"),
                weightKg = 80.5, note = "")
        )
        val csv = """
            Date,Weight
            2024-01-01,79.0
        """.trimIndent()

        val result = WeightFitParser.parse(csv, existingEntries = existing)

        assertTrue(result is ParseResult.Success)
        val success = result as ParseResult.Success
        assertEquals(1, success.entries.size)
        assertEquals(0, success.duplicatesSkipped)
    }

    // ── Helper ───────────────────────────────────────────────────────────────

    private fun dateToTimestamp(dateStr: String): Long {
        val (year, month, day) = dateStr.split("-").map { it.toInt() }
        return java.time.LocalDate.of(year, month, day).toEpochDay() * 86_400_000L
    }
}
