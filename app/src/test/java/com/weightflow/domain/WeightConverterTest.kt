package com.weightflow.domain

import org.junit.Assert.assertEquals
import org.junit.Test

/**
 * TDD: These tests were written BEFORE WeightConverter exists.
 * Run them — they will all fail. Then implement WeightConverter to make them pass.
 *
 * All weights stored internally in kg. Unit conversion is display-only.
 */
class WeightConverterTest {

    private val tolerance = 0.01

    // ── kg ↔ lbs ────────────────────────────────────────────────────────────

    @Test
    fun `kgToLbs converts 80kg to 176_37lbs`() {
        assertEquals(176.37, WeightConverter.kgToLbs(80.0), tolerance)
    }

    @Test
    fun `kgToLbs converts 0kg to 0lbs`() {
        assertEquals(0.0, WeightConverter.kgToLbs(0.0), tolerance)
    }

    @Test
    fun `kgToLbs converts 1kg correctly`() {
        assertEquals(2.205, WeightConverter.kgToLbs(1.0), tolerance)
    }

    @Test
    fun `lbsToKg converts 176_37lbs to approximately 80kg`() {
        assertEquals(80.0, WeightConverter.lbsToKg(176.37), tolerance)
    }

    @Test
    fun `kg to lbs round trip stays within tolerance`() {
        val original = 73.5
        val roundTripped = WeightConverter.lbsToKg(WeightConverter.kgToLbs(original))
        assertEquals(original, roundTripped, tolerance)
    }

    @Test
    fun `kgToLbs handles large value`() {
        assertEquals(441.0, WeightConverter.kgToLbs(200.0), 0.5)
    }

    // ── kg ↔ stones ─────────────────────────────────────────────────────────

    @Test
    fun `kgToStones converts 80kg to 12 stones 8 lbs`() {
        val result = WeightConverter.kgToStones(80.0)
        assertEquals(12, result.stones)
        assertEquals(8, result.pounds)
    }

    @Test
    fun `kgToStones converts 0kg to 0 stones 0 lbs`() {
        val result = WeightConverter.kgToStones(0.0)
        assertEquals(0, result.stones)
        assertEquals(0, result.pounds)
    }

    @Test
    fun `stonesToKg round trip stays within tolerance`() {
        val original = 80.0
        val stones = WeightConverter.kgToStones(original)
        val roundTripped = WeightConverter.stonesToKg(stones.stones, stones.pounds)
        // Integer stones+lbs loses fractional pound precision (~0.45kg max drift)
        assertEquals(original, roundTripped, 0.5)
    }

    // ── stToKg (decimal stones input) ───────────────────────────────────────

    @Test
    fun `stToKg converts 1 stone to approximately 6 35 kg`() {
        assertEquals(6.35029, WeightConverter.stToKg(1.0), 0.001)
    }

    @Test
    fun `stToKg converts 12 point 8 stones to approximately 81 kg`() {
        assertEquals(81.284, WeightConverter.stToKg(12.8), 0.01)
    }

    @Test
    fun `stToKg zero returns zero`() {
        assertEquals(0.0, WeightConverter.stToKg(0.0), 0.001)
    }
}
