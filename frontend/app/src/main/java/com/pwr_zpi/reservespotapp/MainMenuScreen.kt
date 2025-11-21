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
            RestaurantInfoCard(
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

                RestaurantInfoCard(
                    modifier = Modifier
                        .weight(1f)
                        .height(300.dp), // fixed width
                    recommendedList[recNum]
                )
                recNum += 1

                Spacer(modifier = Modifier.padding(4.dp))

                RestaurantInfoCard(
                    modifier = Modifier
                        .weight(1f) // dividing space in row
                        .height(300.dp), // fixed width
                    recommendedList[recNum]
                )
                recNum += 1
            }
        }

        while (recommendedList.size - recNum > 0) {
            RestaurantInfoCard(
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

fun fetchRecommendations(): List<RestaurantDto> {
    // TODO fetch from backend. This is a placeholder
    return listOf(
        RestaurantDto(
            id = 1L,
            ownerId = 10L,
            name = "Pasta Palace",
            address = "123 Italian St",
            city = "Rome",
            description = "Authentic Italian pasta and wines.",
            openingHours = mapOf("Mon" to "10:00–22:00", "Tue" to "10:00–22:00"),
            averageRating = 4.71,
            latitude = 41.9028,
            longitude = 12.4964,
            pic = "https://example.com/pasta.jpg",
            tableIds = setOf(1, 2, 3),
            reviewIds = setOf(101, 102),
            aiAnalysisIds = emptySet(),
            statisticIds = emptySet(),
            tagIds = setOf(5, 9),
            pictureIds = setOf(11, 12),
            views = 2300
        ),
        RestaurantDto(
            id = 2L,
            ownerId = 20L,
            name = "Sushi World",
            address = "99 Sakura Blvd",
            city = "Tokyo",
            description = "Fresh sushi prepared by top chefs.",
            openingHours = mapOf("Mon" to "11:00–23:00", "Tue" to "11:00–23:00"),
            averageRating = 4.58,
            latitude = 35.6762,
            longitude = 139.6503,
            pic = "https://example.com/sushi.jpg",
            tableIds = setOf(4, 5, 6),
            reviewIds = setOf(201),
            aiAnalysisIds = emptySet(),
            statisticIds = emptySet(),
            tagIds = setOf(3, 7),
            pictureIds = setOf(21, 22),
            views = 1800
        ),
        RestaurantDto(
            id = 3L,
            ownerId = 30L,
            name = "Burger Hub",
            address = "57 Grill Ave",
            city = "New York",
            description = "Juicy burgers and homemade fries.",
            openingHours = mapOf("Mon" to "09:00–21:00", "Tue" to "09:00–21:00"),
            averageRating = 4.20,
            latitude = 40.7128,
            longitude = -74.0060,
            pic = "https://example.com/burger.jpg",
            tableIds = setOf(7, 8),
            reviewIds = setOf(301, 302, 303),
            aiAnalysisIds = emptySet(),
            statisticIds = emptySet(),
            tagIds = setOf(2, 6),
            pictureIds = setOf(31, 32),
            views = 900
        ),
        RestaurantDto(
            id = 4L,
            ownerId = 40L,
            name = "Kebab Spot",
            address = "44 Spice Rd",
            city = "Istanbul",
            description = "Traditional Turkish kebabs with rich spices.",
            openingHours = mapOf("Mon" to "08:00–23:00", "Tue" to "08:00–23:00"),
            averageRating = 4.10,
            latitude = 41.0082,
            longitude = 28.9784,
            pic = "https://example.com/kebab.jpg",
            tableIds = setOf(9, 10),
            reviewIds = setOf(401),
            aiAnalysisIds = emptySet(),
            statisticIds = emptySet(),
            tagIds = setOf(4),
            pictureIds = setOf(41),
            views = 560
        )
    )
}