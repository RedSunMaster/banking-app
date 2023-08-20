@file:OptIn(ExperimentalMaterial3Api::class, ExperimentalComposeUiApi::class)

package com.mcnut.banking.helpers

import android.app.Activity
import android.content.Intent
import android.provider.ContactsContract
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Alignment.Companion.CenterHorizontally
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.core.app.ActivityCompat.startActivityForResult
import com.mcnut.banking.types.CategoryItem
import java.time.LocalDate
import java.time.format.DateTimeFormatter


@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun AddMoneyOwedDialog(openDialog: Boolean, categories: List<CategoryItem>, onDismiss: () -> Unit,
                       onSubmit: (amount: String, chosenDate: String, descriptionText: String, personText: String, selectedItem: String) -> Unit) {
    var amount by remember { mutableStateOf("") }
    val showDialog = remember { mutableStateOf(false) }
    var today = LocalDate.now()
    val formatter =
        DateTimeFormatter.ofPattern("yyyy-MM-dd")
    val formattedDate =
        formatter.format(today)
    val chosenDate = remember { mutableStateOf(formattedDate) }
    var descriptionText by remember { mutableStateOf("") }
    var personText by remember { mutableStateOf("") }
    var selectedItem by remember { mutableStateOf("") }
    val focusManager = LocalFocusManager.current
    val rowPadding = 10.dp
    val categoryList = categories.map { it.category }
    val keyboardController = LocalSoftwareKeyboardController.current

    selectedItem = try {
        categoryList[0]
    } catch (e: Exception) {
        ""
    }
// In your Activity or Fragment
    val contentResolver = LocalContext.current.contentResolver

// Pass the contentResolver as a parameter to the contactPickerLauncher
    val contactPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickContact()
    ) { contactUri ->
        // Use the contentResolver passed as a parameter
        if (contactUri != null) {
            val projection = arrayOf(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME)
            val cursor = contentResolver.query(contactUri, projection, null, null, null)
            cursor?.use {
                if (it.moveToFirst()) {
                    val nameIndex = it.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME)
                    val name = it.getString(nameIndex)
                    // Use the selected contact's name
                    personText = name
                }
            }
        }
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
                    OutlinedButton(onClick = { contactPickerLauncher.launch(null) }, Modifier.fillMaxWidth()) {
                        Text("Pick Contact")
                    }
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
                            onClick = { onSubmit(amount, chosenDate.value, descriptionText, personText, selectedItem) }
                        ) {
                            Text("Submit")
                        }
                    }
                }
            }
        }
    }
}
