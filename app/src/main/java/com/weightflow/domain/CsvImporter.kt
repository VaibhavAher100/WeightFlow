package com.weightflow.domain

object CsvImporter {

    /**
     * Single entry point for CSV import.
     * Tries each parser in order: HappyScale → AppleHealth → Generic → WeightFit.
     * Detection order prioritizes more specific/distinctive formats first.
     * If a parser succeeds, sets the format field and returns.
     * If all parsers fail, returns the last error.
     */
    fun import(csv: String, existingEntries: List<WeightEntry>): ParseResult {
        if (csv.length > 5_000_000) return ParseResult.Error("File too large (max 5 MB)")
        // Try HappyScale first (distinctive: quoted headers + "Pounds")
        val happyScaleResult = HappyScaleParser.parse(csv, existingEntries)
        if (happyScaleResult is ParseResult.Success) {
            return happyScaleResult.copy(format = CsvFormat.HAPPY_SCALE)
        }

        // Try AppleHealth (distinctive: "type", "HKQuantityTypeIdentifierBodyMass")
        val appleHealthResult = AppleHealthParser.parse(csv, existingEntries)
        if (appleHealthResult is ParseResult.Success) {
            return appleHealthResult.copy(format = CsvFormat.APPLE_HEALTH)
        }

        // Try Generic (distinctive: "weight_kg" or "weight_lbs" specific header)
        val genericResult = GenericCsvParser.parse(csv, existingEntries)
        if (genericResult is ParseResult.Success) {
            return genericResult.copy(format = CsvFormat.GENERIC)
        }

        // Try WeightFit last (generic: just "date" + "weight")
        val weightFitResult = WeightFitParser.parse(csv, existingEntries)
        if (weightFitResult is ParseResult.Success) {
            return weightFitResult.copy(format = CsvFormat.WEIGHT_FIT)
        }

        // All parsers failed — return the last error
        return weightFitResult
    }
}
