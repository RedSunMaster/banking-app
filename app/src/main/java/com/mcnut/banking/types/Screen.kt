import com.mcnut.banking.R

sealed class Screen(val route: String, val title: String, val icon: Int) {
    //Main Navigation Items
    object Balances : Screen("Balances", "Balances", R.drawable.ic_account_balances)
    object MoneyOwed : Screen("Money Owed", "Money Owed", R.drawable.ic_money_owed)
    object Transfer : Screen("Transfer", "Transfer", R.drawable.ic_transfer)

    //Navigation Items
    object Transactions : Screen("Transactions/{item}", "Transactions", R.drawable.ic_transactions)
    object Budget : Screen("Budget", "Budget", R.drawable.ic_budget)

    //Settings Items
    object Settings : Screen("Settings", "Settings", R.drawable.ic_settings)
    object ThemeSettings : Screen("Theme", "Theme", R.drawable.ic_theme)
    object CategorySettings : Screen("Categories", "Categories", R.drawable.ic_category)
    object ProfileSettings : Screen("Profile", "Profile", R.drawable.ic_profile)




    object ServerDown : Screen("Server Down", "Server Down", R.drawable.ic_no_connection)

}
