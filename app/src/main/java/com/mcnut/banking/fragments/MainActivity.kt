package com.mcnut.banking.fragments

import android.os.Bundle
import android.util.Log
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.core.view.WindowCompat
import co.yml.charts.common.extensions.isNotNull
import com.mcnut.banking.helpers.StoreAuthToken
import com.mcnut.banking.helpers.StoreDarkMode
import com.mcnut.banking.helpers.getRequest
import com.mcnut.banking.screens.AccountScreen
import com.mcnut.banking.screens.MainActivityScreen
import com.mcnut.banking.screens.SplashScreen
import com.mcnut.banking.types.BalanceItem
import com.mcnut.banking.types.CategoryItem
import com.mcnut.banking.types.Data
import com.mcnut.banking.types.OwedItem
import com.mcnut.banking.types.Transaction
import com.mcnut.banking.types.TransactionSummary
import com.mcnut.banking.types.TrendSummary
import com.mcnut.banking.types.UserItem
import com.mcnut.banking.types.WeekSummary
import com.mcnut.banking.ui.theme.BudgetingTheme
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import org.json.JSONArray
import java.time.LocalDate
import java.time.OffsetDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.temporal.WeekFields
import java.util.Locale

class MainActivity : AppCompatActivity() {
    private val client = OkHttpClient.Builder().build()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)




        WindowCompat.setDecorFitsSystemWindows(window, false)
        setContent {
            val context = LocalContext.current
            val dataStore = StoreAuthToken(context)
            val storeDarkMode = StoreDarkMode(context)
            val canLogin = remember { mutableStateOf(false) }
            val showSplash = remember { mutableStateOf(true) }
            val userToken = runBlocking { dataStore.getAuthToken.firstOrNull() }
            val darkModeFlow = runBlocking {storeDarkMode.getDarkMode.firstOrNull()}
            var darkModeToggle by remember {
                mutableStateOf(false)
            }
            LaunchedEffect(userToken) {
                if (!userToken.isNullOrEmpty()) {
                    val result = withContext(Dispatchers.IO) {
                        getRequest(
                            client,
                            "http://banking.mcnut.net:8085/api/login",
                            userToken,
                            listOf()
                        )
                    }
                    val success = result.first
                    canLogin.value = success
                }
            }
            val systemDarkMode = isSystemInDarkTheme()
            LaunchedEffect (darkModeFlow) {

                if (darkModeFlow.isNotNull()) {
                    when (darkModeFlow) {
                        0 -> darkModeToggle = false
                        1 -> darkModeToggle = true
                        2 -> darkModeToggle = systemDarkMode
                    }
                } else {
                    darkModeToggle = systemDarkMode
                }
            }


            when (canLogin.value) {
                // Set dark mode
                true -> {
                    val data = remember { mutableStateOf(Data()) }
                    val valuesGotten = remember {
                        mutableStateOf(false)
                    }

                    var categories by remember { mutableStateOf(listOf<CategoryItem>()) }
                    var transactions by remember { mutableStateOf(listOf<Transaction>()) }
                    var monthlyTrends by remember { mutableStateOf(listOf<TrendSummary>()) }
                    var alikeTransactions by remember { mutableStateOf(listOf<TransactionSummary>()) }
                    var balanceItems by remember { mutableStateOf(listOf<BalanceItem>()) }
                    var owedItems by remember { mutableStateOf(listOf<OwedItem>()) }
                    var loggedInUser by remember { mutableStateOf(listOf<UserItem>()) }

                    LaunchedEffect(Unit) {
                        if (!valuesGotten.value) {
                            data.value = updateData(client, userToken!!)
                            categories = data.value.categories
                            balanceItems = data.value.balanceItems
                            owedItems = data.value.owedItems
                            loggedInUser = data.value.loggedInUser
                            transactions = data.value.transactions
                            monthlyTrends = data.value.monthlyTrends
                            alikeTransactions = data.value.alikeTransactions

                            valuesGotten.value = true
                            showSplash.value = false
                        }
                    }

                    if (showSplash.value) {
                        BudgetingTheme(darkModeToggle) {
                            SplashScreen(darkModeToggle)
                        }
                    } else {
                        if (!valuesGotten.value) {
                            BudgetingTheme(darkModeToggle) {
                                SplashScreen(darkModeToggle)
                            }
                        } else {
                            BudgetingTheme(darkModeToggle) {
                                MainActivityScreen(data = data.value, authToken = userToken!!, darkModeToggle)
                            }
                        }
                    }
                }
                else -> {
                    BudgetingTheme(darkModeToggle) {
                        AccountScreen(darkModeToggle)
                    }
                }
            }
        }
    }
}
suspend fun getCategories(client: OkHttpClient, authToken: String): List<CategoryItem> {
    val categoriesResult = getRequest(client, "http://banking.mcnut.net:8085/api/categories", authToken, listOf())
    if (categoriesResult.first) {
        val jsonArray = JSONArray(categoriesResult.second.toString())
        return List(jsonArray.length()) { index ->
            val jsonObject = jsonArray.getJSONObject(index)
            val id = jsonObject.getString("categoryId").toInt()
            val category = jsonObject.getString("categoryName")
            val colour = jsonObject.getString("colour")
            CategoryItem(
                id,
                category,
                Color(colour.replace("#", "").toLong(radix = 16) or 0xFF000000)
            )
        }
    }
    return emptyList()
}

suspend fun getBalances(client: OkHttpClient, authToken: String): List<BalanceItem> {
    val balancesResult = getRequest(client, "http://banking.mcnut.net:8085/api/balances", authToken, listOf())
    if (balancesResult.first) {
        val jsonArray = JSONArray(balancesResult.second.toString())
        return List(jsonArray.length()) { index ->
            val jsonObject = jsonArray.getJSONObject(index)
            val category = jsonObject.getString("Category")
            val amount = jsonObject.getDouble("Amount")
            val colour = jsonObject.getString("Colour")

            BalanceItem(
                category,
                amount,
                Color(colour.replace("#", "").toLong(radix = 16) or 0xFF000000)
            )
        }
    }
    return emptyList()
}

suspend fun getMoneyOwed(client: OkHttpClient, authToken: String): List<OwedItem> {
    val moneyOwedResult = getRequest(client, "http://banking.mcnut.net:8085/api/moneyOwed", authToken, listOf())
    if (moneyOwedResult.first) {
        val jsonArray = JSONArray(moneyOwedResult.second.toString())
        return List(jsonArray.length()) { index ->
            val jsonObject = jsonArray.getJSONObject(index)
            val owedId = jsonObject.getInt("ID")
            val userId = jsonObject.getInt("userId")
            val person = jsonObject.getString("Person")
            val amount = jsonObject.getDouble("Amount")
            val category = jsonObject.getString("Category")
            val description = jsonObject.getString("Description")
            val date = jsonObject.getString("Date")
            val offsetDateTime =
                OffsetDateTime.parse(date)
            val zonedDateTime =
                offsetDateTime.atZoneSameInstant(ZoneId.systemDefault())
            val formatter =
                DateTimeFormatter.ofPattern("yyyy-MM-dd")
            val formattedDate =
                formatter.format(zonedDateTime)
            val daysElapsed = jsonObject.getInt("Days Elapsed")
            val payed = jsonObject.getInt("Payed")
            OwedItem(
                owedId,
                userId,
                person,
                amount,
                category,
                formattedDate,
                description,
                daysElapsed,
                payed
            )
        }
    }
    return emptyList()
}

suspend fun getUser(client: OkHttpClient, authToken: String): List<UserItem> {
    val userResult = getRequest(client, "http://banking.mcnut.net:8085/api/user", authToken, listOf())
    if (userResult.first) {
        val jsonArray = JSONArray(userResult.second.toString())
        return List(jsonArray.length()) { index ->
            val jsonObject = jsonArray.getJSONObject(index)
            val fName = jsonObject.getString("First Name")
            val lName = jsonObject.getString("Last Name")
            val email = jsonObject.getString("email")
            val phone = jsonObject.getString("phone")
            val smsTimestamp = jsonObject.getString("smsTimestamp")
            val depositAccount = jsonObject.getString("bankAccount")
            UserItem(fName, lName, email, phone, smsTimestamp, depositAccount)
        }
    }
    return emptyList()
}

suspend fun getTransactions(client: OkHttpClient, authToken: String): Triple<List<Transaction>, List<TrendSummary>, List<TransactionSummary>> {
    val transactionsResult = getRequest(client, "http://banking.mcnut.net:8085/api/transactions", authToken, listOf())
    if (transactionsResult.first && JSONArray(transactionsResult.second.toString()).length() != 0) {
        val jsonArray = JSONArray(transactionsResult.second.toString())
        val transactionList = List(jsonArray.length()) { index ->
            val jsonObject = jsonArray.getJSONObject(index)
            val id = jsonObject.getInt("transactionID")
            val date = jsonObject.getString("Date")
            val amount = jsonObject.getDouble("Amount")
            val description = jsonObject.getString("Description")
            val category = jsonObject.getString("Category")
            val transType = jsonObject.getString("Transaction")
            val offsetDateTime =
                OffsetDateTime.parse(date)
            val zonedDateTime =
                offsetDateTime.atZoneSameInstant(ZoneId.systemDefault())
            val formatter =
                DateTimeFormatter.ofPattern("yyyy-MM-dd")
            val formattedDate =
                formatter.format(zonedDateTime)
            Transaction(id, formattedDate, amount, description, category, transType)
        }
        val trendSummary = getTrendSummary(transactionList)
        val transactionSummary = getTransactionSummary(transactionList)
        Log.d("TESTING", transactionSummary.toString())
        return Triple(transactionList, trendSummary, transactionSummary)
    }
    return Triple(emptyList(), emptyList(), emptyList())
}



fun getTrendSummary(transactionList: List<Transaction>): List<TrendSummary> {
    return transactionList
        .filter { it.TransType == "Withdraw" && !it.Description.contains("Balancing") && !it.Description.contains("Trans") }
        .groupBy { Triple(LocalDate.parse(it.Date).year, LocalDate.parse(it.Date).month, it.Category) }
        .map { (yearMonthCategory, transactions) ->
            val year = yearMonthCategory.first
            val month = yearMonthCategory.second
            val category = yearMonthCategory.third
            val totalCost = transactions.sumOf { kotlin.math.abs(it.Amount) }
            TrendSummary(year, month.toString(), category, totalCost)
        }
}

fun getLastWeeklySummary(transactionList: List<Transaction>): List<WeekSummary> {
    val weekFields = WeekFields.of(Locale.getDefault()) // Get the WeekFields for the default locale
    return transactionList
        .asSequence()
        .filter { it.TransType == "Withdraw" && !it.Description.contains("Balancing") && !it.Description.contains("Trans") }
        .filter { LocalDate.parse(it.Date).get(weekFields.weekOfYear()) == LocalDate.now().get(weekFields.weekOfYear()) - 1 } // Filter by previous week using WeekFields
        .filter { LocalDate.parse(it.Date).year == LocalDate.now().year}
        .groupBy { Triple(LocalDate.parse(it.Date).year, LocalDate.parse(it.Date).get(weekFields.weekOfYear()), it.Category) }
        .map { (yearWeekCategory, transactions) ->
            val category = yearWeekCategory.third
            val totalCost = transactions.sumOf { kotlin.math.abs(it.Amount) }
            WeekSummary(category, totalCost)
        }
        .toList()
}

fun getThisWeeklySummary(transactionList: List<Transaction>): List<WeekSummary> {
    val weekFields = WeekFields.of(Locale.getDefault()) // Get the WeekFields for the default locale
    return transactionList
        .asSequence()
        .filter { it.TransType == "Withdraw" && !it.Description.contains("Balancing") && !it.Description.contains("Trans") }
        .filter { LocalDate.parse(it.Date).get(weekFields.weekOfYear()) == LocalDate.now().get(weekFields.weekOfYear())}
        .filter { LocalDate.parse(it.Date).year == LocalDate.now().year}
        .groupBy { Triple(LocalDate.parse(it.Date).year, LocalDate.parse(it.Date).get(weekFields.weekOfYear()), it.Category) }
        .map { (yearWeekCategory, transactions) ->
            val category = yearWeekCategory.third
            val totalCost = transactions.sumOf { kotlin.math.abs(it.Amount) }
            WeekSummary(category, totalCost)
        }
        .toList()
}



fun getTransactionSummary(transactionList: List<Transaction>): List<TransactionSummary> {
    val today = LocalDate.now()
    val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
    val maxDate = transactionList.filter { LocalDate.parse(it.Date, formatter).year == today.year }.maxOf { LocalDate.parse(it.Date, formatter) }
    val cte = transactionList
        .filter { it.TransType == "Withdraw" && !it.Description.contains("Balancing") && !it.Description.contains("Trans") }
        .filter { LocalDate.parse(it.Date, formatter).let { it.monthValue < maxDate.monthValue || (it.monthValue == maxDate.monthValue && it.dayOfMonth <= maxDate.dayOfMonth) } }
        .groupBy { Triple(LocalDate.parse(it.Date, formatter).month, it.Description, it.Category ) }

        .map { (monthItemCategory, transactions) ->
            val month = monthItemCategory.first
            val item = monthItemCategory.second
            val category = monthItemCategory.third
            val amount = transactions.size
            val cost = transactions.sumOf { kotlin.math.abs(it.Amount) }
            val avgCost = cost / amount
            TransactionSummary(month.value, item, amount, cost, avgCost, category, 0.0)
        }

    return cte.map { transactionSummary ->
        val previousMonthCost = cte.filter { it.Item == transactionSummary.Item && it.Category == transactionSummary.Category && it.Month < transactionSummary.Month }.maxByOrNull { it.Month }?.Cost ?: 0.0
        val percentageDifference = if (previousMonthCost == 0.0) 0.0 else (transactionSummary.Cost - previousMonthCost) / previousMonthCost * 100
        transactionSummary.copy(PercentageDifference = percentageDifference)
    }.sortedWith(compareBy({ it.Month }, { it.Category }, { -it.Amount }, { -it.Cost }))
}



suspend fun updateData(client: OkHttpClient, authToken: String): Data {
    val categories = getCategories(client, authToken)
    val balanceItems = getBalances(client, authToken)
    val owedItems = getMoneyOwed(client, authToken)
    val loggedInUser = getUser(client, authToken)
    val transactions = getTransactions(client, authToken)
    return Data(categories, balanceItems, owedItems, loggedInUser, transactions.first, transactions.second, transactions.third)
}
