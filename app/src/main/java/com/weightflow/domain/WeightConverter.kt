package com.weightflow.domain

data class StonesResult(val stones: Int, val pounds: Int)

object WeightConverter {

    private const val KG_TO_LBS = 2.20462
    private const val LBS_PER_STONE = 14.0

    fun kgToLbs(kg: Double): Double = kg * KG_TO_LBS

    fun lbsToKg(lbs: Double): Double = lbs / KG_TO_LBS

    fun kgToStones(kg: Double): StonesResult {
        val totalLbs = kgToLbs(kg)
        val stones = (totalLbs / LBS_PER_STONE).toInt()
        val remainingPounds = (totalLbs % LBS_PER_STONE).toInt()
        return StonesResult(stones, remainingPounds)
    }

    fun stonesToKg(stones: Int, pounds: Int): Double {
        val totalLbs = stones * LBS_PER_STONE + pounds
        return lbsToKg(totalLbs)
    }

    fun format(kg: Double, unit: WeightUnit): String = when (unit) {
        WeightUnit.KG -> "${"%.1f".format(kg)} kg"
        WeightUnit.LBS -> "${"%.1f".format(kgToLbs(kg))} lbs"
        WeightUnit.ST -> {
            val r = kgToStones(kg)
            "${r.stones}st ${r.pounds}lb"
        }
    }
}
