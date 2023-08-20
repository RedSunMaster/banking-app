@file:OptIn(ExperimentalMaterial3Api::class)

package com.mcnut.banking.settings

import android.annotation.SuppressLint
import android.app.Activity
import android.widget.Toast
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.mcnut.banking.R
import com.mcnut.banking.fragments.MainActivity
import com.mcnut.banking.helpers.StoreAuthToken
import com.mcnut.banking.helpers.StoreDarkMode
import com.mcnut.banking.helpers.getRequest
import com.mcnut.banking.helpers.postRequest
import com.mcnut.banking.screens.AccountScreen
import com.mcnut.banking.types.BankingInfo
import com.mcnut.banking.types.DatabaseInformation
import com.mcnut.banking.ui.theme.BudgetingTheme
import kotlinx.coroutines.launch

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
fun ProfileSettings(state: DatabaseInformation, bankingInfo: BankingInfo) {
    val firstName by remember { mutableStateOf(bankingInfo.user[0].firstName) }
    val lastName by remember { mutableStateOf(bankingInfo.user[0].lastName) }
    val email by remember { mutableStateOf(bankingInfo.user[0].email) }
    val phone by remember { mutableStateOf(bankingInfo.user[0].phone) }
    val context = LocalContext.current

    val activity = LocalContext.current as Activity

    val dataStore = StoreAuthToken(context)
    val storeDarkMode = StoreDarkMode(context)

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
                    contentDescription = "Money icon",
                    modifier = Modifier.padding(end = 16.dp)
                )
                OutlinedTextField(
                    value = firstName,
                    onValueChange = { /*TODO*/ },
                    label = { Text("First Name") },
                    modifier = Modifier.fillMaxWidth()
                )
            }
            Spacer(Modifier.height(8.dp))
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(top = rowPadding)
            ) {
                Icon(
                    painterResource(id = R.drawable.ic_name),
                    contentDescription = "Money icon",
                    modifier = Modifier.padding(end = 16.dp)
                )
                OutlinedTextField(
                    value = lastName,
                    onValueChange = { /*TODO*/ },
                    label = { Text("Last Name") },
                    modifier = Modifier.fillMaxWidth()
                )
            }
            Spacer(Modifier.height(8.dp))
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(top = rowPadding)
            ) {
                Icon(
                    painterResource(id = R.drawable.ic_email),
                    contentDescription = "Money icon",
                    modifier = Modifier.padding(end = 16.dp)
                )
                OutlinedTextField(
                    value = email,
                    onValueChange = { /*TODO*/ },
                    label = { Text("Email") },
                    modifier = Modifier.fillMaxWidth()
                )
            }
            Spacer(Modifier.height(8.dp))
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(top = rowPadding)
            ) {
                Icon(
                    painterResource(id = R.drawable.ic_phone),
                    contentDescription = "Money icon",
                    modifier = Modifier.padding(end = 16.dp)
                )
                OutlinedTextField(
                    value = phone,
                    onValueChange = { /*TODO*/ },
                    label = { Text("Phone") },
                    modifier = Modifier.fillMaxWidth()
                )
            }
            Spacer(Modifier.height(8.dp))
            OutlinedButton(onClick = {
                coroutineScope.launch {
                    val result = postRequest(
                        bankingInfo.client,
                        "http://mcgarage.hopto.org:8085/api/logout",
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

            }, Modifier.fillMaxWidth()) {
                Text("Save")
            }
        }
    }
}