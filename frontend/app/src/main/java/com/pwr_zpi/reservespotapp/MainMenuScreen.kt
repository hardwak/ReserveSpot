package com.pwr_zpi.reservespotapp

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainMenuScreen(navController: NavHostController) {
    Column {
        Text(
            text = "You might enjoy",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier
                .align(Alignment.CenterHorizontally)
                .padding(top = 16.dp)
        )

        InfoCard(300, width = 400, restaurantName = "Restauracja 1", rating = 4.72f, views = 523)

        Row {
            InfoCard(
                300,
                width = 190,
                restaurantName = "Restauracja 2",
                rating = 4.54f,
                views = 258
            )
            Spacer(modifier = Modifier.padding(4.dp))
            InfoCard(
                300,
                width = 190,
                restaurantName = "Restauracja 3",
                rating = 4.97f,
                views = 1032
            )
        }
    }
}