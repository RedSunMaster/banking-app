package com.example.budgeting

import android.util.Log
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import okhttp3.Call
import okhttp3.Callback
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import org.json.JSONArray
import java.io.IOException
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.tooling.preview.Preview
import com.example.budgeting.ui.theme.BudgetingTheme


data class BalanceItem(val category: String, val amount: Double)

@OptIn(ExperimentalMaterial3Api::class)
@Preview
@Composable
fun AccountBalancesScreen() {
    var items by remember { mutableStateOf(listOf<BalanceItem>()) }

    LaunchedEffect(Unit) {
        getRequest("http://mcgarage.hopto.org:8085/api/balances") { jsonArray ->
            items = List(jsonArray.length()) { index ->
                val jsonObject = jsonArray.getJSONObject(index)
                val category = jsonObject.getString("Category")
                val amount = jsonObject.getDouble("Amount")
                BalanceItem(category, amount)
            }
        }
    }

    BudgetingTheme(true) {
        Column(
        ) {
            TopAppBar(
                title = { Text("Account Balances") }
            )

            LazyColumn {
                items(items) { item ->
                    ListItem(
                        headlineContent = { Text(item.category) },
                        trailingContent = { Text(item.amount.toString()) }
                    )
                }
            }
        }
    }
}

fun getRequest(url: String, callback: (JSONArray) -> Unit) {
    val request = Request.Builder()
        .url(url)
        .build()

    val client = OkHttpClient()
    client.newCall(request).enqueue(object : Callback {
        override fun onFailure(call: Call, e: IOException) {
            // Handle failure
        }

        override fun onResponse(call: Call, response: Response) {
            response.body?.string()?.let { jsonString ->
                val jsonArray = JSONArray(jsonString)
                callback(jsonArray)
            }
        }
    })
}

