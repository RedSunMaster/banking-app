package com.mcnut.banking.types

// Transaction.kt
data class Transaction(
    val ID: Int? = null,
    val Date: String,
    val Amount: Double,
    val Description: String,
    val Category: String,
    val TransType: String
)
