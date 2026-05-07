package com.weightflow.domain

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * TDD: CsvImporter test suite.
 * Tests the single entry point that wraps all four CSV parsers and sets the format field.
 */
class CsvImporterTest {

    @Test
    fun `CsvImporter detects WeightFit format`() {
        val csv = """
            Date,Weight
            2024-01-01,80.5
        """.trimIndent()

        val result = CsvImporter.import(csv, existingEntries = emptyList())

        assertTrue(result is ParseResult.Success)
        val success = result as ParseResult.Success
        assertEquals(1, success.entries.size)
        assertEquals(CsvFormat.WEIGHT_FIT, success.format)
    }

    @Test
    fun `CsvImporter detects HappyScale format`() {
        val csv = """
            "Date","Pounds"
            "2024-01-01","176.4"
        """.trimIndent()

        val result = CsvImporter.import(csv, existingEntries = emptyList())

        assertTrue(result is ParseResult.Success)
        val success = result as ParseResult.Success
        assertEquals(1, success.entries.size)
        assertEquals(CsvFormat.HAPPY_SCALE, success.format)
    }

    @Test
    fun `CsvImporter detects AppleHealth format`() {
        val csv = """
            type,sourceName,sourceVersion,device,unit,creationDate,startDate,endDate,value
            HKQuantityTypeIdentifierBodyMass,iPhone,,,kg,2024-01-01 08:00:00 +0000,2024-01-01 08:00:00 +0000,2024-01-01 08:00:00 +0000,80.5
        """.trimIndent()

        val result = CsvImporter.import(csv, existingEntries = emptyList())

        assertTrue(result is ParseResult.Success)
        val success = result as ParseResult.Success
        assertEquals(1, success.entries.size)
        assertEquals(CsvFormat.APPLE_HEALTH, success.format)
    }

    @Test
    fun `CsvImporter falls back to Generic format`() {
        val csv = """
            date,weight_kg
            2024-01-01,80.5
        """.trimIndent()

        val result = CsvImporter.import(csv, existingEntries = emptyList())

        assertTrue(result is ParseResult.Success)
        val success = result as ParseResult.Success
        assertEquals(1, success.entries.size)
        assertEquals(CsvFormat.GENERIC, success.format)
    }

    @Test
    fun `CsvImporter returns Error when nothing matches`() {
        val csv = "this is not a csv at all"

        val result = CsvImporter.import(csv, existingEntries = emptyList())

        assertTrue(result is ParseResult.Error)
    }

    @Test
    fun `CsvImporter rejects file larger than 5MB`() {
        val csv = "x".repeat(5_000_001)

        val result = CsvImporter.import(csv, existingEntries = emptyList())

        assertTrue(result is ParseResult.Error)
        assertEquals("File too large (max 5 MB)", (result as ParseResult.Error).message)
    }
}
