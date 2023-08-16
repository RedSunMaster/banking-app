@file:OptIn(ExperimentalMaterial3Api::class, ExperimentalComposeUiApi::class)

package com.mcnut.banking.screens

import android.annotation.SuppressLint
import android.util.Log
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FabPosition
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavBackStackEntry
import co.yml.charts.common.extensions.isNotNull
import com.mcnut.banking.R
import com.mcnut.banking.helpers.AddTransactionDialog
import com.mcnut.banking.helpers.CategoryDropdown
import com.mcnut.banking.helpers.EditTransactionDialogPopup
import com.mcnut.banking.helpers.deleteRequest
import com.mcnut.banking.helpers.patchRequest
import com.mcnut.banking.helpers.postRequest
import com.mcnut.banking.helpers.rememberChartStyle
import com.mcnut.banking.helpers.rememberMarker
import com.mcnut.banking.types.BankingInfo
import com.mcnut.banking.types.DatabaseInformation
import com.mcnut.banking.types.StatsInfo
import com.mcnut.banking.types.Transaction
import com.mcnut.banking.types.TransactionSummary
import com.mcnut.banking.ui.theme.BudgetingTheme
import com.patrykandpatrick.vico.compose.axis.horizontal.bottomAxis
import com.patrykandpatrick.vico.compose.axis.vertical.startAxis
import com.patrykandpatrick.vico.compose.chart.Chart
import com.patrykandpatrick.vico.compose.chart.line.lineChart
import com.patrykandpatrick.vico.compose.chart.line.lineSpec
import com.patrykandpatrick.vico.compose.style.ProvideChartStyle
import com.patrykandpatrick.vico.core.axis.AxisPosition
import com.patrykandpatrick.vico.core.axis.formatter.AxisValueFormatter
import com.patrykandpatrick.vico.core.entry.ChartEntryModel
import com.patrykandpatrick.vico.core.entry.composed.plus
import com.patrykandpatrick.vico.core.entry.entryModelOf
import com.patrykandpatrick.vico.core.entry.entryOf
import kotlinx.coroutines.launch
import java.lang.Float.min
import java.time.LocalDate
import java.time.Month
import java.time.format.TextStyle
import java.util.Locale


@OptIn(ExperimentalComposeUiApi::class)
@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
fun AllTransactionsScreen(state: DatabaseInformation, bankingInfo: BankingInfo, navBackStackEntry: NavBackStackEntry, stats: StatsInfo) {
    val item = navBackStackEntry.arguments?.getString("item") ?: bankingInfo.categories[0].category

    val filterCategory = rememberSaveable { mutableStateOf(item) }
    val showDialog = remember { mutableStateOf(false) }
    val coroutineScope = rememberCoroutineScope()
    val context = LocalContext.current

    val tabs = listOf("Transactions", "Stats")
    var tabIndex by rememberSaveable { mutableIntStateOf(0) }

    var fabHeight by remember { mutableIntStateOf(0) }

    val filterText = rememberSaveable { mutableStateOf("") }
    val openDialog by remember { mutableStateOf(false) }


    val displayedItems = remember { mutableStateOf(listOf<Transaction>()) }
    val displayedAlikeItems = remember { mutableStateOf(listOf<TransactionSummary>()) }

    LaunchedEffect(
        filterText.value,
        filterCategory.value,
        bankingInfo.transactions,
        openDialog,
        stats.alikeTransactions
    ) {
        Log.d("TEST", "UPDATED")
        val allItems = bankingInfo.transactions
        val filteredListCategory =
            allItems.filter { it.Category.lowercase() == filterCategory.value.lowercase() }
        val filteredAlikeCategory =
            stats.alikeTransactions.filter { it.Category.lowercase() == filterCategory.value.lowercase() && it.Month == LocalDate.now().monthValue && it.PercentageDifference != 0.0 }
        val filteredListBoth = allItems.filter {
            it.Category.lowercase() == filterCategory.value.lowercase(
            ) && it.Description.lowercase().contains(filterText.value.lowercase())
        }
        displayedItems.value = if (filterText.value == "") {
            filteredListCategory
        } else {
            filteredListBoth
        }
        displayedAlikeItems.value = filteredAlikeCategory
    }

    BudgetingTheme(darkTheme = state.darkModeToggle) {
        Scaffold(
            floatingActionButton = {
                ExtendedFloatingActionButton(
                    onClick = {
                        showDialog.value = true
                    },
                    content = {
                        Icon(painterResource(id = R.drawable.ic_purchase), "")
                    },
                    modifier = Modifier.onGloballyPositioned {
                        fabHeight = it.size.height
                    }
                )
                if (showDialog.value) {
                    AddTransactionDialog(
                        openDialog = showDialog.value,
                        categories = bankingInfo.categories,
                        onSubmit = { category, date, description, amount, transaction ->
                            coroutineScope.launch {
                                val result = postRequest(
                                    bankingInfo.client,
                                    "http://mcgarage.hopto.org:8085/api/transactions",
                                    bankingInfo.authToken,
                                    listOf(
                                        Pair("category", category),
                                        Pair("date", date),
                                        Pair("description", description),
                                        Pair("amount", amount),
                                        Pair("trans_type", transaction)
                                    )
                                )
                                when {
                                    result.first -> {
                                        Toast.makeText(
                                            context,
                                            "Successfully Added",
                                            Toast.LENGTH_SHORT
                                        ).show()
                                        state.onBalancesUpdatedChange(true)
                                        state.onTransactionUpdatedChange(true)
                                    }

                                    else -> {
                                        Toast.makeText(
                                            context,
                                            "Addition Failed! ${result.second}",
                                            Toast.LENGTH_SHORT
                                        ).show()
                                    }
                                }

                                showDialog.value = false
                            }
                        },
                        onDismiss = { showDialog.value = false },
                        selectedCategory = filterCategory.value
                    )
                }
            },
            floatingActionButtonPosition = FabPosition.End,
        ) { _ ->
            Column {
                TabRow(selectedTabIndex = tabIndex) {
                    tabs.forEachIndexed { index, title ->
                        Tab(
                            selected = tabIndex == index,
                            onClick = { tabIndex = index },
                            text = { Text(title) },
                            icon = {
                                when (index) {
                                    0 -> Icon(
                                        painterResource(id = R.drawable.ic_transactions),
                                        contentDescription = "Transactions"
                                    )

                                    1 -> Icon(
                                        painterResource(id = R.drawable.ic_stats),
                                        contentDescription = "Stats"
                                    )
                                }
                            }
                        )
                    }
                }

                when (tabIndex) {
                    0 -> TransactionsScreen(filterCategory, filterText, displayedItems, bankingInfo, state, fabHeight)
                    1 -> StatsScreen(filterCategory, displayedAlikeItems, bankingInfo, state, stats, fabHeight)
                }
            }
        }
    }
}

@Composable
fun TransactionsScreen(filterCategory: MutableState<String>, filterText: MutableState<String>, displayedItems: MutableState<List<Transaction>>, bankingInfo: BankingInfo, state: DatabaseInformation,fabHeight: Int) {
    val keyboardController = LocalSoftwareKeyboardController.current
    var openDialog by remember { mutableStateOf(false) }
    var currentTransactionItem by remember { mutableStateOf<Transaction?>(null) }
    val coroutineScope = rememberCoroutineScope()
    val context = LocalContext.current
    val heightInDp = with(LocalDensity.current) { fabHeight.toDp() }

    Column(
        modifier = Modifier
            .verticalScroll(rememberScrollState())
            .wrapContentHeight()
            .fillMaxWidth()
            .padding(PaddingValues(bottom = heightInDp * 2))
            .background(Color.Transparent)
    ) {
        Spacer(Modifier.height(8.dp))
        CategoryDropdown(
            padding = true,
            selectedItem = filterCategory.value,
            onSelectedItemChange = { newItem ->
                Log.d("MyComposable", "filterCategory changed: $newItem")
                filterCategory.value = newItem
            },
            categories = bankingInfo.categories,
        )
        OutlinedTextField(
            value = filterText.value,
            onValueChange = { filterText.value = it },
            label = {Text("Filter")},
            maxLines = 1,
            trailingIcon = { Icon(Icons.Filled.Search, "Search Bar") },
            modifier = Modifier
                .fillMaxWidth().padding(horizontal = 16.dp),
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Text,
                imeAction = ImeAction.Done
            ),
            keyboardActions = KeyboardActions(
                onDone = { keyboardController?.hide() }
            ),
        )
        Spacer(Modifier.height(8.dp))
        Divider(
            color = Color.LightGray,
            modifier = Modifier
                .height(1.dp)
                .fillMaxHeight()
                .fillMaxWidth()
        )
        displayedItems.value.forEach { transactionItem ->
            ListItem(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable {
                        openDialog = true
                        currentTransactionItem = transactionItem
                    },
                headlineContent = {
                    Text(
                        transactionItem.Description,
                    )
                },
                supportingContent = {
                    Text(
                        transactionItem.Date,
                    )
                },
                trailingContent = {
                    Text(
                        "$" + String.format("%.2f", transactionItem.Amount),
                    )
                },
            )
            Divider(
                color = Color.LightGray,
                modifier = Modifier
                    .height(1.dp)
                    .fillMaxHeight()
                    .fillMaxWidth()
            )

        }
    }
    if (openDialog) {
        EditTransactionDialogPopup(
            openDialog = true,
            transaction = currentTransactionItem!!,
            onDismiss = { openDialog = false },
            onDelete = { id ->
                coroutineScope.launch {
                    coroutineScope.launch {
                        val result = deleteRequest(
                            bankingInfo.client,
                            "http://mcgarage.hopto.org:8085/api/transactions",
                            bankingInfo.authToken,
                            listOf(
                                Pair("transactionId", id),
                            )
                        )
                        when {
                            result.first -> {
                                Toast.makeText(
                                    context,
                                    "Successfully Deleted",
                                    Toast.LENGTH_SHORT
                                ).show()
                                state.onBalancesUpdatedChange(true)
                                state.onTransactionUpdatedChange(true)
                            }

                            else -> {
                                Toast.makeText(
                                    context,
                                    "Update Failed! ${result.second}",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        }
                        openDialog = false
                    }
                }
            },
            categories = bankingInfo.categories,
            onSubmit = { newCategory, newDescription, newAmount, newTrans, newDate, trans_id ->
                coroutineScope.launch {
                    val result = patchRequest(
                        bankingInfo.client,
                        "http://mcgarage.hopto.org:8085/api/transactions",
                        bankingInfo.authToken,
                        listOf(
                            Pair("transactionId", trans_id),
                            Pair("category", newCategory),
                            Pair("date", newDate),
                            Pair("description", newDescription),
                            Pair("amount", newAmount),
                            Pair("trans_type", newTrans)
                        )
                    )
                    when {
                        result.first -> {
                            Toast.makeText(
                                context,
                                "Successfully Updated",
                                Toast.LENGTH_SHORT
                            ).show()
                            state.onBalancesUpdatedChange(true)
                            state.onTransactionUpdatedChange(true)
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
                openDialog = false
            }
        )
    }
}

@Composable
fun StatsScreen(filterCategory: MutableState<String>, displayedAlikeItems: MutableState<List<TransactionSummary>>, bankingInfo: BankingInfo, state: DatabaseInformation, stats: StatsInfo, fabHeight: Int) {
    val descriptionWeight = .4f
    var expandedStats by remember { mutableStateOf(false) }
    val heightInDp = with(LocalDensity.current) { fabHeight.toDp() }

    Column(
        modifier = Modifier
            .verticalScroll(rememberScrollState())
            .wrapContentHeight()
            .fillMaxWidth()
            .padding(PaddingValues(bottom = heightInDp * 2))
            .background(Color.Transparent)
    ) {
        val filteredData =
            stats.monthlyTrends.filter { it.category == filterCategory.value }


        val groupedData = filteredData.groupBy { it.year }


        val allChartEntryModels = mutableListOf<ChartEntryModel>()

        for (group in groupedData.toSortedMap()) {
            val filteredData2 = group.value
            val data = (0..11).associate { monthNumber ->
                val month = Month.of(monthNumber + 1)
                val totalCost = filteredData2.find {
                    it.month.equals(
                        month.name,
                        ignoreCase = true
                    )
                }?.totalCost?.toFloat() ?: 0f
                monthNumber to totalCost
            }

            val xValuesToDates = data.keys.associateBy { it.toFloat() }
            val chartEntryModel =
                xValuesToDates.keys.zip(data.values) { x, y -> entryOf(x, y) }
                    .let { entryModelOf(it) }
            allChartEntryModels.add(chartEntryModel)
        }

        val horizontalAxisValueFormatter =
            AxisValueFormatter<AxisPosition.Horizontal.Bottom> { value, _ ->
                Month.of(value.toInt() + 1)
                    .getDisplayName(TextStyle.SHORT, Locale.getDefault())
            }
        val colours = mutableMapOf<String, Pair<Float, Color>>()
        val lineSpecs = allChartEntryModels.mapIndexed { index, _ ->
            val alpha = if (index == allChartEntryModels.size - 1) 1f else min(
                (index.toFloat() / allChartEntryModels.size) + 0.2f,
                0.8f
            )
            val colour =
                if (index == allChartEntryModels.size - 1) Color.Green else bankingInfo.categories.find { it.category == filterCategory.value }!!.colour
            val year = groupedData.toSortedMap().keys.toList()[index].toString()
            colours[year] = Pair(alpha, colour)
            lineSpec(
                lineColor = colour.copy(alpha = alpha),
            )
        }


        Column(modifier = Modifier.padding(vertical = 16.dp)) {
            Text(
                text = "Monthly Spending Trends",
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth(),
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp
            )
            Row(modifier = Modifier.align(Alignment.CenterHorizontally)) {
                if (groupedData.isNotEmpty()) {
                    for (group in groupedData.toSortedMap()) {
                        val year = group.key.toString()
                        val color = colours[year]!!.second
                        val alpha = colours[year]!!.first
                        Box(
                            modifier = Modifier
                                .width(10.dp)
                                .height(10.dp)
                                .clip(RoundedCornerShape(100))
                                .background(color.copy(alpha))
                                .align(Alignment.CenterVertically)
                        )
                        Spacer(Modifier.width(4.dp))
                        Text(
                            text = year,
                            textAlign = TextAlign.Center,
                            fontSize = 12.sp
                        )
                        Spacer(Modifier.width(16.dp))
                    }
                } else {
                    Text(
                        text = "No Data",
                        textAlign = TextAlign.Center,
                        fontSize = 12.sp
                    )
                }
            }
            if (groupedData.isNotEmpty()) {
                ProvideChartStyle(rememberChartStyle(colours.values.map { it.second }
                    .toList(), state)) {
                    Chart(
                        chart = lineChart(lineSpecs),
                        model = allChartEntryModels.reduce { acc, chartEntryModel -> acc + chartEntryModel },
                        startAxis = startAxis(),
                        bottomAxis = bottomAxis(valueFormatter = horizontalAxisValueFormatter),
                        marker = rememberMarker(),

                        )
                }
            } else {
                //Do Nothing
            }
        }
        Row(
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = "Show This Months Trends",
                style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold),
            )
            Spacer(Modifier.weight(1f))
            IconButton(onClick = { expandedStats = !expandedStats }) {
                if (expandedStats) {
                    Icon(
                        painterResource(id = R.drawable.ic_dropless),
                        contentDescription = "Dropup"
                    )
                } else {
                    Icon(
                        painterResource(id = R.drawable.ic_dropdown),
                        contentDescription = "Dropdown"
                    )
                }
            }
        }
        AnimatedVisibility(visible = expandedStats) {
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                TableCell(
                    text = "Item",
                    weight = 0.4f,
                    alignment = TextAlign.Left,
                    title = true
                )
                TableCell(text = "#", weight = 0.2f, title = true)
                TableCell(text = "Cost", weight = 0.4f, title = true)
                TableCell(
                    text = "% Change",
                    weight = descriptionWeight,
                    title = true
                )
            }
            Divider(
                color = Color.LightGray,
                modifier = Modifier
                    .height(1.dp)
                    .fillMaxHeight()
                    .fillMaxWidth()
            )
        }
        displayedAlikeItems.value.forEach { transactionItem ->
            AnimatedVisibility(visible = expandedStats) {
                Row(
                    Modifier
                        .fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    TableCell(
                        text = transactionItem.Item,
                        weight = 0.5f,
                        alignment = TextAlign.Left,
                    )
                    TableCell(
                        text = transactionItem.Amount.toString(),
                        weight = 0.2f
                    )
                    TableCell(
                        text = "$" + String.format(
                            "%.2f",
                            transactionItem.Cost
                        ), weight = 0.4f
                    )
                    val color =
                        if (transactionItem.PercentageDifference < 0) Color.Green else Color.Red


                    TableCell(
                        text = String.format(
                            "%.2f",
                            transactionItem.PercentageDifference
                        ) + "%",
                        weight = descriptionWeight,
                        title = true,
                        color = color
                    )

                }
                Divider(
                    color = Color.LightGray,
                    modifier = Modifier
                        .height(1.dp)
                        .fillMaxHeight()
                        .fillMaxWidth()
                )
            }
        }
    }
}



@Composable
fun RowScope.TableCell(
    text: String,
    weight: Float,
    alignment: TextAlign = TextAlign.Center,
    title: Boolean = false,
    color: Color = MaterialTheme.colorScheme.onSurface
) {
    Text(
        text = text,
        Modifier
            .weight(weight)
            .padding(10.dp),
        fontWeight = if (title) FontWeight.Bold else FontWeight.Normal,
        textAlign = alignment,
        color = color
    )
}
