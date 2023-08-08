@file:OptIn(ExperimentalMaterial3Api::class)

package com.mcnut.banking.helpers

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.mcnut.banking.R
import com.mcnut.banking.types.CategoryItem
import kotlinx.coroutines.launch


@Composable
fun FilterPieDropdown(
    padding: Boolean,
    label: String = "Filter",
    selectedItem: String,
    categories: List<CategoryItem>? = listOf(),
) {
    val context = LocalContext.current
    var checkedCategories by remember { mutableStateOf(mapOf<String, Boolean>())}
    val storeCheckedCategories = StoreCheckedCategories(context)
    val savedCheckedCategories by storeCheckedCategories.getCheckedCategories.collectAsState(initial = setOf())
    checkedCategories = savedCheckedCategories.associateWith { true }
    var expanded by remember { mutableStateOf(false) }
    val coroutineScope = rememberCoroutineScope()

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
            modifier = Modifier.padding(horizontal = dropdownPadding)
        ) {
            // text field
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
                modifier = Modifier
                    .menuAnchor()
                    .fillMaxWidth()
            )

            // menu
            ExposedDropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false },
            ) {
                // this is a column scope
                // all the items are added vertically
                categories?.forEach { selectedOption ->
                    // menu item
                    Row {
                        DropdownMenuItem(
                            onClick = {
                                // Toggle the checked state of the category
                                val newState = !(checkedCategories[selectedOption.category] ?: false)
                                checkedCategories = checkedCategories.toMutableMap().apply { set(selectedOption.category, newState) }
                                coroutineScope.launch {
                                    storeCheckedCategories.saveCheckedCategories(checkedCategories.keys.filter { checkedCategories[it] == true }.toSet())
                                }
                            },
                            text = { Text(text = selectedOption.category) },
                            trailingIcon = {
                                val isChecked = checkedCategories[selectedOption.category] ?: false
                                Icon(
                                    painterResource(id = if (isChecked) R.drawable.ic_checked else R.drawable.ic_notchecked),
                                    contentDescription = "Checkbox",
                                )
                            },
                            leadingIcon = {
                                Box(modifier = Modifier
                                    .width(10.dp)
                                    .height(10.dp)
                                    .clip(RoundedCornerShape(100))
                                    .background(selectedOption.colour))
                            }
                        )

                    }
                }
            }
        }
    }
}
