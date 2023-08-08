package com.mcnut.banking.types

import androidx.navigation.NavHostController

data class DatabaseInformation(
    var categoryUpdated: Boolean,
    var owedUpdated: Boolean,
    var transactionUpdated: Boolean,
    var balancesUpdated: Boolean,
    var updateAll: Boolean,
    val onCategoryUpdatedChange: (Boolean) -> Unit,
    val onOwedUpdatedChange: (Boolean) -> Unit,
    val onTransactionUpdatedChange: (Boolean) -> Unit,
    val onBalancesUpdatedChange: (Boolean) -> Unit,
    val onUpdateAllChange: (Boolean) -> Unit,
    val navController: NavHostController
)
