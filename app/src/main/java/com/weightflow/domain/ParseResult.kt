package com.weightflow.domain

sealed class ParseResult {
    data class Success(val entries: List<WeightEntry>, val duplicatesSkipped: Int) : ParseResult()
    data class Error(val message: String) : ParseResult()
}
