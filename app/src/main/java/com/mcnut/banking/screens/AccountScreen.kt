@file:OptIn(ExperimentalMaterial3Api::class)

package com.mcnut.banking.screens

import android.annotation.SuppressLint
import android.content.Intent
import android.widget.Toast
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.autofill.AutofillNode
import androidx.compose.ui.autofill.AutofillType
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.boundsInWindow
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalAutofill
import androidx.compose.ui.platform.LocalAutofillTree
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mcnut.banking.R
import com.mcnut.banking.fragments.MainActivity
import com.mcnut.banking.helpers.accountPostRequest
import com.mcnut.banking.helpers.StoreAuthToken
import com.mcnut.banking.ui.theme.BudgetingTheme
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
fun AccountScreen(darkModeToggle: Boolean) {
    val tabs = listOf("Login", "Register")
    var tabIndex by remember { mutableIntStateOf(0) }
    BudgetingTheme(darkTheme = darkModeToggle) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = {
                        Text(
                            "Login / Register",
                            fontSize = 30.sp,
                            fontWeight = FontWeight.Bold
                        )
                    },
                    navigationIcon = {
                        IconButton(onClick = { }) {
                            Icon(Icons.Filled.Menu, contentDescription = "Menu")
                        }
                    },
                    actions = {
                        IconButton(onClick = { }) {
                            Icon(Icons.Filled.Refresh, contentDescription = "Refresh")
                        }
                        IconButton(onClick = { }) {
                            Icon(Icons.Filled.Person, contentDescription = "Profile")
                        }
                    })
            },
            bottomBar = {
                NavigationBar {
                }
            },
        ) { contentPadding ->
            Column (modifier = Modifier.padding(contentPadding)){
                TabRow(selectedTabIndex = tabIndex) {
                    tabs.forEachIndexed { index, title ->
                        Tab(
                            selected = tabIndex == index,
                            onClick = { tabIndex = index },
                            text = { Text(title) },
                            icon = {
                                when (index) {
                                    0 -> Icon(
                                        painterResource(id = R.drawable.ic_login),
                                        contentDescription = "Login"
                                    )

                                    1 -> Icon(
                                        painterResource(id = R.drawable.ic_register),
                                        contentDescription = "Register"
                                    )
                                }
                            }
                        )
                    }
                }
                when (tabIndex) {
                    0 -> LoginScreen()
                    1 -> RegisterScreen()
                }
            }
        }
    }
}





@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun LoginScreen() {
    var emailText by remember { mutableStateOf("") }
    var passwordText by remember { mutableStateOf("") }
    val coroutineScope = rememberCoroutineScope()
    val rowPadding = 10.dp
    val context = LocalContext.current
    val keyboardController = LocalSoftwareKeyboardController.current
    val dataStore = StoreAuthToken(context)
    var confirmPasswordVisibility by remember { mutableStateOf(false) }
    val autofill = LocalAutofill.current

    Scaffold(
        floatingActionButton = {
            ExtendedFloatingActionButton(
                text = { Text(text = "LOGIN") },
                onClick = {
                    coroutineScope.launch {
                        val result =
                            accountPostRequest("http://banking-app.mcnut.net/api/login", null, listOf(
                                Pair("email", emailText.lowercase()),
                                Pair("password", passwordText)
                            ))
                        when {
                            result.first -> {
                                dataStore.saveAuthToken(result.second as String)
                                Toast.makeText(
                                    context,
                                    "Successfully Logged In",
                                    Toast.LENGTH_SHORT
                                ).show()
                                context.startActivity(
                                    Intent(
                                        context,
                                        MainActivity::class.java
                                    )
                                )
                            }
                            else -> {
                                Toast.makeText(
                                    context,
                                    "Login Failed: ${result.second}",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        }
                    }
                },
                icon = { Icon(Icons.Filled.Add, "")},
                modifier = Modifier.padding(end = 24.dp, start=16.dp)

            )
        },
    ) { _ ->
        Column(modifier = Modifier.padding(end = 24.dp, start=16.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(top=rowPadding)
            ) {
                Icon(
                    painterResource(id = R.drawable.ic_email),
                    contentDescription = "Money icon",
                    modifier = Modifier.padding(end = 16.dp)
                )
                Autofill(
                    autofillTypes = listOf(AutofillType.Username),
                    onFill = { emailText = it }
                ) { autofillNode ->
                    OutlinedTextField(
                        label = { Text("Email") },
                        modifier = Modifier.fillMaxWidth().onFocusChanged {
                            autofill?.apply {
                                if (it.isFocused) {
                                    requestAutofillForNode(autofillNode)
                                } else {
                                    cancelAutofillForNode(autofillNode)
                                }
                            }
                        },
                        value = emailText,
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Email,
                            imeAction = ImeAction.Done
                        ),
                        keyboardActions = KeyboardActions(
                            onDone = {keyboardController?.hide()}
                        ),
                        maxLines = 1,
                        onValueChange = { emailText = it }
                    )
                }
            }
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(top=rowPadding)
            ) {
                Icon(
                    painterResource(id = R.drawable.ic_password),
                    contentDescription = "Money icon",
                    modifier = Modifier.padding(end = 16.dp)
                )
                Autofill(
                    autofillTypes = listOf(AutofillType.Password),
                    onFill = { passwordText = it }
                ) { autofillNode ->
                    OutlinedTextField(
                        label = { Text("Password") },
                        modifier = Modifier.fillMaxWidth().onFocusChanged {
                            autofill?.apply {
                                if (it.isFocused) {
                                    requestAutofillForNode(autofillNode)
                                } else {
                                    cancelAutofillForNode(autofillNode)
                                }
                            }
                        },
                        value = passwordText,
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Password,
                            imeAction = ImeAction.Done
                        ),
                        keyboardActions = KeyboardActions(
                            onDone = {keyboardController?.hide()}
                        ),
                        maxLines = 1,
                        onValueChange = { passwordText = it },
                        visualTransformation = if (confirmPasswordVisibility) VisualTransformation.None else PasswordVisualTransformation(),
                        trailingIcon = {
                            IconButton(onClick = { confirmPasswordVisibility = !confirmPasswordVisibility }) {
                                Icon(
                                    painterResource(id = if (confirmPasswordVisibility) R.drawable.ic_visibility else R.drawable.ic_novisibility ),
                                    contentDescription = "Toggle password visibility"
                                )
                            }
                        },
                    )
                }
            }
        }
    }
}


@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun RegisterScreen() {
    var firstNameText by remember { mutableStateOf("") }
    var lastNameText by remember { mutableStateOf("") }
    var emailText by remember { mutableStateOf("") }
    var phoneText by remember { mutableStateOf("") }
    var passwordText by remember { mutableStateOf("") }
    var passwordVisibility by remember { mutableStateOf(false) }
    var confirmPasswordVisibility by remember { mutableStateOf(false) }
    val isError by remember { mutableStateOf(false) }
    var isConfirmError by remember { mutableStateOf(false) }
    var confirmPasswordText by remember { mutableStateOf("") }
    val coroutineScope = rememberCoroutineScope()
    val rowPadding = 10.dp
    val autofill = LocalAutofill.current
    val context = LocalContext.current
    val dataStore = StoreAuthToken(context)
    val keyboardController = LocalSoftwareKeyboardController.current
    Scaffold(
        floatingActionButton = {
            ExtendedFloatingActionButton(
                text = { Text(text = "REGISTER") },
                onClick = {
                    coroutineScope.launch {
                        if (!isError || !isConfirmError) {
                            val result =
                                accountPostRequest(
                                    "http://banking-app.mcnut.net/api/register", null, listOf(
                                        Pair("fName", firstNameText),
                                        Pair("lName", lastNameText),
                                        Pair("email", emailText.lowercase()),
                                        Pair("phone", phoneText),
                                        Pair("password", passwordText),
                                    )
                                )
                            when {
                                result.first -> {
                                    Toast.makeText(
                                        context,
                                        "Successfully Registered",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                    val loginResult =
                                        accountPostRequest("http://banking-app.mcnut.net/api/login", null, listOf(
                                            Pair("email", emailText.lowercase()),
                                            Pair("password", passwordText)
                                        ))
                                    when {
                                        loginResult.first -> {
                                            dataStore.saveAuthToken(result.second as String)
                                            Toast.makeText(
                                                context,
                                                "Successfully Logged In",
                                                Toast.LENGTH_SHORT
                                            ).show()
                                            context.startActivity(
                                                Intent(
                                                    context,
                                                    MainActivity::class.java
                                                )
                                            )
                                        }
                                        else -> {
                                            Toast.makeText(
                                                context,
                                                "Login Failed: ${result.second}",
                                                Toast.LENGTH_SHORT
                                            ).show()
                                        }
                                    }
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
                },
                icon = { Icon(Icons.Filled.Add, "")}
            )
        },
        modifier = Modifier.padding(end = 24.dp, start=16.dp)
    ) { _ ->
        Column(modifier = Modifier.padding(end = 24.dp, start=16.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    painterResource(id = R.drawable.ic_name),
                    contentDescription = "First Name",
                    modifier = Modifier.padding(end = 16.dp)
                )
                Autofill(
                    autofillTypes = listOf(AutofillType.PersonFirstName),
                    onFill = { firstNameText = it }
                ) { autofillNode ->
                    OutlinedTextField(
                        label = { Text("First Name") },
                        modifier = Modifier.fillMaxWidth().onFocusChanged {
                            autofill?.apply {
                                if (it.isFocused) {
                                    requestAutofillForNode(autofillNode)
                                } else {
                                    cancelAutofillForNode(autofillNode)
                                }
                            }
                        },
                        value = firstNameText,
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Text,
                            imeAction = ImeAction.Done
                        ),
                        keyboardActions = KeyboardActions(
                            onDone = {keyboardController?.hide()}
                        ),
                        maxLines = 1,
                        onValueChange = { firstNameText = it }
                    )
                }
            }
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(top=rowPadding)
            ) {
                Icon(
                    painterResource(id = R.drawable.ic_name),
                    contentDescription = "Last Name",
                    modifier = Modifier.padding(end = 16.dp)
                )
                Autofill(
                    autofillTypes = listOf(AutofillType.PersonLastName),
                    onFill = { lastNameText = it }
                ) { autofillNode ->
                    OutlinedTextField(
                        label = { Text("Last Name") },
                        modifier = Modifier.fillMaxWidth().onFocusChanged {
                            autofill?.apply {
                                if (it.isFocused) {
                                    requestAutofillForNode(autofillNode)
                                } else {
                                    cancelAutofillForNode(autofillNode)
                                }
                            }
                        },
                        value = lastNameText,
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Text,
                            imeAction = ImeAction.Done
                        ),
                        keyboardActions = KeyboardActions(
                            onDone = {keyboardController?.hide()}
                        ),
                        maxLines = 1,
                        onValueChange = { lastNameText = it }
                    )
                }
            }
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(top=rowPadding)
            ) {
                Icon(
                    painterResource(id = R.drawable.ic_email),
                    contentDescription = "Email Icon",
                    modifier = Modifier.padding(end = 16.dp)
                )
                Autofill(
                    autofillTypes = listOf(AutofillType.NewUsername),
                    onFill = { emailText = it }
                ) { autofillNode ->
                    OutlinedTextField(
                        label = { Text("Email") },
                        modifier = Modifier.fillMaxWidth().onFocusChanged {
                            autofill?.apply {
                                if (it.isFocused) {
                                    requestAutofillForNode(autofillNode)
                                } else {
                                    cancelAutofillForNode(autofillNode)
                                }
                            }
                        },
                        value = emailText,
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Text,
                            imeAction = ImeAction.Done
                        ),
                        keyboardActions = KeyboardActions(
                            onDone = {keyboardController?.hide()}
                        ),
                        maxLines = 1,
                        onValueChange = { emailText = it }
                    )
                }
            }
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(top=rowPadding)
            ) {
                Icon(
                    painterResource(id = R.drawable.ic_phone),
                    contentDescription = "Phone icon",
                    modifier = Modifier.padding(end = 16.dp)
                )
                Autofill(
                    autofillTypes = listOf(AutofillType.PhoneNumber),
                    onFill = { phoneText = it }
                ) { autofillNode ->
                    OutlinedTextField(
                        label = { Text("Phone") },
                        modifier = Modifier.fillMaxWidth().onFocusChanged {
                            autofill?.apply {
                                if (it.isFocused) {
                                    requestAutofillForNode(autofillNode)
                                } else {
                                    cancelAutofillForNode(autofillNode)
                                }
                            }
                        },
                        value = phoneText,
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Number,
                            imeAction = ImeAction.Done
                        ),
                        keyboardActions = KeyboardActions(
                            onDone = {keyboardController?.hide()}
                        ),
                        maxLines = 1,
                        onValueChange = { phoneText = it }
                    )
                }
            }
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(top=rowPadding)
            ) {
                Icon(
                    painterResource(id = R.drawable.ic_password),
                    contentDescription = "Money icon",
                    modifier = Modifier.padding(end = 16.dp)
                )
                Autofill(
                    autofillTypes = listOf(AutofillType.NewPassword),
                    onFill = { passwordText = it }
                ) { autofillNode ->
                    OutlinedTextField(
                        label = { Text("Password") },
                        modifier = Modifier.fillMaxWidth().onFocusChanged {
                            autofill?.apply {
                                if (it.isFocused) {
                                    requestAutofillForNode(autofillNode)
                                } else {
                                    cancelAutofillForNode(autofillNode)
                                }
                            }
                        },
                        value = passwordText,
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Text,
                            imeAction = ImeAction.Done
                        ),
                        keyboardActions = KeyboardActions(
                            onDone = {keyboardController?.hide()}
                        ),
                        maxLines = 1,
                        onValueChange = { passwordText = it },
                        visualTransformation = if (confirmPasswordVisibility) VisualTransformation.None else PasswordVisualTransformation(),
                        trailingIcon = {
                            IconButton(onClick = { confirmPasswordVisibility = !confirmPasswordVisibility }) {
                                Icon(
                                    painterResource(id = if (confirmPasswordVisibility) R.drawable.ic_visibility else R.drawable.ic_novisibility ),
                                    contentDescription = "Toggle password visibility"
                                )
                            }
                        },
                    )
                }
            }
            if (isError) {
                Text("Password must be at least 8 characters long", color= Color.Red, fontSize = 12.sp)
            }
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(top=rowPadding)
            ) {
                Icon(
                    painterResource(id = R.drawable.ic_password),
                    contentDescription = "Money icon",
                    modifier = Modifier.padding(end = 16.dp)
                )
                OutlinedTextField(
                    value = confirmPasswordText,
                    onValueChange = {
                        confirmPasswordText = it
                        isConfirmError = it != passwordText
                    },
                    label = { Text("Confirm Password") },
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Password,
                        imeAction = ImeAction.Done
                    ),
                    keyboardActions = KeyboardActions(
                        onDone = {keyboardController?.hide()}
                    ),
                    visualTransformation = if (passwordVisibility) VisualTransformation.None else PasswordVisualTransformation(),
                    trailingIcon = {
                        IconButton(onClick = { passwordVisibility = !passwordVisibility }) {
                            Icon(
                                painterResource(id = if (passwordVisibility) R.drawable.ic_visibility else R.drawable.ic_novisibility ),
                                contentDescription = "Toggle password visibility"
                            )
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                )
            }
            if (isConfirmError) {
                Text("passwords must be equal", color= Color.Red, fontSize = 12.sp)
            }
        }
    }
}

@ExperimentalComposeUiApi
@Composable
private fun Autofill(
    autofillTypes: List<AutofillType>,
    onFill: ((String) -> Unit),
    content: @Composable (AutofillNode) -> Unit
) {
    val autofillNode = AutofillNode(onFill = onFill, autofillTypes = autofillTypes)

    val autofillTree = LocalAutofillTree.current
    autofillTree += autofillNode

    Box(
        Modifier.onGloballyPositioned {
            autofillNode.boundingBox = it.boundsInWindow()
        }
    ) {
        content(autofillNode)
    }
}