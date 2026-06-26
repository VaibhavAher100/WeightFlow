package com.weightflow.domain

const val MIN_WEIGHT_KG = 0.5
const val MAX_WEIGHT_KG = 635.0

fun Double.isValidWeightKg(): Boolean = isFinite() && this in MIN_WEIGHT_KG..MAX_WEIGHT_KG

data class StonesResult(val stones: Int, val pounds: Int)

object WeightConverter {

    private const val KG_TO_LBS = 2.20462
    private const val LBS_PER_STONE = 14.0

    fun kgToLbs(kg: Double): Double = kg * KG_TO_LBS

    fun lbsToKg(lbs: Double): Double = lbs / KG_TO_LBS

    /** Converts decimal stones (e.g. 12.8) to kilograms. 1 stone = 6.35029 kg. */
    fun stToKg(decimalStones: Double): Double = decimalStones * 6.35029

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

}
