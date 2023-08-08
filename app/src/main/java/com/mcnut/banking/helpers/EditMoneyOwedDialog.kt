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
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
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
import com.mcnut.banking.types.OwedItem
import java.text.DateFormat

@Composable
fun EditMoneyOwedDialog(openDialog: Boolean, owed_item: OwedItem, categories: List<CategoryItem>, onDismiss: () -> Unit,
                        onSubmit: (amount: String, chosenDate: String, descriptionText: String, personText: String, selectedItem: String, itemID: Int) -> Unit) {
    var amount by remember { mutableStateOf("") }
    val showDialog = remember { mutableStateOf(false) }
    val chosenDate = remember { mutableStateOf("") }
    var descriptionText by remember { mutableStateOf("") }
    val itemID by remember { mutableIntStateOf(owed_item.ID) }
    var personText by remember { mutableStateOf("") }
    var selectedItem by remember { mutableStateOf("") }
    val focusManager = LocalFocusManager.current
    val rowPadding = 10.dp
    val keyboardController = LocalSoftwareKeyboardController.current
    selectedItem = owed_item.Category
    val date = DateFormat.getDateInstance().parse(owed_item.Date)
    val outputString = date?.let { DateFormat.getDateInstance(DateFormat.SHORT).format(it) }
    if (outputString != null) {
        chosenDate.value = outputString
    }
    descriptionText = owed_item.Description
    amount = owed_item.Amount.toString()
    personText = owed_item.Person

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
                            value = personText,
                            onValueChange = {personText = it},
                            label = { Text("Person") },
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
                            onClick = { onSubmit(amount, chosenDate.value, descriptionText, personText, selectedItem, itemID) }
                        ) {
                            Text("Save")
                        }
                    }
                }
            }
        }
    }
}
