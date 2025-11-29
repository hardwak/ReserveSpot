package com.pwr_zpi.reservespotapp

import android.R.attr.onClick
import android.annotation.SuppressLint
import android.content.Context
import android.util.Log
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.pwr_zpi.reservespotapp.data.DataStoreManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@SuppressLint("SuspiciousIndentation")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainMenuScreen(navController: NavHostController) {

    val context = LocalContext.current
    var recommendations by remember { mutableStateOf<List<RestaurantDto>>(emptyList()) }
    var isLoading by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        isLoading = true
        recommendations = fetchRecommendations(context)
        Log.d("", recommendations.toString())
        isLoading = false
    }

    if (isLoading) {
        CircularProgressIndicator()
    } else {
        Column(
            modifier = Modifier
                .verticalScroll(rememberScrollState())
        )
        {
            Text(
                text = "You might enjoy",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier
                    .align(Alignment.CenterHorizontally)
                    .padding(top = 16.dp)
            )

        var recNum = 0
        while (recommendations.size - recNum >= 3) {
            val num = recNum
            RestaurantInfoCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(300.dp)
                    .padding(horizontal = 16.dp, vertical = 8.dp)
                    .align(Alignment.CenterHorizontally)
                .clickable(onClick = { navController.navigate("restaurantDetails/${recommendations[num].id}") }),
                recommendations[recNum]
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
                        .height(300.dp) // fixed width
                    .clickable(onClick = { navController.navigate("restaurantDetails/${recommendations[num + 1].id}") }),
                    recommendations[recNum]
                )
                recNum += 1

                    Spacer(modifier = Modifier.padding(4.dp))

                RestaurantInfoCard(
                    modifier = Modifier
                        .weight(1f) // dividing space in row
                        .height(300.dp)
                        .clickable(onClick = { navController.navigate("restaurantDetails/${recommendations[num + 2].id}") }),// fixed width
                    recommendations[recNum]
                )
                recNum += 1
            }
        }

        while (recommendations.size - recNum > 0) {
            val num = recNum
            RestaurantInfoCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(300.dp)
                    .padding(horizontal = 16.dp, vertical = 8.dp)
                    .align(Alignment.CenterHorizontally)
                    .clickable(onClick = { navController.navigate("restaurantDetails/${recommendations[num].id}") }),
                recommendations[recNum]
            )
            recNum += 1
        }

        }
    }
}

suspend fun fetchRecommendations(context: Context): List<RestaurantDto> {
    return withContext(Dispatchers.IO) {
        try {
            val dataStoreManager = DataStoreManager(context)
            val token = dataStoreManager.getBackendToken()

            if (token.isNullOrEmpty()) {
                Log.e("fetchFavourites", "No authentication token found")
                return@withContext emptyList()
            }

            val response = RetrofitClient.restaurantApi
                .getRecommendations("Bearer $token")

            if (response.isSuccessful) {
                response.body() ?: emptyList()
            } else {
                Log.e("fetchRecommendations", "Error: ${response.code()} - ${response.message()}")
                emptyList()
            }
        } catch (e: Exception) {
            Log.e("fetchRecommendations", "Exception while fetching recommendations", e)
            emptyList()
        }
    }
}