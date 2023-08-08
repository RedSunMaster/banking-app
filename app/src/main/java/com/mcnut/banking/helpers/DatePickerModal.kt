package com.mcnut.banking.helpers

import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.ui.focus.FocusManager
import com.mcnut.banking.screens.getDate

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DatePickerModal(
    showDialog: MutableState<Boolean>,
    focusManager: FocusManager,
    chosenDate: MutableState<String>
) {
    val datePickerState = rememberDatePickerState()
    if (showDialog.value) {
        DatePickerDialog(
            onDismissRequest = {
                showDialog.value = false
                focusManager.clearFocus()
            },
            confirmButton = ({
                TextButton(
                    onClick = {
                        showDialog.value = false
                        datePickerState.selectedDateMillis?.let {
                            chosenDate.value = getDate(it, "yyyy-MM-dd").toString()
                        }
                        focusManager.clearFocus()
                    },
                ) {
                    Text("OK")
                }
            }),
        ) {
            DatePicker(state = datePickerState)
        }
    }
}
