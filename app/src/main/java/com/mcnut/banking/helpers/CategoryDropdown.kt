@file:OptIn(ExperimentalMaterial3Api::class)

package com.mcnut.banking.helpers

import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.mcnut.banking.types.CategoryItem


@Composable
fun CategoryDropdown(
    padding: Boolean,
    label: String = "Category",
    selectedItem: String,
    onSelectedItemChange: (String) -> Unit,
    categories: List<CategoryItem> = listOf()
) {
    var expanded by remember { mutableStateOf(false) }
    val focusManager = LocalFocusManager.current
    val dropdownPadding: Dp = if (padding) {
        16.dp
    } else {
        0.dp
    }


    Row {
        ExposedDropdownMenuBox(
            expanded = expanded,
            onExpandedChange = {
                expanded = !expanded
            },
            modifier = Modifier.fillMaxWidth()

        ) {
            // text field
            val interactionSource = remember { MutableInteractionSource() }

            OutlinedTextField(
                value = selectedItem,
                readOnly = true,
                onValueChange = { },
                label = { Text(text = label) },
                trailingIcon = {
                    ExposedDropdownMenuDefaults.TrailingIcon(
                        expanded = expanded
                    )
                },
                interactionSource = interactionSource,
                modifier = Modifier
                    .menuAnchor()
                    .fillMaxWidth()
                    .onFocusChanged { state ->
                        if (state.isFocused) {
                            expanded = true
                        }
                    }
            )


            // menu
            ExposedDropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false },
            ) {
                // this is a column scope
                // all the items are added vertically
                categories.forEach { selectedOption ->
                    // menu item
                    DropdownMenuItem(onClick = {
                        onSelectedItemChange(selectedOption.category)
                        expanded = false
                        focusManager.clearFocus()
                    },
                        text = { Text(text = selectedOption.category) },
                    )

                }
            }
        }
    }
}