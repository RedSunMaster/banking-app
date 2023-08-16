@file:OptIn(ExperimentalMaterial3Api::class)

package com.mcnut.banking.screens

import android.annotation.SuppressLint
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mcnut.banking.fragments.getLastWeeklySummary
import com.mcnut.banking.fragments.getThisWeeklySummary
import com.mcnut.banking.helpers.rememberChartStyle
import com.mcnut.banking.helpers.rememberMarker
import com.mcnut.banking.types.BankingInfo
import com.mcnut.banking.types.DatabaseInformation
import com.mcnut.banking.ui.theme.BudgetingTheme
import com.patrykandpatrick.vico.compose.axis.horizontal.bottomAxis
import com.patrykandpatrick.vico.compose.axis.vertical.startAxis
import com.patrykandpatrick.vico.compose.chart.Chart
import com.patrykandpatrick.vico.compose.chart.column.columnChart
import com.patrykandpatrick.vico.compose.component.lineComponent
import com.patrykandpatrick.vico.compose.style.ProvideChartStyle
import com.patrykandpatrick.vico.core.axis.AxisPosition
import com.patrykandpatrick.vico.core.axis.formatter.AxisValueFormatter
import com.patrykandpatrick.vico.core.component.shape.Shapes
import com.patrykandpatrick.vico.core.entry.composed.plus
import com.patrykandpatrick.vico.core.entry.entryModelOf
import com.patrykandpatrick.vico.core.entry.entryOf

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun StatsScreen(state: DatabaseInformation, bankingInfo: BankingInfo) {

    val lastWeekSummary = getLastWeeklySummary(bankingInfo.transactions)
    val thisWeekSummary = getThisWeeklySummary(bankingInfo.transactions)

    BudgetingTheme(darkTheme = state.darkModeToggle) {
        Scaffold(
            modifier = Modifier.padding(end = 24.dp, start = 16.dp)
        ) { _ ->
            LazyColumn {
                item {
                    val lastWeekData = (bankingInfo.categories).associate { category ->
                        val totalCost =
                            lastWeekSummary.find { it.category == category.category }?.total?.toFloat()
                                ?: 0f
                        category to totalCost
                    }

                    val thisWeekData = (bankingInfo.categories).associate { category ->
                        val totalCost =
                            thisWeekSummary.find { it.category == category.category }?.total?.toFloat()
                                ?: 0f
                        category to totalCost
                    }

                    val categories = bankingInfo.categories // Get the list of categories
                    val xValuesToCategories = categories.withIndex()
                        .associate { (index, category) -> index.toFloat() to category.category } // Associate the x-values with the categories
                    val lastChartEntryModel =
                        xValuesToCategories.keys.zip(lastWeekData.values) { x, y -> entryOf(x, y) }
                            .let { entryModelOf(it) } // Create the chart entry model
                    val thisChartEntryModel =
                        xValuesToCategories.keys.zip(thisWeekData.values) { x, y -> entryOf(x, y) }
                            .let { entryModelOf(it) } // Create the chart entry model


                    val horizontalAxisValueFormatterWeekly =
                        AxisValueFormatter<AxisPosition.Horizontal.Bottom> { value, _ ->
                            xValuesToCategories[value] ?: ""
                        }

                    val lastWeek = columnChart(
                        columns = listOf(
                            lineComponent(
                                color = Color.Red,
                                thickness = 8.dp,
                                shape = Shapes.pillShape,
                            ),
                            lineComponent(
                                color = Color.Green,
                                thickness = 8.dp,
                                shape = Shapes.pillShape
                            )
                        )
                    )

                    Column(modifier = Modifier.padding(vertical = 16.dp)) {
                        Text(
                            text = "Weekly Spending Comparison",
                            textAlign = TextAlign.Center,
                            modifier = Modifier.fillMaxWidth(),
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp
                        )
                        Row(modifier = Modifier.align(Alignment.CenterHorizontally)) {
                            Box(
                                modifier = Modifier
                                    .width(10.dp)
                                    .height(10.dp)
                                    .clip(RoundedCornerShape(100))
                                    .background(Color.Red)
                                    .align(Alignment.CenterVertically)
                            )
                            Spacer(Modifier.width(4.dp))
                            Text(text = "Last Week", textAlign = TextAlign.Center, fontSize = 12.sp)
                            Spacer(Modifier.width(16.dp))
                            Box(
                                modifier = Modifier
                                    .width(10.dp)
                                    .height(10.dp)
                                    .clip(RoundedCornerShape(100))
                                    .background(Color.Green)
                                    .align(Alignment.CenterVertically)
                            )
                            Spacer(Modifier.width(4.dp))
                            Text(text = "This Week", textAlign = TextAlign.Center, fontSize = 12.sp)
                            Spacer(Modifier.width(16.dp))
                        }
                        ProvideChartStyle(rememberChartStyle(listOf(Color.Red, Color.Green),state)) {
                            Chart(
                                chart = lastWeek,
                                model = lastChartEntryModel + thisChartEntryModel,
                                startAxis = startAxis(),
                                bottomAxis = bottomAxis(valueFormatter = horizontalAxisValueFormatterWeekly),
                                marker = rememberMarker(),
                            )
                        }
                    }
                }
            }
        }
    }
}