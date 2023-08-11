package com.mcnut.banking.types

import androidx.navigation.NavController
import okhttp3.OkHttpClient

data class StatsInfo(
    val monthlyTrends: List<TrendSummary>,
    val alikeTransactions: List<TransactionSummary>
)
