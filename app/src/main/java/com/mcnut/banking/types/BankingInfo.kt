package com.mcnut.banking.types

import androidx.navigation.NavController
import okhttp3.OkHttpClient

data class BankingInfo(
    val user: List<UserItem>,
    val transactions: List<Transaction>,
    val categories: List<CategoryItem>,
    val accountBalances: List<BalanceItem>,
    val moneyOwed: List<OwedItem>,
    val authToken: String,
    val client: OkHttpClient,
    val navController: NavController
)
