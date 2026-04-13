package com.weightflow.domain

import java.time.LocalDate

object CsvExporter {

    fun export(entries: List<WeightEntry>, unit: WeightUnit): String {
        val header = when (unit) {
            WeightUnit.KG -> "date,weight_kg"
            WeightUnit.LBS -> "date,weight_lbs"
            WeightUnit.ST -> "date,weight_st"
        }

        val rows = entries.sortedBy { it.timestamp }.map { entry ->
            val date = LocalDate.ofEpochDay(entry.timestamp / 86_400_000L)
            val weight = when (unit) {
                WeightUnit.KG -> "%.1f".format(entry.weightKg)
                WeightUnit.LBS -> "%.1f".format(WeightConverter.kgToLbs(entry.weightKg))
                WeightUnit.ST -> {
                    val totalStones = entry.weightKg / 6.35029
                    "%.2f".format(totalStones)
                }
            }
            "$date,$weight"
        }

        return buildString {
            append(header)
            rows.forEach { append('\n'); append(it) }
        }
    }
}
