package com.mcnut.banking.types

data class TrendSummary(
    val year: Int,
    val month: String,
    val category: String,
    val totalCost: Double
)