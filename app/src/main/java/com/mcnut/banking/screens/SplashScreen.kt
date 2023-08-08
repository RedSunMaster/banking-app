package com.mcnut.banking.screens

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

@Composable
fun SplashScreen() {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        // Your loading animation or image here
        Column (horizontalAlignment = Alignment.CenterHorizontally){
            CircularProgressIndicator()
            Spacer(Modifier.height(8.dp))
            Text(text = "Connecting to server", modifier = Modifier.padding(top = 8.dp), fontWeight = FontWeight.Bold, color = Color.White)
        }
    }
}

