package com.pwr_zpi.reservespotapp

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.pwr_zpi.reservespotapp.ui.theme.RSRed

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainMenuScreen(navController: NavHostController) {
    val navBarItems = listOf("home", "reservations", "restaurants", "favourites", "account")

    var selectedItem by rememberSaveable { mutableStateOf("home") }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row (
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.logo_red_outside_small),
                            contentDescription = "App Logo",
                            modifier = Modifier
                                .size(44.dp)
                                .padding(end = 12.dp)
                        )

                        Text(
                            text = "ReserveSpot",
                            fontSize = 32.sp,
                            fontWeight = FontWeight.Bold,
                        )
                    }
                },
            )
        },

        bottomBar = {
            NavigationBar (
                containerColor = RSRed
            ) {
                navBarItems.forEach {
                    item -> NavigationBarItem(
                    icon = {
                        Icon(
                            imageVector = when (item) {
                                "home" -> Icons.Default.Home
                                "reservations" -> Icons.Default.DateRange
                                "restaurants" -> Icons.Default.LocationOn
                                "favourites" -> Icons.Default.Favorite
                                "account" -> Icons.Default.AccountCircle
                                else -> Icons.Default.Close
                            },
                            contentDescription = item
                        )
                    },
                    label = { Text(
                        text = item.replaceFirstChar { it.uppercase() },
                        fontSize = 10.sp,
                        color = Color.White
                    ) },
                    selected = selectedItem == item,
                    onClick = {

                        selectedItem = item
                        when (item) {
                            "restaurants" -> navController.navigate("ChooseRestaurant")
                        }
                    },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = RSRed,
                        unselectedIconColor = Color.White,
                        indicatorColor = Color.White
                    )
                )
                }
            }
        },

    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
        ) {
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
                InfoCard(300, width = 190, restaurantName = "Restauracja 2", rating = 4.54f, views = 258)
                Spacer(modifier = Modifier.padding(4.dp))
                InfoCard(300, width = 190, restaurantName = "Restauracja 3", rating = 4.97f, views = 1032)
            }


        }
    }
}