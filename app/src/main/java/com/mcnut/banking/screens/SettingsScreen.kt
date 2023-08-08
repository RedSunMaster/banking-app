package com.mcnut.banking.screens
import Screen
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.mcnut.banking.types.BankingInfo
import com.mcnut.banking.types.DatabaseInformation

val buttons = listOf(
    Screen.ThemeSettings,
    Screen.CategorySettings,
    Screen.ProfileSettings
)

@Composable
fun SettingsScreen(navController: NavHostController, state: DatabaseInformation, bankingInfo: BankingInfo) {
    Column(
        modifier = Modifier.padding(end = 24.dp, start = 16.dp)
    ) {
        buttons.forEach { button ->
            ListItem(modifier = Modifier.clip(RoundedCornerShape(20.dp)).clickable
            { navController.navigate(button.route)},
                headlineContent = {Text(button.title, fontWeight = FontWeight.Bold)},
                colors = ListItemDefaults.colors(
                    containerColor = Color.Black.copy(0.2f)
                ))
            Spacer(Modifier.height(8.dp))
        }
    }
}