package com.mcnut.banking.types

data class Data(
    val categories: List<CategoryItem> = emptyList(),
    val balanceItems: List<BalanceItem> = emptyList(),
    val owedItems: List<OwedItem> = emptyList(),
    val loggedInUser: List<UserItem> = emptyList(),
    val transactions: List<Transaction> = emptyList(),
    val monthlyTrends: List<TrendSummary> = emptyList(),
    val alikeTransactions: List<TransactionSummary> = emptyList()
)