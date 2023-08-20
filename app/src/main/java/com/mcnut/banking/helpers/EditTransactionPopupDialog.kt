@file:OptIn(ExperimentalMaterial3Api::class, ExperimentalComposeUiApi::class)

package com.mcnut.banking.helpers

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableDoubleStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.mcnut.banking.types.CategoryItem
import com.mcnut.banking.types.Transaction
import kotlin.math.abs


@Composable
fun EditTransactionDialogPopup(openDialog: Boolean, transaction: Transaction, categories: List<CategoryItem>,onDelete: (id: Int) -> Unit, onDismiss: () -> Unit, onSubmit: (newCategoryText: String, newDescription: String, newAmount: Double, newTransType: String, newDate: String, id: Int) -> Unit) {
    var amount by remember { mutableStateOf(abs(transaction.Amount).toString()) }
    var moneyInput by remember { mutableDoubleStateOf(0.0) }
    val showDialog = remember { mutableStateOf(false) }
    val chosenDate = remember { mutableStateOf(transaction.Date) }
    var descriptionText by remember { mutableStateOf(transaction.Description) }
    var selectedItem by remember { mutableStateOf("") }
    var switchState by remember { mutableStateOf(transaction.TransType != "Withdraw") }
    var transactionText by remember { mutableStateOf(transaction.TransType) }
    val focusManager = LocalFocusManager.current
    val rowPadding = 10.dp
    val keyboardController = LocalSoftwareKeyboardController.current

    selectedItem = transaction.Category
    if (openDialog) {
        AlertDialog(
            onDismissRequest = onDismiss,
        ) {
            Surface(
                modifier = Modifier
                    .wrapContentWidth()
                    .wrapContentHeight(),
                shape = MaterialTheme.shapes.large,
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Transaction ID : ${transaction.ID}")
                    DatePickerModal(showDialog, focusManager, chosenDate)
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedTextField(
                            value = chosenDate.value,
                            readOnly = true,
                            onValueChange = {},
                            label = { Text("Date") },
                            modifier = Modifier
                                .fillMaxWidth()
                                .onFocusChanged {
                                    if (it.isFocused) {
                                        showDialog.value = true
                                    }
                                }
                        )
                    }
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(top=rowPadding)
                    ) {
                        OutlinedTextField(
                            value = transactionText,
                            readOnly = true,
                            onValueChange = {},
                            label = { Text("Transaction Type") },
                            trailingIcon = {
                                Switch(
                                    checked = switchState,
                                    onCheckedChange = {
                                        switchState = it
                                        transactionText = if (switchState) "Deposit" else "Withdraw"
                                    },
                                    modifier = Modifier.padding(end = 10.dp)
                                )
                            },
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(top=rowPadding)
                    ) {
                        OutlinedTextField(
                            value = descriptionText,
                            onValueChange = {descriptionText = it},
                            label = { Text("Description") },
                            modifier = Modifier.fillMaxWidth(),
                            keyboardOptions = KeyboardOptions(
                                keyboardType = KeyboardType.Text,
                                imeAction = ImeAction.Done
                            ),
                            keyboardActions = KeyboardActions(
                                onDone = {keyboardController?.hide()}
                            ),
                        )
                    }
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(top=rowPadding)
                    ) {
                        OutlinedTextField(
                            value = amount,
                            onValueChange = {
                                amount = it
                            },
                            label = { Text("Amount") },
                            keyboardOptions = KeyboardOptions(
                                keyboardType = KeyboardType.Decimal,
                                imeAction = ImeAction.Done
                            ),
                            keyboardActions = KeyboardActions(
                                onDone = {keyboardController?.hide()}
                            ),
                            maxLines = 1,
                            modifier = Modifier.fillMaxWidth(),
                        )
                    }
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(top=rowPadding)
                    ) {
                        CategoryDropdown(
                            padding = false,
                            selectedItem = selectedItem,
                            onSelectedItemChange = { newItem ->
                                selectedItem = newItem
                            },
                            categories = categories,
                        )
                    }
                    Row(
                        modifier = Modifier.padding(all = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Button(
                            onClick = { transaction.ID?.let { onDelete(it) } }
                        ) {
                            Text("Delete")
                        }
                        Spacer(modifier = Modifier.weight(1f))
                        try {
                            moneyInput = if (!switchState) {
                                amount.toDouble() * -1
                            } else {
                                amount.toDouble()
                            }
                        } catch (e: Exception) {
                            //Do Nothing
                        }
                        Button(
                            onClick = {onSubmit(selectedItem,descriptionText,moneyInput,transactionText,chosenDate.value, transaction.ID!!)}
                        ) {
                            Text("Save")
                        }
                    }
                }
            }
        }
    }
}
