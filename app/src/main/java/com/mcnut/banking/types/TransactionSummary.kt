package com.mcnut.banking.types

data class TransactionSummary(
    val Month: Int,
    val Item: String,
    val Amount: Int,
    val Cost: Double,
    val AvgCost: Double,
    val Category: String,
    val PercentageDifference: Double
)