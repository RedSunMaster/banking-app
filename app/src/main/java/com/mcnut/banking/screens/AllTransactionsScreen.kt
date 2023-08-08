@file:OptIn(ExperimentalMaterial3Api::class, ExperimentalComposeUiApi::class)

package com.mcnut.banking.screens

import android.annotation.SuppressLint
import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FabPosition
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.NavBackStackEntry
import com.mcnut.banking.R
import com.mcnut.banking.helpers.AddTransactionDialog
import com.mcnut.banking.helpers.CategoryDropdown
import com.mcnut.banking.helpers.EditTransactionDialogPopup
import com.mcnut.banking.helpers.deleteRequest
import com.mcnut.banking.helpers.patchRequest
import com.mcnut.banking.helpers.postRequest
import com.mcnut.banking.types.BankingInfo
import com.mcnut.banking.types.DatabaseInformation
import com.mcnut.banking.types.Transaction
import kotlinx.coroutines.launch


@OptIn(ExperimentalComposeUiApi::class)
@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
fun AllTransactionsScreen(state: DatabaseInformation, bankingInfo: BankingInfo, navBackStackEntry: NavBackStackEntry) {
    val item = navBackStackEntry.arguments?.getString("item") ?: bankingInfo.categories[0].category
    val dateWeight = .35f
    val amountWeight = .3f
    val descriptionWeight = .4f

    var filterCategory by rememberSaveable { mutableStateOf(item) }
    var filterText by rememberSaveable { mutableStateOf("") }
    val keyboardController = LocalSoftwareKeyboardController.current
    var openDialog by remember { mutableStateOf(false) }
    var currentTransactionItem by remember { mutableStateOf<Transaction?>(null) }
    val showDialog = remember { mutableStateOf(false) }
    var fabHeight by remember { mutableIntStateOf(0) }
    val heightInDp = with(LocalDensity.current) { fabHeight.toDp() }

    val coroutineScope = rememberCoroutineScope()
    val context = LocalContext.current

    var displayedItems by remember { mutableStateOf(listOf<Transaction>()) }

    LaunchedEffect(filterText, filterCategory, bankingInfo.transactions, openDialog) {
        Log.d("TEST", "UPDATED")
        val allItems = bankingInfo.transactions
        val filteredListCategory = allItems.filter { it.Category.lowercase() == filterCategory.lowercase() }
        val filteredListBoth = allItems.filter { it.Category.lowercase() == filterCategory.lowercase(
        ) && it.Description.lowercase().contains(filterText.lowercase()) }
        displayedItems = if (filterText == "") {
            filteredListCategory
        } else {
            filteredListBoth
        }
    }

    Scaffold(
        floatingActionButton = {
            ExtendedFloatingActionButton(
                text = { Text(text = "ADD") },
                onClick = {
                    showDialog.value = true
                },
                icon = { Icon(painterResource(id = R.drawable.ic_purchase), "") },
                modifier = Modifier.onGloballyPositioned {
                    fabHeight = it.size.height
                }
            )
            if (showDialog.value) {
                AddTransactionDialog(
                    openDialog = showDialog.value,
                    categories = bankingInfo.categories,
                    onSubmit = { category, date, description, amount, transaction ->
                        coroutineScope.launch {
                            val result = postRequest(
                                bankingInfo.client,
                                "http://mcgarage.hopto.org:8085/api/transactions", bankingInfo.authToken, listOf(
                                    Pair("category", category),
                                    Pair("date", date),
                                    Pair("description", description),
                                    Pair("amount", amount),
                                    Pair("trans_type", transaction)
                                )
                            )
                            when {
                                result.first -> {
                                    Toast.makeText(
                                        context,
                                        "Successfully Added",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                    state.onBalancesUpdatedChange(true)
                                    state.onTransactionUpdatedChange(true)
                                }

                                else -> {
                                    Toast.makeText(
                                        context,
                                        "Addition Failed! ${result.second}",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }
                            }

                            showDialog.value = false
                        }
                    },
                    onDismiss = { showDialog.value = false },
                    selectedCategory = filterCategory
                )
            }
        },
        floatingActionButtonPosition = FabPosition.End,

    ) { _ ->
        LazyColumn(modifier = Modifier.padding(PaddingValues(bottom = heightInDp * 2))) {
            item {
                Text(
                    text = "Filter",
                    style = MaterialTheme.typography.labelLarge,
                    modifier = Modifier.padding(start = 16.dp, top = 8.dp, bottom = 8.dp)
                )
            }
            item {
                OutlinedTextField(
                    value = filterText,
                    onValueChange = { filterText = it },
                    maxLines = 1,
                    trailingIcon = { Icon(Icons.Filled.Search, "Search Bar") },
                    modifier = Modifier
                        .padding(horizontal = 16.dp)
                        .fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Text,
                        imeAction = ImeAction.Done
                    ),
                    keyboardActions = KeyboardActions(
                        onDone = { keyboardController?.hide() }
                    ),
                )
            }
            item {
                CategoryDropdown(
                    padding = true,
                    selectedItem = filterCategory,
                    onSelectedItemChange = { newItem ->
                        Log.d("MyComposable", "filterCategory changed: $newItem")
                        filterCategory = newItem
                    },
                    categories = bankingInfo.categories,
                )
            }
            item {
                Row(
                    Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    TableCell(text = "Date", weight = dateWeight, alignment = TextAlign.Left, title = true)
                    TableCell(text = "Amount", weight = amountWeight, title = true)
                    TableCell(text = "Description", weight = descriptionWeight, title = true)
                }
                Divider(
                    color = Color.LightGray,
                    modifier = Modifier
                        .height(1.dp)
                        .fillMaxHeight()
                        .fillMaxWidth()
                )
            }
            item {
                Box(Modifier.height(700.dp)) {
                    LazyColumn{
                        items(displayedItems) { transactionItem ->
                            Row(
                                Modifier.fillMaxWidth().clickable { openDialog = true
                                    currentTransactionItem = transactionItem },
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                TableCell(
                                    text = transactionItem.Date,
                                    weight = dateWeight,
                                    alignment = TextAlign.Left,
                                )
                                TableCell(text = "$" + String.format("%.2f", transactionItem.Amount), weight = amountWeight)
                                TableCell(text = transactionItem.Description, weight = descriptionWeight)
                            }
                            Divider(
                                color = Color.LightGray,
                                modifier = Modifier
                                    .height(1.dp)
                                    .fillMaxHeight()
                                    .fillMaxWidth()
                            )
                        }
                    }
                }
            }
        }
        if (openDialog) {
            EditTransactionDialogPopup(
                openDialog = true,
                transaction = currentTransactionItem!!,
                onDismiss = { openDialog = false },
                onDelete = { id ->
                    coroutineScope.launch {
                        coroutineScope.launch {
                            val result = deleteRequest(
                                bankingInfo.client,
                                "http://mcgarage.hopto.org:8085/api/transactions", bankingInfo.authToken, listOf(
                                    Pair("transactionId", id),
                                )
                            )
                            when {
                                result.first -> {
                                    Toast.makeText(
                                        context,
                                        "Successfully Deleted",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                    state.onBalancesUpdatedChange(true)
                                    state.onTransactionUpdatedChange(true)
                                }

                                else -> {
                                    Toast.makeText(
                                        context,
                                        "Update Failed! ${result.second}",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }
                            }
                            openDialog = false
                        }
                    }
                },
                categories = bankingInfo.categories,
                onSubmit = { newCategory, newDescription, newAmount, newTrans, newDate, trans_id ->
                    coroutineScope.launch {
                        val result = patchRequest(
                            bankingInfo.client,
                            "http://mcgarage.hopto.org:8085/api/transactions", bankingInfo.authToken, listOf(
                                Pair("transactionId", trans_id),
                                Pair("category", newCategory),
                                Pair("date", newDate),
                                Pair("description", newDescription),
                                Pair("amount", newAmount),
                                Pair("trans_type", newTrans)
                            )
                        )
                        when {
                            result.first -> {
                                Toast.makeText(
                                    context,
                                    "Successfully Updated",
                                    Toast.LENGTH_SHORT
                                ).show()
                                state.onBalancesUpdatedChange(true)
                                state.onTransactionUpdatedChange(true)
                            }

                            else -> {
                                Toast.makeText(
                                    context,
                                    "Update Failed! ${result.second}",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        }
                    }
                    openDialog = false
                }
            )
        }
    }
}

@Composable
fun RowScope.TableCell(
    text: String,
    weight: Float,
    alignment: TextAlign = TextAlign.Center,
    title: Boolean = false
) {
    Text(
        text = text,
        Modifier
            .weight(weight)
            .padding(10.dp),
        fontWeight = if (title) FontWeight.Bold else FontWeight.Normal,
        textAlign = alignment,
    )
}