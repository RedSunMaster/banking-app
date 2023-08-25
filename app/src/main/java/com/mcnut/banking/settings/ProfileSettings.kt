@file:OptIn(ExperimentalMaterial3Api::class)

package com.mcnut.banking.settings

import android.annotation.SuppressLint
import android.app.Activity
import android.widget.Toast
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.mcnut.banking.R
import com.mcnut.banking.helpers.StoreAuthToken
import com.mcnut.banking.helpers.patchRequest
import com.mcnut.banking.helpers.postRequest
import com.mcnut.banking.types.BankingInfo
import com.mcnut.banking.types.DatabaseInformation
import com.mcnut.banking.ui.theme.BudgetingTheme
import kotlinx.coroutines.launch

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
fun ProfileSettings(state: DatabaseInformation, bankingInfo: BankingInfo
                    ) {
    val firstName = remember { mutableStateOf(bankingInfo.user[0].firstName) }
    val lastName = remember { mutableStateOf(bankingInfo.user[0].lastName) }
    val email = remember { mutableStateOf(bankingInfo.user[0].email) }
    val phone = remember { mutableStateOf(bankingInfo.user[0].phone) }
    val depositAccount = remember { mutableStateOf(bankingInfo.user[0].depositAccount) }
    val context = LocalContext.current

    val activity = LocalContext.current as Activity

    val dataStore = StoreAuthToken(context)

    val coroutineScope = rememberCoroutineScope()
    val rowPadding = 10.dp
    BudgetingTheme(darkTheme = state.darkModeToggle) {
        Column(
            modifier = Modifier.padding(end = 24.dp, start = 16.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(top = rowPadding)
            ) {
                Icon(
                    painterResource(id = R.drawable.ic_name),
                    contentDescription = "First Name icon",
                    modifier = Modifier.padding(end = 16.dp)
                )
                OutlinedTextField(
                    value = firstName.value,
                    onValueChange = { firstName.value = it },
                    label = { Text("First Name") },
                    modifier = Modifier.fillMaxWidth(),
                    maxLines = 1
                )
            }
            Spacer(Modifier.height(8.dp))
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(top = rowPadding)
            ) {
                Icon(
                    painterResource(id = R.drawable.ic_name),
                    contentDescription = "Last Name icon",
                    modifier = Modifier.padding(end = 16.dp)
                )
                OutlinedTextField(
                    value = lastName.value,
                    onValueChange = { lastName.value = it },
                    label = { Text("Last Name") },
                    modifier = Modifier.fillMaxWidth(),
                    maxLines = 1
                )
            }
            Spacer(Modifier.height(8.dp))
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(top = rowPadding)
            ) {
                Icon(
                    painterResource(id = R.drawable.ic_email),
                    contentDescription = "Email icon",
                    modifier = Modifier.padding(end = 16.dp)
                )
                OutlinedTextField(
                    value = email.value,
                    onValueChange = { email.value = it },
                    label = { Text("Email") },
                    modifier = Modifier.fillMaxWidth(),
                    maxLines = 1
                )
            }
            Spacer(Modifier.height(8.dp))
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(top = rowPadding)
            ) {
                Icon(
                    painterResource(id = R.drawable.ic_phone),
                    contentDescription = "Phone Icon",
                    modifier = Modifier.padding(end = 16.dp)
                )
                OutlinedTextField(
                    value = phone.value,
                    onValueChange = { phone.value = it },
                    label = { Text("Phone") },
                    modifier = Modifier.fillMaxWidth(),
                    maxLines = 1
                )
            }
            Spacer(Modifier.height(8.dp))
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(top = rowPadding)
            ) {
                Icon(
                    painterResource(id = R.drawable.ic_depositaccount),
                    contentDescription = "Deposit Account Icon",
                    modifier = Modifier.padding(end = 16.dp)
                )
                OutlinedTextField(
                    value = depositAccount.value,
                    onValueChange = { depositAccount.value = it },
                    label = { Text("Deposit Account (For Auto SMS)") },
                    modifier = Modifier.fillMaxWidth(),
                    maxLines = 1
                )
            }
            Spacer(Modifier.height(8.dp))
            OutlinedButton(onClick = {
                coroutineScope.launch {
                    val result = postRequest(
                        bankingInfo.client,
                        "http://banking.mcnut.net:8085/api/logout",
                        bankingInfo.authToken,
                        listOf()
                    )
                    if (result.first) {
                        dataStore.saveAuthToken("")
                        Toast.makeText(
                            context,
                            "Logged Out",
                            Toast.LENGTH_SHORT
                        ).show()
                        activity.recreate()
                    } else {
                        Toast.makeText(
                            context,
                            "Logout Failed ${result.second}",
                            Toast.LENGTH_SHORT
                        ).show()
                    }

                }
            }, Modifier.fillMaxWidth()) {
                Text("Logout")
            }
            Spacer(Modifier.height(8.dp))
            FilledTonalButton(onClick = {
                coroutineScope.launch {
                    val result = patchRequest(
                        bankingInfo.client,
                        "http://banking.mcnut.net:8085/api/user",
                        bankingInfo.authToken,
                        listOf(
                            Pair("fName", firstName.value),
                            Pair("lName", lastName.value),
                            Pair("email", email.value),
                            Pair("phone", phone.value),
                            Pair("depositAccount", depositAccount.value),
                        )
                    )
                    if (result.first) {
                        Toast.makeText(
                            context,
                            "Updated User",
                            Toast.LENGTH_SHORT
                        ).show()
                        activity.recreate()
                    } else {
                        Toast.makeText(
                            context,
                            "Update Failed ${result.second}",
                            Toast.LENGTH_SHORT
                        ).show()
                    }

                }
            }, Modifier.fillMaxWidth()) {
                Text("Save")
            }
        }
    }
}