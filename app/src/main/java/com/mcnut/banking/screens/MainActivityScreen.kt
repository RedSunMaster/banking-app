@file:OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3Api::class)

package com.mcnut.banking.screens

import Screen
import android.annotation.SuppressLint
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.NavType
import androidx.navigation.compose.*
import androidx.navigation.navArgument
import com.mcnut.banking.fragments.getBalances
import com.mcnut.banking.fragments.getCategories
import com.mcnut.banking.fragments.getMoneyOwed
import com.mcnut.banking.fragments.getTransactions
import com.mcnut.banking.fragments.updateData
import com.mcnut.banking.helpers.StoreAuthToken
import com.mcnut.banking.helpers.StoreCheckedCategories
import com.mcnut.banking.helpers.StoreDarkMode
import com.mcnut.banking.settings.CategorySettings
import com.mcnut.banking.settings.ProfileSettings
import com.mcnut.banking.settings.ThemeSettings
import com.mcnut.banking.types.BalanceItem
import com.mcnut.banking.types.BankingInfo
import com.mcnut.banking.types.CategoryItem
import com.mcnut.banking.types.Data
import com.mcnut.banking.types.DatabaseInformation
import com.mcnut.banking.types.OwedItem
import com.mcnut.banking.types.Transaction
import com.mcnut.banking.types.UserItem
import kotlinx.coroutines.launch
import okhttp3.Cache
import okhttp3.OkHttpClient
import java.io.File


@OptIn(ExperimentalMaterial3Api::class)
@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
fun MainActivityScreen(data: Data, authToken: String) {
    val navItems = listOf(
        Screen.Balances,
        Screen.MoneyOwed,
        Screen.Transfer
    )
    val drawerItems = listOf(
        Screen.Budget
    )

    val settingsItems = listOf(
        Screen.Settings,
    )

    val settingsRoutes = setOf(
        Screen.ThemeSettings.route,
        Screen.CategorySettings.route,
        Screen.ProfileSettings.route
    )



    val navController = rememberNavController()
    val currentRoute = currentRoute(navController)
    val currentTitle = when (currentRoute) {
        Screen.Balances.route -> Screen.Balances.title
        Screen.MoneyOwed.route -> Screen.MoneyOwed.title
        Screen.Transfer.route -> Screen.Transfer.title
        Screen.Transactions.route -> Screen.Transactions.title
        Screen.Budget.route -> Screen.Budget.title
        Screen.Settings.route -> Screen.Settings.title
        Screen.ThemeSettings.route -> Screen.ThemeSettings.title
        Screen.CategorySettings.route -> Screen.CategorySettings.title
        Screen.ProfileSettings.route -> Screen.ProfileSettings.title
        Screen.ServerDown.route -> Screen.ServerDown.title
        else -> null
    }
    val drawerState = rememberDrawerState(DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    var selectedItem by remember { mutableStateOf<Screen?>(null) }

    var categories by remember { mutableStateOf(listOf<CategoryItem>()) }
    var transactions by remember { mutableStateOf(listOf<Transaction>()) }
    var balanceItems by remember { mutableStateOf(listOf<BalanceItem>()) }
    var owedItems by remember { mutableStateOf(listOf<OwedItem>()) }
    var loggedInUser by remember { mutableStateOf(listOf<UserItem>())}

    val context = LocalContext.current
    val dataStore = StoreAuthToken(context)
    val storeDarkMode = StoreDarkMode(context)
    val storePieChart = StoreCheckedCategories(context)



    var categoriesUpdated by remember { mutableStateOf(false) }
    var owedUpdated by remember { mutableStateOf(false) }
    var transactionsUpdated by remember { mutableStateOf(false) }
    var balancesUpdated by remember { mutableStateOf(false) }
    var updateAll by remember { mutableStateOf(false) }

    var openProfileDialog by remember { mutableStateOf(false) }
    val state = DatabaseInformation(
        categoryUpdated = categoriesUpdated,
        owedUpdated = owedUpdated,
        transactionUpdated = transactionsUpdated,
        balancesUpdated = balancesUpdated,
        updateAll = updateAll,
        onCategoryUpdatedChange = { newCategoryUpdated -> categoriesUpdated = newCategoryUpdated },
        onOwedUpdatedChange = { newOwedUpdated -> owedUpdated = newOwedUpdated },
        onTransactionUpdatedChange = { newTransactionUpdated -> transactionsUpdated = newTransactionUpdated },
        onBalancesUpdatedChange = { newBalancesUpdated -> balancesUpdated = newBalancesUpdated },
        onUpdateAllChange = {newUpdateAll -> updateAll = newUpdateAll},
        navController = navController
    )

    val cacheSize = 10 * 1024 * 5096 // 10 MB
    val cacheDirectory = File(context.cacheDir, "http-cache")
    val cache = Cache(cacheDirectory, cacheSize.toLong())
    val client = OkHttpClient.Builder().cache(cache).build()


    LaunchedEffect(navController) {
        navController.addOnDestinationChangedListener { _, destination, _ ->
            selectedItem = navItems.find { it.route == destination.route }
        }
    }

    LaunchedEffect(Unit) {
        categories = data.categories
        balanceItems = data.balanceItems
        owedItems = data.owedItems
        loggedInUser = data.loggedInUser
        transactions = data.transactions
    }

    LaunchedEffect(updateAll) {
        if (updateAll) {
            val newData = updateData(client, authToken)
            categories = newData.categories
            balanceItems = newData.balanceItems
            owedItems = newData.owedItems
            loggedInUser = newData.loggedInUser
            transactions = newData.transactions
            updateAll = false
        }
    }

    LaunchedEffect(categoriesUpdated) {
        if (categoriesUpdated) {
            categories = getCategories(client, authToken)
            categoriesUpdated = false
        }
    }
    LaunchedEffect(balancesUpdated) {
        if (balancesUpdated) {
            balanceItems = getBalances(client, authToken)
            balancesUpdated = false
        }
    }
    LaunchedEffect(owedUpdated) {
        if (owedUpdated) {
            owedItems = getMoneyOwed(client, authToken)
            owedUpdated = false
        }
    }
    LaunchedEffect(transactionsUpdated) {
        if (transactionsUpdated) {
            transactions = getTransactions(client, authToken)
            transactionsUpdated = false
        }
    }
    ModalNavigationDrawer(
            drawerState = drawerState,
            drawerContent = {
                ModalDrawerSheet {
                    Spacer(Modifier.height(24.dp))
                    Text(
                        text = "Main",
                        style = MaterialTheme.typography.labelLarge,
                        modifier = Modifier.padding(start = 16.dp, top = 8.dp, bottom = 8.dp)
                    )
                    navItems.forEach { screen ->
                        NavigationDrawerItem(
                            icon = {
                                Icon(
                                    painterResource(id = screen.icon),
                                    contentDescription = null
                                )
                            },
                            label = { Text(screen.title) },
                            selected = screen == selectedItem,
                            onClick = {
                                scope.launch { drawerState.close() }
                                selectedItem = screen
                                navController.navigate(screen.route)
                            },
                            modifier = Modifier.padding(horizontal = 16.dp)
                        )
                    }
                    Spacer(Modifier.height(16.dp))
                    Divider(
                        thickness = 3.dp,
                        modifier = Modifier.padding(horizontal = 16.dp)
                    )
                    Spacer(Modifier.height(16.dp))
                    Text(
                        text = "Extras",
                        style = MaterialTheme.typography.labelLarge,
                        modifier = Modifier.padding(start = 16.dp, top = 8.dp, bottom = 8.dp)
                    )
                    drawerItems.forEach { screen ->
                        NavigationDrawerItem(
                            icon = {
                                Icon(
                                    painterResource(id = screen.icon),
                                    contentDescription = null
                                )
                            },
                            label = { Text(screen.title) },
                            selected = screen == selectedItem,
                            onClick = {
                                scope.launch { drawerState.close() }
                                selectedItem = screen
                                navController.navigate(screen.route)
                            },
                            modifier = Modifier.padding(vertical = 4.dp, horizontal = 16.dp)
                        )
                    }
                    Spacer(Modifier.height(16.dp))
                    Divider(
                        thickness = 3.dp,
                        modifier = Modifier.padding(horizontal = 16.dp)
                    )
                    Spacer(Modifier.height(16.dp))
                    Text(
                        text = "Settings",
                        style = MaterialTheme.typography.labelLarge,
                        modifier = Modifier.padding(start = 16.dp, top = 8.dp, bottom = 8.dp)
                    )
                    settingsItems.forEach { screen ->
                        NavigationDrawerItem(
                            icon = {
                                Icon(
                                    painterResource(id = screen.icon),
                                    contentDescription = null
                                )
                            },
                            label = { Text(screen.title) },
                            selected = screen == selectedItem,
                            onClick = {
                                scope.launch { drawerState.close() }
                                selectedItem = screen
                                navController.navigate(screen.route)
                            },
                            modifier = Modifier.padding(vertical = 4.dp, horizontal = 16.dp)
                        )
                    }
                }
            }
        ) {
            Scaffold(
                topBar = {
                    TopAppBar(
                        title = {
                            Text(
                                currentTitle ?: "",
                                fontSize = 30.sp,
                                fontWeight = FontWeight.Bold
                            )
                        },
                        navigationIcon = {
                            IconButton(onClick = { scope.launch { drawerState.open() } }) {
                                Icon(Icons.Filled.Menu, contentDescription = "Menu")
                            }
                        },
                        actions = {
                            IconButton(onClick = { state.onUpdateAllChange(true) }) {
                                Icon(Icons.Filled.Refresh, contentDescription = "Refresh")
                            }

                            IconButton(onClick = { navController.navigate(Screen.ProfileSettings.route) }) {
                                Icon(Icons.Filled.Person, contentDescription = "Profile")
                            }
                        }
                    )
                },
                bottomBar = {
                    NavigationBar {
                        navItems.forEach { screen ->
                            NavigationBarItem(
                                icon = {
                                    Icon(
                                        painterResource(id = screen.icon),
                                        contentDescription = null
                                    )
                                },
                                label = { Text(screen.title) },
                                selected = currentRoute == screen.route,
                                onClick = {
                                    navController.navigate(screen.route)
                                },
                            )
                        }
                    }
                },
            ) { contentPadding ->
                NavHost(
                    navController,
                    startDestination = Screen.Balances.route,
                    Modifier.padding(contentPadding)
                ) {
                    val bankingInfo = BankingInfo(
                        loggedInUser,
                        transactions,
                        categories,
                        balanceItems,
                        owedItems,
                        authToken,
                        client,
                        navController
                    )

                    composable(Screen.Balances.route) { AccountBalancesScreen(state, bankingInfo!!) }
                    composable(Screen.MoneyOwed.route) { MoneyOwed(state, bankingInfo!!) }
                    composable(Screen.Transfer.route) { TransferScreen(state, bankingInfo!!) }
                    composable(Screen.Transactions.route, arguments = listOf(navArgument("item") { type = NavType.StringType })) { backStackEntry -> AllTransactionsScreen(state, bankingInfo!!, backStackEntry) }
                    composable(Screen.Budget.route) { IncomeScreen(state, bankingInfo!!) }
                    composable(Screen.Settings.route) { SettingsScreen(navController, state, bankingInfo!!) }
                    composable(Screen.ThemeSettings.route) { ThemeSettings(navController) }
                    composable(Screen.CategorySettings.route) { CategorySettings(state, bankingInfo!!) }
                    composable(Screen.ProfileSettings.route) { ProfileSettings(state, bankingInfo!!) }

                    composable(Screen.ServerDown.route) { ServerDownScreen() }


                }
            }
    }
}
@Composable
fun currentRoute(navController: NavController): String? {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    return navBackStackEntry?.destination?.route
}

@Composable
fun ServerDownScreen() {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            CircularProgressIndicator()
            Spacer(modifier = Modifier.height(8.dp))
            Text(text = "Connecting to server...")
        }
    }
}
