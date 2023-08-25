@file:OptIn(ExperimentalMaterial3Api::class)

package com.mcnut.banking.screens

import android.annotation.SuppressLint
import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FabPosition
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
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
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
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
import androidx.compose.ui.unit.sp
import com.mcnut.banking.R
import com.mcnut.banking.helpers.postRequest
import com.mcnut.banking.types.BalanceItem
import com.mcnut.banking.types.BankingInfo
import com.mcnut.banking.types.DatabaseInformation
import com.mcnut.banking.types.Transaction
import com.mcnut.banking.ui.theme.BudgetingTheme
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
    var balanceItems by remember { mutableStateOf(listOf<BalanceItem>()) }
    var incomeString by remember { mutableStateOf("") }
    var amount by remember { mutableDoubleStateOf(0.0) }
    val coroutineScope = rememberCoroutineScope()

    val context = LocalContext.current
    val keyboardController = LocalSoftwareKeyboardController.current
    var fabHeight by remember { mutableIntStateOf(0) }
    val heightInDp = with(LocalDensity.current) { fabHeight.toDp() }
    val categoryAmounts = rememberSaveable { mutableMapOf<String, Double>() }
    var updated by remember { mutableStateOf(false)}
    var totalAmount by remember { mutableDoubleStateOf(0.0) }
    var color by remember { mutableStateOf(Color.Red) }
    balanceItems = bankingInfo.accountBalances
    LaunchedEffect(updated) {
        try {
            totalAmount = categoryAmounts.values.sumOf { it }
            color = if (totalAmount == amount && totalAmount != 0.0) {
                Color.Green
            } else {
                Color.Red
            }
        } catch (e: Exception) {
            totalAmount = 0.0
            color = Color.Red
        }
        updated = false

    }



    BudgetingTheme(darkTheme = state.darkModeToggle) {
        Scaffold(
            floatingActionButton = {
                ExtendedFloatingActionButton(
                    text = { Text(text = "DISTRIBUTE BUDGET") },
                    onClick = {
                        if (totalAmount == amount && totalAmount != 0.0) {
                            bankingInfo.categories.forEach { category ->
                                if (categoryAmounts[category.category] != 0.0 && categoryAmounts[category.category] != null) {
                                    val inputDate =
                                        getDate(System.currentTimeMillis(), "yyyy-MM-dd")
                                    val inputAmount = categoryAmounts[category.category] ?: 0.0
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
                            Toast.makeText(
                                context,
                                "FAILED! Please Reach 100% Allocation",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    },
                    icon = { Icon(Icons.Filled.Add, "") },
                    modifier = Modifier.onGloballyPositioned {
                        fabHeight = it.size.height
                    }
                )

            },
            floatingActionButtonPosition = FabPosition.End,
        ) { _ ->
            Column(
                modifier = Modifier
                    .verticalScroll(rememberScrollState())
                    .wrapContentHeight()
                    .fillMaxWidth()
                    .padding(end = 24.dp, start = 16.dp)
                    .padding(PaddingValues(bottom = heightInDp * 2))
                    .background(Color.Transparent)
            )
            {
                Text(
                    text = "Income",
                    style = MaterialTheme.typography.headlineSmall,
                    modifier = Modifier.padding(start = 16.dp, top = 8.dp, bottom = 8.dp)
                )

                OutlinedTextField(
                    value = incomeString,
                    onValueChange = {

                        incomeString = it
                        amount = try {
                            incomeString.toDouble()
                        } catch (e: NumberFormatException) {
                            0.0
                        }
                        updated = true
                    },
                    label = { Text("Income") },
                    leadingIcon = { Icon(painterResource(id = R.drawable.ic_money), "Money") },
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
                    text = "$" + String.format("%.2f", totalAmount),
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(start = 16.dp, top = 8.dp, bottom = 8.dp),
                    color = color
                )
                Divider(
                    thickness = 3.dp,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 24.dp)
                )
                balanceItems.forEach { item ->
                    ListItem(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(20.dp)),
                        headlineContent = {
                            Text(
                                item.category,
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Bold
                            )
                        },
                        supportingContent = {
                            Text(
                                "$" + String.format("%.2f", item.amount + (categoryAmounts[item.category] ?: 0.0))
                            )
                        },
                        trailingContent = {
                            var amountString by rememberSaveable { mutableStateOf("") }
                            BasicTextField(

                                value = amountString,
                                onValueChange = {
                                    if (it.length < 10) {
                                        amountString = it
                                        try {
                                            categoryAmounts[item.category] = amountString.toDouble()
                                        } catch (e: Exception) {
                                            categoryAmounts[item.category] = 0.0
                                        }
                                        updated = true
                                    }
                                },
                                keyboardOptions = KeyboardOptions(
                                    keyboardType = KeyboardType.Number,
                                    imeAction = ImeAction.Done
                                ),
                                keyboardActions = KeyboardActions(
                                    onDone = { keyboardController?.hide() }
                                ),
                                decorationBox = { innerTextField ->
                                    Row(
                                        Modifier
                                            .background(Color.LightGray.copy(0.5f), RoundedCornerShape(20.dp))
                                            .padding(16.dp)
                                    ) {
                                        innerTextField()  //<-- Add this
                                    }
                                },
                                modifier = Modifier.border(3.dp, item.colour, RoundedCornerShape(20.dp))
                            )
                        },
                        colors = ListItemDefaults.colors(
                            containerColor = item.colour.copy(alpha = 0.3f)
                        )
                    )
                    Spacer(Modifier.height(8.dp))
                }
            }
        }
    }
}

suspend fun sendTransaction(transaction: Transaction, coroutineScope: CoroutineScope, bankingInfo: BankingInfo): Pair<Boolean, Any?> {
    return withContext(coroutineScope.coroutineContext) {
        Log.d("Date", transaction.Date)
        val result = postRequest(bankingInfo.client,
            "http://banking.mcnut.net:8085/api/transactions",bankingInfo.authToken ,listOf(
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
