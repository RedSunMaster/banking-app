package com.mcnut.banking.screens
import android.annotation.SuppressLint
import android.text.TextUtils
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Divider
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FabPosition
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import co.yml.charts.common.model.PlotType
import co.yml.charts.ui.piechart.charts.PieChart
import co.yml.charts.ui.piechart.models.PieChartConfig
import co.yml.charts.ui.piechart.models.PieChartData
import com.mcnut.banking.R
import com.mcnut.banking.helpers.AddTransactionDialog
import com.mcnut.banking.helpers.FilterPieDropdown
import com.mcnut.banking.helpers.StoreCheckedCategories
import com.mcnut.banking.helpers.postRequest
import com.mcnut.banking.types.BalanceItem
import com.mcnut.banking.types.BankingInfo
import com.mcnut.banking.types.DatabaseInformation
import kotlinx.coroutines.launch


@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
fun AccountBalancesScreen(state: DatabaseInformation, bankingInfo: BankingInfo) {
    var items by remember { mutableStateOf(listOf<BalanceItem>()) }
    val coroutineScope = rememberCoroutineScope()
    var checkedCategories by remember { mutableStateOf(mapOf<String, Boolean>())}
    val showDialog = remember { mutableStateOf(false) }
    val context = LocalContext.current
    var fabHeight by remember { mutableIntStateOf(0) }
    val heightInDp = with(LocalDensity.current) { fabHeight.toDp() }
    items = bankingInfo.accountBalances
    val categoryList = bankingInfo.categories
    val storeCheckedCategories = StoreCheckedCategories(context)
    val savedCheckedCategories by storeCheckedCategories.getCheckedCategories.collectAsState(initial = setOf())
    checkedCategories = savedCheckedCategories.associateWith { true }
    var slices by remember { mutableStateOf(listOf<PieChartData.Slice>()) }


        Scaffold(
            floatingActionButton = {
                ExtendedFloatingActionButton(
                    text = { Text(text = "ADD TRANSACTION") },
                    onClick = {
                        showDialog.value = true
                    },
                    icon = { Icon(painterResource(id = R.drawable.ic_purchase), "") },
                    modifier = Modifier.onGloballyPositioned {
                        fabHeight = it.size.height
                    }
                )
                if (showDialog.value){
                    AddTransactionDialog(openDialog = showDialog.value, categories = categoryList ,onSubmit = { category, date, description, amount, transaction ->
                        coroutineScope.launch {
                            val result = postRequest(bankingInfo.client,
                                "http://mcgarage.hopto.org:8085/api/transactions", bankingInfo.authToken,listOf(
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
                    }, onDismiss = {showDialog.value = false},
                        selectedCategory = null
                    )
                }
            },
            floatingActionButtonPosition = FabPosition.End,
            modifier = Modifier.padding(end = 24.dp, start=16.dp)
        ) { _ ->
            Column(modifier = Modifier
                .verticalScroll(rememberScrollState())
                .wrapContentHeight()
                .fillMaxWidth()
                .padding(PaddingValues(bottom = heightInDp * 2))
                .background(Color.Transparent))
            {
                items.forEach { item ->
                    ListItem(
                        modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(20.dp))
                            .clickable {
                                val route =
                                    Screen.Transactions.route.replace("{item}", item.category)
                                bankingInfo.navController.navigate(route)
                            },
                        headlineContent = { Text(item.category, fontSize = 20.sp, fontWeight = FontWeight.Bold) },
                        trailingContent = {
                            Text(
                                "$" + String.format("%.2f", item.amount),
                                fontSize = 15.sp, fontWeight = FontWeight.Bold
                            )
                        },
                        colors = ListItemDefaults.colors(
                            containerColor = item.colour.copy(alpha = 0.2f)
                        )
                    )
                    Spacer(Modifier.height(8.dp))
                }
                Spacer(Modifier.height(16.dp))
                Divider(
                    thickness = 3.dp,
                    modifier = Modifier.padding(horizontal = 16.dp)
                )
                Spacer(Modifier.height(16.dp))

                FilterPieDropdown(
                    padding = false,
                    selectedItem = "Filter",
                    categories = bankingInfo.categories,
                )
                Spacer(Modifier.height(16.dp))

                slices = bankingInfo.accountBalances
                    .filter { savedCheckedCategories.contains(it.category) }
                    .map { PieChartData.Slice(it.category, it.amount.toFloat(), it.colour) }

                if (slices.isEmpty()) {
                    slices = bankingInfo.accountBalances
                        .filter { checkedCategories[it.category] == true }
                        .map { PieChartData.Slice(it.category, it.amount.toFloat(), it.colour) }
                } else {
                    val pieChartData = PieChartData(slices, PlotType.Donut)
                    val donutChartConfig = PieChartConfig(
                        strokeWidth = 100f,
                        chartPadding = 30,
                        isAnimationEnable = true,
                        backgroundColor = Color.Transparent,
                        showSliceLabels = true,
                        sliceLabelTextColor = MaterialTheme.colorScheme.primary,
                        isClickOnSliceEnabled = false,
                        sliceLabelEllipsizeAt = TextUtils.TruncateAt.START
                    )
                    PieChart(
                        modifier = Modifier
                            .align(Alignment.CenterHorizontally)
                            .fillMaxWidth(),
                        pieChartData,
                        donutChartConfig
                    )
                }
            }
        }
}
