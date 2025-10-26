package com.pwr_zpi.reservespotapp

import android.net.Uri
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController

@Composable
fun ScreenTest1(navController: NavHostController) {

    Column (
        Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(text = "Screen 1")

        var text by remember { mutableStateOf("") }
        TextField(
            value = text,
            onValueChange = {text = it},
            label = {Text("Enter text")}
        )
        Spacer(modifier = Modifier.height(16.dp))

        Button(onClick = {
            val encodedText = Uri.encode(text)
            navController.navigate("screentest2/$encodedText")
        }) {
            Text(text = "Screen 2")
        }
    }
}