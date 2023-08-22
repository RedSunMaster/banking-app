package com.mcnut.banking.types

import androidx.compose.ui.graphics.Color

data class UserItem(
    val firstName: String,
    val lastName: String,
    val email: String,
    val phone: String,
    val smsTimestamp: String,
    val depositAccount: String
    )