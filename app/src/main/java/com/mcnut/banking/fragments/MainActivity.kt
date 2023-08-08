package com.mcnut.banking.fragments

import SplashScreen
import android.os.Bundle
import android.util.Log
import android.util.SparseLongArray
import androidx.activity.OnBackPressedCallback
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.core.view.WindowCompat
import co.yml.charts.common.extensions.isNotNull
import com.mcnut.banking.helpers.StoreAuthToken
import com.mcnut.banking.helpers.StoreCheckedCategories
import com.mcnut.banking.helpers.StoreDarkMode
import com.mcnut.banking.helpers.getRequest
import com.mcnut.banking.screens.AccountScreen
import com.mcnut.banking.screens.MainActivityScreen
import com.mcnut.banking.types.BalanceItem
import com.mcnut.banking.types.CategoryItem
import com.mcnut.banking.types.Data
import com.mcnut.banking.types.OwedItem
import com.mcnut.banking.types.Transaction
import com.mcnut.banking.types.UserItem
import com.mcnut.banking.ui.theme.BudgetingTheme
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import org.json.JSONArray
import java.time.OffsetDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter

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
            LaunchedEffect(userToken) {
                if (!userToken.isNullOrEmpty()) {
                    val result = withContext(Dispatchers.IO) {
                        getRequest(
                            client,
                            "http://mcgarage.hopto.org:8085/api/login",
                            userToken,
                            listOf()
                        )
                    }
                    val success = result.first
                    if (success) {
                        canLogin.value = true
                    } else {
                        dataStore.saveAuthToken("")
                        canLogin.value = false
                    }
                }
            }
            LaunchedEffect (darkModeFlow) {
                if (darkModeFlow.isNotNull()) {
                    when (darkModeFlow) {
                        0 -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
                        1 -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
                        2 -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM)
                    }
                } else {
                    AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM)
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

                            valuesGotten.value = true
                            showSplash.value = false
                        }
                    }

                    if (showSplash.value) {
                        BudgetingTheme {
                            SplashScreen()
                        }
                    } else {
                        if (!valuesGotten.value) {
                            BudgetingTheme {
                                SplashScreen()
                            }
                        } else {
                            BudgetingTheme {
                                MainActivityScreen(data = data.value, authToken = userToken!!)
                            }
                        }
                    }
                }
                else -> {
                    BudgetingTheme {
                        AccountScreen()
                    }
                }
            }
        }
    }
}
suspend fun getCategories(client: OkHttpClient, authToken: String): List<CategoryItem> {
    val categoriesResult = getRequest(client, "http://mcgarage.hopto.org:8085/api/categories", authToken, listOf())
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
    val balancesResult = getRequest(client, "http://mcgarage.hopto.org:8085/api/balances", authToken, listOf())
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
    val moneyOwedResult = getRequest(client, "http://mcgarage.hopto.org:8085/api/moneyOwed", authToken, listOf())
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
            val offsetDateTime = OffsetDateTime.parse(date)
            val zonedDateTime =
                offsetDateTime.atZoneSameInstant(ZoneId.systemDefault())
            val formatter = DateTimeFormatter.ofPattern("d/M/yy")
            val formattedDate = formatter.format(zonedDateTime)
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
    val userResult = getRequest(client, "http://mcgarage.hopto.org:8085/api/user", authToken, listOf())
    if (userResult.first) {
        val jsonArray = JSONArray(userResult.second.toString())
        return List(jsonArray.length()) { index ->
            val jsonObject = jsonArray.getJSONObject(index)
            val fName = jsonObject.getString("First Name")
            val lName = jsonObject.getString("Last Name")
            val email = jsonObject.getString("email")
            val phone = jsonObject.getString("phone")
            val smsTimestamp = jsonObject.getString("smsTimestamp")
            UserItem(fName, lName, email, phone, smsTimestamp)
        }
    }
    return emptyList()
}

suspend fun getTransactions(client: OkHttpClient, authToken: String): List<Transaction> {
    val transactionsResult = getRequest(client, "http://mcgarage.hopto.org:8085/api/transactions", authToken, listOf())
    if (transactionsResult.first) {
        val jsonArray = JSONArray(transactionsResult.second.toString())
        return List(jsonArray.length()) { index ->
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
    }
    return emptyList()
}

suspend fun updateData(client: OkHttpClient, authToken: String): Data {
    val categories = getCategories(client, authToken)
    val balanceItems = getBalances(client, authToken)
    val owedItems = getMoneyOwed(client, authToken)
    val loggedInUser = getUser(client, authToken)
    val transactions = getTransactions(client, authToken)
    return Data(categories, balanceItems, owedItems, loggedInUser, transactions)
}
