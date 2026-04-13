package com.weightflow.domain

/**
 * RFC #27: Type-safe wrapper for weight values stored in Room (always kg).
 * Prevents accidental mixing of raw Doubles from different contexts.
 */
@JvmInline
value class StoredWeight(val kg: Double)
