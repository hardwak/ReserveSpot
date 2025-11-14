package com.pwr_zpi.reservespotapp

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
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

    val recommendedList = fetchRecommendations()

    Column (
        modifier = Modifier
            .verticalScroll(rememberScrollState())
    ) {
        Text(
            text = "You might enjoy",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier
                .align(Alignment.CenterHorizontally)
                .padding(top = 16.dp)
        )

        var recNum = 0
        while (recommendedList.size - recNum >= 3) {
            InfoCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(300.dp)
                    .padding(horizontal = 16.dp, vertical = 8.dp)
                    .align(Alignment.CenterHorizontally),
                recommendedList[recNum]
            )
            recNum += 1

            Row(
                modifier = Modifier
                    .fillMaxWidth() // row fills full width
                    .padding(horizontal = 16.dp) // padding for section
            ) {

                InfoCard(
                    modifier = Modifier
                        .weight(1f)
                        .height(300.dp), // fixed width
                    recommendedList[recNum]
                )
                recNum += 1

                Spacer(modifier = Modifier.padding(4.dp))

                InfoCard(
                    modifier = Modifier
                        .weight(1f) // dividing space in row
                        .height(300.dp), // fixed width
                    recommendedList[recNum]
                )
                recNum += 1
            }
        }

        while (recommendedList.size - recNum > 0) {
            InfoCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(300.dp)
                    .padding(horizontal = 16.dp, vertical = 8.dp)
                    .align(Alignment.CenterHorizontally),
                recommendedList[recNum]
            )
            recNum += 1
        }

    }
}

fun fetchRecommendations(): List<RestaurantCardInfo> {
    // TODO fetch from backend. This is a placeholder
    return listOf(
        RestaurantCardInfo(
            restaurantName = "Pasta Palace",
            imageURL = "https://example.com/pasta.jpg",
            rating = 4.71f,
            views = 2300
        ),
        RestaurantCardInfo(
            restaurantName = "Sushi World",
            imageURL = "https://example.com/sushi.jpg",
            rating = 4.58f,
            views = 1800
        ),
        RestaurantCardInfo(
            restaurantName = "Burger Hub",
            imageURL = "https://example.com/burger.jpg",
            rating = 4.2f,
            views = 900
        ),
        RestaurantCardInfo(
            restaurantName = "Kebab Spot",
            imageURL = "https://example.com/kebab.jpg",
            rating = 4.1f,
            views = 560
        )
    )
}