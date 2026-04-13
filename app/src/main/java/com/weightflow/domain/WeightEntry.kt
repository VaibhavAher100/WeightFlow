package com.weightflow.domain

data class WeightEntry(
    val id: Long,
    val timestamp: Long,
    val weightKg: Double,
    val note: String
)
