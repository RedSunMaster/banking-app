@file:OptIn(ExperimentalMaterial3Api::class)

package com.mcnut.banking.screens

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.provider.ContactsContract
import android.telephony.SmsManager
import android.util.Log
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.ripple.rememberRipple
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FabPosition
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.mcnut.banking.R
import com.mcnut.banking.helpers.AddMoneyOwedDialog
import com.mcnut.banking.helpers.EditMoneyOwedDialog
import com.mcnut.banking.helpers.patchRequest
import com.mcnut.banking.helpers.postRequest
import com.mcnut.banking.types.BankingInfo
import com.mcnut.banking.types.DatabaseInformation
import com.mcnut.banking.types.OwedItem
import com.mcnut.banking.ui.theme.BudgetingTheme
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.time.LocalDateTime
import java.time.OffsetDateTime
import java.time.ZoneId


@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
fun MoneyOwed(state: DatabaseInformation, bankingInfo: BankingInfo) {
    val tabs = listOf("Not Payed", "Payed")
    var tabIndex by rememberSaveable { mutableIntStateOf(0) }
    var fabHeight by remember { mutableIntStateOf(0) }
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val heightInDp = with(LocalDensity.current) { fabHeight.toDp() }
    var openDialog by remember { mutableStateOf(false) }

    BudgetingTheme(darkTheme = state.darkModeToggle) {
        Scaffold(
            floatingActionButton = {
                ExtendedFloatingActionButton(
                    onClick = { openDialog = true },
                    icon = { Icon(Icons.Filled.Add, "Localized Description") },
                    text = { Text(text = "ADD") },
                    modifier = Modifier.onGloballyPositioned {
                        fabHeight = it.size.height
                    }
                )
                if (openDialog) {
                    AddMoneyOwedDialog(
                        openDialog = true,
                        onDismiss = { openDialog = false },
                        categories = bankingInfo.categories,
                        onSubmit = { amount, chosenDate, descriptionText, personText, selectedItem ->
                            openDialog = false
                            coroutineScope.launch {
                                val result = postRequest(
                                    bankingInfo.client,
                                    "http://mcgarage.hopto.org:8085/api/moneyOwed",
                                    bankingInfo.authToken,
                                    listOf(
                                        Pair("person", personText),
                                        Pair("category", selectedItem),
                                        Pair("description", descriptionText),
                                        Pair("date", chosenDate),
                                        Pair(
                                            "amount",
                                            if (amount.isEmpty()) 0.0 else amount.toDouble()
                                        )
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
                                        state.onOwedUpdatedChange(true)
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
            },
            floatingActionButtonPosition = FabPosition.End,
        ) {
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
                                        painterResource(id = R.drawable.ic_notpayed),
                                        contentDescription = "Not Payed"
                                    )

                                    1 -> Icon(
                                        painterResource(id = R.drawable.ic_money),
                                        contentDescription = "Payed"
                                    )
                                }
                            }
                        )
                    }
                }
                when (tabIndex) {
                    0 -> NotPayedTab(bankingInfo, state, heightInDp)
                    1 -> PayedTab(bankingInfo, state, heightInDp)
                }
            }
        }
    }
}

@Composable
fun NotPayedTab(data: BankingInfo, state: DatabaseInformation, heightInDp: Dp) {
    val context = LocalContext.current
    val smsManager: SmsManager = context.getSystemService(SmsManager::class.java)
    val owedItems = data.moneyOwed.filter { it.Payed == 0 }
    val groupedItems = owedItems.groupBy { it.Person }
    val coroutineScope = rememberCoroutineScope()
    var canSend by remember { mutableStateOf(false) }

    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) {}

    LaunchedEffect(canSend) {
        canSend = if (data.user[0].smsTimestamp == "null") {
            true
        } else {
            val offsetDateTime = OffsetDateTime.parse(data.user[0].smsTimestamp)
            val smsDateTime =
                offsetDateTime.atZoneSameInstant(ZoneId.systemDefault()).toLocalDateTime()
                    .plusHours(1)
            val nextWeek = smsDateTime.plusWeeks(1)
            nextWeek.isBefore(LocalDateTime.now())
        }
    }


    Column {
        val totalOwed = if (owedItems.isNotEmpty()) owedItems.sumOf { it.Amount } else 0.0
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text("Total Owed: $${String.format("%.2f", totalOwed)}", fontSize = 20.sp, modifier = Modifier.padding(8.dp))
            Spacer(Modifier.weight(1f))
            IconButton(onClick = {
                if (ContextCompat.checkSelfPermission(
                        context,
                        Manifest.permission.SEND_SMS
                    ) == PackageManager.PERMISSION_GRANTED && ContextCompat.checkSelfPermission(
                        context,
                        Manifest.permission.READ_CONTACTS
                    ) == PackageManager.PERMISSION_GRANTED
                ) {
                    coroutineScope.launch {
                        canSend = if (canSend) {
                            val patchResult = patchRequest(
                                data.client,
                                "http://mcgarage.hopto.org:8085/api/sms",
                                data.authToken,
                                listOf()
                            )
                            if (patchResult.first) {
                                sendSMSMessagesAsync(groupedItems, smsManager, context)
                                Toast.makeText(
                                    context,
                                    "Sent SMS to owed people",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                            false
                        } else {
                            Toast.makeText(
                                context,
                                "It hasn't been a week since the last SMS was sent.",
                                Toast.LENGTH_SHORT
                            ).show()
                            false
                        }
                    }
                } else {
                    launcher.launch(Manifest.permission.SEND_SMS)
                }
            }) {
                Icon(
                    painter = painterResource(
                        id = if (canSend) R.drawable.ic_send_sms else R.drawable.ic_cant_send
                    ),
                    contentDescription = null
                )
            }
        }
        LazyColumn(
            modifier = Modifier.padding(8.dp),
            contentPadding = PaddingValues(bottom = heightInDp * 2)
        ) {
            items(groupedItems.entries.toList()) { (person, items) ->
                val totalOwedEach = items.sumOf { it.Amount }
                var showItems by remember { mutableStateOf(false) }
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = person,
                        style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold),
                    )

                    Spacer(Modifier.weight(1f))
                    Text(
                        text = "$" + String.format("%.2f", totalOwedEach),
                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                    )
                    IconButton(onClick = { showItems = !showItems }) {
                        if (showItems) {
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
                DisplayItemsList(items, showItems, data, state)
            }
        }
    }
}

@Composable
fun PayedTab(data: BankingInfo, state: DatabaseInformation, heightInDp: Dp) {
    val owedItems = data.moneyOwed.filter { it.Payed == 1 }
    val groupedItems = owedItems.groupBy { it.Person }
    Column {
        val totalPayed = if (owedItems.isNotEmpty()) owedItems.sumOf { it.Amount } else 0.0
        Text(
            "Total Payed: $${String.format("%.2f", totalPayed)}",
            fontSize = 20.sp,
            modifier = Modifier.padding(8.dp)
        )
        LazyColumn(
            modifier = Modifier.padding(8.dp),
            contentPadding = PaddingValues(bottom = heightInDp * 2)
        ) {

            items(groupedItems.entries.toList()) { (person, items) ->
                var showItems by remember { mutableStateOf(false) }
                val averageDaysElapsed = items.map { it.DaysElapsed }.average()
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = person,
                        style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold),
                    )

                    Spacer(Modifier.weight(1f))
                    Text(
                        text = String.format("%.2f", averageDaysElapsed) + " Day(s)",
                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                    )
                    IconButton(onClick = { showItems = !showItems }) {
                        if (showItems) {
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
                DisplayItemsList(items, showItems, data, state)

            }
        }
    }
}

suspend fun sendSMSMessagesAsync(groupedItems: Map<String, List<OwedItem>>, smsManager: SmsManager, context: Context) {
    withContext(Dispatchers.IO) {
        groupedItems.forEach { (person, items) ->
            val totalOwedSMS = items.sumOf { it.Amount }
            val maxDays = items.maxOf { it.DaysElapsed }
            val message: String
            val itemizedOwes: String
            Log.d("TEST", items.size.toString())
            if (items.size == 1) {
                itemizedOwes = items.joinToString(",\n") { it.Description }
                message = "Hello $person. It has been ~$maxDays Day(s)\nsince you owed me a total of $$totalOwedSMS for\n$itemizedOwes\nPlease pay to the following account - '06-0821-0862927-01'"
            } else {
                itemizedOwes = items.joinToString(",\n") { "${it.Description} - $${it.Amount}" }
                message = "Hello $person. It has been ~$maxDays Day(s)\nsince you owed me a total of $$totalOwedSMS.\nThis is split between:\n$itemizedOwes\nPlease pay to the following account - '06-0821-0862927-01'"
            }

            val phoneNumber = getPhoneNumber(context, person)
            if (phoneNumber != null) {
                try {
                    Log.d("TEST", phoneNumber)

                    val parts: ArrayList<String> = smsManager.divideMessage(message)
                    Log.d("TEST", parts.toString())
                    smsManager.sendMultipartTextMessage(phoneNumber, null, parts, null, null)
                } catch (e: Exception) {
                    Log.d("TEST", e.message.toString())
                }
            }
        }
    }
}


@Composable
fun DisplayItemsList(items: List<OwedItem>, showItems: Boolean, bankingInfo: BankingInfo, state: DatabaseInformation) {
    val indication = rememberRipple(bounded = false, radius = 16.dp)
    var openDialog by remember { mutableStateOf(false) }
    var currentOwedItem by remember { mutableStateOf<OwedItem?>(null) }
    val coroutineScope = rememberCoroutineScope()
    val context = LocalContext.current
    items.forEach { item ->
        AnimatedVisibility(
            visible = showItems) {
            val interactionSource = remember { MutableInteractionSource() }
            val interactionSource2 = remember { MutableInteractionSource() }
            Card(
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.padding(8.dp),
                content = {
                    ListItem(
                        headlineContent = { Text(item.Description) },
                        overlineContent = { Text("${item.DaysElapsed} Day(s)") },
                        supportingContent = { Text(item.Category) },
                        trailingContent = {
                            Column(
                                verticalArrangement = Arrangement.Center,
                                horizontalAlignment = Alignment.End
                            ) {
                                Text("$${item.Amount}")
                                Icon(
                                    if (item.Payed == 0) painterResource(id = R.drawable.ic_done) else painterResource(
                                        id = R.drawable.ic_cancel
                                    ),
                                    contentDescription = if (item.Payed == 0) "Check" else "Close",
                                    modifier = Modifier.clickable(
                                        onClick = {
                                            coroutineScope.launch {
                                                val result = patchRequest(bankingInfo.client,
                                                    "http://mcgarage.hopto.org:8085/api/updateOwedItem",
                                                    bankingInfo.authToken,
                                                    listOf(Pair("owed_id", item.ID.toString()))
                                                )

                                                when {
                                                    result.first -> {
                                                        Toast.makeText(
                                                            context,
                                                            "Successfully Updated",
                                                            Toast.LENGTH_SHORT
                                                        ).show()
                                                        state.onBalancesUpdatedChange(true)
                                                        state.onOwedUpdatedChange(true)
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
                                        },
                                        indication = indication,
                                        interactionSource = interactionSource
                                    )
                                )
                            }
                        },
                        leadingContent = {
                            Icon(
                                Icons.Default.Edit,
                                contentDescription = "Edit",
                                modifier = Modifier.clickable(
                                    onClick = {openDialog = true
                                        currentOwedItem = item},
                                    indication = indication,
                                    interactionSource = interactionSource2
                                )
                            )
                        },
                        colors = ListItemDefaults.colors(containerColor = MaterialTheme.colorScheme.secondaryContainer),
                    )
                }
            )
        }
    }
    if (openDialog) {
        EditMoneyOwedDialog(
            openDialog = true,
            owedItem = currentOwedItem!!,
            onDismiss = { openDialog = false },
            categories = bankingInfo.categories,
            onSubmit = { amount, chosenDate, descriptionText, personText, selectedItem, itemId ->
                openDialog = false
                coroutineScope.launch {
                    val result = patchRequest(bankingInfo.client,"http://mcgarage.hopto.org:8085/api/editOwedItem",
                        bankingInfo.authToken,listOf(
                            Pair("owed_id", itemId),
                            Pair("person", personText),
                            Pair("category", selectedItem),
                            Pair("description", descriptionText),
                            Pair("date", chosenDate),
                            Pair("amount", amount.toDouble())
                        ))
                    when {
                        result.first -> {
                            Toast.makeText(
                                context,
                                "Successfully Updated",
                                Toast.LENGTH_SHORT
                            ).show()
                            state.onBalancesUpdatedChange(true)
                            state.onOwedUpdatedChange(true)
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
        )
    }
}


fun getPhoneNumber(context: Context, contactName: String): String? {
    val contentResolver = context.contentResolver
    val cursor = contentResolver.query(
        ContactsContract.Data.CONTENT_URI,
        null,
        "${ContactsContract.CommonDataKinds.StructuredName.GIVEN_NAME} = ? AND ${ContactsContract.Data.MIMETYPE} = ?",
        arrayOf(contactName, ContactsContract.CommonDataKinds.StructuredName.CONTENT_ITEM_TYPE),
        null
    )
    var phoneNumber: String? = null
    if (cursor?.moveToFirst() == true) {
        val contactIdIndex = cursor.getColumnIndex(ContactsContract.Data.CONTACT_ID)
        if (contactIdIndex != -1) {
            val contactId = cursor.getString(contactIdIndex)
            val phoneCursor = contentResolver.query(
                ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                null,
                "${ContactsContract.CommonDataKinds.Phone.CONTACT_ID} = ?",
                arrayOf(contactId),
                null
            )
            if (phoneCursor?.moveToFirst() == true) {
                val phoneNumberIndex =
                    phoneCursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER)
                if (phoneNumberIndex != -1) {
                    phoneNumber = phoneCursor.getString(phoneNumberIndex)
                }
            }
            phoneCursor?.close()
        }
    }
    cursor?.close()
    return phoneNumber
}
