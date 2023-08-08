package com.mcnut.banking.types

import androidx.compose.ui.graphics.Color

data class BalanceItem(
    val category: String,
    val amount: Double,
    val colour: Color
    )