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
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.mcnut.banking.R
import com.mcnut.banking.types.CategoryItem
import java.time.LocalDate.now
import java.time.OffsetDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter


@OptIn(ExperimentalComposeUiApi::class, ExperimentalMaterial3Api::class)
@Composable
fun AddTransactionDialog(openDialog: Boolean, categories: List<CategoryItem>, selectedCategory: String?, onDismiss: () -> Unit,
                       onSubmit: (category: String, date: String, description: String, amount: Double, transaction: String) -> Unit) {
    var amount by remember { mutableStateOf("") }
    val showDialog = remember { mutableStateOf(false) }
    var today = now()
    val formatter =
        DateTimeFormatter.ofPattern("yyyy-MM-dd")
    val formattedDate =
        formatter.format(today)
    val chosenDate = remember { mutableStateOf(formattedDate) }
    var descriptionText by remember { mutableStateOf("") }
    var selectedItem by remember { mutableStateOf("") }
    var switchState by remember { mutableStateOf(false) }
    var transactionText by remember { mutableStateOf("Withdraw") }
    val focusManager = LocalFocusManager.current
    val rowPadding = 10.dp
    val keyboardController = LocalSoftwareKeyboardController.current

    selectedItem = try {
        selectedCategory ?: categories[0].category
    } catch (e: Exception) {
        ""
    }

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
                    DatePickerModal(showDialog, focusManager, chosenDate)
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            painterResource(id = R.drawable.ic_calender),
                            contentDescription = "Money icon",
                            modifier = Modifier.padding(end = 16.dp)
                        )
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
                        Icon(
                            painterResource(id = R.drawable.ic_transaction),
                            contentDescription = "Money icon",
                            modifier = Modifier.padding(end = 16.dp)
                        )
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
                        Icon(
                            painterResource(id = R.drawable.ic_description),
                            contentDescription = "Money icon",
                            modifier = Modifier.padding(end = 16.dp)
                        )
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
                        Icon(
                            painterResource(id = R.drawable.ic_category),
                            contentDescription = "Money icon",
                            modifier = Modifier.padding(end = 16.dp)
                        )
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
                            onClick = onDismiss
                        ) {
                            Text("Exit")
                        }
                        Spacer(modifier = Modifier.weight(1f))
                        Button(
                            onClick = { onSubmit(selectedItem, chosenDate.value, descriptionText, if(transactionText == "Withdraw") {
                                amount.toDoubleOrNull() ?: (0.0 * -1)
                            } else {amount.toDoubleOrNull() ?: 0.0 }, transactionText) }
                        ) {
                            Text("Submit")
                        }
                    }
                }
            }
        }
    }
}
