package com.mcnut.banking.types

data class OwedItem (
    val ID: Int,
    val userId: Int,
    val Person: String,
    val Amount: Double,
    val Category: String,
    val Date: String,
    val Description: String,
    val DaysElapsed: Int,
    val Payed: Int
)