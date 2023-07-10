import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.budgeting.R
import com.example.budgeting.ui.theme.BudgetingTheme

@Preview
@Composable
fun PurchaseScreen()
{
    BudgetingTheme (true) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(start = 16.dp, end = 16.dp)
        ) {
            Text(
                text = "Purchase",
                fontSize = 34.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier
                    .width(357.dp)
                    .height(45.dp)
            )
            Column {
                OutlinedTextField(
                    value = "",
                    onValueChange = {},
                    label = { Text("Date") },
                    trailingIcon = {
                        Icon(
                            painterResource(id = R.drawable.ic_calender),
                            contentDescription = null
                        )
                    }
                )
                OutlinedTextField(
                    value = "",
                    onValueChange = {},
                    label = { Text("Description") }
                )
            }
            FloatingActionButton(
                onClick = {},
                modifier = Modifier.padding(16.dp)
            ) {
                Icon(painterResource(id = R.drawable.ic_add), contentDescription = null)
            }
        }
    }
}
