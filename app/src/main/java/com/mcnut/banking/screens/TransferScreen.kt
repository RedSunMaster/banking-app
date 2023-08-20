@file:OptIn(ExperimentalMaterial3Api::class)

package com.mcnut.banking.screens

import android.annotation.SuppressLint
import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.mcnut.banking.R
import com.mcnut.banking.helpers.CategoryDropdown
import com.mcnut.banking.helpers.postRequest
import com.mcnut.banking.types.BankingInfo
import com.mcnut.banking.types.DatabaseInformation
import com.mcnut.banking.ui.theme.BudgetingTheme
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun TransferScreen(state: DatabaseInformation, bankingInfo: BankingInfo) {
    var amount by remember { mutableStateOf("") }
    var selectedFromItem by remember { mutableStateOf("") }
    var selectedToItem by remember { mutableStateOf("") }
    val coroutineScope = rememberCoroutineScope()
    val rowPadding = 10.dp
    val context = LocalContext.current
    val categoryList = bankingInfo.categories.map { it.category }
    val keyboardController = LocalSoftwareKeyboardController.current
    selectedFromItem = try {
        categoryList[0]
    } catch (e: Exception) {""}
    selectedToItem = try {
        categoryList[1]
    } catch (e: Exception) {""}
    BudgetingTheme(darkTheme = state.darkModeToggle) {
        Scaffold(
            floatingActionButton = {
                FloatingActionButton(
                    onClick = {
                        coroutineScope.launch {
                            val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                            val currentDate = sdf.format(Date())
                            val inputAmount = if (amount.isEmpty()) 0.0 else amount.toDouble()
                            val fromResult = postRequest(
                                bankingInfo.client,
                                "http://mcgarage.hopto.org:8085/api/transactions",
                                bankingInfo.authToken,
                                listOf(
                                    Pair("category", selectedFromItem),
                                    Pair("date", currentDate),
                                    Pair("description", "Trans - $selectedToItem"),
                                    Pair("amount", inputAmount * -1),
                                    Pair("trans_type", "Withdraw")
                                )
                            )
                            when {
                                fromResult.first -> {
                                    val toResult = postRequest(
                                        bankingInfo.client,
                                        "http://mcgarage.hopto.org:8085/api/transactions",
                                        bankingInfo.authToken,
                                        listOf(
                                            Pair("category", selectedToItem),
                                            Pair("date", currentDate),
                                            Pair("description", "Trans - $selectedFromItem"),
                                            Pair("amount", inputAmount),
                                            Pair("trans_type", "Deposit")
                                        )
                                    )
                                    when {
                                        toResult.first -> {
                                            Toast.makeText(
                                                context,
                                                "Transferred Successfully!",
                                                Toast.LENGTH_SHORT
                                            ).show()

                                        }

                                        else -> {
                                            Toast.makeText(
                                                context,
                                                "Transfer Failed: ${toResult.second}",
                                                Toast.LENGTH_SHORT
                                            ).show()
                                        }
                                    }
                                    state.onBalancesUpdatedChange(true)
                                    state.onTransactionUpdatedChange(true)
                                }

                                else -> {
                                    Toast.makeText(
                                        context,
                                        "Transfer Failed: ${fromResult.second}",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }
                            }
                        }
                    },
                    Modifier.padding(PaddingValues(end =24.dp)).size(75.dp)
                )
                {
                    Icon(painterResource(id = R.drawable.ic_transferadd), "")
                }
            },
        ) { _ ->
            Column(
                Modifier.padding(end = 24.dp, start = 16.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(top = rowPadding)
                ) {
                    Icon(
                        painterResource(id = R.drawable.ic_send),
                        contentDescription = "Money icon",
                        modifier = Modifier.padding(end = 16.dp)
                    )
                    CategoryDropdown(
                        padding = false,
                        label = "From",
                        selectedItem = selectedFromItem,
                        onSelectedItemChange = { newItem ->
                            selectedFromItem = newItem
                        },
                        categories = bankingInfo.categories,
                    )
                }
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(top = rowPadding)
                ) {
                    Icon(
                        painterResource(id = R.drawable.ic_receive),
                        contentDescription = "Money icon",
                        modifier = Modifier.padding(end = 16.dp)
                    )
                    CategoryDropdown(
                        padding = false,
                        label = "To",
                        selectedItem = selectedToItem,
                        onSelectedItemChange = { newItem ->
                            Log.d("TEST", newItem)
                            selectedToItem = newItem
                        },
                        categories = bankingInfo.categories,
                    )
                }
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(top = rowPadding)
                ) {
                    Icon(
                        painterResource(id = R.drawable.ic_money),
                        contentDescription = "Money icon",
                        modifier = Modifier.padding(end = 16.dp)
                    )
                    OutlinedTextField(
                        value = amount,
                        onValueChange = { amount = it },
                        label = { Text("Amount") },
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
                }
            }
        }
    }
}