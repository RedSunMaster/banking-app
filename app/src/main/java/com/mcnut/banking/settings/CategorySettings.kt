package com.mcnut.banking.settings

import android.annotation.SuppressLint
import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FabPosition
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mcnut.banking.helpers.AddCategoryDialog
import com.mcnut.banking.helpers.EditCategoryDialog
import com.mcnut.banking.helpers.patchRequest
import com.mcnut.banking.helpers.postRequest
import com.mcnut.banking.types.BankingInfo
import com.mcnut.banking.types.CategoryItem
import com.mcnut.banking.types.DatabaseInformation
import com.mcnut.banking.ui.theme.BudgetingTheme
import kotlinx.coroutines.launch

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
fun CategorySettings(state: DatabaseInformation, bankingInfo: BankingInfo) {

    val context = LocalContext.current
    var items by remember { mutableStateOf(listOf<CategoryItem>()) }
    val coroutineScope = rememberCoroutineScope()
    items = bankingInfo.categories
    val showEditDialog = remember { mutableStateOf(false) }
    var currentCategoryItem by remember { mutableStateOf<CategoryItem?>(null) }
    val showDialog = remember { mutableStateOf(false) }
    var fabHeight by remember { mutableIntStateOf(0) }

    BudgetingTheme(darkTheme = state.darkModeToggle) {
        Scaffold(
            floatingActionButton = {
                ExtendedFloatingActionButton(
                    text = { Text(text = "ADD CATEGORY") },
                    onClick = {
                        showDialog.value = true
                    },
                    icon = { Icon(Icons.Filled.Add, "") },
                    modifier = Modifier.onGloballyPositioned {
                        fabHeight = it.size.height
                    }
                )
                if (showDialog.value) {
                    AddCategoryDialog(
                        openDialog = showDialog.value,
                        onSubmit = { newCategoryText, newCategoryColour ->
                            coroutineScope.launch {
                                val result = postRequest(
                                    bankingInfo.client,
                                    "http://banking.mcnut.net:8085/api/categories",
                                    bankingInfo.authToken,
                                    listOf(
                                        Pair("categoryName", newCategoryText),
                                        Pair("colour", newCategoryColour),
                                    )
                                )
                                when {
                                    result.first -> {
                                        Toast.makeText(
                                            context,
                                            "Successfully Added Category",
                                            Toast.LENGTH_SHORT
                                        ).show()
                                        state.onCategoryUpdatedChange(true)
                                    }

                                    else -> {
                                        Toast.makeText(
                                            context,
                                            "Category Addition Failed:  ${result.second}",
                                            Toast.LENGTH_SHORT
                                        ).show()
                                    }
                                }
                                showDialog.value = false
                            }
                        },
                        onDismiss = { showDialog.value = false })
                }
            },
            floatingActionButtonPosition = FabPosition.End,
            modifier = Modifier.padding(end = 24.dp, start = 16.dp)
        ) { _ ->
            Column {
                LazyColumn(modifier = Modifier.height(600.dp)) {
                    items(items) { item ->

                        ListItem(
                            leadingContent = {
                                IconButton(
                                    onClick = {
                                        showEditDialog.value = true
                                        currentCategoryItem = item
                                    }) {
                                    Icon(Icons.Filled.Edit, contentDescription = null)
                                }
                            },
                            headlineContent = { Text(item.category, fontSize = 20.sp) },
                            trailingContent = {
                                Box(
                                    modifier = Modifier
                                        .width(20.dp)
                                        .height(20.dp)
                                        .clip(RoundedCornerShape(10))
                                        .background(item.colour)
                                )
                            },
                        )
                    }
                }
                if (showEditDialog.value) {
                    EditCategoryDialog(
                        openDialog = showEditDialog.value,
                        currentCategoryItem = currentCategoryItem!!,
                        onDismiss = { showEditDialog.value = false },
                        onSubmit = { newCategoryText, newCategoryColour, categoryId ->
                            Log.d("WORKING", newCategoryText)
                            showEditDialog.value = false
                            coroutineScope.launch {
                                val result = patchRequest(
                                    bankingInfo.client,
                                    "http://banking.mcnut.net:8085/api/categories",
                                    bankingInfo.authToken,
                                    listOf(
                                        Pair("categoryName", newCategoryText),
                                        Pair("colour", newCategoryColour),
                                        Pair("categoryId", categoryId),
                                    )
                                )
                                when {
                                    result.first -> {
                                        Toast.makeText(
                                            context,
                                            "Successfully Updated",
                                            Toast.LENGTH_SHORT
                                        ).show()
                                        state.onCategoryUpdatedChange(true)

                                    }

                                    else -> {
                                        Toast.makeText(
                                            context,
                                            "Update Failed! ${result.second}",
                                            Toast.LENGTH_SHORT
                                        ).show()
                                    }
                                }
                            }
                        }
                    )
                }
            }
        }
    }
}
