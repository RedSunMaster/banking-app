@file:OptIn(ExperimentalMaterial3Api::class)

package com.mcnut.banking.settings

import android.annotation.SuppressLint
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.mcnut.banking.helpers.StoreDarkMode
import com.mcnut.banking.types.DatabaseInformation
import com.mcnut.banking.ui.theme.BudgetingTheme
import kotlinx.coroutines.launch

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ThemeSettings(state: DatabaseInformation) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val storeDarkMode = StoreDarkMode(context)
    var expanded by remember { mutableStateOf(false) }
    val items = listOf("Light Mode", "Dark Mode", "Match System")
    val selectedIndex by storeDarkMode.getDarkMode.collectAsState(initial = 2)
    BudgetingTheme(darkTheme = state.darkModeToggle) {
        Scaffold(
            modifier = Modifier.padding(end = 24.dp, start = 16.dp)
        ) {
            Row {
                Box {
                    ExposedDropdownMenuBox(
                        expanded = expanded,
                        onExpandedChange = {
                            expanded = !expanded
                        },
                    ) {

                        OutlinedTextField(
                            value = items[selectedIndex],
                            readOnly = true,
                            onValueChange = { },
                            label = { Text(text = "Theme") },
                            trailingIcon = {
                                ExposedDropdownMenuDefaults.TrailingIcon(
                                    expanded = expanded
                                )
                            },
                            modifier = Modifier
                                .menuAnchor()
                                .fillMaxWidth()
                                .onFocusChanged { state ->
                                    if (state.isFocused) {
                                        expanded = true

                                    }
                                }
                        )
                        ExposedDropdownMenu(
                            expanded = expanded,
                            onDismissRequest = { expanded = false },
                        ) {
                            val systemDarkMode = isSystemInDarkTheme()
                            items.forEachIndexed { index, label ->
                                DropdownMenuItem(
                                    text = { Text(label) },
                                    onClick = {
                                        expanded = false
                                        when (index) {
                                            0 -> state.onDarkModeUpdate(false)

                                            1 -> state.onDarkModeUpdate(true)

                                            2 -> state.onDarkModeUpdate(systemDarkMode)
                                        }
                                        coroutineScope.launch {
                                            StoreDarkMode(context).saveDarkMode(index)
                                        }
                                    })
                            }
                        }
                    }
                }
            }
        }
    }
}
