package com.pwr_zpi.reservespotapp

import android.content.Context
import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.PopupProperties
import androidx.navigation.NavHostController
import com.pwr_zpi.reservespotapp.data.DataStoreManager
import com.pwr_zpi.reservespotapp.ui.theme.RSRed
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainMenuScreen(navController: NavHostController) {

    // start
    val context = LocalContext.current
    var recommendations by remember { mutableStateOf<List<RestaurantDto>>(emptyList()) }
    var isLoading by remember { mutableStateOf(false) }

    val dataStoreManager = DataStoreManager(context)

    var cities by remember { mutableStateOf<List<String>>(emptyList()) }
    var selectedCity by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(Unit) {
        isLoading = true
        cities = getCities(context)
        Log.d("cities", cities.toString())
        if (cities.isNotEmpty()) {
            selectedCity = dataStoreManager.getCity()
            Log.d("selectedCity", selectedCity.toString())
        }
        isLoading = false
    }

    LaunchedEffect(selectedCity) {
        if (selectedCity != null) {
            isLoading = true
            recommendations = fetchRecommendations(context, selectedCity)
            dataStoreManager.saveCity(city = selectedCity)
            isLoading = false
            Log.d("recommendations", recommendations.toString())
        }
    }

    if (isLoading) {
        CircularProgressIndicator()
    } else {

        Box(modifier = Modifier.fillMaxSize()) {

            Column(
                modifier = Modifier
                    .verticalScroll(rememberScrollState())
            )
            {
//                Text(
//                    text = "You might enjoy",
//                    fontSize = 24.sp,
//                    fontWeight = FontWeight.Bold,
//                    modifier = Modifier
//                        .align(Alignment.CenterHorizontally)
//                        .padding(top = 16.dp)
//                )

                Spacer(modifier = Modifier.padding(40.dp))

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
                        info = recommendations[recNum]
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
                            info = recommendations[recNum]
                        )
                        recNum += 1

                        Spacer(modifier = Modifier.padding(4.dp))

                        RestaurantInfoCard(
                            modifier = Modifier
                                .weight(1f) // dividing space in row
                                .height(300.dp) // fixed width
                                .clickable(onClick = { navController.navigate("restaurantDetails/${recommendations[num + 2].id}") }),
                            info = recommendations[recNum]
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

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            CitySelector(
                cities = cities,
                selectedCity = selectedCity,
                onCitySelected = { selectedCity = it},
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.TopCenter)
                    .background(color = Color.Transparent, RoundedCornerShape(32.dp))
            )
        }
    }
}

@Composable
fun CitySelector(
    cities: List<String>,
    selectedCity: String?,
    onCitySelected: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var query by remember { mutableStateOf(selectedCity ?: "") }
    var expanded by remember { mutableStateOf(false) }

    val filteredCities = remember(query) {
        if (query.isBlank()) cities else cities.filter { it.contains(query, ignoreCase = true) }
    }

    val focusRequester = remember { FocusRequester() }

    Box(modifier = modifier) {
        TextField(
            value = query,
            onValueChange = {
                query = it
                expanded = true
            },
            modifier = Modifier
                .focusRequester(focusRequester)
                .heightIn(min = 48.dp)
                .align(Alignment.TopCenter)
                .background(Color.White, RoundedCornerShape(32.dp))
                .fillMaxWidth(),
            shape = RoundedCornerShape(32.dp), // Rounded edges
            label = { Text("Choose your city") },
            colors = TextFieldDefaults.colors(
                focusedTextColor = Color.Black,           // Text color when focused
                unfocusedTextColor = Color.Gray,          // Text color when not focused
                focusedContainerColor = Color.White,
                unfocusedContainerColor = Color.White,
                focusedIndicatorColor = Color.Transparent,
                unfocusedIndicatorColor = Color.Transparent,
                disabledIndicatorColor = Color.Transparent,
                focusedLabelColor = RSRed,
                unfocusedLabelColor = RSRed
            ),
            singleLine = true,
        )

        DropdownMenu(
            expanded = expanded && filteredCities.isNotEmpty(),
            onDismissRequest = { expanded = false },
            properties = PopupProperties(focusable = false),
            modifier = Modifier
                .fillMaxWidth()
                .background(Color.White, RoundedCornerShape(12.dp))
        ) {
            filteredCities.forEach { city ->
                DropdownMenuItem(
                    text = { Text(city) },
                    onClick = {
                        query = city
                        expanded = false
                        onCitySelected(city)
                    }
                )
            }
        }
    }
}



suspend fun fetchRecommendations(
    context: Context,
    city: String?
): List<RestaurantDto> {
    return withContext(Dispatchers.IO) {
        try {
            val dataStoreManager = DataStoreManager(context)
            val token = dataStoreManager.getBackendToken()

            if (token.isNullOrEmpty()) {
                Log.e("fetchFavourites", "No authentication token found")
                return@withContext emptyList()
            }

            val response = RetrofitClient.restaurantApi
                .getRecommendations("Bearer $token", city)

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

suspend fun getCities(context: Context): List<String> {
    return withContext(Dispatchers.IO) {
        try {
            val dataStoreManager = DataStoreManager(context)
            val token = dataStoreManager.getBackendToken()

            if (token.isNullOrEmpty()) {
                Log.e("getCities", "No authentication token found")
                return@withContext emptyList()
            }
            // Log.d("token", "Not null")

            val response = RetrofitClient.restaurantApi
                .getCities("Bearer $token")

            if (response.isSuccessful) {
                response.body() ?: emptyList()
            } else {
                Log.e("getCities", "Error: ${response.code()} - ${response.message()}")
                emptyList()
            }
        } catch (e: Exception) {
            Log.e("getCities", "Exception while fetching cities", e)
            emptyList()
        }
    }
}