package com.example.budgeting

import PurchaseScreen
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material.icons.*
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Text
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.res.painterResource
import androidx.navigation.NavController
import androidx.navigation.compose.*
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.tooling.preview.Preview
import com.example.budgeting.ui.theme.BudgetingTheme

@Preview
@Composable
fun MainActivityScreen() {
    val navItems = listOf(
        Screen.Balances,
        Screen.Purchase,
        Screen.MoneyOwed,
        Screen.Transfer
    )
    val navController = rememberNavController()
    val currentRoute = currentRoute(navController)
    BudgetingTheme (true) {
        Scaffold(
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
                                navController.navigate(screen.route) {
                                    popUpTo(navController.graph.startDestinationId)
                                    launchSingleTop = true
                                }
                            }
                        )
                    }
                }
            }
        ) { innerPadding ->
            NavHost(
                navController,
                startDestination = Screen.Balances.route,
                Modifier.padding(innerPadding)
            ) {
                composable(Screen.Balances.route) { AccountBalancesScreen() }
                composable(Screen.Purchase.route) { PurchaseScreen() }
                composable(Screen.MoneyOwed.route) { }
                composable(Screen.Transfer.route) { }
            }
        }
    }
}

sealed class Screen(val route: String, val title: String, val icon: Int) {
    object Balances : Screen("balances", "Balances", R.drawable.account_balance_fill0_wght500_grad0_opsz48)
    object Purchase : Screen("purchase", "Purchase", R.drawable.round_add_shopping_cart_24)
    object MoneyOwed : Screen("money_owed", "Money Owed", R.drawable.round_hourglass_top_24)
    object Transfer : Screen("transfer", "Transfer", R.drawable.round_import_export_24)
}

@Composable
fun currentRoute(navController: NavController): String? {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    return navBackStackEntry?.destination?.route
}
