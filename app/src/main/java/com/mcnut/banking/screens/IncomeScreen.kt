@file:OptIn(ExperimentalMaterial3Api::class)

package com.mcnut.banking.screens

import android.annotation.SuppressLint
import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
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
import androidx.compose.runtime.mutableDoubleStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
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
import androidx.compose.ui.unit.dp
import com.mcnut.banking.R
import com.mcnut.banking.helpers.postRequest
import com.mcnut.banking.types.BankingInfo
import com.mcnut.banking.types.DatabaseInformation
import com.mcnut.banking.types.Transaction
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun IncomeScreen(state: DatabaseInformation, bankingInfo: BankingInfo) {
    var amount by remember { mutableDoubleStateOf(0.0) }
    var amountString by remember { mutableStateOf("") }
    val coroutineScope = rememberCoroutineScope()
    val context = LocalContext.current
    val keyboardController = LocalSoftwareKeyboardController.current
    var fabHeight by remember { mutableIntStateOf(0) }
    val heightInDp = with(LocalDensity.current) { fabHeight.toDp() }
    val categoryAmounts = rememberSaveable { mutableMapOf<String, Double>() }
    var updated by remember { mutableStateOf(false)}
    var totalPercentage by remember { mutableDoubleStateOf(0.0) }
    var color by remember { mutableStateOf(Color.Red) }

    val categoryPercents = rememberSaveable { mutableMapOf<String, Double>() }
    LaunchedEffect(updated) {
        try {
            totalPercentage = categoryPercents.values.sumOf { it }
            color = if (totalPercentage == 100.0) {
                Color.Green
            } else {
                Color.Red
            }
            categoryPercents.forEach { (category: String, percentage: Double) ->
                categoryAmounts[category] = String.format("%.2f", (amount * (percentage / 100))).toDouble()
            }
            Log.d("TEST", categoryAmounts.toString())
        } catch (e: Exception) {
            totalPercentage = 0.0
            color = Color.Red
            categoryPercents.forEach { (category: String) ->
                categoryAmounts[category] = 0.0
            }
        }
        updated = false

    }


        Scaffold(
            floatingActionButton = {
                ExtendedFloatingActionButton(
                    text = { Text(text = "DISTRIBUTE BUDGET") },
                    onClick = {
                        if (totalPercentage == 100.0) {
                            bankingInfo.categories!!.forEach { category ->
                                if (categoryAmounts[category.category] != 0.0 && categoryAmounts[category.category] != null) {
                                    val inputDate = getDate(System.currentTimeMillis(), "yyyy-MM-dd")
                                    val inputAmount = categoryAmounts[category.category]?: 0.0
                                    val inputDescription = "Income"
                                    val inputTransType = "Deposit"
                                    val transactionToAdd = Transaction(
                                        null,
                                        inputDate!!,
                                        inputAmount,
                                        inputDescription,
                                        category.category,
                                        inputTransType
                                    )
                                    coroutineScope.launch {
                                        val result = sendTransaction(
                                            transaction = transactionToAdd,
                                            coroutineScope,
                                            bankingInfo
                                        )
                                        when {
                                            result.first -> {
                                                // DO NOTHING
                                            }

                                            else -> {
                                                Toast.makeText(
                                                    context,
                                                    "Distribution Error" + result.second,
                                                    Toast.LENGTH_SHORT
                                                ).show()
                                            }
                                        }
                                    }
                                } else {
                                    //NOTHING
                                }
                            }
                            Toast.makeText(
                                context,
                                "Distributed Income",
                                Toast.LENGTH_SHORT
                            ).show()
                            state.onBalancesUpdatedChange(true)
                            state.onTransactionUpdatedChange(true)
                        } else {
                            Toast.makeText(context, "FAILED! Please Reach 100% Allocation", Toast.LENGTH_SHORT).show()
                        }
                    },
                    icon = { Icon(Icons.Filled.Add, "") },
                    modifier = Modifier.onGloballyPositioned {
                        fabHeight = it.size.height
                    }
                )

            },
            floatingActionButtonPosition = FabPosition.End,
            modifier = Modifier.padding(end = 24.dp, start = 16.dp)
        ) { _ ->
            Box (modifier = Modifier.fillMaxSize()) {
                Column (modifier = Modifier.fillMaxSize()){
                    Text(
                        text = "Income",
                        style = MaterialTheme.typography.headlineSmall,
                        modifier = Modifier.padding(start = 16.dp, top = 8.dp, bottom = 8.dp)
                    )
                    OutlinedTextField(
                        value = amountString,
                        onValueChange = {
                            amountString = it
                            amount = try {
                                amountString.toDouble()
                            } catch (e: NumberFormatException) {
                                0.0
                            }
                            updated = true
                        },
                        label = { Text("Income") },
                        leadingIcon = {Icon(painterResource(id = R.drawable.ic_money), "Money")},
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Number,
                            imeAction = ImeAction.Done
                        ),
                        keyboardActions = KeyboardActions(
                            onDone = { keyboardController?.hide() }
                        ),
                        maxLines = 1,
                        modifier = Modifier.fillMaxWidth(),

                    )
                    Text(
                        text = String.format("%.2f", totalPercentage) + "%",
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.padding(start = 16.dp, top = 8.dp, bottom = 8.dp),
                        color = color
                    )
                    Divider(
                        thickness = 3.dp,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 24.dp)
                    )

                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize(),
                        contentPadding = PaddingValues(bottom = heightInDp * 2)
                    ) {
                        bankingInfo.categories!!.forEach { category ->

                            item {
                                var percentageString by rememberSaveable { mutableStateOf("") }
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(
                                        text = category.category,
                                        style = MaterialTheme.typography.labelLarge,
                                        modifier = Modifier
                                            .padding(
                                                start = 16.dp,
                                                top = 8.dp,
                                                bottom = 8.dp
                                            )
                                            .weight(.3f),
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        text = "$${
                                            String.format(
                                                "%.2f",
                                                categoryAmounts[category.category] ?: 0.0
                                            )
                                        }",
                                        style = MaterialTheme.typography.labelLarge,
                                        modifier = Modifier.weight(.3f)
                                    )
                                    OutlinedTextField(
                                        value = percentageString,
                                        onValueChange = {
                                            if (it.length <= 6) {
                                                percentageString = it
                                                try {
                                                    categoryPercents[category.category] = percentageString.toDouble()
                                                } catch (e: Exception) {
                                                    categoryPercents[category.category] = 0.0
                                                }
                                                updated = true
                                            }
                                        },
                                        label = { Text("%") },
                                        keyboardOptions = KeyboardOptions(
                                            keyboardType = KeyboardType.Number,
                                            imeAction = ImeAction.Done
                                        ),
                                        keyboardActions = KeyboardActions(
                                            onDone = { keyboardController?.hide() }
                                        ),
                                        modifier = Modifier.weight(.3f)
                                    )
                                }
                            }
                        }
                    }
                }
            }
    }
}

suspend fun sendTransaction(transaction: Transaction, coroutineScope: CoroutineScope, bankingInfo: BankingInfo): Pair<Boolean, Any?> {
    return withContext(coroutineScope.coroutineContext) {
        Log.d("Date", transaction.Date)
        val result = postRequest(bankingInfo.client,
            "http://mcgarage.hopto.org:8085/api/transactions",bankingInfo.authToken ,listOf(
                Pair("category", transaction.Category),
                Pair("date", transaction.Date),
                Pair("description", transaction.Description),
                Pair("amount", transaction.Amount),
                Pair("trans_type", transaction.TransType)
            )
        )
        result
    }
}

fun getDate(milliSeconds: Long, dateFormat: String?): String? {
    // Create a DateFormatter object for displaying date in specified format.
    val formatter = SimpleDateFormat(dateFormat, Locale.getDefault())

    // Create a calendar object that will convert the date and time value in milliseconds to date.
    val calendar: Calendar = Calendar.getInstance()
    calendar.timeInMillis = milliSeconds
    return formatter.format(calendar.time)
}